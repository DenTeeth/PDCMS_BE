package com.dental.clinic.management.exception;

import com.dental.clinic.management.utils.FormatRestResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler to return a consistent RestResponse wrapper for errors.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleErrorResponseException(ErrorResponseException ex,
            HttpServletRequest request) {
        ProblemDetail pd = ex.getBody();
        int status = HttpStatus.BAD_REQUEST.value();
        if (pd != null) {
            try {
                // ProblemDetail.getStatus() may be int or HttpStatus depending on Spring version
                Object s = pd.getStatus();
                if (s instanceof Integer) {
                    status = (Integer) s;
                } else if (s instanceof HttpStatus) {
                    status = ((HttpStatus) s).value();
                }
            } catch (Exception ignore) {
            }
        }

        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(status);
        // prefer title (human message) then detail
        String title = pd != null ? pd.getTitle() : null;
        String detail = pd != null ? pd.getDetail() : null;
        res.setMessage(title != null ? title : detail != null ? detail : "Bad request");

        // copy machine-readable error key if present
        Object msgProp = null;
        if (pd != null) {
            java.util.Map<String, Object> props = pd.getProperties();
            if (props != null) {
                msgProp = props.get("message");
            }
        }
        if (msgProp != null) {
            res.setError(msgProp.toString());
        }

        res.setData(null);
        return ResponseEntity.status(status).body(res);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FormatRestResponse.RestResponse<Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        FormatRestResponse.RestResponse<Object> res = new FormatRestResponse.RestResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setMessage(ex.getMessage() != null ? ex.getMessage() : "Internal server error");
        res.setError("error.internal");
        res.setData(null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}
