package com.personalfinance.dashboard.dto;

import com.personalfinance.transaction.dto.TransactionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private String period; // THIS_MONTH, THIS_WEEK, THIS_YEAR, LAST_MONTH
    private Long totalBalance;
    private Long totalReceivables;
    private Long totalPayables;
    private Long netWorth;
    private Long income;
    private Long grossExpense;
    private Long personalExpense;
    private Long netIncome; // income - personalExpense
    private long unclassifiedCount;
    private PeriodComparisonResponse periodComparison;
    private List<TransactionResponse> recentTransactions;
    private List<UpcomingDebtItem> upcomingDebts;
    private List<IncomeExpenseChartItem> incomeExpenseChart;
    private List<CategoryExpenseChartItem> categoryExpenseChart;
}
