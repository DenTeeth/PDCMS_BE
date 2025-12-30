package com.dental.clinic.management.payment.enums;

/**
 * Payment Transaction Status Enum
 * Trang thai giao dich thanh toan (dac biet cho PayOS)
 */
public enum PaymentTransactionStatus {
    /**
     * PENDING: Dang cho xu ly
     */
    PENDING,

    /**
     * SUCCESS: Thanh cong
     */
    SUCCESS,

    /**
     * FAILED: That bai
     */
    FAILED,

    /**
     * CANCELLED: Da huy
     */
    CANCELLED
}
