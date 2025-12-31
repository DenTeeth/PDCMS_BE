package com.dental.clinic.management.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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
     * Check if employee exists by phone number.
     * Used for phone uniqueness validation.
     *
     * @param phone Phone number
     * @return True if exists
     */
    Boolean existsByPhone(String phone);

    /**
     * Find all active employees.
     * Used for annual leave balance reset.
     *
     * @return List of active employees
     */
    List<Employee> findByIsActiveTrue();

    /**
     * Find all inactive employees.
     * Used by Job P3 (CleanupInactiveEmployeeRegistrationsJob) to cleanup registrations.
     *
     * @return List of inactive employees
     */
    List<Employee> findByIsActiveFalse();

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

    /**
     * Find all active employee IDs.
     * Used for annual leave balance reset.
     *
     * @return List of employee IDs
     */
    @Query("SELECT e.employeeId FROM Employee e WHERE e.isActive = true")
    List<Integer> findAllActiveEmployeeIds();

    /**
     * Find ACTIVE medical staff (dentists, nurses, interns)
     * Used for appointment doctor/participant selection
     * Excludes Admin/Receptionist/Accountant/Manager who are non-medical
     *
     * @return List of medical staff employees
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
            "LEFT JOIN FETCH e.specializations s " +
            "LEFT JOIN FETCH e.account a " +
            "LEFT JOIN FETCH a.role r " +
            "WHERE e.isActive = true " +
            "AND r.roleId IN ('ROLE_DENTIST', 'ROLE_NURSE', 'ROLE_DENTIST_INTERN') " +
            "ORDER BY e.employeeCode ASC")
    List<Employee> findActiveEmployeesWithSpecializations();

    /**
     * Check if employee is medical staff (dentist, nurse, or intern)
     * Medical staff can be assigned to appointments
     * Non-medical staff (admin, receptionist, accountant) cannot
     *
     * @param employeeId Employee ID
     * @return True if employee has a medical role
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Employee e " +
            "JOIN e.account a " +
            "JOIN a.role r " +
            "WHERE e.employeeId = :employeeId " +
            "AND r.roleId IN ('ROLE_DENTIST', 'ROLE_NURSE', 'ROLE_DENTIST_INTERN')")
    boolean hasSpecializations(@org.springframework.data.repository.query.Param("employeeId") Integer employeeId);
}
