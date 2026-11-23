package com.personalfinance.learning.dto;

import com.personalfinance.learning.entity.CoursePriority;
import com.personalfinance.learning.entity.CourseStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    @NotBlank(message = "Tên khóa học không được để trống")
    @Size(max = 255, message = "Tên khóa học tối đa 255 ký tự")
    private String title;

    private String description;

    @Size(max = 150, message = "Nhà cung cấp tối đa 150 ký tự")
    private String provider;

    @Size(max = 2048, message = "URL tối đa 2048 ký tự")
    private String url;

    @Size(max = 100, message = "Danh mục tối đa 100 ký tự")
    private String category;

    private CourseStatus status;

    private CoursePriority priority;

    private LocalDate startDate;

    private LocalDate targetDate;

    @DecimalMin(value = "0.0", message = "Thời lượng dự kiến phải lớn hơn hoặc bằng 0")
    private BigDecimal estimatedHours;

    @Min(value = 0, message = "Tổng số bài học phải lớn hơn hoặc bằng 0")
    private Integer totalLessons;

    @Min(value = 0, message = "Số bài đã hoàn thành phải lớn hơn hoặc bằng 0")
    private Integer completedLessons;

    private UUID goalId;

    private String notes;
}
