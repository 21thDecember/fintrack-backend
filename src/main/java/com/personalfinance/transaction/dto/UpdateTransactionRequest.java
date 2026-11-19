package com.personalfinance.transaction.dto;

import com.personalfinance.transaction.entity.ClassificationStatus;
import com.personalfinance.transaction.entity.TransactionDirection;
import com.personalfinance.transaction.entity.TransactionType;
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
public class UpdateTransactionRequest {

    private UUID accountId;

    private UUID categoryId;

    private TransactionType type;

    private Long amount;

    private TransactionDirection direction;

    private String description;

    private String note;

    private OffsetDateTime transactionDate;

    private ClassificationStatus classificationStatus;

    private UUID debtId;
}
