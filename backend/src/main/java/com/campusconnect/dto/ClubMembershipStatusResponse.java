package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for club membership status responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMembershipStatusResponse {

    private Long clubId;
    private boolean joined;
    private LocalDateTime joinedAt;
}
