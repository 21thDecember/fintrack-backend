package com.personalfinance.account.service;

import com.personalfinance.account.dto.AccountResponse;
import com.personalfinance.account.dto.AccountSummaryResponse;
import com.personalfinance.account.dto.CreateAccountRequest;
import com.personalfinance.account.dto.UpdateAccountRequest;
import com.personalfinance.account.entity.Account;
import com.personalfinance.account.repository.AccountRepository;
import com.personalfinance.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Tính số dư hiện tại của tài khoản: opening_balance + SUM(IN) - SUM(OUT)
     */
    @Transactional(readOnly = true)
    public Long calculateBalance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", accountId));
        return calculateBalance(account);
    }

    public Long calculateBalance(Account account) {
        Long delta = accountRepository.calculateTransactionNetDelta(account.getId());
        return account.getOpeningBalance() + (delta != null ? delta : 0L);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts(Boolean activeOnly) {
        List<Account> accounts;
        if (Boolean.TRUE.equals(activeOnly)) {
            accounts = accountRepository.findAllByIsActiveTrueOrderByCreatedAtAsc();
        } else {
            accounts = accountRepository.findAll();
        }
        return accounts.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return getAllAccounts(false);
    }

    @Transactional
    public AccountResponse toggleAccountStatus(UUID id, Boolean isActive) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", id));

        if (isActive != null) {
            account.setIsActive(isActive);
        } else {
            account.setIsActive(!Boolean.TRUE.equals(account.getIsActive()));
        }

        Account updated = accountRepository.save(account);
        log.info("Đã cập nhật trạng thái tài khoản: {} ({}) -> isActive={}", account.getName(), id, updated.getIsActive());
        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID id) {
        Account account = accountRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", id));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public Account getEntityById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", id));
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = Account.builder()
                .name(request.getName().trim())
                .accountType(request.getAccountType())
                .bankCode(request.getBankCode())
                .accountNumber(request.getAccountNumber())
                .openingBalance(request.getOpeningBalance() != null ? request.getOpeningBalance() : 0L)
                .isActive(true)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Đã tạo tài khoản mới: {} (ID: {})", saved.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public AccountResponse updateAccount(UUID id, UpdateAccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", id));

        account.setName(request.getName().trim());
        account.setAccountType(request.getAccountType());
        account.setBankCode(request.getBankCode());
        account.setAccountNumber(request.getAccountNumber());
        account.setOpeningBalance(request.getOpeningBalance());
        if (request.getIsActive() != null) {
            account.setIsActive(request.getIsActive());
        }

        Account updated = accountRepository.save(account);
        log.info("Đã cập nhật thông tin tài khoản: {} (ID: {})", updated.getName(), updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public void deleteAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", id));

        // Soft delete
        account.setIsActive(false);
        accountRepository.save(account);
        log.info("Đã vô hiệu hóa (soft delete) tài khoản: {} (ID: {})", account.getName(), account.getId());
    }

    @Transactional(readOnly = true)
    public AccountSummaryResponse getAccountsSummary() {
        List<AccountResponse> accounts = getAllAccounts();
        long totalBalance = accounts.stream()
                .mapToLong(AccountResponse::getCurrentBalance)
                .sum();

        return AccountSummaryResponse.builder()
                .totalBalance(totalBalance)
                .totalAccounts(accounts.size())
                .accounts(accounts)
                .build();
    }

    public AccountResponse toResponse(Account account) {
        Long currentBalance = calculateBalance(account);
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .accountType(account.getAccountType())
                .bankCode(account.getBankCode())
                .accountNumber(account.getAccountNumber())
                .openingBalance(account.getOpeningBalance())
                .currentBalance(currentBalance)
                .isActive(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
