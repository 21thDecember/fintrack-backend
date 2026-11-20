package com.personalfinance.goal.dto;

import com.personalfinance.goal.entity.MilestoneStatus;
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
public class MilestoneResponse {

    private UUID id;
    private UUID goalId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private MilestoneStatus status;
    private int displayOrder;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;
}
