package com.personalfinance.mood.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.mood.dto.MoodCalendarDayResponse;
import com.personalfinance.mood.dto.MoodEntryRequest;
import com.personalfinance.mood.dto.MoodEntryResponse;
import com.personalfinance.mood.dto.MoodStatisticsResponse;
import com.personalfinance.mood.service.MoodService;
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
@RequestMapping("/api/moods")
@RequiredArgsConstructor
public class MoodController {

    private final MoodService moodService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MoodEntryResponse> createMood(@Valid @RequestBody MoodEntryRequest request) {
        return ApiResponse.success("Ghi nhận tâm trạng thành công", moodService.createOrUpdateMood(request));
    }

    @GetMapping
    public ApiResponse<List<MoodEntryResponse>> listMoods(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(moodService.listMoods(from, to));
    }

    @GetMapping("/by-date/{date}")
    public ApiResponse<MoodEntryResponse> getMoodByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(moodService.getMoodByDate(date));
    }

    @GetMapping("/{id}")
    public ApiResponse<MoodEntryResponse> getMoodById(@PathVariable UUID id) {
        return ApiResponse.success(moodService.getMoodById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<MoodEntryResponse> updateMood(
            @PathVariable UUID id,
            @Valid @RequestBody MoodEntryRequest request
    ) {
        return ApiResponse.success("Cập nhật tâm trạng thành công", moodService.updateMoodById(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMood(@PathVariable UUID id) {
        moodService.deleteMood(id);
        return ApiResponse.success("Xóa tâm trạng thành công", null);
    }

    @GetMapping("/calendar")
    public ApiResponse<List<MoodCalendarDayResponse>> getCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(moodService.getCalendarData(from, to));
    }

    @GetMapping("/statistics")
    public ApiResponse<MoodStatisticsResponse> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(moodService.getStatistics(from, to));
    }
}
