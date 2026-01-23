# Appointment Business Rules - Frontend Integration Guide

**Document Version:** 1.0  
**Last Updated:** December 8, 2024  
**Target Audience:** Frontend Developers  

---

## Overview

This document summarizes the appointment booking business rules implemented in the backend and how the frontend should handle them.

**Total Rules Implemented:** 7 out of 10  
**Status:** 3 rules (Rule #2, Rule #4, Rule #5, Rule #9 - patient-facing) + 2 rules (Rule #1, Rule #6 - system automation) + 2 rules (Rule #3 - authentication)

---

## ✅ Implemented Rules - FE Action Required

### Rule #1: 3-Month Maximum Advance Booking ⚙️ AUTOMATIC
**Business Logic:** Patients cannot book appointments more than 3 months in advance.

**Backend Behavior:**
- Automatically validates on `POST /api/appointments`
- Returns 400 Bad Request if date > 3 months ahead

**FE Implementation:**
```javascript
// Date picker configuration
const maxBookingDate = new Date();
maxBookingDate.setMonth(maxBookingDate.getMonth() + 3);

<DatePicker 
  maxDate={maxBookingDate}
  label="Chọn ngày hẹn"
  helperText="Chỉ được đặt trước tối đa 3 tháng"
/>
```

**Error Response:**
```json
{
  "error": "BOOKING_TOO_FAR_IN_ADVANCE",
  "message": "Cannot book appointment more than 3 months in advance. Requested time: 2025-05-08 10:00, Maximum allowed date: 2025-03-08 23:59. Please choose an earlier date.",
  "requestedTime": "2025-05-08T10:00:00",
  "maximumAllowedDate": "2025-03-08T23:59:59"
}
```

**FE Error Handling:**
```javascript
if (error.error === 'BOOKING_TOO_FAR_IN_ADVANCE') {
  showError('Không thể đặt lịch xa quá 3 tháng. Vui lòng chọn ngày gần hơn.');
}
```

---

### Rule #2: 2-Hour Minimum Lead Time 🚨 VALIDATION
**Business Logic:** Patients must book at least 2 hours before appointment time.

**Backend Behavior:**
- Validates on `POST /api/appointments`
- Returns 400 Bad Request if `appointmentStartTime < now + 2 hours`

**FE Implementation:**
```javascript
// Time slot filtering
const minBookingTime = new Date();
minBookingTime.setHours(minBookingTime.getHours() + 2);

// Filter available time slots
const availableSlots = allSlots.filter(slot => 
  new Date(slot.startTime) >= minBookingTime
);

// Display warning
if (selectedTime < minBookingTime) {
  showWarning('Vui lòng chọn giờ hẹn ít nhất 2 tiếng kể từ bây giờ');
}
```

**Error Response:**
```json
{
  "error": "BOOKING_TOO_SOON",
  "message": "Cannot book appointment less than 2 hours in advance. Requested time: 2024-12-08 10:30, Earliest allowed time: 2024-12-08 11:00. Please choose a later time.",
  "requestedTime": "2024-12-08T10:30:00",
  "earliestAllowedTime": "2024-12-08T11:00:00"
}
```

**UI Recommendations:**
- Gray out time slots that are less than 2 hours away
- Show tooltip: "Không thể đặt (< 2 giờ)"
- Highlight earliest available slot

---

### Rule #3: Concurrent Login Prevention 🔒 AUTOMATIC
**Business Logic:** When user logs in from device B, device A's session is automatically invalidated.

**Backend Behavior:**
- On login → invalidates previous token
- Old token returns 401 Unauthorized

**FE Implementation:**
```javascript
// Handle 401 Unauthorized globally
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Check if token was invalidated (not expired)
      const errorMessage = error.response?.data?.error;
      
      if (errorMessage === 'Token has been invalidated') {
        // Show specific message for concurrent login
        showAlert({
          title: 'Phiên đăng nhập bị hủy',
          message: 'Tài khoản đã được đăng nhập từ thiết bị khác. Vui lòng đăng nhập lại.',
          type: 'warning'
        });
      } else {
        // Normal token expiration
        showAlert({
          title: 'Phiên đăng nhập hết hạn',
          message: 'Vui lòng đăng nhập lại.',
          type: 'info'
        });
      }
      
      // Redirect to login
      logout();
      redirectTo('/login');
    }
    return Promise.reject(error);
  }
);
```

**User Experience:**
```
Scenario: User logs in on computer, then logs in on phone

Computer screen shows:
┌─────────────────────────────────────┐
│  ⚠️  Phiên đăng nhập bị hủy         │
│                                     │
│  Tài khoản đã được đăng nhập từ     │
│  thiết bị khác. Vui lòng đăng nhập  │
│  lại.                               │
│                                     │
│         [Đăng nhập lại]             │
└─────────────────────────────────────┘
```

---

### Rule #4: 24-Hour Cancellation Deadline 🚨 VALIDATION
**Business Logic:** Appointments can only be cancelled at least 24 hours before scheduled time.

**Backend Behavior:**
- Validates on `PUT /api/appointments/{code}/status` with `status=CANCELLED`
- Returns 400 Bad Request if `now > appointmentStartTime - 24 hours`

**FE Implementation:**
```javascript
// Check if cancellation is allowed
const canCancel = (appointmentStartTime) => {
  const now = new Date();
  const startTime = new Date(appointmentStartTime);
  const cancellationDeadline = new Date(startTime);
  cancellationDeadline.setHours(cancellationDeadline.getHours() - 24);
  
  return now <= cancellationDeadline;
};

// UI rendering
{appointment.status === 'SCHEDULED' && (
  <>
    {canCancel(appointment.appointmentStartTime) ? (
      <Button onClick={handleCancel} color="error">
        Hủy lịch hẹn
      </Button>
    ) : (
      <Tooltip title="Không thể hủy trong vòng 24 giờ trước giờ hẹn">
        <span>
          <Button disabled color="error">
            Hủy lịch hẹn
          </Button>
        </span>
      </Tooltip>
    )}
  </>
)}

// Show deadline info
const deadline = new Date(appointment.appointmentStartTime);
deadline.setHours(deadline.getHours() - 24);

<Alert severity="info">
  Hạn chót hủy lịch: {formatDateTime(deadline)}
</Alert>
```

**Error Response:**
```json
{
  "error": "LATE_CANCELLATION",
  "message": "Cannot cancel appointment within 24 hours of scheduled time. Appointment start: 2024-12-09 10:00, Cancellation deadline: 2024-12-08 10:00. Please contact clinic staff for assistance.",
  "appointmentStartTime": "2024-12-09T10:00:00",
  "cancellationDeadline": "2024-12-08T10:00:00"
}
```

**FE Error Handling:**
```javascript
if (error.error === 'LATE_CANCELLATION') {
  showDialog({
    title: 'Không thể hủy lịch',
    message: 'Đã quá hạn hủy lịch (24 giờ trước giờ hẹn). Vui lòng liên hệ phòng khám để được hỗ trợ.',
    actions: [
      { label: 'Đóng', onClick: closeDialog },
      { label: 'Gọi phòng khám', onClick: () => window.open('tel:1900xxxx') }
    ]
  });
}
```

---

### Rule #5: No-Show Blocking (3 Strikes) 🚨 CRITICAL
**Business Logic:** After 3 consecutive no-shows, patient is blocked from online booking.

**Backend Behavior:**
- Tracks `consecutiveNoShows` counter (resets on check-in)
- Blocks booking when counter >= 3
- Returns 400 Bad Request on booking attempt

**FE Implementation:**

#### 1. Display Patient Status
```javascript
// Patient profile page
const PatientStatus = ({ patient }) => {
  if (patient.isBookingBlocked) {
    return (
      <Alert severity="error">
        <AlertTitle>Tài khoản bị chặn đặt lịch</AlertTitle>
        <Typography variant="body2">
          Lý do: {patient.bookingBlockReason}
        </Typography>
        <Typography variant="body2">
          Ngày chặn: {formatDateTime(patient.blockedAt)}
        </Typography>
        <Typography variant="body2" sx={{ mt: 1 }}>
          Vui lòng liên hệ phòng khám để được mở khóa.
        </Typography>
        <Button 
          variant="contained" 
          color="primary" 
          onClick={() => window.open('tel:1900xxxx')}
          sx={{ mt: 1 }}
        >
          Gọi phòng khám
        </Button>
      </Alert>
    );
  }
  
  if (patient.consecutiveNoShows > 0) {
    return (
      <Alert severity="warning">
        <AlertTitle>Cảnh báo</AlertTitle>
        Bạn đã bỏ hẹn {patient.consecutiveNoShows}/3 lần liên tiếp.
        {patient.consecutiveNoShows === 2 && (
          <Typography variant="body2" color="error" sx={{ mt: 1 }}>
            ⚠️ Lần bỏ hẹn tiếp theo sẽ bị chặn đặt lịch online!
          </Typography>
        )}
      </Alert>
    );
  }
  
  return null;
};
```

#### 2. Disable Booking Button
```javascript
// Booking page
const BookingButton = ({ patient }) => {
  if (patient.isBookingBlocked) {
    return (
      <Button 
        variant="contained" 
        color="error" 
        disabled
        fullWidth
      >
        Tài khoản bị chặn - Liên hệ phòng khám
      </Button>
    );
  }
  
  return (
    <Button 
      variant="contained" 
      color="primary" 
      onClick={handleBooking}
      fullWidth
    >
      Đặt lịch hẹn
    </Button>
  );
};
```

#### 3. Handle Booking Error
```json
{
  "error": "PATIENT_BLOCKED",
  "message": "Patient is currently blocked from online booking. Block reason: Bị chặn do bỏ hẹn 3 lần liên tiếp. Lần cuối: APT-20241208-001. Blocked since: 2024-12-08 10:30. Please contact clinic staff to restore booking access.",
  "blockReason": "Bị chặn do bỏ hẹn 3 lần liên tiếp. Lần cuối: APT-20241208-001",
  "blockedAt": "2024-12-08T10:30:00"
}
```

```javascript
if (error.error === 'PATIENT_BLOCKED') {
  showDialog({
    title: 'Tài khoản bị chặn',
    message: error.message,
    severity: 'error',
    actions: [
      { label: 'Đóng', onClick: closeDialog },
      { label: 'Liên hệ phòng khám', onClick: contactClinic, color: 'primary' }
    ]
  });
}
```

#### 4. Show No-Show Counter Badge
```javascript
// Appointment history list
<Badge 
  badgeContent={patient.consecutiveNoShows} 
  color={patient.consecutiveNoShows >= 2 ? 'error' : 'warning'}
  invisible={patient.consecutiveNoShows === 0}
>
  <Avatar src={patient.avatar} />
</Badge>
```

**UI Flow:**
```
No-shows: 0 → 1 → 2 → 3 (BLOCKED)
   ✅      ⚠️    🔴   🚫

0: No warning
1: Yellow badge "Đã bỏ hẹn 1 lần"
2: Red alert "⚠️ Lần bỏ hẹn tiếp theo sẽ bị chặn!"
3: Blocked - Cannot book online
```

---

### Rule #6: Auto-Cancel if Late >15 Minutes ⚙️ AUTOMATIC
**Business Logic:** System automatically marks appointments as NO_SHOW if patient arrives >15 minutes late.

**Backend Behavior:**
- Scheduled job runs every 5 minutes
- Finds SCHEDULED appointments where `startTime + 15 min < now`
- Auto-updates status to NO_SHOW
- Triggers Rule #5 (no-show counter increments)

**FE Implementation:**

#### 1. Show Late Status Indicator
```javascript
// Calculate if appointment is late
const getAppointmentLateStatus = (appointment) => {
  if (appointment.status !== 'SCHEDULED') return null;
  
  const now = new Date();
  const startTime = new Date(appointment.appointmentStartTime);
  const lateThreshold = new Date(startTime);
  lateThreshold.setMinutes(lateThreshold.getMinutes() + 15);
  
  if (now > lateThreshold) {
    const minutesLate = Math.floor((now - startTime) / 60000);
    return { isLate: true, minutesLate };
  }
  
  if (now > startTime) {
    const minutesLate = Math.floor((now - startTime) / 60000);
    return { isLate: false, minutesLate };
  }
  
  return null;
};

// UI rendering
const lateStatus = getAppointmentLateStatus(appointment);

{lateStatus?.isLate && (
  <Alert severity="error">
    ⏰ Lịch hẹn đã muộn {lateStatus.minutesLate} phút.
    Hệ thống sẽ tự động hủy sau {15 - lateStatus.minutesLate} phút nữa.
  </Alert>
)}

{lateStatus && !lateStatus.isLate && lateStatus.minutesLate > 0 && (
  <Alert severity="warning">
    ⏰ Bạn đã đến muộn {lateStatus.minutesLate} phút.
    Vui lòng check-in ngay để tránh bị hủy lịch.
  </Alert>
)}
```

#### 2. Real-time Status Updates
```javascript
// Poll appointment status every minute for today's appointments
useEffect(() => {
  const interval = setInterval(async () => {
    if (appointment.status === 'SCHEDULED') {
      const updated = await fetchAppointmentStatus(appointment.appointmentCode);
      if (updated.status === 'NO_SHOW') {
        showNotification({
          title: 'Lịch hẹn đã bị hủy',
          message: 'Lịch hẹn đã bị hủy tự động do đến muộn quá 15 phút.',
          severity: 'error'
        });
        setAppointment(updated);
      }
    }
  }, 60000); // Check every minute
  
  return () => clearInterval(interval);
}, [appointment]);
```

**No FE Validation Required:** This is purely backend automation. FE only needs to:
1. Show late warning when patient is late (1-15 minutes)
2. Refresh status to see NO_SHOW after auto-cancel
3. Display no-show notes in appointment history

---

### Rule #9: Reschedule Limit (Max 2 Times) 🚨 VALIDATION
**Business Logic:** Each appointment can only be rescheduled a maximum of 2 times.

**Backend Behavior:**
- Tracks `rescheduleCount` per appointment
- Increments counter on each reschedule
- Returns 400 Bad Request when count >= 2

**FE Implementation:**

#### 1. Display Reschedule Counter
```javascript
// Appointment detail page
const RescheduleInfo = ({ appointment }) => {
  const remaining = 2 - (appointment.rescheduleCount || 0);
  
  if (appointment.rescheduleCount >= 2) {
    return (
      <Alert severity="error">
        <AlertTitle>Không thể dời lịch</AlertTitle>
        Lịch hẹn này đã được dời tối đa 2 lần. 
        Vui lòng liên hệ phòng khám để được hỗ trợ.
      </Alert>
    );
  }
  
  if (appointment.rescheduleCount > 0) {
    return (
      <Alert severity="warning">
        Đã dời lịch: {appointment.rescheduleCount}/2 lần.
        Còn {remaining} lần dời lịch.
      </Alert>
    );
  }
  
  return (
    <Typography variant="caption" color="textSecondary">
      Có thể dời lịch tối đa 2 lần
    </Typography>
  );
};
```

#### 2. Disable Reschedule Button
```javascript
const canReschedule = appointment.rescheduleCount < 2;

<Button
  variant="outlined"
  color="primary"
  onClick={handleReschedule}
  disabled={!canReschedule}
>
  {canReschedule ? 'Dời lịch' : 'Đã hết lượt dời'}
</Button>

{!canReschedule && (
  <Typography variant="caption" color="error">
    Liên hệ: 0764009726
  </Typography>
)}
```

#### 3. Show Counter Badge
```javascript
<Badge 
  badgeContent={`${appointment.rescheduleCount}/2`}
  color={appointment.rescheduleCount >= 2 ? 'error' : 'default'}
  invisible={appointment.rescheduleCount === 0}
>
  <EventRepeatIcon />
</Badge>
```

**Error Response:**
```json
{
  "error": "RESCHEDULE_LIMIT_EXCEEDED",
  "message": "Appointment has reached maximum reschedule limit (2 times). Current reschedule count: 2. Please contact clinic staff for assistance.",
  "currentCount": 2,
  "maxAllowed": 2
}
```

**FE Error Handling:**
```javascript
if (error.error === 'RESCHEDULE_LIMIT_EXCEEDED') {
  showDialog({
    title: 'Không thể dời lịch',
    message: 'Lịch hẹn đã được dời tối đa 2 lần. Vui lòng liên hệ phòng khám để được hỗ trợ.',
    actions: [
      { label: 'Đóng', onClick: closeDialog },
      { label: 'Gọi phòng khám', onClick: () => window.open('tel:1900xxxx') }
    ]
  });
}
```

---

## ⏳ Out of Scope (Future Implementation)

### Rule #7: Doctor Queue After Check-in ⚠️ NOT IMPLEMENTED
**Status:** Backend logic exists but not enforced  
**Reason:** Requires UX redesign for doctor dashboard  
**ETA:** Q1 2025

### Rule #8: Email/SMS Notifications ⚠️ NOT IMPLEMENTED
**Status:** Framework ready, needs SMTP/SMS configuration  
**Reason:** Requires external service integration  
**ETA:** Q2 2025

### Rule #10: Review After Completion ⚠️ NOT IMPLEMENTED
**Status:** Validation service created, waiting for review module  
**Reason:** Review/rating feature not in current scope  
**ETA:** Q2 2025

---

## API Error Response Format

All business rule validations return consistent error format:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message in Vietnamese",
  "timestamp": "2024-12-08T10:30:00",
  "path": "/api/appointments",
  "additionalInfo": {
    // Rule-specific details
  }
}
```

**Common Error Codes:**
- `BOOKING_TOO_SOON` - Rule #2
- `BOOKING_TOO_FAR_IN_ADVANCE` - Rule #1
- `LATE_CANCELLATION` - Rule #4
- `PATIENT_BLOCKED` - Rule #5
- `RESCHEDULE_LIMIT_EXCEEDED` - Rule #9
- `Token has been invalidated` - Rule #3

---

## Testing Checklist for FE

### Rule #2: 2-Hour Lead Time
- [ ] Time picker disables slots < 2 hours away
- [ ] Error message displays correctly
- [ ] Warning shown when selecting invalid time

### Rule #4: 24-Hour Cancellation
- [ ] Cancel button disabled when within 24 hours
- [ ] Tooltip shows cancellation deadline
- [ ] Error dialog appears with contact option

### Rule #5: No-Show Blocking
- [ ] Warning badge shows at 1-2 no-shows
- [ ] Booking disabled when blocked
- [ ] Block reason displays clearly
- [ ] Contact clinic button works

### Rule #9: Reschedule Limit
- [ ] Counter displays correctly (0/2, 1/2, 2/2)
- [ ] Reschedule button disabled at limit
- [ ] Error dialog shows contact info

### Rule #3: Concurrent Login
- [ ] 401 error handler shows correct message
- [ ] Auto-logout on concurrent session
- [ ] Redirect to login works

---

## UI/UX Best Practices

### 1. Proactive Validation
✅ Disable invalid options before user selects them  
✅ Show warnings before errors  
✅ Use visual cues (colors, icons) for limits

### 2. Clear Error Messages
✅ Use Vietnamese language  
✅ Explain WHY action is blocked  
✅ Provide ALTERNATIVE actions (call clinic)  
✅ Include relevant timestamps/deadlines

### 3. Visual Indicators
```
Status Colors:
- Green: Available
- Yellow: Warning (approaching limit)
- Red: Blocked/Exceeded
- Gray: Disabled

Icons:
- ✅ Available
- ⚠️ Warning
- 🚫 Blocked
- ⏰ Time-related
- 📞 Contact required
```

### 4. Loading States
```javascript
// When checking booking availability
<Button loading={isChecking} disabled={isChecking}>
  {isChecking ? 'Đang kiểm tra...' : 'Đặt lịch hẹn'}
</Button>
```

---

## Contact Information for Errors

When business rules block actions, always provide:

```javascript
const CLINIC_CONTACT = {
  phone: '0764009726',
  email: 'support@dentalclinic.com',
  hours: '8:00 - 20:00 (Thứ 2 - Thứ 7)'
};

// Error dialog template
<Dialog>
  <DialogTitle>Cần hỗ trợ?</DialogTitle>
  <DialogContent>
    <Typography>Vui lòng liên hệ phòng khám:</Typography>
    <List>
      <ListItem>
        <Phone /> {CLINIC_CONTACT.phone}
      </ListItem>
      <ListItem>
        <Email /> {CLINIC_CONTACT.email}
      </ListItem>
      <ListItem>
        <Schedule /> {CLINIC_CONTACT.hours}
      </ListItem>
    </List>
  </DialogContent>
</Dialog>
```

---

## Questions or Issues?

Contact backend team for:
- API endpoint clarification
- Error response format changes
- New validation requirements

**Backend Contact:** backend-team@dentalclinic.com
