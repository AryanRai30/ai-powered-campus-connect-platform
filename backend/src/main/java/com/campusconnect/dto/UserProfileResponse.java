package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean active;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional Student Profile fields (populated if role is STUDENT)
    private String studentId;
    private String course;
    private String department;
    private String year;
    private String semester;
    private String skills;
    private String interests;
    private String bio;
}
