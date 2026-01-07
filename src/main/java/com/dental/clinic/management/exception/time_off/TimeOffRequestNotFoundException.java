package com.dental.clinic.management.exception.time_off;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Exception thrown when time-off request is not found
 */
public class TimeOffRequestNotFoundException extends ErrorResponseException {

    public TimeOffRequestNotFoundException(String requestId) {
        super(HttpStatus.NOT_FOUND, asProblemDetail(requestId), null);
    }

    public TimeOffRequestNotFoundException(String requestId, String additionalMessage) {
        super(HttpStatus.NOT_FOUND, asProblemDetailWithMessage(requestId, additionalMessage), null);
    }

    private static ProblemDetail asProblemDetail(String requestId) {
        String message = "Không tìm thấy yêu cầu nghỉ phép với ID '" + requestId + "'";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setTitle("Không Tìm Thấy Yêu Cầu Nghỉ Phép");
        problemDetail.setProperty("errorCode", "TIMEOFF_REQUEST_NOT_FOUND");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }

    private static ProblemDetail asProblemDetailWithMessage(String requestId, String additionalMessage) {
        String message = "Không tìm thấy yêu cầu nghỉ phép với ID '" + requestId + "' " + additionalMessage;
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setTitle("Không Tìm Thấy Yêu Cầu Nghỉ Phép");
        problemDetail.setProperty("errorCode", "TIMEOFF_REQUEST_NOT_FOUND");
        problemDetail.setProperty("message", message);
        return problemDetail;
    }
}
