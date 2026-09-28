package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.config.QdrantConfig;
import com.campusconnect.ai.rag.dto.DocumentIngestionRequest;
import com.campusconnect.ai.rag.dto.DocumentIngestionResult;
import com.campusconnect.ai.rag.service.DocumentChunkingService;
import com.campusconnect.ai.rag.service.DocumentIngestionService;
import com.campusconnect.ai.rag.service.EmbeddingService;
import com.campusconnect.ai.rag.service.VectorStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentIngestionServiceTest {

    private DocumentIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        DocumentChunkingService chunkingService = new DocumentChunkingService();
        EmbeddingService embeddingService = new EmbeddingService();
        QdrantConfig qdrantConfig = new QdrantConfig();
        ReflectionTestUtils.setField(qdrantConfig, "enabled", false);
        VectorStoreService vectorStoreService = new VectorStoreService(qdrantConfig, null);

        ingestionService = new DocumentIngestionService(chunkingService, embeddingService, vectorStoreService);
    }

    @Test
    @DisplayName("Should process document ingestion successfully without requiring live vector database")
    void testIngestDocumentOffline() {
        DocumentIngestionRequest request = DocumentIngestionRequest.builder()
                .documentId("doc-campus-rules")
                .title("Campus Rules and Regulations 2026")
                .category("policy")
                .content("Rule 1: Library quiet hours start at 9 PM. Rule 2: Student IDs must be scanned at gate entry.")
                .sourceUrl("https://campus.edu/rules.pdf")
                .metadata(Map.of("department", "Administration"))
                .build();

        DocumentIngestionResult result = ingestionService.ingestDocument(request);

        assertNotNull(result);
        assertEquals("doc-campus-rules", result.getDocumentId());
        assertTrue(result.isSuccess());
        assertTrue(result.getChunksIngested() > 0);
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("Should reject ingestion for null or empty document request")
    void testIngestEmptyRequest() {
        DocumentIngestionResult resultNull = ingestionService.ingestDocument(null);
        assertNotNull(resultNull);
        assertFalse(resultNull.isSuccess());
        assertEquals(0, resultNull.getChunksIngested());

        DocumentIngestionRequest emptyReq = DocumentIngestionRequest.builder()
                .documentId("doc-empty")
                .content("   ")
                .build();
        DocumentIngestionResult resultEmpty = ingestionService.ingestDocument(emptyReq);
        assertNotNull(resultEmpty);
        assertFalse(resultEmpty.isSuccess());
        assertEquals(0, resultEmpty.getChunksIngested());
    }
}
