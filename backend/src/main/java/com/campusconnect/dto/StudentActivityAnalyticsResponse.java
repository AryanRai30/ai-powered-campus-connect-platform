package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Student Activity Analytics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentActivityAnalyticsResponse {
    private long totalEventRegistrations;
    private long totalClubMemberships;
    private long totalOpportunityBookmarks;
    private long totalOpportunityApplications;
    private long totalAccessibleResources;
    private List<ActivityLogItemDto> recentActivities;
    private List<EventResponse> registeredEvents;
    private List<ClubResponse> joinedClubs;
    private List<OpportunityResponse> applications;
    private List<OpportunityResponse> bookmarks;
}
