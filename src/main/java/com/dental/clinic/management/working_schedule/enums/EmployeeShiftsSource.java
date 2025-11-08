package com.dental.clinic.management.working_schedule.enums;

/**
 * Enum representing the source/origin of an employee shift assignment.
 */
public enum EmployeeShiftsSource {
    /**
     * Created automatically by batch job for full-time employees.
     * Tạo tự động bởi job tự động cho Full-time.
     */
    BATCH_JOB,

    /**
     * Created automatically by registration job for part-time employees.
     * Tạo tự động bởi job tự động cho Part-time.
     */
    REGISTRATION_JOB,

    /**
     * Created from overtime approval.
     * Tạo từ việc duyệt OT.
     */
    OT_APPROVAL,

    /**
     * Created manually by manager/admin.
     * Do quản lý/admin tạo thủ công.
     */
    MANUAL_ENTRY
}
