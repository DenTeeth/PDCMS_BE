package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.enums.EmploymentType;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.work_shift.WorkShiftNotFoundException;
import com.dental.clinic.management.exception.employee_shift.RelatedResourceNotFoundException;
import com.dental.clinic.management.exception.fixed_registration.DuplicateFixedShiftRegistrationException;
import com.dental.clinic.management.exception.fixed_registration.FixedRegistrationNotFoundException;
import com.dental.clinic.management.exception.fixed_registration.InvalidEmployeeTypeException;
import com.dental.clinic.management.working_schedule.domain.FixedRegistrationDay;
import com.dental.clinic.management.working_schedule.domain.FixedShiftRegistration;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.dto.request.CreateFixedRegistrationRequest;
import com.dental.clinic.management.working_schedule.dto.request.UpdateFixedRegistrationRequest;
import com.dental.clinic.management.working_schedule.dto.response.FixedRegistrationResponse;
import com.dental.clinic.management.working_schedule.repository.FixedShiftRegistrationRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing fixed shift registrations (Full-Time & Part-Time Fixed
 * employees).
 * Schema V14 - LuÃ¡Â»â€œng 1: LÃ¡Â»â€¹ch CÃ¡Â»â€˜ Ã„â€˜Ã¡Â»â€¹nh
 */
@Service
@Transactional(readOnly = true)
public class FixedShiftRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(FixedShiftRegistrationService.class);

    private final FixedShiftRegistrationRepository registrationRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;

    public FixedShiftRegistrationService(FixedShiftRegistrationRepository registrationRepository,
            EmployeeRepository employeeRepository,
            WorkShiftRepository workShiftRepository) {
        this.registrationRepository = registrationRepository;
        this.employeeRepository = employeeRepository;
        this.workShiftRepository = workShiftRepository;
    }

    /**
     * Create a fixed shift registration for an employee.
     *
     * @param request creation request
     * @return created registration details
     */
    @PreAuthorize("hasAuthority('MANAGE_FIXED_REGISTRATIONS')")
    @Transactional
    public FixedRegistrationResponse createFixedRegistration(CreateFixedRegistrationRequest request) {

        log.info("Creating fixed registration for employee: {}", request.getEmployeeId());

        // 1. Validate employee exists and get employee_type
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RelatedResourceNotFoundException("Nhân viên không tồn tại"));

        // 2. Check employee type - ONLY allow FULL_TIME and PART_TIME_FIXED
        EmploymentType empType = employee.getEmploymentType();
        if (empType != EmploymentType.FULL_TIME && empType != EmploymentType.PART_TIME_FIXED) {
            throw new InvalidEmployeeTypeException();
        }

        // 3. Validate work shift exists
        WorkShift workShift = workShiftRepository.findById(request.getWorkShiftId())
                .orElseThrow(
                        () -> new RelatedResourceNotFoundException("Ca làm việc không tồn tại"));

        // 4. Validate effectiveFrom is not in the past
        if (request.getEffectiveFrom().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được là quá khứ");
        }

        // 5. Validate daysOfWeek
        if (request.getDaysOfWeek() == null || request.getDaysOfWeek().isEmpty()) {
            throw new IllegalArgumentException(
                    "Danh sách ngày làm việc không được rỗng");
        }

        // Validate day of week values (1=Monday, 7=Sunday)
        for (Integer day : request.getDaysOfWeek()) {
            if (day < 1 || day > 7) {
                throw new IllegalArgumentException(
                        "Ngày làm việc phải từ 1 (Thứ 2) đến 7 (Chủ nhật): "
                                + day);
            }
        }

        // 6. Check for duplicate registration (same employee + same work shift +
        // active)
        boolean duplicate = registrationRepository.existsActiveByEmployeeAndWorkShift(
                request.getEmployeeId(), request.getWorkShiftId());

        if (duplicate) {
            throw new DuplicateFixedShiftRegistrationException(workShift.getShiftName());
        }

        // 7. Create registration
        FixedShiftRegistration registration = new FixedShiftRegistration();
        registration.setEmployee(employee);
        registration.setWorkShift(workShift);
        registration.setEffectiveFrom(request.getEffectiveFrom());
        registration.setEffectiveTo(request.getEffectiveTo());
        registration.setIsActive(true);

        // 8. Add registration days (convert Integer to String: 1->MONDAY, 2->TUESDAY,
        // etc.)
        for (Integer dayNumber : request.getDaysOfWeek()) {
            FixedRegistrationDay day = new FixedRegistrationDay();
            day.setDayOfWeek(convertDayNumberToString(dayNumber));
            registration.addDay(day);
        }

        // 9. Save registration
        FixedShiftRegistration saved = registrationRepository.save(registration);
        log.info("Created fixed registration: {} for employee: {}", saved.getRegistrationId(),
                employee.getEmployeeId());

        return toResponse(saved);
    }

    /**
     * Get all fixed registrations for an employee.
     *
     * @param employeeId           employee ID (required for VIEW_ALL, auto-set for
     *                             VIEW_OWN)
     * @param currentEmployeeId    current user's employee ID
     * @param hasViewAllPermission whether user has VIEW_ALL permission
     * @return list of registrations
     */
    @PreAuthorize("hasAnyAuthority('VIEW_FIXED_REGISTRATIONS_ALL', 'VIEW_FIXED_REGISTRATIONS_OWN')")
    public List<FixedRegistrationResponse> getFixedRegistrations(
            Integer employeeId,
            Integer currentEmployeeId,
            boolean hasViewAllPermission,
            Boolean isActive) {

        List<FixedShiftRegistration> registrations;

        if (hasViewAllPermission) {
            // Admin/Manager can view all or filter by employeeId and isActive
            if (employeeId == null) {
                // View all registrations
                registrations = registrationRepository.findAllByActiveStatus(isActive);
            } else {
                // Filter by specific employee
                if (!employeeRepository.existsById(employeeId)) {
                    throw new RelatedResourceNotFoundException("Nhân viên không tồn tại");
                }
                registrations = registrationRepository.findByEmployeeIdAndActiveStatus(employeeId, isActive);
            }
        } else {
            // Regular employee: cannot provide employeeId, use JWT token's employeeId
            if (employeeId != null) {
                throw new AccessDeniedException(
                        "Bạn không thể chỉ định employeeId. Hãy thử nghiệm sẽ tự động lấy từ tài khoản của bạn.");
            }
            // Regular employees can only see their own active registrations
            registrations = registrationRepository.findActiveByEmployeeId(currentEmployeeId);
        }

        return registrations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update a fixed registration.
     *
     * @param registrationId registration ID
     * @param request        update request
     * @return updated registration details
     */
    @PreAuthorize("hasAuthority('MANAGE_FIXED_REGISTRATIONS')")
    @Transactional
    public FixedRegistrationResponse updateFixedRegistration(Integer registrationId,
            UpdateFixedRegistrationRequest request) {

        log.info("Updating fixed registration: {}", registrationId);

        // 1. Find registration
        FixedShiftRegistration registration = registrationRepository.findByIdWithDetails(registrationId)
                .orElseThrow(() -> new FixedRegistrationNotFoundException(registrationId));

        // 2. Check employee type - ONLY allow FULL_TIME and PART_TIME_FIXED
        EmploymentType empType = registration.getEmployee().getEmploymentType();
        if (empType != EmploymentType.FULL_TIME && empType != EmploymentType.PART_TIME_FIXED) {
            throw new InvalidEmployeeTypeException();
        }

        // 3. Update work shift if provided
        if (request.getWorkShiftId() != null) {
            WorkShift newWorkShift = workShiftRepository.findById(request.getWorkShiftId())
                    .orElseThrow(() -> new WorkShiftNotFoundException(request.getWorkShiftId()));
            registration.setWorkShift(newWorkShift);
        }

        // 4. Update effective dates if provided
        if (request.getEffectiveFrom() != null) {
            registration.setEffectiveFrom(request.getEffectiveFrom());
        }
        if (request.getEffectiveTo() != null) {
            registration.setEffectiveTo(request.getEffectiveTo());
        }

        // 5. Update days if provided
        if (request.getDaysOfWeek() != null && !request.getDaysOfWeek().isEmpty()) {
            // Validate day numbers
            for (Integer day : request.getDaysOfWeek()) {
                if (day < 1 || day > 7) {
                    throw new IllegalArgumentException(
                            "Ngày làm việc phải từ 1 (Thứ 2) đến 7 (Chủ nhật): "
                                    + day);
                }
            }

            // Clear old days and add new ones
            registration.clearDays();
            for (Integer dayNumber : request.getDaysOfWeek()) {
                FixedRegistrationDay day = new FixedRegistrationDay();
                day.setDayOfWeek(convertDayNumberToString(dayNumber));
                registration.addDay(day);
            }
        }

        // 6. Save changes
        FixedShiftRegistration updated = registrationRepository.save(registration);
        log.info("Updated fixed registration: {}", registrationId);

        return toResponse(updated);
    }

    /**
     * Delete (soft delete) a fixed registration.
     *
     * @param registrationId registration ID
     */
    @PreAuthorize("hasAuthority('MANAGE_FIXED_REGISTRATIONS')")
    @Transactional
    public void deleteFixedRegistration(Integer registrationId) {

        log.info("Deleting fixed registration: {}", registrationId);

        // 1. Find registration
        FixedShiftRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new FixedRegistrationNotFoundException(registrationId));

        // 2. Soft delete: set is_active = false, effective_to = now
        registration.setIsActive(false);
        registration.setEffectiveTo(LocalDate.now());
        registration.setUpdatedAt(LocalDateTime.now());

        registrationRepository.save(registration);
        log.info("Deleted fixed registration: {}", registrationId);
    }

    /**
     * Convert entity to response DTO.
     */
    private FixedRegistrationResponse toResponse(FixedShiftRegistration registration) {
        List<Integer> daysOfWeek = registration.getRegistrationDays().stream()
                .map(day -> convertDayStringToNumber(day.getDayOfWeek()))
                .sorted()
                .collect(Collectors.toList());

        String employeeName = registration.getEmployee().getFirstName() + " " +
                registration.getEmployee().getLastName();

        return new FixedRegistrationResponse(
                registration.getRegistrationId(),
                registration.getEmployee().getEmployeeId(),
                employeeName,
                registration.getWorkShift().getWorkShiftId(),
                registration.getWorkShift().getShiftName(),
                daysOfWeek,
                registration.getEffectiveFrom(),
                registration.getEffectiveTo(),
                registration.getIsActive());
    }

    /**
     * Convert day number to day string (1=MONDAY, 2=TUESDAY, ..., 7=SUNDAY).
     */
    private String convertDayNumberToString(Integer dayNumber) {
        return switch (dayNumber) {
            case 1 -> "MONDAY";
            case 2 -> "TUESDAY";
            case 3 -> "WEDNESDAY";
            case 4 -> "THURSDAY";
            case 5 -> "FRIDAY";
            case 6 -> "SATURDAY";
            case 7 -> "SUNDAY";
            default -> throw new IllegalArgumentException("Invalid day number: " + dayNumber);
        };
    }

    /**
     * Convert day string to day number (MONDAY=1, TUESDAY=2, ..., SUNDAY=7).
     */
    private Integer convertDayStringToNumber(String dayString) {
        return switch (dayString) {
            case "MONDAY" -> 1;
            case "TUESDAY" -> 2;
            case "WEDNESDAY" -> 3;
            case "THURSDAY" -> 4;
            case "FRIDAY" -> 5;
            case "SATURDAY" -> 6;
            case "SUNDAY" -> 7;
            default -> throw new IllegalArgumentException("Invalid day string: " + dayString);
        };
    }
}
