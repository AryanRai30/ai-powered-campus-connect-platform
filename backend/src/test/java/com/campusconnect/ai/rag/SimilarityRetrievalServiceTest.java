package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.config.QdrantConfig;
import com.campusconnect.ai.rag.dto.VectorSearchResult;
import com.campusconnect.ai.rag.service.EmbeddingService;
import com.campusconnect.ai.rag.service.SimilarityRetrievalService;
import com.campusconnect.ai.rag.service.VectorStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimilarityRetrievalServiceTest {

    private SimilarityRetrievalService retrievalService;

    @BeforeEach
    void setUp() {
        EmbeddingService embeddingService = new EmbeddingService();
        QdrantConfig qdrantConfig = new QdrantConfig();
        ReflectionTestUtils.setField(qdrantConfig, "enabled", false);
        VectorStoreService vectorStoreService = new VectorStoreService(qdrantConfig, null);

        retrievalService = new SimilarityRetrievalService(embeddingService, vectorStoreService);
    }

    @Test
    @DisplayName("Should return empty list for empty or blank query")
    void testRetrieveBlankQuery() {
        List<VectorSearchResult> resultsNull = retrievalService.retrieveSimilarSegments(null, 5, 0.5);
        assertTrue(resultsNull.isEmpty());

        List<VectorSearchResult> resultsBlank = retrievalService.retrieveSimilarSegments("   ", 5, 0.5);
        assertTrue(resultsBlank.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when vector store is offline")
    void testRetrieveOfflineVectorStore() {
        List<VectorSearchResult> results = retrievalService.retrieveSimilarSegments("What are the library opening hours?", 5, 0.6);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
