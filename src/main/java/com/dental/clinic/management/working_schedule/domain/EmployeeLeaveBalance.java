package com.dental.clinic.management.working_schedule.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_leave_balances")
public class EmployeeLeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long balanceId;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "time_off_type_id", nullable = false, length = 50)
    private String timeOffTypeId;

    @Column(name = "cycle_year", nullable = false)
    private Integer year;

    @Column(name = "total_days_allowed", nullable = false)
    private Double totalAllotted;

    @Column(name = "days_taken", nullable = false)
    private Double used = 0.0;

    @Transient
    private Double remaining;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public EmployeeLeaveBalance() {
    }

    public EmployeeLeaveBalance(Long balanceId, Integer employeeId, String timeOffTypeId, Integer year,
            Double totalAllotted, Double used, String notes, LocalDateTime updatedAt) {
        this.balanceId = balanceId;
        this.employeeId = employeeId;
        this.timeOffTypeId = timeOffTypeId;
        this.year = year;
        this.totalAllotted = totalAllotted;
        this.used = used;
        this.notes = notes;
        this.updatedAt = updatedAt;
    }

    public Long getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(Long balanceId) {
        this.balanceId = balanceId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getTimeOffTypeId() {
        return timeOffTypeId;
    }

    public void setTimeOffTypeId(String timeOffTypeId) {
        this.timeOffTypeId = timeOffTypeId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getTotalAllotted() {
        return totalAllotted;
    }

    public void setTotalAllotted(Double totalAllotted) {
        this.totalAllotted = totalAllotted;
    }

    public Double getUsed() {
        return used;
    }

    public void setUsed(Double used) {
        this.used = used;
    }

    public Double getRemaining() {
        return remaining;
    }

    public void setRemaining(Double remaining) {
        this.remaining = remaining;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    protected void calculateRemaining() {
        this.remaining = totalAllotted - used;
    }
}
