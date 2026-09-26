package com.campusconnect.ai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for LangChain4j Google AI Gemini Integration.
 * Configures Primary ChatLanguageModel using GEMINI_API_KEY environment variable or properties.
 */
@Configuration
public class GeminiConfig {

    private static final Logger log = LoggerFactory.getLogger(GeminiConfig.class);

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String apiKey;

    @Value("${gemini.model.name:gemini-3.6-flash}")
    private String modelName;

    @Value("${gemini.temperature:0.7}")
    private Double temperature;

    @Bean(name = "geminiChatLanguageModel")
    @Primary
    public ChatLanguageModel chatLanguageModel() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("GEMINI_API_KEY is not configured or empty. ChatLanguageModel bean will not be initialized.");
            return null;
        }

        log.info("Initializing LangChain4j GoogleAiGeminiChatModel with model: {}", modelName);
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey.trim())
                .modelName(modelName.trim())
                .temperature(temperature)
                .timeout(java.time.Duration.ofSeconds(60))
                .build();
    }
}
