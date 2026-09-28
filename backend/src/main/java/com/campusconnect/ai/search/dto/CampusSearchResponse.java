package com.campusconnect.ai.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload DTO containing ranked search results for Phase 11.4 Semantic Campus Search.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusSearchResponse {

    private String query;
    private int totalResults;
    private List<CampusSearchResultItem> results;
    private LocalDateTime timestamp;
}
