package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing department-level student distribution statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentStatDto {
    private String department;
    private long studentCount;
}
