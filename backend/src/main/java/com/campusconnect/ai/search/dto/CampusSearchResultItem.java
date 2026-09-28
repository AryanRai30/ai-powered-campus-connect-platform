package com.campusconnect.ai.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO representing an individual ranked campus search result item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusSearchResultItem {

    private String id;
    private String title;
    private String content;
    private String sourceType;
    private double score;
    private String category;
    private String sourceId;
    private String sourceUrl;
    private Map<String, String> metadata;
}
