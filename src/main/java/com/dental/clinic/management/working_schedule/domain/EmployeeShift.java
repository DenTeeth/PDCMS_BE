package com.dental.clinic.management.working_schedule.domain;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.working_schedule.enums.ShiftSource;
import com.dental.clinic.management.working_schedule.enums.ShiftStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing an actual scheduled shift for an employee.
 * This is the final schedule after registration/approval.
 *
 * Created by:
 * - Batch job (monthly for full-time, weekly for part-time)
 * - Manual scheduling by admin
 * - Approved overtime requests
 */
@Entity
@Table(name = "employee_shifts", indexes = {
        @Index(name = "idx_shift_employee", columnList = "employee_id"),
        @Index(name = "idx_shift_work_date", columnList = "work_date"),
        @Index(name = "idx_shift_status", columnList = "status"),
        @Index(name = "idx_shift_source", columnList = "source")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_date_shift", columnNames = { "employee_id", "work_date",
                "work_shift_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_id")
    private Long shiftId;

    /**
     * The employee assigned to this shift.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    /**
     * The work date for this shift.
     */
    @Column(name = "work_date", nullable = false)
    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    /**
     * The work shift (MORNING, AFTERNOON, EVENING, etc.).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_shift_id", nullable = false)
    @NotNull(message = "Work shift is required")
    private WorkShift workShift;

    /**
     * Source of this shift (how it was created).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    @NotNull(message = "Source is required")
    private ShiftSource source;

    /**
     * Optional reference to the employee_shift_registration that generated this
     * shift.
     * Only populated for part-time employees.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id")
    private EmployeeShiftRegistration registration;

    /**
     * Current status of the shift.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    private ShiftStatus status = ShiftStatus.SCHEDULED;

    /**
     * Timestamp when the shift was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the shift was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Notes about this shift.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ShiftStatus.SCHEDULED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
