package com.campusconnect.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for exposing Academic Resource details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicResourceResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String subject;
    private String resourceType;
    private String resourceUrl;
    private String originalFileName;
    private String storedFileName;
    private String fileContentType;
    private Long fileSize;
    private Boolean hasFile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
