package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for exposing Announcement details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementResponse {

    private Long id;
    private String title;
    private String content;
    private String category;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
