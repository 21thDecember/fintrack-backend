package com.personalfinance.task.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.common.response.PageResponse;
import com.personalfinance.task.dto.TaskRequest;
import com.personalfinance.task.dto.TaskResponse;
import com.personalfinance.task.dto.TaskStatisticsResponse;
import com.personalfinance.task.dto.TaskStatusUpdateRequest;
import com.personalfinance.task.entity.TaskPriority;
import com.personalfinance.task.entity.TaskStatus;
import com.personalfinance.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        return ApiResponse.success("Tạo công việc thành công", taskService.createTask(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<TaskResponse>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dueDateTo,
            @RequestParam(required = false) UUID goalId,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) Boolean parentOnly
    ) {
        return ApiResponse.success(taskService.getTasks(page, size, keyword, status, priority, dueDateFrom, dueDateTo, goalId, view, parentOnly));
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskResponse> getTaskById(@PathVariable UUID id) {
        return ApiResponse.success(taskService.getTaskById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskResponse> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskRequest request
    ) {
        return ApiResponse.success("Cập nhật công việc thành công", taskService.updateTask(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<TaskResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TaskStatusUpdateRequest request
    ) {
        return ApiResponse.success("Cập nhật trạng thái thành công", taskService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ApiResponse.success("Xóa công việc thành công", null);
    }

    @GetMapping("/statistics")
    public ApiResponse<TaskStatisticsResponse> getStatistics() {
        return ApiResponse.success(taskService.getStatistics());
    }
}
