package com.campusconnect.ai.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Summary DTO reporting total records and chunks synchronized across campus data sources.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionSummaryDto {
    private int announcementsProcessed;
    private int eventsProcessed;
    private int academicResourcesProcessed;
    private int clubsProcessed;
    private int opportunitiesProcessed;
    private int totalDocumentsProcessed;
    private int totalChunksIngested;
    private int failedRecords;
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
}
