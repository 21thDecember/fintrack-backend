package com.personalfinance.learning.dto;

import com.personalfinance.learning.entity.BookStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookStatusUpdateRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private BookStatus status;
}
