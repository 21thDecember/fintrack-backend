package com.personalfinance.backup.service;

import com.personalfinance.account.repository.AccountRepository;
import com.personalfinance.backup.dto.BackupDataDto;
import com.personalfinance.budget.repository.BudgetRepository;
import com.personalfinance.category.repository.CategoryRepository;
import com.personalfinance.common.repository.AppSettingRepository;
import com.personalfinance.debt.repository.DebtAllocationRepository;
import com.personalfinance.debt.repository.DebtPaymentRepository;
import com.personalfinance.debt.repository.DebtRepository;
import com.personalfinance.person.repository.PersonRepository;
import com.personalfinance.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final PersonRepository personRepository;
    private final TransactionRepository transactionRepository;
    private final DebtRepository debtRepository;
    private final DebtAllocationRepository debtAllocationRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final BudgetRepository budgetRepository;
    private final AppSettingRepository appSettingRepository;

    @Transactional(readOnly = true)
    public BackupDataDto exportData() {
        return BackupDataDto.builder()
                .version("1.0")
                .exportedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .accounts(accountRepository.findAll())
                .categories(categoryRepository.findAll())
                .persons(personRepository.findAll())
                .transactions(transactionRepository.findAll())
                .debts(debtRepository.findAll())
                .debtAllocations(debtAllocationRepository.findAll())
                .debtPayments(debtPaymentRepository.findAll())
                .budgets(budgetRepository.findAll())
                .appSettings(appSettingRepository.findAll())
                .build();
    }

    @Transactional
    public void importData(BackupDataDto data) {
        if (data == null) {
            return;
        }

        // Nhập dữ liệu theo thứ tự quan hệ khóa ngoại
        if (data.getAccounts() != null && !data.getAccounts().isEmpty()) {
            accountRepository.saveAll(data.getAccounts());
        }
        if (data.getCategories() != null && !data.getCategories().isEmpty()) {
            categoryRepository.saveAll(data.getCategories());
        }
        if (data.getPersons() != null && !data.getPersons().isEmpty()) {
            personRepository.saveAll(data.getPersons());
        }
        if (data.getTransactions() != null && !data.getTransactions().isEmpty()) {
            transactionRepository.saveAll(data.getTransactions());
        }
        if (data.getDebts() != null && !data.getDebts().isEmpty()) {
            debtRepository.saveAll(data.getDebts());
        }
        if (data.getDebtAllocations() != null && !data.getDebtAllocations().isEmpty()) {
            debtAllocationRepository.saveAll(data.getDebtAllocations());
        }
        if (data.getDebtPayments() != null && !data.getDebtPayments().isEmpty()) {
            debtPaymentRepository.saveAll(data.getDebtPayments());
        }
        if (data.getBudgets() != null && !data.getBudgets().isEmpty()) {
            budgetRepository.saveAll(data.getBudgets());
        }
        if (data.getAppSettings() != null && !data.getAppSettings().isEmpty()) {
            appSettingRepository.saveAll(data.getAppSettings());
        }

        log.info("Đã phục hồi dữ liệu sao lưu thành công từ bản sao lưu tạo lúc: {}", data.getExportedAt());
    }
}
