package com.personalfinance.review.dto;

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
public class WeeklyReviewResponse {

    private UUID id;
    private UUID userId;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String summary;
    private String wins;
    private String challenges;
    private String lessonsLearned;
    private String improvements;
    private String nextWeekPriorities;
    private String additionalNotes;
    private String status;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
