package com.personalfinance.transaction.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.common.response.PageResponse;
import com.personalfinance.transaction.dto.CreateTransactionRequest;
import com.personalfinance.transaction.dto.TransactionFilterRequest;
import com.personalfinance.transaction.dto.TransactionResponse;
import com.personalfinance.transaction.dto.TransferRequest;
import com.personalfinance.transaction.dto.TransferResponse;
import com.personalfinance.transaction.dto.UpdateClassificationRequest;
import com.personalfinance.transaction.dto.UpdateTransactionRequest;
import com.personalfinance.transaction.dto.VoidTransactionRequest;
import com.personalfinance.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ApiResponse<PageResponse<TransactionResponse>> getTransactions(
            @ModelAttribute TransactionFilterRequest filter) {
        PageResponse<TransactionResponse> response = transactionService.getTransactions(filter);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> getTransactionById(@PathVariable UUID id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ApiResponse.success("Tạo giao dịch thành công", response);
    }

    @PostMapping("/transfer")
    public ApiResponse<TransferResponse> createTransfer(
            @Valid @RequestBody TransferRequest request) {
        TransferResponse response = transactionService.createTransfer(request);
        return ApiResponse.success("Chuyển khoản thành công", response);
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        TransactionResponse response = transactionService.updateTransaction(id, request);
        return ApiResponse.success("Cập nhật giao dịch thành công", response);
    }

    @PatchMapping("/{id}/classification")
    public ApiResponse<TransactionResponse> updateClassificationPatch(
            @PathVariable UUID id,
            @RequestBody UpdateClassificationRequest request) {
        TransactionResponse response = transactionService.updateClassification(id, request.getCategoryId(), request.getClassificationStatus());
        return ApiResponse.success("Phân loại giao dịch thành công", response);
    }

    @PutMapping("/{id}/classification")
    public ApiResponse<TransactionResponse> updateClassificationPut(
            @PathVariable UUID id,
            @RequestBody UpdateClassificationRequest request) {
        TransactionResponse response = transactionService.updateClassification(id, request.getCategoryId(), request.getClassificationStatus());
        return ApiResponse.success("Phân loại giao dịch thành công", response);
    }

    @PostMapping("/{id}/void")
    public ApiResponse<TransactionResponse> voidTransaction(
            @PathVariable UUID id,
            @RequestBody(required = false) VoidTransactionRequest request) {
        TransactionResponse response = transactionService.voidTransaction(id, request);
        return ApiResponse.success("Hủy giao dịch thành công", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTransaction(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        return ApiResponse.success("Xóa giao dịch thành công", null);
    }
}
