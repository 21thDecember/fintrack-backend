package com.personalfinance.common.controller;

import com.personalfinance.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Controller kiểm tra trạng thái hoạt động (Health check / Ping)
 * Dùng cho các dịch vụ giám sát (UptimeRobot, Cron-job.org) để giữ server không bị sleep trên Render
 */
@RestController
@RequestMapping
public class HealthController {

    @GetMapping({"/api/health", "/api/ping", "/health"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> status = Map.of(
                "status", "UP",
                "service", "fintrack-backend",
                "timestamp", OffsetDateTime.now()
        );
        return ResponseEntity.ok(ApiResponse.success("FinTrack backend is healthy", status));
    }
}
