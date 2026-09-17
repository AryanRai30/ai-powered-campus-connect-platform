package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing Faculty user item for Admin Faculty Management listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFacultyResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
