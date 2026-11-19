package com.personalfinance.integration.sepay.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.integration.sepay.dto.SePayWebhookPayload;
import com.personalfinance.integration.sepay.service.SePayService;
import com.personalfinance.transaction.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class SePayWebhookController {

    private final SePayService sePayService;

    @PostMapping("/sepay")
    public ApiResponse<Map<String, Object>> handleSePayWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SePayWebhookPayload payload) {

        log.info("Nhận được SePay webhook: Transaction ID={}, Số tiền={}",
                payload.getId(), payload.getTransferAmount());

        sePayService.verifyWebhookAuth(authHeader);
        Transaction tx = sePayService.processWebhook(payload);

        return ApiResponse.success("Xử lý giao dịch SePay thành công", Map.of(
                "success", true,
                "transactionId", tx.getId(),
                "externalTransactionId", tx.getExternalTransactionId()
        ));
    }
}
