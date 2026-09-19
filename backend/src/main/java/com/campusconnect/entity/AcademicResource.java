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

    @Size(max = 255)
    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Size(max = 255)
    @Column(name = "stored_file_name", length = 255)
    private String storedFileName;

    @Size(max = 100)
    @Column(name = "file_content_type", length = 100)
    private String fileContentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Size(max = 500)
    @Column(name = "file_storage_path", length = 500)
    private String fileStoragePath;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Builder.Default
    @Column(name = "published", nullable = false)
    private Boolean published = true;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 100)
    @Column(name = "target_department", length = 100)
    private String targetDepartment;

    @Size(max = 100)
    @Column(name = "target_course", length = 100)
    private String targetCourse;

    @Column(name = "target_year")
    private Integer targetYear;

    @Column(name = "target_semester")
    private Integer targetSemester;

    @Size(max = 50)
    @Column(name = "target_section", length = 50)
    private String targetSection;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.published == null) {
            this.published = true;
        }
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
