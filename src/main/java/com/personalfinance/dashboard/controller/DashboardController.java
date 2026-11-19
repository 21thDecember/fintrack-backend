package com.personalfinance.dashboard.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.dashboard.dto.DashboardResponse;
import com.personalfinance.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiResponse<DashboardResponse> getDashboard(
            @RequestParam(required = false, defaultValue = "THIS_MONTH") String period) {
        DashboardResponse response = dashboardService.getDashboardData(period);
        return ApiResponse.success(response);
    }
}
