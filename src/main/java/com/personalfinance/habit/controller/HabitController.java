package com.personalfinance.habit.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.habit.dto.CheckinRequest;
import com.personalfinance.habit.dto.CheckinResponse;
import com.personalfinance.habit.dto.HabitRequest;
import com.personalfinance.habit.dto.HabitResponse;
import com.personalfinance.habit.dto.HabitStatisticsResponse;
import com.personalfinance.habit.dto.TodayHabitResponse;
import com.personalfinance.habit.service.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HabitResponse> createHabit(@Valid @RequestBody HabitRequest request) {
        return ApiResponse.success("Tạo thói quen thành công", habitService.createHabit(request));
    }

    @GetMapping
    public ApiResponse<List<HabitResponse>> getHabits(
            @RequestParam(required = false) UUID goalId,
            @RequestParam(required = false) Boolean activeOnly
    ) {
        return ApiResponse.success(habitService.getHabits(goalId, activeOnly));
    }

    @GetMapping("/today")
    public ApiResponse<List<TodayHabitResponse>> getTodayHabits(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(habitService.getHabitsByDate(date));
    }

    @GetMapping("/by-date")
    public ApiResponse<List<TodayHabitResponse>> getHabitsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(habitService.getHabitsByDate(date));
    }

    @GetMapping("/check-ins")
    public ApiResponse<List<CheckinResponse>> getCheckins(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(habitService.getCheckins(from, to));
    }

    @GetMapping("/statistics")
    public ApiResponse<HabitStatisticsResponse> getStatistics() {
        return ApiResponse.success(habitService.getStatistics());
    }

    @GetMapping("/{id}")
    public ApiResponse<HabitResponse> getHabitById(@PathVariable UUID id) {
        return ApiResponse.success(habitService.getHabitById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<HabitResponse> updateHabit(
            @PathVariable UUID id,
            @Valid @RequestBody HabitRequest request
    ) {
        return ApiResponse.success("Cập nhật thói quen thành công", habitService.updateHabit(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteHabit(@PathVariable UUID id) {
        habitService.deleteHabit(id);
        return ApiResponse.success("Xóa thói quen thành công", null);
    }

    @PostMapping("/{id}/check-ins")
    public ApiResponse<HabitResponse> checkin(
            @PathVariable UUID id,
            @RequestBody(required = false) CheckinRequest request
    ) {
        return ApiResponse.success("Điểm danh thói quen thành công", habitService.checkin(id, request));
    }

    @DeleteMapping("/{id}/check-ins/{date}")
    public ApiResponse<Void> removeCheckin(
            @PathVariable UUID id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        habitService.removeCheckin(id, date);
        return ApiResponse.success("Đã hủy điểm danh", null);
    }
}
