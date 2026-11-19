package com.personalfinance.backup.dto;

import com.personalfinance.account.entity.Account;
import com.personalfinance.budget.entity.Budget;
import com.personalfinance.category.entity.Category;
import com.personalfinance.common.entity.AppSetting;
import com.personalfinance.debt.entity.Debt;
import com.personalfinance.debt.entity.DebtAllocation;
import com.personalfinance.debt.entity.DebtPayment;
import com.personalfinance.person.entity.Person;
import com.personalfinance.transaction.entity.Transaction;
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
public class BackupDataDto {

    private String version;
    private OffsetDateTime exportedAt;
    private List<Account> accounts;
    private List<Category> categories;
    private List<Person> persons;
    private List<Transaction> transactions;
    private List<Debt> debts;
    private List<DebtAllocation> debtAllocations;
    private List<DebtPayment> debtPayments;
    private List<Budget> budgets;
    private List<AppSetting> appSettings;
}
