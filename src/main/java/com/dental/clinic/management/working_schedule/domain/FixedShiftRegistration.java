package com.dental.clinic.management.working_schedule.domain;

import com.dental.clinic.management.employee.domain.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a fixed shift registration for Full-Time and Part-Time
 * Fixed employees.
 * This is the template that defines which shifts an employee works on which
 * days of the week.
 *
 * Schema V14 - LuÃ¡Â»â€œng 1: LÃ¡Â»â€¹ch CÃ¡Â»â€˜ Ã„â€˜Ã¡Â»â€¹nh
 */
@Entity
@Table(name = "fixed_shift_registrations", indexes = {
        @Index(name = "idx_fixed_shift_employee", columnList = "employee_id, work_shift_id, is_active")
})
public class FixedShiftRegistration {

    /**
     * Primary key - Auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Integer registrationId;

    /**
     * The employee assigned to this fixed schedule.
     * Must be FULL_TIME or PART_TIME_FIXED (NOT PART_TIME_FLEX).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    /**
     * The work shift template (e.g., SANG_HC, CHIEU_HC).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_shift_id", nullable = false)
    @NotNull(message = "Work shift is required")
    private WorkShift workShift;

    /**
     * Start date when this registration becomes effective.
     */
    @Column(name = "effective_from", nullable = false)
    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    /**
     * End date when this registration expires.
     * NULL = permanent (for Full-Time employees).
     */
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    /**
     * Indicates if this registration is active.
     * Used for soft delete.
     */
    @Column(name = "is_active", nullable = false)
    @NotNull(message = "Active status is required")
    private Boolean isActive = true;

    /**
     * The days of the week when this shift applies.
     * OneToMany relationship with FixedRegistrationDay.
     */
    @OneToMany(mappedBy = "fixedShiftRegistration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FixedRegistrationDay> registrationDays = new ArrayList<>();

    /**
     * Timestamp when the registration was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the registration was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public FixedShiftRegistration() {
    }

    public FixedShiftRegistration(Integer registrationId, Employee employee, WorkShift workShift,
                                  LocalDate effectiveFrom, LocalDate effectiveTo, Boolean isActive,
                                  List<FixedRegistrationDay> registrationDays, LocalDateTime createdAt,
                                  LocalDateTime updatedAt) {
        this.registrationId = registrationId;
        this.employee = employee;
        this.workShift = workShift;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.isActive = isActive;
        this.registrationDays = registrationDays;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Integer registrationId) {
        this.registrationId = registrationId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public WorkShift getWorkShift() {
        return workShift;
    }

    public void setWorkShift(WorkShift workShift) {
        this.workShift = workShift;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<FixedRegistrationDay> getRegistrationDays() {
        return registrationDays;
    }

    public void setRegistrationDays(List<FixedRegistrationDay> registrationDays) {
        this.registrationDays = registrationDays;
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
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to add a day to the registration.
     */
    public void addDay(FixedRegistrationDay day) {
        registrationDays.add(day);
        day.setFixedShiftRegistration(this);
    }

    /**
     * Helper method to remove a day from the registration.
     */
    public void removeDay(FixedRegistrationDay day) {
        registrationDays.remove(day);
        day.setFixedShiftRegistration(null);
    }

    /**
     * Helper method to clear all days.
     */
    public void clearDays() {
        for (FixedRegistrationDay day : new ArrayList<>(registrationDays)) {
            removeDay(day);
        }
    }
}
