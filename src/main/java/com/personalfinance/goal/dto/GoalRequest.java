package com.personalfinance.goal.dto;

import com.personalfinance.goal.entity.GoalCategory;
import com.personalfinance.goal.entity.GoalPriority;
import com.personalfinance.goal.entity.GoalStatus;
import com.personalfinance.goal.entity.GoalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {

    @NotBlank(message = "Tiêu đề mục tiêu không được để trống")
    @Size(max = 255, message = "Tiêu đề không quá 255 ký tự")
    private String title;

    private String description;

    private GoalCategory category;

    private GoalPriority priority;

    private GoalStatus status;

    private GoalType goalType;

    private BigDecimal targetValue;

    private BigDecimal currentValue;

    private String unit;

    private LocalDate startDate;

    private LocalDate targetDate;
}

