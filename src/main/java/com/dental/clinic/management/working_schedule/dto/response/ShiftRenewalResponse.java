package com.dental.clinic.management.working_schedule.dto.response;

import com.dental.clinic.management.working_schedule.enums.RenewalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for shift renewal request.
 * Used for both GET pending renewals and PATCH respond actions.
 */
public class ShiftRenewalResponse {
    private String renewalId;
    private Integer expiringRegistrationId;
    private Integer employeeId;
    private String employeeName;
    private RenewalStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;

    /**
     * Reason for declining (only set when status = DECLINED).
     */
    private String declineReason;

    // Additional information about the expiring FIXED registration
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String workShiftName; // e.g., "Ca sáng" (from work_shift table)
    private String shiftDetails; // e.g., "Monday, Wednesday (Ca sáng)"

    /**
     * Dynamic message for FE to display.
     * Format: "Hợp lệ/Đã lịch làm việc của bạn sẽ hết hạn vào
     * Định nghĩa [shiftName] của bạn sẽ hết hạn vào
     * ngày [date].
     * Bạn có muốn gia hạn thêm [1 tháng] không?"
     */
    private String message;

    public ShiftRenewalResponse() {
    }

    public ShiftRenewalResponse(String renewalId, Integer expiringRegistrationId, Integer employeeId,
            String employeeName, RenewalStatus status, LocalDateTime expiresAt,
            LocalDateTime confirmedAt, LocalDateTime createdAt, String declineReason,
            LocalDate effectiveFrom, LocalDate effectiveTo, String workShiftName,
            String shiftDetails, String message) {
        this.renewalId = renewalId;
        this.expiringRegistrationId = expiringRegistrationId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.status = status;
        this.expiresAt = expiresAt;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
        this.declineReason = declineReason;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.workShiftName = workShiftName;
        this.shiftDetails = shiftDetails;
        this.message = message;
    }

    public String getRenewalId() {
        return renewalId;
    }

    public void setRenewalId(String renewalId) {
        this.renewalId = renewalId;
    }

    public Integer getExpiringRegistrationId() {
        return expiringRegistrationId;
    }

    public void setExpiringRegistrationId(Integer expiringRegistrationId) {
        this.expiringRegistrationId = expiringRegistrationId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public RenewalStatus getStatus() {
        return status;
    }

    public void setStatus(RenewalStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getWorkShiftName() {
        return workShiftName;
    }

    public void setWorkShiftName(String workShiftName) {
        this.workShiftName = workShiftName;
    }

    public String getShiftDetails() {
        return shiftDetails;
    }

    public void setShiftDetails(String shiftDetails) {
        this.shiftDetails = shiftDetails;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
