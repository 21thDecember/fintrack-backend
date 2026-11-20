package com.personalfinance.task.dto;

import com.personalfinance.task.entity.TaskPriority;
import com.personalfinance.task.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private UUID id;
    private UUID userId;
    private UUID parentTaskId;
    private UUID goalId;
    private String goalTitle;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private OffsetDateTime startDate;
    private OffsetDateTime dueDate;
    private Integer estimatedMinutes;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private boolean isOverdue;

    private List<TaskResponse> subtasks;
    private int subtaskCount;
    private int completedSubtaskCount;
}
