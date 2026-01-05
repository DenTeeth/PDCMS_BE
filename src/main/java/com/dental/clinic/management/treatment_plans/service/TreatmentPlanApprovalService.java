package com.dental.clinic.management.treatment_plans.service;

import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.BadRequestException;
import com.dental.clinic.management.exception.ConflictException;
import com.dental.clinic.management.exception.NotFoundException;
import com.dental.clinic.management.treatment_plans.domain.ApprovalStatus;
import com.dental.clinic.management.treatment_plans.domain.PatientPlanItem;
import com.dental.clinic.management.treatment_plans.domain.PatientPlanPhase;
import com.dental.clinic.management.treatment_plans.domain.PatientTreatmentPlan;
import com.dental.clinic.management.treatment_plans.domain.PlanAuditLog;
import com.dental.clinic.management.treatment_plans.dto.TreatmentPlanDetailResponse;
import com.dental.clinic.management.treatment_plans.enums.PlanItemStatus;
import com.dental.clinic.management.treatment_plans.dto.request.ApproveTreatmentPlanRequest;
import com.dental.clinic.management.treatment_plans.dto.response.ApprovalMetadataDTO;
import com.dental.clinic.management.treatment_plans.repository.PatientTreatmentPlanRepository;
import com.dental.clinic.management.treatment_plans.repository.PlanAuditLogRepository;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.payment.dto.CreateInvoiceRequest;
import com.dental.clinic.management.payment.enums.InvoiceType;
import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.domain.Invoice;
import com.dental.clinic.management.payment.domain.InvoiceItem;
import com.dental.clinic.management.service.domain.DentalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Treatment Plan Approval Workflow (API 5.9 - V20).
 * Handles approval/rejection of treatment plans by managers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanApprovalService {

    private final PatientTreatmentPlanRepository planRepository;
    private final PlanAuditLogRepository auditLogRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;

    // V21: Clinical Rules Validation
    private final com.dental.clinic.management.service.service.ClinicalRulesValidationService clinicalRulesValidationService;
    private final com.dental.clinic.management.booking_appointment.repository.PatientPlanItemRepository itemRepository;

    // FE Issue: Auto-create invoices on approval
    // (ISSUE_BE_TREATMENT_PLAN_PHASED_INVOICE_CREATION.md)
    private final com.dental.clinic.management.payment.service.InvoiceService invoiceService;
    private final com.dental.clinic.management.service.repository.DentalServiceRepository dentalServiceRepository;
    
    // Fix: Check existing invoices to prevent duplicates when adding items
    // (ISSUE_BE_TREATMENT_PLAN_ADD_ITEMS_INVOICE_DUPLICATE.md)
    private final com.dental.clinic.management.payment.repository.InvoiceRepository invoiceRepository;
    private final com.dental.clinic.management.payment.repository.InvoiceItemRepository invoiceItemRepository;

    /**
     * API 5.9: Approve or Reject a treatment plan.
     *
     * Business Rules:
     * 1. Plan must exist
     * 2. Plan must be in PENDING_REVIEW status
     * 3. If REJECTED, notes are mandatory
     * 4. (REMOVED) Zero-price validation - plans with free services can be approved
     * 5. Log audit trail
     *
     * @param planCode The unique plan code
     * @param request  The approval request with status and notes
     * @return Updated treatment plan detail
     */
    @Transactional
    public TreatmentPlanDetailResponse approveTreatmentPlan(
            String planCode,
            ApproveTreatmentPlanRequest request) {

        log.info("Starting approval process for plan: {}", planCode);

        // 1. Get current manager from security context
        Integer managerId = getCurrentEmployeeId();

        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new NotFoundException("Nhân viên không tồn tại"));

        // 2. Find treatment plan
        PatientTreatmentPlan plan = planRepository.findByPlanCode(planCode)
                .orElseThrow(() -> new NotFoundException("Lộ trình điều trị không tồn tại"));

        // 3. GUARD: Must be in PENDING_REVIEW status
        if (plan.getApprovalStatus() != ApprovalStatus.PENDING_REVIEW) {
            throw new ConflictException(
                    String.format("Không thể duyệt lộ trình ở trạng thái '%s'. Chỉ duyệt được lộ trình 'Chờ duyệt'.",
                            plan.getApprovalStatus()));
        }

        // 4. GUARD: Notes required for REJECTION
        if (request.isRejection() && !StringUtils.hasText(request.getNotes())) {
            throw new BadRequestException("Phải có lý do khi từ chối lộ trình điều trị");
        }

        // 5. GUARD (P1): Check for zero-price items if APPROVED
        // FE Issue #3 Fix: Removed zero-price validation to allow approval of plans
        // with free services
        // Finance can adjust prices later via API 5.13 (Update Prices)
        // if (request.isApproval()) {
        // validateNoPriceItemsForApproval(plan);
        // }

        // 6. Store old status for audit log
        ApprovalStatus oldStatus = plan.getApprovalStatus();

        // 7. Update plan based on approval decision
        ApprovalStatus newStatus = determineNewApprovalStatus(request);
        plan.setApprovalStatus(newStatus);
        plan.setApprovedBy(manager);
        plan.setApprovedAt(LocalDateTime.now());

        // Store notes (could be approval reason or rejection reason)
        if (StringUtils.hasText(request.getNotes())) {
            plan.setRejectionReason(request.getNotes()); // Reuse rejection_reason column for all notes
        }

        // 8. Save plan
        plan = planRepository.save(plan);
        log.info("Updated plan {} to approval status: {}", planCode, newStatus);

        // 8B. V21: If APPROVED, activate items with clinical rules check
        if (newStatus == ApprovalStatus.APPROVED) {
            activateItemsWithClinicalRulesCheck(plan);

            // FE Issue: Auto-create invoices based on paymentType
            // (ISSUE_BE_TREATMENT_PLAN_PHASED_INVOICE_CREATION.md)
            try {
                createInvoicesForApprovedPlan(plan);
                log.info("✅ Successfully auto-created invoices for approved plan: {}", planCode);
            } catch (Exception e) {
                // Don't rollback approval if invoice creation fails
                // Invoices can be created manually later
                log.error("❌ Failed to auto-create invoices for plan {}: {}", planCode, e.getMessage(), e);
            }
        }

        // 9. Create audit log (P0 requirement)
        PlanAuditLog auditLog = PlanAuditLog.createApprovalLog(
                plan,
                manager,
                oldStatus,
                newStatus,
                request.getNotes());
        auditLogRepository.save(auditLog);
        log.info("Created audit log for plan {} approval action", planCode);

        // 10. Map to response DTO
        TreatmentPlanDetailResponse response = mapToDetailResponse(plan);

        // 11. Add approval metadata to response (P1 requirement)
        if (plan.getApprovedBy() != null && plan.getApprovedAt() != null) {
            response.setApprovalMetadata(buildApprovalMetadata(plan));
        }

        log.info("Approval process completed for plan: {}", planCode);
        return response;
    }

    /**
     * Map PatientTreatmentPlan entity to TreatmentPlanDetailResponse.
     * Simplified mapping for approval response - focuses on plan-level data.
     */
    private TreatmentPlanDetailResponse mapToDetailResponse(PatientTreatmentPlan plan) {
        // Build doctor info
        Employee doctor = plan.getCreatedBy();
        TreatmentPlanDetailResponse.DoctorInfoDTO doctorInfo = null;
        if (doctor != null) {
            doctorInfo = TreatmentPlanDetailResponse.DoctorInfoDTO.builder()
                    .employeeCode(doctor.getEmployeeCode())
                    .fullName(doctor.getFirstName() + " " + doctor.getLastName())
                    .build();
        }

        // Build patient info
        TreatmentPlanDetailResponse.PatientInfoDTO patientInfo = TreatmentPlanDetailResponse.PatientInfoDTO.builder()
                .patientCode(plan.getPatient().getPatientCode())
                .fullName(plan.getPatient().getFirstName() + " " + plan.getPatient().getLastName())
                .build();

        // Retrieve submit notes from audit log (if plan was submitted for review)
        String submitNotes = getSubmitNotesFromAuditLog(plan.getPlanId());

        // Build response
        return TreatmentPlanDetailResponse.builder()
                .planId(plan.getPlanId())
                .planCode(plan.getPlanCode())
                .planName(plan.getPlanName())
                .status(plan.getStatus() != null ? plan.getStatus().name() : null)
                .approvalStatus(plan.getApprovalStatus() != null ? plan.getApprovalStatus().name() : null)
                .doctor(doctorInfo)
                .patient(patientInfo)
                .startDate(plan.getStartDate())
                .expectedEndDate(plan.getExpectedEndDate())
                .createdAt(plan.getCreatedAt())
                .totalPrice(plan.getTotalPrice())
                .discountAmount(plan.getDiscountAmount())
                .finalCost(plan.getFinalCost())
                .paymentType(plan.getPaymentType() != null ? plan.getPaymentType().name() : null)
                .submitNotes(submitNotes)
                // Phases will be loaded separately if needed, or can be mapped here
                .build();
    }

    /**
     * Retrieve submit notes from audit log.
     * Looks for the most recent SUBMITTED_FOR_REVIEW action and returns its notes.
     *
     * @param planId Treatment plan ID
     * @return Submit notes, or null if not found
     */
    private String getSubmitNotesFromAuditLog(Long planId) {
        List<PlanAuditLog> logs = auditLogRepository.findByPlanIdOrderByCreatedAtDesc(planId);

        return logs.stream()
                .filter(log -> "SUBMITTED_FOR_REVIEW".equals(log.getActionType()))
                .findFirst()
                .map(PlanAuditLog::getNotes)
                .orElse(null);
    }

    /**
     * Validate that all items have price > 0 before approval (P1 Guard)
     */
    @SuppressWarnings("unused")
    private void validateNoPriceItemsForApproval(PatientTreatmentPlan plan) {
        boolean hasZeroPriceItem = plan.getPhases().stream()
                .flatMap(phase -> phase.getItems().stream())
                .anyMatch(item -> {
                    BigDecimal price = item.getPrice();
                    return price == null || price.compareTo(BigDecimal.ZERO) <= 0;
                });

        if (hasZeroPriceItem) {
            throw new BadRequestException(
                    "Không thể duyệt: Còn hạng mục có giá 0đ hoặc chưa có giá. " +
                            "Yêu cầu Bác sĩ cập nhật lại giá trước khi duyệt.");
        }
    }

    /**
     * Get current employee ID from security context.
     */
    private Integer getCurrentEmployeeId() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Người dùng chưa được xác thực"));

        return accountRepository.findOneByUsername(username)
                .map(account -> account.getEmployee().getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên cho người dùng: " + username));
    }

    /**
     * Determine new approval status based on request.
     * APPROVED -> APPROVED
     * REJECTED -> DRAFT (return to draft for doctor to revise)
     */
    private ApprovalStatus determineNewApprovalStatus(ApproveTreatmentPlanRequest request) {
        if (request.isApproval()) {
            return ApprovalStatus.APPROVED;
        } else if (request.isRejection()) {
            return ApprovalStatus.DRAFT; // Return to DRAFT for revision
        }
        throw new IllegalStateException("Trạng thái duyệt không hợp lệ: " + request.getApprovalStatus());
    }

    /**
     * Build approval metadata DTO for response (P1 requirement)
     */
    private ApprovalMetadataDTO buildApprovalMetadata(PatientTreatmentPlan plan) {
        Employee approver = plan.getApprovedBy();

        ApprovalMetadataDTO.EmployeeBasicDTO approverInfo = ApprovalMetadataDTO.EmployeeBasicDTO.builder()
                .employeeCode(approver.getEmployeeCode())
                .fullName(approver.getFirstName() + " " + approver.getLastName())
                .build();

        return ApprovalMetadataDTO.builder()
                .approvedBy(approverInfo)
                .approvedAt(plan.getApprovedAt())
                .notes(plan.getRejectionReason()) // Reusing this field for all approval notes
                .build();
    }

    // ====================================================================
    // V21: Clinical Rules Integration for Plan Activation
    // ====================================================================

    /**
     * V21: Activate plan items with clinical rules awareness.
     *
     * When plan is APPROVED, iterate through all items in PENDING status.
     * For each item with a service_id:
     * - If service has prerequisites → set status to WAITING_FOR_PREREQUISITE
     * - Else → set status to READY_FOR_BOOKING
     *
     * This ensures items are not bookable until prerequisites are met.
     *
     * @param plan The approved treatment plan
     */
    private void activateItemsWithClinicalRulesCheck(PatientTreatmentPlan plan) {
        log.info("V21: Activating plan {} items with clinical rules check", plan.getPlanCode());

        int itemsActivated = 0;
        int itemsWaiting = 0;

        for (var phase : plan.getPhases()) {
            for (PatientPlanItem item : phase.getItems()) {
                // Only process PENDING items
                if (item.getStatus() != PlanItemStatus.PENDING) {
                    continue;
                }

                // Skip items without service (shouldn't happen, but safety check)
                if (item.getServiceId() == null) {
                    log.warn("V21: Item {} has no service, skipping", item.getItemId());
                    continue;
                }

                Long serviceId = item.getServiceId().longValue();

                // Check if service has prerequisites
                boolean hasPrereqs = clinicalRulesValidationService.hasPrerequisites(serviceId);

                if (hasPrereqs) {
                    // Service requires prerequisites → WAITING
                    item.setStatus(PlanItemStatus.WAITING_FOR_PREREQUISITE);
                    itemsWaiting++;
                    log.debug("V21: Item {} (service {}) → WAITING_FOR_PREREQUISITE (has prerequisites)",
                            item.getItemId(), serviceId);
                } else {
                    // No prerequisites → READY
                    item.setStatus(PlanItemStatus.READY_FOR_BOOKING);
                    itemsActivated++;
                    log.debug("V21: Item {} (service {}) → READY_FOR_BOOKING (no prerequisites)",
                            item.getItemId(), serviceId);
                }

                itemRepository.save(item);
            }
        }

        log.info("V21:  Plan {} activation complete - {} items READY, {} items WAITING for prerequisites",
                plan.getPlanCode(), itemsActivated, itemsWaiting);
    }

    /**
     * API 5.12: Submit Treatment Plan for Review.
     * Changes plan status from DRAFT → PENDING_REVIEW.
     *
     * Business Rules:
     * 1. Plan must exist
     * 2. Plan must be in DRAFT status
     * 3. Plan must have at least 1 phase and 1 item
     * 4. Log audit trail
     *
     * @param planCode The unique plan code
     * @param request  The submit request with optional notes
     * @return Updated treatment plan detail
     */
    @Transactional
    public TreatmentPlanDetailResponse submitForReview(
            String planCode,
            com.dental.clinic.management.treatment_plans.dto.request.SubmitForReviewRequest request) {
        log.info("API 5.12: Submitting treatment plan {} for review", planCode);

        // STEP 1: Find plan
        PatientTreatmentPlan plan = planRepository.findByPlanCode(planCode)
                .orElseThrow(() -> new NotFoundException(
                        "PLAN_NOT_FOUND",
                        "Không tìm thấy lộ trình điều trị với mã: " + planCode));

        log.debug("Found plan {} with current status: {}", planCode, plan.getApprovalStatus());

        // STEP 2: Validate plan is in DRAFT status
        if (plan.getApprovalStatus() != ApprovalStatus.DRAFT) {
            throw new ConflictException(
                    String.format("Chỉ có thể gửi duyệt lộ trình ở trạng thái 'Nháp'. Trạng thái hiện tại: %s",
                            plan.getApprovalStatus()));
        }

        // STEP 3: Validate plan has content (at least 1 phase and 1 item)
        if (plan.getPhases() == null || plan.getPhases().isEmpty()) {
            throw new BadRequestException(
                    "EMPTY_PLAN",
                    "Không thể gửi duyệt lộ trình chưa có giai đoạn nào.");
        }

        boolean hasItems = plan.getPhases().stream()
                .anyMatch(phase -> phase.getItems() != null && !phase.getItems().isEmpty());

        if (!hasItems) {
            throw new BadRequestException(
                    "NO_ITEMS",
                    "Không thể gửi duyệt lộ trình chưa có hạng mục nào.");
        }

        // STEP 4: Get current user for audit
        Integer employeeId = getCurrentEmployeeId();
        Employee submitter = null;
        if (employeeId != null) {
            submitter = employeeRepository.findById(employeeId).orElse(null);
        }

        // STEP 5: Change status to PENDING_REVIEW
        ApprovalStatus oldStatus = plan.getApprovalStatus();
        plan.setApprovalStatus(ApprovalStatus.PENDING_REVIEW);

        PatientTreatmentPlan savedPlan = planRepository.save(plan);

        log.info("Plan {} status changed: {} → PENDING_REVIEW",
                planCode, oldStatus);

        // STEP 6: Create audit log
        String notes = (request != null && StringUtils.hasText(request.getNotes()))
                ? request.getNotes()
                : "Gửi duyệt lộ trình điều trị";

        PlanAuditLog auditLog = PlanAuditLog.createApprovalLog(
                savedPlan,
                submitter,
                oldStatus,
                ApprovalStatus.PENDING_REVIEW,
                notes);
        auditLogRepository.save(auditLog);

        log.info("Audit log created for plan {} submission", planCode);

        // STEP 7: Build response
        TreatmentPlanDetailResponse response = mapToDetailResponse(savedPlan);

        // Add approval metadata
        if (savedPlan.getApprovedAt() != null) {
            response.setApprovalMetadata(buildApprovalMetadata(savedPlan));
        }

        log.info("API 5.12: Successfully submitted plan {} for review", planCode);

        return response;
    }

    // ========================================
    // AUTO-CREATE INVOICES ON APPROVAL
    // FE Issue: ISSUE_BE_TREATMENT_PLAN_PHASED_INVOICE_CREATION.md
    // ========================================

    /**
     * Auto-create invoices when treatment plan is approved.
     * 
     * FIX: Check for existing invoices before creating new ones to prevent duplicates
     * when items are added to approved plans.
     * 
     * Logic:
     * 1. Check if plan has existing invoices
     * 2. If PENDING_PAYMENT invoice exists → Update it with new items
     * 3. If PAID/PARTIAL_PAID invoices exist → Create SUPPLEMENTAL invoice for new items
     * 4. If no invoices exist → Create new invoices based on paymentType
     * 
     * Payment Types:
     * - FULL: Create 1 invoice for entire plan
     * - PHASED: Create separate invoice for each phase
     * - INSTALLMENT: Create invoices by installment
     *
     * Made public to allow recreation of invoices when plan is updated (Issue 3)
     *
     * @param plan The approved treatment plan
     */
    public void createInvoicesForApprovedPlan(PatientTreatmentPlan plan) {
        log.info("Creating invoices for approved plan: {} (paymentType: {})",
                plan.getPlanCode(), plan.getPaymentType());

        // STEP 1: Check for existing invoices
        List<Invoice> existingInvoices = invoiceRepository.findByTreatmentPlanIdOrderByCreatedAtDesc(
                plan.getPlanId().intValue());
        
        if (!existingInvoices.isEmpty()) {
            log.info("Found {} existing invoice(s) for plan {}", existingInvoices.size(), plan.getPlanCode());
        }

        // STEP 2: Check if there's a PENDING_PAYMENT invoice
        Optional<Invoice> pendingInvoice = existingInvoices.stream()
                .filter(inv -> inv.getPaymentStatus() == InvoicePaymentStatus.PENDING_PAYMENT)
                .findFirst();

        if (pendingInvoice.isPresent()) {
            // CASE: Update existing PENDING_PAYMENT invoice instead of creating new one
            log.info("Found existing PENDING_PAYMENT invoice {}. Updating with new items instead of creating new invoice.",
                    pendingInvoice.get().getInvoiceCode());
            updateInvoiceWithNewPlanItems(pendingInvoice.get(), plan);
            return; // Don't create new invoice
        }

        // STEP 3: Check if there are PAID or PARTIAL_PAID invoices
        boolean hasPaidInvoices = existingInvoices.stream()
                .anyMatch(inv -> inv.getPaymentStatus() == InvoicePaymentStatus.PAID
                        || inv.getPaymentStatus() == InvoicePaymentStatus.PARTIAL_PAID);

        if (hasPaidInvoices) {
            // CASE: Create SUPPLEMENTAL invoice for new items
            log.info("Found PAID/PARTIAL_PAID invoices. Creating SUPPLEMENTAL invoice for new items.");
            createSupplementalInvoiceForNewItems(plan, existingInvoices);
            return;
        }

        // STEP 4: No existing invoices - create new invoices as normal
        log.info("No existing invoices found. Creating new invoices for plan.");
        switch (plan.getPaymentType()) {
            case FULL:
                createFullPaymentInvoice(plan);
                break;
            case PHASED:
                createPhasedInvoices(plan);
                break;
            case INSTALLMENT:
                createInstallmentInvoices(plan);
                break;
            default:
                log.warn("Unknown payment type {} for plan: {}", plan.getPaymentType(), plan.getPlanCode());
        }
    }

    /**
     * Create ONE invoice for entire plan (paymentType = FULL).
     * - phaseNumber = null
     * - installmentNumber = null
     * - Includes all items from all phases
     */
    private void createFullPaymentInvoice(PatientTreatmentPlan plan) {
        log.info("Creating FULL payment invoice for plan: {}", plan.getPlanCode());

        List<CreateInvoiceRequest.InvoiceItemDto> allItems = new java.util.ArrayList<>();

        // Collect all items from all phases
        for (PatientPlanPhase phase : plan.getPhases()) {
            for (PatientPlanItem item : phase.getItems()) {
                // Fetch service to get serviceCode
                DentalService service = dentalServiceRepository.findById(item.getServiceId().longValue())
                        .orElseThrow(() -> new NotFoundException("Service not found: " + item.getServiceId()));

                allItems.add(CreateInvoiceRequest.InvoiceItemDto.builder()
                        .serviceId(item.getServiceId())
                        .serviceCode(service.getServiceCode())
                        .serviceName(item.getItemName())
                        .quantity(1)
                        .unitPrice(item.getPrice())
                        .notes("Phase " + phase.getPhaseNumber() + ": " + phase.getPhaseName())
                        .build());
            }
        }

        if (allItems.isEmpty()) {
            log.warn("⚠️ No items found for plan: {}. Skipping invoice creation.", plan.getPlanCode());
            return;
        }

        // Create single invoice
        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .invoiceType(InvoiceType.TREATMENT_PLAN)
                .patientId(plan.getPatient().getPatientId())
                .treatmentPlanId(plan.getPlanId().intValue())
                .phaseNumber(null) // FULL payment: no specific phase
                .installmentNumber(null)
                .items(allItems)
                .notes("Tự động tạo khi duyệt lộ trình - Thanh toán toàn bộ (FULL)")
                .dueDate(LocalDateTime.now().plusDays(7)) // Due in 7 days
                .build();

        invoiceService.createInvoice(request);
        log.info("✅ Created FULL payment invoice for plan: {}", plan.getPlanCode());
    }

    /**
     * Create separate invoices for EACH PHASE (paymentType = PHASED).
     * Each invoice has:
     * - phaseNumber = phase.phaseNumber (1, 2, 3, ...)
     * - installmentNumber = null
     * - Items only from that phase
     */
    private void createPhasedInvoices(PatientTreatmentPlan plan) {
        log.info("Creating PHASED invoices for plan: {} ({} phases)",
                plan.getPlanCode(), plan.getPhases().size());

        int createdCount = 0;

        for (PatientPlanPhase phase : plan.getPhases()) {
            List<CreateInvoiceRequest.InvoiceItemDto> phaseItems = new java.util.ArrayList<>();

            // Collect items from this phase only
            for (PatientPlanItem item : phase.getItems()) {
                // Fetch service to get serviceCode
                DentalService service = dentalServiceRepository.findById(item.getServiceId().longValue())
                        .orElseThrow(() -> new NotFoundException("Service not found: " + item.getServiceId()));

                phaseItems.add(CreateInvoiceRequest.InvoiceItemDto.builder()
                        .serviceId(item.getServiceId())
                        .serviceCode(service.getServiceCode())
                        .serviceName(item.getItemName())
                        .quantity(1)
                        .unitPrice(item.getPrice())
                        .notes(null)
                        .build());
            }

            if (phaseItems.isEmpty()) {
                log.warn("⚠️ No items in phase {} of plan: {}. Skipping invoice for this phase.",
                        phase.getPhaseNumber(), plan.getPlanCode());
                continue;
            }

            // Create invoice for this phase
            CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                    .invoiceType(InvoiceType.TREATMENT_PLAN)
                    .patientId(plan.getPatient().getPatientId())
                    .treatmentPlanId(plan.getPlanId().intValue())
                    .phaseNumber(phase.getPhaseNumber()) // PHASED: specific phase number
                    .installmentNumber(null)
                    .items(phaseItems)
                    .notes(String.format("Tự động tạo khi duyệt lộ trình - Giai đoạn %d: %s",
                            phase.getPhaseNumber(), phase.getPhaseName()))
                    .dueDate(LocalDateTime.now().plusDays(7)) // Due in 7 days
                    .build();

            invoiceService.createInvoice(request);
            createdCount++;
            log.info("✅ Created invoice for phase {} of plan: {}", phase.getPhaseNumber(), plan.getPlanCode());
        }

        log.info("✅ Successfully created {} phased invoices for plan: {}", createdCount, plan.getPlanCode());
    }

    /**
     * Create multiple invoices for INSTALLMENT payment type.
     * - Divides total cost evenly across installments
     * - phaseNumber = null (not phase-based)
     * - installmentNumber = 1, 2, 3, ...
     * - Each installment has proportional share of items
     * 
     * Business Rules:
     * - Number of installments from plan.installmentCount (default: 3)
     * - Interval between installments from plan.installmentIntervalDays (default: 30 days)
     * - Items distributed evenly across installments
     * - If items can't be divided evenly, first installments get extra items
     * - Due dates staggered based on interval
     * 
     * Example: 3 installments, 5 items total
     * - Installment 1: 2 items (due in 7 days)
     * - Installment 2: 2 items (due in 37 days)
     * - Installment 3: 1 item (due in 67 days)
     */
    private void createInstallmentInvoices(PatientTreatmentPlan plan) {
        log.info("Creating INSTALLMENT invoices for plan: {}", plan.getPlanCode());

        // Get installment configuration (with defaults)
        int installmentCount = (plan.getInstallmentCount() != null && plan.getInstallmentCount() > 0) 
                               ? plan.getInstallmentCount() : 3;
        int intervalDays = (plan.getInstallmentIntervalDays() != null && plan.getInstallmentIntervalDays() > 0) 
                           ? plan.getInstallmentIntervalDays() : 30;

        log.info("Installment configuration - count: {}, interval: {} days", installmentCount, intervalDays);

        // Collect all items from all phases
        List<CreateInvoiceRequest.InvoiceItemDto> allItems = new java.util.ArrayList<>();
        
        for (PatientPlanPhase phase : plan.getPhases()) {
            for (PatientPlanItem item : phase.getItems()) {
                DentalService service = dentalServiceRepository.findById(item.getServiceId().longValue())
                        .orElseThrow(() -> new NotFoundException("Service not found: " + item.getServiceId()));

                allItems.add(CreateInvoiceRequest.InvoiceItemDto.builder()
                        .serviceId(item.getServiceId())
                        .serviceCode(service.getServiceCode())
                        .serviceName(item.getItemName())
                        .quantity(1)
                        .unitPrice(item.getPrice())
                        .notes(String.format("Giai đoạn %d - %s", phase.getPhaseNumber(), phase.getPhaseName()))
                        .build());
            }
        }

        if (allItems.isEmpty()) {
            log.warn("No items found in plan: {}. Skipping installment invoice creation.", plan.getPlanCode());
            return;
        }

        // Distribute items across installments
        int totalItems = allItems.size();
        int itemsPerInstallment = totalItems / installmentCount;
        int remainderItems = totalItems % installmentCount;

        log.info("Distributing {} items across {} installments ({} items per installment, {} remainder)", 
                 totalItems, installmentCount, itemsPerInstallment, remainderItems);

        int currentIndex = 0;
        int createdCount = 0;

        for (int installmentNum = 1; installmentNum <= installmentCount; installmentNum++) {
            // Calculate how many items for this installment
            // First 'remainderItems' installments get one extra item
            int itemsForThisInstallment = itemsPerInstallment + (installmentNum <= remainderItems ? 1 : 0);
            
            // Extract items for this installment
            List<CreateInvoiceRequest.InvoiceItemDto> installmentItems = new java.util.ArrayList<>();
            for (int i = 0; i < itemsForThisInstallment && currentIndex < totalItems; i++) {
                installmentItems.add(allItems.get(currentIndex));
                currentIndex++;
            }

            if (installmentItems.isEmpty()) {
                log.warn("No items for installment {}. Skipping.", installmentNum);
                continue;
            }

            // Calculate due date for this installment
            // First installment due in 7 days, subsequent ones based on interval
            int daysUntilDue = 7 + ((installmentNum - 1) * intervalDays);
            LocalDateTime dueDate = LocalDateTime.now().plusDays(daysUntilDue);

            // Create invoice for this installment
            CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                    .invoiceType(InvoiceType.TREATMENT_PLAN)
                    .patientId(plan.getPatient().getPatientId())
                    .treatmentPlanId(plan.getPlanId().intValue())
                    .phaseNumber(null) // INSTALLMENT: no specific phase
                    .installmentNumber(installmentNum)
                    .items(installmentItems)
                    .notes(String.format("Tự động tạo khi duyệt lộ trình - Đợt trả góp %d/%d", 
                            installmentNum, installmentCount))
                    .dueDate(dueDate)
                    .build();

            invoiceService.createInvoice(request);
            createdCount++;
            
            log.info("✅ Created invoice for installment {}/{} (due: {}, items: {}) of plan: {}", 
                     installmentNum, installmentCount, dueDate.toLocalDate(), 
                     installmentItems.size(), plan.getPlanCode());
        }

        log.info("✅ Successfully created {} installment invoices for plan: {}", createdCount, plan.getPlanCode());
    }

    /**
     * Update existing PENDING_PAYMENT invoice with new plan items.
     * This prevents duplicate invoices when items are added to approved plans.
     * 
     * @param existingInvoice The PENDING_PAYMENT invoice to update
     * @param plan The treatment plan with all items (including newly added ones)
     */
    private void updateInvoiceWithNewPlanItems(Invoice existingInvoice, PatientTreatmentPlan plan) {
        log.info("Updating invoice {} with new items from plan {}",
                existingInvoice.getInvoiceCode(), plan.getPlanCode());

        // Get all plan items (including newly added ones)
        List<PatientPlanItem> allPlanItems = getAllPlanItems(plan);

        // Get existing invoice items
        List<InvoiceItem> existingItems = invoiceItemRepository.findByInvoice_InvoiceId(
                existingInvoice.getInvoiceId());

        // Find new items that are not in existing invoice
        List<PatientPlanItem> newItems = findNewItemsNotInInvoice(allPlanItems, existingItems);

        if (newItems.isEmpty()) {
            log.info("No new items to add to invoice {}. Invoice remains unchanged.",
                    existingInvoice.getInvoiceCode());
            return;
        }

        log.info("Found {} new items to add to invoice {}", newItems.size(), existingInvoice.getInvoiceCode());

        // Add new items to invoice
        BigDecimal additionalAmount = BigDecimal.ZERO;
        for (PatientPlanItem planItem : newItems) {
            DentalService service = dentalServiceRepository.findById(planItem.getServiceId().longValue())
                    .orElse(null);
            String serviceCode = service != null ? service.getServiceCode() : "N/A";

            InvoiceItem newInvoiceItem = InvoiceItem.builder()
                    .invoice(existingInvoice)
                    .serviceId(planItem.getServiceId())
                    .serviceCode(serviceCode)
                    .serviceName(planItem.getItemName())
                    .quantity(1)
                    .unitPrice(planItem.getPrice())
                    .subtotal(planItem.getPrice())
                    .notes("Hạng mục mới được thêm vào lộ trình")
                    .build();

            invoiceItemRepository.save(newInvoiceItem);
            additionalAmount = additionalAmount.add(planItem.getPrice());
            
            log.debug("Added item: {} - {} VND", planItem.getItemName(), planItem.getPrice());
        }

        // Update invoice total
        BigDecimal oldTotalAmount = existingInvoice.getTotalAmount();
        BigDecimal newTotalAmount = oldTotalAmount.add(additionalAmount);
        existingInvoice.setTotalAmount(newTotalAmount);
        existingInvoice.setRemainingDebt(newTotalAmount.subtract(existingInvoice.getPaidAmount()));
        existingInvoice.setUpdatedAt(LocalDateTime.now());

        // Update notes
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String updateNote = String.format(" | Đã cập nhật: Thêm %d hạng mục mới (+%s VND) vào %s",
                newItems.size(),
                formatCurrency(additionalAmount),
                timestamp);
        
        String currentNotes = existingInvoice.getNotes() != null ? existingInvoice.getNotes() : "";
        existingInvoice.setNotes(currentNotes + updateNote);

        // Save updated invoice
        invoiceRepository.save(existingInvoice);

        log.info("✅ Updated invoice {} with {} new items. Total amount: {} → {} VND",
                existingInvoice.getInvoiceCode(),
                newItems.size(),
                oldTotalAmount,
                newTotalAmount);
    }

    /**
     * Create SUPPLEMENTAL invoice for new items when plan has PAID/PARTIAL_PAID invoices.
     * This handles the case where items are added after some payment has been made.
     * 
     * @param plan The treatment plan
     * @param existingInvoices List of existing invoices for this plan
     */
    private void createSupplementalInvoiceForNewItems(PatientTreatmentPlan plan, List<Invoice> existingInvoices) {
        log.info("Creating SUPPLEMENTAL invoice for new items in plan {}", plan.getPlanCode());

        // Get all plan items
        List<PatientPlanItem> allPlanItems = getAllPlanItems(plan);

        // Get all items already in existing invoices
        Set<Integer> serviceIdsInInvoices = existingInvoices.stream()
                .flatMap(inv -> invoiceItemRepository.findByInvoice_InvoiceId(inv.getInvoiceId()).stream())
                .map(InvoiceItem::getServiceId)
                .collect(Collectors.toSet());

        // Find new items not in any invoice
        List<PatientPlanItem> newItems = allPlanItems.stream()
                .filter(item -> !serviceIdsInInvoices.contains(item.getServiceId()))
                .collect(Collectors.toList());

        if (newItems.isEmpty()) {
            log.info("No new items to create supplemental invoice for plan {}.", plan.getPlanCode());
            return;
        }

        log.info("Found {} new items for supplemental invoice", newItems.size());

        // Create invoice items for new items
        List<CreateInvoiceRequest.InvoiceItemDto> supplementalItems = new ArrayList<>();
        for (PatientPlanItem planItem : newItems) {
            DentalService service = dentalServiceRepository.findById(planItem.getServiceId().longValue())
                    .orElse(null);
            String serviceCode = service != null ? service.getServiceCode() : "N/A";

            supplementalItems.add(CreateInvoiceRequest.InvoiceItemDto.builder()
                    .serviceId(planItem.getServiceId())
                    .serviceCode(serviceCode)
                    .serviceName(planItem.getItemName())
                    .quantity(1)
                    .unitPrice(planItem.getPrice())
                    .notes("Hạng mục bổ sung sau khi lộ trình đã được thanh toán")
                    .build());
        }

        // Create SUPPLEMENTAL invoice
        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .invoiceType(InvoiceType.SUPPLEMENTAL)
                .patientId(plan.getPatient().getPatientId())
                .treatmentPlanId(plan.getPlanId().intValue())
                .phaseNumber(null)
                .installmentNumber(null)
                .items(supplementalItems)
                .notes(String.format("Hóa đơn bổ sung cho các hạng mục mới được thêm vào lộ trình %s",
                        plan.getPlanCode()))
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();

        invoiceService.createInvoice(request);
        log.info("✅ Created SUPPLEMENTAL invoice with {} items for plan {}",
                supplementalItems.size(), plan.getPlanCode());
    }

    /**
     * Get all plan items from all phases.
     * 
     * @param plan The treatment plan
     * @return List of all items across all phases
     */
    private List<PatientPlanItem> getAllPlanItems(PatientTreatmentPlan plan) {
        List<PatientPlanItem> allItems = new ArrayList<>();
        for (PatientPlanPhase phase : plan.getPhases()) {
            allItems.addAll(phase.getItems());
        }
        return allItems;
    }

    /**
     * Find plan items that are not yet in the invoice.
     * Compares by serviceId to identify new items.
     * 
     * @param allPlanItems All items from the plan
     * @param existingInvoiceItems Existing items in the invoice
     * @return List of new items not in the invoice
     */
    private List<PatientPlanItem> findNewItemsNotInInvoice(
            List<PatientPlanItem> allPlanItems,
            List<InvoiceItem> existingInvoiceItems) {

        Set<Integer> existingServiceIds = existingInvoiceItems.stream()
                .map(InvoiceItem::getServiceId)
                .collect(Collectors.toSet());

        return allPlanItems.stream()
                .filter(planItem -> !existingServiceIds.contains(planItem.getServiceId()))
                .collect(Collectors.toList());
    }

    /**
     * Format currency amount for display in notes.
     * 
     * @param amount The amount to format
     * @return Formatted string (e.g., "1,000,000")
     */
    private String formatCurrency(BigDecimal amount) {
        return String.format("%,d", amount.longValue());
    }
}
