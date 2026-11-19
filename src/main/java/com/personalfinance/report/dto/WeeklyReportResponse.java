package com.personalfinance.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportResponse {

    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Long totalIncome;
    private Long grossExpense;
    private Long debtAllocations; // Khoản chi hộ bạn bè/cho vay trong chi tiêu
    private Long personalExpense; // grossExpense - debtAllocations
    private Long netCashFlow;     // totalIncome - grossExpense
    private List<CategoryBreakdownResponse> categoryBreakdown;
}
