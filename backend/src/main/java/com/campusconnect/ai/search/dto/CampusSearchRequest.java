package com.campusconnect.ai.search.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload DTO for Phase 11.4 Semantic Campus Search.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusSearchRequest {

    @NotBlank(message = "Search query must not be blank")
    private String query;

    private String category;

    private Integer maxResults;

    private Double minScoreThreshold;
}
