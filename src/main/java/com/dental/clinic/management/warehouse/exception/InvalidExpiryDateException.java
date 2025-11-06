package com.dental.clinic.management.warehouse.exception;

import com.dental.clinic.management.warehouse.enums.WarehouseType;
import java.util.UUID;

/**
 * Exception thrown when COLD storage item is missing expiry date.
 */
public class InvalidExpiryDateException extends RuntimeException {

    public InvalidExpiryDateException(UUID itemMasterId, WarehouseType warehouseType) {
        super(String.format("Item %s requires expiry date for warehouse type: %s",
                itemMasterId, warehouseType));
    }

    public InvalidExpiryDateException(String message) {
        super(message);
    }
}
