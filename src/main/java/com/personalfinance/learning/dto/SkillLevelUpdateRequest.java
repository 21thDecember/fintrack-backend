package com.personalfinance.learning.dto;

import jakarta.validation.constraints.Max;
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
public class SkillLevelUpdateRequest {

    @NotNull(message = "Level mới không được để trống")
    @Min(value = 1, message = "Level tối thiểu là 1")
    @Max(value = 5, message = "Level tối đa là 5")
    private Short newLevel;

    private String note;
}
