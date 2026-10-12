package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing an activity log entry for student or platform timeline.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogItemDto {
    private String type;         // EVENT_REGISTRATION, CLUB_JOIN, OPPORTUNITY_APPLY, OPPORTUNITY_BOOKMARK, RESOURCE_ACCESS
    private String title;        // Event title, Club name, Opportunity title, Resource title
    private String description;  // Context/details (e.g. venue, status, category)
    private LocalDateTime timestamp;
    private Long referenceId;
}
