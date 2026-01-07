
package com.dental.clinic.management.employee.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

import com.dental.clinic.management.employee.enums.EmploymentType;

/**
 * DTO for partial updating an existing employee
 * All fields are optional - only non-null fields will be updated
 */
public class UpdateEmployeeRequest {

    @Size(max = 20, message = "Mã vai trò không được vượt quá 20 ký tự")
    private String roleId;

    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    private String firstName;

    @Size(max = 50, message = "Họ không được vượt quá 50 ký tự")
    private String lastName;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Số điện thoại phải từ 10-15 chữ số")
    private String phone;

    @Past(message = "Ngày sinh phải trong quá khứ")
    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;

    private EmploymentType employmentType;

    private Boolean isActive;

    private Set<Integer> specializationIds;

    // Constructors
    public UpdateEmployeeRequest() {
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
        return "UpdateEmployeeRequest{" +
                "roleId=" + roleId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", employmentType=" + employmentType +
                ", isActive=" + isActive +
                ", specializationIds=" + specializationIds +
                '}';
    }
}
