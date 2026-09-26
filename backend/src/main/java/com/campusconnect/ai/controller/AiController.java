package com.campusconnect.ai.controller;

import com.campusconnect.ai.dto.AiChatRequest;
import com.campusconnect.ai.dto.AiChatResponse;
import com.campusconnect.ai.dto.AiHealthResponse;
import com.campusconnect.ai.exception.GeminiQuotaExceededException;
import com.campusconnect.ai.exception.GeminiRateLimitedException;
import com.campusconnect.ai.exception.GeminiServiceUnavailableException;
import com.campusconnect.ai.service.AiAssistantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Controller exposing REST APIs for AI Foundation (Gemini + LangChain4j).
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final AiAssistantService aiAssistantService;

    public AiController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /**
     * GET /api/ai/health
     * Health check endpoint to verify AI module initialization and Gemini API key presence.
     */
    @GetMapping("/health")
    public ResponseEntity<AiHealthResponse> checkAiHealth() {
        boolean configured = aiAssistantService.isApiKeyConfigured();
        AiHealthResponse response = AiHealthResponse.builder()
                .status(configured ? "UP" : "DEGRADED")
                .apiKeyConfigured(configured)
                .model(aiAssistantService.getModelName())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/ai/chat
     * Accepts user prompt message and returns Gemini generated response.
     */
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        try {
            String aiResponseText = aiAssistantService.generateResponse(request.getMessage());
            AiChatResponse response = AiChatResponse.builder()
                    .response(aiResponseText)
                    .status("SUCCESS")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            AiChatResponse response = AiChatResponse.builder()
                    .response(e.getMessage())
                    .status("UNCONFIGURED")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        } catch (IllegalArgumentException e) {
            AiChatResponse response = AiChatResponse.builder()
                    .response(e.getMessage())
                    .status("BAD_REQUEST")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (GeminiQuotaExceededException e) {
            log.warn("Gemini daily quota limit reached: {}", e.getMessage());
            AiChatResponse response = AiChatResponse.builder()
                    .response(e.getMessage())
                    .status("QUOTA_EXHAUSTED")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        } catch (GeminiRateLimitedException e) {
            log.warn("Gemini per-minute rate limit reached: {}", e.getMessage());
            AiChatResponse response = AiChatResponse.builder()
                    .response(e.getMessage())
                    .status("RATE_LIMITED")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        } catch (GeminiServiceUnavailableException e) {
            log.warn("Gemini service unavailable: {}", e.getMessage());
            AiChatResponse response = AiChatResponse.builder()
                    .response(e.getMessage())
                    .status("SERVICE_UNAVAILABLE")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        } catch (Exception e) {
            log.error("AI Controller error: class={}, message={}", e.getClass().getName(), e.getMessage(), e);
            HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            String statusString = "ERROR";

            if (aiAssistantService.isDailyQuotaError(e)) {
                httpStatus = HttpStatus.TOO_MANY_REQUESTS;
                statusString = "QUOTA_EXHAUSTED";
            } else if (aiAssistantService.isRateLimitedError(e)) {
                httpStatus = HttpStatus.TOO_MANY_REQUESTS;
                statusString = "RATE_LIMITED";
            } else if (aiAssistantService.isServiceUnavailableError(e)) {
                httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
                statusString = "SERVICE_UNAVAILABLE";
            }

            AiChatResponse response = AiChatResponse.builder()
                    .response("Error processing request: " + e.getMessage())
                    .status(statusString)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(httpStatus).body(response);
        }
    }
}
