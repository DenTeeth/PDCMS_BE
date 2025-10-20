package com.dental.clinic.management.employee_shift_registrations.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dental.clinic.management.employee_shift_registrations.domain.EmployeeShiftRegistration;

// EmployeeShiftRegistrationRepository.java
public interface EmployeeShiftRegistrationRepository extends JpaRepository<EmployeeShiftRegistration, String> {

    // Ghi đè phương thức findAll để yêu cầu join fetch
    @Override
    @EntityGraph(attributePaths = "registrationDays")
    Page<EmployeeShiftRegistration> findAll(Pageable pageable);

    // Tạo phương thức mới cho luồng VIEW_OWN
    @EntityGraph(attributePaths = "registrationDays")
    Page<EmployeeShiftRegistration> findByEmployeeId(Integer employeeId, Pageable pageable);

    Optional<EmployeeShiftRegistration> findByEmployeeId(Integer employeeId);
}
