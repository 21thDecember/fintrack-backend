package com.personalfinance.transaction.dto;

import com.personalfinance.debt.dto.DebtAllocationResponse;
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
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private UUID id;
    private UUID accountId;
    private String accountName;
    private UUID categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private TransactionType type;
    private Long amount;
    private TransactionDirection direction;
    private TransactionSource source;
    private String externalTransactionId;
    private String externalReference;
    private String originalDescription;
    private String description;
    private String note;
    private OffsetDateTime transactionDate;
    private ClassificationStatus classificationStatus;
    private UUID transferGroupId;
    private TransactionStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<DebtAllocationResponse> debtAllocations;
    private List<UUID> debtIds;
    private UUID personId;
    private String personName;
    private java.time.LocalDate dueDate;

    private CategorySummary category;
    private AccountSummary account;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private UUID id;
        private String name;
        private String icon;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountSummary {
        private UUID id;
        private String name;
    }
}
