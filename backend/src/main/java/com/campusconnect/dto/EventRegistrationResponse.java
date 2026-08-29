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
public class EventRegistrationResponse {

    private Long registrationId;
    private Long studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String year;
    private LocalDateTime registeredAt;
}
