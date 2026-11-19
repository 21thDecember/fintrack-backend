package com.personalfinance.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAlertDto {

    private String id;
    private String type; // DEBT_DUE, DEBT_OVERDUE, BUDGET_WARNING, BUDGET_EXCEEDED, UNCLASSIFIED_TX
    private String title;
    private String message;
    private String severity; // INFO, WARNING, CRITICAL
    private OffsetDateTime timestamp;
    private Object payload;
}
