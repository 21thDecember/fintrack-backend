package com.personalfinance.notification.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.notification.dto.NotificationAlertDto;
import com.personalfinance.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationAlertDto>> getActiveAlerts() {
        List<NotificationAlertDto> alerts = notificationService.getActiveAlerts();
        return ApiResponse.success(alerts);
    }
}
