# BR-41 Implementation: Self-Approval Prevention

## Business Rule
**BR-41**: Managers cannot approve their own Leave or Overtime requests.
- Vietnamese: Quản lý không được tự phê duyệt yêu cầu Nghỉ phép hoặc Làm thêm giờ của chính mình.

## Implementation Summary

### 1. New Exception Class
**File**: `SelfApprovalNotAllowedException.java`
- Location: `src/main/java/com/dental/clinic/management/working_schedule/exception/`
- Purpose: Custom exception thrown when a manager attempts to approve their own request
- Message format: "Bạn không thể tự phê duyệt yêu cầu {type} {requestId}. Quản lý không được phép tự phê duyệt yêu cầu của chính mình."

### 2. Overtime Request Service Update
**File**: `OvertimeRequestService.java`
- Location: `src/main/java/com/dental/clinic/management/working_schedule/service/`
- Method: `handleApproval(OvertimeRequest request, Employee approvedBy)`
- Added validation after permission check:
```java
// BR-41: Managers cannot approve their own Leave or Overtime requests
if (request.getEmployee().getEmployeeId().equals(approvedBy.getEmployeeId())) {
    log.warn("Manager {} attempting to self-approve overtime request {}", 
        approvedBy.getEmployeeId(), request.getRequestId());
    throw new SelfApprovalNotAllowedException("Làm thêm giờ", request.getRequestId());
}
```

### 3. Time-Off Request Service Update
**File**: `TimeOffRequestService.java`
- Location: `src/main/java/com/dental/clinic/management/working_schedule/service/`
- Method: `handleApproval(TimeOffRequest timeOffRequest)`
- Added validation after getting approver ID:
```java
// BR-41: Managers cannot approve their own Leave or Overtime requests
if (timeOffRequest.getEmployeeId().equals(approvedBy)) {
    log.warn("Manager {} attempting to self-approve time-off request {}", 
        approvedBy, timeOffRequest.getRequestId());
    throw new SelfApprovalNotAllowedException("Nghỉ phép", timeOffRequest.getRequestId());
}
```

## Validation Logic
1. **Check timing**: Validation occurs during the approval process, after permission checks but before status updates
2. **Comparison**: Compares the requester's employee ID with the approver's employee ID
3. **Scope**: Applies to both:
   - Overtime requests (làm thêm giờ)
   - Time-off/leave requests (nghỉ phép)

## Error Response
When a manager tries to self-approve:
- **Exception**: `SelfApprovalNotAllowedException`
- **HTTP Status**: 400 Bad Request (or as configured in global exception handler)
- **Message Examples**:
  - Overtime: "Bạn không thể tự phê duyệt yêu cầu Làm thêm giờ OTR123456. Quản lý không được phép tự phê duyệt yêu cầu của chính mình."
  - Leave: "Bạn không thể tự phê duyệt yêu cầu Nghỉ phép TOR123456. Quản lý không được phép tự phê duyệt yêu cầu của chính mình."

## Testing Recommendations
1. **Test Case 1**: Manager creates overtime request for themselves
   - Action: Manager tries to approve their own overtime request
   - Expected: `SelfApprovalNotAllowedException` thrown
   
2. **Test Case 2**: Manager creates leave request for themselves
   - Action: Manager tries to approve their own leave request
   - Expected: `SelfApprovalNotAllowedException` thrown

3. **Test Case 3**: Manager approves another employee's request
   - Action: Manager approves a different employee's request
   - Expected: Success (approval goes through)

4. **Test Case 4**: Admin/another manager approves
   - Action: Different admin/manager approves the request
   - Expected: Success (approval goes through)

## Notes
- The validation ensures managers cannot bypass the approval process
- Managers still need to create requests for their own leave/overtime
- These requests must be approved by other managers/admins
- The validation maintains audit trail integrity by ensuring separate requester and approver roles

## Implementation Date
January 12, 2026
