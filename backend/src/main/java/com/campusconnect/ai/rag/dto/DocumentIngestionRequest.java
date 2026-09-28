package com.campusconnect.ai.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request payload for ingesting document content into the campus knowledge base.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentIngestionRequest {
    private String documentId;
    private String title;
    private String category;
    private String content;
    private String sourceUrl;
    private Map<String, String> metadata;
}
