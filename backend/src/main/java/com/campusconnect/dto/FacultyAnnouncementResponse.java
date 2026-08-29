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
public class FacultyAnnouncementResponse {

    private Long id;
    private String title;
    private String content;
    private String category;
    private Boolean published;
    private Boolean active;

    private String targetDepartment;
    private String targetCourse;
    private Integer targetYear;
    private Integer targetSemester;

    private String createdByEmail;
    private String createdByName;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
