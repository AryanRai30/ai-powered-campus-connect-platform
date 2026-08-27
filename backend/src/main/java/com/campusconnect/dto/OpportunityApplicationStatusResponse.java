package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for opportunity application tracking status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityApplicationStatusResponse {

    private Long opportunityId;
    private boolean applied;
    private String status;
    private LocalDateTime appliedAt;
}
