package com.dental.clinic.management.payment.controller;

import com.dental.clinic.management.payment.dto.SePayWebhookData;
import com.dental.clinic.management.payment.service.SePayWebhookService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SePay Webhook Controller
 *
 * Endpoint: POST /api/v1/webhooks/sepay
 *
 * Nhận thông báo từ SePay khi có giao dịch chuyển khoản vào tài khoản ngân
 * hàng.
 * SePay tự động gửi POST request với dữ liệu giao dịch.
 *
 * BẢO MẬT:
 * - SePay đã bảo mật webhook bằng IP whitelist
 * - Không cần API Key validation ở đây
 * - Chỉ cần return {"success": true} với status 200 hoặc 201
 *
 * Reference: https://docs.sepay.vn/tich-hop-webhooks.html
 */
@RestController
@RequestMapping("/api/v1/webhooks/sepay")
@RequiredArgsConstructor
@Slf4j
public class SePayWebhookController {

    private final SePayWebhookService sePayWebhookService;

    /**
     * Xử lý webhook từ SePay
     *
     * SePay gửi POST request khi phát hiện giao dịch chuyển khoản.
     * Backend phải return {"success": true} để SePay biết đã nhận thành công.
     *
     * @param webhookData Dữ liệu giao dịch từ SePay
     * @return ResponseEntity với {"success": true}
     */
    @PostMapping
    @ApiMessage("Webhook processed successfully")
    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody SePayWebhookData webhookData) {

        log.info("🔔 Received SePay webhook - ID: {}, Gateway: {}, Amount: {}, Content: {}",
                webhookData.getId(),
                webhookData.getGateway(),
                webhookData.getTransferAmount(),
                webhookData.getContent());

        try {
            // Xử lý webhook: extract payment code → find invoice → update status
            sePayWebhookService.processWebhook(webhookData);

            // QUAN TRỌNG: Phải return success để SePay không retry
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Webhook processed successfully"));

        } catch (Exception e) {
            log.error("❌ Error processing SePay webhook: ", e);

            // Vẫn return success để tránh SePay retry (gây duplicate)
            // Log error để admin xử lý thủ công
            return ResponseEntity.ok()
                    .body(Map.of(
                            "success", true,
                            "message", "Logged for manual investigation",
                            "error", e.getMessage()));
        }
    }
}
