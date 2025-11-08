package com.dental.clinic.management.exception.fixed_registration;

/**
 * Exception thrown when attempting to assign fixed schedule to PART_TIME_FLEX
 * employee.
 */
public class InvalidEmployeeTypeException extends RuntimeException {

    public InvalidEmployeeTypeException() {
        super("Không thể gán lịch làm việc cho nhân viên này. Nhân viên này thuộc loại hình làm việc Linh hoạt (Flex).");
    }
}
