package com.dental.clinic.management.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO for creating a new employee
 * Supports two modes:
 * 1. With existing account: provide accountId
 * 2. With new account: provide username, email, password (accountId will be
 * auto-generated)
 */
public class CreateEmployeeRequest {

    // Option 1: Use existing account
    @Size(max = 20, message = "Account ID must not exceed 20 characters")
    private String accountId;

    // Option 2: Create new account (required if accountId is not provided)
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    // Employee information (required)
    @NotBlank(message = "Role ID is required")
    @Size(max = 50, message = "Role ID must not exceed 50 characters")
    private String roleId;

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

    private Set<String> specializationIds;

    // Constructors
    public CreateEmployeeRequest() {
    }

    public CreateEmployeeRequest(String accountId, String roleId, String firstName, String lastName) {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
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

    public Set<String> getSpecializationIds() {
        return specializationIds;
    }

    public void setSpecializationIds(Set<String> specializationIds) {
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
