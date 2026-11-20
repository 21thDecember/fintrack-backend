package com.personalfinance.habit.dto;

import com.personalfinance.habit.entity.HabitFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayHabitResponse {

    private UUID id;
    private String name;
    private String description;
    private HabitFrequency frequency;
    private int targetCount;
    private int todayCount;
    private boolean isCompletedToday;
    private int currentStreak;
    private UUID goalId;
    private String goalTitle;
}
