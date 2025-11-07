package com.dental.clinic.management.booking_appointment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Bridge table entity for N-N relationship between appointments and patient plan items
 *
 * Purpose: Link appointments to treatment plan items (Luồng 2: Đặt theo lộ trình)
 * Example: Appointment APT-20251208-001 → Item 307 (Lần 3: Siết niềng)
 *
 * Business Flow:
 * 1. Receptionist creates appointment with patientPlanItemIds = [307, 308]
 * 2. System inserts 2 rows: (appointmentId, 307) and (appointmentId, 308)
 * 3. System updates items 307, 308: status READY_FOR_BOOKING → SCHEDULED
 */
@Entity
@Table(name = "appointment_plan_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentPlanItem {

    @EmbeddedId
    private AppointmentPlanItemId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Composite Primary Key for appointment_plan_items
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentPlanItemId implements Serializable {

        @Column(name = "appointment_id")
        private Long appointmentId;

        @Column(name = "item_id")
        private Long itemId;
    }
}
