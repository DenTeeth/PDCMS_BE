package com.dental.clinic.management.payment.enums;

/**
 * Invoice Type Enum
 * Loại hóa đơn trong hệ thống
 *
 * NOTE: Invoice KHÔNG được tự động tạo. Admin/Receptionist phải tạo thủ công
 * qua API.
 * Reference: INVOICE_MODULE_RESPONSE_TO_FE_TEAM.md (Issue #4)
 */
public enum InvoiceType {
    /**
     * APPOINTMENT: Hóa đơn cho appointment đặt lẻ (khám, điều trị đơn lẻ)
     *
     * Workflow:
     * 1. Admin/Receptionist tạo appointment
     * 2. Appointment được hoàn thành (COMPLETED status)
     * 3. Admin/Receptionist TẠO THỦ CÔNG invoice qua API: POST /api/v1/invoices
     *
     * NOTE: KHÔNG tự động tạo khi tạo appointment (manual creation only)
     */
    APPOINTMENT,

    /**
     * TREATMENT_PLAN: Hóa đơn cho kế hoạch điều trị dài hạn
     *
     * Workflow depends on paymentType:
     *
     * 1. FULL Payment:
     * - Admin tạo 1 invoice duy nhất cho toàn bộ treatment plan
     * - Tạo thủ công khi patient đồng ý plan
     *
     * 2. PHASED Payment:
     * - Admin tạo nhiều invoices, mỗi invoice cho 1 phase
     * - Tạo thủ công khi phase bắt đầu hoặc hoàn thành
     * - Fields: phaseNumber = 1, 2, 3, ..., installmentNumber = null
     *
     * 3. INSTALLMENT Payment:
     * - Admin tạo nhiều invoices theo lịch thanh toán
     * - Tạo thủ công khi đến kỳ thanh toán
     * - Fields: installmentNumber = 1, 2, 3, ..., phaseNumber = null
     *
     * NOTE: KHÔNG tự động tạo khi tạo treatment plan (manual creation only)
     */
    TREATMENT_PLAN,

    /**
     * SUPPLEMENTAL: Hóa đơn phát sinh (deprecated - replaced by DIRECT)
     *
     * Tạo khi bác sĩ thêm dịch vụ ngoài kế hoạch ban đầu
     *
     * @deprecated Use DIRECT instead for direct sales (medicine, services without
     *             appointment)
     */
    @Deprecated
    SUPPLEMENTAL
}
