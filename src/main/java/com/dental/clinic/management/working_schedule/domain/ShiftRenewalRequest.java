package com.dental.clinic.management.working_schedule.domain;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.utils.IdGenerator;
import com.dental.clinic.management.working_schedule.enums.RenewalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a shift renewal request for part-time employees.
 *
 * When a part-time employee's shift registration is about to expire,
 * the system automatically creates a renewal request inviting them to extend.
 *
 * ID Format: SRRYYMMDDSSS (e.g., SRR251022001)
 * - SRR: Shift Renewal Request prefix (3 chars)
 * - YYMMDD: Date (6 digits) - 251022 = Oct 22, 2025
 * - SSS: Daily sequence (001-999)
 */
@Entity
@Table(name = "shift_renewal_requests", indexes = {
        @Index(name = "idx_renewal_employee", columnList = "employee_id"),
        @Index(name = "idx_renewal_status", columnList = "status"),
        @Index(name = "idx_renewal_expires_at", columnList = "expires_at"),
        @Index(name = "idx_renewal_expiring_registration", columnList = "expiring_registration_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRenewalRequest {

    private static final String ID_PREFIX = "SRR";
    private static IdGenerator idGenerator;

    /**
     * Set the IdGenerator for this entity (called by EntityIdGeneratorConfig).
     */
    public static void setIdGenerator(IdGenerator generator) {
        idGenerator = generator;
    }

    @Id
    @Column(name = "renewal_id", length = 12)
    @NotBlank(message = "Renewal ID is required")
    @Size(max = 12, message = "Renewal ID must not exceed 12 characters")
    private String renewalId; // Format: SRRYYMMDDSSS (e.g., SRR251022001)

    /**
     * Reference to the original employee_shift_registration that is expiring.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expiring_registration_id", nullable = false)
    @NotNull(message = "Expiring registration is required")
    private EmployeeShiftRegistration expiringRegistration;

    /**
     * The part-time employee who needs to respond to this renewal.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    /**
     * Current status of the renewal request.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    private RenewalStatus status = RenewalStatus.PENDING_ACTION;

    /**
     * Message/invitation text for the employee.
     */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Message is required")
    private String message;

    /**
     * Timestamp when this renewal invitation expires.
     * After this time, employee can no longer respond.
     */
    @Column(name = "expires_at", nullable = false)
    @NotNull(message = "Expiry time is required")
    private LocalDateTime expiresAt;

    /**
     * Timestamp when employee confirmed or declined the renewal.
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * Timestamp when the renewal request was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the renewal request was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Pre-persist hook to set timestamps and generate ID.
     */
    @PrePersist
    protected void onCreate() {
        if (renewalId == null && idGenerator != null) {
            renewalId = idGenerator.generateId(ID_PREFIX);
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = RenewalStatus.PENDING_ACTION;
        }
    }

    /**
     * Pre-update hook to set the update timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if the renewal is still pending.
     * 
     * @return true if status is PENDING_ACTION
     */
    @Transient
    public boolean isPending() {
        return status == RenewalStatus.PENDING_ACTION;
    }

    /**
     * Check if the renewal was confirmed.
     * 
     * @return true if status is CONFIRMED
     */
    @Transient
    public boolean isConfirmed() {
        return status == RenewalStatus.CONFIRMED;
    }

    /**
     * Check if the renewal was declined.
     * 
     * @return true if status is DECLINED
     */
    @Transient
    public boolean isDeclined() {
        return status == RenewalStatus.DECLINED;
    }

    /**
     * Check if the renewal has expired.
     * 
     * @return true if expires_at is in the past
     */
    @Transient
    public boolean isExpired() {
        return status == RenewalStatus.EXPIRED ||
                (expiresAt != null && LocalDateTime.now().isAfter(expiresAt));
    }

    /**
     * Check if employee can still respond to this renewal.
     * 
     * @return true if status is PENDING_ACTION and not expired
     */
    @Transient
    public boolean canRespond() {
        return isPending() && !isExpired();
    }

    /**
     * Check if this renewal belongs to a specific employee.
     * 
     * @param employeeId the employee ID to check
     * @return true if the employee is the owner
     */
    @Transient
    public boolean isOwnedBy(Integer employeeId) {
        return employee != null && employee.getEmployeeId().equals(employeeId);
    }
}
