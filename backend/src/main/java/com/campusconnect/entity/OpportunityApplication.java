package com.campusconnect.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OpportunityApplication Entity tracking student application submissions to career opportunities.
 */
@Entity
@Table(
    name = "opportunity_applications",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_opportunity_user_application", columnNames = {"opportunity_id", "user_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class OpportunityApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false, foreignKey = @ForeignKey(name = "fk_opportunity_applications_opportunity"))
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_opportunity_applications_user"))
    private User user;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "APPLIED"; // APPLIED, INTERVIEW, SELECTED, REJECTED

    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    protected void onCreate() {
        this.appliedAt = LocalDateTime.now();
    }
}
