package com.personalfinance.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalCalendarDayResponse {

    private LocalDate date;
    private boolean hasJournal;
    private UUID journalId;
    private String title;
    private boolean hasContent;
    private Integer moodScore;
    private Integer energyScore;
}
