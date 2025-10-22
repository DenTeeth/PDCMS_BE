package com.dental.clinic.management.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.dental.clinic.management.employee.domain.Employee;

/**
 * Spring Data JPA repository for the {@link Employee} entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer>, JpaSpecificationExecutor<Employee> {

  Optional<Employee> findOneByEmployeeCode(String employeeCode);

  Optional<Employee> findByEmployeeCodeAndIsActiveTrue(String employeeCode);

  boolean existsByEmployeeCode(String employeeCode);

  Optional<Employee> findOneByAccountAccountId(Integer accountId);

  /**
   * Find employee by account username.
   * Used for owner validation and getting current employee info from JWT token.
   *
   * @param username Account username from security context
   * @return Optional employee entity
   */
  Optional<Employee> findByAccount_Username(String username);

  /**
   * Check if employee exists by account username.
   *
   * @param username Account username
   * @return True if exists
   */
  boolean existsByAccount_Username(String username);

  /**
   * Find all active employees.
   * Used for annual leave balance reset.
   *
   * @return List of active employees
   */
  List<Employee> findByIsActiveTrue();

  /**
   * Find employees by employment type and active status.
   * Used by scheduled jobs to create shifts.
   *
   * @param employmentType Employment type (FULL_TIME or PART_TIME)
   * @param isActive       Active status
   * @return List of matching employees
   */
  List<Employee> findByEmploymentTypeAndIsActive(
      com.dental.clinic.management.employee.enums.EmploymentType employmentType,
      Boolean isActive);
}
