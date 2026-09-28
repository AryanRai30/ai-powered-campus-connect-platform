package com.campusconnect.ai.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO returning grounded AI answer and matched knowledge chunks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagQueryResponse {

    private String query;
    private String answer;
    private boolean grounded;
    private int retrievedChunksCount;
    private List<VectorSearchResult> matchedChunks;
    private LocalDateTime timestamp;
}
