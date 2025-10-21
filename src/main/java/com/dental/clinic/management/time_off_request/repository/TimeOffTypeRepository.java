package com.dental.clinic.management.time_off_request.repository;

import com.dental.clinic.management.time_off_request.domain.TimeOffType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
