package com.campusconnect.ai.rag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for querying the RAG campus knowledge base.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagQueryRequest {

    @NotBlank(message = "Query message cannot be blank")
    private String query;

    private Integer maxResults;
    private Double minScoreThreshold;

    // Student targeting/authorization context (optional if auto-resolved from auth)
    private String department;
    private String course;
    private Integer year;
    private Integer semester;
}
