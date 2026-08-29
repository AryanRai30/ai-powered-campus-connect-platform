package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyResourceRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String category;

    @NotBlank(message = "Subject is required")
    @Size(max = 100)
    private String subject;

    private String resourceType; // NOTES, PDF, VIDEO, WEBSITE, OTHER
    private String resourceUrl;

    private String targetDepartment;
    private String targetCourse;
    private Integer targetYear;
    private Integer targetSemester;
    private String targetSection;

    @Builder.Default
    private Boolean published = false;
}
