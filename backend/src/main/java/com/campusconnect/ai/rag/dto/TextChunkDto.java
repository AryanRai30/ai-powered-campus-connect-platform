package com.campusconnect.ai.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO representing a text chunk derived from a campus document.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextChunkDto {
    private String chunkId;
    private String documentId;
    private String text;
    private int chunkIndex;
    private Map<String, String> metadata;
}
