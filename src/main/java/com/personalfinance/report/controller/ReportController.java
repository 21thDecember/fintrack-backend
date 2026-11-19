package com.personalfinance.report.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.report.dto.MonthlyReportResponse;
import com.personalfinance.report.dto.NetWorthResponse;
import com.personalfinance.report.dto.WeeklyReportResponse;
import com.personalfinance.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/weekly")
    public ApiResponse<WeeklyReportResponse> getWeeklyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        WeeklyReportResponse report = reportService.getWeeklyReport(date);
        return ApiResponse.success(report);
    }

    @GetMapping("/monthly")
    public ApiResponse<MonthlyReportResponse> getMonthlyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        MonthlyReportResponse report = reportService.getMonthlyReport(year, month);
        return ApiResponse.success(report);
    }

    @GetMapping("/net-worth")
    public ApiResponse<NetWorthResponse> getNetWorth() {
        NetWorthResponse netWorth = reportService.getNetWorth();
        return ApiResponse.success(netWorth);
    }
}
