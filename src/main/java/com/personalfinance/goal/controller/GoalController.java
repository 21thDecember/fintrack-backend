package com.personalfinance.goal.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.goal.dto.GoalProgressResponse;
import com.personalfinance.goal.dto.GoalRequest;
import com.personalfinance.goal.dto.GoalResponse;
import com.personalfinance.goal.dto.GoalStatisticsResponse;
import com.personalfinance.goal.dto.MilestoneRequest;
import com.personalfinance.goal.dto.MilestoneResponse;
import com.personalfinance.goal.entity.GoalCategory;
import com.personalfinance.goal.entity.GoalPriority;
import com.personalfinance.goal.entity.GoalStatus;
import com.personalfinance.goal.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        return ApiResponse.success("Tạo mục tiêu thành công", goalService.createGoal(request));
    }

    @GetMapping
    public ApiResponse<List<GoalResponse>> getGoals(
            @RequestParam(required = false) GoalCategory category,
            @RequestParam(required = false) GoalStatus status,
            @RequestParam(required = false) GoalPriority priority
    ) {
        return ApiResponse.success(goalService.getGoals(category, status, priority));
    }

    @GetMapping("/{id}")
    public ApiResponse<GoalResponse> getGoalById(@PathVariable UUID id) {
        return ApiResponse.success(goalService.getGoalById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<GoalResponse> updateGoal(
            @PathVariable UUID id,
            @Valid @RequestBody GoalRequest request
    ) {
        return ApiResponse.success("Cập nhật mục tiêu thành công", goalService.updateGoal(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGoal(@PathVariable UUID id) {
        goalService.deleteGoal(id);
        return ApiResponse.success("Xóa mục tiêu thành công", null);
    }

    @PostMapping("/{id}/milestones")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MilestoneResponse> createMilestone(
            @PathVariable UUID id,
            @Valid @RequestBody MilestoneRequest request
    ) {
        return ApiResponse.success("Thêm cột mốc thành công", goalService.createMilestone(id, request));
    }

    @PutMapping("/{id}/milestones/{milestoneId}")
    public ApiResponse<MilestoneResponse> updateMilestone(
            @PathVariable UUID id,
            @PathVariable UUID milestoneId,
            @Valid @RequestBody MilestoneRequest request
    ) {
        return ApiResponse.success("Cập nhật cột mốc thành công", goalService.updateMilestone(id, milestoneId, request));
    }

    @DeleteMapping("/{id}/milestones/{milestoneId}")
    public ApiResponse<Void> deleteMilestone(
            @PathVariable UUID id,
            @PathVariable UUID milestoneId
    ) {
        goalService.deleteMilestone(id, milestoneId);
        return ApiResponse.success("Xóa cột mốc thành công", null);
    }

    @GetMapping("/{id}/progress")
    public ApiResponse<GoalProgressResponse> getProgress(@PathVariable UUID id) {
        return ApiResponse.success(goalService.getProgress(id));
    }

    @GetMapping("/statistics")
    public ApiResponse<GoalStatisticsResponse> getStatistics() {
        return ApiResponse.success(goalService.getStatistics());
    }
}
