package com.dental.clinic.management.payment.controller;

import com.dental.clinic.management.payment.dto.SePayWebhookData;
import com.dental.clinic.management.payment.service.SePayWebhookService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SePay Webhook Controller
 * Receives bank transfer notifications from SePay
 * Reference: https://docs.sepay.vn/tich-hop-webhooks.html
 */
@RestController
@RequestMapping("/api/v1/webhooks/sepay")
@RequiredArgsConstructor
@Slf4j
public class SePayWebhookController {

    @Value("${sepay.api-key:}")
    private String sePayApiKey;

    private final SePayWebhookService sePayWebhookService;

    /**
     * Handle SePay webhook notification
     * SePay sends POST request with Authorization header: "Apikey YOUR_API_KEY"
     * Must return {"success": true} with status 200 or 201
     */
    @PostMapping
    @ApiMessage("Webhook processed successfully")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody SePayWebhookData webhookData,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("Received SePay webhook - ID: {}, Amount: {}",
                webhookData.getId(), webhookData.getTransferAmount());

        // Validate API Key
        if (authorization == null || !authorization.equals("Apikey " + sePayApiKey)) {
            log.error("Invalid API Key in webhook request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid API Key"));
        }

        try {
            sePayWebhookService.processWebhook(webhookData);

            // SePay requires {"success": true} with status 200 or 201
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Webhook processed successfully"));

        } catch (Exception e) {
            log.error("Error processing SePay webhook: ", e);

            // Still return success to prevent SePay from retrying
            // Log error for manual investigation
            return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "Logged for investigation", "error", e.getMessage()));
        }
    }
}
