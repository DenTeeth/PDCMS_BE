package com.dental.clinic.management.service;

import com.dental.clinic.management.domain.enums.AccountStatus;
import com.dental.clinic.management.dto.request.CreateEmployeeRequest;
import com.dental.clinic.management.dto.request.UpdateEmployeeRequest;
import com.dental.clinic.management.dto.response.EmployeeInfoResponse;
import com.dental.clinic.management.exception.BadRequestAlertException;
import com.dental.clinic.management.exception.EmployeeNotFoundException;
import com.dental.clinic.management.mapper.EmployeeMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

import com.dental.clinic.management.domain.Account;
import com.dental.clinic.management.domain.Employee;
import com.dental.clinic.management.domain.Specialization;
import com.dental.clinic.management.repository.AccountRepository;
import com.dental.clinic.management.repository.EmployeeRepository;
import com.dental.clinic.management.repository.RoleRepository;
import com.dental.clinic.management.repository.SpecializationRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AccountRepository accountRepository;
    private final SpecializationRepository specializationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            EmployeeMapper employeeMapper,
            AccountRepository accountRepository,
            SpecializationRepository specializationRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.accountRepository = accountRepository;
        this.specializationRepository = specializationRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all ACTIVE employees only (isActive = true) with pagination, sorting and
     * mapping to DTO
     * This is the default method for normal operations
     *
     * @param page          page number (zero-based)
     * @param size          number of items per page
     * @param sortBy        field name to sort by
     * @param sortDirection ASC or DESC
     * @return Page of EmployeeInfoResponse
     */
    @PreAuthorize("hasRole('" + ADMIN + "')")
    public Page<EmployeeInfoResponse> getAllActiveEmployees(
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
        Pageable pageable = PageRequest.of(page, size, sort);

        // Create specification to filter only active employees
        Specification<Employee> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isActive"),
                true);

        // Fetch employees and map to DTO
        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);
        return employeePage.map(employeeMapper::toEmployeeInfoResponse);
    }

    /**
     * Get ALL employees including deleted ones (isActive = true AND false)
     * This method is for admin management purposes only
     *
     * @param page          page number (zero-based)
     * @param size          number of items per page
     * @param sortBy        field name to sort by
     * @param sortDirection ASC or DESC
     * @return Page of EmployeeInfoResponse
     */
    @PreAuthorize("hasRole('" + ADMIN + "')")
    public Page<EmployeeInfoResponse> getAllEmployeesIncludingDeleted(
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
        Pageable pageable = PageRequest.of(page, size, sort);

        // Fetch ALL employees (no filter) and map to DTO
        Page<Employee> employeePage = employeeRepository.findAll(pageable);
        return employeePage.map(employeeMapper::toEmployeeInfoResponse);
    }

    /**
     * Get ACTIVE employee by employee code with DTO response (isActive = true only)
     * This is the default method for normal operations
     *
     * @param employeeCode the code of the employee
     * @return EmployeeInfoResponse
     * @throws EmployeeNotFoundException if employee not found or deleted
     */
    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public EmployeeInfoResponse getActiveEmployeeByCode(String employeeCode) {
        Employee employee = findActiveEmployeeByCode(employeeCode);
        return employeeMapper.toEmployeeInfoResponse(employee);
    }

    /**
     * Get employee by code INCLUDING deleted ones (isActive = true or false)
     * This method is for admin management purposes only
     *
     * @param employeeCode the code of the employee
     * @return EmployeeInfoResponse
     * @throws EmployeeNotFoundException if employee not found
     */
    @PreAuthorize("hasRole('" + ADMIN + "')")
    @Transactional(readOnly = true)
    public EmployeeInfoResponse getEmployeeByCodeIncludingDeleted(String employeeCode) {
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee code cannot be null or empty");
        }

        Employee employee = employeeRepository.findOneByEmployeeCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with code: " + employeeCode));

        return employeeMapper.toEmployeeInfoResponse(employee);
    }

    /**
     * Find ACTIVE employee entity by code (isActive = true only)
     *
     * @param employeeCode the code of the employee
     * @return Employee entity
     * @throws EmployeeNotFoundException if employee not found or deleted
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + READ_EMPLOYEE_BY_CODE + "')")
    @Transactional(readOnly = true)
    public Employee findActiveEmployeeByCode(String employeeCode) {
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee code cannot be null or empty");
        }

        Employee employee = employeeRepository.findOneByEmployeeCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeCode));

        // Check if employee is active (not soft-deleted)
        if (employee.getIsActive() == null || !employee.getIsActive()) {
            throw new EmployeeNotFoundException(employeeCode);
        }

        return employee;
    }

    /**
     * Create new employee with account
     *
     * FLOW: Tạo Employee → Tự động tạo Account mới
     * - Admin/Manager tạo employee
     * - System tự động tạo account với username/password
     * - Gửi thông tin đăng nhập cho employee
     *
     * @param request employee information including username/password
     * @return EmployeeInfoResponse
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + CREATE_EMPLOYEE + "')")
    @Transactional
    public EmployeeInfoResponse createEmployee(CreateEmployeeRequest request) {
        log.debug("Request to create employee: {}", request);

        // Validate required fields
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BadRequestAlertException(
                    "Username is required",
                    "employee",
                    "usernamerequired");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestAlertException(
                    "Email is required",
                    "employee",
                    "emailrequired");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BadRequestAlertException(
                    "Password is required",
                    "employee",
                    "passwordrequired");
        }

        // Check uniqueness
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestAlertException(
                    "Username already exists",
                    "account",
                    "usernameexists");
        }

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestAlertException(
                    "Email already exists",
                    "account",
                    "emailexists");
        }

        // Create new account for employee
        Account account = new Account();
        account.setAccountId(UUID.randomUUID().toString());
        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(java.time.LocalDateTime.now());

        account = accountRepository.save(account);
        log.info("Created account with ID: {} for employee", account.getAccountId());

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
        employee.setCreatedAt(java.time.LocalDateTime.now());

        // Add specializations if provided
        if (request.getSpecializationIds() != null && !request.getSpecializationIds().isEmpty()) {
            Set<Specialization> specializations = new HashSet<>();
            for (String specializationId : request.getSpecializationIds()) {
                Specialization specialization = specializationRepository.findById(specializationId)
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
     * Using synchronized to prevent duplicate codes in concurrent requests
     */
    private synchronized String generateEmployeeCode() {
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
            for (String specializationId : request.getSpecializationIds()) {
                Specialization specialization = specializationRepository.findById(specializationId)
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
     * Replace (full update) an employee - all fields are required
     * This is a PUT operation that replaces the entire resource
     *
     * @param employeeCode the code of the employee to replace
     * @param request      the replacement data (all fields required)
     * @return the replaced employee as DTO
     */
    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + UPDATE_EMPLOYEE + "')")
    @Transactional
    public EmployeeInfoResponse replaceEmployee(String employeeCode,
            com.dental.clinic.management.dto.request.ReplaceEmployeeRequest request) {
        // Find existing employee
        Employee employee = employeeRepository.findOneByEmployeeCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with code: " + employeeCode));

        // Verify role exists
        if (!roleRepository.existsById(request.getRoleId())) {
            throw new BadRequestAlertException(
                    "Role not found with ID: " + request.getRoleId(),
                    "role",
                    "rolenotfound");
        }

        // Replace ALL fields (required by PUT semantics)
        employee.setRoleId(request.getRoleId());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setAddress(request.getAddress());
        employee.setIsActive(request.getIsActive());

        // Replace specializations
        if (request.getSpecializationIds() != null && !request.getSpecializationIds().isEmpty()) {
            Set<Specialization> specializations = new HashSet<>();
            for (String specializationId : request.getSpecializationIds()) {
                Specialization specialization = specializationRepository.findById(specializationId)
                        .orElseThrow(() -> new BadRequestAlertException(
                                "Specialization not found with ID: " + specializationId,
                                "specialization",
                                "specializationnotfound"));
                specializations.add(specialization);
            }
            employee.setSpecializations(specializations);
        } else {
            // Clear specializations if none provided
            employee.setSpecializations(new HashSet<>());
        }

        // Save replaced employee
        Employee replacedEmployee = employeeRepository.save(employee);

        // Return DTO response
        return employeeMapper.toEmployeeInfoResponse(replacedEmployee);
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
}
