package com.personalfinance.debt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDebtFromTransactionRequest {

    @NotNull(message = "Mã giao dịch không được để trống")
    private UUID transactionId;

    @NotEmpty(message = "Danh sách phân bổ nợ không được để trống")
    @Valid
    private List<DebtAllocationItemRequest> allocations;
}
