package com.campusconnect.ai;

import com.campusconnect.ai.exception.GeminiQuotaExceededException;
import com.campusconnect.ai.exception.GeminiRateLimitedException;
import com.campusconnect.ai.exception.GeminiServiceUnavailableException;
import com.campusconnect.ai.service.AiAssistantService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock
    private ChatLanguageModel geminiChatLanguageModel;

    @Mock
    private ChatLanguageModel groqChatLanguageModel;

    private AiAssistantService aiAssistantService;

    @BeforeEach
    void setUp() {
        aiAssistantService = new AiAssistantService(geminiChatLanguageModel, groqChatLanguageModel);
    }

    @Test
    @DisplayName("Should successfully generate response from Gemini primary provider")
    void testGenerateResponse_GeminiSuccess() {
        when(geminiChatLanguageModel.generate("Hello Gemini")).thenReturn("Hello! How can I assist you with Campus Connect today?");

        String response = aiAssistantService.generateResponse("Hello Gemini");

        assertNotNull(response);
        assertEquals("Hello! How can I assist you with Campus Connect today?", response);
        verify(geminiChatLanguageModel, times(1)).generate("Hello Gemini");
        verifyNoInteractions(groqChatLanguageModel);
    }

    @Test
    @DisplayName("Should fallback to Groq when Gemini fails with 503 service unavailable after retries")
    void testGenerateResponse_GeminiFailure_GroqFallbackSuccess() {
        when(geminiChatLanguageModel.generate(anyString()))
                .thenThrow(new RuntimeException("503 Service Unavailable: High demand on Gemini model"));
        when(groqChatLanguageModel.generate("Test prompt")).thenReturn("Fallback response from Groq GPT-OSS 20B model!");

        String response = aiAssistantService.generateResponse("Test prompt");

        assertNotNull(response);
        assertEquals("Fallback response from Groq GPT-OSS 20B model!", response);
        verify(geminiChatLanguageModel, times(4)).generate("Test prompt");
        verify(groqChatLanguageModel, times(1)).generate("Test prompt");
    }

    @Test
    @DisplayName("Should fallback to Groq on Gemini daily quota exhaustion and return Groq response")
    void testGenerateResponse_GeminiQuotaExhausted_GroqFallbackSuccess() {
        when(geminiChatLanguageModel.generate(anyString()))
                .thenThrow(new RuntimeException("RESOURCE_EXHAUSTED (code 429) Quota exceeded for free_tier_requests"));
        when(groqChatLanguageModel.generate("Campus query")).thenReturn("Groq generated response after Gemini quota limit.");

        String response = aiAssistantService.generateResponse("Campus query");

        assertNotNull(response);
        assertEquals("Groq generated response after Gemini quota limit.", response);
        verify(geminiChatLanguageModel, times(1)).generate("Campus query");
        verify(groqChatLanguageModel, times(1)).generate("Campus query");
    }

    @Test
    @DisplayName("Should throw Exception when both Gemini and Groq providers fail")
    void testGenerateResponse_BothProvidersFail() {
        when(geminiChatLanguageModel.generate(anyString()))
                .thenThrow(new RuntimeException("503 Service Unavailable"));
        when(groqChatLanguageModel.generate(anyString()))
                .thenThrow(new RuntimeException("500 Groq Internal Server Error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> aiAssistantService.generateResponse("Test prompt")
        );

        assertTrue(exception.getMessage().contains("Both Gemini and Groq AI providers failed"));
        verify(geminiChatLanguageModel, times(4)).generate("Test prompt");
        verify(groqChatLanguageModel, times(1)).generate("Test prompt");
    }

    @Test
    @DisplayName("Should rethrow Gemini exception when Gemini fails with transient error and Groq is unconfigured")
    void testGenerateResponse_MissingGroqConfig_RethrowsGeminiException() {
        AiAssistantService serviceWithoutGroq = new AiAssistantService(geminiChatLanguageModel, null);
        when(geminiChatLanguageModel.generate(anyString()))
                .thenThrow(new RuntimeException("503 Service Unavailable"));

        GeminiServiceUnavailableException exception = assertThrows(
                GeminiServiceUnavailableException.class,
                () -> serviceWithoutGroq.generateResponse("Test prompt")
        );

        assertTrue(exception.getMessage().contains("service is temporarily unavailable"));
        verify(geminiChatLanguageModel, times(4)).generate("Test prompt");
    }

    @Test
    @DisplayName("Should not fallback to Groq when Gemini fails due to non-transient invalid API key / authentication error")
    void testGenerateResponse_AuthError_NoFallback() {
        when(geminiChatLanguageModel.generate(anyString()))
                .thenThrow(new RuntimeException("401 Unauthorized: invalid_api_key"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> aiAssistantService.generateResponse("Test prompt")
        );

        assertTrue(exception.getMessage().contains("Gemini API communication failure"));
        verify(geminiChatLanguageModel, times(1)).generate("Test prompt");
        verifyNoInteractions(groqChatLanguageModel);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user prompt is blank")
    void testGenerateResponse_BlankPrompt() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiAssistantService.generateResponse("   ")
        );

        assertTrue(exception.getMessage().contains("User message cannot be blank"));
        verifyNoInteractions(geminiChatLanguageModel);
        verifyNoInteractions(groqChatLanguageModel);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when Gemini ChatLanguageModel is null")
    void testGenerateResponse_NullModel() {
        AiAssistantService unconfiguredService = new AiAssistantService(null, null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> unconfiguredService.generateResponse("Test prompt")
        );

        assertTrue(exception.getMessage().contains("Gemini API key is not configured"));
    }

    @Test
    @DisplayName("Should correctly classify auth, daily quota, RPM rate limit, and 503 errors")
    void testClassificationMethods() {
        Throwable dailyQuotaErr = new RuntimeException("Quota exceeded for free_tier_requests");
        Throwable rpmErr = new RuntimeException("429 Too Many Requests: Rate limit exceeded for requests per minute");
        Throwable unavailableErr = new RuntimeException("503 Service Unavailable");
        Throwable authErr = new RuntimeException("401 Unauthorized: invalid_api_key");

        assertTrue(aiAssistantService.isDailyQuotaError(dailyQuotaErr));
        assertFalse(aiAssistantService.isRateLimitedError(dailyQuotaErr));

        assertFalse(aiAssistantService.isDailyQuotaError(rpmErr));
        assertTrue(aiAssistantService.isRateLimitedError(rpmErr));

        assertTrue(aiAssistantService.isServiceUnavailableError(unavailableErr));

        assertTrue(aiAssistantService.isAuthOrConfigurationError(authErr));
        assertFalse(aiAssistantService.isTransientError(authErr));
    }

    @Test
    @DisplayName("Should correctly report API key configuration states")
    void testIsApiKeyConfigured() {
        assertTrue(aiAssistantService.isApiKeyConfigured());
        assertTrue(aiAssistantService.isGroqConfigured());

        AiAssistantService unconfiguredService = new AiAssistantService(null, null);
        assertFalse(unconfiguredService.isApiKeyConfigured());
        assertFalse(unconfiguredService.isGroqConfigured());
    }
}
