package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Data Transfer Object for exposing Club details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String presidentName;
    private String meetingDay;
    private LocalTime meetingTime;
    private String meetingVenue;
    private long memberCount;
    private boolean joined;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
