package com.dental.clinic.management.exception;

import com.dental.clinic.management.utils.FormatRestResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler to return consistent RestResponse format for all
 * errors.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle Spring Security BadCredentialsException.
     * Returns 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        log.warn("Authentication failed at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setMessage("Invalid username or password");
        res.setError("error.authentication.failed");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    /**
     * Handle UsernameNotFoundException from Spring Security.
     * Returns 401 Unauthorized.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleUsernameNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request) {

        log.warn("User not found at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setMessage("Invalid username or password");
        res.setError("error.authentication.failed");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    /**
     * Handle AccessDeniedException from Spring Security.
     * Returns 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());

        // Use Vietnamese message for consistent error responses
        String message = ex.getMessage();
        if (message == null || message.equals("Access Denied")) {
            message = "Không tìm thấy tài nguyên hoặc bạn không có quyền truy cập.";
        }

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setMessage(message);
        res.setError("FORBIDDEN");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    /**
     * Handle ALL ErrorResponseException subclasses (custom exceptions).
     * This includes: AccountNotFoundException, EmployeeNotFoundException,
     * BadRequestAlertException, JwtValidationException, etc.
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleErrorResponseException(
            ErrorResponseException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        log.warn("{} exception at {}: {}", status, request.getRequestURI(), ex.getBody().getTitle());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(status.value());
        
        // Extract error code and message from ProblemDetail properties
        Object errorCodeProperty = ex.getBody().getProperties() != null
                ? ex.getBody().getProperties().get("errorCode")
                : null;
        Object messageProperty = ex.getBody().getProperties() != null
                ? ex.getBody().getProperties().get("message")
                : null;
        
        // Set error code (use errorCode property if available, otherwise fallback to generic error)
        res.setError(errorCodeProperty != null 
                ? errorCodeProperty.toString() 
                : "error." + status.name().toLowerCase());
        
        // Set message (use message property if available, otherwise use detail from ProblemDetail)
        res.setMessage(messageProperty != null 
                ? messageProperty.toString() 
                : ex.getBody().getDetail());
        
        res.setData(null);

        return ResponseEntity.status(status).body(res);
    }

    /**
     * Handle slot conflict exception.
     * Returns 409 Conflict.
     */
    @ExceptionHandler(com.dental.clinic.management.exception.employee_shift.SlotConflictException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleSlotConflict(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Slot conflict at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.CONFLICT.value());
        res.setError("SLOT_CONFLICT");
        res.setMessage(ex.getMessage());
        res.setData(null);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    /**
     * Handle holiday conflict exception.
     * Returns 409 Conflict.
     */
    @ExceptionHandler(com.dental.clinic.management.exception.employee_shift.HolidayConflictException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleHolidayConflict(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Holiday conflict at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.CONFLICT.value());
        res.setError("HOLIDAY_CONFLICT");
        res.setMessage(ex.getMessage());
        res.setData(null);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    /**
     * Handle shift finalized exception.
     * Returns 409 Conflict.
     */
    @ExceptionHandler(com.dental.clinic.management.exception.employee_shift.ShiftFinalizedException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleShiftFinalized(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Shift finalized at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.CONFLICT.value());
        res.setError("SHIFT_FINALIZED");
        res.setMessage(ex.getMessage());
        res.setData(null);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    /**
     * Handle shift not found exception.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(com.dental.clinic.management.exception.employee_shift.ShiftNotFoundException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleShiftNotFound(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Shift not found at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setError("SHIFT_NOT_FOUND");
        res.setMessage(ex.getMessage());
        res.setData(null);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    /**
     * Handle related resource not found exception (Employee, WorkShift not found).
     * Returns 404 Not Found.
     */
    @ExceptionHandler({
        com.dental.clinic.management.exception.employee_shift.RelatedResourceNotFoundException.class,
        com.dental.clinic.management.exception.overtime.RelatedResourceNotFoundException.class
    })
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleRelatedResourceNotFound(
            Exception ex,
            HttpServletRequest request) {

        log.warn("Related resource not found at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setError("RELATED_RESOURCE_NOT_FOUND");
        res.setMessage(ex.getMessage());
        res.setData(null);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    /**
     * Handle invalid status transition exception.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(com.dental.clinic.management.exception.employee_shift.InvalidStatusTransitionException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleInvalidStatusTransition(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Invalid status transition at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("error.invalid.status.transition");
        res.setMessage(ex.getMessage());
        res.setData(null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * Handle cannot cancel batch shift exception.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(com.dental.clinic.management.exception.employee_shift.CannotCancelBatchShiftException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleCannotCancelBatchShift(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Cannot cancel batch shift at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("CANNOT_CANCEL_BATCH");
        res.setMessage(ex.getMessage());
        res.setData(null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * Handle cannot cancel completed shift exception.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(com.dental.clinic.management.exception.employee_shift.CannotCancelCompletedShiftException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleCannotCancelCompletedShift(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Cannot cancel completed shift at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("CANNOT_CANCEL_COMPLETED");
        res.setMessage(ex.getMessage());
        res.setData(null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * Handle validation errors from @Valid annotations.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Validation failed at {}: {}", request.getRequestURI(), ex.getMessage());

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(errorMessage);
        res.setError("error.validation");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * Handle missing required request parameters.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        log.warn("Missing parameter at {}: {}", request.getRequestURI(), ex.getMessage());

        // Special handling for date parameters in shift calendar endpoint
        String message;
        if (request.getRequestURI().contains("/api/v1/shifts") && 
            (ex.getParameterName().equals("start_date") || ex.getParameterName().equals("end_date"))) {
            message = "Vui lòng cung cấp ngày bắt đầu và ngày kết thúc hợp lệ.";
        } else {
            message = "Missing required parameter: " + ex.getParameterName();
        }

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(message);
        res.setError("INVALID_DATE_RANGE");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * Handle type mismatch errors (e.g., invalid date format).
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("Type mismatch at {}: {}", request.getRequestURI(), ex.getMessage());

        // Special handling for date parameters
        String message;
        if (request.getRequestURI().contains("/api/v1/shifts") && 
            (ex.getName().equals("startDate") || ex.getName().equals("endDate"))) {
            message = "Định dạng ngày không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD.";
        } else {
            message = "Invalid parameter type: " + ex.getName();
        }

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(message);
        res.setError("INVALID_DATE_FORMAT");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * Handle IllegalArgumentException.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(ex.getMessage() != null ? ex.getMessage() : "Invalid argument");
        res.setError("error.bad.request");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * Handle AccountNotVerifiedException.
     * Returns 403 Forbidden.
     */
    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleAccountNotVerified(
            AccountNotVerifiedException ex,
            HttpServletRequest request) {

        log.warn("Account not verified at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setMessage(ex.getMessage());
        res.setError("error.account.not.verified");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    /**
     * Handle TokenExpiredException.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleTokenExpired(
            TokenExpiredException ex,
            HttpServletRequest request) {

        log.warn("Token expired at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(ex.getMessage());
        res.setError("error.token.expired");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * Handle InvalidTokenException.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleInvalidToken(
            InvalidTokenException ex,
            HttpServletRequest request) {

        log.warn("Invalid token at {}: {}", request.getRequestURI(), ex.getMessage());

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(ex.getMessage());
        res.setError("error.token.invalid");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    /**
     * Fallback handler for any other unexpected exceptions.
     * Returns 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setMessage("Internal server error");
        res.setError("error.internal");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}
