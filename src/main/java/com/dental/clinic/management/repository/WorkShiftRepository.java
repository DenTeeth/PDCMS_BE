package com.dental.clinic.management.repository;

import com.dental.clinic.management.domain.WorkShift;
import com.dental.clinic.management.domain.enums.WorkShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for WorkShift entity.
 * Manages predefined work shifts for the clinic.
 */
@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, String> {

    /**
     * Find shift by unique business code.
     * @param shiftCode Shift code (e.g., SHIFT_MORNING)
     * @return Optional shift entity
     */
    Optional<WorkShift> findByShiftCode(String shiftCode);

    /**
     * Find shift by name (case-insensitive).
     * @param shiftName Shift name
     * @return Optional shift entity
     */
    Optional<WorkShift> findByShiftNameIgnoreCase(String shiftName);

    /**
     * Find shift by type.
     * @param shiftType Shift type enum (MORNING, AFTERNOON, EVENING)
     * @return Optional shift entity
     */
    Optional<WorkShift> findByShiftType(WorkShiftType shiftType);

    /**
     * Get all active shifts, ordered by start time.
     * @param isActive Filter by active status
     * @return List of active shifts
     */
    List<WorkShift> findByIsActiveTrueOrderByStartTimeAsc();

    /**
     * Get all shifts, ordered by start time.
     * @return List of all shifts
     */
    List<WorkShift> findAllByOrderByStartTimeAsc();

    /**
     * Check if shift code already exists (for validation).
     * @param shiftCode Shift code to check
     * @return True if exists
     */
    boolean existsByShiftCode(String shiftCode);

    /**
     * Check if shift name already exists (case-insensitive).
     * @param shiftName Shift name to check
     * @return True if exists
     */
    boolean existsByShiftNameIgnoreCase(String shiftName);

    /**
     * Find shifts that overlap with given time range.
     * Used for validation when creating new shifts.
     * 
     * @param startTime Start time to check
     * @param endTime End time to check
     * @return List of overlapping shifts
     */
    @Query("SELECT s FROM WorkShift s WHERE s.isActive = true " +
           "AND ((s.startTime <= :startTime AND s.endTime > :startTime) " +
           "OR (s.startTime < :endTime AND s.endTime >= :endTime) " +
           "OR (s.startTime >= :startTime AND s.endTime <= :endTime))")
    List<WorkShift> findOverlappingShifts(@Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime);

    /**
     * Count active shifts.
     * @return Number of active shifts
     */
    long countByIsActiveTrue();
}
