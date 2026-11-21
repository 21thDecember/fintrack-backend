package com.personalfinance.goal.dto;

import com.personalfinance.goal.entity.GoalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgressResponse {

    private UUID goalId;
    private GoalType goalType;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String unit;
    private int totalMilestones;
    private int completedMilestones;
    private double progressPercentage;
    private int linkedTasksCount;
    private int completedTasksCount;
    private int linkedHabitsCount;
}
