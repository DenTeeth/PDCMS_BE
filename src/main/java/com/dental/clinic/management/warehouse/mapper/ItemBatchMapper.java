package com.dental.clinic.management.warehouse.mapper;

import com.dental.clinic.management.warehouse.domain.ItemBatch;
import com.dental.clinic.management.warehouse.dto.response.ItemBatchResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Mapper for ItemBatch entity.
 */
@Component
public class ItemBatchMapper {

    /**
     * Convert ItemBatch to response with calculated fields.
     */
    public ItemBatchResponse toResponse(ItemBatch batch) {
        if (batch == null) {
            return null;
        }

        LocalDate now = LocalDate.now();
        Boolean isExpired = batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(now);

        // Expiring soon = within 30 days
        Boolean isExpiringSoon = batch.getExpiryDate() != null
                && batch.getExpiryDate().isAfter(now)
                && batch.getExpiryDate().isBefore(now.plusDays(30));

        // Calculate days until expiry
        Integer daysUntilExpiry = null;
        if (batch.getExpiryDate() != null && !isExpired) {
            daysUntilExpiry = (int) ChronoUnit.DAYS.between(now, batch.getExpiryDate());
        }

        // Calculate total value
        BigDecimal totalValue = batch.getQuantityOnHand() != null && batch.getImportPrice() != null
                ? batch.getImportPrice().multiply(BigDecimal.valueOf(batch.getQuantityOnHand()))
                : BigDecimal.ZERO;

        return ItemBatchResponse.builder()
                .id(batch.getId())
                .itemMasterId(batch.getItemMaster() != null ? batch.getItemMaster().getId() : null)
                .itemName(batch.getItemMaster() != null ? batch.getItemMaster().getItemName() : null)
                .lotNumber(batch.getLotNumber())
                .expiryDate(batch.getExpiryDate())
                .quantityOnHand(batch.getQuantityOnHand())
                .importPrice(batch.getImportPrice())
                .createdAt(batch.getCreatedAt())
                .isExpired(isExpired)
                .isExpiringSoon(isExpiringSoon)
                .daysUntilExpiry(daysUntilExpiry)
                .totalValue(totalValue)
                .build();
    }
}
