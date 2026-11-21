package com.personalfinance.journal.dto;

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
public class DailyJournalRequest {

    @NotNull(message = "Ngày nhật ký không được để trống")
    private LocalDate journalDate;

    private String title;
    private String content;
    private String highlight;
    private String gratitude;
    private String improvement;
    private String tomorrowPlan;
}
