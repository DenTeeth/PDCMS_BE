package com.dental.clinic.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO for creating a new employee
 */
public class CreateEmployeeRequest {

    @NotBlank(message = "Account ID is required")
    @Size(max = 20, message = "Account ID must not exceed 20 characters")
    private String accountId;

    @NotNull(message = "Role ID is required")
    private Integer roleId;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phone;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    private Set<Integer> specializationIds;

    // Constructors
    public CreateEmployeeRequest() {
    }

    public CreateEmployeeRequest(String accountId, Integer roleId, String firstName, String lastName) {
        this.accountId = accountId;
        this.roleId = roleId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
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

    public Set<Integer> getSpecializationIds() {
        return specializationIds;
    }

    public void setSpecializationIds(Set<Integer> specializationIds) {
        this.specializationIds = specializationIds;
    }

    @Override
    public String toString() {
        return "CreateEmployeeRequest{" +
                "accountId='" + accountId + '\'' +
                ", roleId=" + roleId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", specializationIds=" + specializationIds +
                '}';
    }
}
