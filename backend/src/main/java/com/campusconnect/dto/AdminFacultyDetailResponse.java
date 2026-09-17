package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing detailed Faculty user account info and real managed content counts for Admin view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFacultyDetailResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Real content metrics managed by this faculty member
    private long resourceCount;
    private long announcementCount;
    private long eventCount;
    private long clubCount;
    private long opportunityCount;
}
