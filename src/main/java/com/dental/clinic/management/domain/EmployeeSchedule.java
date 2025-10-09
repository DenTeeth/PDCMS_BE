package com.dental.clinic.management.domain;

import com.dental.clinic.management.domain.enums.EmployeeScheduleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Employee Schedule entity - Attendance tracking for all employees.
 * 
 * Business Rules:
 * - Auto-generated from recurring_schedules for FULL_TIME employees
 * - Manually created from dentist_work_schedules for PART_TIME dentists
 * - Used for attendance tracking and payroll calculation
 * - Status updated by attendance system or HR
 * 
 * Status Flow:
 * SCHEDULED (initial) → PRESENT (checked in on time)
 * SCHEDULED → LATE (checked in late)
 * SCHEDULED → ABSENT (no check-in)
 * SCHEDULED → ON_LEAVE (pre-approved absence)
 */
@Entity
@Table(name = "employee_schedules", indexes = {
    @Index(name = "idx_schedule_code", columnList = "schedule_code", unique = true),
    @Index(name = "idx_employee_date_status", columnList = "employee_id, work_date, status"),
    @Index(name = "idx_date_status", columnList = "work_date, status")
})
public class EmployeeSchedule {

    @Id
    @Column(name = "schedule_id", length = 36)
    private String scheduleId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "schedule_code", nullable = false, unique = true, length = 20)
    private String scheduleCode; // EMP_SCH_YYYYMMDD_SEQ (e.g., EMP_SCH_20251015_001)

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
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EmployeeScheduleStatus status = EmployeeScheduleStatus.SCHEDULED;

    /**
     * Actual check-in time (for attendance tracking).
     * Null if employee hasn't checked in yet.
     */
    @Column(name = "actual_start_time")
    private LocalTime actualStartTime;

    /**
     * Actual check-out time (for overtime calculation).
     * Null if employee hasn't checked out yet.
     */
    @Column(name = "actual_end_time")
    private LocalTime actualEndTime;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Reference to recurring_schedules.recurring_id if auto-generated.
     * Null for one-time schedules.
     */
    @Column(name = "recurring_id", length = 36)
    private String recurringId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_id", insertable = false, updatable = false)
    private RecurringSchedule recurringSchedule;

    /**
     * Reference to dentist_work_schedules.schedule_id for part-time dentists.
     * Null for full-time employees.
     */
    @Column(name = "dentist_schedule_id", length = 36)
    private String dentistScheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_schedule_id", insertable = false, updatable = false)
    private DentistWorkSchedule dentistWorkSchedule;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public EmployeeSchedule() {
    }

    public EmployeeSchedule(String scheduleCode, String employeeId, LocalDate workDate,
                            LocalTime startTime, LocalTime endTime) {
        this.scheduleCode = scheduleCode;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (scheduleId == null) {
            scheduleId = java.util.UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleCode() {
        return scheduleCode;
    }

    public void setScheduleCode(String scheduleCode) {
        this.scheduleCode = scheduleCode;
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

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
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

    public EmployeeScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeScheduleStatus status) {
        this.status = status;
    }

    public LocalTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRecurringId() {
        return recurringId;
    }

    public void setRecurringId(String recurringId) {
        this.recurringId = recurringId;
    }

    public RecurringSchedule getRecurringSchedule() {
        return recurringSchedule;
    }

    public void setRecurringSchedule(RecurringSchedule recurringSchedule) {
        this.recurringSchedule = recurringSchedule;
    }

    public String getDentistScheduleId() {
        return dentistScheduleId;
    }

    public void setDentistScheduleId(String dentistScheduleId) {
        this.dentistScheduleId = dentistScheduleId;
    }

    public DentistWorkSchedule getDentistWorkSchedule() {
        return dentistWorkSchedule;
    }

    public void setDentistWorkSchedule(DentistWorkSchedule dentistWorkSchedule) {
        this.dentistWorkSchedule = dentistWorkSchedule;
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
