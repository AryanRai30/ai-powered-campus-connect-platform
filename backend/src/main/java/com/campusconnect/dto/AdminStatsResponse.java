package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing Admin System Overview statistics calculated from real database records.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {

    private long totalStudents;
    private long totalFaculty;
    private long totalAdmins;
    private long totalEvents;
    private long totalAnnouncements;
    private long totalClubs;
    private long totalAcademicResources;
    private long totalResources; // Alias for compatibility
    private long totalOpportunities;
}
