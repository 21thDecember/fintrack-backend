package com.personalfinance.learning.dto;

import com.personalfinance.learning.entity.SessionSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSessionRequest {

    @NotBlank(message = "Tiêu đề phiên học không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String title;

    private String description;

    @NotNull(message = "Ngày học không được để trống")
    private LocalDate learningDate;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    @NotNull(message = "Thời lượng học không được để trống")
    @Min(value = 1, message = "Thời lượng học phải ít nhất 1 phút")
    @Max(value = 1440, message = "Thời lượng học không được vượt quá 1440 phút (24h)")
    private Integer durationMinutes;

    private UUID courseId;

    private UUID bookId;

    private UUID skillId;

    private SessionSource source;

    private String notes;
}
