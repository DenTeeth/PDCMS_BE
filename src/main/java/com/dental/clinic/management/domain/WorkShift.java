package com.dental.clinic.management.domain;

import com.dental.clinic.management.domain.enums.WorkShiftType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Work Shift entity - Fixed shifts for full-time employees.
 * Each clinic has 3 predefined shifts: MORNING, AFTERNOON, EVENING.
 * 
 * Business Rules:
 * - Duration: 3-8 hours (validated in service layer)
 * - Working hours: 08:00 - 21:00
 * - Each shift_type can only exist once (unique constraint)
 */
@Entity
@Table(name = "work_shifts")
public class WorkShift {

    @Id
    @Column(name = "shift_id", length = 36)
    private String shiftId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "shift_code", nullable = false, unique = true, length = 20)
    private String shiftCode; // SHIFT_MORNING, SHIFT_AFTERNOON, SHIFT_EVENING

    @NotBlank
    @Size(max = 50)
    @Column(name = "shift_name", nullable = false, length = 50)
    private String shiftName; // Ca sáng, Ca chiều, Ca tối

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false, unique = true)
    private WorkShiftType shiftType;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public WorkShift() {
    }

    public WorkShift(String shiftCode, String shiftName, WorkShiftType shiftType,
                     LocalTime startTime, LocalTime endTime) {
        this.shiftCode = shiftCode;
        this.shiftName = shiftName;
        this.shiftType = shiftType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (shiftId == null) {
            shiftId = java.util.UUID.randomUUID().toString();
        }
    }

    // Getters and Setters
    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getShiftCode() {
        return shiftCode;
    }

    public void setShiftCode(String shiftCode) {
        this.shiftCode = shiftCode;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
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
}
