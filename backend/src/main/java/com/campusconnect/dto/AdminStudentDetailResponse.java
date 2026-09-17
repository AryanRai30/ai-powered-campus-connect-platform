package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing detailed Student user account info and profile data for Admin view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStudentDetailResponse {

    private Long id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String course;
    private String year;
    private String semester;
    private String skills;
    private String interests;
    private String bio;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
