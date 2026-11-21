package com.personalfinance.goal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "goals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    @Builder.Default
    private GoalCategory category = GoalCategory.PERSONAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private GoalPriority priority = GoalPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GoalStatus status = GoalStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 30)
    @Builder.Default
    private GoalType goalType = GoalType.MILESTONE;

    @Column(name = "target_value")
    @Builder.Default
    private BigDecimal targetValue = BigDecimal.ZERO;

    @Column(name = "current_value")
    @Builder.Default
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Column(name = "unit", length = 50)
    @Builder.Default
    private String unit = "";

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.category == null) {
            this.category = GoalCategory.PERSONAL;
        }
        if (this.priority == null) {
            this.priority = GoalPriority.MEDIUM;
        }
        if (this.status == null) {
            this.status = GoalStatus.NOT_STARTED;
        }
        if (this.goalType == null) {
            this.goalType = GoalType.MILESTONE;
        }
        if (this.targetValue == null) {
            this.targetValue = BigDecimal.ZERO;
        }
        if (this.currentValue == null) {
            this.currentValue = BigDecimal.ZERO;
        }
        if (this.unit == null) {
            this.unit = "";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
