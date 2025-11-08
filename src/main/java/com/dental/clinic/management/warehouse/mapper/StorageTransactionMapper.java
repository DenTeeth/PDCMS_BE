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

        return new StorageTransactionResponse(
                transaction.getId(),
                transaction.getBatch() != null ? transaction.getBatch().getId() : null,
                transaction.getBatch() != null && transaction.getBatch().getItemMaster() != null
                        ? transaction.getBatch().getItemMaster().getItemName()
                        : null,
                transaction.getBatch() != null ? transaction.getBatch().getLotNumber() : null,
                transaction.getTransactionType(),
                transaction.getQuantity(),
                transaction.getUnitPrice(),
                transaction.getTotalValue(),
                transaction.getTransactionDate(),
                transaction.getPerformedBy(),
                null, // Will be populated by service layer with employee name
                transaction.getNotes(),
                transaction.getCreatedAt()
        );
    }
}

