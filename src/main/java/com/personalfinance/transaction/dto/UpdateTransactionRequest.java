package com.personalfinance.transaction.dto;

import com.personalfinance.transaction.entity.ClassificationStatus;
import com.personalfinance.transaction.entity.TransactionDirection;
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

    // Danh sách phân bổ người vay nợ (nếu có, khi chi tiêu chia tiền bạn bè hoặc cho vay nhiều người)
    private List<com.personalfinance.debt.dto.DebtAllocationItemRequest> debtAllocations;

    // Người liên quan & ngày đến hạn (dùng cho LOAN_GIVEN hoặc BORROW_RECEIVED)
    private UUID personId;
    private java.time.LocalDate dueDate;

    private UUID debtId;
    private List<UUID> debtIds;
}
