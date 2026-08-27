package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for event registration status response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationStatusResponse {

    private Long eventId;
    private boolean registered;
    private LocalDateTime registeredAt;
}
