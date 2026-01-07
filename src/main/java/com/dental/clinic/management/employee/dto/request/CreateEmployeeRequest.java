
package com.dental.clinic.management.employee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

import com.dental.clinic.management.employee.enums.EmploymentType;

/**
 * DTO for creating a new employee
 *
 * FLOW: Tạo Employee → Tự động tạo Account
 * - Admin cung cấp: username, email, password + thông tin employee
 * - System tự động tạo account và employee
 */
public class CreateEmployeeRequest {

    // Account information (REQUIRED - sẽ tạo account mới)
    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3-50 ký tự")
    private String username;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email phải hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
    private String password;

    // Employee information (REQUIRED)
    @NotBlank(message = "Mã vai trò là bắt buộc")
    @Size(max = 50, message = "Mã vai trò không được vượt quá 50 ký tự")
    private String roleId;

    @NotBlank(message = "Tên là bắt buộc")
    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    private String firstName;

    @NotBlank(message = "Họ là bắt buộc")
    @Size(max = 50, message = "Họ không được vượt quá 50 ký tự")
    private String lastName;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Số điện thoại phải từ 10-15 chữ số")
    private String phone;

    @Past(message = "Ngày sinh phải trong quá khứ")
    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;

    @NotNull(message = "Loại hợp đồng là bắt buộc")
    private EmploymentType employmentType;

    private Set<Integer> specializationIds;

    // Constructors
    public CreateEmployeeRequest() {
    }

    public CreateEmployeeRequest(String username, String roleId, String firstName, String lastName) {
        this.username = username;
        this.roleId = roleId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
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
        return "CreateEmployeeRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", roleId=" + roleId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", employmentType=" + employmentType +
                ", specializationIds=" + specializationIds +
                '}';
    }
}
