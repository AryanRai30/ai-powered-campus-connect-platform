package com.campusconnect.ai.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO representing a vector similarity retrieval hit from the Qdrant knowledge base.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchResult {
    private String segmentId;
    private String text;
    private double score;
    private Map<String, String> metadata;
}
