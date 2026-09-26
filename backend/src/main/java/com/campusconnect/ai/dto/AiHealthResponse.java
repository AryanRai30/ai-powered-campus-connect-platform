package com.campusconnect.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Health check response payload for AI service foundation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiHealthResponse {

    private String status;
    private boolean apiKeyConfigured;
    private String model;
    private LocalDateTime timestamp;
}
