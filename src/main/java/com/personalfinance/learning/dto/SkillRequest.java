package com.personalfinance.learning.dto;

import com.personalfinance.learning.entity.SkillCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillRequest {

    @NotBlank(message = "Tên kỹ năng không được để trống")
    @Size(max = 255, message = "Tên kỹ năng tối đa 255 ký tự")
    private String name;

    private String description;

    private SkillCategory category;

    @NotNull(message = "Level hiện tại không được để trống")
    @Min(value = 1, message = "Level tối thiểu là 1 (Beginner)")
    @Max(value = 5, message = "Level tối đa là 5 (Expert)")
    private Short currentLevel;

    @Min(value = 1, message = "Level mục tiêu tối thiểu là 1")
    @Max(value = 5, message = "Level mục tiêu tối đa là 5")
    private Short targetLevel;

    private Boolean isActive;

    private UUID goalId;
}
