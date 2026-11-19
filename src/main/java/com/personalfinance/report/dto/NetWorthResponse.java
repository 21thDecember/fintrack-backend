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
public class NetWorthResponse {

    private Long netWorth; // totalBalance + totalReceivables - totalPayables
    private Long totalBalance;
    private Long totalReceivables;
    private Long totalPayables;
    private List<AccountBalanceItem> accounts;
    private List<DebtReportItem> receivables;
    private List<DebtReportItem> payables;
}
