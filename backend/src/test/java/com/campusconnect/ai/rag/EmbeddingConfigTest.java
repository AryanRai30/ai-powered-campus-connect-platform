package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.config.EmbeddingConfig;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingConfigTest {

    @Test
    @DisplayName("Should return null EmbeddingModel when no API key is provided")
    void testUninitializedWithoutApiKey() {
        EmbeddingConfig config = new EmbeddingConfig();
        ReflectionTestUtils.setField(config, "geminiApiKey", "");
        ReflectionTestUtils.setField(config, "embeddingApiKey", "");
        ReflectionTestUtils.setField(config, "modelName", "text-embedding-004");

        EmbeddingModel model = config.productionEmbeddingModel();
        assertNull(model);
    }

    @Test
    @DisplayName("Should initialize GoogleAiEmbeddingModel when GEMINI_API_KEY is configured")
    void testGoogleAiEmbeddingModelInitialization() {
        EmbeddingConfig config = new EmbeddingConfig();
        ReflectionTestUtils.setField(config, "geminiApiKey", "test-gemini-key");
        ReflectionTestUtils.setField(config, "embeddingApiKey", "");
        ReflectionTestUtils.setField(config, "modelName", "text-embedding-004");

        EmbeddingModel model = config.productionEmbeddingModel();
        assertNotNull(model);
        assertTrue(model instanceof GoogleAiEmbeddingModel);
    }
}
