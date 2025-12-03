# Issue #41: Reschedule & Delay APIs - Validation Clarification

**Date**: December 3, 2025
**Status**: ✅ RESOLVED - No Backend Bugs
**Reporter**: Frontend Team
**Category**: API Validation & Business Rules

---

## 📋 Summary

Frontend reported errors when testing **DELAY** and **RESCHEDULE** appointment APIs. After thorough testing, **both APIs are working correctly**. The errors are due to proper business rule validation, not bugs.

---

## 🔍 Root Cause Analysis

### Issue 1: DELAY API - Past Date Validation ✅

**Error Reported**:

```json
{
  "statusCode": 400,
  "error": "error.bad_request",
  "message": "Cannot delay appointment to a time in the past: 2025-11-04T10:00"
}
```

**Root Cause**: Testing with past-dated appointments (November 2025) when current date is December 3, 2025.

**Business Rule**: `newStartTime` must be:

- After `appointmentStartTime` (original time)
- NOT in the past (future date/time only)

**Resolution**: Use future appointments (Dec 4+ or Jan 2026+).

---

### Issue 2: RESCHEDULE API - Specialization Validation ✅

**Error Reported**:

```json
{
  "statusCode": 400,
  "error": "error.bad_request",
  "message": "Doctor EMP003 does not have required specializations. Missing IDs: [4]"
}
```

**Root Cause**: Selected doctor doesn't have the required specialization for the service.

**Business Rule**: New doctor must have ALL specializations required by appointment services.

**Resolution**: Filter doctors by service specializations before showing in UI.

---

### Issue 3: RESCHEDULE API - Shift Validation ✅

**Error Reported**:

```json
{
  "statusCode": 400,
  "error": "error.bad_request",
  "message": "Doctor EMP001 has no shift on 2025-12-05"
}
```

**Root Cause**: Selected doctor doesn't work on target date.

**Business Rule**: New doctor must have active shift on target date.

**Resolution**: Check doctor availability/shifts before allowing reschedule.

---

## ✅ Successful Test Results

### DELAY API Test

**Endpoint**: `PATCH /api/v1/appointments/{appointmentCode}/delay`

**Test Case**: Delay APT-20251204-001 from 09:00 → 10:00

**Request**:

```bash
curl -X PATCH "http://localhost:8080/api/v1/appointments/APT-20251204-001/delay" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "newStartTime": "2025-12-04T10:00:00",
    "reasonCode": "PATIENT_REQUEST",
    "notes": "Test delay 1 hour"
  }'
```

**Response**: ✅ `200 OK`

```json
{
  "appointmentId": 101,
  "appointmentCode": "APT-20251204-001",
  "status": "SCHEDULED",
  "computedStatus": "UPCOMING",
  "appointmentStartTime": "2025-12-04T10:00:00",
  "appointmentEndTime": "2025-12-04T10:45:00",
  "expectedDurationMinutes": 45,
  "patient": {
    "patientCode": "BN-1001",
    "fullName": "Đoàn Thanh Phong"
  },
  "doctor": {
    "employeeCode": "EMP001",
    "fullName": "Lê Anh Khoa"
  },
  "room": {
    "roomCode": "P-01",
    "roomName": "Phòng thường 1"
  },
  "services": [
    {
      "serviceCode": "ENDO_POST_CORE",
      "serviceName": "Đóng chốt tái tạo cùi răng"
    },
    {
      "serviceCode": "ENDO_TREAT_ANT",
      "serviceName": "Điều trị tủy răng trước"
    }
  ]
}
```

**Result**: ✅ Appointment successfully delayed by 1 hour

---

### RESCHEDULE API Test (Validation Working)

**Endpoint**: `POST /api/v1/appointments/{appointmentCode}/reschedule`

**Test Cases**:

#### ❌ Case 1: Doctor Missing Specialization

```json
{
  "newEmployeeCode": "EMP003",
  "newRoomCode": "P-03",
  "newStartTime": "2025-12-05T09:00:00",
  "newParticipantCodes": [],
  "reasonCode": "DOCTOR_UNAVAILABLE",
  "cancelNotes": "Test reschedule"
}
```

**Response**: ✅ `400 Bad Request`

```json
{
  "statusCode": 400,
  "error": "error.bad_request",
  "message": "Doctor EMP003 does not have required specializations. Missing IDs: [4]"
}
```

**Validation**: ✅ Correctly prevents assigning doctor without required skills

---

#### ❌ Case 2: Doctor No Shift on Target Date

```json
{
  "newEmployeeCode": "EMP001",
  "newRoomCode": "P-03",
  "newStartTime": "2025-12-05T15:00:00",
  "newParticipantCodes": [],
  "reasonCode": "PATIENT_REQUEST",
  "cancelNotes": "Test reschedule to Dec 5"
}
```

**Response**: ✅ `400 Bad Request`

```json
{
  "statusCode": 400,
  "error": "error.bad_request",
  "message": "Doctor EMP001 has no shift on 2025-12-05"
}
```

**Validation**: ✅ Correctly prevents scheduling outside doctor's working hours

---

## 📝 API Specifications

### DELAY API

**Endpoint**: `PATCH /api/v1/appointments/{appointmentCode}/delay`

**Permission**: `DELAY_APPOINTMENT`

**Request Body**:

```json
{
  "newStartTime": "LocalDateTime (ISO-8601 format: yyyy-MM-ddTHH:mm:ss)",
  "reasonCode": "AppointmentReasonCode (enum)",
  "notes": "string (optional)"
}
```

**Valid Reason Codes**:

- `PATIENT_REQUEST`
- `DOCTOR_EMERGENCY`
- `EQUIPMENT_FAILURE`
- `TRAFFIC_DELAY`
- `OTHER`

**Business Rules**:

1. Only `SCHEDULED` or `CHECKED_IN` appointments can be delayed
2. `newStartTime` MUST be **after** `appointmentStartTime`
3. `newStartTime` must NOT be in the past
4. Duration remains unchanged (automatically calculated)
5. Same doctor, room, services, and patient

**Response**: Full appointment object with updated times

---

### RESCHEDULE API

**Endpoint**: `POST /api/v1/appointments/{appointmentCode}/reschedule`

**Permission**: `CREATE_APPOINTMENT` (creates new appointment)

**Request Body**:

```json
{
  "newEmployeeCode": "string (required)",
  "newRoomCode": "string (required)",
  "newStartTime": "LocalDateTime (ISO-8601 format: yyyy-MM-ddTHH:mm:ss)",
  "newParticipantCodes": ["string array (optional)"],
  "reasonCode": "AppointmentReasonCode (enum)",
  "cancelNotes": "string (required - reason for cancellation)"
}
```

**Valid Reason Codes**:

- `PATIENT_REQUEST`
- `DOCTOR_UNAVAILABLE`
- `ROOM_UNAVAILABLE`
- `SCHEDULE_CONFLICT`
- `OTHER`

**Business Rules**:

1. Cancels old appointment with reason
2. Creates new appointment in same transaction
3. **Services are reused** (cannot be changed)
4. Patient remains the same
5. New doctor MUST have:
   - Required specializations for all services
   - Active shift on target date
6. New room must be available
7. Treatment plan item linkage is preserved

**Response**:

```json
{
  "cancelledAppointment": {
    /* Old appointment with CANCELLED status */
  },
  "newAppointment": {
    /* New appointment with SCHEDULED status */
  }
}
```

---

## 🎯 Frontend Integration Guide

### Pre-Flight Checks for DELAY

Before showing DELAY option:

1. Check appointment status: `SCHEDULED` or `CHECKED_IN` only
2. Current time < appointment start time (cannot delay past appointments)

**UI Flow**:

```javascript
// Step 1: Validate appointment can be delayed
if (!["SCHEDULED", "CHECKED_IN"].includes(appointment.status)) {
  // Hide delay button
  return;
}

if (new Date(appointment.appointmentStartTime) < new Date()) {
  // Hide delay button - appointment already passed
  return;
}

// Step 2: Show delay form with minimum time validation
const minTime = appointment.appointmentStartTime;
const now = new Date();
const minAllowedTime = new Date(Math.max(new Date(minTime), now));

// Step 3: Call API
const response = await fetch(`/api/v1/appointments/${code}/delay`, {
  method: "PATCH",
  headers: {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    newStartTime: selectedTime.toISOString().slice(0, 19),
    reasonCode: "PATIENT_REQUEST",
    notes: userNotes,
  }),
});

// Step 4: Handle response
if (response.ok) {
  const updated = await response.json();
  // Update UI with new times
  console.log("Delayed to:", updated.appointmentStartTime);
} else {
  const error = await response.json();
  // Show error message
  alert(error.message);
}
```

---

### Pre-Flight Checks for RESCHEDULE

Before showing RESCHEDULE form:

**Step 1: Get Available Doctors**

```javascript
// Call availability API with service requirements
const response = await fetch(`/api/v1/appointments/availability/doctors`, {
  method: "POST",
  headers: {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    serviceCodes: appointment.services.map((s) => s.serviceCode),
    date: targetDate,
    startTime: targetStartTime,
  }),
});

const availableDoctors = await response.json();
// Only show doctors from this list
```

**Step 2: Check Doctor Shifts**

```javascript
// When doctor selected, verify they have shift
const shiftResponse = await fetch(
  `/api/v1/work-shifts/employee/${doctorCode}/date/${targetDate}`,
  {
    headers: { Authorization: `Bearer ${token}` },
  }
);

if (!shiftResponse.ok) {
  alert("Doctor not available on selected date");
  return;
}
```

**Step 3: Call Reschedule API**

```javascript
const response = await fetch(`/api/v1/appointments/${code}/reschedule`, {
  method: "POST",
  headers: {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    newEmployeeCode: selectedDoctor.employeeCode,
    newRoomCode: selectedRoom.roomCode,
    newStartTime: selectedTime.toISOString().slice(0, 19),
    newParticipantCodes: selectedAssistants.map((a) => a.employeeCode),
    reasonCode: selectedReason,
    cancelNotes: userNotes,
  }),
});

// Step 4: Handle response
if (response.ok) {
  const result = await response.json();
  console.log("Old appointment:", result.cancelledAppointment);
  console.log("New appointment:", result.newAppointment);
  // Update UI with new appointment
} else {
  const error = await response.json();
  // Show specific error message
  if (error.message.includes("specializations")) {
    alert("Selected doctor does not have required skills for this service");
  } else if (error.message.includes("shift")) {
    alert("Doctor not available on selected date");
  } else {
    alert(error.message);
  }
}
```

---

## 🔧 Recommended FE Error Handling

### User-Friendly Error Messages

Map backend errors to clear user messages:

```javascript
const ERROR_MESSAGES = {
  // DELAY errors
  "Cannot delay appointment to a time in the past":
    "Không thể dời lịch hẹn về thời gian trong quá khứ. Vui lòng chọn thời gian trong tương lai.",

  "Only SCHEDULED or CHECKED_IN appointments can be delayed":
    "Chỉ có thể dời lịch hẹn đang ở trạng thái Đã đặt hoặc Đã check-in.",

  "newStartTime must be after appointmentStartTime":
    "Thời gian mới phải sau thời gian gốc của lịch hẹn.",

  // RESCHEDULE errors
  "does not have required specializations":
    "Bác sĩ được chọn không có chuyên môn phù hợp với dịch vụ này. Vui lòng chọn bác sĩ khác.",

  "has no shift on":
    "Bác sĩ không có ca làm việc vào ngày đã chọn. Vui lòng chọn ngày khác hoặc bác sĩ khác.",

  "Room is not available":
    "Phòng khám không khả dụng vào thời gian này. Vui lòng chọn phòng khác.",

  "Time slot is not available":
    "Khung giờ này đã được đặt. Vui lòng chọn thời gian khác.",
};

function getErrorMessage(backendError) {
  const message = backendError.message;

  for (const [key, value] of Object.entries(ERROR_MESSAGES)) {
    if (message.includes(key)) {
      return value;
    }
  }

  return "Có lỗi xảy ra. Vui lòng thử lại sau.";
}
```

---

## 📊 Test Data Reference

### Valid Future Appointments for Testing

| Appointment Code | Date  | Time        | Doctor | Status    | Notes                   |
| ---------------- | ----- | ----------- | ------ | --------- | ----------------------- |
| APT-20251204-001 | Dec 4 | 09:00-09:45 | EMP001 | SCHEDULED | ✅ Can DELAY            |
| APT-20251204-002 | Dec 4 | 14:00-14:30 | EMP002 | SCHEDULED | ⚠️ Specialization issue |
| APT-20251204-003 | Dec 4 | 08:00-08:30 | EMP001 | SCHEDULED | ✅ Can DELAY            |
| APT-20251206-001 | Dec 6 | 09:00-09:30 | EMP001 | SCHEDULED | ✅ Can DELAY/RESCHEDULE |
| APT-20251206-002 | Dec 6 | 14:30-15:00 | EMP003 | SCHEDULED | ⚠️ Specialization issue |
| APT-20251207-001 | Dec 7 | 10:00-10:45 | EMP004 | SCHEDULED | ✅ Can DELAY/RESCHEDULE |

### Doctor Specializations

| Doctor          | Code   | Specializations       | Notes                |
| --------------- | ------ | --------------------- | -------------------- |
| Lê Anh Khoa     | EMP001 | Endodontics, General  | ✅ Most services     |
| Trịnh Công Thái | EMP002 | General               | ⚠️ Limited services  |
| Jimmy Donaldson | EMP003 | Pediatric             | ⚠️ Pediatric only    |
| Junya Ota       | EMP004 | Orthodontics, Implant | ✅ Advanced services |

---

## ✅ Verification Checklist

### Backend Testing (Completed)

- [x] DELAY API works with future appointments
- [x] DELAY API rejects past dates
- [x] DELAY API validates appointment status
- [x] RESCHEDULE API validates doctor specializations
- [x] RESCHEDULE API validates doctor shifts
- [x] RESCHEDULE API cancels old + creates new in transaction
- [x] Error messages are clear and descriptive

### Frontend Integration (TODO)

- [ ] Implement availability check before showing DELAY
- [ ] Add date/time validation in DELAY form
- [ ] Implement doctor filtering by specialization for RESCHEDULE
- [ ] Add shift availability check in RESCHEDULE form
- [ ] Map backend errors to Vietnamese user messages
- [ ] Add loading states during API calls
- [ ] Refresh appointment list after successful operation
- [ ] Show success notification with new appointment details

---

## 📚 Related Documentation

- **Appointment API Full Guide**: `docs/api-guides/booking/appointment/Appointment.md`

  - DELAY API: Lines 2620-2750
  - RESCHEDULE API: Lines 3260-3450

- **Doctor Availability API**: `docs/api-guides/booking/appointment/Appointment.md` Lines 570-710

- **Issue #39**: Treatment Plan Reschedule Relink (Related to treatment plan item updates)

---

## 🎉 Conclusion

**Both APIs are working correctly!** The errors reported by Frontend are proper business rule validations, not bugs.

**Action Items for Frontend**:

1. ✅ Add pre-flight validation before showing DELAY/RESCHEDULE options
2. ✅ Use availability APIs to filter compatible doctors
3. ✅ Implement user-friendly error messages in Vietnamese
4. ✅ Add proper date/time pickers with validation

**No Backend Changes Required** ✨

---

**Last Updated**: December 3, 2025
**Tested By**: Backend Team
**Status**: ✅ Closed - Working as Designed
