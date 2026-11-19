package com.personalfinance.budget.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBudgetRequest {

    @NotNull(message = "Danh mục không được để trống")
    private UUID categoryId;

    @NotNull(message = "Năm không được để trống")
    @Min(value = 2000, message = "Năm không hợp lệ")
    private Integer year;

    @NotNull(message = "Tháng không được để trống")
    @Min(value = 1, message = "Tháng phải từ 1 đến 12")
    @Max(value = 12, message = "Tháng phải từ 1 đến 12")
    private Integer month;

    @NotNull(message = "Hạn mức chi tiêu không được để trống")
    @PositiveOrZero(message = "Hạn mức chi tiêu phải lớn hơn hoặc bằng 0")
    private Long limitAmount;
}
