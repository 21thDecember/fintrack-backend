package com.personalfinance.mood.dto;

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
public class MoodCalendarDayResponse {

    private LocalDate date;
    private boolean hasEntry;
    private UUID id;
    private Integer moodScore;
    private Integer energyScore;
    private String note;
    private boolean hasJournal;
}
