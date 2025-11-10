package com.dental.clinic.management.booking_appointment.repository;

import com.dental.clinic.management.booking_appointment.domain.AppointmentPlanItemBridge;
import com.dental.clinic.management.booking_appointment.domain.AppointmentPlanItemBridge.AppointmentPlanItemBridgeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for AppointmentPlanItemBridge table (Treatment Plan Integration)
 *
 * Purpose: Manage N-N relationship between appointments and patient plan items
 *
 * Usage in AppointmentCreationService:
 * - insertAppointmentPlanItems(): Create bridge records after appointment
 * created
 *
 * Example:
 * Request: patientPlanItemIds = [307, 308]
 * Result: Insert 2 rows:
 * - (appointmentId=123, itemId=307)
 * - (appointmentId=123, itemId=308)
 */
@Repository
public interface AppointmentPlanItemRepository
        extends JpaRepository<AppointmentPlanItemBridge, AppointmentPlanItemBridgeId> {
    // No custom queries needed - using default save() from JpaRepository
}
