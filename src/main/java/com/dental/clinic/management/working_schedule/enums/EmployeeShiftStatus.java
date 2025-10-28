package com.dental.clinic.management.working_schedule.enums;

/**
 * Enum representing the status of an employee shift.
 * Used to track shift lifecycle and attendance.
 */
public enum EmployeeShiftStatus {
    /**
     * Shift has been scheduled (initial state).
     * Đã được xếp lịch (trạng thái khởi tạo).
     */
    SCHEDULED,

    /**
     * Employee has completed the shift.
     * Nhân viên đã hoàn thành ca làm.
     */
    COMPLETED,

    /**
     * Employee was absent without permission.
     * Nhân viên vắng mặt không phép.
     */
    ABSENT,

    /**
     * Employee is on leave.
     * Nghỉ phép.
     */
    ON_LEAVE,

    /**
     * Shift was cancelled by management before it occurred.
     * Ca làm đã bị quản lý hủy trước khi diễn ra.
     */
    CANCELLED
}
