package com.personalfinance.habit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitStatisticsResponse {

    private long totalHabits;
    private long activeHabits;
    private long todayScheduledCount;
    private long completedTodayCount;
    private double overallCompletionRate;
}
