package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyEventRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    private LocalTime eventTime;

    @NotBlank(message = "Venue is required")
    @Size(max = 150)
    private String venue;

    private String category;
    private String organizer;
    private Boolean registrationRequired;

    private String targetDepartment;
    private String targetCourse;
    private Integer targetYear;
    private Integer targetSemester;

    @Builder.Default
    private Boolean published = false;
}
