package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.config.QdrantConfig;
import com.campusconnect.ai.rag.service.VectorStoreService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VectorStoreServiceTest {

    private VectorStoreService vectorStoreService;
    private QdrantConfig qdrantConfig;

    @BeforeEach
    void setUp() {
        qdrantConfig = new QdrantConfig();
        ReflectionTestUtils.setField(qdrantConfig, "enabled", false);
        vectorStoreService = new VectorStoreService(qdrantConfig, null);
    }

    @Test
    @DisplayName("Should report store unavailable when disabled or store bean is null")
    void testIsStoreAvailable() {
        assertFalse(vectorStoreService.isStoreAvailable());
    }

    @Test
    @DisplayName("Should handle add gracefully when store is unavailable")
    void testAddWhenUnavailable() {
        String result = vectorStoreService.add(Embedding.from(new float[]{0.1f, 0.2f}), TextSegment.from("sample"));
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle findRelevant gracefully when store is unavailable")
    void testFindRelevantWhenUnavailable() {
        List<EmbeddingMatch<TextSegment>> matches = vectorStoreService.findRelevant(
                Embedding.from(new float[]{0.1f, 0.2f}), 5, 0.7);
        assertNotNull(matches);
        assertTrue(matches.isEmpty());
    }
}
