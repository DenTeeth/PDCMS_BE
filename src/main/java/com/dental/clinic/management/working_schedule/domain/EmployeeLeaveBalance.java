package com.dental.clinic.management.working_schedule.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_leave_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long balanceId;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "time_off_type_id", nullable = false, length = 50)
    private String timeOffTypeId;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "total_allotted", nullable = false)
    private Double totalAllotted;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Double used = 0.0;

    @Column(name = "remaining", nullable = false)
    private Double remaining;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (remaining == null) {
            remaining = totalAllotted - (used != null ? used : 0.0);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        remaining = totalAllotted - used;
    }
}
