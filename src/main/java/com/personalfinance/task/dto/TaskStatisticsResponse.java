package com.personalfinance.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatisticsResponse {

    private long totalTasks;
    private long todoTasks;
    private long inProgressTasks;
    private long completedTasks;
    private long cancelledTasks;
    private long overdueTasks;
    private double completionRate;
}
