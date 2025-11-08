package com.dental.clinic.management.working_schedule.dto.response;

import java.time.LocalDate;

/**
 * Response DTO for part-time registrations.
 * 
 * NEW SPECIFICATION: Includes approval workflow fields.
 */
public class RegistrationResponse {

    private Integer registrationId;
    private Integer employeeId;
    private Long partTimeSlotId;
    private String workShiftId;
    private String shiftName; // More intuitive field name
    private String dayOfWeek;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    /**
     * Registration status: PENDING, APPROVED, REJECTED
     */
    private String status;

    /**
     * The dates based on status:
     * - PENDING: Requested dates awaiting approval
     * - APPROVED: Accepted dates for work
     * - REJECTED: Dates that were rejected
     */
    private java.util.List<LocalDate> dates;

    /**
     * Rejection reason (only present if status = REJECTED)
     */
    private String reason;

    /**
     * Manager name who processed this registration
     */
    private String processedBy;

    /**
     * When the registration was processed
     */
    private String processedAt;

    /**
     * When the registration was created
     */
    private String createdAt;

    // Constructors
    public RegistrationResponse() {
    }

    public RegistrationResponse(Integer registrationId, Integer employeeId, Long partTimeSlotId,
            String workShiftId, String shiftName, String dayOfWeek, LocalDate effectiveFrom,
            LocalDate effectiveTo, String status, java.util.List<LocalDate> dates, String reason,
            String processedBy, String processedAt, String createdAt) {
        this.registrationId = registrationId;
        this.employeeId = employeeId;
        this.partTimeSlotId = partTimeSlotId;
        this.workShiftId = workShiftId;
        this.shiftName = shiftName;
        this.dayOfWeek = dayOfWeek;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.status = status;
        this.dates = dates;
        this.reason = reason;
        this.processedBy = processedBy;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Integer registrationId) {
        this.registrationId = registrationId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Long getPartTimeSlotId() {
        return partTimeSlotId;
    }

    public void setPartTimeSlotId(Long partTimeSlotId) {
        this.partTimeSlotId = partTimeSlotId;
    }

    public String getWorkShiftId() {
        return workShiftId;
    }

    public void setWorkShiftId(String workShiftId) {
        this.workShiftId = workShiftId;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.util.List<LocalDate> getDates() {
        return dates;
    }

    public void setDates(java.util.List<LocalDate> dates) {
        this.dates = dates;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
