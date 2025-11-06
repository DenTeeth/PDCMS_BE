package com.dental.clinic.management.warehouse.mapper;

import com.dental.clinic.management.warehouse.domain.StorageTransaction;
import com.dental.clinic.management.warehouse.dto.response.StorageTransactionResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for StorageTransaction entity.
 */
@Component
public class StorageTransactionMapper {

    /**
     * Convert StorageTransaction to response.
     */
    public StorageTransactionResponse toResponse(StorageTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        return StorageTransactionResponse.builder()
                .id(transaction.getId())
                .batchId(transaction.getBatch() != null ? transaction.getBatch().getId() : null)
                .itemName(transaction.getBatch() != null && transaction.getBatch().getItemMaster() != null
                        ? transaction.getBatch().getItemMaster().getItemName()
                        : null)
                .lotNumber(transaction.getBatch() != null ? transaction.getBatch().getLotNumber() : null)
                .transactionType(transaction.getTransactionType())
                .quantity(transaction.getQuantity())
                .unitPrice(transaction.getUnitPrice())
                .totalValue(transaction.getTotalValue())
                .transactionDate(transaction.getTransactionDate())
                .performedBy(transaction.getPerformedBy())
                .performedByName(null) // Will be populated by service layer with employee name
                .notes(transaction.getNotes())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

