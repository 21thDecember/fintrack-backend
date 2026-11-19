package com.personalfinance.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBreakdownResponse {

    private UUID categoryId;
    private String categoryName;
    private String icon;
    private String color;
    private Long amount;
    private Double percentage;
}
