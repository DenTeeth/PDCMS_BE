package com.dental.clinic.management.treatment_plans.service;

import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.booking_appointment.repository.PatientPlanItemRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.BadRequestException;
import com.dental.clinic.management.exception.ConflictException;
import com.dental.clinic.management.exception.NotFoundException;
import com.dental.clinic.management.payment.domain.Invoice;
import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.repository.InvoiceRepository;
import com.dental.clinic.management.treatment_plans.domain.ApprovalStatus;
import com.dental.clinic.management.treatment_plans.domain.PatientPlanItem;
import com.dental.clinic.management.treatment_plans.domain.PatientTreatmentPlan;
import com.dental.clinic.management.treatment_plans.domain.PlanAuditLog;
import com.dental.clinic.management.treatment_plans.dto.request.UpdatePlanItemRequest;
import com.dental.clinic.management.treatment_plans.dto.response.UpdatePlanItemResponse;
import com.dental.clinic.management.treatment_plans.enums.PlanItemStatus;
import com.dental.clinic.management.treatment_plans.repository.PatientTreatmentPlanRepository;
import com.dental.clinic.management.treatment_plans.repository.PlanAuditLogRepository;
import com.dental.clinic.management.utils.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for API 5.10: Update Treatment Plan Item.
 * Handles updating item details (name, price, estimated time) with business
 * rules validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanItemUpdateService {

    private final PatientPlanItemRepository itemRepository;
    private final PatientTreatmentPlanRepository planRepository;
    private final PlanAuditLogRepository auditLogRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final TreatmentPlanRBACService rbacService;
    private final InvoiceRepository invoiceRepository;
    private final TreatmentPlanApprovalService approvalService;
    private final com.dental.clinic.management.payment.service.InvoiceService invoiceService;

    /**
     * API 5.10: Update a treatment plan item.
     *
     * Business Rules:
     * 1. Item must exist
     * 2. Item must NOT be SCHEDULED, IN_PROGRESS, or COMPLETED
     * 3. Plan must NOT be APPROVED or PENDING_REVIEW (unless invoice allows it)
     * 4. Update item fields (only non-null values)
     * 5. Recalculate plan finances if price changed
     * 6. Create audit log (action: ITEM_UPDATED)
     * 7. Keep approval status as DRAFT (no auto-trigger to PENDING_REVIEW)
     * 8. (Issue 3) Handle invoice sync: cancel/recreate or create supplemental
     *
     * @param itemId  The item ID to update
     * @param request The update request with optional fields
     * @return Response with updated item and financial impact
     */
    @Transactional
    public UpdatePlanItemResponse updatePlanItem(Long itemId, UpdatePlanItemRequest request) {

        log.info("Starting update for plan item: {}", itemId);

        // 0. Validate request has at least one field
        if (!request.hasAnyUpdate()) {
            throw new BadRequestException("Phải có ít nhất một trường cần cập nhật");
        }

        // 1. Find item
        PatientPlanItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Hạng mục không tồn tại"));

        // 2. Get treatment plan
        PatientTreatmentPlan plan = item.getPhase().getTreatmentPlan();

        // 2.5. RBAC verification (EMPLOYEE can only modify plans they created)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        rbacService.verifyEmployeeCanModifyPlan(plan, authentication);

        // 3. GUARD 1: Item status check
        validateItemNotScheduledOrCompleted(item);

        // 4. GUARD 2: Approval status check
        validatePlanNotApprovedOrPendingReview(plan);

        // 4.5. GUARD 3: Check if item is in a PAID invoice (Issue 3 - Case 3)
        validateItemNotInPaidInvoice(plan, itemId);

        // 5. Store old price for financial calculation
        BigDecimal oldPrice = item.getPrice();

        // 6. Update item fields (only non-null)
        boolean updated = updateItemFields(item, request);

        if (!updated) {
            throw new BadRequestException("Không có thay đổi nào được thực hiện");
        }

        // 7. Save item
        item = itemRepository.save(item);
        log.info("Updated item {} successfully", itemId);

        // 8. Calculate financial impact
        BigDecimal newPrice = item.getPrice();
        BigDecimal priceChange = BigDecimal.ZERO;

        if (!oldPrice.equals(newPrice)) {
            priceChange = newPrice.subtract(oldPrice);
            updatePlanFinances(plan, priceChange);
            log.info("Updated plan finances. Price change: {}", priceChange);
        }

        // 9. Handle invoice sync if plan is APPROVED (Issue 3)
        handleInvoiceSyncOnPlanUpdate(plan, priceChange);

        // 10. Recreate invoices if needed (for PENDING_PAYMENT invoices that were cancelled)
        recreateInvoicesForUpdatedPlan(plan);

        // 11. Create audit log
        createAuditLog(plan, item, oldPrice, newPrice);

        // 12. Build response
        return buildResponse(item, plan, priceChange);
    }

    /**
     * GUARD 1: Validate item is not scheduled or completed
     */
    private void validateItemNotScheduledOrCompleted(PatientPlanItem item) {
        PlanItemStatus status = item.getStatus();

        if (status == PlanItemStatus.SCHEDULED ||
                status == PlanItemStatus.IN_PROGRESS ||
                status == PlanItemStatus.COMPLETED) {

            throw new ConflictException(
                    String.format("Không thể sửa hạng mục đã được đặt lịch hoặc đã hoàn thành (Trạng thái: %s). " +
                            "Vui lòng hủy lịch hẹn trước khi sửa.", status));
        }
    }

    /**
     * GUARD 2: Validate plan is not approved or pending review
     * Updated to allow editing APPROVED plans based on invoice status:
     * - PENDING_PAYMENT: Allow editing (will cancel invoice and create new)
     * - PARTIAL_PAID: Allow editing (will create supplemental invoice)
     * - PAID: Allow ONLY adding new items (will create supplemental invoice)
     */
    private void validatePlanNotApprovedOrPendingReview(PatientTreatmentPlan plan) {
        ApprovalStatus approvalStatus = plan.getApprovalStatus();

        // PENDING_REVIEW: Not allowed to edit
        if (approvalStatus == ApprovalStatus.PENDING_REVIEW) {
            throw new ConflictException(
                    String.format("Không thể sửa lộ trình đang chờ duyệt (Trạng thái: %s). " +
                            "Yêu cầu Quản lý 'Từ chối' (Reject) về DRAFT trước khi sửa.", approvalStatus));
        }

        // APPROVED: Check invoice status before allowing edit
        if (approvalStatus == ApprovalStatus.APPROVED) {
            List<Invoice> invoices = invoiceRepository.findByTreatmentPlanIdOrderByCreatedAtDesc(plan.getPlanId().intValue());
            
            if (invoices.isEmpty()) {
                // No invoices yet - allow edit (shouldn't happen, but handle gracefully)
                log.warn("⚠️ Plan {} is APPROVED but has no invoices. Allowing edit.", plan.getPlanCode());
                return;
            }

            // Check if any invoice is PAID
            boolean hasFullyPaidInvoice = invoices.stream()
                    .anyMatch(inv -> inv.getPaymentStatus() == InvoicePaymentStatus.PAID);

            if (hasFullyPaidInvoice) {
                // PAID invoices exist - editing is restricted (only adding new items allowed)
                // This validation will be enforced at item level, not at plan level
                log.info("Plan {} has PAID invoices. Only adding new items is allowed.", plan.getPlanCode());
                // Note: We allow the flow to continue, but specific validations will be applied
                // when trying to modify/delete existing items
            } else {
                // All invoices are PENDING_PAYMENT or PARTIAL_PAID - allow edit
                log.info("Plan {} has unpaid/partially-paid invoices. Edit allowed with invoice sync.", plan.getPlanCode());
            }
        }
    }

    /**
     * GUARD 3: Validate item is not in a PAID invoice (Issue 3 - Case 3)
     * Items that have been fully paid cannot be modified
     */
    private void validateItemNotInPaidInvoice(PatientTreatmentPlan plan, Long itemId) {
        if (plan.getApprovalStatus() != ApprovalStatus.APPROVED) {
            return; // Only check for APPROVED plans
        }

        List<Invoice> paidInvoices = invoiceRepository.findByTreatmentPlanIdOrderByCreatedAtDesc(plan.getPlanId().intValue())
                .stream()
                .filter(inv -> inv.getPaymentStatus() == InvoicePaymentStatus.PAID)
                .collect(java.util.stream.Collectors.toList());

        if (!paidInvoices.isEmpty()) {
            // Plan has PAID invoices - item modifications are not allowed
            // Note: Adding new items is allowed (handled in TreatmentPlanItemAdditionService)
            throw new ConflictException(
                    "ITEM_IN_PAID_INVOICE",
                    String.format("Không thể sửa hạng mục này vì đã được thanh toán trong hóa đơn. " +
                            "Chỉ có thể thêm hạng mục mới vào lộ trình đã thanh toán. " +
                            "Nếu cần thay đổi, vui lòng tạo hóa đơn bổ sung (SUPPLEMENTAL)."));
        }
    }

    /**
     * Update item fields (only non-null values from request)
     * Returns true if any field was updated
     */
    private boolean updateItemFields(PatientPlanItem item, UpdatePlanItemRequest request) {
        boolean updated = false;

        if (request.getItemName() != null) {
            item.setItemName(request.getItemName());
            updated = true;
            log.debug("Updated itemName to: {}", request.getItemName());
        }

        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
            updated = true;
            log.debug("Updated price to: {}", request.getPrice());
        }

        if (request.getEstimatedTimeMinutes() != null) {
            item.setEstimatedTimeMinutes(request.getEstimatedTimeMinutes());
            updated = true;
            log.debug("Updated estimatedTimeMinutes to: {}", request.getEstimatedTimeMinutes());
        }

        return updated;
    }

    /**
     * Update plan finances when price changes
     */
    private void updatePlanFinances(PatientTreatmentPlan plan, BigDecimal priceChange) {
        // Update total price
        BigDecimal newTotalPrice = plan.getTotalPrice().add(priceChange);
        plan.setTotalPrice(newTotalPrice);

        // Update final cost (assuming discount is fixed and doesn't change)
        BigDecimal newFinalCost = plan.getFinalCost().add(priceChange);
        plan.setFinalCost(newFinalCost);

        planRepository.save(plan);
    }

    /**
     * Handle invoice synchronization when plan is updated.
     * Implements Issue 3: Plan update sync with invoices
     * 
     * Case 1: PENDING_PAYMENT - Cancel old invoice, create new one
     * Case 2: PARTIAL_PAID - Keep old invoice, create supplemental invoice for changes
     * Case 3: PAID - Keep old invoice, create supplemental invoice for changes
     */
    private void handleInvoiceSyncOnPlanUpdate(PatientTreatmentPlan plan, BigDecimal priceChange) {
        if (plan.getApprovalStatus() != ApprovalStatus.APPROVED) {
            // Only sync invoices for APPROVED plans
            return;
        }

        if (priceChange.compareTo(BigDecimal.ZERO) == 0) {
            log.info("No price change detected. Skipping invoice sync.");
            return; // No price change, no invoice update needed
        }

        List<Invoice> invoices = invoiceRepository.findByTreatmentPlanIdOrderByCreatedAtDesc(plan.getPlanId().intValue());
        
        if (invoices.isEmpty()) {
            log.warn("⚠️ Plan {} is APPROVED but has no invoices. No sync needed.", plan.getPlanCode());
            return;
        }

        for (Invoice invoice : invoices) {
            InvoicePaymentStatus status = invoice.getPaymentStatus();
            
            if (status == InvoicePaymentStatus.PENDING_PAYMENT) {
                // Case 1: Cancel old invoice and create new one
                handlePendingPaymentInvoice(invoice, plan);
                
            } else if (status == InvoicePaymentStatus.PARTIAL_PAID) {
                // Case 2: Keep old invoice, create supplemental invoice for price difference
                handlePartialPaidInvoice(plan, priceChange);
                
            } else if (status == InvoicePaymentStatus.PAID) {
                // Case 3: Keep old invoice, create supplemental invoice for price difference
                handlePaidInvoice(plan, priceChange);
            }
        }
    }

    /**
     * Case 1: Handle PENDING_PAYMENT invoice - cancel and recreate
     */
    private void handlePendingPaymentInvoice(Invoice invoice, PatientTreatmentPlan plan) {
        log.info("Cancelling PENDING_PAYMENT invoice {} due to plan update", invoice.getInvoiceCode());
        
        // Cancel old invoice
        invoice.setPaymentStatus(InvoicePaymentStatus.CANCELLED);
        String currentNotes = invoice.getNotes() != null ? invoice.getNotes() : "";
        invoice.setNotes(currentNotes + " | Cancelled due to plan update at " + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        invoiceRepository.save(invoice);
        
        log.info("✅ Cancelled invoice {}. New invoice will be created after plan update completes.", 
                invoice.getInvoiceCode());
        
        // Note: New invoice creation is delegated to approvalService.createInvoicesForApprovedPlan()
        // This will be called after the update completes
    }

    /**
     * Case 2: Handle PARTIAL_PAID invoice - create supplemental invoice for price difference
     */
    private void handlePartialPaidInvoice(PatientTreatmentPlan plan, BigDecimal priceChange) {
        log.info("Creating SUPPLEMENTAL invoice for PARTIAL_PAID plan {} with price change: {}", 
                plan.getPlanCode(), priceChange);
        
        createSupplementalInvoice(plan, priceChange, "Bổ sung do chỉnh sửa lộ trình điều trị (thanh toán một phần)");
    }

    /**
     * Case 3: Handle PAID invoice - create supplemental invoice for price difference
     */
    private void handlePaidInvoice(PatientTreatmentPlan plan, BigDecimal priceChange) {
        log.info("Creating SUPPLEMENTAL invoice for PAID plan {} with price change: {}", 
                plan.getPlanCode(), priceChange);
        
        createSupplementalInvoice(plan, priceChange, "Bổ sung do chỉnh sửa lộ trình điều trị (đã thanh toán)");
    }

    /**
     * Create supplemental invoice for plan changes
     */
    private void createSupplementalInvoice(PatientTreatmentPlan plan, BigDecimal priceChange, String notes) {
        if (priceChange.compareTo(BigDecimal.ZERO) == 0) {
            log.info("No price change, skipping supplemental invoice creation");
            return;
        }

        String changeDescription = priceChange.compareTo(BigDecimal.ZERO) > 0 
                ? "Tăng giá" 
                : "Giảm giá";

        List<com.dental.clinic.management.payment.dto.CreateInvoiceRequest.InvoiceItemDto> items = new java.util.ArrayList<>();
        
        // Create a single invoice item for the price difference
        items.add(com.dental.clinic.management.payment.dto.CreateInvoiceRequest.InvoiceItemDto.builder()
                .serviceId(null) // No specific service for price adjustment
                .serviceCode("PLAN_ADJUSTMENT")
                .serviceName(changeDescription + " lộ trình điều trị")
                .quantity(1)
                .unitPrice(priceChange.abs()) // Use absolute value
                .notes("Điều chỉnh giá do chỉnh sửa lộ trình: " + plan.getPlanCode())
                .build());

        com.dental.clinic.management.payment.dto.CreateInvoiceRequest request = 
                com.dental.clinic.management.payment.dto.CreateInvoiceRequest.builder()
                .invoiceType(com.dental.clinic.management.payment.enums.InvoiceType.TREATMENT_PLAN)
                .patientId(plan.getPatient().getPatientId())
                .treatmentPlanId(plan.getPlanId().intValue())
                .phaseNumber(null)
                .installmentNumber(null)
                .items(items)
                .notes("[SUPPLEMENTAL] " + notes + " | " + changeDescription + ": " + priceChange)
                .dueDate(java.time.LocalDateTime.now().plusDays(7))
                .build();

        invoiceService.createInvoice(request);
        log.info("✅ Created SUPPLEMENTAL invoice for plan {} with amount: {}", plan.getPlanCode(), priceChange);
    }

    /**
     * Recreate invoices for an updated plan.
     * Called after plan update when PENDING_PAYMENT invoices were cancelled.
     */
    private void recreateInvoicesForUpdatedPlan(PatientTreatmentPlan plan) {
        log.info("Recreating invoices for updated plan: {}", plan.getPlanCode());
        
        // Check if we need to recreate invoices (i.e., if any were cancelled)
        List<Invoice> invoices = invoiceRepository.findByTreatmentPlanIdOrderByCreatedAtDesc(plan.getPlanId().intValue());
        boolean hasCancelledInvoices = invoices.stream()
                .anyMatch(inv -> inv.getPaymentStatus() == InvoicePaymentStatus.CANCELLED);
        
        boolean hasActiveInvoices = invoices.stream()
                .anyMatch(inv -> inv.getPaymentStatus() != InvoicePaymentStatus.CANCELLED);
        
        if (hasCancelledInvoices && !hasActiveInvoices) {
            // All invoices were cancelled - recreate them
            log.info("All invoices were cancelled. Recreating invoices for plan: {}", plan.getPlanCode());
            approvalService.createInvoicesForApprovedPlan(plan);
        }
    }

    /**
     * Create audit log for item update
     */
    private void createAuditLog(PatientTreatmentPlan plan, PatientPlanItem item,
            BigDecimal oldPrice, BigDecimal newPrice) {

        Integer doctorId = getCurrentEmployeeId();
        Employee doctor = employeeRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Nhân viên không tồn tại"));

        String notes = buildAuditNotes(item, oldPrice, newPrice);

        PlanAuditLog auditLog = PlanAuditLog.builder()
                .treatmentPlan(plan)
                .actionType("ITEM_UPDATED")
                .performedBy(doctor)
                .notes(notes)
                .oldApprovalStatus(plan.getApprovalStatus())
                .newApprovalStatus(plan.getApprovalStatus()) // No change (keep DRAFT)
                .build();

        auditLogRepository.save(auditLog);
        log.info("Created audit log for item update: {}", item.getItemId());
    }

    /**
     * Build audit log notes
     */
    private String buildAuditNotes(PatientPlanItem item, BigDecimal oldPrice, BigDecimal newPrice) {
        if (!oldPrice.equals(newPrice)) {
            return String.format("Cập nhật item %d (%s): Giá thay đổi từ %s -> %s",
                    item.getItemId(),
                    item.getItemName(),
                    oldPrice,
                    newPrice);
        } else {
            return String.format("Cập nhật item %d (%s): Sửa tên hoặc thời gian ước tính",
                    item.getItemId(),
                    item.getItemName());
        }
    }

    /**
     * Build response DTO
     */
    private UpdatePlanItemResponse buildResponse(PatientPlanItem item,
            PatientTreatmentPlan plan,
            BigDecimal priceChange) {

        UpdatePlanItemResponse.UpdatedItemDTO itemDTO = UpdatePlanItemResponse.UpdatedItemDTO.builder()
                .itemId(item.getItemId())
                .sequenceNumber(item.getSequenceNumber())
                .itemName(item.getItemName())
                .serviceId(item.getServiceId())
                .price(item.getPrice())
                .estimatedTimeMinutes(item.getEstimatedTimeMinutes())
                .status(item.getStatus().name())
                .build();

        UpdatePlanItemResponse.FinancialImpactDTO financialImpact = UpdatePlanItemResponse.FinancialImpactDTO.builder()
                .planTotalCost(plan.getTotalPrice())
                .planFinalCost(plan.getFinalCost())
                .priceChange(priceChange)
                .build();

        return UpdatePlanItemResponse.builder()
                .updatedItem(itemDTO)
                .financialImpact(financialImpact)
                .build();
    }

    /**
     * Get current employee ID from security context
     */
    private Integer getCurrentEmployeeId() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Người dùng chưa được xác thực"));

        return accountRepository.findOneByUsername(username)
                .map(account -> account.getEmployee().getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên cho người dùng: " + username));
    }
}
