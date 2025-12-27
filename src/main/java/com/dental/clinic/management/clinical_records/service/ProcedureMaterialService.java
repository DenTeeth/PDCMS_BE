package com.dental.clinic.management.clinical_records.service;

import com.dental.clinic.management.clinical_records.domain.ClinicalRecordProcedure;
import com.dental.clinic.management.clinical_records.domain.ProcedureMaterialUsage;
import com.dental.clinic.management.clinical_records.repository.ClinicalRecordProcedureRepository;
import com.dental.clinic.management.clinical_records.repository.ProcedureMaterialUsageRepository;
import com.dental.clinic.management.warehouse.domain.ItemBatch;
import com.dental.clinic.management.warehouse.domain.ServiceConsumable;
import com.dental.clinic.management.warehouse.repository.ItemBatchRepository;
import com.dental.clinic.management.warehouse.repository.ServiceConsumableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to handle material deduction for clinical procedures
 * 
 * Integration Flow:
 * 1. Procedure is performed → Get BOM from service_consumables
 * 2. Deduct materials from warehouse using FEFO
 * 3. Record actual usage in procedure_material_usage
 * 4. Create audit trail in storage_transactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcedureMaterialService {

    private final ClinicalRecordProcedureRepository procedureRepository;
    private final ServiceConsumableRepository serviceConsumableRepository;
    private final ItemBatchRepository itemBatchRepository;
    private final ProcedureMaterialUsageRepository materialUsageRepository;

    /**
     * Deduct materials for a procedure based on service BOM
     * 
     * Process:
     * 1. Get BOM from service_consumables table
     * 2. For each material:
     *    a) Use planned quantity from BOM as default editable quantity
     *    b) Deduct from warehouse using FEFO
     *    c) Create procedure_material_usage record with editable quantity
     * 3. Update procedure.materials_deducted_at
     * 
     * @param procedureId The procedure to deduct materials for
     * @return List of material usage records created
     */
    @Transactional
    public List<ProcedureMaterialUsage> deductMaterialsForProcedure(Integer procedureId) {
        log.info("Starting material deduction for procedure {}", procedureId);

        // Step 1: Get procedure
        ClinicalRecordProcedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new IllegalArgumentException("Procedure not found: " + procedureId));

        // Check if already deducted
        if (procedure.getMaterialsDeductedAt() != null) {
            log.warn("Materials already deducted for procedure {} at {}", 
                procedureId, procedure.getMaterialsDeductedAt());
            return materialUsageRepository.findByProcedure_ProcedureId(procedureId);
        }

        // Step 2: Get service BOM
        Long serviceId = procedure.getService().getServiceId();
        List<ServiceConsumable> bom = serviceConsumableRepository.findByServiceIdWithDetails(serviceId);

        if (bom.isEmpty()) {
            log.info("No BOM defined for service {}, skipping deduction", serviceId);
            return new ArrayList<>();
        }

        // Step 3: Get current user
        String username = getCurrentUsername();

        // Step 4: Deduct each material
        List<ProcedureMaterialUsage> usageRecords = new ArrayList<>();

        for (ServiceConsumable bomItem : bom) {
            // Use planned quantity from BOM as default editable quantity
            BigDecimal plannedQty = bomItem.getQuantityPerService();

            // Deduct from warehouse using FEFO
            deductFromWarehouse(bomItem.getItemMaster().getItemMasterId(), plannedQty);

            // Create usage record with editable quantity field
            ProcedureMaterialUsage usage = ProcedureMaterialUsage.builder()
                .procedure(procedure)
                .itemMaster(bomItem.getItemMaster())
                .plannedQuantity(plannedQty)      // Base quantity from BOM
                .quantity(plannedQty)             // Editable quantity (defaults to planned)
                .actualQuantity(plannedQty)       // Actual quantity (defaults to quantity)
                .unit(bomItem.getUnit())
                .recordedAt(LocalDateTime.now())
                .recordedBy(username)
                .build();

            usageRecords.add(materialUsageRepository.save(usage));

            log.debug("Deducted {} {} of {} for procedure {}", 
                plannedQty, bomItem.getUnit().getUnitName(),
                bomItem.getItemMaster().getItemName(), procedureId);
        }

        // Step 5: Mark procedure as deducted
        procedure.setMaterialsDeductedAt(LocalDateTime.now());
        procedure.setMaterialsDeductedBy(username);
        procedureRepository.save(procedure);

        log.info("Material deduction completed for procedure {}. {} items deducted", 
            procedureId, usageRecords.size());

        return usageRecords;
    }

    /**
     * Update editable quantities before deduction
     * Allows users to customize material quantities for this specific procedure
     * 
     * @param usageId Material usage record ID
     * @param newQuantity New quantity to use for deduction
     */
    @Transactional
    public ProcedureMaterialUsage updateEditableQuantity(Long usageId, BigDecimal newQuantity) {
        ProcedureMaterialUsage usage = materialUsageRepository.findById(usageId)
                .orElseThrow(() -> new IllegalArgumentException("Usage record not found: " + usageId));

        // Can only update if materials not yet deducted
        if (usage.getProcedure().getMaterialsDeductedAt() != null) {
            throw new IllegalStateException("Cannot update quantity after materials have been deducted");
        }

        usage.setQuantity(newQuantity);
        usage.setActualQuantity(newQuantity); // Update actual to match
        usage.setRecordedAt(LocalDateTime.now());
        usage.setRecordedBy(getCurrentUsername());

        return materialUsageRepository.save(usage);
    }

    /**
     * Deduct quantity from warehouse using FEFO (First Expired First Out)
     * 
     * @param itemMasterId Item to deduct
     * @param quantity Quantity to deduct
     */
    private void deductFromWarehouse(Long itemMasterId, BigDecimal quantity) {
        // Get batches sorted by expiry date (FEFO)
        List<ItemBatch> batches = itemBatchRepository.findByItemMasterIdFEFO(itemMasterId);

        if (batches.isEmpty()) {
            throw new IllegalStateException(
                "No stock available for item master ID: " + itemMasterId);
        }

        int remainingToDeduct = quantity.intValue();

        for (ItemBatch batch : batches) {
            if (remainingToDeduct <= 0) {
                break;
            }

            int availableInBatch = batch.getQuantityOnHand() != null ? 
                batch.getQuantityOnHand() : 0;

            if (availableInBatch <= 0) {
                continue;
            }

            int deductFromThisBatch = Math.min(remainingToDeduct, availableInBatch);

            // Update batch quantity
            batch.setQuantityOnHand(availableInBatch - deductFromThisBatch);
            itemBatchRepository.save(batch);

            remainingToDeduct -= deductFromThisBatch;

            log.debug("Deducted {} from batch {} (Lot: {}). Remaining: {}", 
                deductFromThisBatch, batch.getBatchId(), 
                batch.getLotNumber(), batch.getQuantityOnHand());
        }

        if (remainingToDeduct > 0) {
            throw new IllegalStateException(
                String.format("Insufficient stock for item %d. Needed: %d, Available: %d", 
                    itemMasterId, quantity.intValue(), 
                    quantity.intValue() - remainingToDeduct));
        }
    }

    /**
     * Update actual quantity used (called by assistant/nurse)
     * 
     * @param usageId Material usage record ID
     * @param actualQuantity New actual quantity
     * @param varianceReason Reason for variance
     */
    @Transactional
    public ProcedureMaterialUsage updateActualQuantity(
            Long usageId, 
            BigDecimal actualQuantity,
            String varianceReason) {
        
        ProcedureMaterialUsage usage = materialUsageRepository.findById(usageId)
                .orElseThrow(() -> new IllegalArgumentException("Usage record not found: " + usageId));

        BigDecimal oldActual = usage.getActualQuantity();
        BigDecimal difference = actualQuantity.subtract(oldActual);

        // If actual increased, deduct more from warehouse
        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            deductFromWarehouse(usage.getItemMaster().getItemMasterId(), difference);
        }
        // If actual decreased, we could reverse deduction (add back to stock)
        // For now, we just log it - reversing is complex with FEFO
        else if (difference.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Actual quantity decreased for usage {}. Original: {}, New: {}. " +
                "Stock NOT reversed - manual adjustment needed", 
                usageId, oldActual, actualQuantity);
        }

        // Update usage record
        usage.setActualQuantity(actualQuantity);
        usage.setVarianceReason(varianceReason);
        usage.setRecordedAt(LocalDateTime.now());
        usage.setRecordedBy(getCurrentUsername());

        return materialUsageRepository.save(usage);
    }

    /**
     * Get all material usage for a procedure
     */
    @Transactional(readOnly = true)
    public List<ProcedureMaterialUsage> getMaterialUsage(Integer procedureId) {
        return materialUsageRepository.findByProcedure_ProcedureId(procedureId);
    }

    /**
     * Update multiple material quantities at once (bulk update)
     * Used by assistants to adjust all materials after procedure
     * 
     * @param procedureId Procedure ID
     * @param updates List of material quantity updates
     * @return List of updated usage records
     */
    @Transactional
    public List<ProcedureMaterialUsage> updateMaterialQuantities(
            Integer procedureId,
            List<MaterialQuantityUpdate> updates) {
        
        List<ProcedureMaterialUsage> updatedRecords = new ArrayList<>();
        
        for (MaterialQuantityUpdate update : updates) {
            ProcedureMaterialUsage updated = updateActualQuantity(
                update.getUsageId(),
                update.getActualQuantity(),
                update.getVarianceReason()
            );
            
            // Update notes if provided
            if (update.getNotes() != null) {
                updated.setNotes(update.getNotes());
                updated = materialUsageRepository.save(updated);
            }
            
            updatedRecords.add(updated);
        }
        
        return updatedRecords;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }

    /**
     * DTO for bulk material quantity updates
     */
    public static class MaterialQuantityUpdate {
        private Long usageId;
        private BigDecimal actualQuantity;
        private String varianceReason;
        private String notes;

        public MaterialQuantityUpdate(Long usageId, BigDecimal actualQuantity, 
                                     String varianceReason, String notes) {
            this.usageId = usageId;
            this.actualQuantity = actualQuantity;
            this.varianceReason = varianceReason;
            this.notes = notes;
        }

        public Long getUsageId() { return usageId; }
        public BigDecimal getActualQuantity() { return actualQuantity; }
        public String getVarianceReason() { return varianceReason; }
        public String getNotes() { return notes; }
    }
}
