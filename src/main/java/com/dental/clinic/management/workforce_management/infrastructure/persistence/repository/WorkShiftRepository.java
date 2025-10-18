package com.dental.clinic.management.workforce_management.infrastructure.persistence.repository;

import com.dental.clinic.management.workforce_management.infrastructure.persistence.entity.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WorkShift entity.
 */
@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, String> {

    /**
     * Find all work shifts by active status.
     * 
     * @param isActive true to find active shifts, false for inactive
     * @return List of work shifts matching the status
     */
    List<WorkShift> findByIsActive(Boolean isActive);

    /**
     * Check if a work shift ID already exists.
     * 
     * @param workShiftId the work shift ID to check
     * @return true if exists, false otherwise
     */
    boolean existsByWorkShiftId(String workShiftId);

    /**
     * Find active work shift by ID.
     * 
     * @param workShiftId the work shift ID
     * @return Optional containing the work shift if found and active
     */
    Optional<WorkShift> findByWorkShiftIdAndIsActive(String workShiftId, Boolean isActive);

    /**
     * Find all work shifts with IDs starting with a specific prefix.
     * Used to determine the next sequence number.
     * 
     * @param prefix ID prefix (e.g., "WKS_MORNING_")
     * @return List of work shifts with matching prefix
     */
    List<WorkShift> findByWorkShiftIdStartingWith(String prefix);
}
