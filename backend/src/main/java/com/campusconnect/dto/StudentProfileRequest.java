package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Data Transfer Object for creating or updating a Student Profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileRequest {

    @NotBlank(message = "Student ID is required")
    @Size(max = 50, message = "Student ID cannot exceed 50 characters")
    private String studentId;

    @NotBlank(message = "Course is required")
    @Size(max = 100, message = "Course cannot exceed 100 characters")
    private String course;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @NotBlank(message = "Year is required")
    @Size(max = 20, message = "Year cannot exceed 20 characters")
    private String year;

    @NotBlank(message = "Semester is required")
    @Size(max = 20, message = "Semester cannot exceed 20 characters")
    private String semester;

    private String skills;

    private String interests;

    private String bio;
}
