package com.personalfinance.review.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReviewRequest {

    @NotNull(message = "Ngày bắt đầu tuần không được để trống")
    private LocalDate weekStartDate;

    private String summary;
    private String wins;
    private String challenges;
    private String lessonsLearned;
    private String improvements;
    private String nextWeekPriorities;
    private String additionalNotes;
    private String status; // DRAFT or COMPLETED
}
