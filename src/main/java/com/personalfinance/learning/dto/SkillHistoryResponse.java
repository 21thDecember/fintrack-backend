package com.personalfinance.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillHistoryResponse {

    private UUID id;
    private UUID skillId;
    private Short previousLevel;
    private Short newLevel;
    private String note;
    private OffsetDateTime changedAt;
}
