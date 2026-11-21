package com.personalfinance.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySummaryResponse {

    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private long journalDaysCount;
    private long moodDaysCount;
    private Double averageMood;
    private Double averageEnergy;
    private long completedTasksCount;
    private long totalTasksCount;
    private long habitCheckinsCount;
}
