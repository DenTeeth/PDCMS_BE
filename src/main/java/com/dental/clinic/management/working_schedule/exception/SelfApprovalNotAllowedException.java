package com.dental.clinic.management.working_schedule.exception;

/**
 * Exception thrown when a manager attempts to approve their own Leave or Overtime request.
 * BR-41: Managers cannot approve their own Leave or Overtime requests.
 */
public class SelfApprovalNotAllowedException extends RuntimeException {
    
    public SelfApprovalNotAllowedException(String requestType) {
        super(String.format(
            "Bạn không thể tự phê duyệt yêu cầu %s của chính mình. " +
            "Quản lý không được phép tự phê duyệt yêu cầu.",
            requestType
        ));
    }
    
    public SelfApprovalNotAllowedException(String requestType, String requestId) {
        super(String.format(
            "Bạn không thể tự phê duyệt yêu cầu %s %s. " +
            "Quản lý không được phép tự phê duyệt yêu cầu của chính mình.",
            requestType, requestId
        ));
    }
}
