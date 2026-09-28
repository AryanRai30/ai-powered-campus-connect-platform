package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.dto.IngestionSummaryDto;
import com.campusconnect.ai.rag.service.CampusContentIngestionService;
import com.campusconnect.ai.rag.service.CampusKnowledgeStartupIngestor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampusKnowledgeStartupIngestorTest {

    @Mock
    private CampusContentIngestionService ingestionService;

    @InjectMocks
    private CampusKnowledgeStartupIngestor startupIngestor;

    @Test
    @DisplayName("Should trigger syncAllCampusContent upon startup execution")
    void testStartupIngestionTrigger() {
        IngestionSummaryDto expectedSummary = IngestionSummaryDto.builder()
                .totalDocumentsProcessed(5)
                .totalChunksIngested(5)
                .success(true)
                .message("Sync success")
                .timestamp(LocalDateTime.now())
                .build();

        when(ingestionService.syncAllCampusContent()).thenReturn(expectedSummary);

        IngestionSummaryDto result = startupIngestor.runIngestion();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(5, result.getTotalDocumentsProcessed());
        verify(ingestionService, times(1)).syncAllCampusContent();
    }

    @Test
    @DisplayName("Should handle startup ingestion exception gracefully without throwing")
    void testStartupIngestionExceptionHandling() {
        when(ingestionService.syncAllCampusContent()).thenThrow(new RuntimeException("Vector store offline"));

        IngestionSummaryDto result = startupIngestor.runIngestion();

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Vector store offline"));
    }
}
