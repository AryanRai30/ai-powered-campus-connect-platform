package com.campusconnect.ai.rag.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for production LangChain4j EmbeddingModel integration using Google AI Gemini.
 */
@Configuration
public class EmbeddingConfig {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingConfig.class);

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String geminiApiKey;

    @Value("${embedding.api.key:${EMBEDDING_API_KEY:}}")
    private String embeddingApiKey;

    @Value("${embedding.model.name:${GEMINI_EMBEDDING_MODEL:text-embedding-004}}")
    private String modelName;

    @Bean
    @Primary
    public EmbeddingModel productionEmbeddingModel() {
        String key = (embeddingApiKey != null && !embeddingApiKey.trim().isEmpty())
                ? embeddingApiKey.trim()
                : (geminiApiKey != null ? geminiApiKey.trim() : "");

        if (key.isEmpty()) {
            log.warn("GEMINI_API_KEY / EMBEDDING_API_KEY is not configured. Production EmbeddingModel bean is uninitialized.");
            return null;
        }

        log.info("Initializing LangChain4j GoogleAiEmbeddingModel for vector embeddings with model: {}", modelName);
        try {
            return GoogleAiEmbeddingModel.builder()
                    .apiKey(key)
                    .modelName(modelName.trim())
                    .build();
        } catch (Exception e) {
            log.warn("Could not build production GoogleAiEmbeddingModel: {}. EmbeddingModel fallback active.", e.getMessage());
            return null;
        }
    }
}

