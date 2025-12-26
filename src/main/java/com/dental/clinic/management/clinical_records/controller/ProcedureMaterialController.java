package com.dental.clinic.management.clinical_records.controller;

import com.dental.clinic.management.clinical_records.domain.ClinicalRecordProcedure;
import com.dental.clinic.management.clinical_records.domain.ProcedureMaterialUsage;
import com.dental.clinic.management.clinical_records.dto.ProcedureMaterialsResponse;
import com.dental.clinic.management.clinical_records.dto.UpdateProcedureMaterialsRequest;
import com.dental.clinic.management.clinical_records.dto.UpdateProcedureMaterialsResponse;
import com.dental.clinic.management.clinical_records.repository.ClinicalRecordProcedureRepository;
import com.dental.clinic.management.clinical_records.service.ProcedureMaterialService;
import com.dental.clinic.management.exception.NotFoundException;
import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.warehouse.repository.ItemBatchRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for procedure material consumption management
 * APIs for viewing and updating actual material usage
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/clinical-records/procedures")
@RequiredArgsConstructor
@Tag(name = "Procedure Materials", description = "Material consumption tracking for clinical procedures")
public class ProcedureMaterialController {

    private final ProcedureMaterialService procedureMaterialService;
    private final ClinicalRecordProcedureRepository procedureRepository;
    private final ItemBatchRepository itemBatchRepository;

    /**
     * API 8.7: Get Procedure Materials
     * 
     * Returns all materials used/planned for a procedure
     * Shows planned vs actual quantities, variance, and cost
     * 
     * Authorization: VIEW_CLINICAL_RECORD or WRITE_CLINICAL_RECORD
     */
    @Operation(summary = "API 8.7 - Get Procedure Materials",
               description = "Get all materials used for a procedure with planned vs actual quantities")
    @GetMapping("/{procedureId}/materials")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('VIEW_CLINICAL_RECORD') or hasAuthority('WRITE_CLINICAL_RECORD')")
    public ResponseEntity<ProcedureMaterialsResponse> getProcedureMaterials(
            @PathVariable Integer procedureId) {
        
        log.info("GET /api/v1/clinical-records/procedures/{}/materials", procedureId);

        // Get procedure
        ClinicalRecordProcedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new NotFoundException("PROCEDURE_NOT_FOUND",
                        "Procedure not found: " + procedureId));

        // Get material usage
        List<ProcedureMaterialUsage> usages = procedureMaterialService.getMaterialUsage(procedureId);

        // Check permission for viewing cost data
        boolean hasViewCostPermission = SecurityUtil
                .hasCurrentUserPermission(AuthoritiesConstants.VIEW_WAREHOUSE_COST);
        log.debug("Permission check - VIEW_WAREHOUSE_COST: {}", hasViewCostPermission);

        // Build response
        ProcedureMaterialsResponse response = ProcedureMaterialsResponse.builder()
                .procedureId(procedureId)
                .serviceName(procedure.getService() != null ? 
                    procedure.getService().getServiceName() : null)
                .serviceCode(procedure.getService() != null ? 
                    procedure.getService().getServiceCode() : null)
                .toothNumber(procedure.getToothNumber())
                .materialsDeducted(procedure.getMaterialsDeductedAt() != null)
                .deductedAt(procedure.getMaterialsDeductedAt())
                .deductedBy(procedure.getMaterialsDeductedBy())
                .storageTransactionId(procedure.getStorageTransactionId())
                .materials(mapToMaterialDTOs(usages, hasViewCostPermission))
                .build();

        // Calculate cost summaries (only if user has VIEW_WAREHOUSE_COST permission)
        if (hasViewCostPermission) {
            BigDecimal totalPlanned = usages.stream()
                    .map(u -> u.getPlannedQuantity().multiply(
                        u.getItemMaster().getCurrentMarketPrice() != null ? 
                        u.getItemMaster().getCurrentMarketPrice() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalActual = usages.stream()
                    .map(u -> u.getActualQuantity().multiply(
                        u.getItemMaster().getCurrentMarketPrice() != null ? 
                        u.getItemMaster().getCurrentMarketPrice() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            response.setTotalPlannedCost(totalPlanned);
            response.setTotalActualCost(totalActual);
            response.setCostVariance(totalActual.subtract(totalPlanned));
        } else {
            // Hide cost data from users without permission
            response.setTotalPlannedCost(null);
            response.setTotalActualCost(null);
            response.setCostVariance(null);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * API 8.8: Update Procedure Materials
     * 
     * Update actual quantities used by assistants/nurses
     * Adjusts warehouse stock if actual differs from planned
     * 
     * Authorization: WRITE_CLINICAL_RECORD
     */
    @Operation(summary = "API 8.8 - Update Procedure Materials",
               description = "Update actual material quantities used during procedure")
    @PutMapping("/{procedureId}/materials")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('WRITE_CLINICAL_RECORD')")
    public ResponseEntity<UpdateProcedureMaterialsResponse> updateProcedureMaterials(
            @PathVariable Integer procedureId,
            @Valid @RequestBody UpdateProcedureMaterialsRequest request) {
        
        log.info("PUT /api/v1/clinical-records/procedures/{}/materials", procedureId);

        // Verify procedure exists
        procedureRepository.findById(procedureId)
                .orElseThrow(() -> new NotFoundException("PROCEDURE_NOT_FOUND",
                        "Procedure not found: " + procedureId));

        // Convert request to service DTOs
        List<ProcedureMaterialService.MaterialQuantityUpdate> updates = request.getMaterials().stream()
                .map(m -> new ProcedureMaterialService.MaterialQuantityUpdate(
                        m.getUsageId(),
                        m.getActualQuantity(),
                        m.getVarianceReason(),
                        m.getNotes()))
                .collect(Collectors.toList());

        // Update materials
        List<ProcedureMaterialUsage> updatedUsages = 
            procedureMaterialService.updateMaterialQuantities(procedureId, updates);

        // Build stock adjustment summary
        List<UpdateProcedureMaterialsResponse.StockAdjustmentDTO> adjustments = new ArrayList<>();
        for (ProcedureMaterialUsage usage : updatedUsages) {
            double adjustment = usage.getVarianceQuantity() != null ? 
                usage.getVarianceQuantity().doubleValue() : 0.0;
            
            String reason = adjustment > 0 ? "Sử dụng thêm" : 
                           adjustment < 0 ? "Sử dụng ít hơn" : "Không thay đổi";
            
            adjustments.add(UpdateProcedureMaterialsResponse.StockAdjustmentDTO.builder()
                    .itemName(usage.getItemMaster().getItemName())
                    .adjustment(adjustment)
                    .reason(reason)
                    .build());
        }

        UpdateProcedureMaterialsResponse response = UpdateProcedureMaterialsResponse.builder()
                .message("Cập nhật số lượng vật tư thành công")
                .procedureId(procedureId)
                .materialsUpdated(updatedUsages.size())
                .stockAdjustments(adjustments)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Map ProcedureMaterialUsage entities to DTOs
     * 
     * @param usages List of material usage records
     * @param hasViewCostPermission Whether user has VIEW_WAREHOUSE_COST permission
     */
    private List<ProcedureMaterialsResponse.MaterialItemDTO> mapToMaterialDTOs(
            List<ProcedureMaterialUsage> usages, boolean hasViewCostPermission) {
        
        return usages.stream().map(usage -> {
            // Get current stock
            Integer currentStock = itemBatchRepository
                    .sumQuantityByItemMasterId(usage.getItemMaster().getItemMasterId());

            // Determine stock status
            String stockStatus = "OK";
            if (currentStock == null || currentStock == 0) {
                stockStatus = "OUT_OF_STOCK";
            } else if (usage.getItemMaster().getMinStockLevel() != null && 
                       currentStock <= usage.getItemMaster().getMinStockLevel()) {
                stockStatus = "LOW";
            }

            // Get price/cost data only if user has permission
            BigDecimal unitPrice = hasViewCostPermission ? 
                usage.getItemMaster().getCurrentMarketPrice() : null;
            
            BigDecimal totalPlannedCost = hasViewCostPermission && unitPrice != null ?
                usage.getPlannedQuantity().multiply(unitPrice) : null;
            
            BigDecimal totalActualCost = hasViewCostPermission && unitPrice != null ?
                usage.getActualQuantity().multiply(unitPrice) : null;

            return ProcedureMaterialsResponse.MaterialItemDTO.builder()
                    .usageId(usage.getUsageId())
                    .itemMasterId(usage.getItemMaster().getItemMasterId())
                    .itemCode(usage.getItemMaster().getItemCode())
                    .itemName(usage.getItemMaster().getItemName())
                    .categoryName(usage.getItemMaster().getCategory() != null ? 
                        usage.getItemMaster().getCategory().getCategoryName() : null)
                    .plannedQuantity(usage.getPlannedQuantity())
                    .actualQuantity(usage.getActualQuantity())
                    .varianceQuantity(usage.getVarianceQuantity())
                    .varianceReason(usage.getVarianceReason())
                    .unitName(usage.getUnit().getUnitName())
                    .unitPrice(unitPrice)              // null if no permission
                    .totalPlannedCost(totalPlannedCost) // null if no permission
                    .totalActualCost(totalActualCost)   // null if no permission
                    .stockStatus(stockStatus)
                    .currentStock(currentStock)
                    .recordedAt(usage.getRecordedAt())
                    .recordedBy(usage.getRecordedBy())
                    .notes(usage.getNotes())
                    .build();
        }).collect(Collectors.toList());
    }
}
