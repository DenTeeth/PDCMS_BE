package com.dental.clinic.management.working_schedule.repository;

import com.dental.clinic.management.working_schedule.domain.PartTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartTimeSlotRepository extends JpaRepository<PartTimeSlot, Long> {

       /**
        * Check if a slot already exists for the given shift and day.
        */
       boolean existsByWorkShiftIdAndDayOfWeek(String workShiftId, String dayOfWeek);

       /**
        * Find slot by shift and day.
        */
       Optional<PartTimeSlot> findByWorkShiftIdAndDayOfWeek(String workShiftId, String dayOfWeek);

       /**
        * Find all active slots for a specific day of week.
        * Used for V14 Hybrid shift checking.
        */
       @Query("SELECT pts FROM PartTimeSlot pts " +
                     "LEFT JOIN FETCH pts.workShift " +
                     "LEFT JOIN FETCH pts.registrations " +
                     "WHERE pts.dayOfWeek = :dayOfWeek AND pts.isActive = true")
       List<PartTimeSlot> findByDayOfWeekAndIsActiveTrue(@Param("dayOfWeek") String dayOfWeek);

       /**
        * Count active registrations for a slot (Schema V14 - part_time_registrations).
        * Used to check quota before allowing new registrations.
        */
       @Query("SELECT COUNT(r) FROM PartTimeRegistration r " +
                     "WHERE r.partTimeSlotId = :slotId AND r.isActive = true")
       long countActiveRegistrations(@Param("slotId") Long slotId);

       /**
        * Check if a work shift is being used by any part-time slots.
        * V2: Used to prevent deletion/modification of shifts that have part-time
        * slots.
        */
       boolean existsByWorkShiftId(String workShiftId);

       /**
        * Count part-time slots using a specific work shift.
        * V2: Used to provide detailed error messages.
        */
       long countByWorkShiftId(String workShiftId);
}
