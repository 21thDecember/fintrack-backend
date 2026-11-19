package com.personalfinance.debt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtSummaryResponse {

    private Long totalReceivables;
    private Long totalPayables;
    private Long netDebt; // totalReceivables - totalPayables
    private long openReceivablesCount;
    private long openPayablesCount;
    private List<PersonDebtSummaryResponse> personSummaries;
}
