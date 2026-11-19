package com.personalfinance.transaction.dto;

import com.personalfinance.debt.dto.DebtAllocationItemRequest;
import com.personalfinance.transaction.entity.TransactionDirection;
import com.personalfinance.transaction.entity.TransactionSource;
import com.personalfinance.transaction.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateTransactionRequest {

    @NotNull(message = "Tài khoản không được để trống")
    private UUID accountId;

    private UUID categoryId;

    @NotNull(message = "Loại giao dịch không được để trống")
    private TransactionType type;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private Long amount;

    private TransactionDirection direction;

    @Builder.Default
    private TransactionSource source = TransactionSource.MANUAL;

    private String description;

    private String note;

    private OffsetDateTime transactionDate;

    // Danh sách phân bổ người vay nợ (nếu có, khi chi tiêu chia tiền bạn bè hoặc cho vay nhiều người)
    private List<DebtAllocationItemRequest> debtAllocations;

    // Người liên quan & ngày đến hạn (dùng cho LOAN_GIVEN hoặc BORROW_RECEIVED)
    private UUID personId;
    private java.time.LocalDate dueDate;

    // Khoản nợ liên kết thanh toán (nếu là DEBT_COLLECTION hoặc DEBT_REPAYMENT)
    private UUID debtId;
    private List<UUID> debtIds;
}
