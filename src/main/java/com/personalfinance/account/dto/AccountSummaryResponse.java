package com.personalfinance.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryResponse {

    private Long totalBalance;
    private int totalAccounts;
    private List<AccountResponse> accounts;
}
