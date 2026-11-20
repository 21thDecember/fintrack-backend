package com.personalfinance.account.controller;

import com.personalfinance.account.dto.AccountResponse;
import com.personalfinance.account.dto.AccountSummaryResponse;
import com.personalfinance.account.dto.CreateAccountRequest;
import com.personalfinance.account.dto.UpdateAccountRequest;
import com.personalfinance.account.service.AccountService;
import com.personalfinance.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.personalfinance.account.dto.UpdateAccountStatusRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ApiResponse<List<AccountResponse>> getAllAccounts(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        List<AccountResponse> accounts = accountService.getAllAccounts(activeOnly);
        return ApiResponse.success(accounts);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<AccountResponse> toggleAccountStatus(
            @PathVariable UUID id,
            @RequestBody(required = false) UpdateAccountStatusRequest request,
            @RequestParam(required = false) Boolean active) {
        Boolean isActive = null;
        if (request != null && request.getIsActive() != null) {
            isActive = request.getIsActive();
        } else if (active != null) {
            isActive = active;
        }
        AccountResponse account = accountService.toggleAccountStatus(id, isActive);
        return ApiResponse.success("Cập nhật trạng thái tài khoản thành công", account);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<AccountResponse> updateAccountStatusPut(
            @PathVariable UUID id,
            @RequestBody(required = false) UpdateAccountStatusRequest request,
            @RequestParam(required = false) Boolean active) {
        return toggleAccountStatus(id, request, active);
    }

    @GetMapping("/summary")
    public ApiResponse<AccountSummaryResponse> getAccountsSummary() {
        AccountSummaryResponse summary = accountService.getAccountsSummary();
        return ApiResponse.success(summary);
    }

    @GetMapping("/{id}")
    public ApiResponse<AccountResponse> getAccountById(@PathVariable UUID id) {
        AccountResponse account = accountService.getAccountById(id);
        return ApiResponse.success(account);
    }

    @PostMapping
    public ApiResponse<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse account = accountService.createAccount(request);
        return ApiResponse.success("Tạo tài khoản thành công", account);
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountResponse> updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request) {
        AccountResponse account = accountService.updateAccount(id, request);
        return ApiResponse.success("Cập nhật tài khoản thành công", account);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ApiResponse.success("Xóa tài khoản thành công", null);
    }
}
