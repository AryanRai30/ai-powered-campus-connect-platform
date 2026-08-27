package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for opportunity bookmark status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityBookmarkStatusResponse {

    private Long opportunityId;
    private boolean bookmarked;
    private LocalDateTime bookmarkedAt;
}
