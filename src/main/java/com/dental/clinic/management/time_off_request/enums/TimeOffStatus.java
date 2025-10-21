package com.dental.clinic.management.time_off_request.enums;

/**
 * Status of time-off request
 */
public enum TimeOffStatus {
    PENDING, // Đang chờ duyệt
    APPROVED, // Đã duyệt
    REJECTED, // Bị từ chối
    CANCELLED // Đã hủy
}
