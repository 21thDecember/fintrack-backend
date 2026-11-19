package com.personalfinance.transaction.dto;

import com.personalfinance.transaction.entity.ClassificationStatus;
import com.personalfinance.transaction.entity.TransactionDirection;
import com.personalfinance.transaction.entity.TransactionSource;
import com.personalfinance.transaction.entity.TransactionStatus;
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
public class TransactionFilterRequest {

    private UUID accountId;
    private UUID categoryId;
    private TransactionType type;
    private TransactionDirection direction;
    private TransactionSource source;
    private ClassificationStatus classificationStatus;
    private TransactionStatus status;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Long minAmount;
    private Long maxAmount;
    private String keyword;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "transactionDate";

    @Builder.Default
    private String sortDirection = "DESC";
}
