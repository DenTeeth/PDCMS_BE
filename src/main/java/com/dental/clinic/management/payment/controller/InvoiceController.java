package com.dental.clinic.management.payment.controller;

import com.dental.clinic.management.payment.dto.CreateInvoiceRequest;
import com.dental.clinic.management.payment.dto.InvoiceResponse;
import com.dental.clinic.management.payment.service.InvoiceService;
import com.dental.clinic.management.utils.annotation.ApiMessage;
import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.enums.InvoiceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoice Management", description = "APIs for managing invoices and billing")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_INVOICE')") // RECEPTIONIST, ADMIN
    @ApiMessage("Tạo hóa đơn thành công")
    @Operation(summary = "Create new invoice", description = "Create a new invoice for appointment or treatment plan")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        log.info("REST request to create invoice for patient: {}", request.getPatientId());
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('VIEW_INVOICE_ALL', 'VIEW_INVOICE_OWN')") // RECEPTIONIST, ACCOUNTANT, PATIENT, ADMIN
    @ApiMessage("Lấy danh sách hóa đơn thành công")
    @Operation(summary = "Get invoices by patient", description = "Get all invoices for a specific patient")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByPatient(@PathVariable Integer patientId) {
        log.info("REST request to get invoices for patient: {}", patientId);
        List<InvoiceResponse> invoices = invoiceService.getInvoicesByPatient(patientId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyAuthority('VIEW_INVOICE_ALL', 'VIEW_APPOINTMENT_ALL', 'VIEW_INVOICE_OWN')") // RECEPTIONIST, ACCOUNTANT, MANAGER, ADMIN, PATIENT
    @ApiMessage("Lấy danh sách hóa đơn thành công")
    @Operation(summary = "Get invoices by appointment", description = "Get all invoices for a specific appointment")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByAppointment(@PathVariable Integer appointmentId) {
        log.info("REST request to get invoices for appointment: {}", appointmentId);
        List<InvoiceResponse> invoices = invoiceService.getInvoicesByAppointment(appointmentId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{invoiceCode}")
    @PreAuthorize("hasAnyAuthority('VIEW_INVOICE_ALL', 'VIEW_INVOICE_OWN')") // RECEPTIONIST, ACCOUNTANT, PATIENT, ADMIN
    @ApiMessage("Lấy thông tin hóa đơn thành công")
    @Operation(summary = "Get invoice by code", description = "Get invoice details by invoice code")
    public ResponseEntity<InvoiceResponse> getInvoiceByCode(@PathVariable String invoiceCode) {
        log.info("REST request to get invoice: {}", invoiceCode);
        InvoiceResponse invoice = invoiceService.getInvoiceByCode(invoiceCode);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/patient/{patientId}/unpaid")
    @PreAuthorize("hasAnyAuthority('VIEW_INVOICE_ALL', 'VIEW_INVOICE_OWN')") // RECEPTIONIST, ACCOUNTANT, PATIENT, ADMIN
    @ApiMessage("Lấy danh sách hóa đơn chưa thanh toán thành công")
    @Operation(summary = "Get unpaid invoices by patient", description = "Get all unpaid invoices for a specific patient")
    public ResponseEntity<List<InvoiceResponse>> getUnpaidInvoicesByPatient(@PathVariable Integer patientId) {
        log.info("REST request to get unpaid invoices for patient: {}", patientId);
        List<InvoiceResponse> invoices = invoiceService.getUnpaidInvoicesByPatient(patientId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{invoiceCode}/payment-status")
    @PreAuthorize("hasAnyAuthority('VIEW_INVOICE_ALL', 'VIEW_INVOICE_OWN', 'VIEW_PAYMENT_ALL')") // RECEPTIONIST, ACCOUNTANT, PATIENT, ADMIN
    @ApiMessage("Lấy trạng thái thanh toán thành công")
    @Operation(summary = "Check payment status", description = "Check payment status of invoice by invoice code")
    public ResponseEntity<InvoiceResponse> checkPaymentStatus(@PathVariable String invoiceCode) {
        log.info("REST request to check payment status for invoice: {}", invoiceCode);
        InvoiceResponse invoice = invoiceService.getInvoiceByCode(invoiceCode);
        return ResponseEntity.ok(invoice);
    }

    /**
     * Get all invoices with pagination and filtering.
     * Only accessible by admin/manager (VIEW_INVOICE_ALL permission).
     * 
     * Supports filtering by:
     * - status: Invoice payment status (PENDING_PAYMENT, PARTIAL_PAID, PAID, CANCELLED)
     * - type: Invoice type (TREATMENT_PLAN, APPOINTMENT)
     * - patientId: Patient ID
     * - startDate: Start date for filtering (inclusive)
     * - endDate: End date for filtering (inclusive)
     * 
     * @param status Optional filter by payment status
     * @param type Optional filter by invoice type
     * @param patientId Optional filter by patient ID
     * @param startDate Optional start date filter (format: yyyy-MM-dd)
     * @param endDate Optional end date filter (format: yyyy-MM-dd)
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of invoice responses
     * 
     * Example: GET /api/v1/invoices?status=PENDING_PAYMENT&page=0&size=20&sort=createdAt,desc
     */
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_INVOICE_ALL')") // RECEPTIONIST, ACCOUNTANT, MANAGER, ADMIN
    @ApiMessage("Lấy danh sách tất cả hóa đơn thành công")
    @Operation(summary = "Get all invoices with filters", 
               description = "Get paginated list of all invoices with optional filtering by status, type, patient, and date range. Admin/Manager only.")
    public ResponseEntity<Page<InvoiceResponse>> getAllInvoices(
            @RequestParam(required = false) InvoicePaymentStatus status,
            @RequestParam(required = false) InvoiceType type,
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get all invoices - status: {}, type: {}, patientId: {}, startDate: {}, endDate: {}, page: {}, size: {}", 
                 status, type, patientId, startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<InvoiceResponse> result = invoiceService.getAllInvoices(status, type, patientId, startDate, endDate, pageable);
        
        log.info("Retrieved {} invoices out of {} total", 
                 result.getNumberOfElements(), result.getTotalElements());
        
        return ResponseEntity.ok(result);
    }
}
