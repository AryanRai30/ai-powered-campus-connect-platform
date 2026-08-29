package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyOpportunityApplicationResponse {
    private Long applicationId;
    private Long opportunityId;
    private String opportunityTitle;
    private Long studentUserId;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String course;
    private String department;
    private String year;
    private String applicationStatus;
    private LocalDateTime appliedAt;
}
