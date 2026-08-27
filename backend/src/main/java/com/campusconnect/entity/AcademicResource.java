package com.campusconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AcademicResource Entity representing study materials, notes, PDFs, video lectures, and learning links.
 */
@Entity
@Table(name = "academic_resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class AcademicResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @NotBlank
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Size(max = 50)
    @Column(name = "category", length = 50)
    private String category;

    @NotBlank
    @Size(max = 100)
    @Column(name = "subject", nullable = false, length = 100)
    private String subject;

    @Size(max = 30)
    @Column(name = "resource_type", length = 30)
    private String resourceType; // e.g. NOTES, PDF, VIDEO, WEBSITE, OTHER

    @Size(max = 500)
    @Column(name = "resource_url", length = 500)
    private String resourceUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
