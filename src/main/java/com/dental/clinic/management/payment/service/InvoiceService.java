package com.dental.clinic.management.payment.service;

import com.dental.clinic.management.account.domain.Account;
import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.booking_appointment.domain.Appointment;
import com.dental.clinic.management.booking_appointment.domain.AppointmentService;
import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.booking_appointment.repository.AppointmentServiceRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.ResourceNotFoundException;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.repository.PatientRepository;
import com.dental.clinic.management.payment.domain.Invoice;
import com.dental.clinic.management.payment.domain.InvoiceItem;
import com.dental.clinic.management.payment.dto.CreateInvoiceRequest;
import com.dental.clinic.management.payment.dto.InvoiceResponse;
import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.enums.InvoiceType;
import com.dental.clinic.management.payment.repository.InvoiceItemRepository;
import com.dental.clinic.management.payment.repository.InvoiceRepository;
import com.dental.clinic.management.treatment_plans.domain.PatientTreatmentPlan;
import com.dental.clinic.management.treatment_plans.repository.PatientTreatmentPlanRepository;
import com.dental.clinic.management.utils.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final VietQRService vietQRService;

    // Repositories for populating response fields (Fix FE Issues #1, #2, #3)
    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceRepository appointmentServiceRepository; // ✅ NEW: For fetching appointment services
    private final PatientRepository patientRepository;
    private final PatientTreatmentPlanRepository treatmentPlanRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;

    /**
     * Tao invoice moi
     * 
     * DUPLICATE PREVENTION:
     * - Uses @Transactional to prevent concurrent creation
     * - Generates unique invoice_code with do-while loop
     * - invoice_code has UNIQUE constraint in database
     * - If duplicate code somehow created, DB will reject with constraint violation
     */
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        log.info("Creating new invoice for patient: {} (appointmentId: {}, type: {})", 
                 request.getPatientId(), request.getAppointmentId(), request.getInvoiceType());

        // ✅ AUTO-DETERMINE INVOICE TYPE: If appointmentId is provided and already has APPOINTMENT invoice,
        // automatically convert to SUPPLEMENTAL to ensure data integrity
        InvoiceType invoiceType = request.getInvoiceType();
        
        if (request.getAppointmentId() != null && invoiceType == InvoiceType.APPOINTMENT) {
            boolean hasAppointmentInvoice = invoiceRepository.existsByAppointmentIdAndInvoiceType(
                    request.getAppointmentId(), 
                    InvoiceType.APPOINTMENT
            );
            
            if (hasAppointmentInvoice) {
                log.info("✅ AUTO-CONVERT: Appointment {} already has APPOINTMENT invoice, converting type from APPOINTMENT to SUPPLEMENTAL", 
                         request.getAppointmentId());
                invoiceType = InvoiceType.SUPPLEMENTAL;
            }
        }

        // ✅ DATA INTEGRITY VALIDATION: If appointmentId is provided, validate patientId
        // matches and get the appointment doctor for created_by
        Integer invoiceCreatedBy = 1; // Default system user
        List<CreateInvoiceRequest.InvoiceItemDto> invoiceItems = request.getItems();
        
        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("APPOINTMENT_NOT_FOUND",
                            "Appointment not found: " + request.getAppointmentId()));

            // Validate patient matches
            if (!appointment.getPatientId().equals(request.getPatientId())) {
                String errorMsg = String.format(
                        "Invoice patientId (%d) does not match appointment patientId (%d) for appointment %d",
                        request.getPatientId(),
                        appointment.getPatientId(),
                        request.getAppointmentId());
                log.error("Data integrity violation: {}", errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            // ✅ SECURITY CHECK: Users with CREATE_INVOICE permission (RECEPTIONIST, ADMIN) can create invoice for any appointment
            // Users without CREATE_INVOICE permission can only create invoice for their own appointments
            boolean hasCreateInvoicePermission = SecurityUtil.hasCurrentUserPermission("CREATE_INVOICE");
            
            if (!hasCreateInvoicePermission) {
                // User doesn't have CREATE_INVOICE permission - check if they own the appointment
                Integer currentEmployeeId = getCurrentEmployeeId();
                if (!appointment.getEmployeeId().equals(currentEmployeeId)) {
                    log.error("Access denied: User {} attempted to create invoice for appointment {} owned by doctor {}", 
                             currentEmployeeId, appointment.getAppointmentId(), appointment.getEmployeeId());
                    throw new AccessDeniedException("Bạn chỉ có thể tạo hóa đơn cho lịch hẹn của chính mình");
                }
            }
            // If has CREATE_INVOICE permission, allow creating invoice for any appointment

            // ✅ FIX BUG: Set invoice created_by to match appointment's doctor
            // This ensures invoice creator is the same as the appointment's responsible doctor
            invoiceCreatedBy = appointment.getEmployeeId();
            log.debug("✅ Validated: Invoice patientId matches appointment patientId");
            log.debug("✅ Setting invoice created_by to appointment doctor: {}", invoiceCreatedBy);
            
            // 🔥 CRITICAL: Only fetch appointment services for APPOINTMENT type invoices
            // For SUPPLEMENTAL invoices, use FE-provided services (they are additional/different services)
            if (invoiceType == InvoiceType.APPOINTMENT) {
                // Fetch services from appointment_services table - SOURCE OF TRUTH for APPOINTMENT invoices
                List<AppointmentService> appointmentServices = appointmentServiceRepository
                        .findByIdAppointmentId(request.getAppointmentId());
                
                if (appointmentServices.isEmpty()) {
                    log.warn("⚠️ No services found in appointment_services for appointment {}, using FE data as fallback",
                            request.getAppointmentId());
                    // Fallback to FE data if no services in DB (shouldn't happen normally)
                } else {
                    log.info("✅ APPOINTMENT invoice: Fetched {} services from appointment_services table for appointment {}", 
                            appointmentServices.size(), request.getAppointmentId());
                    
                    // Map appointment services to invoice items
                    List<CreateInvoiceRequest.InvoiceItemDto> mappedItems = new ArrayList<>();
                    for (AppointmentService aptService : appointmentServices) {
                        com.dental.clinic.management.service.domain.DentalService service = aptService.getService();
                        CreateInvoiceRequest.InvoiceItemDto itemDto = CreateInvoiceRequest.InvoiceItemDto.builder()
                                .serviceId(aptService.getId().getServiceId())
                                .serviceCode(service.getServiceCode())
                                .serviceName(service.getServiceName())
                                .quantity(1)
                                .unitPrice(service.getPrice())
                                .notes("Dịch vụ từ lịch hẹn " + appointment.getAppointmentCode())
                                .build();
                        mappedItems.add(itemDto);
                    }
                    invoiceItems = mappedItems;
                    
                    log.info("✅ Mapped {} appointment services to APPOINTMENT invoice items", invoiceItems.size());
                }
            } else if (invoiceType == InvoiceType.SUPPLEMENTAL) {
                // For SUPPLEMENTAL invoices, use FE-provided services (additional/different services)
                log.info("✅ SUPPLEMENTAL invoice: Using {} FE-provided services (additional services, may have different prices)", 
                         invoiceItems.size());
            }
        }
        // ✅ NEW: Handle TREATMENT_PLAN invoices - set created_by to plan creator
        else if (request.getTreatmentPlanId() != null) {
            PatientTreatmentPlan plan = treatmentPlanRepository.findById(request.getTreatmentPlanId().longValue())
                    .orElseThrow(() -> new ResourceNotFoundException("TREATMENT_PLAN_NOT_FOUND",
                            "Treatment plan not found: " + request.getTreatmentPlanId()));
            
            // Validate patient matches
            if (!plan.getPatient().getPatientId().equals(request.getPatientId())) {
                String errorMsg = String.format(
                        "Invoice patientId (%d) does not match treatment plan patientId (%d) for plan %d",
                        request.getPatientId(),
                        plan.getPatient().getPatientId(),
                        request.getTreatmentPlanId());
                log.error("Data integrity violation: {}", errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
            
            // ✅ FIX: Set invoice created_by to match plan's creator (doctor)
            if (plan.getCreatedBy() != null && plan.getCreatedBy().getEmployeeId() != null) {
                invoiceCreatedBy = plan.getCreatedBy().getEmployeeId();
                log.debug("✅ Setting invoice created_by to treatment plan creator: {}", invoiceCreatedBy);
            } else {
                log.warn("⚠️ Treatment plan {} has no creator (createdBy is null). Using default system user (1).", 
                        plan.getPlanCode());
            }
        }
        // If neither appointmentId nor treatmentPlanId, created_by = 1 (system user)
        // This is OK for SUPPLEMENTAL invoices created manually

        // ✅ NEW: Get invoice creator (person who clicked "Create Invoice" button)
        Integer invoiceCreatorEmployeeId = null;
        try {
            invoiceCreatorEmployeeId = getCurrentEmployeeId();
            log.debug("✅ Invoice creator (logged-in user): {}", invoiceCreatorEmployeeId);
        } catch (Exception e) {
            log.warn("⚠️ Could not get current employee ID for invoice creator, using null");
        }

        // Generate payment code for SePay webhook matching
        String paymentCode = generatePaymentCode();

        // ✅ Build notes with payment code and invoice creator ID
        String notes = "Payment Code: " + paymentCode;
        if (invoiceCreatorEmployeeId != null) {
            notes += " | Creator: " + invoiceCreatorEmployeeId;
        }
        if (request.getNotes() != null) {
            notes += " | " + request.getNotes();
        }

        Invoice invoice = Invoice.builder()
                .invoiceCode(generateInvoiceCode())
                .invoiceType(invoiceType) // ✅ Use auto-determined type instead of request.getInvoiceType()
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
                .notes(notes)
                .createdBy(invoiceCreatedBy) // Set to appointment doctor if appointment exists
                .build();

        invoice = invoiceRepository.save(invoice);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateInvoiceRequest.InvoiceItemDto itemDto : invoiceItems) {
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

        log.info("✅ Successfully created invoice: {} (type: {}, total: {}, appointmentId: {})", 
                 invoice.getInvoiceCode(), invoice.getInvoiceType(), totalAmount, invoice.getAppointmentId());

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
     * Returns both APPOINTMENT and SUPPLEMENTAL invoices for the appointment
     * If user has VIEW_INVOICE_OWN but not VIEW_INVOICE_ALL, only return invoices for their own patient account
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByAppointment(Integer appointmentId) {
        log.info("Getting invoices for appointment: {}", appointmentId);
        
        // ✅ FIX: Get all invoices for appointment (both APPOINTMENT and SUPPLEMENTAL)
        List<Invoice> invoices = invoiceRepository
            .findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
        
        // ✅ Filter: Only include APPOINTMENT and SUPPLEMENTAL types (exclude TREATMENT_PLAN if any)
        invoices = invoices.stream()
            .filter(inv -> inv.getInvoiceType() == InvoiceType.APPOINTMENT 
                       || inv.getInvoiceType() == InvoiceType.SUPPLEMENTAL)
            .collect(Collectors.toList());
        
        log.debug("Found {} invoices for appointment {} (APPOINTMENT + SUPPLEMENTAL)", 
                  invoices.size(), appointmentId);
        
        // ✅ RBAC: If user has VIEW_INVOICE_OWN but not VIEW_INVOICE_ALL, filter by their patientId
        if (SecurityUtil.hasCurrentUserPermission("VIEW_INVOICE_OWN") && 
            !SecurityUtil.hasCurrentUserPermission("VIEW_INVOICE_ALL")) {
            Integer currentPatientId = getCurrentPatientId();
            log.debug("Filtering invoices for patient: {}", currentPatientId);
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getPatientId().equals(currentPatientId))
                    .collect(Collectors.toList());
        }
        
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
     * Get all invoices with pagination and filtering.
     * Supports filtering by payment status, invoice type, patient ID, and date range.
     * Only accessible by admin/manager with VIEW_INVOICE_ALL permission.
     * 
     * @param status Optional filter by payment status
     * @param type Optional filter by invoice type
     * @param patientId Optional filter by patient ID
     * @param startDate Optional start date (inclusive)
     * @param endDate Optional end date (inclusive)
     * @param pageable Pagination and sorting parameters
     * @return Page of invoice responses
     */
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAllInvoices(
            InvoicePaymentStatus status,
            InvoiceType type,
            Integer patientId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        try {
            log.info("Getting all invoices with filters - status: {}, type: {}, patientId: {}, startDate: {}, endDate: {}, page: {}, size: {}", 
                     status, type, patientId, startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());
            
            // Convert LocalDate to LocalDateTime for database queries
            LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;
            
            Page<Invoice> invoices = invoiceRepository.findAllWithFilters(
                    status, type, patientId, startDateTime, endDateTime, pageable);
            
            log.info("Found {} invoices (total: {}, page: {}/{})", 
                     invoices.getNumberOfElements(), invoices.getTotalElements(),
                     invoices.getNumber() + 1, invoices.getTotalPages());
            
            return invoices.map(this::mapToResponse);
        } catch (Exception e) {
            log.error("Error getting all invoices with filters - status: {}, type: {}, patientId: {}, startDate: {}, endDate: {}", 
                      status, type, patientId, startDate, endDate, e);
            throw new RuntimeException("Failed to retrieve invoices: " + e.getMessage(), e);
        }
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
     *
     * FIX: Populate appointmentCode, patientName, treatmentPlanCode, createdByName
     * Reference: INVOICE_MODULE_ISSUES_AND_CONFIRMATIONS.md (Issues #1, #2, #3)
     */
    private InvoiceResponse mapToResponse(Invoice invoice) {
        try {
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
            if (paymentCode != null && invoice.getRemainingDebt() != null 
                    && invoice.getRemainingDebt().compareTo(BigDecimal.ZERO) > 0) {
                try {
                    qrCodeUrl = vietQRService.generateQRUrl(invoice.getRemainingDebt().longValue(), paymentCode);
                } catch (Exception e) {
                    log.warn("Failed to generate QR code for invoice {}: {}", invoice.getInvoiceCode(), e.getMessage());
                }
            }

            // FIX Issue #1 (HIGH): Populate appointmentCode from Appointment table
            String appointmentCode = null;
            if (invoice.getAppointmentId() != null) {
                try {
                    appointmentCode = appointmentRepository.findById(invoice.getAppointmentId())
                            .map(Appointment::getAppointmentCode)
                            .orElse(null);
                } catch (Exception e) {
                    log.warn("Failed to fetch appointmentCode for invoice {}: {}", invoice.getInvoiceCode(), e.getMessage());
                }
            }

            // FIX Issue #2 (MEDIUM): Populate patientName from Patient table
            String patientName = null;
            if (invoice.getPatientId() != null) {
                try {
                    patientName = patientRepository.findById(invoice.getPatientId())
                            .map(Patient::getFullName)
                            .orElse(null);
                } catch (Exception e) {
                    log.warn("Failed to fetch patientName for invoice {}: {}", invoice.getInvoiceCode(), e.getMessage());
                }
            }

            // BONUS: Populate treatmentPlanCode from PatientTreatmentPlan table
            String treatmentPlanCode = null;
            if (invoice.getTreatmentPlanId() != null) {
                try {
                    treatmentPlanCode = treatmentPlanRepository.findById(invoice.getTreatmentPlanId().longValue())
                            .map(PatientTreatmentPlan::getPlanCode) // ✅ Fixed: Use planCode, not treatmentPlanCode
                            .orElse(null);
                } catch (Exception e) {
                    log.warn("Failed to fetch treatmentPlanCode for invoice {}: {}", invoice.getInvoiceCode(), e.getMessage());
                }
            }

            // FIX Issue #3 (LOW): Populate createdByName from Employee table (Bác sĩ phụ trách)
            String createdByName = null;
            if (invoice.getCreatedBy() != null) {
                try {
                    createdByName = employeeRepository.findById(invoice.getCreatedBy())
                            .map(Employee::getFullName)
                            .orElse(null);
                } catch (Exception e) {
                    log.warn("Failed to fetch createdByName for invoice {}: {}", invoice.getInvoiceCode(), e.getMessage());
                }
            }

            // ✅ NEW: Populate invoice creator info (Người tạo hóa đơn)
            Integer invoiceCreatorId = extractInvoiceCreatorIdFromNotes(invoice.getNotes());
            String invoiceCreatorName = null;
            if (invoiceCreatorId != null) {
                try {
                    invoiceCreatorName = employeeRepository.findById(invoiceCreatorId)
                            .map(Employee::getFullName)
                            .orElse(null);
                } catch (Exception e) {
                    log.warn("Failed to fetch invoiceCreatorName for invoice {}: {}", invoice.getInvoiceCode(), e.getMessage());
                }
            }

            return InvoiceResponse.builder()
                    .invoiceId(invoice.getInvoiceId())
                    .invoiceCode(invoice.getInvoiceCode())
                    .invoiceType(invoice.getInvoiceType())
                    .patientId(invoice.getPatientId())
                    .patientName(patientName) // ✅ Fixed - FE Issue #2
                    .appointmentId(invoice.getAppointmentId())
                    .appointmentCode(appointmentCode) // ✅ Fixed - FE Issue #1
                    .treatmentPlanId(invoice.getTreatmentPlanId())
                    .treatmentPlanCode(treatmentPlanCode) // ✅ Bonus
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
                    .createdBy(invoice.getCreatedBy()) // Bác sĩ phụ trách
                    .createdByName(createdByName) // Tên bác sĩ phụ trách
                    .invoiceCreatorId(invoiceCreatorId) // ✅ NEW: Người tạo hóa đơn
                    .invoiceCreatorName(invoiceCreatorName) // ✅ NEW: Tên người tạo hóa đơn
                    .createdAt(invoice.getCreatedAt())
                    .updatedAt(invoice.getUpdatedAt())
                    .items(itemResponses)
                    .build();
        } catch (Exception e) {
            log.error("Error mapping invoice {} to response: {}", invoice.getInvoiceCode(), e.getMessage(), e);
            throw new RuntimeException("Failed to map invoice to response: " + invoice.getInvoiceCode(), e);
        }
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
     * Extract invoice creator ID from notes field
     * Format: "Payment Code: PDCMS25123001 | Creator: 5 | original notes"
     * 
     * @param notes Notes field containing creator ID
     * @return Employee ID of invoice creator, or null if not found
     */
    private Integer extractInvoiceCreatorIdFromNotes(String notes) {
        if (notes == null || !notes.contains("Creator: ")) {
            return null;
        }

        try {
            String[] parts = notes.split("Creator: ");
            if (parts.length < 2) {
                return null;
            }

            String creatorPart = parts[1].split(" \\| ")[0].trim();
            return Integer.parseInt(creatorPart);
        } catch (Exception e) {
            log.warn("Failed to extract invoice creator ID from notes: {}", notes, e);
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

    /**
     * Get current employee ID from security context.
     * Used for permission checks - doctors can only create invoices for their own appointments.
     */
    private Integer getCurrentEmployeeId() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AccessDeniedException("Người dùng chưa được xác thực"));

        return accountRepository.findOneByUsername(username)
                .map(Account::getEmployee)
                .map(Employee::getEmployeeId)
                .orElseThrow(() -> new AccessDeniedException("Không tìm thấy nhân viên cho người dùng: " + username));
    }

    /**
     * Get current patient ID from security context.
     * Used for permission checks - patients can only view their own invoices.
     */
    private Integer getCurrentPatientId() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AccessDeniedException("Người dùng chưa được xác thực"));

        return accountRepository.findOneByUsername(username)
                .map(Account::getPatient)
                .map(Patient::getPatientId)
                .orElseThrow(() -> new AccessDeniedException("Không tìm thấy bệnh nhân cho người dùng: " + username));
    }
}
