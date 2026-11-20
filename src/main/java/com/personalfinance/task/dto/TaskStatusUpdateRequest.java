package com.personalfinance.task.dto;

import com.personalfinance.task.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusUpdateRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private TaskStatus status;
}
