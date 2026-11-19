package com.personalfinance.budget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBudgetRequest {

    @NotNull(message = "Hạn mức chi tiêu không được để trống")
    @PositiveOrZero(message = "Hạn mức chi tiêu phải lớn hơn hoặc bằng 0")
    private Long limitAmount;
}
