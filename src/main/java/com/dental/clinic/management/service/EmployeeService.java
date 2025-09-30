package com.dental.clinic.management.service;

import java.util.List;

import com.dental.clinic.management.exception.EmployeeNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static com.dental.clinic.management.utils.security.AuthoritiesConstants.*;

import com.dental.clinic.management.domain.Employee;
import com.dental.clinic.management.repository.EmployeeRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @PreAuthorize("hasRole('" + ADMIN + "')")
    public List<Employee> findAllEmployees() {
        return employeeRepository.findAll();
    }

    @PreAuthorize("hasRole('" + ADMIN + "') or hasAuthority('" + READ_ALL_EMPLOYEES + "')")
    @Transactional(readOnly = true)
    public Employee findEmployeeByCode(String employeeCode) {
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee code cannot be null or empty");
        }

        return employeeRepository.findOneByEmployeeCode(employeeCode)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with code: " + employeeCode));
    }
}
