package com.personalfinance.learning.dto;

import com.personalfinance.learning.entity.CourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatusUpdateRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private CourseStatus status;
}
