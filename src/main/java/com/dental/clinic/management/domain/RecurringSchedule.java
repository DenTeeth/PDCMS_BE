package com.dental.clinic.management.domain;

import com.dental.clinic.management.domain.enums.DayOfWeek;
import com.dental.clinic.management.domain.enums.WorkShiftType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Recurring Schedule entity - Repeating weekly work patterns for full-time employees.
 * 
 * Business Rules:
 * - Only FULL_TIME employees can have recurring schedules
 * - Active schedules auto-generate employee_schedules records
 * - Used for payroll calculations and attendance tracking
 * - Can be temporarily disabled without deletion
 * 
 * Example: Doctor works MORNING shift every MONDAY and WEDNESDAY
 */
@Entity
@Table(name = "recurring_schedules", indexes = {
    @Index(name = "idx_recurring_code", columnList = "recurring_code", unique = true),
    @Index(name = "idx_employee_active", columnList = "employee_id, is_active"),
    @Index(name = "idx_day_shift", columnList = "day_of_week, shift_type")
})
public class RecurringSchedule {

    @Id
    @Column(name = "recurring_id", length = 36)
    private String recurringId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "recurring_code", nullable = false, unique = true, length = 20)
    private String recurringCode; // REC_YYYYMMDD_SEQ (e.g., REC_20251015_001)

    @NotNull
    @Column(name = "employee_id", nullable = false, length = 36)
    private String employeeId;

    /**
     * Read-only relationship to Employee.
     * Use employeeId field for updates to avoid duplication.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "shift_id", length = 36)
    private String shiftId;

    /**
     * Optional reference to predefined WorkShift.
     * If null, use custom start_time/end_time.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", insertable = false, updatable = false)
    private WorkShift workShift;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type")
    private WorkShiftType shiftType;

    /**
     * Custom start time if not using predefined shift.
     * Null if shiftId is set.
     */
    @Column(name = "start_time")
    private LocalTime startTime;

    /**
     * Custom end time if not using predefined shift.
     * Null if shiftId is set.
     */
    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public RecurringSchedule() {
    }

    public RecurringSchedule(String recurringCode, String employeeId, DayOfWeek dayOfWeek, String shiftId) {
        this.recurringCode = recurringCode;
        this.employeeId = employeeId;
        this.dayOfWeek = dayOfWeek;
        this.shiftId = shiftId;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (recurringId == null) {
            recurringId = java.util.UUID.randomUUID().toString();
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getRecurringId() {
        return recurringId;
    }

    public void setRecurringId(String recurringId) {
        this.recurringId = recurringId;
    }

    public String getRecurringCode() {
        return recurringCode;
    }

    public void setRecurringCode(String recurringCode) {
        this.recurringCode = recurringCode;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public WorkShift getWorkShift() {
        return workShift;
    }

    public void setWorkShift(WorkShift workShift) {
        this.workShift = workShift;
    }

    public WorkShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(WorkShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
