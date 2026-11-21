package com.personalfinance.mood.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodStatisticsResponse {

    private LocalDate from;
    private LocalDate to;
    private long totalRecordedDays;
    private Double averageMood;
    private Double averageEnergy;
    private Map<Integer, Long> moodDistribution;   // Key 1..5 -> count
    private Map<Integer, Long> energyDistribution; // Key 1..5 -> count
    private String predominantMood;
}
