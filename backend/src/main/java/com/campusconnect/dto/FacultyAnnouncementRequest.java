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
public class FacultyAnnouncementRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String category;

    private String targetDepartment;
    private String targetCourse;
    private Integer targetYear;
    private Integer targetSemester;

    @Builder.Default
    private Boolean published = false;
}
