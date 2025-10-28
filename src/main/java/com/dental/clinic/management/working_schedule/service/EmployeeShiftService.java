package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.working_schedule.dto.request.CreateShiftRequestDto;
import com.dental.clinic.management.working_schedule.dto.request.UpdateShiftRequestDto;
import com.dental.clinic.management.working_schedule.dto.response.EmployeeShiftResponseDto;
import com.dental.clinic.management.working_schedule.mapper.EmployeeShiftMapper;
import com.dental.clinic.management.working_schedule.repository.EmployeeShiftRepository;
import com.dental.clinic.management.exception.shift.*;
import com.dental.clinic.management.exception.EmployeeNotFoundException;
import com.dental.clinic.management.exception.InvalidDateRangeException;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.working_schedule.domain.WorkShift;
import com.dental.clinic.management.working_schedule.repository.WorkShiftRepository;
import com.dental.clinic.management.working_schedule.domain.EmployeeShift;
import com.dental.clinic.management.working_schedule.enums.EmployeeShiftStatus;
import com.dental.clinic.management.working_schedule.enums.ShiftSource;
import com.dental.clinic.management.working_schedule.utils.EmployeeShiftValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing employee shifts.
 * Implements all business logic and validation rules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeShiftService {

    private final EmployeeShiftRepository employeeShiftRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeShiftMapper employeeShiftMapper;

    /**
     * Get shifts for calendar view.
     * 
     * @param startDate  start date (inclusive)
     * @param endDate    end date (inclusive)
     * @param employeeId optional employee ID filter
     * @return List of EmployeeShiftResponseDto
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('VIEW_SHIFTS_ALL', 'VIEW_SHIFTS_OWN')")
    public List<EmployeeShiftResponseDto> getShiftsForCalendarView(
            LocalDate startDate,
            LocalDate endDate,
            Integer employeeId) {

        log.info("Fetching shifts for calendar view: startDate={}, endDate={}, employeeId={}",
                startDate, endDate, employeeId);

        // Validate date range
        if (startDate == null || endDate == null) {
            throw new InvalidDateRangeException(
                    "Vui lòng cung cấp ngày bắt đầu và ngày kết thúc hợp lệ.");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(
                    "Vui lòng cung cấp ngày bắt đầu và ngày kết thúc hợp lệ.");
        }

        // Get current user's authorities and employee info
        List<String> authorities = SecurityUtil.getCurrentUserAuthorities();
        boolean hasViewAll = authorities.contains("VIEW_SHIFTS_ALL");
        boolean hasViewOwn = authorities.contains("VIEW_SHIFTS_OWN");

        if (!hasViewAll && !hasViewOwn) {
            throw new ForbiddenAccessException(
                    "Không tìm thấy tài nguyên hoặc bạn không có quyền truy cập.");
        }

        // Get current employee
        Employee currentEmployee = getCurrentEmployee();

        // Apply authorization logic
        Integer targetEmployeeId;
        if (hasViewAll) {
            // User has VIEW_SHIFTS_ALL: allow optional filter
            targetEmployeeId = employeeId; // Can be null (view all)
        } else {
            // User ONLY has VIEW_SHIFTS_OWN
            if (employeeId != null && !employeeId.equals(currentEmployee.getEmployeeId())) {
                // Trying to view someone else's shifts
                throw new ForbiddenAccessException(
                        "Không tìm thấy tài nguyên hoặc bạn không có quyền truy cập.");
            }
            // Force filter to current user
            targetEmployeeId = currentEmployee.getEmployeeId();
        }

        // Fetch shifts based on filter
        List<EmployeeShift> shifts;
        if (targetEmployeeId != null) {
            shifts = employeeShiftRepository.findByEmployee_EmployeeIdAndWorkDateBetween(
                    targetEmployeeId, startDate, endDate);
        } else {
            shifts = employeeShiftRepository.findByWorkDateBetween(startDate, endDate);
        }

        log.info("Found {} shifts for the specified period", shifts.size());

        return shifts.stream()
                .map(employeeShiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get shift details by ID.
     * 
     * @param shiftId employee shift ID
     * @return EmployeeShiftResponseDto
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('VIEW_SHIFTS_ALL', 'VIEW_SHIFTS_OWN')")
    public EmployeeShiftResponseDto getShiftDetails(String shiftId) {
        log.info("Fetching shift details for ID: {}", shiftId);

        // Get current user's authorities
        List<String> authorities = SecurityUtil.getCurrentUserAuthorities();
        boolean hasViewAll = authorities.contains("VIEW_SHIFTS_ALL");
        boolean hasViewOwn = authorities.contains("VIEW_SHIFTS_OWN");

        if (!hasViewAll && !hasViewOwn) {
            throw new ForbiddenAccessException(
                    "Không tìm thấy tài nguyên hoặc bạn không có quyền truy cập.");
        }

        // Find the shift
        EmployeeShift shift = employeeShiftRepository.findById(shiftId)
                .orElseThrow(() -> new EmployeeShiftNotFoundException(
                        "Không tìm thấy ca làm việc, hoặc bạn không có quyền xem."));

        // If user ONLY has VIEW_SHIFTS_OWN, verify ownership
        if (!hasViewAll && hasViewOwn) {
            Employee currentEmployee = getCurrentEmployee();
            if (!shift.getEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId())) {
                // Don't reveal that the shift exists
                throw new EmployeeShiftNotFoundException(
                        "Không tìm thấy ca làm việc, hoặc bạn không có quyền xem.");
            }
        }

        return employeeShiftMapper.toResponse(shift);
    }

    /**
     * Create a manual shift entry.
     * 
     * @param requestDto CreateShiftRequestDto
     * @return EmployeeShiftResponseDto
     */
    @Transactional
    @PreAuthorize("hasAuthority('CREATE_SHIFTS')")
    public EmployeeShiftResponseDto createManualShift(CreateShiftRequestDto requestDto) {
        log.info("Creating manual shift for employee: {}, date: {}, shift: {}",
                requestDto.getEmployeeId(), requestDto.getWorkDate(), requestDto.getWorkShiftId());

        // Validate employee exists
        Employee employee = employeeRepository.findById(requestDto.getEmployeeId())
                .orElse(null);

        // Validate work shift exists
        WorkShift workShift = workShiftRepository.findById(requestDto.getWorkShiftId())
                .orElse(null);
        
        // If either employee or work shift is not found, throw single exception
        if (employee == null || workShift == null) {
            throw new RelatedResourceNotFoundException(
                    "Nhân viên hoặc Mẫu ca làm việc không tồn tại.");
        }

        // Check if work date is a holiday (simple check: Sunday = holiday)
        if (EmployeeShiftValidator.isHoliday(requestDto.getWorkDate())) {
            throw new HolidayShiftException(
                    "Không thể tạo ca làm vào ngày nghỉ lễ. Vui lòng sử dụng quy trình OT.");
        }

        // Check for existing shift on same date and same work shift
        boolean shiftExists = employeeShiftRepository.existsByEmployee_EmployeeIdAndWorkShift_WorkShiftIdAndWorkDate(
                requestDto.getEmployeeId(),
                requestDto.getWorkShiftId(),
                requestDto.getWorkDate());

        if (shiftExists) {
            throw new ShiftConflictException(
                    "Nhân viên đã có lịch làm việc vào ngày và ca này.");
        }

        // Create new shift
        EmployeeShift shift = employeeShiftMapper.toEntity(requestDto);
        shift.setEmployee(employee);
        shift.setWorkShift(workShift);
        shift.setSource(ShiftSource.MANUAL_ENTRY);
        shift.setStatus(EmployeeShiftStatus.SCHEDULED);

        // Generate ID in format: EMS + yyMMdd + SEQ (3 digits)
        shift.setId(generateEmployeeShiftId(requestDto.getWorkDate()));

        // Set creator ID from current authenticated user (if employee exists)
        try {
            Employee currentEmployee = getCurrentEmployee();
            shift.setCreatedBy(currentEmployee.getEmployeeId());
        } catch (EmployeeNotFoundException e) {
            // User is authenticated but not an employee (e.g., super admin)
            // createdBy will be null
            log.warn("Current user is not an employee. createdBy will be null.");
        }

        EmployeeShift savedShift = employeeShiftRepository.save(shift);

        log.info("Successfully created manual shift with ID: {}", savedShift.getId());

        return employeeShiftMapper.toResponse(savedShift);
    }

    /**
     * Update an existing shift (partial update).
     * 
     * @param shiftId    employee shift ID
     * @param requestDto UpdateShiftRequestDto
     * @return EmployeeShiftResponseDto
     */
    @Transactional
    @PreAuthorize("hasAuthority('UPDATE_SHIFTS')")
    public EmployeeShiftResponseDto updateShift(String shiftId, UpdateShiftRequestDto requestDto) {
        log.info("Updating shift: {}", shiftId);

        // Find the shift
        EmployeeShift shift = employeeShiftRepository.findById(shiftId)
                .orElseThrow(() -> new EmployeeShiftNotFoundException(
                        "Không tìm thấy ca làm việc này."));

        // Check current status - cannot update COMPLETED or CANCELLED shifts
        if (shift.getStatus() == EmployeeShiftStatus.COMPLETED ||
                shift.getStatus() == EmployeeShiftStatus.CANCELLED) {
            throw new ShiftUpdateNotAllowedException(
                    "Không thể cập nhật ca làm đã hoàn thành hoặc đã bị hủy.");
        }

        // Validate incoming status - cannot manually set to ON_LEAVE
        if (requestDto.getStatus() == EmployeeShiftStatus.ON_LEAVE) {
            throw new InvalidStatusTransitionException(
                    "Không thể cập nhật thủ công trạng thái thành ON_LEAVE. Vui lòng tạo yêu cầu nghỉ phép.");
        }

        // Apply partial updates
        employeeShiftMapper.updateEntity(shift, requestDto);

        EmployeeShift updatedShift = employeeShiftRepository.save(shift);

        log.info("Successfully updated shift ID: {}", shiftId);

        return employeeShiftMapper.toResponse(updatedShift);
    }

    /**
     * Cancel a shift (soft delete).
     * 
     * @param shiftId employee shift ID
     */
    @Transactional
    @PreAuthorize("hasAuthority('DELETE_SHIFTS')")
    public void cancelShift(String shiftId) {
        log.info("Cancelling shift: {}", shiftId);

        // Find the shift
        EmployeeShift shift = employeeShiftRepository.findById(shiftId)
                .orElseThrow(() -> new EmployeeShiftNotFoundException(
                        "Không tìm thấy ca làm việc này."));

        // Cannot cancel BATCH_JOB shifts (default shifts for full-time employees)
        if (shift.getSource() == ShiftSource.BATCH_JOB) {
            throw new BatchJobShiftCancellationException(
                    "Không thể hủy ca làm mặc định của nhân viên Full-time. Vui lòng tạo yêu cầu nghỉ phép.");
        }

        // Cannot cancel already COMPLETED shifts
        if (shift.getStatus() == EmployeeShiftStatus.COMPLETED) {
            throw new CannotCancelCompletedException(
                    "Không thể hủy ca làm đã được hoàn thành.");
        }

        // Set status to CANCELLED
        shift.setStatus(EmployeeShiftStatus.CANCELLED);
        employeeShiftRepository.save(shift);

        log.info("Successfully cancelled shift: {}", shiftId);
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Get the current authenticated employee.
     */
    private Employee getCurrentEmployee() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new ForbiddenAccessException(
                        "Không tìm thấy tài nguyên hoặc bạn không có quyền truy cập."));

        return employeeRepository.findByAccount_Username(username)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found for current user: " + username));
    }

    /**
     * Generate employee shift ID in format: EMSyyMMddSEQ
     * Example: EMS251021001
     * 
     * @param workDate the work date
     * @return generated ID
     */
    private String generateEmployeeShiftId(LocalDate workDate) {
        // Format: EMS + yyMMdd
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String datePrefix = "EMS" + workDate.format(formatter);

        // Find all shifts with same date prefix to get next sequence number
        List<EmployeeShift> shiftsOnDate = employeeShiftRepository.findByWorkDate(workDate);

        // Get next sequence number (3 digits)
        int nextSeq = shiftsOnDate.size() + 1;
        String seqStr = String.format("%03d", nextSeq);

        // Check for collision and increment if needed
        String candidateId = datePrefix + seqStr;
        while (employeeShiftRepository.existsById(candidateId)) {
            nextSeq++;
            seqStr = String.format("%03d", nextSeq);
            candidateId = datePrefix + seqStr;
        }

        return candidateId;
    }
}
