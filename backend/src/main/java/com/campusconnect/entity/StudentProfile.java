package com.campusconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * StudentProfile Entity representing detailed student profile information.
 */
@Entity
@Table(name = "student_profiles", uniqueConstraints = {
    @UniqueConstraint(name = "uk_student_profiles_user_id", columnNames = "user_id"),
    @UniqueConstraint(name = "uk_student_profiles_student_id", columnNames = "student_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_student_profile_user"))
    private User user;

    @NotBlank
    @Size(max = 50)
    @Column(name = "student_id", nullable = false, unique = true, length = 50)
    private String studentId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "course", nullable = false, length = 100)
    private String course;

    @NotBlank
    @Size(max = 100)
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @NotBlank
    @Size(max = 20)
    @Column(name = "year", nullable = false, length = 20)
    private String year;

    @NotBlank
    @Size(max = 20)
    @Column(name = "semester", nullable = false, length = 20)
    private String semester;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "interests", columnDefinition = "TEXT")
    private String interests;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

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
