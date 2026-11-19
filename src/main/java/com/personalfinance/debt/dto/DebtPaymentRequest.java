package com.personalfinance.debt.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtPaymentRequest {

    @NotNull(message = "Tài khoản thực hiện thanh toán không được để trống")
    private UUID accountId;

    @NotNull(message = "Số tiền thanh toán không được để trống")
    @Positive(message = "Số tiền thanh toán phải lớn hơn 0")
    private Long amount;

    private OffsetDateTime paymentDate;

    private String note;
}
