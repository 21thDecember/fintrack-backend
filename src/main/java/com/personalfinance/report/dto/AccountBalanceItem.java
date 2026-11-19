package com.personalfinance.report.dto;

import com.personalfinance.account.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceItem {

    private UUID accountId;
    private String accountName;
    private AccountType accountType;
    private Long balance;
}
