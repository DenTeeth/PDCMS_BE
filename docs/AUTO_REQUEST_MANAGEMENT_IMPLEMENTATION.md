# AUTO REQUEST MANAGEMENT IMPLEMENTATION

**Date:** December 29, 2025  
**Feature:** Automatic Request Cancellation, Manager Reminders, and Data Cleanup  
**Applies to:** Overtime Requests, Time-Off Requests, Registration Requests (Part-Time Flex)

---

## 📋 OVERVIEW

This implementation provides automated management for all types of employee requests in the system:
1. **Overtime Requests** (OvertimeRequest)
2. **Time-Off Requests** (TimeOffRequest)
3. **Registration Requests** (PartTimeRegistration)

### Business Requirements

1. **Auto-Cancellation**: Automatically cancel PENDING requests when they exceed their deadline
2. **Manager Reminders**: Send notifications to managers 1 day before request deadlines
3. **Auto-Cleanup**: Delete old completed/cancelled/rejected requests after 30 days to save storage

---

## 🏗️ ARCHITECTURE

### Scheduled Jobs

Three new scheduled job components have been created:

#### 1. **RequestAutoCancellationJob**
- **Schedule**: Daily at 1:00 AM (Vietnam time)
- **Purpose**: Cancel overdue PENDING requests
- **Logic**:
  - **Overtime**: Cancel if `work_date` has passed
  - **Time-Off**: Cancel if `start_date` has passed
  - **Registration**: Cancel if `effective_from` has passed
- **File**: `RequestAutoCancellationJob.java`

#### 2. **RequestReminderNotificationJob**
- **Schedule**: Daily at 9:00 AM (Vietnam time)
- **Purpose**: Send reminder notifications to managers for PENDING requests due tomorrow
- **Logic**:
  - Find PENDING requests where deadline is tomorrow
  - Send notification to all ADMIN users
  - Include request details and employee information
- **File**: `RequestReminderNotificationJob.java`

#### 3. **RequestAutoCleanupJob**
- **Schedule**: Daily at 2:00 AM (Vietnam time)
- **Purpose**: Delete old completed/cancelled/rejected requests
- **Logic**:
  - Delete requests older than 30 days after completion/cancellation/rejection
  - Only delete final states (APPROVED, REJECTED, CANCELLED)
  - PENDING requests handled by cancellation job
- **File**: `RequestAutoCleanupJob.java`

---

## 📊 REQUEST TYPES & CRITERIA

### 1. Overtime Requests

| Action | Criteria | Field Checked |
|--------|----------|---------------|
| **Cancel** | work_date < today AND status = PENDING | `work_date` |
| **Remind** | work_date = tomorrow AND status = PENDING | `work_date` |
| **Delete (Approved)** | work_date < (today - 30 days) AND status = APPROVED | `work_date` |
| **Delete (Rejected/Cancelled)** | approved_at < (now - 30 days) AND status IN (REJECTED, CANCELLED) | `approved_at` |

**Cancellation Logic:**
```java
request.setStatus(RequestStatus.CANCELLED);
request.setCancellationReason("Tự động hủy: Đã quá thời hạn xử lý (quá ngày làm việc yêu cầu)");
request.setApprovedAt(LocalDateTime.now());
```

### 2. Time-Off Requests

| Action | Criteria | Field Checked |
|--------|----------|---------------|
| **Cancel** | start_date < today AND status = PENDING | `start_date` |
| **Remind** | start_date = tomorrow AND status = PENDING | `start_date` |
| **Delete (Approved)** | end_date < (today - 30 days) AND status = APPROVED | `end_date` |
| **Delete (Rejected/Cancelled)** | approved_at < (now - 30 days) AND status IN (REJECTED, CANCELLED) | `approved_at` |

**Cancellation Logic:**
```java
request.setStatus(TimeOffStatus.CANCELLED);
request.setCancellationReason("Tự động hủy: Đã quá thời hạn xử lý (quá ngày bắt đầu yêu cầu)");
request.setApprovedAt(LocalDateTime.now());
```

### 3. Registration Requests (Part-Time Flex)

| Action | Criteria | Field Checked |
|--------|----------|---------------|
| **Cancel** | effective_from < today AND status = PENDING | `effective_from` |
| **Remind** | effective_from = tomorrow AND status = PENDING | `effective_from` |
| **Delete (Approved)** | effective_to < (today - 30 days) AND status = APPROVED AND is_active = false | `effective_to`, `is_active` |
| **Delete (Rejected/Cancelled)** | processed_at < (now - 30 days) AND status IN (REJECTED, CANCELLED) | `processed_at` |

**Cancellation Logic:**
```java
request.setStatus(RegistrationStatus.CANCELLED);
request.setReason("Tự động hủy: Đã quá thời hạn xử lý (quá ngày bắt đầu hiệu lực)");
request.setProcessedAt(LocalDateTime.now());
request.setIsActive(false);
```

---

## 🔔 NOTIFICATION DETAILS

### Manager Reminder Notifications

**Recipients**: All users with ADMIN role

**Notification Types**:
- `REQUEST_OVERTIME_PENDING` - For overtime requests
- `REQUEST_TIME_OFF_PENDING` - For time-off requests  
- `REQUEST_PART_TIME_PENDING` - For registration requests

**Notification Content**:

#### Overtime Request
```
Title: "Nhắc nhở: Phê duyệt yêu cầu tăng ca"
Message: "Yêu cầu tăng ca của [Employee Name] cho ngày [Date] ca [Shift Name] cần được xử lý trước ngày mai"
```

#### Time-Off Request
```
Title: "Nhắc nhở: Phê duyệt yêu cầu nghỉ phép"
Message: "Yêu cầu nghỉ phép của [Employee Name] từ [Start Date] đến [End Date] cần được xử lý trước ngày mai"
```

#### Registration Request
```
Title: "Nhắc nhở: Phê duyệt yêu cầu đăng ký ca"
Message: "Yêu cầu đăng ký ca của nhân viên ID [ID] từ [Start Date] đến [End Date] cần được xử lý trước ngày mai"
```

---

## 💾 DATABASE CHANGES

### New Repository Methods

#### OvertimeRequestRepository
```java
// For cancellation job
List<OvertimeRequest> findByStatusAndWorkDateBefore(RequestStatus status, LocalDate date);

// For reminder job
List<OvertimeRequest> findByStatusAndWorkDate(RequestStatus status, LocalDate date);

// For cleanup job
List<OvertimeRequest> findByStatusAndApprovedAtBefore(RequestStatus status, LocalDateTime dateTime);
```

#### TimeOffRequestRepository
```java
// For cancellation job
List<TimeOffRequest> findByStatusAndStartDateBefore(TimeOffStatus status, LocalDate date);

// For reminder job
List<TimeOffRequest> findByStatusAndStartDate(TimeOffStatus status, LocalDate date);

// For cleanup job
List<TimeOffRequest> findByStatusAndEndDateBefore(TimeOffStatus status, LocalDate date);
List<TimeOffRequest> findByStatusAndApprovedAtBefore(TimeOffStatus status, LocalDateTime dateTime);
```

#### PartTimeRegistrationRepository
```java
// For cancellation job
List<PartTimeRegistration> findByStatusAndEffectiveFromBefore(RegistrationStatus status, LocalDate date);

// For reminder job
List<PartTimeRegistration> findByStatusAndEffectiveFrom(RegistrationStatus status, LocalDate date);

// For cleanup job
List<PartTimeRegistration> findByStatusAndEffectiveToBeforeAndIsActive(RegistrationStatus status, LocalDate date, Boolean isActive);
List<PartTimeRegistration> findByStatusAndProcessedAtBefore(RegistrationStatus status, LocalDateTime dateTime);
```

---

## ⏰ JOB SCHEDULE

| Job | Cron Expression | Time (Vietnam) | Frequency |
|-----|----------------|----------------|-----------|
| RequestAutoCancellationJob | `0 0 6 * * ?` | 6:00 AM | Daily |
| RequestReminderNotificationJob | `0 0 9 * * ?` | 9:00 AM | Daily |
| RequestAutoCleanupJob | `0 0 23 * * SUN` | 11:00 PM Sunday | Weekly |

**Note**: All jobs use timezone `Asia/Ho_Chi_Minh`

---

## 📝 CONFIGURATION

### Cleanup Threshold

The cleanup job uses a configurable threshold defined in `RequestAutoCleanupJob.java`:

```java
private static final int CLEANUP_DAYS_THRESHOLD = 30;
```

This can be adjusted to change the retention period for old requests.

---

## 🧪 TESTING

### Manual Testing

You can trigger the jobs manually for testing using the existing `ScheduledJobTestController`:

```bash
# Test cancellation job
POST /api/test/jobs/request-auto-cancellation

# Test reminder job
POST /api/test/jobs/request-reminder-notification

# Test cleanup job
POST /api/test/jobs/request-auto-cleanup
```

### Test Scenarios

#### Scenario 1: Auto-Cancellation
1. Create an overtime request with `work_date` = yesterday
2. Set status to PENDING
3. Run cancellation job
4. Verify status changed to CANCELLED with reason

#### Scenario 2: Reminder Notification
1. Create a time-off request with `start_date` = tomorrow
2. Set status to PENDING
3. Run reminder job
4. Verify notification sent to all ADMIN users

#### Scenario 3: Auto-Cleanup
1. Create an old approved overtime request with `work_date` = 31 days ago
2. Run cleanup job
3. Verify request is deleted from database

---

## 📊 LOGGING

All jobs provide detailed logging:

### Log Levels

- **INFO**: Job start/completion, summary statistics
- **DEBUG**: Individual request processing (in cleanup job)
- **WARN**: No warnings expected in normal operation
- **ERROR**: Failed operations (notification send failures, deletion failures)

### Sample Log Output

```
INFO  RequestAutoCancellationJob - ==== Starting auto-cancellation of overdue pending requests ====
INFO  RequestAutoCancellationJob - Auto-cancelled overtime request OTR251220001 for employee 5 (work_date: 2025-12-20)
INFO  RequestAutoCancellationJob - Cancelled 3 overdue overtime requests
INFO  RequestAutoCancellationJob - Cancelled 2 overdue time-off requests
INFO  RequestAutoCancellationJob - Cancelled 1 overdue registration requests
INFO  RequestAutoCancellationJob - ==== Auto-cancellation completed ====
INFO  RequestAutoCancellationJob - Summary: 3 overtime, 2 time-off, 1 registration requests cancelled
```

---

## 🔍 MONITORING

### Key Metrics to Monitor

1. **Cancellation Rate**: Number of requests auto-cancelled per day
2. **Reminder Count**: Number of reminders sent per day
3. **Cleanup Count**: Number of old requests deleted per day
4. **Error Rate**: Failed operations in each job

### Expected Behavior

- **Cancellation Job**: Should process requests within seconds
- **Reminder Job**: May take longer if many reminders to send (notification creation + WebSocket)
- **Cleanup Job**: Should complete quickly with batch deletes

---

## 🚨 ERROR HANDLING

All jobs implement try-catch blocks to ensure:

1. **Individual failures don't stop processing**: If one request fails, others continue
2. **Transaction rollback**: Failed operations don't leave partial data
3. **Detailed error logging**: Stack traces logged for debugging
4. **Job completion**: Jobs always log completion summary even if errors occur

### Example Error Handling

```java
for (OvertimeRequest request : overdueRequests) {
    try {
        // Process request
        overtimeRequestRepository.save(request);
        count++;
    } catch (Exception e) {
        log.error("Failed to cancel overtime request {}: {}", 
                request.getRequestId(), e.getMessage());
        // Continue processing other requests
    }
}
```

---

## 📚 RELATED FILES

### Core Files
- `RequestAutoCancellationJob.java` - Auto-cancellation logic
- `RequestReminderNotificationJob.java` - Reminder notification logic
- `RequestAutoCleanupJob.java` - Data cleanup logic

### Repository Files
- `OvertimeRequestRepository.java` - Added query methods
- `TimeOffRequestRepository.java` - Added query methods
- `PartTimeRegistrationRepository.java` - Added query methods

### Domain Files
- `OvertimeRequest.java` - Entity model
- `TimeOffRequest.java` - Entity model
- `PartTimeRegistration.java` - Entity model

### Service Files
- `NotificationService.java` - Notification creation
- `NotificationServiceImpl.java` - Implementation

---

## 🎯 BUSINESS IMPACT

### Benefits

1. **Reduced Manual Work**: Managers don't need to manually cancel overdue requests
2. **Improved Response Time**: Reminders help managers process requests before deadline
3. **Storage Optimization**: Old requests automatically cleaned up
4. **Better UX**: Employees get timely feedback (cancellation = implicit rejection)
5. **Audit Trail**: All auto-actions logged with reasons

### User Experience

**For Employees:**
- Requests automatically cancelled if not processed in time
- Clear cancellation reason in request history
- Can resubmit if needed

**For Managers:**
- Daily reminders for pending requests
- Reduced backlog of old requests
- Focus on current/upcoming requests

---

## 🔄 FUTURE ENHANCEMENTS

Potential improvements:

1. **Configurable Thresholds**: Make cleanup days configurable via application properties
2. **Email Notifications**: Send email reminders in addition to in-app notifications
3. **Escalation**: Send additional reminders if request still pending closer to deadline
4. **Analytics Dashboard**: Show trends of auto-cancelled requests
5. **Custom Cancellation Rules**: Allow different deadlines per request type
6. **Batch Processing**: Optimize for large volumes with batch operations

---

## ✅ DEPLOYMENT CHECKLIST

Before deploying to production:

- [ ] Verify scheduled job times are appropriate for business hours
- [ ] Test cancellation logic doesn't affect valid requests
- [ ] Confirm notification recipients (ADMIN role users)
- [ ] Review cleanup threshold (30 days) with stakeholders
- [ ] Test error handling with invalid data
- [ ] Monitor first few runs in production
- [ ] Document any configuration changes

---

## 📞 SUPPORT

For issues or questions:
- Check logs first: Look for ERROR level messages
- Verify job execution: Check scheduled task logs
- Test manually: Use test controller endpoints
- Review business rules: Ensure criteria match requirements

---

**Implementation Complete**: ✅  
**Ready for Testing**: ✅  
**Production Ready**: Pending testing and review
