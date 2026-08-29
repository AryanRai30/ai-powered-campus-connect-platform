package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyOpportunityResponse {

    private Long id;
    private String title;
    private String description;
    private String organization;
    private String opportunityType;
    private String location;
    private String skills;
    private LocalDate deadline;
    private String applicationUrl;
    private String eligibility;
    private Boolean published;
    private Boolean active;

    private String targetDepartment;
    private String targetCourse;
    private Integer targetYear;
    private Integer targetSemester;

    private String createdByEmail;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
