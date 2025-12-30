package com.dental.clinic.management.payment.enums;

/**
 * Invoice Type Enum
 * Loai hoa don trong he thong
 */
public enum InvoiceType {
    /**
     * APPOINTMENT: Hoa don cho appointment dat le
     * Tao tu dong khi tao appointment
     */
    APPOINTMENT,

    /**
     * TREATMENT_PLAN: Hoa don cho ke hoach dieu tri
     * Tao tu dong khi tao treatment plan
     * Co the co nhieu hoa don tuy theo paymentType (FULL/PHASED/INSTALLMENT)
     */
    TREATMENT_PLAN,

    /**
     * SUPPLEMENTAL: Hoa don phat sinh
     * Tao khi bac si them dich vu ngoai ke hoach ban dau
     */
    SUPPLEMENTAL
}
