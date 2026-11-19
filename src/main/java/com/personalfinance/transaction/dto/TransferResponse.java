package com.personalfinance.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private UUID transferGroupId;
    private TransactionResponse fromTransaction;
    private TransactionResponse toTransaction;
    private TransactionResponse feeTransaction;
}
