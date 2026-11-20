package com.personalfinance.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalStatisticsResponse {

    private long totalGoals;
    private long notStartedGoals;
    private long inProgressGoals;
    private long completedGoals;
    private long cancelledGoals;
    private long overdueGoals;
    private double averageProgressPercentage;
}
