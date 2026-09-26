package com.campusconnect.ai.service;

import com.campusconnect.ai.exception.GeminiQuotaExceededException;
import com.campusconnect.ai.exception.GeminiRateLimitedException;
import com.campusconnect.ai.exception.GeminiServiceUnavailableException;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service encapsulating AI assistant interaction powered by LangChain4j with Gemini as PRIMARY
 * and Groq as SECONDARY/FALLBACK provider.
 */
@Service
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);

    private final ChatLanguageModel geminiModel;
    private final ChatLanguageModel groqModel;

    @Value("${gemini.model.name:gemini-3.6-flash}")
    private String geminiModelName;

    @Value("${groq.model.name:openai/gpt-oss-20b}")
    private String groqModelName;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 1000L;

    public AiAssistantService(@Qualifier("geminiChatLanguageModel") @Autowired(required = false) ChatLanguageModel geminiModel) {
        this(geminiModel, null);
    }

    @Autowired
    public AiAssistantService(
            @Qualifier("geminiChatLanguageModel") @Autowired(required = false) ChatLanguageModel geminiModel,
            @Qualifier("groqChatLanguageModel") @Autowired(required = false) ChatLanguageModel groqModel) {
        this.geminiModel = geminiModel;
        this.groqModel = groqModel;
    }

    /**
     * Generates an AI response for the user prompt using Gemini as primary provider with retry logic.
     * If Gemini fails due to temporary 503/service-unavailable or rate-limit/quota errors, falls back to Groq once.
     * Does NOT fallback for invalid API-key/configuration/authentication errors.
     *
     * @param userPrompt The input message prompt
     * @return AI generated text response
     */
    public String generateResponse(String userPrompt) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            throw new IllegalArgumentException("User message cannot be blank.");
        }

        if (geminiModel == null) {
            log.error("Attempted AI chat generation but Gemini model is null (GEMINI_API_KEY is unconfigured or invalid).");
            throw new IllegalStateException("Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable.");
        }

        String prompt = userPrompt.trim();
        int attempts = 0;
        long backoffMs = INITIAL_BACKOFF_MS;

        while (true) {
            attempts++;
            try {
                log.info("Sending prompt to Gemini model ({}) via LangChain4j (attempt {}/{})...", geminiModelName, attempts, MAX_RETRIES + 1);
                String response = geminiModel.generate(prompt);
                log.info("Successfully received response from Gemini on attempt {}.", attempts);
                return response;
            } catch (Exception e) {
                if (isAuthOrConfigurationError(e)) {
                    log.error("Non-transient Gemini authentication/configuration error encountered: {}. Will not retry or fallback to Groq.", e.getMessage());
                    throw new RuntimeException("Gemini API communication failure: [" + e.getClass().getSimpleName() + "] " + e.getMessage(), e);
                }

                if (isDailyQuotaError(e)) {
                    log.error("Gemini API daily quota exhausted (429 QUOTA_EXHAUSTED). Attempting Groq fallback once...");
                    return tryGroqFallback(prompt, new GeminiQuotaExceededException("The Gemini AI daily quota has been exceeded. Please wait until the daily quota resets or try again later.", e));
                }

                log.error("Gemini API error on attempt {}/{}: class={}, message={}", attempts, MAX_RETRIES + 1, e.getClass().getName(), e.getMessage());

                if (attempts <= MAX_RETRIES && isTransientError(e)) {
                    log.warn("Transient Gemini API error detected (RPM 429/503/UNAVAILABLE/Timeout). Retrying in {} ms (attempt {}/{})...", backoffMs, attempts, MAX_RETRIES);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("AI request interrupted during retry backoff.", ie);
                    }
                    backoffMs *= 2;
                } else {
                    if (isRateLimitedError(e)) {
                        log.warn("Gemini per-minute rate limit exhausted after retries. Attempting Groq fallback once...");
                        return tryGroqFallback(prompt, new GeminiRateLimitedException("Gemini AI rate limit exceeded (requests per minute). Please wait a few seconds before trying again.", e));
                    }
                    if (isServiceUnavailableError(e)) {
                        log.warn("Gemini service unavailable after retries. Attempting Groq fallback once...");
                        return tryGroqFallback(prompt, new GeminiServiceUnavailableException("Gemini AI service is temporarily unavailable. Please try again shortly.", e));
                    }

                    log.error("Exhausted retries or non-transient Gemini API error encountered. Will not fallback.");
                    throw new RuntimeException("Gemini API communication failure: [" + e.getClass().getSimpleName() + "] " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Attempts to call the secondary/fallback Groq model once if Gemini failed due to transient/rate-limit/503 error.
     *
     * @param prompt The input message prompt
     * @param geminiException The original Gemini exception triggering fallback
     * @return AI response from Groq fallback model
     */
    private String tryGroqFallback(String prompt, RuntimeException geminiException) {
        if (groqModel == null) {
            log.warn("Gemini encountered transient/rate-limit error, but Groq fallback model is not configured (GROQ_API_KEY is missing). Rethrowing primary Gemini exception.");
            throw geminiException;
        }

        try {
            log.info("Sending prompt to Groq fallback model ({}) via LangChain4j (1 attempt)...", groqModelName);
            String response = groqModel.generate(prompt);
            log.info("Successfully received fallback response from Groq model ({}).", groqModelName);
            return response;
        } catch (Exception groqException) {
            log.error("Groq fallback attempt failed: class={}, message={}", groqException.getClass().getName(), groqException.getMessage());
            throw new RuntimeException("Both Gemini and Groq AI providers failed. Gemini error: " + geminiException.getMessage() + " | Groq error: " + groqException.getMessage(), groqException);
        }
    }

    /**
     * Checks if the exception explicitly represents an invalid API key, authentication, or configuration error.
     */
    public boolean isAuthOrConfigurationError(Throwable t) {
        Throwable curr = t;
        while (curr != null) {
            String msg = curr.getMessage() != null ? curr.getMessage().toLowerCase() : "";
            if (msg.contains("invalid_api_key") || msg.contains("api_key_invalid") ||
                msg.contains("unauthorized") || msg.contains("forbidden") ||
                msg.contains("permission_denied") || msg.contains("401") ||
                msg.contains("403") || msg.contains("invalid argument") ||
                msg.contains("cannot be blank")) {
                return true;
            }
            curr = curr.getCause();
        }
        return false;
    }

    /**
     * Checks if the exception explicitly represents a Gemini daily quota exhaustion error.
     */
    public boolean isDailyQuotaError(Throwable t) {
        Throwable curr = t;
        while (curr != null) {
            String msg = curr.getMessage() != null ? curr.getMessage().toLowerCase() : "";
            if (msg.contains("daily_quota") ||
                msg.contains("daily quota") ||
                msg.contains("quota_exceeded") ||
                msg.contains("quota exceeded") ||
                msg.contains("free_tier_requests") ||
                msg.contains("free-tier daily quota") ||
                msg.contains("free tier daily quota") ||
                msg.contains("free_tier_daily_quota") ||
                msg.contains("free tier quota") ||
                msg.contains("requests per day") ||
                msg.contains("requests_per_day") ||
                msg.contains("per day") ||
                msg.contains("exceeded your current quota") ||
                msg.contains("exceeded daily quota") ||
                msg.contains("daily limit")) {
                return true;
            }
            curr = curr.getCause();
        }
        return false;
    }

    /**
     * Checks if the exception represents a Gemini per-minute rate limit (RPM / short-term 429) error.
     */
    public boolean isRateLimitedError(Throwable t) {
        if (isDailyQuotaError(t)) {
            return false;
        }

        Throwable curr = t;
        while (curr != null) {
            String msg = curr.getMessage() != null ? curr.getMessage().toLowerCase() : "";
            if (msg.contains("rate limit") ||
                msg.contains("rate_limit") ||
                msg.contains("requests per minute") ||
                msg.contains("requests_per_minute") ||
                msg.contains("rpm") ||
                msg.contains("too many requests") ||
                msg.contains("429") ||
                msg.contains("resource_exhausted")) {
                return true;
            }
            curr = curr.getCause();
        }
        return false;
    }

    /**
     * Checks if the exception represents a temporary service unavailable / 503 error.
     */
    public boolean isServiceUnavailableError(Throwable t) {
        if (isDailyQuotaError(t) || isRateLimitedError(t)) {
            return false;
        }

        Throwable curr = t;
        while (curr != null) {
            String className = curr.getClass().getName().toLowerCase();
            String msg = curr.getMessage() != null ? curr.getMessage().toLowerCase() : "";

            if (className.contains("timeout") || className.contains("socket") || className.contains("ioexception") || className.contains("connect")) {
                return true;
            }

            if (msg.contains("503") ||
                msg.contains("unavailable") ||
                msg.contains("service unavailable") ||
                msg.contains("high demand") ||
                msg.contains("temporarily unavailable") ||
                msg.contains("overloaded") ||
                msg.contains("server error") ||
                msg.contains("timeout")) {
                return true;
            }

            curr = curr.getCause();
        }
        return false;
    }

    /**
     * Checks if the exception represents a transient service error suitable for retry (RPM rate limits, 503, UNAVAILABLE, timeouts).
     */
    public boolean isTransientError(Throwable t) {
        if (isAuthOrConfigurationError(t) || isDailyQuotaError(t)) {
            return false;
        }

        return isRateLimitedError(t) || isServiceUnavailableError(t);
    }

    /**
     * Checks if primary Gemini API key and ChatLanguageModel are properly configured.
     *
     * @return true if initialized, false otherwise
     */
    public boolean isApiKeyConfigured() {
        return geminiModel != null;
    }

    /**
     * Checks if fallback Groq API key and ChatLanguageModel are properly configured.
     *
     * @return true if initialized, false otherwise
     */
    public boolean isGroqConfigured() {
        return groqModel != null;
    }

    /**
     * Gets primary model name.
     *
     * @return Gemini model name string
     */
    public String getModelName() {
        return geminiModelName;
    }

    /**
     * Gets fallback Groq model name.
     *
     * @return Groq model name string
     */
    public String getGroqModelName() {
        return groqModelName;
    }
}
