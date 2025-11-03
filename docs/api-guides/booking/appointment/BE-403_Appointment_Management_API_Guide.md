# BE-403: Appointment Management API Guide

## Table of Contents

1. [Overview](#overview)
2. [P3.1: Find Available Times](#p31-find-available-times)
3. [P3.2: Create Appointment](#p32-create-appointment)
4. [Business Rules](#business-rules)
5. [Request/Response Examples](#requestresponse-examples)
6. [Error Scenarios](#error-scenarios)
7. [Postman Testing Guide](#postman-testing-guide)
8. [TypeScript Integration](#typescript-integration)
9. [Performance Notes](#performance-notes)

---

## Overview

This guide covers the complete Appointment Management module (BE-403) with two main APIs:

**P3.1: Find Available Times** - Query available time slots before booking
**P3.2: Create Appointment** - Book the appointment after selecting a slot

---

## P3.1: Find Available Times

**Purpose**: Find available time slots for booking appointments based on:

- Doctor's availability (work shifts minus busy appointments)
- Services requested (duration + buffer time calculation)
- Participants' availability (optional assistants)
- Compatible rooms (based on room_services V16)

**Use Case**: Receptionist needs to find free slots to book a patient for "Cắm trụ Implant" with Dr. Nguyen on Oct 30, 2025.

**Algorithm**: Intersection-based availability check

1. Validate inputs (date not in past, employee/services active)
2. Calculate total duration: SUM(serviceDuration + buffer)
3. Check doctor has required specializations
4. Filter compatible rooms (room_services junction table)
5. Get doctor's work shifts (employee_shifts) - "source of truth"
6. Subtract busy times (appointments + participants' busy times)
7. Split free intervals into 15-min slots
8. Return slots with available compatible rooms

---

## API Specification

### Endpoint

```
GET /api/v1/appointments/available-times
```

### Authorization

```
Required Permission: CREATE_APPOINTMENT
```

### Query Parameters

| Parameter          | Type          | Required | Description                                      | Example                        |
| ------------------ | ------------- | -------- | ------------------------------------------------ | ------------------------------ |
| `date`             | String        | ✅ Yes   | Date to search (YYYY-MM-DD), must not be in past | `2025-10-30`                   |
| `employeeCode`     | String        | ✅ Yes   | Employee code of primary doctor                  | `BS-NGUYEN-VAN-A`              |
| `serviceCodes`     | Array<String> | ✅ Yes   | List of service codes (at least 1)               | `["IMPLANT_01", "BONE_GRAFT"]` |
| `participantCodes` | Array<String> | ❌ No    | List of participant codes (assistants)           | `["PT-001", "PT-002"]`         |

### Response Format

**Success (200 OK)**:

```json
{
  "totalDurationNeeded": 120,
  "availableSlots": [
    {
      "startTime": "2025-10-30T09:30:00",
      "availableCompatibleRoomCodes": ["P-IMPLANT-01", "P-IMPLANT-02"],
      "note": null
    },
    {
      "startTime": "2025-10-30T14:00:00",
      "availableCompatibleRoomCodes": ["P-IMPLANT-01"],
      "note": null
    }
  ],
  "message": null
}
```

**No Compatible Rooms (200 OK)**:

```json
{
  "totalDurationNeeded": 60,
  "availableSlots": [],
  "message": "Không có phòng nào hỗ trợ các dịch vụ này"
}
```

---

## Business Rules

### Rule 1: Date Validation

- ✅ **MUST** be in format `YYYY-MM-DD`
- ✅ **MUST NOT** be in the past (`date >= LocalDate.now()`)
- ❌ **Example**: Requesting `2025-10-20` on Oct 25 → `400 DATE_IN_PAST`

### Rule 2: Employee Validation

- ✅ **MUST** exist in database
- ✅ **MUST** be active (`is_active = true`)
- ❌ **Example**: `BS-JOHN-DOE` not found → `404 EMPLOYEE_NOT_FOUND`

### Rule 3: Services Validation

- ✅ **ALL** service codes **MUST** exist
- ✅ **ALL** services **MUST** be active
- ❌ **Example**: `["IMPLANT_01", "INVALID_CODE"]` → `404 SERVICES_NOT_FOUND`
- ❌ **Example**: Service is `is_active = false` → `400 SERVICES_INACTIVE`

### Rule 4: Duration Calculation

```
totalDuration = SUM(service.defaultDurationMinutes + service.defaultBufferMinutes)
```

**Example**:

- Service 1: 45 min + 15 min buffer = 60 min
- Service 2: 30 min + 10 min buffer = 40 min
- **Total**: 100 min

### Rule 5: Doctor Specialization Check

- ✅ Doctor **MUST** have ALL required specializations for the services
- ❌ **Example**: Service requires "Implant Specialist" but doctor doesn't have it → `409 EMPLOYEE_NOT_QUALIFIED`

### Rule 6: Compatible Rooms (V16)

- ✅ Rooms **MUST** support **ALL** requested services (from `room_services`)
- ✅ Uses SQL: `SELECT room_id WHERE service_id IN (...) GROUP BY room_id HAVING COUNT(*) = N`
- ❌ **Example**: No room supports both "Implant" AND "X-ray" → Empty slots with message

### Rule 7: Holiday Handling

- ✅ **Automatically handled** by `employee_shifts` table
- ✅ If date is holiday, doctor has no shifts → Empty response
- ✅ **Note**: Holiday check is NOT done explicitly in API (shifts are "source of truth")

### Rule 8: Shift as Source of Truth

- ✅ Only work shifts create availability windows
- ✅ Busy appointments SUBTRACT from these windows
- ✅ **Example**: Shift 8:00-17:00, appointment 9:00-10:00 → Available: [8:00-9:00, 10:00-17:00]

### Rule 9: Participant Availability

- ✅ If `participantCodes` provided, check their busy times too
- ✅ Participant can be busy as:
  - Primary doctor in another appointment
  - Participant in another appointment
- ❌ **Example**: Assistant busy 10:00-11:00 → That slot excluded from results

### Rule 10: Slot Interval

- ✅ Slots are split every **15 minutes**
- ✅ Only show slots where full duration fits
- ❌ **Example**: Free 9:00-9:40 with 60 min needed → No slot shown (not enough time)

---

## Request Examples

### Example 1: Simple Request (No Participants)

```http
GET /api/v1/appointments/available-times?date=2025-10-30&employeeCode=BS-001&serviceCodes=SCALING_L1
```

### Example 2: Multiple Services

```http
GET /api/v1/appointments/available-times?date=2025-11-15&employeeCode=BS-IMPLANT-01&serviceCodes=IMPLANT_01&serviceCodes=BONE_GRAFT&serviceCodes=XRAY_3D
```

### Example 3: With Participants (Assistants)

```http
GET /api/v1/appointments/available-times?date=2025-12-01&employeeCode=BS-002&serviceCodes=SURGERY_COMPLEX&participantCodes=PT-001&participantCodes=PT-002
```

### Example 4: Using Postman

**Query Params Tab**:
| KEY | VALUE |
|-----|-------|
| date | 2025-10-30 |
| employeeCode | BS-NGUYEN-VAN-A |
| serviceCodes | IMPLANT_01 |
| serviceCodes | BONE_GRAFT |
| participantCodes | PT-ASSISTANT-01 |

---

## Response Examples

### Success Case: Found Slots

```json
{
  "totalDurationNeeded": 120,
  "availableSlots": [
    {
      "startTime": "2025-10-30T08:00:00",
      "availableCompatibleRoomCodes": [
        "P-IMPLANT-01",
        "P-IMPLANT-02",
        "P-IMPLANT-03"
      ],
      "note": null
    },
    {
      "startTime": "2025-10-30T08:15:00",
      "availableCompatibleRoomCodes": ["P-IMPLANT-01", "P-IMPLANT-03"],
      "note": null
    },
    {
      "startTime": "2025-10-30T14:00:00",
      "availableCompatibleRoomCodes": ["P-IMPLANT-02"],
      "note": null
    }
  ],
  "message": null
}
```

**Interpretation**:

- Need 120 minutes total
- 3 time slots available
- At 8:00, can use any of 3 rooms
- At 8:15, room P-IMPLANT-02 became busy
- At 14:00, only 1 room available

### Edge Case: No Compatible Rooms

```json
{
  "totalDurationNeeded": 60,
  "availableSlots": [],
  "message": "Không có phòng nào hỗ trợ các dịch vụ này"
}
```

**Reason**: Services requested require equipment not in any room (e.g., X-ray + Surgery combo)

### Edge Case: Doctor Has No Shifts

```json
{
  "totalDurationNeeded": 45,
  "availableSlots": [],
  "message": null
}
```

**Reason**: Doctor not scheduled to work on that date (or it's a holiday)

### Edge Case: All Slots Busy

```json
{
  "totalDurationNeeded": 30,
  "availableSlots": [],
  "message": null
}
```

**Reason**: Doctor has shifts but all times are booked with appointments

---

## Error Scenarios

### Error 1: Invalid Date Format

**Request**:

```http
GET /api/v1/appointments/available-times?date=30-10-2025&employeeCode=BS-001&serviceCodes=SCALING_L1
```

**Response**: `400 Bad Request`

```json
{
  "title": "Invalid date format: 30-10-2025",
  "entityName": "appointment",
  "errorKey": "INVALID_DATE"
}
```

### Error 2: Date in Past

**Request**:

```http
GET /api/v1/appointments/available-times?date=2025-10-01&employeeCode=BS-001&serviceCodes=SCALING_L1
```

(Assuming today is Oct 25)
**Response**: `400 Bad Request`

```json
{
  "title": "Cannot search for past dates: 2025-10-01",
  "entityName": "appointment",
  "errorKey": "DATE_IN_PAST"
}
```

### Error 3: Employee Not Found

**Request**:

```http
GET /api/v1/appointments/available-times?date=2025-11-01&employeeCode=INVALID_CODE&serviceCodes=SCALING_L1
```

**Response**: `404 Not Found`

```json
{
  "title": "Employee not found or inactive: INVALID_CODE",
  "entityName": "appointment",
  "errorKey": "EMPLOYEE_NOT_FOUND"
}
```

### Error 4: Services Not Found

**Request**:

```http
GET /api/v1/appointments/available-times?date=2025-11-01&employeeCode=BS-001&serviceCodes=INVALID_SERVICE&serviceCodes=SCALING_L1
```

**Response**: `404 Not Found`

```json
{
  "title": "Services not found: INVALID_SERVICE",
  "entityName": "appointment",
  "errorKey": "SERVICES_NOT_FOUND"
}
```

### Error 5: Services Inactive

**Request**:

```http
GET /api/v1/appointments/available-times?date=2025-11-01&employeeCode=BS-001&serviceCodes=OLD_SERVICE
```

(Assuming OLD_SERVICE has `is_active = false`)
**Response**: `400 Bad Request`

```json
{
  "title": "Services are inactive: OLD_SERVICE",
  "entityName": "appointment",
  "errorKey": "SERVICES_INACTIVE"
}
```

### Error 6: Employee Not Qualified

**Request**:

```http
GET /api/v1/appointments/available-times?date=2025-11-01&employeeCode=BS-GENERAL&serviceCodes=IMPLANT_ADVANCED
```

(BS-GENERAL doesn't have Implant Specialist certification)
**Response**: `409 Conflict`

```json
{
  "title": "Employee does not have required specializations for these services",
  "entityName": "appointment",
  "errorKey": "EMPLOYEE_NOT_QUALIFIED"
}
```

### Error 7: Participant Not Found

**Request**:

```http
GET /api/v1/appointments/available-times?date=2025-11-01&employeeCode=BS-001&serviceCodes=SURGERY&participantCodes=PT-INVALID
```

**Response**: `404 Not Found`

```json
{
  "title": "Participant not found or inactive: PT-INVALID",
  "entityName": "appointment",
  "errorKey": "PARTICIPANT_NOT_FOUND"
}
```

---

## Postman Testing Guide

### Test Case 1: Happy Path - Find Slots Successfully

**Setup**:

1. Create test employee: `BS-TEST-001` (active, has shifts on target date)
2. Create test services: `TEST_SERVICE_01` (30 min + 10 buffer)
3. Ensure room `TEST_ROOM_01` supports this service

**Request**:

```
GET {{base_url}}/appointments/available-times
  ?date=2025-11-15
  &employeeCode=BS-TEST-001
  &serviceCodes=TEST_SERVICE_01
```

**Assertions**:

```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
pm.test("Has totalDurationNeeded", () => {
  const json = pm.response.json();
  pm.expect(json.totalDurationNeeded).to.equal(40); // 30 + 10
});
pm.test("Has availableSlots array", () => {
  const json = pm.response.json();
  pm.expect(json.availableSlots).to.be.an("array");
});
```

### Test Case 2: Date in Past - Should Return 400

**Request**:

```
GET {{base_url}}/appointments/available-times
  ?date=2025-10-01
  &employeeCode=BS-TEST-001
  &serviceCodes=TEST_SERVICE_01
```

**Assertions**:

```javascript
pm.test("Status is 400", () => pm.response.to.have.status(400));
pm.test("Error key is DATE_IN_PAST", () => {
  const json = pm.response.json();
  pm.expect(json.errorKey).to.equal("DATE_IN_PAST");
});
```

### Test Case 3: Invalid Employee - Should Return 404

**Request**:

```
GET {{base_url}}/appointments/available-times
  ?date=2025-11-15
  &employeeCode=NONEXISTENT
  &serviceCodes=TEST_SERVICE_01
```

**Assertions**:

```javascript
pm.test("Status is 404", () => pm.response.to.have.status(404));
pm.test("Error key is EMPLOYEE_NOT_FOUND", () => {
  const json = pm.response.json();
  pm.expect(json.errorKey).to.equal("EMPLOYEE_NOT_FOUND");
});
```

### Test Case 4: No Compatible Rooms

**Setup**: Create services that no room supports

**Request**:

```
GET {{base_url}}/appointments/available-times
  ?date=2025-11-15
  &employeeCode=BS-TEST-001
  &serviceCodes=UNCOMMON_SERVICE_A
  &serviceCodes=UNCOMMON_SERVICE_B
```

**Assertions**:

```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
pm.test("Slots array is empty", () => {
  const json = pm.response.json();
  pm.expect(json.availableSlots).to.have.lengthOf(0);
});
pm.test("Has message about no compatible rooms", () => {
  const json = pm.response.json();
  pm.expect(json.message).to.include("Không có phòng nào");
});
```

### Test Case 5: With Participants

**Setup**: Create participant employees

**Request**:

```
GET {{base_url}}/appointments/available-times
  ?date=2025-11-15
  &employeeCode=BS-TEST-001
  &serviceCodes=TEST_SERVICE_01
  &participantCodes=PT-TEST-001
  &participantCodes=PT-TEST-002
```

**Assertions**:

```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
pm.test("Slots consider participant availability", () => {
  const json = pm.response.json();
  // Slots should exclude times when participants are busy
  pm.expect(json.availableSlots).to.be.an("array");
});
```

---

## TypeScript Integration

### Interface Definitions

```typescript
interface AvailableTimesRequest {
  date: string; // YYYY-MM-DD
  employeeCode: string;
  serviceCodes: string[];
  participantCodes?: string[];
}

interface TimeSlot {
  startTime: string; // ISO 8601
  availableCompatibleRoomCodes: string[];
  note?: string | null;
}

interface AvailableTimesResponse {
  totalDurationNeeded: number; // minutes
  availableSlots: TimeSlot[];
  message?: string | null;
}
```

### Example Usage (React + Axios)

```typescript
import axios from "axios";

async function findAvailableSlots(
  date: string,
  doctorCode: string,
  services: string[]
): Promise<AvailableTimesResponse> {
  const response = await axios.get<AvailableTimesResponse>(
    "/api/v1/appointments/available-times",
    {
      params: {
        date,
        employeeCode: doctorCode,
        serviceCodes: services,
      },
      paramsSerializer: {
        indexes: null, // Use array format: serviceCodes=A&serviceCodes=B
      },
    }
  );
  return response.data;
}

// Usage
const slots = await findAvailableSlots("2025-10-30", "BS-NGUYEN-VAN-A", [
  "IMPLANT_01",
  "BONE_GRAFT",
]);

console.log(`Need ${slots.totalDurationNeeded} minutes`);
console.log(`Found ${slots.availableSlots.length} slots`);

slots.availableSlots.forEach((slot, index) => {
  console.log(`Slot ${index + 1}: ${slot.startTime}`);
  console.log(`  Rooms: ${slot.availableCompatibleRoomCodes.join(", ")}`);
});
```

### Display in UI (Example Component)

```tsx
function AvailableSlotsPicker({ slots }: { slots: TimeSlot[] }) {
  return (
    <div className="slots-grid">
      {slots.map((slot, i) => (
        <div key={i} className="slot-card">
          <h4>{formatTime(slot.startTime)}</h4>
          <p>Available Rooms:</p>
          <ul>
            {slot.availableCompatibleRoomCodes.map((roomCode) => (
              <li key={roomCode}>{roomCode}</li>
            ))}
          </ul>
          <button onClick={() => selectSlot(slot)}>Book This Slot</button>
        </div>
      ))}
    </div>
  );
}
```

---

## Performance Notes

### Optimization 1: Caching Employee Shifts

```java
// Consider caching employee_shifts for the day to avoid repeated DB queries
@Cacheable(value = "employeeShifts", key = "#employeeId + '-' + #date")
public List<EmployeeShift> getShiftsForDate(Integer employeeId, LocalDate date) {
    return shiftRepository.findByEmployeeAndDate(employeeId, date);
}
```

### Optimization 2: Batch Query for Participants

```java
// Instead of querying each participant separately, use IN clause
List<Integer> participantIds = // ...
List<EmployeeShift> allParticipantShifts =
    shiftRepository.findByEmployeeIdsAndDate(participantIds, date);
```

### Optimization 3: Database Indexes

Ensure these indexes exist:

```sql
CREATE INDEX idx_appt_employee_time ON appointments(employee_id, appointment_start_time, appointment_end_time);
CREATE INDEX idx_appt_room_time ON appointments(room_id, appointment_start_time, appointment_end_time);
CREATE INDEX idx_employee_shift_date ON employee_shifts(employee_id, work_date);
```

### Optimization 4: Limit Slot Generation

```java
// Don't generate slots beyond clinic closing time
private static final LocalTime CLINIC_CLOSE = LocalTime.of(18, 0);

while (slotTime.toLocalTime().isBefore(CLINIC_CLOSE)) {
    // Generate slot...
}
```

### Performance Expectations

- **Single Employee, 1 Service**: < 100ms
- **With 2 Participants, 3 Services**: < 300ms
- **Complex (5 Services, 3 Participants)**: < 500ms

**Note**: If response time > 500ms, consider:

1. Adding Redis cache for shifts
2. Pre-computing busy times daily
3. Using database materialized views

---

## Known Limitations & Future Enhancements

### Current Limitations

1. **Room Availability Check Disabled**: Due to schema mismatch (`rooms.room_id` VARCHAR vs `appointments.room_id` INTEGER), room conflict checking is temporarily disabled. All compatible rooms are returned without checking if they're busy.

2. **No Equipment Tracking**: Cannot check if specific equipment (e.g., X-ray machine) is available.

3. **No Priority Slots**: All slots have equal priority (no VIP/urgent slot reservation).

### Phase 2 Enhancements

1. **Service Dependencies**: Check if patient has completed prerequisite services (e.g., "Nâng xoang" before "Cắm trụ Implant").

2. **Patient Validation**: Check patient payment status, allergies before showing slots.

3. **Smart Recommendations**: Suggest best slots based on doctor's success rate at different times.

4. **Multi-Day Search**: Find next available slot across multiple days.

---

## P3.2: Create Appointment

### Endpoint

```
POST /api/v1/appointments
```

### Authorization

```
Required Permission: CREATE_APPOINTMENT
```

### Request Body (JSON)

```json
{
  "patientCode": "BN-1001",
  "employeeCode": "DR_AN_KHOA",
  "roomCode": "P-IMPLANT-01",
  "serviceCodes": ["SV-IMPLANT", "SV-NANGXOANG"],
  "appointmentStartTime": "2025-10-30T09:30:00",
  "participantCodes": ["PT-BINH", "PT-AN"],
  "notes": "Bệnh nhân có tiền sử cao huyết áp"
}
```

| Field                  | Type          | Required | Description                               |
| ---------------------- | ------------- | -------- | ----------------------------------------- |
| `patientCode`          | String        | ✅ Yes   | Patient code (must exist & active)        |
| `employeeCode`         | String        | ✅ Yes   | Primary doctor code (must exist & active) |
| `roomCode`             | String        | ✅ Yes   | Room code (from P3.1 available slots)     |
| `serviceCodes`         | Array<String> | ✅ Yes   | Service codes (at least 1)                |
| `appointmentStartTime` | String        | ✅ Yes   | ISO 8601 format: YYYY-MM-DDTHH:mm:ss      |
| `participantCodes`     | Array<String> | ❌ No    | Participant/assistant codes               |
| `notes`                | String        | ❌ No    | Optional notes from receptionist          |

### Business Logic (9-Step Transaction)

**STEP 1: Get Creator**

- Extract `employee_id` from SecurityContext (logged-in user)
- Populate `created_by` field

**STEP 2: Validate Resources**

- ✅ Patient exists & active
- ✅ Doctor exists & active
- ✅ Room exists & active
- ✅ All services exist & active
- ✅ All participants exist & active

**STEP 3: Validate Doctor Specializations**

- Check doctor has ALL required specializations for services
- Uses `employee_specializations` junction table (ManyToMany relationship)

**STEP 4: Validate Room Compatibility** (V16)

- Check room supports ALL services via `room_services` junction table

**STEP 5: Calculate Duration**

- `totalDuration = SUM(service.defaultDurationMinutes + service.defaultBufferMinutes)`
- `appointmentEndTime = startTime + totalDuration`

**STEP 6: Validate Shifts**

- Doctor must have `employee_shifts` covering time range
- All participants must have shifts covering time range

**STEP 7: Check Conflicts** (CRITICAL - Prevents Double Booking)

- ❌ Doctor has no conflicting appointment
- ❌ Room has no conflicting appointment
- ❌ **Patient has no conflicting appointment** (NEW - prevents same-time booking)
- ❌ Participants have no conflicts (as primary doctor OR assistant)

**STEP 8: Insert Data**

```sql
-- Insert appointment
INSERT INTO appointments (...) VALUES (...);

-- Insert services (loop)
INSERT INTO appointment_services (appointment_id, service_id) VALUES (...);

-- Insert participants with default role 'ASSISTANT'
INSERT INTO appointment_participants (appointment_id, employee_id, role)
VALUES (..., ..., 'ASSISTANT');

-- Insert audit log
INSERT INTO appointment_audit_logs (appointment_id, action_type, changed_by_employee_id)
VALUES (..., 'CREATE', ...);
```

**STEP 9: Return Response**

- Build nested response with summaries

### Response (201 Created)

```json
{
  "appointmentCode": "APT-20251030-001",
  "status": "SCHEDULED",
  "appointmentStartTime": "2025-10-30T09:30:00",
  "appointmentEndTime": "2025-10-30T10:15:00",
  "expectedDurationMinutes": 45,
  "patient": {
    "patientCode": "BN-1001",
    "fullName": "Nguyen Van A"
  },
  "doctor": {
    "employeeCode": "DR_AN_KHOA",
    "fullName": "Dr. Le An Khoa"
  },
  "room": {
    "roomCode": "P-IMPLANT-01",
    "roomName": "Phòng Implant 01"
  },
  "services": [
    {
      "serviceCode": "SV-IMPLANT",
      "serviceName": "Cắm trụ Implant"
    },
    {
      "serviceCode": "SV-NANGXOANG",
      "serviceName": "Nâng xoang"
    }
  ],
  "participants": [
    {
      "employeeCode": "PT-BINH",
      "fullName": "Phụ tá Bình",
      "role": "ASSISTANT"
    },
    {
      "employeeCode": "PT-AN",
      "fullName": "Phụ tá An",
      "role": "ASSISTANT"
    }
  ]
}
```

### Error Codes (P3.2)

| HTTP Status | Error Key                   | Description                                  |
| ----------- | --------------------------- | -------------------------------------------- |
| 400         | `PATIENT_NOT_FOUND`         | Patient code doesn't exist                   |
| 400         | `PATIENT_INACTIVE`          | Patient is not active                        |
| 409         | `PATIENT_HAS_CONFLICT`      | Patient already has appointment at same time |
| 400         | `EMPLOYEE_NOT_FOUND`        | Doctor code doesn't exist or inactive        |
| 409         | `EMPLOYEE_NOT_QUALIFIED`    | Doctor lacks required specializations        |
| 409         | `EMPLOYEE_NOT_SCHEDULED`    | Doctor has no shift covering time range      |
| 409         | `EMPLOYEE_SLOT_TAKEN`       | Doctor is busy during requested time         |
| 400         | `ROOM_NOT_FOUND`            | Room code doesn't exist                      |
| 400         | `ROOM_INACTIVE`             | Room is not active                           |
| 409         | `ROOM_NOT_COMPATIBLE`       | Room doesn't support all services            |
| 409         | `ROOM_SLOT_TAKEN`           | Room is already booked                       |
| 400         | `SERVICES_NOT_FOUND`        | One or more service codes don't exist        |
| 400         | `SERVICES_INACTIVE`         | One or more services are inactive            |
| 400         | `PARTICIPANT_NOT_FOUND`     | Participant code doesn't exist or inactive   |
| 409         | `PARTICIPANT_NOT_SCHEDULED` | Participant has no shift                     |
| 409         | `PARTICIPANT_SLOT_TAKEN`    | Participant is busy (as doctor or assistant) |
| 400         | `START_TIME_IN_PAST`        | Appointment time must be in future           |
| 400         | `INVALID_START_TIME`        | Invalid ISO 8601 format                      |
| 401         | `NOT_AUTHENTICATED`         | User not logged in                           |
| 400         | `NOT_EMPLOYEE_ACCOUNT`      | Logged-in account not linked to employee     |

### Postman Test Case (P3.2)

**Test Case 1: Success - Create Appointment**

```
POST {{base_url}}/appointments
Headers:
  Authorization: Bearer {{token}}
  Content-Type: application/json

Body:
{
  "patientCode": "BN-TEST-001",
  "employeeCode": "BS-TEST-DOCTOR",
  "roomCode": "P-TEST-ROOM",
  "serviceCodes": ["TEST_SERVICE_01"],
  "appointmentStartTime": "2025-11-15T09:00:00",
  "notes": "Test appointment creation"
}

Assertions:
✅ Status is 201
✅ Response has appointmentCode
✅ Status is "SCHEDULED"
✅ expectedDurationMinutes matches service duration + buffer
```

**Test Case 2: Doctor Already Busy**

```
POST {{base_url}}/appointments
Body: (Same doctor, same time as existing appointment)

Expected:
❌ 409 Conflict
❌ errorKey: "EMPLOYEE_SLOT_TAKEN"
```

**Test Case 3: Patient Double Booking**

```
POST {{base_url}}/appointments
Body: (Same patient, same time, different doctor)

Expected:
❌ 409 Conflict
❌ errorKey: "PATIENT_HAS_CONFLICT"
```

### TypeScript Integration (P3.2)

```typescript
interface CreateAppointmentRequest {
  patientCode: string;
  employeeCode: string;
  roomCode: string;
  serviceCodes: string[];
  appointmentStartTime: string; // ISO 8601
  participantCodes?: string[];
  notes?: string;
}

interface CreateAppointmentResponse {
  appointmentCode: string;
  status: "SCHEDULED";
  appointmentStartTime: string;
  appointmentEndTime: string;
  expectedDurationMinutes: number;
  patient: {
    patientCode: string;
    fullName: string;
  };
  doctor: {
    employeeCode: string;
    fullName: string;
  };
  room: {
    roomCode: string;
    roomName: string;
  };
  services: Array<{
    serviceCode: string;
    serviceName: string;
  }>;
  participants: Array<{
    employeeCode: string;
    fullName: string;
    role: "ASSISTANT" | "SECONDARY_DOCTOR" | "OBSERVER";
  }>;
}

// Example Usage
async function createAppointment(
  request: CreateAppointmentRequest
): Promise<CreateAppointmentResponse> {
  const response = await axios.post<CreateAppointmentResponse>(
    "/api/v1/appointments",
    request
  );
  return response.data;
}

// Usage with P3.1 result
const availableSlot = availableSlots[0]; // From P3.1
const roomCode = availableSlot.availableCompatibleRoomCodes[0];

const appointment = await createAppointment({
  patientCode: "BN-1001",
  employeeCode: "DR_AN_KHOA",
  roomCode: roomCode,
  serviceCodes: ["SV-IMPLANT"],
  appointmentStartTime: availableSlot.startTime,
  notes: "Booked via web portal",
});

console.log(`Created: ${appointment.appointmentCode}`);
```

---

## Workflow: P3.1 → P3.2

**Step 1: Find Available Times** (P3.1)

```typescript
const slots = await findAvailableSlots("2025-11-15", "DR_AN_KHOA", [
  "SV-IMPLANT",
]);
// Returns: [{ startTime: '2025-11-15T09:00:00', availableCompatibleRoomCodes: ['P-IMPLANT-01'] }]
```

**Step 2: User Selects Slot**

```typescript
const selectedSlot = slots[2]; // User picked 3rd slot
const selectedRoom = selectedSlot.availableCompatibleRoomCodes[0]; // First room
```

**Step 3: Create Appointment** (P3.2)

```typescript
const appointment = await createAppointment({
  patientCode: patientCode,
  employeeCode: "DR_AN_KHOA",
  roomCode: selectedRoom,
  serviceCodes: ["SV-IMPLANT"],
  appointmentStartTime: selectedSlot.startTime,
});
// Returns: 201 Created with appointmentCode
```

---

## Contact & Support

For questions or issues, contact:

- **Backend Team**: backend@dentalclinic.com
- **API Documentation**: https://api.dentalclinic.com/docs
- **Slack Channel**: #api-support
