package com.dental.clinic.management.warehouse.enums;

/**
 * Enum for storage transaction types.
 *
 * IMPORT: Import goods to warehouse (nhập kho)
 * EXPORT: Export goods from warehouse (xuất kho)
 * ADJUSTMENT: Adjust stock quantity (Điều chỉnh tồn kho)
 * DESTROY: Destroy expired/damaged goods (Hủy hàng)
 */
public enum TransactionType {
    IMPORT, // Nhập kho
    EXPORT, // Xuất kho
    ADJUSTMENT, // Điều chỉnh tồn kho
    DESTROY // Hủy hàng
}
