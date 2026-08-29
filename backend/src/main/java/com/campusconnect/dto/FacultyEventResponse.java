package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyEventResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String venue;
    private String category;
    private String organizer;
    private boolean registrationRequired;
    private long registrationCount;
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
