package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO request body for toggling Faculty account active status by Admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFacultyStatusRequest {

    private boolean active;
}
