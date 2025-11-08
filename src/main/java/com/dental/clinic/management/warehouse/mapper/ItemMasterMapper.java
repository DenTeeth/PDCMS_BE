package com.dental.clinic.management.warehouse.mapper;

import com.dental.clinic.management.warehouse.domain.ItemMaster;
import com.dental.clinic.management.warehouse.dto.request.CreateItemMasterRequest;
import com.dental.clinic.management.warehouse.dto.request.UpdateItemMasterRequest;
import com.dental.clinic.management.warehouse.dto.response.ItemMasterSummaryResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Mapper for ItemMaster entity.
 */
@Component
public class ItemMasterMapper {

    /**
     * Convert CreateItemMasterRequest to ItemMaster entity.
     */
    public ItemMaster toEntity(CreateItemMasterRequest request) {
        if (request == null) {
            return null;
        }

        ItemMaster itemMaster = new ItemMaster();
        itemMaster.setItemName(request.getItemName());
        itemMaster.setDescription(request.getDescription());
        itemMaster.setMinStockLevel(request.getMinStockLevel());
        itemMaster.setMaxStockLevel(request.getMaxStockLevel());

        return itemMaster;
    }

    /**
     * Update entity from UpdateItemMasterRequest (only non-null fields).
     */
    public void updateEntity(ItemMaster entity, UpdateItemMasterRequest request) {
        if (request.getItemName() != null) {
            entity.setItemName(request.getItemName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getMinStockLevel() != null) {
            entity.setMinStockLevel(request.getMinStockLevel());
        }
        if (request.getMaxStockLevel() != null) {
            entity.setMaxStockLevel(request.getMaxStockLevel());
        }
    }

    /**
     * Convert ItemMaster to summary response with calculated fields.
     */
    public ItemMasterSummaryResponse toSummaryResponse(ItemMaster itemMaster) {
        if (itemMaster == null) {
            return null;
        }

        // Calculate total quantity on hand from all batches
        Integer totalQuantity = itemMaster.getBatches() != null
                ? itemMaster.getBatches().stream()
                        .mapToInt(batch -> batch.getQuantityOnHand() != null ? batch.getQuantityOnHand() : 0)
                        .sum()
                : 0;

        // Calculate stock status
        String stockStatus = calculateStockStatus(totalQuantity,
                itemMaster.getMinStockLevel(),
                itemMaster.getMaxStockLevel());

        // Count active batches (not expired)
        Integer activeBatchCount = itemMaster.getBatches() != null
                ? (int) itemMaster.getBatches().stream()
                        .filter(batch -> batch.getExpiryDate() == null
                                || !batch.getExpiryDate().isBefore(LocalDate.now()))
                        .count()
                : 0;

        // Count batches expiring soon (within 30 days)
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        Integer expiringSoonCount = itemMaster.getBatches() != null
                ? (int) itemMaster.getBatches().stream()
                        .filter(batch -> batch.getExpiryDate() != null
                                && batch.getExpiryDate().isAfter(LocalDate.now())
                                && batch.getExpiryDate().isBefore(thirtyDaysFromNow))
                        .count()
                : 0;

        return new ItemMasterSummaryResponse(
                itemMaster.getId(),
                itemMaster.getItemName(),
                itemMaster.getDescription(),
                itemMaster.getCategory() != null ? itemMaster.getCategory().getId() : null,
                itemMaster.getCategory() != null ? itemMaster.getCategory().getCategoryName() : null,
                itemMaster.getMinStockLevel(),
                itemMaster.getMaxStockLevel(),
                totalQuantity,
                stockStatus,
                activeBatchCount,
                expiringSoonCount
        );
    }

    /**
     * Calculate stock status based on quantity and thresholds.
     */
    private String calculateStockStatus(Integer quantity, Integer minLevel, Integer maxLevel) {
        if (quantity == null || quantity == 0) {
            return "OUT_OF_STOCK";
        }
        if (minLevel != null && quantity < minLevel) {
            return "LOW_STOCK";
        }
        if (maxLevel != null && quantity > maxLevel) {
            return "OVERSTOCK";
        }
        return "NORMAL";
    }
}

