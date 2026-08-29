package com.campusconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Event Entity representing campus events and activities.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Event {

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

    @NotNull
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @NotBlank
    @Size(max = 150)
    @Column(name = "venue", nullable = false, length = 150)
    private String venue;

    @Size(max = 50)
    @Column(name = "category", length = 50)
    private String category;

    @Size(max = 100)
    @Column(name = "organizer", length = 100)
    private String organizer;

    @Builder.Default
    @Column(name = "registration_required", nullable = false)
    private boolean registrationRequired = false;

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
