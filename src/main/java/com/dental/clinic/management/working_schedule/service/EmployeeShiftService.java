package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.exception.employee_shift.CannotCancelBatchShiftException;
import com.dental.clinic.management.exception.employee_shift.CannotCancelCompletedShiftException;
import com.dental.clinic.management.exception.employee_shift.ExceedsMaxHoursException;
import com.dental.clinic.management.exception.employee_shift.HolidayConflictException;
import com.dental.clinic.management.exception.employee_shift.InvalidStatusTransitionException;
import com.dental.clinic.management.exception.employee_shift.PastDateNotAllowedException;
import com.dental.clinic.management.exception.employee_shift.RelatedResourceNotFoundException;
import com.dental.clinic.management.exception.employee_shift.ShiftFinalizedException;
import com.dental.clinic.management.exception.employee_shift.ShiftNotFoundException;
import com.dental.clinic.management.exception.employee_shift.TimeOverlapConflictException;
import com.dental.clinic.management.utils.IdGenerator;
import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.dto.request.CreateShiftRequestDto;
import com.dental.clinic.management.working_schedule.dto.request.UpdateShiftRequestDto;
import com.dental.clinic.management.working_schedule.dto.response.EmployeeShiftResponseDto;
import com.dental.clinic.management.working_schedule.dto.response.ShiftSummaryResponseDto;
import com.dental.clinic.management.working_schedule.enums.ShiftSource;
import com.dental.clinic.management.working_schedule.enums.ShiftStatus;
import com.dental.clinic.management.working_schedule.mapper.EmployeeShiftMapper;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import com.dental.clinic.management.working_schedule.repository.HolidayDateRepository;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing employee shifts.
 * Handles shift creation, updates, cancellation, and calendar queries.
 */
@Service
@Transactional(readOnly = true)
public class EmployeeShiftService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeShiftService.class);

    private final EmployeeShiftRepository employeeShiftRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final HolidayDateRepository holidayDateRepository;
    private final EmployeeShiftMapper employeeShiftMapper;
    private final IdGenerator idGenerator;

    public EmployeeShiftService(EmployeeShiftRepository employeeShiftRepository,
            EmployeeRepository employeeRepository,
            WorkShiftRepository workShiftRepository,
            HolidayDateRepository holidayDateRepository,
            EmployeeShiftMapper employeeShiftMapper,
            IdGenerator idGenerator) {
        this.employeeShiftRepository = employeeShiftRepository;
        this.employeeRepository = employeeRepository;
        this.workShiftRepository = workShiftRepository;
        this.holidayDateRepository = holidayDateRepository;
        this.employeeShiftMapper = employeeShiftMapper;
        this.idGenerator = idGenerator;
    }

    /**
     * Get shift calendar for an employee with optional filters.
     *
     * @param employeeId           employee ID to view shifts for (null = all
     *                             employees if has permission)
     * @param startDate            start date filter
     * @param endDate              end date filter
     * @param status               optional status filter
     * @param currentEmployeeId    ID of the authenticated user
     * @param hasViewAllPermission whether user has VIEW_SHIFTS_ALL permission
     * @param pageable             pagination parameters
     * @return paginated list of shifts
     */
    @PreAuthorize("hasAnyAuthority('VIEW_SHIFTS_ALL', 'VIEW_SHIFTS_OWN')")
    public Page<EmployeeShiftResponseDto> getShiftCalendar(
            Integer employeeId,
            LocalDate startDate,
            LocalDate endDate,
            ShiftStatus status,
            Integer currentEmployeeId,
            boolean hasViewAllPermission,
            Pageable pageable) {

        // Check permission: user can only view their own shifts unless they have
        // VIEW_SHIFTS_ALL
        if (!hasViewAllPermission && !employeeId.equals(currentEmployeeId)) {
            throw new RelatedResourceNotFoundException(
                    "BÃ¡ÂºÂ¡n chÃ¡Â»â€° cÃƒÂ³ thÃ¡Â»Æ’ xem lÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c cÃ¡Â»Â§a chÃƒÂ­nh mÃƒÂ¬nh");
        }

        // Query shifts with filters
        List<EmployeeShift> allShifts;
        if (employeeId != null) {
            // Get shifts for specific employee
            allShifts = employeeShiftRepository.findByEmployeeAndDateRange(
                    employeeId, startDate, endDate);
        } else {
            // Get all shifts in date range (only allowed with VIEW_SHIFTS_ALL)
            if (!hasViewAllPermission) {
                throw new RelatedResourceNotFoundException(
                        "BÃ¡ÂºÂ¡n chÃ¡Â»â€° cÃƒÂ³ thÃ¡Â»Æ’ xem lÃ¡Â»â€¹ch lÃƒÂ m viÃ¡Â»â€¡c cÃ¡Â»Â§a chÃƒÂ­nh mÃƒÂ¬nh");
            }
            allShifts = employeeShiftRepository.findByDateRangeAndStatus(startDate, endDate, null);
        }

        // Apply status filter if provided
        if (status != null) {
            allShifts = allShifts.stream()
                    .filter(shift -> shift.getStatus() == status)
                    .collect(Collectors.toList());
        }

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allShifts.size());
        List<EmployeeShift> pageShifts = allShifts.subList(start, end);

        // Convert to DTOs
        List<EmployeeShiftResponseDto> dtoList = pageShifts.stream()
                .map(employeeShiftMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, allShifts.size());
    }

    /**
     * Get shift summary for an employee grouped by date.
     *
     * @param employeeId employee ID (null = all employees if has permission)
     * @param startDate  start date
     * @param endDate    end date
     * @return list of daily shift summaries
     */
    @PreAuthorize("hasAuthority('VIEW_SHIFTS_SUMMARY')")
    public List<ShiftSummaryResponseDto> getShiftSummary(
            Integer employeeId,
            LocalDate startDate,
            LocalDate endDate) {

        // Verify employee exists if employeeId is provided
        if (employeeId != null && !employeeRepository.existsById(employeeId)) {
            throw new RelatedResourceNotFoundException("NhÃƒÂ¢n viÃƒÂªn khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i");
        }

        // Get all shifts in date range
        List<EmployeeShift> shifts;
        if (employeeId != null) {
            // Get shifts for specific employee
            shifts = employeeShiftRepository.findByEmployeeAndDateRange(
                    employeeId, startDate, endDate);
        } else {
            // Get all shifts (for all employees)
            shifts = employeeShiftRepository.findByDateRangeAndStatus(startDate, endDate, null);
        }

        // Group by date
        Map<LocalDate, List<EmployeeShift>> shiftsByDate = shifts.stream()
                .collect(Collectors.groupingBy(EmployeeShift::getWorkDate));

        // Build summary for each date
        return shiftsByDate.entrySet().stream()
                .map(entry -> {
                    // Group shifts by status
                    Map<ShiftStatus, Long> statusBreakdown = entry.getValue().stream()
                            .collect(Collectors.groupingBy(
                                    EmployeeShift::getStatus,
                                    Collectors.counting()));

                    return ShiftSummaryResponseDto.builder()
                            .workDate(entry.getKey())
                            .totalShifts((long) entry.getValue().size())
                            .statusBreakdown(statusBreakdown)
                            .build();
                })
                .sorted(Comparator.comparing(ShiftSummaryResponseDto::getWorkDate))
                .collect(Collectors.toList());
    }

    /**
     * Get detailed information about a specific shift.
     *
     * @param employeeShiftId      shift ID
     * @param currentEmployeeId    ID of the authenticated user
     * @param hasViewAllPermission whether user has VIEW_SHIFTS_ALL permission
     * @return shift details
     */
    @PreAuthorize("hasAnyAuthority('VIEW_SHIFTS_ALL', 'VIEW_SHIFTS_OWN')")
    public EmployeeShiftResponseDto getShiftDetail(
            String employeeShiftId,
            Integer currentEmployeeId,
            boolean hasViewAllPermission) {

        // Find the shift
        EmployeeShift shift = employeeShiftRepository.findById(employeeShiftId)
                .orElseThrow(
                        () -> new ShiftNotFoundException(
                                "KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y ca lÃƒÂ m viÃ¡Â»â€¡c, hoÃ¡ÂºÂ·c bÃ¡ÂºÂ¡n khÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân xem."));

        // Check permission: user can only view their own shifts unless they have
        // VIEW_SHIFTS_ALL
        if (!hasViewAllPermission && !shift.getEmployee().getEmployeeId().equals(currentEmployeeId)) {
            throw new ShiftNotFoundException(
                    "KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y ca lÃƒÂ m viÃ¡Â»â€¡c, hoÃ¡ÂºÂ·c bÃ¡ÂºÂ¡n khÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân xem.");
        }

        return employeeShiftMapper.toResponseDto(shift);
    }

    /**
     * Create a manual shift entry.
     *
     * @param request   shift creation request
     * @param createdBy ID of the user creating the shift
     * @return created shift details
     */
    @PreAuthorize("hasAuthority('CREATE_SHIFTS')")
    @Transactional
    public EmployeeShiftResponseDto createManualShift(CreateShiftRequestDto request, Integer createdBy) {

        // Validate employee exists
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RelatedResourceNotFoundException("NhÃƒÂ¢n viÃƒÂªn khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i"));

        // Validate work shift exists
        WorkShift workShift = workShiftRepository.findById(request.getWorkShiftId())
                .orElseThrow(
                        () -> new RelatedResourceNotFoundException("Ca lÃƒÂ m viÃ¡Â»â€¡c khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i"));

        // Validate shift creation (check for conflicts)
        validateShiftCreation(employee.getEmployeeId(), request.getWorkDate(), workShift.getWorkShiftId());

        // Generate shift ID
        String employeeShiftId = idGenerator.generateId("EMS");

        // Create new shift
        EmployeeShift newShift = new EmployeeShift();
        newShift.setEmployeeShiftId(employeeShiftId);
        newShift.setEmployee(employee);
        newShift.setWorkShift(workShift);
        newShift.setWorkDate(request.getWorkDate());
        newShift.setStatus(ShiftStatus.SCHEDULED);
        newShift.setSource(ShiftSource.MANUAL_ENTRY);
        newShift.setIsOvertime(false);
        newShift.setCreatedBy(createdBy);
        newShift.setNotes(request.getNotes());

        // Save shift
        EmployeeShift savedShift = employeeShiftRepository.save(newShift);
        log.info("Created manual shift: {} for employee: {}", employeeShiftId, employee.getEmployeeId());

        return employeeShiftMapper.toResponseDto(savedShift);
    }

    /**
     * Update an existing shift.
     *
     * @param employeeShiftId shift ID to update
     * @param request         update request
     * @return updated shift details
     */
    @PreAuthorize("hasAuthority('UPDATE_SHIFTS')")
    @Transactional
    public EmployeeShiftResponseDto updateShift(String employeeShiftId, UpdateShiftRequestDto request) {

        // Find the shift
        EmployeeShift shift = employeeShiftRepository.findById(employeeShiftId)
                .orElseThrow(() -> new ShiftNotFoundException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y ca lÃƒÂ m viÃ¡Â»â€¡c"));

        // Check if shift is finalized (cannot update completed or cancelled shifts)
        if (shift.getStatus() == ShiftStatus.COMPLETED || shift.getStatus() == ShiftStatus.CANCELLED) {
            throw new ShiftFinalizedException(
                    "KhÃƒÂ´ng thÃ¡Â»Æ’ cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t ca lÃƒÂ m Ã„â€˜ÃƒÂ£ hoÃƒÂ n thÃƒÂ nh hoÃ¡ÂºÂ·c Ã„â€˜ÃƒÂ£ bÃ¡Â»â€¹ hÃ¡Â»Â§y.");
        }

        // Update status if provided and valid
        if (request.getStatus() != null) {
            // Cannot manually set status to ON_LEAVE
            if (request.getStatus() == ShiftStatus.ON_LEAVE) {
                throw new InvalidStatusTransitionException(
                        "KhÃƒÂ´ng thÃ¡Â»Æ’ cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t thÃ¡Â»Â§ cÃƒÂ´ng trÃ¡ÂºÂ¡ng thÃƒÂ¡i thÃƒÂ nh ON_LEAVE. Vui lÃƒÂ²ng tÃ¡ÂºÂ¡o yÃƒÂªu cÃ¡ÂºÂ§u nghÃ¡Â»â€° phÃƒÂ©p.");
            }
            validateStatusTransition(shift.getStatus(), request.getStatus());
            shift.setStatus(request.getStatus());
        }

        // Update notes if provided
        if (request.getNotes() != null) {
            shift.setNotes(request.getNotes());
        }

        // Save changes
        EmployeeShift updatedShift = employeeShiftRepository.save(shift);
        log.info("Updated shift: {} to status: {}", employeeShiftId, updatedShift.getStatus());

        return employeeShiftMapper.toResponseDto(updatedShift);
    }

    /**
     * Cancel a shift.
     *
     * @param employeeShiftId shift ID to cancel
     */
    @PreAuthorize("hasAuthority('DELETE_SHIFTS')")
    @Transactional
    public void cancelShift(String employeeShiftId) {

        // Find the shift
        EmployeeShift shift = employeeShiftRepository.findById(employeeShiftId)
                .orElseThrow(() -> new ShiftNotFoundException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y ca lÃƒÂ m viÃ¡Â»â€¡c"));

        // Validate cancellation is allowed
        if (shift.getStatus() == ShiftStatus.COMPLETED) {
            throw new CannotCancelCompletedShiftException(
                    "KhÃƒÂ´ng thÃ¡Â»Æ’ hÃ¡Â»Â§y ca lÃƒÂ m Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c hoÃƒÂ n thÃƒÂ nh.");
        }

        // Check if already cancelled (idempotency)
        if (shift.getStatus() == ShiftStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "Ca lÃƒÂ m viÃ¡Â»â€¡c nÃƒÂ y Ã„â€˜ÃƒÂ£ bÃ¡Â»â€¹ hÃ¡Â»Â§y trÃ†Â°Ã¡Â»â€ºc Ã„â€˜ÃƒÂ³.");
        }

        if (shift.getSource() == ShiftSource.BATCH_JOB || shift.getSource() == ShiftSource.REGISTRATION_JOB) {
            throw new CannotCancelBatchShiftException(
                    "KhÃƒÂ´ng thÃ¡Â»Æ’ hÃ¡Â»Â§y ca lÃƒÂ m mÃ¡ÂºÂ·c Ã„â€˜Ã¡Â»â€¹nh cÃ¡Â»Â§a nhÃƒÂ¢n viÃƒÂªn Full-time. Vui lÃƒÂ²ng tÃ¡ÂºÂ¡o yÃƒÂªu cÃ¡ÂºÂ§u nghÃ¡Â»â€° phÃƒÂ©p.");
        }

        // Cancel the shift
        shift.setStatus(ShiftStatus.CANCELLED);
        employeeShiftRepository.save(shift);
        log.info("Cancelled shift: {}", employeeShiftId);
    }

    /**
     * Validate that a shift can be created without conflicts.
     *
     * @param employeeId  employee ID
     * @param workDate    work date
     * @param workShiftId work shift ID
     */
    private void validateShiftCreation(Integer employeeId, LocalDate workDate, String workShiftId) {

        // Check if work date is in the past
        if (workDate.isBefore(LocalDate.now())) {
            throw new PastDateNotAllowedException(workDate);
        }

        // Check if work date is a holiday
        if (holidayDateRepository.isHoliday(workDate)) {
            throw new HolidayConflictException(workDate);
        }

        // Get the new shift details
        WorkShift newWorkShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(
                        () -> new RelatedResourceNotFoundException("Ca lÃƒÂ m viÃ¡Â»â€¡c khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i"));

        // Get all active shifts for this employee on this date
        List<EmployeeShift> existingShifts = employeeShiftRepository.findActiveShiftsByEmployeeAndDate(
                employeeId, workDate);

        // Calculate new shift duration in minutes
        long newShiftMinutes = Duration.between(newWorkShift.getStartTime(), newWorkShift.getEndTime()).toMinutes();

        // Calculate total existing hours
        long existingTotalMinutes = 0;
        for (EmployeeShift existingShift : existingShifts) {
            WorkShift existingWorkShift = existingShift.getWorkShift();

            // Check if time ranges overlap
            if (isTimeOverlap(newWorkShift.getStartTime(), newWorkShift.getEndTime(),
                    existingWorkShift.getStartTime(), existingWorkShift.getEndTime())) {
                throw new TimeOverlapConflictException(
                        newWorkShift.getStartTime(), newWorkShift.getEndTime(),
                        existingWorkShift.getStartTime(), existingWorkShift.getEndTime());
            }

            // Calculate existing shift duration
            long shiftMinutes = Duration.between(existingWorkShift.getStartTime(),
                    existingWorkShift.getEndTime()).toMinutes();
            existingTotalMinutes += shiftMinutes;
        }

        // Check if total hours would exceed 8-hour limit
        long totalMinutes = newShiftMinutes + existingTotalMinutes;
        long totalHours = totalMinutes / 60;

        if (totalHours > 8) {
            throw new ExceedsMaxHoursException(workDate, (int) totalHours);
        }
    }

    /**
     * Check if two time ranges overlap.
     *
     * @param start1 start time of first range
     * @param end1   end time of first range
     * @param start2 start time of second range
     * @param end2   end time of second range
     * @return true if ranges overlap
     */
    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Two time ranges overlap if:
        // - start1 < end2 AND end1 > start2
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * Validate status transition is allowed.
     *
     * @param currentStatus current shift status
     * @param newStatus     new status to transition to
     */
    private void validateStatusTransition(ShiftStatus currentStatus, ShiftStatus newStatus) {

        // Cannot change completed shifts
        if (currentStatus == ShiftStatus.COMPLETED) {
            throw new InvalidStatusTransitionException(
                    "KhÃƒÂ´ng thÃ¡Â»Æ’ thay Ã„â€˜Ã¡Â»â€¢i trÃ¡ÂºÂ¡ng thÃƒÂ¡i cÃ¡Â»Â§a ca lÃƒÂ m viÃ¡Â»â€¡c Ã„â€˜ÃƒÂ£ hoÃƒÂ n thÃƒÂ nh");
        }

        // Cannot change cancelled shifts
        if (currentStatus == ShiftStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "KhÃƒÂ´ng thÃ¡Â»Æ’ thay Ã„â€˜Ã¡Â»â€¢i trÃ¡ÂºÂ¡ng thÃƒÂ¡i cÃ¡Â»Â§a ca lÃƒÂ m viÃ¡Â»â€¡c Ã„â€˜ÃƒÂ£ hÃ¡Â»Â§y");
        }
    }
}
