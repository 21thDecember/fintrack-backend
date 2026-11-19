package com.personalfinance.transaction.dto;

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
public class TransferRequest {

    @NotNull(message = "Tài khoản nguồn không được để trống")
    private UUID fromAccountId;

    @NotNull(message = "Tài khoản đích không được để trống")
    private UUID toAccountId;

    @NotNull(message = "Số tiền chuyển không được để trống")
    @Positive(message = "Số tiền chuyển phải lớn hơn 0")
    private Long amount;

    @Builder.Default
    private Long fee = 0L;

    private OffsetDateTime transactionDate;

    private String description;

    private String note;
}
