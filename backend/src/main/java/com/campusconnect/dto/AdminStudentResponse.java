package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing Student user account item for Admin Student Management listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStudentResponse {

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
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
