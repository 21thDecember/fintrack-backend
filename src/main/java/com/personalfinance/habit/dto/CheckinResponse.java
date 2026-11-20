package com.personalfinance.habit.dto;

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
public class CheckinResponse {

    private UUID id;
    private UUID habitId;
    private LocalDate completionDate;
    private int count;
    private String note;
    private OffsetDateTime createdAt;
}
