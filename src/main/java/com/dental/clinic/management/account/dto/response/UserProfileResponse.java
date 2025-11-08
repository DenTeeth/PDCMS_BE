package com.dental.clinic.management.account.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User profile response containing personal information and roles.
 */
public class UserProfileResponse {

    private Integer id;
    private String username;
    private String email;
    private String accountStatus;
    private List<String> roles; // "Có roles nhưng không có permissions"

    // Personal info
    private String fullName;
    private String phoneNumber;
    private String address;
    private String dateOfBirth;
    private String specializationName;

    // Meta info
    private LocalDateTime createdAt;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Integer id, String username, String email, String accountStatus,
            List<String> roles, String fullName, String phoneNumber, String address,
            String dateOfBirth, String specializationName, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.accountStatus = accountStatus;
        this.roles = roles;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.specializationName = specializationName;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSpecializationName() {
        return specializationName;
    }

    public void setSpecializationName(String specializationName) {
        this.specializationName = specializationName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
