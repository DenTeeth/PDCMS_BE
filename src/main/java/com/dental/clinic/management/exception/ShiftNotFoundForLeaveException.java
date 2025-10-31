package com.dental.clinic.management.exception;

/**
 * Exception thrown when an employee tries to request time-off
 * but doesn't have a scheduled shift for that date/shift.
 * (V14 Hybrid Validation - P5.1)
 */
public class ShiftNotFoundForLeaveException extends RuntimeException {

    public ShiftNotFoundForLeaveException(String message) {
        super(message);
    }

    public ShiftNotFoundForLeaveException(Integer employeeId, String date, String workShiftId) {
        super(String.format(
                "Không thể xin nghỉ. Nhân viên %d không có lịch làm việc vào %s%s.",
                employeeId,
                date,
                workShiftId != null ? " ca " + workShiftId : ""));
    }
}
