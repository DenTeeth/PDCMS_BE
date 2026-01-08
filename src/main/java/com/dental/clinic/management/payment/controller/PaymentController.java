package com.dental.clinic.management.payment.controller;

import com.dental.clinic.management.payment.dto.CreatePaymentRequest;
import com.dental.clinic.management.payment.dto.PaymentResponse;
import com.dental.clinic.management.payment.service.PaymentService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for processing payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PAYMENT')") // RECEPTIONIST, ADMIN
    @ApiMessage("Tạo thanh toán thành công")
    @Operation(summary = "Create payment", description = "Create a new payment for an invoice")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        log.info("REST request to create payment for invoice: {}", request.getInvoiceId());
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyAuthority('VIEW_PAYMENT_ALL', 'VIEW_INVOICE_ALL')") // RECEPTIONIST, ACCOUNTANT, ADMIN
    @ApiMessage("Lấy danh sách thanh toán thành công")
    @Operation(summary = "Get payments by invoice", description = "Get all payments for a specific invoice")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByInvoice(@PathVariable Integer invoiceId) {
        log.info("REST request to get payments for invoice: {}", invoiceId);
        List<PaymentResponse> payments = paymentService.getPaymentsByInvoice(invoiceId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentCode}")
    @PreAuthorize("hasAnyAuthority('VIEW_PAYMENT_ALL', 'VIEW_INVOICE_ALL')") // RECEPTIONIST, ACCOUNTANT, ADMIN
    @ApiMessage("Lấy thông tin thanh toán thành công")
    @Operation(summary = "Get payment by code", description = "Get payment details by payment code")
    public ResponseEntity<PaymentResponse> getPaymentByCode(@PathVariable String paymentCode) {
        log.info("REST request to get payment: {}", paymentCode);
        PaymentResponse payment = paymentService.getPaymentByCode(paymentCode);
        return ResponseEntity.ok(payment);
    }
}
