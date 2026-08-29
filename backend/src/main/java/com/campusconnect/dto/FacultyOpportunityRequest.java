package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyOpportunityRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Organization is required")
    @Size(max = 150)
    private String organization;

    private String opportunityType; // INTERNSHIP, JOB, SCHOLARSHIP, COMPETITION, WORKSHOP, OTHER
    private String location;
    private String skills;
    private LocalDate deadline;
    private String applicationUrl;
    private String eligibility;

    private String targetDepartment;
    private String targetCourse;
    private Integer targetYear;
    private Integer targetSemester;

    @Builder.Default
    private Boolean published = false;
}
