package com.personalfinance.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyJournalResponse {

    private UUID id;
    private UUID userId;
    private LocalDate journalDate;
    private String title;
    private String content;
    private String highlight;
    private String gratitude;
    private String improvement;
    private String tomorrowPlan;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Optional mood summary for the day (if populated)
    private Integer moodScore;
    private Integer energyScore;
    private String moodNote;
}
