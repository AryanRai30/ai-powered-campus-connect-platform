package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyDashboardStatsResponse {

    private long resourceCount;
    private long announcementCount;
    private long eventCount;
    private long opportunityCount;
    private long clubCount;

    private long totalEventRegistrations;
    private long totalClubMembers;
    private long totalOpportunityApplications;
}
