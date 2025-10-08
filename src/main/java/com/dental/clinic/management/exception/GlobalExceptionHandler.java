package com.dental.clinic.management.exception;

import com.dental.clinic.management.utils.FormatRestResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler to return a consistent RestResponse wrapper for
 * errors.
 *
 * Note: ErrorResponseException subclasses (BadCredentialsException,
 * AccountNotFoundException,
 * JwtValidationException, etc.) are automatically handled by Spring Boot and
 * return
 * RFC 7807 ProblemDetail JSON. No need to handle them here.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Fallback handler for any unexpected exceptions.
     * ErrorResponseException subclasses are handled by Spring automatically.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setMessage(ex.getMessage() != null ? ex.getMessage() : "Internal server error");
        res.setError("error.internal");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}
