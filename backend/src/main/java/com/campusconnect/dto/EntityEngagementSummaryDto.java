package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing engagement metrics for a specific entity (Event, Club, Opportunity).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityEngagementSummaryDto {
    private Long id;
    private String title;
    private String subTitle;     // Venue, Organization, Category
    private String entityType;   // EVENT, CLUB, OPPORTUNITY
    private long totalEngagement; // Count of registrations, members, applications
}
