package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubMemberResponse {

    private Long membershipId;
    private Long studentId;
    private String studentIdCode;
    private String firstName;
    private String lastName;
    private String email;
    private String course;
    private String department;
    private String year;
    private String semester;
    private LocalDateTime joinedAt;
}
