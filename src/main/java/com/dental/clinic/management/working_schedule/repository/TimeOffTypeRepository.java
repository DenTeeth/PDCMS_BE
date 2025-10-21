package com.dental.clinic.management.working_schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dental.clinic.management.working_schedule.domain.TimeOffType;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TimeOffType entity
 */
@Repository
public interface TimeOffTypeRepository extends JpaRepository<TimeOffType, String> {

    /**
     * Find time-off type by type_id
     */
    Optional<TimeOffType> findByTypeId(String typeId);

    /**
     * Find time-off type by type_id and is_active
     */
    Optional<TimeOffType> findByTypeIdAndIsActive(String typeId, Boolean isActive);

    /**
     * Find all active time-off types
     */
    List<TimeOffType> findByIsActiveTrue();
}
