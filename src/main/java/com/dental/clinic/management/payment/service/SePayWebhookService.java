package com.dental.clinic.management.payment.service;

// import com.dental.clinic.management.exception.ResourceNotFoundException;
import com.dental.clinic.management.payment.domain.Invoice;
import com.dental.clinic.management.payment.domain.Payment;
import com.dental.clinic.management.payment.domain.PaymentTransaction;
import com.dental.clinic.management.payment.dto.SePayWebhookData;
import com.dental.clinic.management.payment.enums.PaymentMethod;
import com.dental.clinic.management.payment.enums.PaymentTransactionStatus;
import com.dental.clinic.management.payment.repository.InvoiceRepository;
import com.dental.clinic.management.payment.repository.PaymentRepository;
import com.dental.clinic.management.payment.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SePay Webhook Service
 * Handles bank transfer notifications from SePay
 * Reference: https://docs.sepay.vn/tich-hop-webhooks.html
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SePayWebhookService {

    // Payment code pattern: PDCMSyymmddxy (e.g., PDCMS25123001)
    // yy: 2-digit year, mm: month, dd: day, xy: sequence 01-99
    private static final Pattern PAYMENT_CODE_PATTERN = Pattern.compile("PDCMS(\\d{8})");

    private final ObjectMapper objectMapper;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final InvoiceService invoiceService;

    /**
     * Process SePay webhook notification
     * 
     * @param webhookData Webhook data from SePay
     */
    @Transactional
    public void processWebhook(SePayWebhookData webhookData) {
        log.info("Processing SePay webhook - ID: {}, Amount: {}, Content: {}",
                webhookData.getId(), webhookData.getTransferAmount(), webhookData.getContent());

        // Check if webhook already processed (duplicate detection)
        if (isWebhookProcessed(webhookData.getId())) {
            log.warn("Webhook already processed: {}", webhookData.getId());
            return;
        }

        // Only process incoming transfers
        if (!"in".equals(webhookData.getTransferType())) {
            log.info("Skipping outgoing transfer: {}", webhookData.getId());
            return;
        }

        // Extract payment code from content
        String paymentCode = extractPaymentCode(webhookData);
        if (paymentCode == null) {
            log.warn("No valid payment code found in webhook content: {}", webhookData.getContent());
            return;
        }

        log.info("Extracted payment code: {}", paymentCode);

        // Find invoice by payment code
        Invoice invoice = findInvoiceByPaymentCode(paymentCode);
        if (invoice == null) {
            log.error("Invoice not found for payment code: {}", paymentCode);
            return;
        }

        log.info("Found invoice: {}, Remaining debt: {}", invoice.getInvoiceCode(), invoice.getRemainingDebt());

        // Validate transfer amount
        if (webhookData.getTransferAmount().compareTo(invoice.getRemainingDebt()) < 0) {
            log.warn("Transfer amount {} is less than remaining debt {}. Creating partial payment.",
                    webhookData.getTransferAmount(), invoice.getRemainingDebt());
        }

        // Create payment record
        Payment payment = Payment.builder()
                .paymentCode("PAY-" + webhookData.getId())
                .invoice(invoice)
                .amount(webhookData.getTransferAmount())
                .paymentMethod(PaymentMethod.SEPAY)
                .paymentDate(LocalDateTime.now())
                .referenceNumber(webhookData.getReferenceCode())
                .notes("SePay webhook - " + webhookData.getGateway())
                .createdBy(1) // System user
                .createdAt(LocalDateTime.now())
                .build();
        payment = paymentRepository.save(payment);

        log.info("Created payment: {}", payment.getPaymentCode());

        // Create payment transaction
        PaymentTransaction transaction = PaymentTransaction.builder()
                .payment(payment)
                .amount(webhookData.getTransferAmount())
                .status(PaymentTransactionStatus.SUCCESS)
                .paymentLinkId(webhookData.getId().toString())
                .callbackData(serializeToJson(webhookData))
                .build();
        paymentTransactionRepository.save(transaction);

        log.info("Created transaction for payment: {}", payment.getPaymentCode());

        // Update invoice payment status
        invoiceService.updateInvoicePayment(invoice.getInvoiceId(), payment.getAmount());

        log.info("Payment processed successfully for invoice: {}", invoice.getInvoiceCode());
    }

    /**
     * Extract payment code from webhook content
     * Supports both "code" field and parsing from "content" field
     */
    private String extractPaymentCode(SePayWebhookData webhookData) {
        // First check if SePay already extracted the code
        if (webhookData.getCode() != null && !webhookData.getCode().isEmpty()) {
            return webhookData.getCode();
        }

        // Parse from content field
        if (webhookData.getContent() != null) {
            Matcher matcher = PAYMENT_CODE_PATTERN.matcher(webhookData.getContent());
            if (matcher.find()) {
                return matcher.group(0); // Returns "PDCMS123456"
            }
        }

        // Parse from description field
        if (webhookData.getDescription() != null) {
            Matcher matcher = PAYMENT_CODE_PATTERN.matcher(webhookData.getDescription());
            if (matcher.find()) {
                return matcher.group(0);
            }
        }

        return null;
    }

    /**
     * Find invoice by payment code stored in notes field
     */
    private Invoice findInvoiceByPaymentCode(String paymentCode) {
        // Search in invoice notes field
        Optional<Invoice> invoice = invoiceRepository.findByNotesContaining(paymentCode);

        if (invoice.isEmpty()) {
            log.warn("Invoice not found for payment code: {}", paymentCode);
        }

        return invoice.orElse(null);
    }

    /**
     * Check if webhook already processed (duplicate detection)
     */
    private boolean isWebhookProcessed(Long webhookId) {
        // Check if transaction with this webhook ID exists
        return paymentTransactionRepository.findByPaymentLinkId(webhookId.toString()).isPresent();
    }

    /**
     * Serialize object to JSON string
     */
    private String serializeToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Error serializing object to JSON: ", e);
            return null;
        }
    }
}
