package com.dental.clinic.management.workforce_management.domain.service;

import com.dental.clinic.management.workforce_management.application.dto.CreateEmployeeShiftRegistrationRequest;
import com.dental.clinic.management.workforce_management.application.dto.EmployeeShiftRegistrationResponse;

import java.util.List;

/**
 * Domain service interface for employee shift registration business logic.
 */
public interface EmployeeShiftRegistrationService {

    /**
     * Create a new employee shift registration.
     * @param request the create request
     * @return EmployeeShiftRegistrationResponse
     */
    EmployeeShiftRegistrationResponse createRegistration(CreateEmployeeShiftRegistrationRequest request);

    /**
     * Get all active registrations for an employee.
     * @param employeeId the employee ID
     * @return List of registrations
     */
    List<EmployeeShiftRegistrationResponse> getRegistrationsByEmployee(String employeeId);

    /**
     * Get registration by ID.
     * @param registrationId the registration ID
     * @return EmployeeShiftRegistrationResponse
     */
    EmployeeShiftRegistrationResponse getRegistrationById(String registrationId);

    /**
     * Update registration.
     * @param registrationId the registration ID
     * @param request the update request
     * @return EmployeeShiftRegistrationResponse
     */
    EmployeeShiftRegistrationResponse updateRegistration(String registrationId, CreateEmployeeShiftRegistrationRequest request);

    /**
     * Deactivate registration.
     * @param registrationId the registration ID
     */
    void deactivateRegistration(String registrationId);
}