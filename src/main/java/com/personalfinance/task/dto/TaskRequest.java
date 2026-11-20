package com.personalfinance.task.dto;

import com.personalfinance.task.entity.TaskPriority;
import com.personalfinance.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Tiêu đề công việc không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    private String description;

    private TaskPriority priority;

    private TaskStatus status;

    private OffsetDateTime startDate;

    private OffsetDateTime dueDate;

    private UUID goalId;

    private UUID parentTaskId;

    private Integer estimatedMinutes;
}
