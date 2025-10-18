package com.dental.clinic.management.employee_shift_registrations.service;

import com.dental.clinic.management.utils.security.AuthoritiesConstants;
import com.dental.clinic.management.utils.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dental.clinic.management.employee_shift_registrations.dto.response.ShiftRegistrationResponse;
import com.dental.clinic.management.employee_shift_registrations.mapper.ShiftRegistrationMapper;
import com.dental.clinic.management.employee_shift_registrations.repository.EmployeeShiftRegistrationRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for managing employee shift registrations
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistrationService {

    private final ShiftRegistrationMapper shiftRegistrationMapper;

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final EmployeeShiftRegistrationRepository registrationRepository;

    @PreAuthorize("hasRole('" + AuthoritiesConstants.ADMIN + "') or hasAuthority('"
            + AuthoritiesConstants.VIEW_REGISTRATION_ALL + "') or hasAuthority('"
            + AuthoritiesConstants.VIEW_REGISTRATION_OWN + "')")
    public Page<ShiftRegistrationResponse> getAllRegistrations(Pageable pageable) {
        log.debug("Request to get all Employee Shift Registrations");

        // LUỒNG 1: Dành cho Admin hoặc người dùng có quyền xem tất cả
        if (SecurityUtil.hasCurrentUserRole(AuthoritiesConstants.ADMIN)
                || SecurityUtil.hasCurrentUserPermission(AuthoritiesConstants.VIEW_REGISTRATION_ALL)) {

            return registrationRepository.findAll(pageable).map(shiftRegistrationMapper::toShiftRegistrationResponse);
        }
        // LUỒNG 2: Dành cho nhân viên chỉ có quyền VIEW_REGISTRATION_OWN
        else {
            throw new UnsupportedOperationException("VIEW_REGISTRATION_OWN not implemented yet");
        }

    }
}
