package com.dental.clinic.management.working_schedule.enums;

/**
 * Enum representing the source/origin of an employee shift assignment.
 * Used to track how shifts were created in the system.
 */
public enum ShiftSource {
    /**
     * Created manually by manager/admin.
     * Do quản lý/admin tạo thủ công.
     */
    MANUAL_ENTRY,

    /**
     * Created automatically by batch job from employee's default contract.
     * Từ job tự động tạo theo hợp đồng mặc định của nhân viên.
     */
    BATCH_JOB,

    /**
     * Created from overtime approval.
     * Từ việc duyệt OT.
     */
    OVERTIME_REQUEST
}
