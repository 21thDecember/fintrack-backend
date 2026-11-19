package com.personalfinance.debt.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.common.response.PageResponse;
import com.personalfinance.debt.dto.CancelDebtRequest;
import com.personalfinance.debt.dto.CreateDebtFromTransactionRequest;
import com.personalfinance.debt.dto.CreateDebtRequest;
import com.personalfinance.debt.dto.DebtPaymentRequest;
import com.personalfinance.debt.dto.DebtResponse;
import com.personalfinance.debt.dto.DebtSummaryResponse;
import com.personalfinance.debt.dto.UpdateDebtRequest;
import com.personalfinance.debt.entity.DebtStatus;
import com.personalfinance.debt.entity.DebtType;
import com.personalfinance.debt.service.DebtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;

    @GetMapping
    public ApiResponse<PageResponse<DebtResponse>> getDebts(
            @RequestParam(required = false) DebtType type,
            @RequestParam(required = false) DebtStatus status,
            @RequestParam(required = false) UUID personId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<DebtResponse> response = debtService.getDebts(type, status, personId, page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/summary")
    public ApiResponse<DebtSummaryResponse> getDebtSummary() {
        DebtSummaryResponse summary = debtService.getDebtSummary();
        return ApiResponse.success(summary);
    }

    @GetMapping("/{id}")
    public ApiResponse<DebtResponse> getDebtById(@PathVariable UUID id) {
        DebtResponse response = debtService.getDebtById(id);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<DebtResponse> createStandaloneDebt(@Valid @RequestBody CreateDebtRequest request) {
        DebtResponse response = debtService.createStandaloneDebt(request);
        return ApiResponse.success("Tạo khoản nợ thành công", response);
    }

    @PostMapping("/from-transaction")
    public ApiResponse<List<DebtResponse>> createDebtFromTransaction(
            @Valid @RequestBody CreateDebtFromTransactionRequest request) {
        List<DebtResponse> responses = debtService.createDebtFromTransaction(request);
        return ApiResponse.success("Phân bổ nợ từ giao dịch thành công", responses);
    }

    @PostMapping("/{id}/payments")
    public ApiResponse<DebtResponse> recordDebtPayment(
            @PathVariable UUID id,
            @Valid @RequestBody DebtPaymentRequest request) {
        DebtResponse response = debtService.recordDebtPayment(id, request);
        return ApiResponse.success("Ghi nhận thanh toán nợ thành công", response);
    }

    @PutMapping("/{id}")
    public ApiResponse<DebtResponse> updateDebt(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDebtRequest request) {
        DebtResponse response = debtService.updateDebt(id, request);
        return ApiResponse.success("Cập nhật khoản nợ thành công", response);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<DebtResponse> cancelDebt(
            @PathVariable UUID id,
            @RequestBody(required = false) CancelDebtRequest request) {
        DebtResponse response = debtService.cancelDebt(id, request);
        return ApiResponse.success("Hủy khoản nợ thành công", response);
    }
}
