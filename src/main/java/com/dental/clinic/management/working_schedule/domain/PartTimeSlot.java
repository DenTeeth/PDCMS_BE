package com.dental.clinic.management.working_schedule.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a part-time slot that admin creates.
 * Defines clinic's needs (e.g., "Need 2 people for Morning shift on Friday &
 * Saturday").
 * 
 * NEW SPECIFICATION (Dynamic Quota):
 * - effectiveFrom/effectiveTo: Flexible date range (not fixed 3 months)
 * - dayOfWeek: Multiple days (e.g., FRIDAY, SATURDAY)
 * - quota: Number of people needed PER DAY
 * - Employees can register for flexible periods within the slot's effective
 * range
 */
@Entity
@Table(name = "part_time_slots")
public class PartTimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Long slotId;

    @Column(name = "work_shift_id", length = 20, nullable = false)
    private String workShiftId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_shift_id", insertable = false, updatable = false)
    private WorkShift workShift;

    /**
     * Multiple days of week this slot is available.
     * Example: ["FRIDAY", "SATURDAY"]
     * Stored as comma-separated string in DB: "FRIDAY,SATURDAY"
     */
    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek; // Will be migrated to support multiple values

    /**
     * Start date of slot availability.
     * Example: 2025-11-09
     */
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    /**
     * End date of slot availability.
     * Example: 2025-11-30
     */
    @Column(name = "effective_to", nullable = false)
    private LocalDate effectiveTo;

    /**
     * Number of people needed PER DAY for this slot.
     * Example: quota=2 means 2 people needed on EACH working day.
     */
    @Column(name = "quota", nullable = false)
    private Integer quota = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_time_slot_id", insertable = false, updatable = false)
    private List<PartTimeRegistration> registrations = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PartTimeSlot() {
    }

    public PartTimeSlot(Long slotId, String workShiftId, WorkShift workShift, String dayOfWeek,
            LocalDate effectiveFrom, LocalDate effectiveTo, Integer quota, Boolean isActive,
            List<PartTimeRegistration> registrations, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.slotId = slotId;
        this.workShiftId = workShiftId;
        this.workShift = workShift;
        this.dayOfWeek = dayOfWeek;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.quota = quota;
        this.isActive = isActive;
        this.registrations = registrations;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getWorkShiftId() {
        return workShiftId;
    }

    public void setWorkShiftId(String workShiftId) {
        this.workShiftId = workShiftId;
    }

    public WorkShift getWorkShift() {
        return workShift;
    }

    public void setWorkShift(WorkShift workShift) {
        this.workShift = workShift;
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

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<PartTimeRegistration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<PartTimeRegistration> registrations) {
        this.registrations = registrations;
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

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
