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
public class FacultyResourceResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String subject;
    private String resourceType;
    private String resourceUrl;
    private String originalFileName;
    private String storedFileName;
    private String fileContentType;
    private Long fileSize;
    private Boolean hasFile;
    private Boolean published;
    private Boolean active;

    private String targetDepartment;
    private String targetCourse;
    private Integer targetYear;
    private Integer targetSemester;
    private String targetSection;

    private String createdByEmail;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
