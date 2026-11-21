package com.personalfinance.review.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.review.dto.WeeklyReviewRequest;
import com.personalfinance.review.dto.WeeklyReviewResponse;
import com.personalfinance.review.dto.WeeklySummaryResponse;
import com.personalfinance.review.service.WeeklyReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/weekly-reviews")
@RequiredArgsConstructor
public class WeeklyReviewController {

    private final WeeklyReviewService weeklyReviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WeeklyReviewResponse> createReview(@Valid @RequestBody WeeklyReviewRequest request) {
        return ApiResponse.success("Lưu đánh giá tuần thành công", weeklyReviewService.createOrUpdateReview(request));
    }

    @GetMapping
    public ApiResponse<Page<WeeklyReviewResponse>> listReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(weeklyReviewService.listReviews(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<WeeklyReviewResponse> getReviewById(@PathVariable UUID id) {
        return ApiResponse.success(weeklyReviewService.getReviewById(id));
    }

    @GetMapping("/by-week/{date}")
    public ApiResponse<WeeklyReviewResponse> getReviewByWeek(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(weeklyReviewService.getReviewByWeek(date));
    }

    @PutMapping("/{id}")
    public ApiResponse<WeeklyReviewResponse> updateReview(
            @PathVariable UUID id,
            @Valid @RequestBody WeeklyReviewRequest request
    ) {
        return ApiResponse.success("Cập nhật đánh giá tuần thành công", weeklyReviewService.updateReviewById(id, request));
    }

    @PatchMapping("/{id}/complete")
    public ApiResponse<WeeklyReviewResponse> completeReview(@PathVariable UUID id) {
        return ApiResponse.success("Hoàn thành đánh giá tuần", weeklyReviewService.completeReview(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReview(@PathVariable UUID id) {
        weeklyReviewService.deleteReview(id);
        return ApiResponse.success("Xóa đánh giá tuần thành công", null);
    }

    @GetMapping("/summary")
    public ApiResponse<WeeklySummaryResponse> getWeeklySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(weeklyReviewService.getWeeklySummary(date));
    }

    @GetMapping("/{id}/summary")
    public ApiResponse<WeeklySummaryResponse> getSummaryForReview(@PathVariable UUID id) {
        WeeklyReviewResponse review = weeklyReviewService.getReviewById(id);
        return ApiResponse.success(weeklyReviewService.getWeeklySummary(review.getWeekStartDate()));
    }
}
