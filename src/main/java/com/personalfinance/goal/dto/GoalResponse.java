package com.personalfinance.goal.dto;

import com.personalfinance.goal.entity.GoalCategory;
import com.personalfinance.goal.entity.GoalPriority;
import com.personalfinance.goal.entity.GoalStatus;
import com.personalfinance.habit.dto.HabitResponse;
import com.personalfinance.task.dto.TaskResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {

    private UUID id;
    private UUID userId;
    private String title;
    private String description;
    private GoalCategory category;
    private GoalPriority priority;
    private GoalStatus status;
    private LocalDate startDate;
    private LocalDate targetDate;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private boolean isOverdue;

    // Progress metrics
    private double progressPercentage;
    private int totalMilestones;
    private int completedMilestones;

    private List<MilestoneResponse> milestones;
    private List<TaskResponse> linkedTasks;
    private List<HabitResponse> linkedHabits;
}
