package com.dental.clinic.management.employee_shift_registrations.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dental.clinic.management.employee_shift_registrations.dto.response.ShiftRegistrationResponse;
import com.dental.clinic.management.employee_shift_registrations.service.RegistrationService;

@RestController
@RequestMapping("/api/v1")
public class EmployeeShiftRegistrationController {

    private final RegistrationService registrationService;

    public EmployeeShiftRegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/registrations")
    public ResponseEntity<Page<ShiftRegistrationResponse>> getAllRegistrations(Pageable pageable) {
        Page<ShiftRegistrationResponse> page = registrationService.getAllRegistrations(pageable);
        return ResponseEntity.ok(page);
    }
}
