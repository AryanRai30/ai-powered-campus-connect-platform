package com.campusconnect.dto;

import com.campusconnect.entity.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing notification details returned to the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    @JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String actionUrl;

    @JsonProperty("isRead")
    public boolean isRead() {
        return isRead;
    }
}
