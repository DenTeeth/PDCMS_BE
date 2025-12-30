package com.dental.clinic.management.payment.service;

import com.dental.clinic.management.exception.ResourceNotFoundException;
import com.dental.clinic.management.payment.domain.Invoice;
import com.dental.clinic.management.payment.domain.Payment;
import com.dental.clinic.management.payment.dto.CreatePaymentRequest;
import com.dental.clinic.management.payment.dto.PaymentResponse;
import com.dental.clinic.management.payment.enums.PaymentMethod;
import com.dental.clinic.management.payment.repository.InvoiceRepository;
import com.dental.clinic.management.payment.repository.PaymentRepository;
import com.dental.clinic.management.utils.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    /**
     * Tao payment moi va cap nhat invoice
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for invoice: {}", request.getInvoiceId());

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("INVOICE_NOT_FOUND",
                        "Invoice not found: " + request.getInvoiceId()));

        Payment payment = Payment.builder()
                .paymentCode(generatePaymentCode())
                .invoice(invoice)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now())
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .createdBy(1) // Default system user for webhook payments
                .build();

        payment = paymentRepository.save(payment);

        invoiceService.updateInvoicePayment(invoice.getInvoiceId(), request.getAmount());

        log.info("Created payment: {} for invoice: {}, amount: {}",
                payment.getPaymentCode(), invoice.getInvoiceCode(), request.getAmount());

        return mapToResponse(payment);
    }

    /**
     * Lay danh sach payments cua invoice
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByInvoice(Integer invoiceId) {
        log.info("Getting payments for invoice: {}", invoiceId);
        List<Payment> payments = paymentRepository.findByInvoice_InvoiceIdOrderByPaymentDateDesc(invoiceId);
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lay chi tiet payment
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByCode(String paymentCode) {
        log.info("Getting payment: {}", paymentCode);
        Payment payment = paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(
                        () -> new ResourceNotFoundException("PAYMENT_NOT_FOUND", "Payment not found: " + paymentCode));
        return mapToResponse(payment);
    }

    /**
     * Generate payment code: PAY-YYYYMMDD-XXX
     */
    private String generatePaymentCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PAY-" + datePart + "-";

        int sequence = 1;
        String paymentCode;
        do {
            paymentCode = prefix + String.format("%03d", sequence);
            sequence++;
        } while (paymentRepository.existsByPaymentCode(paymentCode));

        return paymentCode;
    }

    /**
     * Map entity to response DTO
     */
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .paymentCode(payment.getPaymentCode())
                .invoiceId(payment.getInvoice().getInvoiceId())
                .invoiceCode(payment.getInvoice().getInvoiceCode())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .referenceNumber(payment.getReferenceNumber())
                .notes(payment.getNotes())
                .createdBy(payment.getCreatedBy())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
