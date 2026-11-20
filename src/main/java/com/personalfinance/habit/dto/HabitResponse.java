package com.personalfinance.habit.dto;

import com.personalfinance.habit.entity.HabitFrequency;
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
public class HabitResponse {

    private UUID id;
    private UUID userId;
    private UUID goalId;
    private String goalTitle;
    private String name;
    private String description;
    private HabitFrequency frequency;
    private int targetCount;
    private List<String> activeDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Progress & Streak Metrics
    private int currentStreak;
    private int longestStreak;
    private int totalCompletedDays;
    private double completionRate;
    private boolean isCompletedToday;
    private int todayCount;
    private List<CheckinResponse> recentCheckins;
}
