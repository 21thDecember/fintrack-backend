package com.personalfinance.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningStatisticsResponse {

    private int totalDurationMinutes;
    private long totalSessionsCount;
    private long activeDaysCount;

    private List<CategoryTimeItem> timeByCourse;
    private List<CategoryTimeItem> timeByBook;
    private List<CategoryTimeItem> timeBySkill;
}
