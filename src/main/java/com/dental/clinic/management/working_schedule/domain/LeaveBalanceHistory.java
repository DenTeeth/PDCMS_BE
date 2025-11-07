package com.dental.clinic.management.working_schedule.domain;

import com.dental.clinic.management.working_schedule.enums.BalanceChangeReason;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balance_history")
public class LeaveBalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "balance_id", nullable = false)
    private Long balanceId;

    @Column(name = "changed_by", nullable = false)
    private Integer changedBy;

    @Column(name = "change_amount", nullable = false)
    private Double changeAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private BalanceChangeReason reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public LeaveBalanceHistory() {
    }

    public LeaveBalanceHistory(Long historyId, Long balanceId, Integer changedBy, Double changeAmount,
            BalanceChangeReason reason, String notes, LocalDateTime createdAt) {
        this.historyId = historyId;
        this.balanceId = balanceId;
        this.changedBy = changedBy;
        this.changeAmount = changeAmount;
        this.reason = reason;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Long getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(Long balanceId) {
        this.balanceId = balanceId;
    }

    public Integer getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Integer changedBy) {
        this.changedBy = changedBy;
    }

    public Double getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Double changeAmount) {
        this.changeAmount = changeAmount;
    }

    public BalanceChangeReason getReason() {
        return reason;
    }

    public void setReason(BalanceChangeReason reason) {
        this.reason = reason;
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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
