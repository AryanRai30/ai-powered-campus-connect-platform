package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for exposing Opportunity information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityResponse {

    private Long id;
    private String title;
    private String description;
    private String organization;
    private String opportunityType;
    private String location;
    private String skills;
    private LocalDate deadline;
    private String applicationUrl;
    private boolean bookmarked;
    private boolean applied;
    private String applicationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
