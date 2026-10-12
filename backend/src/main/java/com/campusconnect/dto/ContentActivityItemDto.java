package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a content creation/management activity item for faculty or admin.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentActivityItemDto {
    private Long id;
    private String contentType;   // RESOURCE, EVENT, ANNOUNCEMENT, OPPORTUNITY, CLUB
    private String title;
    private String category;
    private String status;        // PUBLISHED, DRAFT, ACTIVE
    private LocalDateTime createdAt;
    private long engagementCount; // Registrations, Club members, Applications
}
