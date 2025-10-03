package com.dental.clinic.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO for replacing (full update) an existing employee via PUT request
 * All fields are REQUIRED - this replaces the entire employee resource
 */
public class ReplaceEmployeeRequest {

    @NotBlank(message = "Role ID is required")
    @Size(max = 50, message = "Role ID must not exceed 50 characters")
    private String roleId;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phone;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotNull(message = "isActive status is required")
    private Boolean isActive;

    private Set<String> specializationIds;

    // Constructors
    public ReplaceEmployeeRequest() {
    }

    public ReplaceEmployeeRequest(String roleId, String firstName, String lastName, String phone,
            LocalDate dateOfBirth, String address, Boolean isActive, Set<String> specializationIds) {
        this.roleId = roleId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.isActive = isActive;
        this.specializationIds = specializationIds;
    }

    // Getters and Setters
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Set<String> getSpecializationIds() {
        return specializationIds;
    }

    public void setSpecializationIds(Set<String> specializationIds) {
        this.specializationIds = specializationIds;
    }

    @Override
    public String toString() {
        return "ReplaceEmployeeRequest{" +
                "roleId='" + roleId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", address='" + address + '\'' +
                ", isActive=" + isActive +
                ", specializationIds=" + specializationIds +
                '}';
    }
}
