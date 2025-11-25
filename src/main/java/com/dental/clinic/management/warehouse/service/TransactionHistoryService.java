package com.dental.clinic.management.warehouse.service;

import com.dental.clinic.management.booking_appointment.repository.AppointmentRepository;
import com.dental.clinic.management.exception.BadRequestException;
import com.dental.clinic.management.patient.domain.Patient;
import com.dental.clinic.management.patient.repository.PatientRepository;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.warehouse.domain.StorageTransaction;
import com.dental.clinic.management.warehouse.dto.request.TransactionHistoryRequest;
import com.dental.clinic.management.warehouse.dto.response.TransactionHistoryItemDto;
import com.dental.clinic.management.warehouse.dto.response.TransactionHistoryResponse;
import com.dental.clinic.management.warehouse.dto.response.TransactionSummaryStatsDto;
import com.dental.clinic.management.warehouse.enums.TransactionStatus;
import com.dental.clinic.management.warehouse.enums.TransactionType;
import com.dental.clinic.management.warehouse.repository.StorageTransactionRepository;
import com.dental.clinic.management.warehouse.specification.TransactionHistorySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * API 6.6: Transaction History Service
 *
 * Features:
 * - Comprehensive filtering (type, status, payment, date range, supplier,
 * appointment)
 * - RBAC-aware data masking (VIEW_COST permission)
 * - Aggregated statistics
 * - Pagination & sorting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionHistoryService {

    private final StorageTransactionRepository transactionRepository;
    private final PatientRepository patientRepository;

    /**
     * Get transaction history with advanced filtering
     *
     * @param request Filter criteria
     * @return Paginated transaction history with stats
     */
    @Transactional(readOnly = true)
    public TransactionHistoryResponse getTransactionHistory(TransactionHistoryRequest request) {
        log.info("📋 Fetching transaction history - Type: {}, Status: {}, Page: {}/{}",
                request.getType(), request.getStatus(), request.getPage(), request.getSize());

        // 1. Validate request
        validateRequest(request);

        // 2. Check permissions
        boolean hasViewCostPermission = hasPermission("VIEW_COST");
        log.debug("🔐 Permission check - VIEW_COST: {}", hasViewCostPermission);

        // 3. Build dynamic query specification
        Specification<StorageTransaction> spec = TransactionHistorySpecification.buildSpecification(request);

        // 4. Setup pagination & sorting
        Pageable pageable = createPageable(request);

        // 5. Execute query
        Page<StorageTransaction> page = transactionRepository.findAll(spec, pageable);

        // 6. Map to DTOs with RBAC
        List<TransactionHistoryItemDto> content = page.getContent().stream()
                .map(tx -> mapToDto(tx, hasViewCostPermission))
                .collect(Collectors.toList());

        // 7. Calculate summary stats
        TransactionSummaryStatsDto stats = calculateStats(spec, request, hasViewCostPermission);

        // 8. Build response
        return TransactionHistoryResponse.builder()
                .meta(TransactionHistoryResponse.MetaDto.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalPages(page.getTotalPages())
                        .totalElements(page.getTotalElements())
                        .build())
                .stats(stats)
                .content(content)
                .build();
    }

    /**
     * Validate request parameters
     */
    private void validateRequest(TransactionHistoryRequest request) {
        // Validate date range
        if (request.getFromDate() != null && request.getToDate() != null) {
            if (request.getFromDate().isAfter(request.getToDate())) {
                throw new BadRequestException(
                        "INVALID_DATE_RANGE",
                        "fromDate cannot be after toDate");
            }
        }

        // Validate page & size
        if (request.getPage() < 0) {
            throw new BadRequestException("INVALID_PAGE", "Page number cannot be negative");
        }
        if (request.getSize() <= 0 || request.getSize() > 100) {
            throw new BadRequestException("INVALID_SIZE", "Size must be between 1 and 100");
        }

        // Validate sort direction
        if (!"asc".equalsIgnoreCase(request.getSortDir()) && !"desc".equalsIgnoreCase(request.getSortDir())) {
            throw new BadRequestException("INVALID_SORT_DIR", "Sort direction must be 'asc' or 'desc'");
        }
    }

    /**
     * Create Pageable object with sorting
     */
    private Pageable createPageable(TransactionHistoryRequest request) {
        Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortDir())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, request.getSortBy());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    /**
     * Map entity to DTO with RBAC data masking
     */
    private TransactionHistoryItemDto mapToDto(StorageTransaction tx, boolean hasViewCostPermission) {
        TransactionHistoryItemDto dto = TransactionHistoryItemDto.builder()
                .transactionId(tx.getTransactionId())
                .transactionCode(tx.getTransactionCode())
                .type(tx.getTransactionType())
                .transactionDate(tx.getTransactionDate())
                .status(tx.getApprovalStatus())
                .notes(tx.getNotes())
                .createdByName(tx.getCreatedBy() != null ? tx.getCreatedBy().getFullName() : null)
                .createdAt(tx.getCreatedAt())
                .totalItems(tx.getItems() != null ? tx.getItems().size() : 0)
                .build();

        // Import-specific fields
        if (tx.getTransactionType() == TransactionType.IMPORT && tx.getSupplier() != null) {
            dto.setSupplierName(tx.getSupplier().getSupplierName());
            dto.setInvoiceNumber(tx.getInvoiceNumber());
            dto.setPaymentStatus(tx.getPaymentStatus());
            dto.setDueDate(tx.getDueDate());

            // Payment info (requires VIEW_COST)
            if (hasViewCostPermission) {
                dto.setPaidAmount(tx.getPaidAmount());
                dto.setRemainingDebt(tx.getRemainingDebt());
            }
        }

        // Export-specific fields
        if (tx.getTransactionType() == TransactionType.EXPORT && tx.getRelatedAppointment() != null) {
            dto.setRelatedAppointmentId(tx.getRelatedAppointment().getAppointmentId().longValue());
            dto.setRelatedAppointmentCode(tx.getRelatedAppointment().getAppointmentCode());

            // Get patient name via patientId
            Integer patientId = tx.getRelatedAppointment().getPatientId();
            if (patientId != null) {
                Optional<Patient> patient = patientRepository.findById(patientId);
                patient.ifPresent(p -> dto.setPatientName(p.getFullName()));
            }
        }

        // Approval info
        if (tx.getApprovedBy() != null) {
            dto.setApprovedByName(tx.getApprovedBy().getFullName());
            dto.setApprovedAt(tx.getApprovedAt());
        }

        // Financial data (RBAC: requires VIEW_COST)
        if (hasViewCostPermission) {
            dto.setTotalValue(tx.getTotalValue());
        } else {
            dto.setTotalValue(null); // Hide sensitive data
        }

        return dto;
    }

    /**
     * Calculate summary statistics for filtered transactions
     */
    private TransactionSummaryStatsDto calculateStats(
            Specification<StorageTransaction> spec,
            TransactionHistoryRequest request,
            boolean hasViewCostPermission) {

        TransactionSummaryStatsDto stats = TransactionSummaryStatsDto.builder()
                .periodStart(request.getFromDate())
                .periodEnd(request.getToDate())
                .build();

        // Count pending approval
        Specification<StorageTransaction> pendingSpec = spec.and(
                (root, query, cb) -> cb.equal(root.get("approvalStatus"), TransactionStatus.PENDING_APPROVAL));
        long pendingCount = transactionRepository.count(pendingSpec);
        stats.setPendingApprovalCount((int) pendingCount);

        // Calculate financial stats (requires VIEW_COST)
        if (hasViewCostPermission) {
            List<StorageTransaction> allTx = transactionRepository.findAll(spec);

            BigDecimal totalImport = allTx.stream()
                    .filter(tx -> tx.getTransactionType() == TransactionType.IMPORT)
                    .map(tx -> tx.getTotalValue() != null ? tx.getTotalValue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalExport = allTx.stream()
                    .filter(tx -> tx.getTransactionType() == TransactionType.EXPORT)
                    .map(tx -> tx.getTotalValue() != null ? tx.getTotalValue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            stats.setTotalImportValue(totalImport);
            stats.setTotalExportValue(totalExport);
        } else {
            stats.setTotalImportValue(null);
            stats.setTotalExportValue(null);
        }

        return stats;
    }

    /**
     * Check if current user has specific permission
     */
    private boolean hasPermission(String permission) {
        return SecurityUtil.hasCurrentUserPermission(permission);
    }
}
