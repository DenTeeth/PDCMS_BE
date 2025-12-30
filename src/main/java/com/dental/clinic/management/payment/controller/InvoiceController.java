package com.dental.clinic.management.payment.controller;

import com.dental.clinic.management.payment.dto.CreateInvoiceRequest;
import com.dental.clinic.management.payment.dto.InvoiceResponse;
import com.dental.clinic.management.payment.service.InvoiceService;
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
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoice Management", description = "APIs for managing invoices and billing")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_INVOICE')")
    @ApiMessage("Invoice created successfully")
    @Operation(summary = "Create new invoice", description = "Create a new invoice for appointment or treatment plan")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        log.info("REST request to create invoice for patient: {}", request.getPatientId());
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('VIEW_INVOICE_ALL', 'VIEW_INVOICE_OWN')")
    @ApiMessage("Invoices retrieved successfully")
    @Operation(summary = "Get invoices by patient", description = "Get all invoices for a specific patient")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByPatient(@PathVariable Integer patientId) {
        log.info("REST request to get invoices for patient: {}", patientId);
        List<InvoiceResponse> invoices = invoiceService.getInvoicesByPatient(patientId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyAuthority('VIEW_INVOICE_ALL', 'VIEW_APPOINTMENT_ALL')")
    @ApiMessage("Invoices retrieved successfully")
    @Operation(summary = "Get invoices by appointment", description = "Get all invoices for a specific appointment")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByAppointment(@PathVariable Integer appointmentId) {
        log.info("REST request to get invoices for appointment: {}", appointmentId);
        List<InvoiceResponse> invoices = invoiceService.getInvoicesByAppointment(appointmentId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{invoiceCode}")
    @PreAuthorize("hasAnyAuthority('VIEW_INVOICE_ALL', 'VIEW_INVOICE_OWN')")
    @ApiMessage("Invoice retrieved successfully")
    @Operation(summary = "Get invoice by code", description = "Get invoice details by invoice code")
    public ResponseEntity<InvoiceResponse> getInvoiceByCode(@PathVariable String invoiceCode) {
        log.info("REST request to get invoice: {}", invoiceCode);
        InvoiceResponse invoice = invoiceService.getInvoiceByCode(invoiceCode);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/patient/{patientId}/unpaid")
    @PreAuthorize("hasAnyAuthority('VIEW_INVOICE_ALL', 'VIEW_INVOICE_OWN')")
    @ApiMessage("Unpaid invoices retrieved successfully")
    @Operation(summary = "Get unpaid invoices by patient", description = "Get all unpaid invoices for a specific patient")
    public ResponseEntity<List<InvoiceResponse>> getUnpaidInvoicesByPatient(@PathVariable Integer patientId) {
        log.info("REST request to get unpaid invoices for patient: {}", patientId);
        List<InvoiceResponse> invoices = invoiceService.getUnpaidInvoicesByPatient(patientId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{invoiceCode}/payment-status")
    @PreAuthorize("hasAnyAuthority('VIEW_INVOICE_ALL', 'VIEW_INVOICE_OWN', 'VIEW_PAYMENT_ALL')")
    @ApiMessage("Payment status retrieved successfully")
    @Operation(summary = "Check payment status", description = "Check payment status of invoice by invoice code")
    public ResponseEntity<InvoiceResponse> checkPaymentStatus(@PathVariable String invoiceCode) {
        log.info("REST request to check payment status for invoice: {}", invoiceCode);
        InvoiceResponse invoice = invoiceService.getInvoiceByCode(invoiceCode);
        return ResponseEntity.ok(invoice);
    }
}
