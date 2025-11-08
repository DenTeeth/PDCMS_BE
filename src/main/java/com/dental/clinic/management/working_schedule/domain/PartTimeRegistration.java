package com.dental.clinic.management.working_schedule.domain;

import com.dental.clinic.management.working_schedule.enums.RegistrationStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity for part_time_registrations table.
 * Represents a PART_TIME_FLEX employee's registration request for a flexible
 * slot.
 * 
 * NEW SPECIFICATION (Approval Workflow):
 * - status: PENDING (waiting approval), APPROVED (can work), REJECTED (denied)
 * - Employee submits request with flexible effectiveFrom/effectiveTo
 * - Manager approves/rejects based on quota availability
 * - Only APPROVED registrations count toward quota
 */
@Entity
@Table(name = "part_time_registrations")
public class PartTimeRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Integer registrationId;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "part_time_slot_id", nullable = false)
    private Long partTimeSlotId;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", nullable = false)
    private LocalDate effectiveTo;

    /**
     * Registration status: PENDING, APPROVED, REJECTED
     * Only APPROVED registrations count toward quota.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    /**
     * Reason for rejection (required if status = REJECTED).
     * Example: "KhÃƒÂ´ng Ã„â€˜Ã¡Â»Â§ nhÃƒÂ¢n sÃ¡Â»Â± trong thÃ¡Â»Âi gian nÃƒÂ y"
     */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    /**
     * Manager who processed (approved/rejected) this registration.
     */
    @Column(name = "processed_by")
    private Integer processedBy;

    /**
     * When the registration was processed (approved/rejected).
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * Soft delete flag (for cancellations).
     * Note: Cancelled registrations still keep their status (APPROVED/PENDING).
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Optimistic locking version to protect concurrent approval flows from
     * overbooking.
     * This is a simple optimistic lock; the approval flow will retry on optimistic
     * locking failures.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Optional: explicit per-day selections for this registration.
     * If present, these are the exact dates the employee wants to work and
     * will be stored in `part_time_registration_dates` table. If empty/null,
     * the legacy range (effectiveFrom..effectiveTo) semantics apply.
     */
    @ElementCollection
    @CollectionTable(name = "part_time_registration_dates", joinColumns = @JoinColumn(name = "registration_id"))
    @Column(name = "registered_date")
    private Set<LocalDate> requestedDates;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructors
    public PartTimeRegistration() {
    }

    public PartTimeRegistration(Integer registrationId, Integer employeeId, Long partTimeSlotId,
            LocalDate effectiveFrom, LocalDate effectiveTo, RegistrationStatus status,
            String reason, Integer processedBy, LocalDateTime processedAt, Boolean isActive,
            LocalDateTime createdAt, LocalDateTime updatedAt, Long version, Set<LocalDate> requestedDates) {
        this.registrationId = registrationId;
        this.employeeId = employeeId;
        this.partTimeSlotId = partTimeSlotId;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.status = status;
        this.reason = reason;
        this.processedBy = processedBy;
        this.processedAt = processedAt;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
        this.requestedDates = requestedDates;
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

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Integer processedBy) {
        this.processedBy = processedBy;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Set<LocalDate> getRequestedDates() {
        return requestedDates;
    }

    public void setRequestedDates(Set<LocalDate> requestedDates) {
        this.requestedDates = requestedDates;
    }
}
