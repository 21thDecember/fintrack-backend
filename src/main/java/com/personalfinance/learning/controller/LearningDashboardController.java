package com.personalfinance.learning.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.learning.dto.LearningDashboardResponse;
import com.personalfinance.learning.dto.LearningStatisticsResponse;
import com.personalfinance.learning.service.LearningDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningDashboardController {

    private final LearningDashboardService dashboardService;

    @GetMapping("/dashboard")
    public ApiResponse<LearningDashboardResponse> getDashboard() {
        return ApiResponse.success(dashboardService.getDashboard());
    }

    @GetMapping("/statistics")
    public ApiResponse<LearningStatisticsResponse> getStatistics() {
        return ApiResponse.success(dashboardService.getStatistics());
    }
}
