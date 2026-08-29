package com.campusconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyClubRequest {

    @NotBlank(message = "Club name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    private String category;
    private String presidentName;
    private String meetingDay;
    private LocalTime meetingTime;
    private String meetingVenue;
    private String department;

    @Builder.Default
    private Boolean published = false;
}
