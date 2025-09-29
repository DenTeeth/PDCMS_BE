package com.dental.clinic.management.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.dental.clinic.management.domain.Employee;
import com.dental.clinic.management.repository.EmployeeRepository;

@Service
public class EmployeeService {
  private final EmployeeRepository employeeRepository;

  public EmployeeService(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  @PreAuthorize("hasRole('ADMIN')")
  public List<Employee> findAllEmployees() {
    return employeeRepository.findAllEmployees();
  }

  
}
