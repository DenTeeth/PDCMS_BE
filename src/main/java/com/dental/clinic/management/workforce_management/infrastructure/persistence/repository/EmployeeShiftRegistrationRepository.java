package com.dental.clinic.management.workforce_management.infrastructure.persistence.repository;

import com.dental.clinic.management.workforce_management.infrastructure.persistence.entity.EmployeeShiftRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for EmployeeShiftRegistration entity.
 */
@Repository
public interface EmployeeShiftRegistrationRepository extends JpaRepository<EmployeeShiftRegistration, String> {

    /**
     * Find all registrations by employee ID.
     * 
     * @param employeeId the employee ID
     * @return List of registrations
     */
    List<EmployeeShiftRegistration> findByEmployeeId(String employeeId);

    /**
     * Find active registrations by employee ID.
     * 
     * @param employeeId the employee ID
     * @param isActive   active status
     * @return List of active registrations
     */
    List<EmployeeShiftRegistration> findByEmployeeIdAndIsActive(String employeeId, Boolean isActive);

    /**
     * Find registrations by slot ID.
     * 
     * @param slotId the slot ID
     * @return List of registrations
     */
    List<EmployeeShiftRegistration> findBySlotId(String slotId);
}
