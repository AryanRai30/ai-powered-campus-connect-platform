package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing authenticated faculty account information for the Faculty Dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyDashboardResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String status;
}
