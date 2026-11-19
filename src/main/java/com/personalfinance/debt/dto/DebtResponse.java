package com.personalfinance.debt.dto;

import com.personalfinance.debt.entity.DebtOriginType;
import com.personalfinance.debt.entity.DebtStatus;
import com.personalfinance.debt.entity.DebtType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtResponse {

    private UUID id;
    private UUID personId;
    private String personName;
    private String personPhone;
    private DebtType type;
    private DebtOriginType originType;
    private UUID originTransactionId;
    private String description;
    private Long principalAmount;
    private Long paidAmount;
    private Long remainingAmount;
    private LocalDate dueDate;
    private DebtStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<DebtPaymentResponse> payments;
    private List<DebtAllocationResponse> allocations;
}
