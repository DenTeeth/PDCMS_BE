package com.dental.clinic.management.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.dental.clinic.management.domain.Employee;
import com.dental.clinic.management.domain.Specialization;
import com.dental.clinic.management.dto.response.EmployeeInfoResponse;

@Component
public class EmployeeMapper {

  /**
   * Convert Employee entity to EmployeeInfoResponse DTO.
   */
  public EmployeeInfoResponse toEmployeeInfoResponse(Employee employee) {
    if (employee == null) {
      return null;
    }

    EmployeeInfoResponse response = new EmployeeInfoResponse();

    response.setEmployeeId(employee.getEmployeeId());
    response.setEmployeeCode(employee.getEmployeeCode());
    response.setFirstName(employee.getFirstName());
    response.setLastName(employee.getLastName());
    response.setFullName(employee.getFirstName() + " " + employee.getLastName());
    response.setPhone(employee.getPhone());
    response.setDateOfBirth(employee.getDateOfBirth());
    response.setAddress(employee.getAddress());
    response.setRoleId(employee.getRoleId());

    // Map roleName from Role entity (via ManyToOne relationship)
    if (employee.getRole() != null) {
      response.setRoleName(employee.getRole().getRoleName());
    }

    response.setIsActive(employee.getIsActive());
    response.setCreatedAt(employee.getCreatedAt());

    // Map specializations
    if (employee.getSpecializations() != null) {
      Set<EmployeeInfoResponse.SpecializationResponse> specializationResponses = employee.getSpecializations().stream()
          .map(this::toSpecializationResponse)
          .collect(Collectors.toSet());
      response.setSpecializations(specializationResponses);
    }

    // Map account info
    if (employee.getAccount() != null) {
      EmployeeInfoResponse.AccountInfoResponse accountResponse = new EmployeeInfoResponse.AccountInfoResponse();
      accountResponse.setAccountId(employee.getAccount().getAccountId());
      accountResponse.setUsername(employee.getAccount().getUsername());
      accountResponse.setEmail(employee.getAccount().getEmail());
      accountResponse.setStatus(employee.getAccount().getStatus().name());
      response.setAccount(accountResponse);
    }

    return response;
  }

  /**
   * Convert list of Employee entities to list of EmployeeInfoResponse DTOs.
   */
  public List<EmployeeInfoResponse> toEmployeeInfoResponseList(List<Employee> employees) {
    if (employees == null) {
      return null;
    }

    return employees.stream()
        .map(this::toEmployeeInfoResponse)
        .collect(Collectors.toList());
  }

  /**
   * Convert Specialization entity to SpecializationResponse DTO.
   */
  private EmployeeInfoResponse.SpecializationResponse toSpecializationResponse(Specialization specialization) {
    if (specialization == null) {
      return null;
    }

    EmployeeInfoResponse.SpecializationResponse response = new EmployeeInfoResponse.SpecializationResponse();
    response.setSpecializationId(specialization.getSpecializationId());
    response.setName(specialization.getSpecializationName());
    response.setDescription(specialization.getDescription());

    return response;
  }
}
