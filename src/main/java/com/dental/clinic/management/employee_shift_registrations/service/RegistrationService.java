package com.dental.clinic.management.employee_shift_registrations.service;

import com.dental.clinic.management.account.repository.AccountRepository;
import com.dental.clinic.management.employee_shift_registrations.dto.response.ShiftRegistrationResponse;
import com.dental.clinic.management.employee_shift_registrations.mapper.ShiftRegistrationMapper;
import com.dental.clinic.management.employee_shift_registrations.repository.EmployeeShiftRegistrationRepository;
import com.dental.clinic.management.exception.EmployeeNotFoundException;
import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.utils.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistrationService {

        private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

        private final EmployeeShiftRegistrationRepository employeeShiftRegistrationRepository;
        private final ShiftRegistrationMapper shiftRegistrationMapper;
        private final AccountRepository accountRepository;

        @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or hasAuthority('"
                        + AuthoritiesConstants.VIEW_REGISTRATION_ALL + "') or hasAuthority('"
                        + AuthoritiesConstants.VIEW_REGISTRATION_OWN + "')")
        public Page<ShiftRegistrationResponse> getAllRegistrations(Pageable pageable) {
                log.debug("Request to get all Employee Shift Registrations");

                // LUỒNG 1: Dành cho Admin hoặc người dùng có quyền xem tất cả
                if (SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN)
                                || SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.VIEW_REGISTRATION_ALL)) {

                        log.info("User is ADMIN or has VIEW_REGISTRATION_ALL. Fetching all registrations.");
                        return employeeShiftRegistrationRepository.findAll(pageable)
                                        .map(shiftRegistrationMapper::toShiftRegistrationResponse);
                }
                // LUỒNG 2: Dành cho nhân viên chỉ có quyền VIEW_REGISTRATION_OWN
                else {
                        // 1. Lấy username của người dùng hiện tại một cách an toàn
                        String username = SecurityUtil.getCurrentUserLogin()
                                        .orElseThrow(() -> new IllegalStateException(
                                                        "Current username not found in security context"));

                        Integer employeeId = accountRepository.findOneByUsername(username)
                                        .map(account -> account.getEmployee().getEmployeeId())
                                        .orElseThrow(() -> new EmployeeNotFoundException(
                                                        "Không tìm thấy nhân viên với username: " + username));

                        log.info("User has VIEW_REGISTRATION_OWN. Fetching registrations for employee_id: {}",
                                        employeeId);

                        return employeeShiftRegistrationRepository.findByEmployeeId(employeeId, pageable)
                                        .map(shiftRegistrationMapper::toShiftRegistrationResponse);
                }
        }
}
