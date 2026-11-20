package com.personalfinance.goal.dto;

import com.personalfinance.goal.entity.MilestoneStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneRequest {

    @NotBlank(message = "Tiêu đề cột mốc không được để trống")
    @Size(max = 255, message = "Tiêu đề cột mốc không quá 255 ký tự")
    private String title;

    private String description;

    private LocalDate dueDate;

    private MilestoneStatus status;

    private Integer displayOrder;
}
