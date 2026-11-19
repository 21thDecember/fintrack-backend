package com.personalfinance.debt.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtAllocationItemRequest {

    @NotNull(message = "Người liên quan không được để trống")
    private UUID personId;

    @NotNull(message = "Số tiền phân bổ không được để trống")
    @Positive(message = "Số tiền phân bổ phải lớn hơn 0")
    private Long allocatedAmount;

    private String note;

    private LocalDate dueDate;
}
