package com.dental.clinic.management.employee.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for Employee information.
 *
 * This class represents the response structure for employee data
 * returned by the API, including personal information, role details,
 * specializations, and associated account information.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeInfoResponse {

  private String employeeId;
  private String employeeCode;
  private String firstName;
  private String lastName;
  private String fullName;
  private String phone;
  private LocalDate dateOfBirth;
  private String address;
  private String roleId;
  private String roleName;
  private Set<SpecializationResponse> specializations;
  private Boolean isActive;
  private LocalDateTime createdAt;
  private AccountInfoResponse account;

  /**
   * Response DTO for Specialization information.
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SpecializationResponse {
    private String specializationId;
    private String name;
    private String description;
  }

  /**
   * Response DTO for Account basic information.
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AccountInfoResponse {
    private String accountId;
    private String username;
    private String email;
    private String status;
  }
}
