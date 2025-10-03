package com.dental.clinic.management.service;

import com.dental.clinic.management.dto.request.CreateEmployeeRequest;
import com.dental.clinic.management.dto.request.UpdateEmployeeRequest;
import com.dental.clinic.management.dto.response.EmployeeInfoResponse;
import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.exception.EmployeeNotFoundException;
import com.dental.clinic.management.mapper.EmployeeMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

import com.dental.clinic.management.domain.Account;
import com.dental.clinic.management.domain.Employee;
import com.dental.clinic.management.domain.Specialization;
import com.dental.clinic.management.repository.AccountRepository;
import com.dental.clinic.management.repository.EmployeeRepository;
import com.dental.clinic.management.repository.SpecializationRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AccountRepository accountRepository;
    private final SpecializationRepository specializationRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            EmployeeMapper employeeMapper,
            AccountRepository accountRepository,
            SpecializationRepository specializationRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.accountRepository = accountRepository;
        this.specializationRepository = specializationRepository;
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public Page<Employee> findAllEmployees(
            Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    /**
     * Get all employees with pagination, sorting and mapping to DTO
     *
     * @param page          page number (zero-based)
     * @param size          number of items per page
     * @param sortBy        field name to sort by
     * @param sortDirection ASC or DESC
     * @return Page of EmployeeInfoResponse
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + READ_ALL_EMPLOYEES + "')")
    public Page<EmployeeInfoResponse> getAllEmployees(
            int page, int size, String sortBy, String sortDirection) {

        // Validate and sanitize inputs
        page = Math.max(0, page); // Ensure page is not negative
        size = (size <= 0 || size > 100) ? 10 : size; // Default to 10 if invalid, max 100

        // Create sort direction
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Create sort object
        Sort sort = Sort.by(direction, sortBy);

        // Create pageable
        Pageable pageable = PageRequest.of(page, size,
                sort);

        // Fetch employees and map to DTO
        Page<Employee> employeePage = employeeRepository.findAll(pageable);
        return employeePage.map(employeeMapper::toEmployeeInfoResponse);
    }

    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + READ_EMPLOYEE_BY_CODE + "')")
    @Transactional(readOnly = true)
    public Employee findEmployeeByCode(String employeeCode) {
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee code cannot be null or empty");
        }

        return employeeRepository.findOneByEmployeeCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with code: " + employeeCode));
    }

    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + CREATE_EMPLOYEE + "')")
    @Transactional
    public EmployeeInfoResponse createEmployee(CreateEmployeeRequest request) {
        // Validate account exists
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Account not found with ID: " + request.getAccountId(),
                        "account",
                        "accountnotfound"));

        // Check if employee already exists for this account
        if (employeeRepository.findOneByAccountId(request.getAccountId()).isPresent()) {
            throw new BadRequestAlertException(
                    "Employee already exists",
                    "employee",
                    "employeeexists");
        }

        // Generate unique employee ID
        String employeeId = UUID.randomUUID().toString();

        // Generate employee code (e.g., EMP001, EMP002, ...)
        String employeeCode = generateEmployeeCode();

        // Create new employee
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setAccount(account);
        employee.setRoleId(request.getRoleId());
        employee.setEmployeeCode(employeeCode);
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setAddress(request.getAddress());
        employee.setIsActive(true);

        // Add specializations if provided
        if (request.getSpecializationIds() != null && !request.getSpecializationIds().isEmpty()) {
            Set<Specialization> specializations = new HashSet<>();
            for (Integer specializationId : request.getSpecializationIds()) {
                Specialization specialization = specializationRepository.findById(String.valueOf(specializationId))
                        .orElseThrow(() -> new BadRequestAlertException(
                                "Specialization not found with ID: " + specializationId,
                                "specialization",
                                "specializationnotfound"));
                specializations.add(specialization);
            }
            employee.setSpecializations(specializations);
        }

        // Save employee
        Employee savedEmployee = employeeRepository.save(employee);

        // Return DTO response
        return employeeMapper.toEmployeeInfoResponse(savedEmployee);
    }

    /**
     * Generate unique employee code
     * Format: EMP001, EMP002, EMP003...
     */
    private String generateEmployeeCode() {
        long count = employeeRepository.count();
        return String.format("EMP%03d", count + 1);
    }

    /**
     * Partial update of an employee
     * Only non-null fields will be updated
     *
     * @param employeeCode the code of the employee to update
     * @param request      the update data
     * @return the updated employee as DTO
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + UPDATE_EMPLOYEE + "')")
    @Transactional
    public EmployeeInfoResponse updateEmployee(String employeeCode, UpdateEmployeeRequest request) {
        // Find existing employee
        Employee employee = employeeRepository.findOneByEmployeeCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with code: " + employeeCode));

        // Update only non-null fields
        if (request.getRoleId() != null) {
            employee.setRoleId(request.getRoleId());
        }

        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }

        if (request.getDateOfBirth() != null) {
            employee.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getAddress() != null) {
            employee.setAddress(request.getAddress());
        }

        if (request.getIsActive() != null) {
            employee.setIsActive(request.getIsActive());
        }

        // Update specializations if provided
        if (request.getSpecializationIds() != null) {
            Set<Specialization> specializations = new HashSet<>();
            for (Integer specializationId : request.getSpecializationIds()) {
                Specialization specialization = specializationRepository.findById(String.valueOf(specializationId))
                        .orElseThrow(() -> new BadRequestAlertException(
                                "Specialization not found with ID: " + specializationId,
                                "specialization",
                                "specializationnotfound"));
                specializations.add(specialization);
            }
            employee.setSpecializations(specializations);
        }

        // Save updated employee
        Employee updatedEmployee = employeeRepository.save(employee);

        // Return DTO response
        return employeeMapper.toEmployeeInfoResponse(updatedEmployee);
    }

    /**
     * Delete an employee (soft delete - set isActive to false)
     *
     * @param employeeCode the code of the employee to delete
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + DELETE_EMPLOYEE + "')")
    @Transactional
    public void deleteEmployee(String employeeCode) {
        // Find existing employee
        Employee employee = employeeRepository.findOneByEmployeeCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with code: " + employeeCode));

        // Soft delete - set isActive to false
        employee.setIsActive(false);
        employeeRepository.save(employee);
    }

    /**
     * Permanently delete an employee (hard delete)
     * Use with caution - this cannot be undone
     *
     * @param employeeCode the code of the employee to delete permanently
     */
    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional
    public void deleteEmployeePermanently(String employeeCode) {
        // Find existing employee
        Employee employee = employeeRepository.findOneByEmployeeCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with code: " + employeeCode));

        // Hard delete - remove from database
        employeeRepository.delete(employee);
    }
}
