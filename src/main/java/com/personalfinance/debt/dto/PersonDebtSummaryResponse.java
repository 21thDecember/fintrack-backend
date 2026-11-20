package com.personalfinance.debt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Thống kê tổng hợp nợ theo từng người liên quan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDebtSummaryResponse {

    private UUID personId;
    private String personName;
    private String phone;
    private Long totalReceivable; // Tổng tiền người này đang nợ mình
    private Long totalPayable;    // Tổng tiền mình đang nợ người này
    private Long netAmount;       // Số nợ ròng (dương: họ nợ mình, âm: mình nợ họ)
    private long openDebtsCount;  // Số khoản nợ đang mở/chưa thanh toán xong

    public Long getReceivableAmount() {
        return totalReceivable;
    }

    public Long getPayableAmount() {
        return totalPayable;
    }

    public long getActiveDebtsCount() {
        return openDebtsCount;
    }
}
