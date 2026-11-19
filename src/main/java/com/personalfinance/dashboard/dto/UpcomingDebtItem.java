package com.personalfinance.dashboard.dto;

import com.personalfinance.debt.entity.DebtStatus;
import com.personalfinance.debt.entity.DebtType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingDebtItem {

    private UUID debtId;
    private String personName;
    private DebtType type;
    private Long remainingAmount;
    private LocalDate dueDate;
    private Long daysRemaining; // Âm là quá hạn
    private DebtStatus status;
}
