package com.personalfinance.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportResponse {

    private Integer year;
    private Integer month;
    private Long totalIncome;
    private Long grossExpense;
    private Long debtAllocations; // Khoản chi hộ bạn bè
    private Long personalExpense; // grossExpense - debtAllocations
    private Long netCashFlow;     // totalIncome - grossExpense
    private List<CategoryBreakdownResponse> categoryBreakdown;

    // So sánh với tháng trước
    private Long previousMonthIncome;
    private Long previousMonthExpense;
    private Double incomeChangePercentage;
    private Double expenseChangePercentage;
}
