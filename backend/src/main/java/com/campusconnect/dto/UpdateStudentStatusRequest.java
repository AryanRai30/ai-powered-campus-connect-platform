package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO request body for toggling Student account active status by Admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentStatusRequest {

    private boolean active;
}
