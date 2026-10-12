package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Platform-wide Admin Analytics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPlatformAnalyticsResponse {
    private long totalStudents;
    private long totalFaculty;
    private long totalAdmins;

    private long publishedResourcesCount;
    private long totalResourcesCount;

    private long eventsCount;
    private long publishedEventsCount;
    private long totalEventRegistrations;

    private long clubsCount;
    private long totalClubMemberships;

    private long opportunitiesCount;
    private long publishedOpportunitiesCount;
    private long totalOpportunityApplications;
    private long totalOpportunityBookmarks;

    private long announcementsCount;

    private List<DepartmentStatDto> departmentDistribution;
    private List<ActivityLogItemDto> recentPlatformActivities;
}
