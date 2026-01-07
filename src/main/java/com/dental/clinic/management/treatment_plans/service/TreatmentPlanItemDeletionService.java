package com.dental.clinic.management.treatment_plans.service;

import com.dental.clinic.management.account.domain.Account;
import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.booking_appointment.repository.PatientPlanItemRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.ConflictException;
import com.dental.clinic.management.exception.NotFoundException;
import com.dental.clinic.management.payment.domain.Invoice;
import com.dental.clinic.management.payment.enums.InvoicePaymentStatus;
import com.dental.clinic.management.payment.repository.InvoiceRepository;
import com.dental.clinic.management.treatment_plans.domain.*;
import com.dental.clinic.management.treatment_plans.enums.PlanItemStatus;
import com.dental.clinic.management.treatment_plans.dto.response.DeletePlanItemResponse;
import com.dental.clinic.management.treatment_plans.repository.PlanAuditLogRepository;
import com.dental.clinic.management.treatment_plans.repository.PatientTreatmentPlanRepository;
import com.dental.clinic.management.utils.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service cho API 5.11: Xóa Hạng mục khỏi Lộ trình
 *
 * Business Logic:
 * - 2 Guards: Item status check, Approval status check
 * - Financial calculation TRƯỚC KHI delete (tránh lost reference)
 * - Audit log với format chuẩn: "Item {id} ({name}): -{price} VND"
 * - Keep DRAFT status (no auto-trigger)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanItemDeletionService {

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
         * Xóa một hạng mục khỏi lộ trình điều trị
         *
         * @param itemId ID của hạng mục cần xóa
         * @return DeletePlanItemResponse chứa thông tin item đã xóa và tác động tài
         *         chính
         * @throws NotFoundException nếu item không tồn tại
         * @throws ConflictException nếu item đã scheduled/in-progress/completed hoặc
         *                           plan không ở DRAFT
         */
        @Transactional
        public DeletePlanItemResponse deleteItem(Long itemId) {
                log.info(" API 5.11: Deleting plan item with id: {}", itemId);

                // 1⃣ Validate: Find item
                PatientPlanItem item = itemRepository.findById(itemId)
                                .orElseThrow(() -> {
                                        log.error("Item not found: {}", itemId);
                                        return new NotFoundException("Hạng mục không tồn tại");
                                });

                // 2⃣ Get parent entities and item data (BEFORE delete)
                PatientPlanPhase phase = item.getPhase();
                PatientTreatmentPlan plan = phase.getTreatmentPlan();
                BigDecimal deletedPrice = item.getPrice();
                String deletedItemName = item.getItemName();

                // 2.5⃣ RBAC verification (EMPLOYEE can only modify plans they created)
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                rbacService.verifyEmployeeCanModifyPlan(plan, authentication);

                log.info(" Item details: id={}, name='{}', price={}, status={}, plan_id={}",
                                itemId, deletedItemName, deletedPrice, item.getStatus(), plan.getPlanId());

                // 3⃣ GUARD 1: Item Status Check (CRITICAL!)
                validateItemNotScheduledOrCompleted(item);

                // 4⃣ GUARD 2: Approval Status Check (CRITICAL!)
                validatePlanNotApprovedOrPendingReview(plan);

                // 5⃣ Update Finances (BEFORE delete - tránh lost reference)
                updatePlanFinances(plan, deletedPrice);

                // 6⃣ Execute Delete
                itemRepository.delete(item);
                log.info("Item {} deleted from database", itemId);

                // 6.5⃣ Handle invoice sync if plan is APPROVED (Issue 3)
                // Price change is negative (item deleted)
                BigDecimal priceChange = deletedPrice.negate();
                handleInvoiceSyncOnPlanUpdate(plan, priceChange);

                // 6.7⃣ Recreate invoices if needed
                recreateInvoicesForUpdatedPlan(plan);

                // 7⃣ Create Audit Log (AFTER delete - using saved data)
                Integer performedBy = getCurrentEmployeeId();
                createAuditLog(plan, performedBy, itemId, deletedItemName, deletedPrice);

                // 8⃣ Build Response (Option B)
                DeletePlanItemResponse response = DeletePlanItemResponse.of(
                                itemId,
                                deletedItemName,
                                deletedPrice,
                                plan.getTotalPrice(),
                                plan.getFinalCost());

                log.info("API 5.11 completed: Item {} deleted. Price reduction: {} VND. New plan total: {} VND",
                                itemId, deletedPrice, plan.getTotalPrice());

                return response;
        }

        /**
         * GUARD 1: Item must be PENDING (not scheduled, in-progress, or completed)
         *
         * @throws ConflictException if item is SCHEDULED/IN_PROGRESS/COMPLETED
         */
        private void validateItemNotScheduledOrCompleted(PatientPlanItem item) {
                if (item.getStatus() == PlanItemStatus.SCHEDULED
                                || item.getStatus() == PlanItemStatus.IN_PROGRESS
                                || item.getStatus() == PlanItemStatus.COMPLETED) {

                        String errorMsg = String.format(
                                        "Không thể xóa hạng mục đã được đặt lịch hoặc đang thực hiện (Trạng thái: %s). "
                                                        +
                                                        "Vui lòng hủy lịch hẹn hoặc đánh dấu 'Bỏ qua' (Skip) nếu cần.",
                                        item.getStatus());

                        log.error("GUARD 1 FAILED: Item {} has status {}", item.getItemId(), item.getStatus());

                        // Use specific error code for better frontend handling
                        throw new ConflictException("ITEM_SCHEDULED_CANNOT_DELETE", errorMsg);
                }

                log.debug("GUARD 1 PASSED: Item {} is in status {}", item.getItemId(), item.getStatus());
        }

        /**
         * GUARD 2: Plan must be DRAFT (not APPROVED or PENDING_REVIEW)
         * Updated to allow deleting items from APPROVED plans with PENDING_PAYMENT invoices
         * Prevents deletion if invoices are PAID (items already paid cannot be deleted)
         *
         * @throws ConflictException if plan is PENDING_REVIEW or has PAID invoices
         */
        private void validatePlanNotApprovedOrPendingReview(PatientTreatmentPlan plan) {
                ApprovalStatus approvalStatus = plan.getApprovalStatus();

                // PENDING_REVIEW: Not allowed to delete
                if (approvalStatus == ApprovalStatus.PENDING_REVIEW) {
                        throw new ConflictException(
                                        String.format("Không thể xóa hạng mục khỏi lộ trình đang chờ duyệt (Trạng thái: %s). " +
                                                        "Yêu cầu Quản lý 'Từ chối' (Reject) về DRAFT trước khi sửa.", approvalStatus));
                }

                // APPROVED: Check invoice status
                if (approvalStatus == ApprovalStatus.APPROVED) {
                        List<Invoice> invoices = invoiceRepository.findByTreatmentPlanIdOrderByCreatedAtDesc(plan.getPlanId().intValue());

                        if (invoices.isEmpty()) {
                                log.warn("⚠️ Plan {} is APPROVED but has no invoices. Allowing deletion.", plan.getPlanCode());
                                return;
                        }

                        // Check if any invoice is PAID
                        boolean hasFullyPaidInvoice = invoices.stream()
                                        .anyMatch(inv -> inv.getPaymentStatus() == InvoicePaymentStatus.PAID);

                        if (hasFullyPaidInvoice) {
                                // PAID invoices exist - deletion is NOT allowed
                                throw new ConflictException(
                                                "PLAN_PAID_CANNOT_DELETE",
                                                "Không thể xóa hạng mục khỏi lộ trình đã thanh toán. " +
                                                                "Chỉ có thể xóa hạng mục khi hóa đơn chưa thanh toán hoặc thanh toán một phần.");
                        } else {
                                log.info("Plan {} has unpaid/partially-paid invoices. Deletion allowed with invoice sync.", plan.getPlanCode());
                        }
                }

                log.debug("GUARD 2 PASSED: Plan {} is in approval status {}",
                                plan.getPlanId(), plan.getApprovalStatus());
        }

        /**
         * Update plan financial totals (BEFORE delete)
         *
         * Assumption: Discount amount is fixed, so both totalCost and finalCost
         * decrease by deletedPrice
         */
        private void updatePlanFinances(PatientTreatmentPlan plan, BigDecimal deletedPrice) {
                BigDecimal oldTotalPrice = plan.getTotalPrice();
                BigDecimal oldFinalCost = plan.getFinalCost();

                plan.setTotalPrice(plan.getTotalPrice().subtract(deletedPrice));
                plan.setFinalCost(plan.getFinalCost().subtract(deletedPrice));

                planRepository.save(plan);

                log.info(" Financial update: TotalPrice {} -> {}, FinalCost {} -> {}",
                                oldTotalPrice, plan.getTotalPrice(),
                                oldFinalCost, plan.getFinalCost());
        }

        /**
         * Create audit log với format chuẩn hóa: "Item {id} ({name}): -{price} VND"
         *
         * Format giống API 5.10 để dễ parse/search logs
         */
        private void createAuditLog(PatientTreatmentPlan plan, Integer performedBy,
                        Long itemId, String itemName, BigDecimal price) {
                // Standardized format (consistent with API 5.10)
                String notes = String.format("Item %d (%s): -%.0f VND", itemId, itemName, price);

                Employee performer = employeeRepository.findById(performedBy)
                                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên"));

                PlanAuditLog auditLog = PlanAuditLog.builder()
                                .treatmentPlan(plan)
                                .actionType("ITEM_DELETED")
                                .performedBy(performer)
                                .notes(notes)
                                .oldApprovalStatus(plan.getApprovalStatus())
                                .newApprovalStatus(plan.getApprovalStatus()) // No change (keep DRAFT)
                                .build();

                auditLogRepository.save(auditLog);

                log.info("Audit log created: action=ITEM_DELETED, notes='{}'", notes);
        }

        /**
         * Get current employee ID from security context
         */
        private Integer getCurrentEmployeeId() {
                Optional<String> currentLogin = SecurityUtil.getCurrentUserLogin();
                if (currentLogin.isEmpty()) {
                        log.error("No authenticated user found in security context");
                        throw new ConflictException("AUTH_USER_NOT_FOUND", "Không thể xác định người thực hiện");
                }

                Account account = accountRepository.findByUsernameWithRoleAndPermissions(currentLogin.get())
                                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));

                if (account.getEmployee() == null || account.getEmployee().getEmployeeId() == null) {
                        log.error("Account {} has no linked employee", currentLogin.get());
                        throw new ConflictException("EMPLOYEE_NOT_LINKED", "Tài khoản không liên kết với nhân viên");
                }

                return account.getEmployee().getEmployeeId();
        }

        /**
         * Handle invoice synchronization when plan is updated (item deleted).
         * Implements Issue 3: Plan update sync with invoices
         */
        private void handleInvoiceSyncOnPlanUpdate(PatientTreatmentPlan plan, BigDecimal priceChange) {
                if (plan.getApprovalStatus() != ApprovalStatus.APPROVED) {
                        return;
                }

                if (priceChange.compareTo(BigDecimal.ZERO) == 0) {
                        log.info("No price change detected. Skipping invoice sync.");
                        return;
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
                                // Case 3: This should not happen as deletion is blocked for PAID invoices
                                log.warn("⚠️ Unexpected: Item deletion from PAID plan {}. This should have been blocked.", 
                                        plan.getPlanCode());
                        }
                }
        }

        /**
         * Handle PENDING_PAYMENT invoice - cancel and recreate
         */
        private void handlePendingPaymentInvoice(Invoice invoice, PatientTreatmentPlan plan) {
                log.info("Cancelling PENDING_PAYMENT invoice {} due to plan item deletion", invoice.getInvoiceCode());
                
                invoice.setPaymentStatus(InvoicePaymentStatus.CANCELLED);
                String currentNotes = invoice.getNotes() != null ? invoice.getNotes() : "";
                invoice.setNotes(currentNotes + " | Cancelled due to item deletion at " + 
                                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                invoiceRepository.save(invoice);
                
                log.info("✅ Cancelled invoice {}. New invoice will be created after plan update completes.", 
                                invoice.getInvoiceCode());
        }

        /**
         * Case 2: Handle PARTIAL_PAID invoice - create supplemental invoice for price reduction
         */
        private void handlePartialPaidInvoice(PatientTreatmentPlan plan, BigDecimal priceChange) {
                log.info("Creating SUPPLEMENTAL invoice for PARTIAL_PAID plan {} with price change: {}", 
                                plan.getPlanCode(), priceChange);
                
                createSupplementalInvoice(plan, priceChange, "Bổ sung do xóa hạng mục khỏi lộ trình (thanh toán một phần)");
        }

        /**
         * Create supplemental invoice for item deletion (price reduction)
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
                                .serviceId(null)
                                .serviceCode("PLAN_ADJUSTMENT")
                                .serviceName(changeDescription + " lộ trình điều trị (xóa hạng mục)")
                                .quantity(1)
                                .unitPrice(priceChange.abs())
                                .notes("Điều chỉnh giá do xóa hạng mục: " + plan.getPlanCode())
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
         * Recreate invoices for an updated plan
         */
        private void recreateInvoicesForUpdatedPlan(PatientTreatmentPlan plan) {
                log.info("Checking if invoices need recreation for plan: {}", plan.getPlanCode());
                
                List<Invoice> invoices = invoiceRepository.findByTreatmentPlanIdOrderByCreatedAtDesc(plan.getPlanId().intValue());
                boolean hasCancelledInvoices = invoices.stream()
                                .anyMatch(inv -> inv.getPaymentStatus() == InvoicePaymentStatus.CANCELLED);
                
                boolean hasActiveInvoices = invoices.stream()
                                .anyMatch(inv -> inv.getPaymentStatus() != InvoicePaymentStatus.CANCELLED);
                
                if (hasCancelledInvoices && !hasActiveInvoices) {
                        log.info("All invoices were cancelled. Recreating invoices for plan: {}", plan.getPlanCode());
                        approvalService.createInvoicesForApprovedPlan(plan);
                }
        }
}
