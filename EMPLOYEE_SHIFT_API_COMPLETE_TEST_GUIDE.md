# Employee Shift Management API - Complete Step-by-Step Test Guide

## 📋 Overview
This guide assumes **NO prior data exists** and will walk you through the complete workflow:
1. Understanding prerequisites
2. Creating test data (employees, work shifts)
3. Testing all 6 endpoints in logical order
4. Validating all business rules and error cases

---

## 🔐 Step 0: Authentication Setup

### Test Users Available (from seed data)
| Username | Password | Role | Shift Permissions | Employee ID |
|----------|----------|------|-------------------|-------------|
| `manager` | `123456` | MANAGER | VIEW_SHIFTS_ALL, VIEW_SHIFTS_SUMMARY, CREATE_SHIFTS, UPDATE_SHIFTS, DELETE_SHIFTS | 7 |
| `nhasi1` | `123456` | DOCTOR | VIEW_SHIFTS_OWN (only own shifts) | 2 |
| `nhasi2` | `123456` | DOCTOR | VIEW_SHIFTS_OWN (only own shifts) | 3 |
| `yta` | `123456` | NURSE | VIEW_SHIFTS_OWN (only own shifts) | 6 |
| `letan` | `123456` | RECEPTIONIST | VIEW_SHIFTS_OWN (only own shifts) | 4 |

### 0.1 Login as Manager (Full Access)
**Request:**
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "manager",
  "password": "123456"
}
```

**Expected Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "refresh_token": "def502001a2b3c...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

**Action:** Copy the `access_token` - you'll use it in all subsequent requests as:
```
Authorization: Bearer <access_token>
```

### 0.2 Login as Doctor (Limited Access)
**Request:**
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "nhasi1",
  "password": "123456"
}
```

**Action:** Save this token separately to test RBAC restrictions later.

---

## 📊 Step 1: Check What Work Shifts Exist

Before creating employee shifts, you need to know what work shifts are available in the system.

### 1.1 Query Database to See Available Work Shifts

**Option A: Direct Database Query**
```sql
SELECT * FROM work_shifts ORDER BY work_shift_id;
```

**Expected Work Shifts (from seed data):**
| work_shift_id | shift_name | start_time | end_time |
|---------------|------------|------------|----------|
| WKS_MORNING_01 | Ca sáng 1 | 07:00:00 | 12:00:00 |
| WKS_AFTERNOON_01 | Ca chiều 1 | 13:00:00 | 18:00:00 |
| WKS_MORNING_02 | Ca sáng 2 | 08:00:00 | 13:00:00 |
| WKS_AFTERNOON_02 | Ca chiều 2 | 14:00:00 | 19:00:00 |

**Option B: Check if you have a Work Shift API endpoint**
If your system has a GET endpoint for work shifts, use that. Otherwise, use the IDs above.

---

## 🎯 Step 2: Verify Empty State (Calendar View)

Check that NO shifts exist for the date range you'll be testing.

### 2.1 Query Empty Calendar (Manager)
**Request:**
```http
GET http://localhost:8080/api/v1/shifts?start_date=2025-10-29&end_date=2025-10-31
Authorization: Bearer <manager_token>
```

**Expected Response (200 OK):**
```json
[]
```

**✅ Success Criteria:**
- Response is an empty array
- No shifts exist for Oct 29-31, 2025

---

## ✨ Step 3: Create Your First Employee Shift

Now let's create shifts for testing. We'll create multiple shifts to test different scenarios.

### 3.1 Create Morning Shift for Doctor (Employee ID: 2)
**Request:**
```http
POST http://localhost:8080/api/v1/shifts
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "employee_id": 2,
  "work_date": "2025-10-29",
  "work_shift_id": "WKS_MORNING_01",
  "notes": "Scheduled morning shift"
}
```

**Expected Response (201 Created):**
```json
{
  "employee_shift_id": "EMS251029001",
  "work_date": "2025-10-29",
  "status": "SCHEDULED",
  "employee": {
    "employee_id": 2,
    "full_name": "Dr. Nguyen Van A"
  },
  "work_shift": {
    "work_shift_id": "WKS_MORNING_01",
    "shift_name": "Ca sáng 1"
  },
  "notes": "Scheduled morning shift",
  "source": "MANUAL",
  "is_overtime": false
}
```

**✅ Success Criteria:**
- Status code: 201 Created
- `employee_shift_id` follows format: EMSyyMMddSEQ (e.g., EMS251029001)
- `status` is "SCHEDULED"
- `source` is "MANUAL"
- `is_overtime` is false
- Employee and work shift details are included

**📝 Note:** Save the `employee_shift_id` for later tests!

### 3.2 Create Afternoon Shift for Same Doctor
**Request:**
```http
POST http://localhost:8080/api/v1/shifts
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "employee_id": 2,
  "work_date": "2025-10-29",
  "work_shift_id": "WKS_AFTERNOON_01",
  "notes": "Afternoon shift"
}
```

**Expected Response (201 Created):**
```json
{
  "employee_shift_id": "EMS251029002",
  "work_date": "2025-10-29",
  "status": "SCHEDULED",
  "employee": {
    "employee_id": 2,
    "full_name": "Dr. Nguyen Van A"
  },
  "work_shift": {
    "work_shift_id": "WKS_AFTERNOON_01",
    "shift_name": "Ca chiều 1"
  },
  "notes": "Afternoon shift",
  "source": "MANUAL",
  "is_overtime": false
}
```

### 3.3 Create Shift for Different Employee (Nurse)
**Request:**
```http
POST http://localhost:8080/api/v1/shifts
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "employee_id": 6,
  "work_date": "2025-10-30",
  "work_shift_id": "WKS_MORNING_01",
  "notes": "Nurse morning duty"
}
```

**Expected Response (201 Created):**
```json
{
  "employee_shift_id": "EMS251030001",
  "work_date": "2025-10-30",
  "status": "SCHEDULED",
  "employee": {
    "employee_id": 6,
    "full_name": "Phạm Thị Hoa"
  },
  "work_shift": {
    "work_shift_id": "WKS_MORNING_01",
    "shift_name": "Ca Sáng (8h-16h)"
  },
  "notes": "Nurse morning duty",
  "source": "MANUAL",
  "is_overtime": false
}
```

### 3.4 Create Overtime Shift
**Request:**
```http
POST http://localhost:8080/api/v1/shifts
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "employee_id": 3,
  "work_date": "2025-10-31",
  "work_shift_id": "WKS_AFTERNOON_02",
  "notes": "Emergency overtime coverage",
  "is_overtime": true
}
```

**Expected Response (201 Created):**
```json
{
  "employee_shift_id": "EMS251031001",
  "work_date": "2025-10-31",
  "status": "SCHEDULED",
  "employee": {
    "employee_id": 3,
    "full_name": "Dr. Tran Van B"
  },
  "work_shift": {
    "work_shift_id": "WKS_AFTERNOON_02",
    "shift_name": "Ca chiều 2"
  },
  "notes": "Emergency overtime coverage",
  "source": "MANUAL",
  "is_overtime": true
}
```

**✅ Success Criteria:**
- `is_overtime` is true
- All other fields correct

---

## 🔍 Step 4: Verify Data Creation (View Shifts)

Now that we have created shifts, let's verify they appear correctly in the calendar view.

### 4.1 View All Shifts in Date Range (Manager)
**Request:**
```http
GET http://localhost:8080/api/v1/shifts?start_date=2025-10-29&end_date=2025-10-31
Authorization: Bearer <manager_token>
```

**Expected Response (200 OK):**
```json
[
  {
    "employee_shift_id": "EMS251029001",
    "work_date": "2025-10-29",
    "status": "SCHEDULED",
    "employee": {
      "employee_id": 2,
      "full_name": "Dr. Nguyen Van A"
    },
    "work_shift": {
      "work_shift_id": "WKS_MORNING_01",
      "shift_name": "Ca sáng 1"
    },
    "notes": "Scheduled morning shift",
    "source": "MANUAL",
    "is_overtime": false
  },
  {
    "employee_shift_id": "EMS251029002",
    "work_date": "2025-10-29",
    "status": "SCHEDULED",
    "employee": {
      "employee_id": 2,
      "full_name": "Dr. Nguyen Van A"
    },
    "work_shift": {
      "work_shift_id": "WKS_AFTERNOON_01",
      "shift_name": "Ca chiều 1"
    },
    "notes": "Afternoon shift",
    "source": "MANUAL",
    "is_overtime": false
  },
  {
    "employee_shift_id": "EMS251030001",
    "work_date": "2025-10-30",
    "status": "SCHEDULED",
    "employee": {
      "employee_id": 4,
      "full_name": "Nurse Name"
    },
    "work_shift": {
      "work_shift_id": "WKS_MORNING_01",
      "shift_name": "Ca sáng 1"
    },
    "notes": "Nurse morning duty",
    "source": "MANUAL",
    "is_overtime": false
  },
  {
    "employee_shift_id": "EMS251031001",
    "work_date": "2025-10-31",
    "status": "SCHEDULED",
    "employee": {
      "employee_id": 3,
      "full_name": "Dr. Tran Van B"
    },
    "work_shift": {
      "work_shift_id": "WKS_AFTERNOON_02",
      "shift_name": "Ca chiều 2"
    },
    "notes": "Emergency overtime coverage",
    "source": "MANUAL",
    "is_overtime": true
  }
]
```

**✅ Success Criteria:**
- All 4 shifts appear
- Ordered by date and time
- All fields use snake_case
- Employee and work shift details nested correctly

### 4.2 Filter by Specific Employee
**Request:**
```http
GET http://localhost:8080/api/v1/shifts?start_date=2025-10-29&end_date=2025-10-31&employee_id=2
Authorization: Bearer <manager_token>
```

**Expected Response (200 OK):**
```json
[
  {
    "employee_shift_id": "EMS251029001",
    "work_date": "2025-10-29",
    ...
  },
  {
    "employee_shift_id": "EMS251029002",
    "work_date": "2025-10-29",
    ...
  }
]
```

**✅ Success Criteria:**
- Only 2 shifts returned (both for employee_id: 2)
- No shifts for other employees

### 4.3 View Own Shifts (Doctor Login)
**Request:**
```http
GET http://localhost:8080/api/v1/shifts?start_date=2025-10-29&end_date=2025-10-31
Authorization: Bearer <doctor_token>
```

**Expected Response (200 OK):**
```json
[
  {
    "employee_shift_id": "EMS251029001",
    "employee": {
      "employee_id": 2,
      ...
    },
    ...
  },
  {
    "employee_shift_id": "EMS251029002",
    "employee": {
      "employee_id": 2,
      ...
    },
    ...
  }
]
```

**✅ Success Criteria:**
- Only sees their own shifts (employee_id: 2)
- Cannot see nurse's or other doctor's shifts

---

## 📄 Step 5: Get Shift Details

### 5.1 Get Details of Specific Shift
**Request:**
```http
GET http://localhost:8080/api/v1/shifts/EMS251029001
Authorization: Bearer <manager_token>
```

**Expected Response (200 OK):**
```json
{
  "employee_shift_id": "EMS251029001",
  "work_date": "2025-10-29",
  "status": "SCHEDULED",
  "employee": {
    "employee_id": 2,
    "full_name": "Dr. Nguyen Van A"
  },
  "work_shift": {
    "work_shift_id": "WKS_MORNING_01",
    "shift_name": "Ca sáng 1"
  },
  "notes": "Scheduled morning shift",
  "source": "MANUAL",
  "is_overtime": false
}
```

**✅ Success Criteria:**
- Returns complete shift details
- Same format as list endpoint

---

## ✏️ Step 6: Update Shift Status (Attendance Tracking)

Now let's simulate the day of work and update attendance status.

### 6.1 Mark Employee as PRESENT
**Request:**
```http
PATCH http://localhost:8080/api/v1/shifts/EMS251029001
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "status": "PRESENT"
}
```

**Expected Response (200 OK):**
```json
{
  "employee_shift_id": "EMS251029001",
  "work_date": "2025-10-29",
  "status": "PRESENT",
  "employee": {
    "employee_id": 2,
    "full_name": "Dr. Nguyen Van A"
  },
  "work_shift": {
    "work_shift_id": "WKS_MORNING_01",
    "shift_name": "Ca sáng 1"
  },
  "notes": "Scheduled morning shift",
  "source": "MANUAL",
  "is_overtime": false
}
```

**✅ Success Criteria:**
- Status changed from "SCHEDULED" to "PRESENT"
- All other fields unchanged

### 6.2 Mark Another Employee as ABSENT with Reason
**Request:**
```http
PATCH http://localhost:8080/api/v1/shifts/EMS251029002
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "status": "ABSENT",
  "notes": "Nhân viên báo ốm đột xuất, không thể đến làm việc"
}
```

**Expected Response (200 OK):**
```json
{
  "employee_shift_id": "EMS251029002",
  "work_date": "2025-10-29",
  "status": "ABSENT",
  "employee": {
    "employee_id": 2,
    "full_name": "Dr. Nguyen Van A"
  },
  "work_shift": {
    "work_shift_id": "WKS_AFTERNOON_01",
    "shift_name": "Ca chiều 1"
  },
  "notes": "Nhân viên báo ốm đột xuất, không thể đến làm việc",
  "source": "MANUAL",
  "is_overtime": false
}
```

**✅ Success Criteria:**
- Status changed to "ABSENT"
- Notes updated with reason

### 6.3 Update Only Notes (Status Unchanged)
**Request:**
```http
PATCH http://localhost:8080/api/v1/shifts/EMS251030001
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "notes": "Đã làm việc chăm chỉ, hoàn thành tốt nhiệm vụ"
}
```

**Expected Response (200 OK):**
```json
{
  "employee_shift_id": "EMS251030001",
  "work_date": "2025-10-30",
  "status": "SCHEDULED",
  "employee": {
    "employee_id": 6,
    "full_name": "Phạm Thị Hoa"
  },
  "work_shift": {
    "work_shift_id": "WKS_MORNING_01",
    "shift_name": "Ca Sáng (8h-16h)"
  },
  "notes": "Đã làm việc chăm chỉ, hoàn thành tốt nhiệm vụ",
  "source": "MANUAL",
  "is_overtime": false
}
```

**✅ Success Criteria:**
- Status remains "SCHEDULED"
- Notes updated

---

## 🗑️ Step 7: Cancel a Shift

### 7.1 Cancel a SCHEDULED Shift (Allowed)
**Request:**
```http
DELETE http://localhost:8080/api/v1/shifts/EMS251031001
Authorization: Bearer <manager_token>
```

**Expected Response (200 OK):**
```json
{
  "employee_shift_id": "EMS251031001",
  "work_date": "2025-10-31",
  "status": "CANCELLED",
  "employee": {
    "employee_id": 3,
    "full_name": "Dr. Tran Van B"
  },
  "work_shift": {
    "work_shift_id": "WKS_AFTERNOON_02",
    "shift_name": "Ca chiều 2"
  },
  "notes": "Emergency overtime coverage",
  "source": "MANUAL",
  "is_overtime": true
}
```

**✅ Success Criteria:**
- Status changed to "CANCELLED"
- Shift still exists in database
- All other data preserved

### 7.2 Verify Cancelled Shift Still Appears in Calendar
**Request:**
```http
GET http://localhost:8080/api/v1/shifts?start_date=2025-10-31&end_date=2025-10-31
Authorization: Bearer <manager_token>
```

**Expected Response (200 OK):**
```json
[
  {
    "employee_shift_id": "EMS251031001",
    "work_date": "2025-10-31",
    "status": "CANCELLED",
    ...
  }
]
```

**✅ Success Criteria:**
- Cancelled shift still appears
- Status shows "CANCELLED"

---

## ❌ Step 8: Test Error Cases

### 8.1 ERROR: Try to Cancel PRESENT Shift (Should Fail)
**Request:**
```http
DELETE http://localhost:8080/api/v1/shifts/EMS251029001
Authorization: Bearer <manager_token>
```

**Expected Response (400 Bad Request):**
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Không thể hủy ca làm việc đã hoàn thành (PRESENT/ABSENT)",
  "instance": "/api/v1/shifts/EMS251029001",
  "code": "CANNOT_CANCEL_COMPLETED"
}
```

**✅ Success Criteria:**
- Status code: 400
- Error code: "CANNOT_CANCEL_COMPLETED"
- Vietnamese error message

### 8.2 ERROR: Invalid Date Range
**Request:**
```http
GET http://localhost:8080/api/v1/shifts?start_date=2025-10-31&end_date=2025-10-29
Authorization: Bearer <manager_token>
```

**Expected Response (400 Bad Request):**
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc",
  "instance": "/api/v1/shifts",
  "code": "INVALID_DATE_RANGE"
}
```

**✅ Success Criteria:**
- Status code: 400
- Error code: "INVALID_DATE_RANGE"

### 8.3 ERROR: Doctor Trying to View Other's Shifts (RBAC)
**Request:**
```http
GET http://localhost:8080/api/v1/shifts?start_date=2025-10-29&end_date=2025-10-31&employee_id=6
Authorization: Bearer <doctor_token>
```

**Expected Response (403 Forbidden):**
```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "Bạn không có quyền truy cập tài nguyên này",
  "instance": "/api/v1/shifts",
  "code": "FORBIDDEN"
}
```

**✅ Success Criteria:**
- Status code: 403
- Error code: "FORBIDDEN"
- Doctor with VIEW_SHIFTS_OWN cannot see other employees' shifts

### 8.4 ERROR: Create Shift on Sunday (Holiday)
**Request:**
```http
POST http://localhost:8080/api/v1/shifts
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "employee_id": 2,
  "work_date": "2025-11-02",
  "work_shift_id": "WKS_MORNING_01",
  "notes": "Testing Sunday restriction"
}
```

**Expected Response (409 Conflict):**
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Không thể tạo ca làm việc vào ngày nghỉ lễ",
  "instance": "/api/v1/shifts",
  "code": "HOLIDAY_CONFLICT"
}
```

**✅ Success Criteria:**
- Status code: 409
- Error code: "HOLIDAY_CONFLICT"
- Cannot create shift on Sunday (2025-11-02)

### 8.5 ERROR: Duplicate Shift Conflict
**Request:**
```http
POST http://localhost:8080/api/v1/shifts
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "employee_id": 2,
  "work_date": "2025-10-30",
  "work_shift_id": "WKS_MORNING_01",
  "notes": "Attempting duplicate"
}
```

First, create a shift at this time, then try to create again.

**Expected Response (409 Conflict):**
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Nhân viên đã có ca làm việc vào thời gian này",
  "instance": "/api/v1/shifts",
  "code": "SLOT_CONFLICT"
}
```

**✅ Success Criteria:**
- Status code: 409
- Error code: "SLOT_CONFLICT"

### 8.6 ERROR: Employee Not Found
**Request:**
```http
POST http://localhost:8080/api/v1/shifts
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "employee_id": 999,
  "work_date": "2025-10-30",
  "work_shift_id": "WKS_MORNING_01"
}
```

**Expected Response (404 Not Found):**
```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Không tìm thấy Employee hoặc WorkShift với ID đã cung cấp",
  "instance": "/api/v1/shifts",
  "code": "RELATED_RESOURCE_NOT_FOUND"
}
```

**✅ Success Criteria:**
- Status code: 404
- Error code: "RELATED_RESOURCE_NOT_FOUND"

### 8.7 ERROR: Work Shift Not Found
**Request:**
```http
POST http://localhost:8080/api/v1/shifts
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "employee_id": 2,
  "work_date": "2025-10-30",
  "work_shift_id": "WKS_INVALID_ID"
}
```

**Expected Response (404 Not Found):**
```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Không tìm thấy Employee hoặc WorkShift với ID đã cung cấp",
  "instance": "/api/v1/shifts",
  "code": "RELATED_RESOURCE_NOT_FOUND"
}
```

### 8.8 ERROR: Shift Not Found (GET/PATCH/DELETE)
**Request:**
```http
GET http://localhost:8080/api/v1/shifts/EMS999999999
Authorization: Bearer <manager_token>
```

**Expected Response (404 Not Found):**
```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Không tìm thấy ca làm việc với ID: EMS999999999",
  "instance": "/api/v1/shifts/EMS999999999",
  "code": "SHIFT_NOT_FOUND"
}
```

### 8.9 ERROR: Try to Set ON_LEAVE Status Manually (Should Fail)
**Request:**
```http
PATCH http://localhost:8080/api/v1/shifts/EMS251030001
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "status": "ON_LEAVE"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Không thể chuyển sang trạng thái ON_LEAVE thủ công",
  "instance": "/api/v1/shifts/EMS251030001",
  "code": "INVALID_STATUS_TRANSITION"
}
```

**✅ Success Criteria:**
- Status code: 400
- Error code: "INVALID_STATUS_TRANSITION"
- ON_LEAVE can only be set by batch job

---

## 📊 Step 9: Get Summary Report

### 9.1 Get Summary (Currently Placeholder)
**Request:**
```http
GET http://localhost:8080/api/v1/shifts/summary?start_date=2025-10-29&end_date=2025-10-31
Authorization: Bearer <manager_token>
```

**Current Response (200 OK):**
```json
"Summary endpoint - To be implemented"
```

**Note:** This endpoint needs full implementation to return aggregated statistics.

---

## ✅ Complete Test Checklist

### Prerequisites
- [ ] Application is running on port 8080
- [ ] Database is accessible and has seed data
- [ ] Postman or equivalent HTTP client ready

### Authentication
- [ ] Login as Manager successful (token saved)
- [ ] Login as Doctor successful (token saved)
- [ ] Tokens work in Authorization header

### Work Shifts Setup
- [ ] Verified 4 work shifts exist in database
- [ ] Know the work_shift_id values to use

### Create Operations
- [ ] Created morning shift for doctor (employee_id: 2)
- [ ] Created afternoon shift for same doctor
- [ ] Created shift for nurse (employee_id: 4)
- [ ] Created overtime shift
- [ ] All responses return 201 Created
- [ ] All employee_shift_id values follow EMSyyMMddSEQ format
- [ ] All fields use snake_case (employee_id, work_shift_id, etc.)

### Read Operations (Calendar View)
- [ ] Manager can view all shifts in date range
- [ ] Manager can filter by employee_id
- [ ] Doctor can view only their own shifts
- [ ] Doctor cannot view other employees' shifts (403)
- [ ] Empty date range returns []
- [ ] Response is array of shift objects

### Read Operations (Detail View)
- [ ] Can get specific shift by employee_shift_id
- [ ] Returns 404 for non-existent shift ID
- [ ] Response format matches calendar view format

### Update Operations
- [ ] Can update status to PRESENT
- [ ] Can update status to ABSENT
- [ ] Can update notes only
- [ ] Can update both status and notes simultaneously
- [ ] Cannot set status to ON_LEAVE manually (400)
- [ ] Status changes are reflected in subsequent GET requests

### Delete Operations (Cancel)
- [ ] Can cancel SCHEDULED shift
- [ ] Cancelled shift has status "CANCELLED"
- [ ] Cancelled shift still appears in calendar
- [ ] Cannot cancel PRESENT shift (400)
- [ ] Cannot cancel ABSENT shift (400)

### Error Handling
- [ ] Invalid date range (start > end) returns 400 with INVALID_DATE_RANGE
- [ ] Missing required fields returns 400
- [ ] Non-existent employee_id returns 404 with RELATED_RESOURCE_NOT_FOUND
- [ ] Non-existent work_shift_id returns 404 with RELATED_RESOURCE_NOT_FOUND
- [ ] Non-existent employee_shift_id returns 404 with SHIFT_NOT_FOUND
- [ ] Holiday date (Sunday) returns 409 with HOLIDAY_CONFLICT
- [ ] Duplicate shift conflict returns 409 with SLOT_CONFLICT
- [ ] All error responses follow RFC 7807 ProblemDetail format
- [ ] All error messages are in Vietnamese

### RBAC Authorization
- [ ] Manager (VIEW_SHIFTS_ALL) can view all employees' shifts
- [ ] Manager (CREATE_SHIFTS) can create shifts
- [ ] Manager (UPDATE_SHIFTS) can update shifts
- [ ] Manager (DELETE_SHIFTS) can cancel shifts
- [ ] Doctor (VIEW_SHIFTS_OWN) can only view own shifts
- [ ] Doctor trying to view others' shifts gets 403 FORBIDDEN
- [ ] Doctor without CREATE_SHIFTS cannot create shifts (403)

### Response Format Validation
- [ ] All JSON keys use snake_case
- [ ] Date format is yyyy-MM-dd
- [ ] Boolean fields are true/false (not "true"/"false")
- [ ] Nested objects (employee, work_shift) have correct structure
- [ ] Status values match enum: SCHEDULED, PRESENT, ABSENT, ON_LEAVE, CANCELLED
- [ ] Source values: MANUAL, BATCH_JOB

### Business Logic
- [ ] employee_shift_id auto-generation works (EMSyyMMddSEQ)
- [ ] Sequence number increments correctly (001, 002, 003...)
- [ ] is_overtime flag works correctly
- [ ] Sunday detection prevents shift creation
- [ ] Duplicate shift detection works (same employee, date, shift)

---

## 🎓 Testing Tips

1. **Test in Order**: Follow the steps sequentially - create data before testing updates
2. **Save IDs**: Keep track of employee_shift_id values returned from POST requests
3. **Test Both Success and Failure**: Don't just test happy paths
4. **Verify RBAC**: Test with both manager and doctor tokens
5. **Check Response Format**: Verify snake_case, proper nesting, correct data types
6. **Database State**: You can query the database directly to verify data persistence
7. **Error Messages**: All error messages should be in Vietnamese
8. **Status Codes**: Pay attention to HTTP status codes (200, 201, 400, 403, 404, 409)

---

## 📝 Notes

- **Date Format**: Always use `yyyy-MM-dd` (e.g., 2025-10-29)
- **Sunday Check**: 2025-11-02 is a Sunday (use for holiday testing)
- **Token Expiry**: Tokens expire after 3600 seconds (1 hour) - login again if expired
- **Cancellation**: DELETE doesn't remove from database, just sets status to CANCELLED
- **ON_LEAVE Status**: Can only be set by batch job (when processing time-off requests)

---

## 🔧 Troubleshooting

**Application won't start:**
- Check if port 8080 is already in use
- Verify PostgreSQL database is running
- Check application.yaml configuration

**401 Unauthorized:**
- Token may have expired - login again
- Check Authorization header format: `Bearer <token>`

**Database errors:**
- Ensure seed data has been loaded
- Check work_shifts table has the 4 required shifts
- Verify employees table has test employees

**Wrong response format:**
- Check that @JsonProperty annotations are in DTOs
- Verify controller uses snake_case in @RequestParam

---

**Good luck with testing! 🚀**
