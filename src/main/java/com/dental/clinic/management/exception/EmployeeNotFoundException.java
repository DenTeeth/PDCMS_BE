package com.dental.clinic.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when an employee is not found.
 */
public class EmployeeNotFoundException extends ErrorResponseException {

    public EmployeeNotFoundException(String employeeCode) {
        super(
                HttpStatus.NOT_FOUND,
                createProblemDetail(employeeCode),
                null);
    }

    private static ProblemDetail createProblemDetail(String employeeCode) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setType(ErrorConstants.EMPLOYEE_NOT_FOUND_TYPE);
        problemDetail.setTitle("Employee not found");
        problemDetail.setProperty("message", "error.employee.notfound");
        problemDetail.setProperty("params", employeeCode);
        return problemDetail;
    }
}
