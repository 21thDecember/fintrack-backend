package com.personalfinance.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodComparisonResponse {

    private Long previousIncome;
    private Long previousExpense;
    private Double incomeChangePercentage;
    private Double expenseChangePercentage;
}
