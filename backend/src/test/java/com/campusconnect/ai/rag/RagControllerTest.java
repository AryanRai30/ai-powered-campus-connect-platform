package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.controller.RagController;
import com.campusconnect.ai.rag.dto.RagQueryRequest;
import com.campusconnect.ai.rag.dto.RagQueryResponse;
import com.campusconnect.ai.rag.service.RagRetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RagControllerTest {

    private RagRetrievalService ragRetrievalService;
    private RagController ragController;

    @BeforeEach
    void setUp() {
        ragRetrievalService = mock(RagRetrievalService.class);
        ragController = new RagController(ragRetrievalService);
    }

    @Test
    @DisplayName("Should process RAG query request via controller")
    void testQueryCampusKnowledge() {
        RagQueryRequest request = RagQueryRequest.builder()
                .query("What are the library hours?")
                .build();

        RagQueryResponse mockResponse = RagQueryResponse.builder()
                .query("What are the library hours?")
                .answer("Library hours are 8 AM to 10 PM.")
                .grounded(true)
                .retrievedChunksCount(1)
                .matchedChunks(List.of())
                .timestamp(LocalDateTime.now())
                .build();

        when(ragRetrievalService.queryCampusKnowledge(any(), any())).thenReturn(mockResponse);

        var responseEntity = ragController.queryCampusKnowledge(request);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody().isGrounded());
        assertEquals("Library hours are 8 AM to 10 PM.", responseEntity.getBody().getAnswer());
    }
}
