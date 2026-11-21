package com.personalfinance.mood.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodEntryRequest {

    @NotNull(message = "Ngày ghi nhận không được để trống")
    private LocalDate recordedDate;

    @NotNull(message = "Điểm tâm trạng không được để trống")
    @Min(value = 1, message = "Điểm tâm trạng từ 1 đến 5")
    @Max(value = 5, message = "Điểm tâm trạng từ 1 đến 5")
    private Integer moodScore;

    @Min(value = 1, message = "Điểm năng lượng từ 1 đến 5")
    @Max(value = 5, message = "Điểm năng lượng từ 1 đến 5")
    private Integer energyScore;

    private String note;
}
