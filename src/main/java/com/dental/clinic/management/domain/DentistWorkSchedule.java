package com.dental.clinic.management.domain;

import com.dental.clinic.management.domain.enums.DentistWorkScheduleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Dentist Work Schedule entity - Part-time dentists register flexible
 * schedules.
 *
 * Business Rules:
 * - Only PART_TIME dentists can register
 * - Duration: minimum 2 hours, recommended 3-4 hours
 * - Working hours: 08:00 - 21:00
 * - Max 2 schedules per day
 * - Must register at least 24 hours in advance
 * - Payment commitment: Dentist paid hourly even without patients
 *
 * Status Flow:
 * AVAILABLE → BOOKED (when appointment created)
 * AVAILABLE → CANCELLED (dentist cancels)
 * AVAILABLE/BOOKED → EXPIRED (after work_date)
 */
@Entity
@Table(name = "dentist_work_schedules", indexes = {
        @Index(name = "idx_schedule_code", columnList = "schedule_code", unique = true),
        @Index(name = "idx_dentist_date_status", columnList = "dentist_id, work_date, status"),
        @Index(name = "idx_status_date", columnList = "status, work_date")
})
public class DentistWorkSchedule {

    @Id
    @Column(name = "schedule_id", length = 36)
    private String scheduleId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "schedule_code", nullable = false, unique = true, length = 20)
    private String scheduleCode; // SCH_YYYYMMDD_SEQ (e.g., SCH_20251015_001)

    @NotNull
    @Column(name = "dentist_id", nullable = false, length = 36)
    private String dentistId;

    /**
     * Read-only relationship to Employee.
     * Use dentistId field for updates to avoid duplication.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", insertable = false, updatable = false)
    private Employee dentist;

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
    private DentistWorkScheduleStatus status = DentistWorkScheduleStatus.AVAILABLE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public DentistWorkSchedule() {
    }

    public DentistWorkSchedule(String scheduleCode, String dentistId, LocalDate workDate,
            LocalTime startTime, LocalTime endTime) {
        this.scheduleCode = scheduleCode;
        this.dentistId = dentistId;
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

    public String getDentistId() {
        return dentistId;
    }

    public void setDentistId(String dentistId) {
        this.dentistId = dentistId;
    }

    public Employee getDentist() {
        return dentist;
    }

    public void setDentist(Employee dentist) {
        this.dentist = dentist;
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

    public DentistWorkScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(DentistWorkScheduleStatus status) {
        this.status = status;
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
