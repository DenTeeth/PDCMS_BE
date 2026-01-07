package com.dental.clinic.management.payment.enums;

/**
 * Invoice Payment Status Enum
 * Trang thai thanh toan cua hoa don
 */
public enum InvoicePaymentStatus {
    /**
     * PENDING_PAYMENT: Chua thanh toan
     * paid_amount = 0, remaining_debt = total_amount
     */
    PENDING_PAYMENT,

    /**
     * PARTIAL_PAID: Da thanh toan mot phan
     * 0 < paid_amount < total_amount
     */
    PARTIAL_PAID,

    /**
     * PAID: Da thanh toan du
     * paid_amount = total_amount, remaining_debt = 0
     */
    PAID,

    /**
     * CANCELLED: Hoa don bi huy
     */
    CANCELLED
}
