package com.dental.clinic.management.payment.service;

import com.dental.clinic.management.exception.ResourceNotFoundException;
import com.dental.clinic.management.payment.domain.Invoice;
import com.dental.clinic.management.payment.domain.InvoiceItem;
import com.dental.clinic.management.payment.dto.CreateInvoiceRequest;
import com.dental.clinic.management.payment.dto.InvoiceResponse;
import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
// import com.dental.clinic.management.payment.enums.InvoiceType;
import com.dental.clinic.management.payment.repository.InvoiceItemRepository;
import com.dental.clinic.management.payment.repository.InvoiceRepository;
// import com.dental.clinic.management.utils.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final VietQRService vietQRService;

    /**
     * Tao invoice moi
     */
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        log.info("Creating new invoice for patient: {}", request.getPatientId());

        // Generate payment code for SePay webhook matching
        String paymentCode = generatePaymentCode();

        Invoice invoice = Invoice.builder()
                .invoiceCode(generateInvoiceCode())
                .invoiceType(request.getInvoiceType())
                .patientId(request.getPatientId())
                .appointmentId(request.getAppointmentId())
                .treatmentPlanId(request.getTreatmentPlanId())
                .phaseNumber(request.getPhaseNumber())
                .installmentNumber(request.getInstallmentNumber())
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .remainingDebt(BigDecimal.ZERO)
                .paymentStatus(InvoicePaymentStatus.PENDING_PAYMENT)
                .dueDate(request.getDueDate())
                .notes("Payment Code: " + paymentCode + (request.getNotes() != null ? " | " + request.getNotes() : ""))
                .createdBy(1) // Default system user
                .build();

        invoice = invoiceRepository.save(invoice);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateInvoiceRequest.InvoiceItemDto itemDto : request.getItems()) {
            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .serviceId(itemDto.getServiceId())
                    .serviceCode(itemDto.getServiceCode())
                    .serviceName(itemDto.getServiceName())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .notes(itemDto.getNotes())
                    .build();

            item.calculateSubtotal();
            invoiceItemRepository.save(item);

            totalAmount = totalAmount.add(item.getSubtotal());
        }

        invoice.setTotalAmount(totalAmount);
        invoice.setRemainingDebt(totalAmount);
        invoice = invoiceRepository.save(invoice);

        log.info("Created invoice: {} with total: {}", invoice.getInvoiceCode(), totalAmount);

        return mapToResponse(invoice);
    }

    /**
     * Lay danh sach invoices cua patient
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByPatient(Integer patientId) {
        log.info("Getting invoices for patient: {}", patientId);
        List<Invoice> invoices = invoiceRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        return invoices.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lay danh sach invoices cua appointment
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByAppointment(Integer appointmentId) {
        log.info("Getting invoices for appointment: {}", appointmentId);
        List<Invoice> invoices = invoiceRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
        return invoices.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lay chi tiet invoice
     */
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByCode(String invoiceCode) {
        log.info("Getting invoice: {}", invoiceCode);
        Invoice invoice = invoiceRepository.findByInvoiceCode(invoiceCode)
                .orElseThrow(
                        () -> new ResourceNotFoundException("INVOICE_NOT_FOUND", "Invoice not found: " + invoiceCode));
        return mapToResponse(invoice);
    }

    /**
     * Lay danh sach invoices chua thanh toan cua patient
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getUnpaidInvoicesByPatient(Integer patientId) {
        log.info("Getting unpaid invoices for patient: {}", patientId);
        List<Invoice> invoices = invoiceRepository.findUnpaidInvoicesByPatientId(patientId);
        return invoices.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate xem appointment co con invoice chua thanh toan khong
     */
    @Transactional(readOnly = true)
    public boolean hasUnpaidInvoices(Integer appointmentId) {
        List<Invoice> unpaidInvoices = invoiceRepository.findUnpaidInvoicesByAppointmentId(appointmentId);
        return !unpaidInvoices.isEmpty();
    }

    /**
     * Cap nhat payment status cua invoice
     */
    @Transactional
    public void updateInvoicePayment(Integer invoiceId, BigDecimal paidAmount) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("INVOICE_NOT_FOUND", "Invoice not found: " + invoiceId));

        invoice.setPaidAmount(invoice.getPaidAmount().add(paidAmount));
        invoice.recalculatePaymentStatus();

        invoiceRepository.save(invoice);
        log.info("Updated invoice {} payment: paid={}, remaining={}, status={}",
                invoice.getInvoiceCode(), invoice.getPaidAmount(),
                invoice.getRemainingDebt(), invoice.getPaymentStatus());
    }

    /**
     * Generate invoice code: INV-YYYYMMDD-SEQ
     */
    private String generateInvoiceCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "INV-" + datePart + "-";

        int sequence = 1;
        String invoiceCode;
        do {
            invoiceCode = prefix + String.format("%03d", sequence);
            sequence++;
        } while (invoiceRepository.existsByInvoiceCode(invoiceCode));

        return invoiceCode;
    }

    /**
     * Map entity to response DTO
     */
    private InvoiceResponse mapToResponse(Invoice invoice) {
        List<InvoiceResponse.InvoiceItemResponse> itemResponses = invoiceItemRepository
                .findByInvoice_InvoiceId(invoice.getInvoiceId())
                .stream()
                .map(item -> InvoiceResponse.InvoiceItemResponse.builder()
                        .itemId(item.getItemId())
                        .serviceId(item.getServiceId())
                        .serviceCode(item.getServiceCode())
                        .serviceName(item.getServiceName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .notes(item.getNotes())
                        .build())
                .collect(Collectors.toList());

        // Extract payment code from notes (format: "Payment Code: PDCMS123456 |
        // original notes")
        String paymentCode = extractPaymentCodeFromNotes(invoice.getNotes());

        // Generate QR code URL for unpaid/partial paid invoices
        String qrCodeUrl = null;
        if (paymentCode != null && invoice.getRemainingDebt().compareTo(BigDecimal.ZERO) > 0) {
            qrCodeUrl = vietQRService.generateQRUrl(invoice.getRemainingDebt().longValue(), paymentCode);
        }

        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .invoiceCode(invoice.getInvoiceCode())
                .invoiceType(invoice.getInvoiceType())
                .patientId(invoice.getPatientId())
                .appointmentId(invoice.getAppointmentId())
                .treatmentPlanId(invoice.getTreatmentPlanId())
                .phaseNumber(invoice.getPhaseNumber())
                .installmentNumber(invoice.getInstallmentNumber())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingDebt(invoice.getRemainingDebt())
                .paymentStatus(invoice.getPaymentStatus())
                .dueDate(invoice.getDueDate())
                .notes(invoice.getNotes())
                .paymentCode(paymentCode)
                .qrCodeUrl(qrCodeUrl)
                .createdBy(invoice.getCreatedBy())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .items(itemResponses)
                .build();
    }

    /**
     * Extract payment code from notes field
     * Format: "Payment Code: PDCMS25123001 | original notes"
     */
    private String extractPaymentCodeFromNotes(String notes) {
        if (notes == null || !notes.contains("Payment Code: ")) {
            return null;
        }

        try {
            String[] parts = notes.split("Payment Code: ");
            if (parts.length < 2) {
                return null;
            }

            String codePart = parts[1].split(" \\| ")[0].trim();
            // New format: PDCMS + 8 digits (yymmddxy)
            if (codePart.matches("PDCMS\\d{8}")) {
                return codePart;
            }
        } catch (Exception e) {
            log.warn("Failed to extract payment code from notes: {}", notes, e);
        }

        return null;
    }

    /**
     * Generate payment code for SePay webhook matching
     * Format: PDCMSyymmddxy
     * - yy: 2 digits year (e.g., 25 for 2025)
     * - mm: 2 digits month (e.g., 12 for December)
     * - dd: 2 digits day (e.g., 30)
     * - xy: 2 digits sequence number (01-99)
     *
     * Example: PDCMS25123001 (2025-12-30, first invoice of the day)
     */
    private String generatePaymentCode() {
        LocalDateTime now = LocalDateTime.now();
        String prefix = "PDCMS";

        // Format: yyMMdd
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyMMdd"));

        // Get daily sequence number (01-99)
        // Count invoices created today
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        long todayCount = invoiceRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        int sequence = (int) (todayCount % 99) + 1; // 1-99, wrap around

        String sequenceStr = String.format("%02d", sequence);

        return prefix + dateStr + sequenceStr;
    }
}
