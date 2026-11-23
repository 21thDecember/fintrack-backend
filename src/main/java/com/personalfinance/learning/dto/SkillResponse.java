package com.personalfinance.learning.dto;

import com.personalfinance.learning.entity.SkillCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {

    private UUID id;
    private UUID userId;
    private UUID goalId;
    private String goalTitle;
    private String name;
    private String description;
    private SkillCategory category;
    private Short currentLevel;
    private Short targetLevel;
    private Boolean isActive;
    private Integer totalDurationMinutes;
    private List<SkillHistoryResponse> history;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
