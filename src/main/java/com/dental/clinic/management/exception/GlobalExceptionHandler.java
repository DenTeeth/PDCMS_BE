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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setMessage("You don't have permission to access this resource");
        res.setError("error.access.denied");
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
        res.setMessage(ex.getBody().getTitle());

        // Extract error code from ProblemDetail properties
        Object messageProperty = ex.getBody().getProperties() != null
                ? ex.getBody().getProperties().get("message")
                : null;
        res.setError(messageProperty != null ? messageProperty.toString() : "error." + status.name().toLowerCase());
        res.setData(null);

        return ResponseEntity.status(status).body(res);
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
