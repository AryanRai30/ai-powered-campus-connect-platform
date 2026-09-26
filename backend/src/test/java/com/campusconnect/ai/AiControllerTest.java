package com.campusconnect.ai;

import com.campusconnect.ai.controller.AiController;
import com.campusconnect.ai.dto.AiChatRequest;
import com.campusconnect.ai.exception.GeminiQuotaExceededException;
import com.campusconnect.ai.exception.GeminiRateLimitedException;
import com.campusconnect.ai.exception.GeminiServiceUnavailableException;
import com.campusconnect.ai.service.AiAssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AiAssistantService aiAssistantService;

    @InjectMocks
    private AiController aiController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiController).build();
    }

    @Test
    @DisplayName("GET /api/ai/health - Should return 200 OK with health details")
    void testCheckAiHealth() throws Exception {
        when(aiAssistantService.isApiKeyConfigured()).thenReturn(true);
        when(aiAssistantService.getModelName()).thenReturn("gemini-3.6-flash");

        mockMvc.perform(get("/api/ai/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.apiKeyConfigured").value(true))
                .andExpect(jsonPath("$.model").value("gemini-3.6-flash"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /api/ai/chat - Should return 200 OK with AI response on valid request")
    void testChat_Success() throws Exception {
        AiChatRequest request = new AiChatRequest("What events are happening on campus?");
        when(aiAssistantService.generateResponse(anyString())).thenReturn("Here are the upcoming tech workshops and club events!");

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Here are the upcoming tech workshops and club events!"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /api/ai/chat - Should return 503 Service Unavailable when API key is missing")
    void testChat_Unconfigured() throws Exception {
        AiChatRequest request = new AiChatRequest("Hello Gemini");
        when(aiAssistantService.generateResponse(anyString()))
                .thenThrow(new IllegalStateException("Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable."));

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("UNCONFIGURED"))
                .andExpect(jsonPath("$.response").value("Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable."));
    }

    @Test
    @DisplayName("POST /api/ai/chat - Should return 429 Too Many Requests with QUOTA_EXHAUSTED status when Gemini daily quota is exhausted")
    void testChat_QuotaExceeded() throws Exception {
        AiChatRequest request = new AiChatRequest("What is Java?");
        when(aiAssistantService.generateResponse(anyString()))
                .thenThrow(new GeminiQuotaExceededException("The Gemini AI daily quota has been exceeded. Please wait until the daily quota resets or try again later."));

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("QUOTA_EXHAUSTED"))
                .andExpect(jsonPath("$.response").value("The Gemini AI daily quota has been exceeded. Please wait until the daily quota resets or try again later."));
    }

    @Test
    @DisplayName("POST /api/ai/chat - Should return 429 Too Many Requests with RATE_LIMITED status when Gemini per-minute rate limit is reached")
    void testChat_RateLimited() throws Exception {
        AiChatRequest request = new AiChatRequest("Explain quantum computing");
        when(aiAssistantService.generateResponse(anyString()))
                .thenThrow(new GeminiRateLimitedException("Gemini AI rate limit exceeded (requests per minute). Please wait a few seconds before trying again."));

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.response").value("Gemini AI rate limit exceeded (requests per minute). Please wait a few seconds before trying again."));
    }

    @Test
    @DisplayName("POST /api/ai/chat - Should return 503 Service Unavailable with SERVICE_UNAVAILABLE status when Gemini is overloaded")
    void testChat_ServiceUnavailable() throws Exception {
        AiChatRequest request = new AiChatRequest("Help me build a project");
        when(aiAssistantService.generateResponse(anyString()))
                .thenThrow(new GeminiServiceUnavailableException("Gemini AI service is temporarily unavailable. Please try again shortly."));

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("SERVICE_UNAVAILABLE"))
                .andExpect(jsonPath("$.response").value("Gemini AI service is temporarily unavailable. Please try again shortly."));
    }

    @Test
    @DisplayName("POST /api/ai/chat - Should classify generic daily quota exception as QUOTA_EXHAUSTED 429")
    void testChat_UnhandledException_DailyQuota() throws Exception {
        AiChatRequest request = new AiChatRequest("Test prompt");
        RuntimeException dailyErr = new RuntimeException("429 Quota exceeded for free_tier_requests");

        when(aiAssistantService.generateResponse(anyString())).thenThrow(dailyErr);
        when(aiAssistantService.isDailyQuotaError(any())).thenReturn(true);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("QUOTA_EXHAUSTED"));
    }

    @Test
    @DisplayName("POST /api/ai/chat - Should classify generic rate limit exception as RATE_LIMITED 429")
    void testChat_UnhandledException_RateLimited() throws Exception {
        AiChatRequest request = new AiChatRequest("Test prompt");
        RuntimeException rpmErr = new RuntimeException("429 Rate limit reached");

        when(aiAssistantService.generateResponse(anyString())).thenThrow(rpmErr);
        when(aiAssistantService.isDailyQuotaError(any())).thenReturn(false);
        when(aiAssistantService.isRateLimitedError(any())).thenReturn(true);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("RATE_LIMITED"));
    }

    @Test
    @DisplayName("POST /api/ai/chat - Should classify generic 503 exception as SERVICE_UNAVAILABLE 503")
    void testChat_UnhandledException_ServiceUnavailable() throws Exception {
        AiChatRequest request = new AiChatRequest("Test prompt");
        RuntimeException unavailableErr = new RuntimeException("503 Service Unavailable");

        when(aiAssistantService.generateResponse(anyString())).thenThrow(unavailableErr);
        when(aiAssistantService.isDailyQuotaError(any())).thenReturn(false);
        when(aiAssistantService.isRateLimitedError(any())).thenReturn(false);
        when(aiAssistantService.isServiceUnavailableError(any())).thenReturn(true);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("SERVICE_UNAVAILABLE"));
    }

    @Test
    @DisplayName("POST /api/ai/chat - Should return 400 Bad Request when message is blank")
    void testChat_BlankMessage() throws Exception {
        AiChatRequest request = new AiChatRequest("   ");

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
