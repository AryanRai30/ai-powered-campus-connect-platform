package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for exposing Student Profile response data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String studentId;
    private String course;
    private String department;
    private String year;
    private String semester;
    private String skills;
    private String interests;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
