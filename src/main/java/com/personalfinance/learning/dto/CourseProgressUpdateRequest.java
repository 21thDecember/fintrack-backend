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
public class CourseProgressUpdateRequest {

    @NotNull(message = "Số bài học hoàn thành không được để trống")
    @Min(value = 0, message = "Số bài học hoàn thành phải >= 0")
    private Integer completedLessons;

    @Min(value = 0, message = "Tổng số bài học phải >= 0")
    private Integer totalLessons;
}
