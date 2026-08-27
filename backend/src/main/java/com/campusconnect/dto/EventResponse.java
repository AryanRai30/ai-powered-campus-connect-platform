package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Data Transfer Object for exposing Event information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

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
    private boolean registered;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
