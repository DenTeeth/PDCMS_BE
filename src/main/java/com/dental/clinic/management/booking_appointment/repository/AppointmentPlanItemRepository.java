package com.dental.clinic.management.booking_appointment.repository;

import com.dental.clinic.management.booking_appointment.domain.AppointmentPlanItem;
import com.dental.clinic.management.booking_appointment.domain.AppointmentPlanItem.AppointmentPlanItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for AppointmentPlanItem bridge table (Treatment Plan Integration)
 *
 * Purpose: Manage N-N relationship between appointments and patient plan items
 *
 * Usage in AppointmentCreationService:
 * - insertAppointmentPlanItems(): Create bridge records after appointment created
 *
 * Example:
 * Request: patientPlanItemIds = [307, 308]
 * Result: Insert 2 rows:
 *   - (appointmentId=123, itemId=307)
 *   - (appointmentId=123, itemId=308)
 */
@Repository
public interface AppointmentPlanItemRepository extends JpaRepository<AppointmentPlanItem, AppointmentPlanItemId> {
    // No custom queries needed - using default save() from JpaRepository
}
