package com.personalfinance.debt.dto;

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
public class DebtPaymentResponse {

    private UUID id;
    private UUID debtId;
    private UUID transactionId;
    private Long amount;
    private OffsetDateTime paymentDate;
    private String note;
    private OffsetDateTime createdAt;
}
