package com.dental.clinic.management.employee_shifts.domain;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee_shifts.enums.EmployeeShiftStatus;
import com.dental.clinic.management.employee_shifts.enums.ShiftSource;
import com.dental.clinic.management.work_shifts.domain.WorkShift;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Entity representing an employee's assigned shift on a specific date.
 * This tracks the actual shift assignments and their status.
 */
@Entity
@Table(name = "employee_shifts", indexes = {
        @Index(name = "idx_employee_workdate", columnList = "employee_id, work_date"),
        @Index(name = "idx_workdate_status", columnList = "work_date, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShift {

    @Id
    @Column(name = "employee_shift_id", updatable = false, nullable = false, length = 20)
    private String id;
    // EMS25102301 VD: EmployeeShift 25/10/2023 Shift 01
    // Format: EMS + yyMMdd + SEQ (3 digits)
    @NotNull(message = "Work date is required")
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    private EmployeeShiftStatus status = EmployeeShiftStatus.SCHEDULED;

    @Column(name = "notes", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Column(name = "source", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Source is required")
    private ShiftSource source;

    @Column(name = "is_overtime", nullable = false)
    private Boolean isOvertime = false;

    // ============================================
    // RELATIONSHIPS
    // ============================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_shift_id", nullable = false)
    @NotNull(message = "Work shift is required")
    private WorkShift workShift;

    // ============================================
    // AUDITING FIELDS
    // ============================================

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    /**
     * Check if this shift is editable based on its status.
     * Only SCHEDULED shifts can be edited.
     * 
     * @return true if shift can be edited
     */
    @Transient
    public Boolean isEditable() {
        return status == EmployeeShiftStatus.SCHEDULED;
    }

    /**
     * Check if this shift is cancellable.
     * Only MANUAL_ENTRY shifts with SCHEDULED status can be cancelled.
     * 
     * @return true if shift can be cancelled
     */
    @Transient
    public Boolean isCancellable() {
        return source == ShiftSource.MANUAL_ENTRY && status == EmployeeShiftStatus.SCHEDULED;
    }

    /**
     * Check if this shift is in the past.
     * 
     * @return true if work date is before today
     */
    @Transient
    public Boolean isPastShift() {
        return workDate.isBefore(LocalDate.now());
    }
}
