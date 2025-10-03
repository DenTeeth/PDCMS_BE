package com.dental.clinic.management.controller;

import com.dental.clinic.management.domain.Employee;
import com.dental.clinic.management.dto.request.CreateEmployeeRequest;
import com.dental.clinic.management.dto.request.UpdateEmployeeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dental.clinic.management.dto.response.EmployeeInfoResponse;
import com.dental.clinic.management.mapper.EmployeeMapper;
import com.dental.clinic.management.service.EmployeeService;
import com.dental.clinic.management.utils.annotation.ApiMessage;

import jakarta.validation.Valid;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

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
  public ResponseEntity<Page<EmployeeInfoResponse>> getAllEmployees(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "employeeCode") String sortBy,
      @RequestParam(defaultValue = "ASC") String sortDirection) {

    Page<EmployeeInfoResponse> response = employeeService.getAllEmployees(page, size, sortBy, sortDirection);
    return ResponseEntity.ok().body(response);
  }

  @GetMapping("/{employeeCode}")
  @ApiMessage("Get employee by Employee Code successfully")
  public ResponseEntity<EmployeeInfoResponse> getEmployeeByCode(@PathVariable("employeeCode") String employeeCode) {
    Employee employee = employeeService.findEmployeeByCode(employeeCode);
    EmployeeInfoResponse response = employeeMapper.toEmployeeInfoResponse(employee);
    return ResponseEntity.ok(response);
  }

  /**
   * {@code POST  /employees} : Create a new employee.
   *
   * @param request the employee data to create.
   * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
   *         body the new employee, or with status {@code 400 (Bad Request)} if
   *         validation fails.
   * @throws URISyntaxException if the Location URI syntax is incorrect.
   */
  @PostMapping("")
  @ApiMessage("Create employee successfully")
  public ResponseEntity<EmployeeInfoResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request)
      throws URISyntaxException {

    EmployeeInfoResponse response = employeeService.createEmployee(request);

    return ResponseEntity
        .created(new URI("/api/v1/employees/" + response.getEmployeeCode()))
        .body(response);
  }

  /**
   * {@code PATCH  /employees/:employeeCode} : Partial updates given fields of an
   * existing employee, field will ignore if it is null
   *
   * @param employeeCode the code of the employee to save.
   * @param request      the employee data to update.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
   *         the updated employee,
   *         or with status {@code 400 (Bad Request)} if the employee is not
   *         valid,
   *         or with status {@code 404 (Not Found)} if the employee is not found,
   *         or with status {@code 500 (Internal Server Error)} if the employee
   *         couldn't be updated.
   */
  @PatchMapping("/{employeeCode}")
  @ApiMessage("Update employee successfully")
  public ResponseEntity<EmployeeInfoResponse> updateEmployee(
      @PathVariable("employeeCode") String employeeCode,
      @Valid @RequestBody UpdateEmployeeRequest request) {

    EmployeeInfoResponse response = employeeService.updateEmployee(employeeCode, request);
    return ResponseEntity.ok(response);
  }

  /**
   * {@code DELETE  /employees/:employeeCode} : delete the employee by employee
   * code.
   *
   * @param employeeCode the code of the employee to delete.
   * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
   */
  @DeleteMapping("/{employeeCode}")
  @ApiMessage("Delete employee successfully")
  public ResponseEntity<Void> deleteEmployee(@PathVariable("employeeCode") String employeeCode) {
    employeeService.deleteEmployee(employeeCode);
    return ResponseEntity.noContent().build();
  }

}
