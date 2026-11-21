package com.personalfinance.mood.dto;

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
public class MoodEntryResponse {

    private UUID id;
    private UUID userId;
    private LocalDate recordedDate;
    private Integer moodScore;
    private Integer energyScore;
    private String note;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
