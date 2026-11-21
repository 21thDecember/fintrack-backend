package com.personalfinance.goal.dto;

import com.personalfinance.goal.entity.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgressUpdateRequest {
    private BigDecimal currentValue;
    private GoalStatus status;
}
