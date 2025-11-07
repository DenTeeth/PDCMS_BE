
package com.dental.clinic.management.employee.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.dental.clinic.management.employee.enums.EmploymentType;

/**
 * Response DTO for Employee information.
 *
 * This class represents the response structure for employee data
 * returned by the API, including personal information, role details,
 * specializations, and associated account information.
 *
 */
public class EmployeeInfoResponse {

  private Integer employeeId;
  private String employeeCode;
  private String firstName;
  private String lastName;
  private String fullName;
  private EmploymentType employeeType;
  private String phone;
  private LocalDate dateOfBirth;
  private String address;
  private String roleId;
  private String roleName;
  private Set<SpecializationResponse> specializations;
  private Boolean isActive;
  private LocalDateTime createdAt;
  private AccountInfoResponse account;

  public EmployeeInfoResponse() {
  }

  public EmployeeInfoResponse(Integer employeeId, String employeeCode, String firstName, String lastName,
      String fullName, EmploymentType employeeType, String phone, LocalDate dateOfBirth,
      String address, String roleId, String roleName, Set<SpecializationResponse> specializations,
      Boolean isActive, LocalDateTime createdAt, AccountInfoResponse account) {
    this.employeeId = employeeId;
    this.employeeCode = employeeCode;
    this.firstName = firstName;
    this.lastName = lastName;
    this.fullName = fullName;
    this.employeeType = employeeType;
    this.phone = phone;
    this.dateOfBirth = dateOfBirth;
    this.address = address;
    this.roleId = roleId;
    this.roleName = roleName;
    this.specializations = specializations;
    this.isActive = isActive;
    this.createdAt = createdAt;
    this.account = account;
  }

  public Integer getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(Integer employeeId) {
    this.employeeId = employeeId;
  }

  public String getEmployeeCode() {
    return employeeCode;
  }

  public void setEmployeeCode(String employeeCode) {
    this.employeeCode = employeeCode;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public EmploymentType getEmployeeType() {
    return employeeType;
  }

  public void setEmployeeType(EmploymentType employeeType) {
    this.employeeType = employeeType;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getRoleId() {
    return roleId;
  }

  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public Set<SpecializationResponse> getSpecializations() {
    return specializations;
  }

  public void setSpecializations(Set<SpecializationResponse> specializations) {
    this.specializations = specializations;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public AccountInfoResponse getAccount() {
    return account;
  }

  public void setAccount(AccountInfoResponse account) {
    this.account = account;
  }

  /**
   * Response DTO for Specialization information.
   */
  public static class SpecializationResponse {
    private Integer specializationId;
    private String name;
    private String description;

    public SpecializationResponse() {
    }

    public SpecializationResponse(Integer specializationId, String name, String description) {
      this.specializationId = specializationId;
      this.name = name;
      this.description = description;
    }

    public Integer getSpecializationId() {
      return specializationId;
    }

    public void setSpecializationId(Integer specializationId) {
      this.specializationId = specializationId;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }

  /**
   * Response DTO for Account basic information.
   */
  public static class AccountInfoResponse {
    private Integer accountId;
    private String accountCode;
    private String username;
    private String email;
    private String status;

    public AccountInfoResponse() {
    }

    public AccountInfoResponse(Integer accountId, String accountCode, String username, String email, String status) {
      this.accountId = accountId;
      this.accountCode = accountCode;
      this.username = username;
      this.email = email;
      this.status = status;
    }

    public Integer getAccountId() {
      return accountId;
    }

    public void setAccountId(Integer accountId) {
      this.accountId = accountId;
    }

    public String getAccountCode() {
      return accountCode;
    }

    public void setAccountCode(String accountCode) {
      this.accountCode = accountCode;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }
  }
}
