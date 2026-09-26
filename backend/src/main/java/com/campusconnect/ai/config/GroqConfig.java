package com.campusconnect.ai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class for LangChain4j Groq Integration.
 * Configures Secondary / Fallback ChatLanguageModel using GROQ_API_KEY environment variable or properties.
 */
@Configuration
public class GroqConfig {

    private static final Logger log = LoggerFactory.getLogger(GroqConfig.class);

    @Value("${groq.api.key:${GROQ_API_KEY:}}")
    private String apiKey;

    @Value("${groq.model.name:openai/gpt-oss-20b}")
    private String modelName;

    @Value("${groq.temperature:0.7}")
    private Double temperature;

    @Value("${groq.base.url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Bean(name = "groqChatLanguageModel")
    public ChatLanguageModel groqChatLanguageModel() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("GROQ_API_KEY is not configured or empty. Groq ChatLanguageModel bean will not be initialized.");
            return null;
        }

        log.info("Initializing LangChain4j Groq (OpenAiChatModel) with model: {} at {}", modelName, baseUrl);
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl.trim())
                .apiKey(apiKey.trim())
                .modelName(modelName.trim())
                .temperature(temperature)
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}
