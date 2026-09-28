package com.campusconnect.ai.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO returning details of a document ingestion operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentIngestionResult {
    private String documentId;
    private int chunksIngested;
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
}
