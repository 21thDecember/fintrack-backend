package com.personalfinance.budget.dto;

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
public class BudgetResponse {

    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private Integer year;
    private Integer month;
    private Long limitAmount;
    private Long spentAmount;
    private Long remainingAmount;
    private Double percentage;
    private OffsetDateTime createdAt;
}
