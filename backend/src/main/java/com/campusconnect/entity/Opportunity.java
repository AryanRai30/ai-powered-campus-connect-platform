package com.campusconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Opportunity Entity representing internships, job listings, scholarships, coding competitions, and career workshops.
 */
@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Opportunity {

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

    @NotBlank
    @Size(max = 150)
    @Column(name = "organization", nullable = false, length = 150)
    private String organization;

    @Size(max = 50)
    @Column(name = "opportunity_type", length = 50)
    private String opportunityType; // INTERNSHIP, JOB, SCHOLARSHIP, COMPETITION, WORKSHOP, OTHER

    @Size(max = 100)
    @Column(name = "location", length = 100)
    private String location;

    @Size(max = 255)
    @Column(name = "skills", length = 255)
    private String skills;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Size(max = 500)
    @Column(name = "application_url", length = 500)
    private String applicationUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Builder.Default
    @Column(name = "published", nullable = false)
    private Boolean published = true;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

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
