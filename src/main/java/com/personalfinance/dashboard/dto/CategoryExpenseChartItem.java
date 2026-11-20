package com.personalfinance.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryExpenseChartItem {
    private String name;
    private Long amount;
    private String color;
    private String icon;
}
