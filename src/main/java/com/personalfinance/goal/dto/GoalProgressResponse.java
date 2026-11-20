package com.personalfinance.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgressResponse {

    private UUID goalId;
    private int totalMilestones;
    private int completedMilestones;
    private double progressPercentage;
    private int linkedTasksCount;
    private int completedTasksCount;
    private int linkedHabitsCount;
}
