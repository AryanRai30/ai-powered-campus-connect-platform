package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.config.QdrantConfig;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class QdrantConfigTest {

    @Test
    @DisplayName("Should initialize default Qdrant properties correctly")
    void testDefaultProperties() {
        QdrantConfig config = new QdrantConfig();
        ReflectionTestUtils.setField(config, "url", "http://localhost:6334");
        ReflectionTestUtils.setField(config, "apiKey", "");
        ReflectionTestUtils.setField(config, "collectionName", "campus_knowledge_base");
        ReflectionTestUtils.setField(config, "host", "localhost");
        ReflectionTestUtils.setField(config, "port", 6334);
        ReflectionTestUtils.setField(config, "useTls", false);
        ReflectionTestUtils.setField(config, "enabled", true);

        assertEquals("http://localhost:6334", config.getUrl());
        assertEquals("", config.getApiKey());
        assertEquals("campus_knowledge_base", config.getCollectionName());
        assertEquals("localhost", config.getHost());
        assertEquals(6334, config.getPort());
        assertFalse(config.isUseTls());
        assertTrue(config.isEnabled());
    }

    @Test
    @DisplayName("Should return null EmbeddingStore when disabled")
    void testDisabledConfig() {
        QdrantConfig config = new QdrantConfig();
        ReflectionTestUtils.setField(config, "enabled", false);

        EmbeddingStore<TextSegment> store = config.qdrantEmbeddingStore(null);
        assertNull(store);
    }

    @Test
    @DisplayName("Should parse custom QDRANT_URL correctly")
    void testCustomQdrantUrl() {
        QdrantConfig config = new QdrantConfig();
        ReflectionTestUtils.setField(config, "url", "https://qdrant-cloud.example.com:6334");
        ReflectionTestUtils.setField(config, "apiKey", "test-key");
        ReflectionTestUtils.setField(config, "collectionName", "campus_knowledge_base");
        ReflectionTestUtils.setField(config, "enabled", true);

        assertEquals("https://qdrant-cloud.example.com:6334", config.getUrl());
        assertEquals("test-key", config.getApiKey());
        assertEquals("campus_knowledge_base", config.getCollectionName());
    }
}
