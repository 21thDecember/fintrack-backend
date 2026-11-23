package com.personalfinance.learning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookProgressUpdateRequest {

    @NotNull(message = "Số trang hiện tại không được để trống")
    @Min(value = 0, message = "Số trang hiện tại phải >= 0")
    private Integer currentPage;

    @Min(value = 1, message = "Tổng số trang phải > 0")
    private Integer totalPages;
}
