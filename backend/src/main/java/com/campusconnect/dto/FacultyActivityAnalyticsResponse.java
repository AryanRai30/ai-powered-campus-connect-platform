package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Faculty Activity & Content Analytics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyActivityAnalyticsResponse {
    private long publishedResourcesCount;
    private long eventsCreatedCount;
    private long announcementsCreatedCount;
    private long opportunitiesCreatedCount;
    private long clubsSupervisedCount;

    private long totalEventRegistrationsReceived;
    private long totalClubMembersReceived;
    private long totalOpportunityApplicationsReceived;

    private List<ContentActivityItemDto> recentContent;
    private List<EntityEngagementSummaryDto> eventEngagementBreakdown;
    private List<EntityEngagementSummaryDto> opportunityEngagementBreakdown;
    private List<EntityEngagementSummaryDto> clubEngagementBreakdown;
}
