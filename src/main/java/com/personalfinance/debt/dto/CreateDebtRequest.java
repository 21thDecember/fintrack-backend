package com.personalfinance.debt.dto;

import com.personalfinance.debt.entity.DebtType;
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
public class CreateDebtRequest {

    @NotNull(message = "Đối tượng liên quan không được để trống")
    private UUID personId;

    @NotNull(message = "Loại nợ (RECEIVABLE/PAYABLE) không được để trống")
    private DebtType type;

    @NotNull(message = "Số tiền gốc không được để trống")
    @Positive(message = "Số tiền gốc phải lớn hơn 0")
    private Long principalAmount;

    private String description;

    private LocalDate dueDate;
}
