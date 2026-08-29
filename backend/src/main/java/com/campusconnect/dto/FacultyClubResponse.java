package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyClubResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String presidentName;
    private String meetingDay;
    private LocalTime meetingTime;
    private String meetingVenue;
    private String department;
    private long memberCount;
    private Boolean published;
    private Boolean active;

    private String createdByEmail;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
