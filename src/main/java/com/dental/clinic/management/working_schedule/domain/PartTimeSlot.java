package com.dental.clinic.management.working_schedule.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a part-time slot that admin creates.
 * Defines clinic's needs (e.g., "Need 2 people for Morning shift on Tuesday").
 */
@Entity
@Table(name = "part_time_slots", 
       uniqueConstraints = @UniqueConstraint(name = "unique_shift_day", 
                                             columnNames = {"work_shift_id", "day_of_week"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartTimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Long slotId;

    @Column(name = "work_shift_id", length = 20, nullable = false)
    private String workShiftId;

    @Column(name = "day_of_week", length = 10, nullable = false)
    private String dayOfWeek; // MONDAY, TUESDAY, etc.

    @Column(name = "quota", nullable = false)
    private Integer quota = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
