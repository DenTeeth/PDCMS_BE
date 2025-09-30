package com.dental.clinic.management.controller;

import java.util.List;

import com.dental.clinic.management.domain.Employee;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dental.clinic.management.dto.response.EmployeeInfoResponse;
import com.dental.clinic.management.mapper.EmployeeMapper;
import com.dental.clinic.management.service.EmployeeService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

  private final EmployeeService employeeService;
  private final EmployeeMapper employeeMapper;

  public EmployeeController(EmployeeService employeeService, EmployeeMapper employeeMapper) {
    this.employeeService = employeeService;
    this.employeeMapper = employeeMapper;
  }

  @GetMapping("")
  @ApiMessage("Get all employees successfully")
  public ResponseEntity<List<EmployeeInfoResponse>> getAllEmployees() {
    List<EmployeeInfoResponse> employees = employeeMapper.toEmployeeInfoResponseList(
        employeeService.findAllEmployees());
    return ResponseEntity.ok().body(employees);
  }

    @GetMapping("/{employeeCode}")
    @ApiMessage("Get employee by Employee Code successfully")
    public ResponseEntity<EmployeeInfoResponse> getEmployeeByCode(@PathVariable("employeeCode") String employeeCode) {
        Employee employee = employeeService.findEmployeeByCode(employeeCode);
        EmployeeInfoResponse response = employeeMapper.toEmployeeInfoResponse(employee);
        return ResponseEntity.ok(response);
    }

}
