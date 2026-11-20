package com.personalfinance.habit.dto;

import com.personalfinance.habit.entity.HabitFrequency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitRequest {

    @NotBlank(message = "Tên thói quen không được để trống")
    @Size(max = 255, message = "Tên thói quen không quá 255 ký tự")
    private String name;

    private String description;

    private HabitFrequency frequency;

    @Min(value = 1, message = "Mục tiêu thực hiện tối thiểu 1 lần")
    private Integer targetCount;

    private List<String> activeDays;

    private LocalDate startDate;

    private LocalDate endDate;

    private UUID goalId;

    private Boolean isActive;
}
