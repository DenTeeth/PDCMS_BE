package com.dental.clinic.management.work_schedule.service;

import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.employee.domain.Employee;
import com.dental.clinic.management.employee.enums.EmploymentType;
import com.dental.clinic.management.employee.repository.EmployeeRepository;
import com.dental.clinic.management.work_schedule.domain.EmployeeShiftRegistration;
import com.dental.clinic.management.exception.EmployeeNotFoundException;
import com.dental.clinic.management.exception.InvalidEmploymentTypeException;
import com.dental.clinic.management.exception.InvalidRegistrationDateException;
import com.dental.clinic.management.exception.RegistrationConflictException;
import com.dental.clinic.management.exception.RegistrationNotFoundException;
import com.dental.clinic.management.exception.WorkShiftNotFoundException;
import com.dental.clinic.management.utils.IdGenerator;
import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.utils.security.SecurityUtil;
import com.dental.clinic.management.work_schedule.domain.RegistrationDays;
import com.dental.clinic.management.work_schedule.domain.RegistrationDaysId;
import com.dental.clinic.management.work_schedule.dto.request.CreateShiftRegistrationRequest;
import com.dental.clinic.management.work_schedule.dto.request.ReplaceShiftRegistrationRequest;
import com.dental.clinic.management.work_schedule.dto.request.UpdateShiftRegistrationRequest;
import com.dental.clinic.management.work_schedule.dto.response.ShiftRegistrationResponse;
import com.dental.clinic.management.work_schedule.enums.DayOfWeek;
import com.dental.clinic.management.work_schedule.mapper.ShiftRegistrationMapper;
import com.dental.clinic.management.work_schedule.repository.EmployeeShiftRegistrationRepository;
import com.dental.clinic.management.work_schedule.repository.RegistrationDaysRepository;
import com.dental.clinic.management.work_schedule.repository.WorkShiftRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing employee shift registrations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeShiftRegistrationService {

        private final EmployeeShiftRegistrationRepository registrationRepository;
        private final RegistrationDaysRepository registrationDaysRepository;
        private final EmployeeRepository employeeRepository;
        private final WorkShiftRepository workShiftRepository;
        private final AccountRepository accountRepository;
        private final ShiftRegistrationMapper shiftRegistrationMapper;
        private final IdGenerator idGenerator;

        /**
         * GET /api/v1/registrations
         * Xem danh sách đăng ký ca làm part-time.
         * - Admin hoặc VIEW_REGISTRATION_ALL: xem tất cả
         * - VIEW_REGISTRATION_OWN: chỉ xem của chính mình
         */
        @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_REGISTRATION_ALL + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_REGISTRATION_OWN + "')")
        public Page<ShiftRegistrationResponse> getAllRegistrations(Pageable pageable) {
                log.debug("Request to get all Employee Shift Registrations");

                // LUỒNG 1: Admin hoặc người dùng có quyền xem tất cả
                if (SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN) ||
                                SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.VIEW_REGISTRATION_ALL)) {

                        log.info("User has VIEW_REGISTRATION_ALL permission, fetching all registrations");
                        return registrationRepository.findAll(pageable)
                                        .map(shiftRegistrationMapper::toShiftRegistrationResponse);
                }
                // LUỒNG 2: Nhân viên chỉ có quyền VIEW_REGISTRATION_OWN
                else {
                        String username = SecurityUtil.getCurrentUserLogin()
                                        .orElseThrow(() -> new RuntimeException("User not authenticated"));

                        Integer employeeId = accountRepository.findOneByUsername(username)
                                        .map(account -> account.getEmployee().getEmployeeId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Employee not found for user: " + username));

                        log.info("User has VIEW_REGISTRATION_OWN permission, fetching registrations for employee_id: {}",
                                        employeeId);
                        return registrationRepository.findByEmployeeId(employeeId, pageable)
                                        .map(shiftRegistrationMapper::toShiftRegistrationResponse);
                }
        }

        /**
         * GET /api/v1/registrations/{registration_id}
         * Xem chi tiết một đăng ký ca làm.
         * - Phải có permission VIEW_REGISTRATION_ALL hoặc VIEW_REGISTRATION_OWN (và là
         * chủ sở hữu)
         * - 404 Not Found nếu đăng ký không tồn tại hoặc người dùng không có quyền xem
         */
        @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_REGISTRATION_ALL + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.VIEW_REGISTRATION_OWN + "')")
        public ShiftRegistrationResponse getRegistrationById(String registrationId) {
                log.debug("Request to get Employee Shift Registration: {}", registrationId);

                // LUỒNG 1: Admin hoặc người dùng có quyền xem tất cả
                if (SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN) ||
                                SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.VIEW_REGISTRATION_ALL)) {

                        log.info("User has VIEW_REGISTRATION_ALL permission, fetching registration: {}",
                                        registrationId);
                        return registrationRepository.findByRegistrationId(registrationId)
                                        .map(shiftRegistrationMapper::toShiftRegistrationResponse)
                                        .orElseThrow(() -> new RegistrationNotFoundException(registrationId));
                }
                // LUỒNG 2: Nhân viên chỉ có quyền VIEW_REGISTRATION_OWN (phải là chủ sở hữu)
                else {
                        String username = SecurityUtil.getCurrentUserLogin()
                                        .orElseThrow(() -> new RuntimeException("User not authenticated"));

                        Integer employeeId = accountRepository.findOneByUsername(username)
                                        .map(account -> account.getEmployee().getEmployeeId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Employee not found for user: " + username));

                        log.info("User has VIEW_REGISTRATION_OWN permission, fetching registration: {} for employee_id: {}",
                                        registrationId, employeeId);

                        // Tìm registration và check xem có phải của employee này không
                        return registrationRepository.findByRegistrationIdAndEmployeeId(registrationId, employeeId)
                                        .map(shiftRegistrationMapper::toShiftRegistrationResponse)
                                        .orElseThrow(() -> new RegistrationNotFoundException(registrationId,
                                                        "or you don't have permission to view it"));
                }
        }

        /**
         * POST /api/v1/registrations
         * Tạo đăng ký ca làm part-time mới.
         * - Chỉ nhân viên PART_TIME mới được tạo đăng ký
         * - Kiểm tra xung đột với các đăng ký đang hoạt động
         * - Kiểm tra ngày hiệu lực hợp lệ
         */
        @PreAuthorize("hasAuthority('" + AuthoritiesConstants.CREATE_REGISTRATION + "')")
        @Transactional
        public ShiftRegistrationResponse createRegistration(CreateShiftRegistrationRequest request) {
                log.debug("Request to create Employee Shift Registration: {}", request);

                // 1. Validate employee exists and is PART_TIME
                Employee employee = employeeRepository.findById(request.getEmployeeId())
                                .orElseThrow(() -> new EmployeeNotFoundException(request.getEmployeeId()));

                if (employee.getEmploymentType() != EmploymentType.PART_TIME) {
                        log.warn("Employee {} has employment type {}, but only PART_TIME employees can create registrations",
                                        employee.getEmployeeId(), employee.getEmploymentType());
                        throw new InvalidEmploymentTypeException(
                                        "Chỉ nhân viên PART_TIME mới được đăng ký ca làm. " +
                                                        "Nhân viên này có loại hợp đồng: "
                                                        + employee.getEmploymentType());
                }

                // 2. Validate work shift exists and is active
                workShiftRepository.findByWorkShiftIdAndIsActive(request.getWorkShiftId(), true)
                                .orElseThrow(() -> new WorkShiftNotFoundException(request.getWorkShiftId()));

                // 3. Validate dates
                LocalDate today = LocalDate.now();

                if (request.getEffectiveFrom().isBefore(today)) {
                        throw new InvalidRegistrationDateException(
                                        "Ngày bắt đầu hiệu lực không thể là quá khứ. Ngày bắt đầu: " +
                                                        request.getEffectiveFrom() + ", Ngày hiện tại: " + today);
                }

                if (request.getEffectiveTo() != null &&
                                request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
                        throw new InvalidRegistrationDateException(
                                        "Ngày kết thúc hiệu lực phải sau hoặc bằng ngày bắt đầu. " +
                                                        "Ngày bắt đầu: " + request.getEffectiveFrom() +
                                                        ", Ngày kết thúc: " + request.getEffectiveTo());
                }

                // 4. Check for conflicts with existing active registrations
                List<EmployeeShiftRegistration> conflicts = registrationRepository.findConflictingRegistrations(
                                request.getEmployeeId(),
                                request.getWorkShiftId(),
                                request.getDaysOfWeek());

                if (!conflicts.isEmpty()) {
                        EmployeeShiftRegistration conflict = conflicts.get(0);
                        String conflictingDays = conflict.getRegistrationDays().stream()
                                        .map(rd -> rd.getId().getDayOfWeek().toString())
                                        .reduce((a, b) -> a + ", " + b)
                                        .orElse("");

                        throw new RegistrationConflictException(
                                        String.format("Đã tồn tại đăng ký hoạt động cho nhân viên %d, ca %s vào các ngày: %s. "
                                                        +
                                                        "Registration ID: %s, Hiệu lực từ: %s đến: %s",
                                                        request.getEmployeeId(),
                                                        request.getWorkShiftId(),
                                                        conflictingDays,
                                                        conflict.getRegistrationId(),
                                                        conflict.getEffectiveFrom(),
                                                        conflict.getEffectiveTo() != null ? conflict.getEffectiveTo()
                                                                        : "vô thời hạn"));
                }

                // 5. Generate registration ID
                String registrationId = idGenerator.generateId("REG");
                log.info("Generated registration ID: {}", registrationId);

                // 6. Create and save EmployeeShiftRegistration
                EmployeeShiftRegistration registration = new EmployeeShiftRegistration();
                registration.setRegistrationId(registrationId);
                registration.setEmployeeId(request.getEmployeeId());
                registration.setSlotId(request.getWorkShiftId());
                registration.setEffectiveFrom(request.getEffectiveFrom());
                registration.setEffectiveTo(request.getEffectiveTo());
                registration.setIsActive(true);

                EmployeeShiftRegistration savedRegistration = registrationRepository.save(registration);
                log.info("Saved registration: {}", savedRegistration.getRegistrationId());

                // 7. Create and save RegistrationDays for each day of week
                List<RegistrationDays> registrationDaysList = new ArrayList<>();
                for (DayOfWeek dayOfWeek : request.getDaysOfWeek()) {
                        RegistrationDaysId dayId = new RegistrationDaysId(registrationId, dayOfWeek);
                        RegistrationDays registrationDay = new RegistrationDays(savedRegistration, dayId);
                        registrationDaysList.add(registrationDay);
                }

                registrationDaysRepository.saveAll(registrationDaysList);
                log.info("Saved {} registration days for registration {}", registrationDaysList.size(), registrationId);

                // 8. Load and return the complete registration with days
                EmployeeShiftRegistration completeRegistration = registrationRepository
                                .findByRegistrationId(registrationId)
                                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));

                return shiftRegistrationMapper.toShiftRegistrationResponse(completeRegistration);
        }

        /**
         * PATCH /api/v1/registrations/{registration_id}
         * Cập nhật một phần đăng ký ca làm.
         * - Admin hoặc UPDATE_REGISTRATION_ALL: cập nhật bất kỳ registration nào
         * - UPDATE_REGISTRATION_OWN: chỉ cập nhật của chính mình
         * - Nếu workShiftId hoặc daysOfWeek thay đổi, phải validate conflict lại
         */
        @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.UPDATE_REGISTRATION_ALL + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.UPDATE_REGISTRATION_OWN + "')")
        @Transactional
        public ShiftRegistrationResponse updateRegistration(String registrationId,
                        UpdateShiftRegistrationRequest request) {
                log.debug("Request to update Employee Shift Registration: {}", registrationId);

                // 1. Load existing registration
                EmployeeShiftRegistration registration = loadRegistrationWithOwnershipCheck(registrationId);

                boolean needConflictCheck = false;

                // 2. Update workShiftId if provided
                if (request.getWorkShiftId() != null && !request.getWorkShiftId().equals(registration.getSlotId())) {
                        // Validate work shift exists and is active
                        workShiftRepository.findByWorkShiftIdAndIsActive(request.getWorkShiftId(), true)
                                        .orElseThrow(() -> new WorkShiftNotFoundException(request.getWorkShiftId()));

                        registration.setSlotId(request.getWorkShiftId());
                        needConflictCheck = true;
                        log.info("Updated work shift ID to: {}", request.getWorkShiftId());
                }

                // 3. Update daysOfWeek if provided
                if (request.getDaysOfWeek() != null && !request.getDaysOfWeek().isEmpty()) {
                        // Delete old registration days
                        List<RegistrationDays> oldDays = registrationDaysRepository
                                        .findByIdRegistrationId(registrationId);
                        registrationDaysRepository.deleteAll(oldDays);
                        log.info("Deleted {} old registration days", oldDays.size());

                        // Create new registration days
                        List<RegistrationDays> newDays = new ArrayList<>();
                        for (DayOfWeek dayOfWeek : request.getDaysOfWeek()) {
                                RegistrationDaysId dayId = new RegistrationDaysId(registrationId, dayOfWeek);
                                RegistrationDays registrationDay = new RegistrationDays(registration, dayId);
                                newDays.add(registrationDay);
                        }
                        registrationDaysRepository.saveAll(newDays);
                        log.info("Created {} new registration days", newDays.size());

                        needConflictCheck = true;
                }

                // 4. Update effectiveFrom if provided
                if (request.getEffectiveFrom() != null) {
                        LocalDate today = LocalDate.now();
                        if (request.getEffectiveFrom().isBefore(today)) {
                                throw new InvalidRegistrationDateException(
                                                "Ngày bắt đầu hiệu lực không thể là quá khứ. Ngày bắt đầu: " +
                                                                request.getEffectiveFrom() + ", Ngày hiện tại: "
                                                                + today);
                        }
                        registration.setEffectiveFrom(request.getEffectiveFrom());
                }

                // 5. Update effectiveTo if provided
                if (request.getEffectiveTo() != null) {
                        if (request.getEffectiveTo().isBefore(registration.getEffectiveFrom())) {
                                throw new InvalidRegistrationDateException(
                                                "Ngày kết thúc hiệu lực phải sau hoặc bằng ngày bắt đầu. " +
                                                                "Ngày bắt đầu: " + registration.getEffectiveFrom() +
                                                                ", Ngày kết thúc: " + request.getEffectiveTo());
                        }
                        registration.setEffectiveTo(request.getEffectiveTo());
                }

                // 6. Update isActive if provided
                if (request.getIsActive() != null) {
                        registration.setIsActive(request.getIsActive());
                }

                // 7. Check for conflicts if workShiftId or daysOfWeek changed
                if (needConflictCheck) {
                        // Reload registration days to get updated list
                        List<DayOfWeek> currentDays = registrationDaysRepository.findByIdRegistrationId(registrationId)
                                        .stream()
                                        .map(rd -> rd.getId().getDayOfWeek())
                                        .toList();

                        List<EmployeeShiftRegistration> conflicts = registrationRepository.findConflictingRegistrations(
                                        registration.getEmployeeId(),
                                        registration.getSlotId(),
                                        currentDays);

                        // Filter out the current registration itself
                        conflicts = conflicts.stream()
                                        .filter(c -> !c.getRegistrationId().equals(registrationId))
                                        .toList();

                        if (!conflicts.isEmpty()) {
                                EmployeeShiftRegistration conflict = conflicts.get(0);
                                String conflictingDays = conflict.getRegistrationDays().stream()
                                                .map(rd -> rd.getId().getDayOfWeek().toString())
                                                .reduce((a, b) -> a + ", " + b)
                                                .orElse("");

                                throw new RegistrationConflictException(
                                                String.format("Đã tồn tại đăng ký hoạt động cho nhân viên %d, ca %s vào các ngày: %s. "
                                                                +
                                                                "Registration ID: %s, Hiệu lực từ: %s đến: %s",
                                                                registration.getEmployeeId(),
                                                                registration.getSlotId(),
                                                                conflictingDays,
                                                                conflict.getRegistrationId(),
                                                                conflict.getEffectiveFrom(),
                                                                conflict.getEffectiveTo() != null
                                                                                ? conflict.getEffectiveTo()
                                                                                : "vô thời hạn"));
                        }
                }

                // 8. Save and return
                registrationRepository.save(registration);
                log.info("Updated registration: {}", registrationId);

                EmployeeShiftRegistration updatedRegistration = registrationRepository
                                .findByRegistrationId(registrationId)
                                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));

                return shiftRegistrationMapper.toShiftRegistrationResponse(updatedRegistration);
        }

        /**
         * PUT /api/v1/registrations/{registration_id}
         * Thay thế toàn bộ thông tin đăng ký ca làm.
         * - Admin hoặc UPDATE_REGISTRATION_ALL: cập nhật bất kỳ registration nào
         * - UPDATE_REGISTRATION_OWN: chỉ cập nhật của chính mình
         * - Thực hiện đầy đủ validation như khi POST
         */
        @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.UPDATE_REGISTRATION_ALL + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.UPDATE_REGISTRATION_OWN + "')")
        @Transactional
        public ShiftRegistrationResponse replaceRegistration(String registrationId,
                        ReplaceShiftRegistrationRequest request) {
                log.debug("Request to replace Employee Shift Registration: {}", registrationId);

                // 1. Load existing registration and validate ownership
                EmployeeShiftRegistration registration = loadRegistrationWithOwnershipCheck(registrationId);

                // 2. Validate work shift exists and is active
                workShiftRepository.findByWorkShiftIdAndIsActive(request.getWorkShiftId(), true)
                                .orElseThrow(() -> new WorkShiftNotFoundException(request.getWorkShiftId()));

                // 3. Validate dates
                LocalDate today = LocalDate.now();

                if (request.getEffectiveFrom().isBefore(today)) {
                        throw new InvalidRegistrationDateException(
                                        "Ngày bắt đầu hiệu lực không thể là quá khứ. Ngày bắt đầu: " +
                                                        request.getEffectiveFrom() + ", Ngày hiện tại: " + today);
                }

                if (request.getEffectiveTo() != null &&
                                request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
                        throw new InvalidRegistrationDateException(
                                        "Ngày kết thúc hiệu lực phải sau hoặc bằng ngày bắt đầu. " +
                                                        "Ngày bắt đầu: " + request.getEffectiveFrom() +
                                                        ", Ngày kết thúc: " + request.getEffectiveTo());
                }

                // 4. Check for conflicts (excluding current registration)
                List<EmployeeShiftRegistration> conflicts = registrationRepository.findConflictingRegistrations(
                                registration.getEmployeeId(),
                                request.getWorkShiftId(),
                                request.getDaysOfWeek());

                // Filter out the current registration itself
                conflicts = conflicts.stream()
                                .filter(c -> !c.getRegistrationId().equals(registrationId))
                                .toList();

                if (!conflicts.isEmpty()) {
                        EmployeeShiftRegistration conflict = conflicts.get(0);
                        String conflictingDays = conflict.getRegistrationDays().stream()
                                        .map(rd -> rd.getId().getDayOfWeek().toString())
                                        .reduce((a, b) -> a + ", " + b)
                                        .orElse("");

                        throw new RegistrationConflictException(
                                        String.format("Đã tồn tại đăng ký hoạt động cho nhân viên %d, ca %s vào các ngày: %s. "
                                                        +
                                                        "Registration ID: %s, Hiệu lực từ: %s đến: %s",
                                                        registration.getEmployeeId(),
                                                        request.getWorkShiftId(),
                                                        conflictingDays,
                                                        conflict.getRegistrationId(),
                                                        conflict.getEffectiveFrom(),
                                                        conflict.getEffectiveTo() != null ? conflict.getEffectiveTo()
                                                                        : "vô thời hạn"));
                }

                // 5. Replace all fields
                registration.setSlotId(request.getWorkShiftId());
                registration.setEffectiveFrom(request.getEffectiveFrom());
                registration.setEffectiveTo(request.getEffectiveTo());
                registration.setIsActive(request.getIsActive());

                // 6. Delete old registration days and create new ones
                List<RegistrationDays> oldDays = registrationDaysRepository.findByIdRegistrationId(registrationId);
                registrationDaysRepository.deleteAll(oldDays);
                log.info("Deleted {} old registration days", oldDays.size());

                List<RegistrationDays> newDays = new ArrayList<>();
                for (DayOfWeek dayOfWeek : request.getDaysOfWeek()) {
                        RegistrationDaysId dayId = new RegistrationDaysId(registrationId, dayOfWeek);
                        RegistrationDays registrationDay = new RegistrationDays(registration, dayId);
                        newDays.add(registrationDay);
                }
                registrationDaysRepository.saveAll(newDays);
                log.info("Created {} new registration days", newDays.size());

                // 7. Save and return
                registrationRepository.save(registration);
                log.info("Replaced registration: {}", registrationId);

                EmployeeShiftRegistration replacedRegistration = registrationRepository
                                .findByRegistrationId(registrationId)
                                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));

                return shiftRegistrationMapper.toShiftRegistrationResponse(replacedRegistration);
        }

        /**
         * DELETE /api/v1/registrations/{registration_id}
         * Xóa mềm đăng ký ca làm (set is_active = false).
         * - Admin hoặc DELETE_REGISTRATION_ALL: xóa bất kỳ registration nào
         * - DELETE_REGISTRATION_OWN: chỉ xóa của chính mình
         */
        @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.DELETE_REGISTRATION_ALL + "') or " +
                        "hasAuthority('" + AuthoritiesConstants.DELETE_REGISTRATION_OWN + "')")
        @Transactional
        public void deleteRegistration(String registrationId) {
                log.debug("Request to delete (soft) Employee Shift Registration: {}", registrationId);

                // 1. Load existing registration and validate ownership
                EmployeeShiftRegistration registration = loadRegistrationWithOwnershipCheck(registrationId);

                // 2. Soft delete: set is_active = false
                registration.setIsActive(false);
                registrationRepository.save(registration);

                log.info("Soft deleted registration: {} (set is_active = false)", registrationId);
        }

        /**
         * Helper method: Load registration and check ownership permission
         * Throws RegistrationNotFoundException if not found or user doesn't have
         * permission
         */
        private EmployeeShiftRegistration loadRegistrationWithOwnershipCheck(String registrationId) {
                // Admin or user with _ALL permission can access any registration
                if (SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN) ||
                                SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.UPDATE_REGISTRATION_ALL) ||
                                SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.DELETE_REGISTRATION_ALL)) {

                        return registrationRepository.findByRegistrationId(registrationId)
                                        .orElseThrow(() -> new RegistrationNotFoundException(registrationId));
                }
                // User with _OWN permission can only access their own registration
                else {
                        String username = SecurityUtil.getCurrentUserLogin()
                                        .orElseThrow(() -> new RuntimeException("User not authenticated"));

                        Integer employeeId = accountRepository.findOneByUsername(username)
                                        .map(account -> account.getEmployee().getEmployeeId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Employee not found for user: " + username));

                        return registrationRepository.findByRegistrationIdAndEmployeeId(registrationId, employeeId)
                                        .orElseThrow(() -> new RegistrationNotFoundException(registrationId,
                                                        "or you don't have permission to modify it"));
                }
        }
}
