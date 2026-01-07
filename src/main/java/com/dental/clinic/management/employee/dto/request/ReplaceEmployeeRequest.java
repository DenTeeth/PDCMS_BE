
package com.dental.clinic.management.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

import com.dental.clinic.management.employee.enums.EmploymentType;

/**
 * DTO for replacing (full update) an existing employee via PUT request
 * All fields are REQUIRED - this replaces the entire employee resource
 */
public class ReplaceEmployeeRequest {

    @NotBlank(message = "Mã vai trò là bắt buộc")
    @Size(max = 50, message = "Mã vai trò không được vượt quá 50 ký tự")
    private String roleId;

    @NotBlank(message = "Tên là bắt buộc")
    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    private String firstName;

    @NotBlank(message = "Họ là bắt buộc")
    @Size(max = 50, message = "Họ không được vượt quá 50 ký tự")
    private String lastName;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Số điện thoại phải từ 10-15 chữ số")
    private String phone;

    @NotNull(message = "Ngày sinh là bắt buộc")
    @Past(message = "Ngày sinh phải trong quá khứ")
    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;

    @NotNull(message = "Loại hợp đồng là bắt buộc")
    private EmploymentType employmentType;

    @NotNull(message = "Trạng thái hoạt động là bắt buộc")
    private Boolean isActive;

    private Set<Integer> specializationIds;

    // Constructors
    public ReplaceEmployeeRequest() {
    }

    public ReplaceEmployeeRequest(String roleId, String firstName, String lastName, String phone,
            LocalDate dateOfBirth, String address, EmploymentType employmentType, Boolean isActive,
            Set<Integer> specializationIds) {
        this.roleId = roleId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.employmentType = employmentType;
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

    public Set<Integer> getSpecializationIds() {
        return specializationIds;
    }

    public void setSpecializationIds(Set<Integer> specializationIds) {
        this.specializationIds = specializationIds;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
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
                ", employmentType=" + employmentType +
                ", isActive=" + isActive +
                ", specializationIds=" + specializationIds +
                '}';
    }
}
