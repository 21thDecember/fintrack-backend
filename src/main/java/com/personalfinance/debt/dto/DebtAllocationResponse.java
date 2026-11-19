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
public class DebtAllocationResponse {

    private UUID id;
    private UUID transactionId;
    private UUID debtId;
    private UUID personId;
    private String personName;
    private Long allocatedAmount;
    private String note;
    private OffsetDateTime createdAt;
}
