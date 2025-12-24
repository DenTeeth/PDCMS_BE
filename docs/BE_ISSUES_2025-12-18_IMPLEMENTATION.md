# BE Issues 2025-12-18 - Implementation Documentation

## Change Summary

This document describes the implementation of 3 issues requested by FE team on 2025-12-18.

---

## Issue 1: NO_SHOW Messages in Vietnamese

### Problem

When a patient is automatically marked as NO_SHOW (arrives >15 minutes late), the system displayed English messages.

### Old Behavior

```
Auto-marked as NO_SHOW by system: Patient arrived >15 minutes late (59 minutes late).
Original appointment time: 2025-12-18T13:00. System time: 2025-12-18T13:59:53.876860.
```

### New Behavior

```
Hệ thống tự động đánh dấu KHÔNG ĐẾN: Bệnh nhân đến trễ hơn 15 phút (trễ 59 phút).
Thời gian lịch hẹn gốc: 18/12/2025 13:00. Thời gian hệ thống: 18/12/2025 13:59.
```

### Implementation

**File Changed:** `AppointmentAutoStatusService.java`

**Changes:**

- Added Vietnamese date/time formatter: `dd/MM/yyyy HH:mm`
- Updated auto-NO_SHOW message to Vietnamese
- Format dates in Vietnamese style instead of ISO 8601

**Code:**

```java
// Format Vietnamese date/time (dd/MM/yyyy HH:mm)
java.time.format.DateTimeFormatter vietnameseFormatter =
        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
String originalTime = appointment.getAppointmentStartTime().format(vietnameseFormatter);
String systemTime = now.format(vietnameseFormatter);

request.setNotes(String.format(
        "Hệ thống tự động đánh dấu KHÔNG ĐẾN: Bệnh nhân đến trễ hơn 15 phút (trễ %d phút). " +
        "Thời gian lịch hẹn gốc: %s. Thời gian hệ thống: %s.",
        minutesLate,
        originalTime,
        systemTime));
```

---

## Issue 2: Plan Item Status When Appointment is NO_SHOW

### Problem

When an appointment becomes NO_SHOW, the linked treatment plan items remained in SCHEDULED status, preventing rebooking.

### Old Behavior

- Appointment status: SCHEDULED → NO_SHOW
- Plan item status: SCHEDULED (unchanged)
- Result: Item cannot be rebooked because status is still SCHEDULED

### New Behavior

- Appointment status: SCHEDULED → NO_SHOW
- Plan item status: SCHEDULED → READY_FOR_BOOKING
- Result: Item can be rebooked for a new appointment

### Implementation

**Files Changed:**

1. `AppointmentStatusService.java` - `updateLinkedPlanItemsStatus()` method
2. `AppointmentStatusService.java` - status mapping logic

**Changes:**

1. Added NO_SHOW to the list of statuses that trigger plan item updates:

```java
// Before: Only IN_PROGRESS, COMPLETED, CANCELLED
if (appointmentStatus != AppointmentStatus.IN_PROGRESS
        && appointmentStatus != AppointmentStatus.COMPLETED
        && appointmentStatus != AppointmentStatus.CANCELLED
        && appointmentStatus != AppointmentStatus.NO_SHOW) { // NEW
    return;
}
```

2. Added NO_SHOW mapping to return items to READY_FOR_BOOKING:

```java
case NO_SHOW:
    targetStatus = PlanItemStatus.READY_FOR_BOOKING; // Allow re-booking after NO_SHOW
    break;
```

### Business Logic

When a patient is marked as NO_SHOW:

1. Appointment status changes to NO_SHOW
2. Linked plan items return to READY_FOR_BOOKING
3. Patient can reschedule and the same items can be used
4. No-show counter increments (existing behavior)
5. After 3 consecutive no-shows, patient is blocked (existing behavior)

---

## Issue 3: Reschedule Capability for NO_SHOW Appointments

### Problem

NO_SHOW appointments were considered "terminal" and could not be rescheduled. This prevented patients from rebooking after missing an appointment.

### Old Behavior

- NO_SHOW appointments could NOT be delayed (API 3.6)
- NO_SHOW appointments could NOT be rescheduled (API 3.7)
- Patient had to create a completely new appointment

### New Behavior

- NO_SHOW appointments CAN be delayed (API 3.6)
- NO_SHOW appointments CAN be rescheduled (API 3.7)
- Plan items are already in READY_FOR_BOOKING status (from Issue 2)
- Reschedule works the same as SCHEDULED/CHECKED_IN appointments

### Implementation

**Files Changed:**

1. `AppointmentDelayService.java`
2. `AppointmentRescheduleService.java`
3. `dental-clinic-seed-data.sql` (permission description update)

**Changes:**

1. **AppointmentDelayService.java:**

```java
// Old: Only SCHEDULED or CHECKED_IN
// New: SCHEDULED, CHECKED_IN, or NO_SHOW
if (status != AppointmentStatus.SCHEDULED
    && status != AppointmentStatus.CHECKED_IN
    && status != AppointmentStatus.NO_SHOW) {
    throw new IllegalStateException(...);
}

// Terminal state check updated
private boolean isTerminalState(AppointmentStatus status) {
    return status == AppointmentStatus.COMPLETED
            || status == AppointmentStatus.CANCELLED;
    // NO_SHOW removed: Allow rescheduling NO_SHOW appointments
}
```

2. **AppointmentRescheduleService.java:**

```java
// Allow SCHEDULED, CHECKED_IN, and NO_SHOW
if (status != AppointmentStatus.SCHEDULED
    && status != AppointmentStatus.CHECKED_IN
    && status != AppointmentStatus.NO_SHOW) {
    throw new IllegalStateException(...);
}

// Removed the explicit NO_SHOW check that threw error
// ALLOW NO_SHOW appointments to be rescheduled (patient can return)
```

3. **Permission Update:**

```sql
-- Before:
('DELAY_APPOINTMENT', 'DELAY_APPOINTMENT', 'APPOINTMENT', 'Hoãn lịch hẹn sang thời gian khác (chỉ SCHEDULED/CHECKED_IN) - API 3.6', 56, NULL, TRUE, NOW()),

-- After:
('DELAY_APPOINTMENT', 'DELAY_APPOINTMENT', 'APPOINTMENT', 'Hoãn lịch hẹn sang thời gian khác (SCHEDULED/CHECKED_IN/NO_SHOW) - API 3.6', 56, NULL, TRUE, NOW()),
```

### Use Cases

**Use Case 1: Delay NO_SHOW Appointment**

```bash
# Patient missed appointment APT-001 (status = NO_SHOW)
# Receptionist delays it to tomorrow

PATCH /api/v1/appointments/APT-001/delay
{
  "newStartTime": "2025-12-19T09:00:00",
  "reasonCode": "PATIENT_REQUEST",
  "notes": "Bệnh nhân xin lỗi và yêu cầu đặt lại lịch"
}

# Result:
# - Appointment status: NO_SHOW → SCHEDULED
# - Appointment time: Updated to tomorrow
# - Plan items: Already READY_FOR_BOOKING (from Issue 2)
```

**Use Case 2: Reschedule NO_SHOW Appointment**

```bash
# Patient missed appointment with Dr. Khoa
# Receptionist reschedules to different doctor tomorrow

POST /api/v1/appointments/APT-001/reschedule
{
  "newEmployeeCode": "EMP002",
  "newRoomCode": "P-02",
  "newStartTime": "2025-12-19T14:00:00",
  "reasonCode": "DOCTOR_UNAVAILABLE",
  "cancelNotes": "Reschedule after NO_SHOW"
}

# Result:
# - Old appointment: Cancelled
# - New appointment: Created with status SCHEDULED
# - Plan items: Linked to new appointment
```

---

## Issue 4 (Bonus): Excel OUT_OF_STOCK Highlighting

### Problem

When exporting warehouse inventory to Excel, OUT_OF_STOCK rows used standard red color instead of the specific brand color #fa6666.

### Implementation

**File Changed:** `WarehouseExcelExportService.java`

**Changes:**

- Added imports for `XSSFCellStyle` and `XSSFColor`
- Changed from `IndexedColors.RED` to custom RGB color
- Applied to all OUT_OF_STOCK style methods

**Code:**

```java
// Before: IndexedColors.RED
style.setFillForegroundColor(IndexedColors.RED.getIndex());

// After: Custom color #fa6666 (RGB: 250, 102, 102)
XSSFColor outOfStockColor = new XSSFColor(new byte[]{(byte)250, (byte)102, (byte)102}, null);
style.setFillForegroundColor(outOfStockColor);
```

**Affected Methods:**

- `createOutOfStockDataStyle()`
- `createOutOfStockNumberStyle()`
- `createOutOfStockDateStyle()`

---

## Database Impact

### No Schema Changes Required

All changes are business logic only - no database migrations needed.

### Seed Data Changes

- Updated DELAY_APPOINTMENT permission description to include NO_SHOW

---

## Testing Guide

### Test 1: Vietnamese NO_SHOW Message

**Prerequisites:**

- Wait for auto-NO_SHOW job to run (every 5 minutes)
- Or manually mark appointment as NO_SHOW

**Steps:**

1. Create appointment for current time - 20 minutes
2. Wait for cron job (runs every 5 minutes at :00, :05, :10, etc.)
3. Check appointment notes

**Expected:**

```
Hệ thống tự động đánh dấu KHÔNG ĐẾN: Bệnh nhân đến trễ hơn 15 phút (trễ 20 phút).
Thời gian lịch hẹn gốc: 18/12/2025 13:00. Thời gian hệ thống: 18/12/2025 13:20.
```

---

### Test 2: Plan Item Status After NO_SHOW

**Prerequisites:**

- Appointment with linked treatment plan items
- Login as receptionist (password: 123456)

**Steps:**

```bash
# 1. Get appointment with plan items
GET /api/v1/appointments/APT-001

# 2. Mark as NO_SHOW
PATCH /api/v1/appointments/APT-001/status
{
  "status": "NO_SHOW",
  "notes": "Bệnh nhân không đến"
}

# 3. Check plan item status
GET /api/v1/patients/BN-1001/treatment-plans/{planId}
```

**Expected:**

- Appointment status: NO_SHOW
- Plan item status: READY_FOR_BOOKING
- Item can be used in new appointment

---

### Test 3: Delay NO_SHOW Appointment

**Prerequisites:**

- Appointment APT-001 with status = NO_SHOW
- Login as receptionist (password: 123456)

**Test Case:**

```bash
PATCH /api/v1/appointments/APT-001/delay
Authorization: Bearer {receptionist_token}
Content-Type: application/json

{
  "newStartTime": "2025-12-19T09:00:00",
  "reasonCode": "PATIENT_REQUEST",
  "notes": "Bệnh nhân xin đặt lại lịch"
}
```

**Expected Response: 200 OK**

```json
{
  "appointmentCode": "APT-001",
  "status": "SCHEDULED",
  "appointmentStartTime": "2025-12-19T09:00:00",
  "notes": "Bệnh nhân xin đặt lại lịch"
}
```

**Validation:**

- Appointment status changed from NO_SHOW to SCHEDULED
- Time updated to new slot
- Audit log created with action = DELAY

---

### Test 4: Reschedule NO_SHOW Appointment

**Prerequisites:**

- Appointment APT-001 with status = NO_SHOW
- Login as receptionist (password: 123456)

**Test Case:**

```bash
POST /api/v1/appointments/APT-001/reschedule
Authorization: Bearer {receptionist_token}
Content-Type: application/json

{
  "newEmployeeCode": "EMP002",
  "newRoomCode": "P-02",
  "newStartTime": "2025-12-19T14:00:00",
  "reasonCode": "PATIENT_REQUEST",
  "cancelNotes": "Bệnh nhân xin đổi bác sĩ"
}
```

**Expected Response: 200 OK**

```json
{
  "cancelledAppointment": {
    "appointmentCode": "APT-001",
    "status": "CANCELLED"
  },
  "newAppointment": {
    "appointmentCode": "APT-20251219-001",
    "status": "SCHEDULED",
    "employeeCode": "EMP002",
    "roomCode": "P-02",
    "appointmentStartTime": "2025-12-19T14:00:00"
  }
}
```

---

### Test 5: Excel OUT_OF_STOCK Highlighting

**Prerequisites:**

- Login as inventory manager (password: 123456)
- Some items with OUT_OF_STOCK status in database

**Test Case:**

```bash
GET /api/v1/warehouse/summary/export?stockStatus=OUT_OF_STOCK
Authorization: Bearer {inventory_manager_token}
```

**Expected:**

- Excel file downloads successfully
- OUT_OF_STOCK rows have background color #fa6666 (light red)
- Font is bold
- All cells in the row are highlighted (not just one column)

**Verification:**

1. Open Excel file
2. Select OUT_OF_STOCK row
3. Check cell fill color
4. RGB values should be: R=250, G=102, B=102

---

## Rollback Plan

If issues occur, revert these commits:

1. AppointmentAutoStatusService.java (Vietnamese messages)
2. AppointmentStatusService.java (NO_SHOW plan item mapping)
3. AppointmentDelayService.java (NO_SHOW delay support)
4. AppointmentRescheduleService.java (NO_SHOW reschedule support)
5. WarehouseExcelExportService.java (Excel color change)
6. dental-clinic-seed-data.sql (permission description)

No data migration needed for rollback.

---

## Related APIs

- API 3.5: Update Appointment Status (existing)
- API 3.6: Delay Appointment (updated to support NO_SHOW)
- API 3.7: Reschedule Appointment (updated to support NO_SHOW)
- API 5.6: Update Plan Item Status (existing)
- API 6.1.1: Export Inventory Summary (updated with color change)

---

## FE Integration Notes

### For Issue 1 (Vietnamese Messages)

- No FE changes needed
- Notes field will show Vietnamese text automatically
- Date format changed from ISO to Vietnamese style

### For Issue 2 (Plan Item Status)

- After appointment becomes NO_SHOW:
  - Refresh treatment plan view
  - Item status will be READY_FOR_BOOKING
  - "Book Appointment" button should be enabled

### For Issue 3 (Reschedule NO_SHOW)

- Enable "Delay" button for NO_SHOW appointments
- Enable "Reschedule" button for NO_SHOW appointments
- Use existing APIs (no new endpoints)

### For Issue 4 (Excel Color)

- No FE changes needed
- Excel export will show new color automatically

---

**Implementation Date:** 2025-12-18
**Priority:** MEDIUM
**Status:** COMPLETED
