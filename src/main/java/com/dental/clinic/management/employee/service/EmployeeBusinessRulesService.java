package com.dental.clinic.management.employee.service;

import com.dental.clinic.management.exception.validation.BadRequestAlertException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

/**
 * Business Rules Validation Service for Employee Module
 * 
 * Implements:
 * - Rule #24: Employees must be at least 18 years old
 */
@Service
public class EmployeeBusinessRulesService {

    /**
     * Rule #24: Validate employee age (must be >= 18 years old)
     * 
     * Business Rule: Employees added to the system must be at least 18 years old
     * 
     * @param dateOfBirth Employee's date of birth
     * @throws BadRequestAlertException if employee is under 18
     */
    public void validateEmployeeAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new BadRequestAlertException(
                "Ngày sinh là bắt buộc khi tạo nhân viên",
                "Employee",
                "dateOfBirthRequired"
            );
        }

        LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
        
        if (dateOfBirth.isAfter(eighteenYearsAgo)) {
            int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
            
            throw new BadRequestAlertException(
                String.format("Nhân viên phải đủ 18 tuổi trở lên. Tuổi hiện tại: %d (Sinh nhật: %s)", age, dateOfBirth),
                "Employee",
                "employeeUnderage"
            );
        }
    }

    /**
     * Helper method: Calculate age from date of birth
     * 
     * @param dateOfBirth Date of birth
     * @return Age in years
     */
    public int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
