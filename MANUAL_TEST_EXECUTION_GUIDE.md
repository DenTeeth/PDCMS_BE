# Manual Testing Guide - Part-Time Registration Bugs
**Login as**: quanli1 / 123456

## Prerequisites

### 1. Start Docker Database
```powershell
# Navigate to project directory
cd c:\Users\ADMiN_KN\Desktop\DenTeeth\PDCMS_BE

# Start PostgreSQL container
docker-compose up -d postgres

# Wait 5 seconds for database to be ready
Start-Sleep -Seconds 5

# Verify container is running
docker ps | Select-String "postgres-dental"
```

### 2. Connect to Database and Setup Test Data
```powershell
# Connect to database
docker exec -it postgres-dental psql -U root -d dental_clinic_db

# Then paste and run the setup queries from test_part_time_bugs.sql (STEP 1 & 2)
```

### 3. Login to Get JWT Token
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "quanli1",
  "password": "123456"
}
```

**Response (copy the token):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "account": {...}
}
```

**Set this for all subsequent requests:**
```
Authorization: Bearer <your-token-here>
```

---

## Test Order (Logical Sequence)

### Test Scenario 3 FIRST → Test Scenario 1 → Test Scenario 2

**Why this order?**
- Scenario 3 (Bug #3) uses a different employee (EMP002), so it won't interfere with Scenarios 1 & 2
- Scenarios 1 & 2 (Bug #1 and #2) use the same employee (EMP001) and test the same fix
- This order prevents data conflicts

---

## 🧪 TEST SCENARIO 3: Pre-existing Employee Shift Prevention (Bug #3)
**Employee**: EMP002  
**Objective**: Prevent approving registration when employee already has shifts created

### Step 3.1: Setup - Create Pre-existing Employee Shift (via SQL)

```sql
-- Run in docker exec -it postgres-dental psql -U root -d dental_clinic_db

-- Get employee_id for EMP002
SELECT employee_id, employee_code, first_name, last_name
FROM employees
WHERE employee_code = 'EMP002'
LIMIT 1;
-- Note the employee_id (let's assume it's 2)

-- Get work_shift_id for MORNING shift
SELECT work_shift_id, shift_name, start_time, end_time
FROM work_shifts
WHERE category = 'NORMAL' AND start_time = '08:00:00'
LIMIT 1;
-- Note the work_shift_id (let's assume it's 1)

-- Create pre-existing shift on 2025-11-17 (Monday)
INSERT INTO employee_shifts (
    employee_id,
    work_date,
    work_shift_id,
    status,
    notes,
    created_at
)
VALUES (
    2,  -- Replace with actual employee_id for EMP002
    '2025-11-17',
    1,  -- Replace with actual work_shift_id for MORNING
    'SCHEDULED',
    'Pre-existing shift for Bug #3 testing',
    NOW()
)
ON CONFLICT (employee_id, work_date, work_shift_id) DO NOTHING;

-- Verify the shift exists
SELECT 
    es.shift_id,
    e.employee_code,
    es.work_date,
    ws.shift_name,
    es.status
FROM employee_shifts es
JOIN employees e ON es.employee_id = e.employee_id
JOIN work_shifts ws ON es.work_shift_id = ws.work_shift_id
WHERE e.employee_code = 'EMP002'
  AND es.work_date = '2025-11-17';
```

### Step 3.2: Get Test Data IDs

```sql
-- Get slot_id for MONDAY slot
SELECT slot_id, work_shift_id, day_of_week, quota
FROM part_time_slots
WHERE day_of_week = 'MONDAY' AND is_active = true
LIMIT 1;
-- Note the slot_id (let's assume it's 5)
```

### Step 3.3: Create Part-Time Registration (via API)

```http
POST http://localhost:8080/api/v1/part-time/registrations
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "employeeId": 2,
  "partTimeSlotId": 5,
  "effectiveFrom": "2025-11-10",
  "effectiveTo": "2025-11-24",
  "reason": "Test Bug #3 - Registration overlaps with existing shift on 2025-11-17"
}
```

**Expected Response**: 200 OK (registration created with PENDING status)
```json
{
  "registrationId": 101,
  "status": "PENDING",
  "message": "Đăng ký ca part-time thành công"
}
```

**Note the registrationId (e.g., 101)**

### Step 3.4: Attempt to Approve Registration (Should FAIL)

```http
PUT http://localhost:8080/api/v1/part-time/registrations/101/approve
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "notes": "Testing Bug #3 fix"
}
```

**Expected Response**: ❌ **409 Conflict** or **400 Bad Request**
```json
{
  "error": "IllegalStateException",
  "message": "Không thể duyệt đăng ký vì nhân viên đã có ca làm việc trùng lặp:\n\n- Ngày 2025-11-17 (Thứ 2) - Ca MORNING (08:00 - 17:00)\n\nVui lòng kiểm tra lịch làm việc hoặc xóa các ca làm việc trùng trước khi duyệt.",
  "timestamp": "2025-11-07T..."
}
```

### Step 3.5: Verify Database State (No Duplicate Shifts)

```sql
-- Check that only ONE shift exists for EMP002 on 2025-11-17
SELECT 
    e.employee_code,
    es.work_date,
    ws.shift_name,
    COUNT(*) as shift_count
FROM employee_shifts es
JOIN employees e ON es.employee_id = e.employee_id
JOIN work_shifts ws ON es.work_shift_id = ws.work_shift_id
WHERE e.employee_code = 'EMP002'
  AND es.work_date = '2025-11-17'
GROUP BY e.employee_code, es.work_date, ws.shift_name;

-- Expected: shift_count = 1 (not 2)
```

### ✅ Test 3 Pass Criteria:
- [ ] API returns 409/400 error (not 200)
- [ ] Error message mentions "ca làm việc trùng lặp"
- [ ] Error message shows date "2025-11-17"
- [ ] Error message shows shift name "MORNING"
- [ ] Database query shows only 1 shift (not 2)
- [ ] Registration status remains PENDING (not APPROVED)

---

## 🧪 TEST SCENARIO 1: Overlapping Date Range Validation (Bug #1)
**Employee**: EMP001  
**Objective**: Prevent approving two registrations with overlapping date ranges

### Step 1.1: Create First Registration

```http
POST http://localhost:8080/api/v1/part-time/registrations
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "employeeId": 1,
  "partTimeSlotId": 5,
  "effectiveFrom": "2025-11-09",
  "effectiveTo": "2025-12-21",
  "reason": "Test Bug #1 - First registration (will be approved)"
}
```

**Expected Response**: 200 OK
```json
{
  "registrationId": 102,
  "status": "PENDING"
}
```

**Note the registrationId (e.g., 102)**

### Step 1.2: Approve First Registration (Should SUCCESS)

```http
PUT http://localhost:8080/api/v1/part-time/registrations/102/approve
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "notes": "Approved first registration for Bug #1 test"
}
```

**Expected Response**: ✅ **200 OK**
```json
{
  "registrationId": 102,
  "status": "APPROVED",
  "message": "Đã duyệt đăng ký thành công"
}
```

### Step 1.3: Create Second Overlapping Registration

```http
POST http://localhost:8080/api/v1/part-time/registrations
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "employeeId": 1,
  "partTimeSlotId": 5,
  "effectiveFrom": "2025-11-07",
  "effectiveTo": "2026-01-07",
  "reason": "Test Bug #1 - Second registration (overlaps with first)"
}
```

**Expected Response**: 200 OK (creation allowed, but approval should fail)
```json
{
  "registrationId": 103,
  "status": "PENDING"
}
```

**Note the registrationId (e.g., 103)**

### Step 1.4: Attempt to Approve Second Registration (Should FAIL)

```http
PUT http://localhost:8080/api/v1/part-time/registrations/103/approve
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "notes": "Testing Bug #1 fix - this should be rejected"
}
```

**Expected Response**: ❌ **409 Conflict**
```json
{
  "error": "RegistrationConflictException",
  "message": "Đăng ký bị trung lặp với các đăng ký đã có:\n\nPhát hiện 44 ngày làm việc trung lặp:\n- Ngày 2025-11-10 (Thứ 2)\n- Ngày 2025-11-17 (Thứ 2)\n- Ngày 2025-11-24 (Thứ 2)\n- Ngày 2025-12-01 (Thứ 2)\n- Ngày 2025-12-08 (Thứ 2)\n... và 39 ngày khác\n\nĐăng ký trùng: #102 (APPROVED)",
  "timestamp": "2025-11-07T..."
}
```

### Step 1.5: Verify Database State

```sql
-- Check for overlapping APPROVED/PENDING registrations
SELECT 
    r1.registration_id as reg1_id,
    r2.registration_id as reg2_id,
    r1.effective_from as r1_from,
    r1.effective_to as r1_to,
    r2.effective_from as r2_from,
    r2.effective_to as r2_to,
    r1.status as r1_status,
    r2.status as r2_status
FROM part_time_registrations r1
JOIN part_time_registrations r2 
    ON r1.employee_id = r2.employee_id 
    AND r1.part_time_slot_id = r2.part_time_slot_id
    AND r1.registration_id < r2.registration_id
WHERE r1.employee_id = 1  -- EMP001
  AND (r1.status IN ('APPROVED', 'PENDING') AND r2.status IN ('APPROVED', 'PENDING'))
  AND (r1.effective_from, r1.effective_to) OVERLAPS (r2.effective_from, r2.effective_to);

-- Expected: 0 rows (no overlapping APPROVED registrations)
```

### ✅ Test 1 Pass Criteria:
- [ ] First registration approved successfully (status = APPROVED)
- [ ] Second registration rejected with 409 error
- [ ] Error message shows "44 ngày làm việc trung lặp" (or similar count)
- [ ] Error message shows first 5 dates
- [ ] Error message shows "và 39 ngày khác" (or similar)
- [ ] Error message shows "Đăng ký trùng: #102 (APPROVED)"
- [ ] Database query shows 0 overlapping APPROVED registrations

---

## 🧪 TEST SCENARIO 2: Clear Error Messages (Bug #2)
**This is tested simultaneously with Scenario 1!**

### Verification Checklist for Bug #2:

In the error response from Step 1.4, verify the message includes:

✅ **Clear conflict type**: "Đăng ký bị trung lặp với các đăng ký đã có"

✅ **Specific count**: "44 ngày làm việc trung lặp" (actual number may vary)

✅ **Specific dates**: Shows at least 5 dates with day-of-week:
- "Ngày 2025-11-10 (Thứ 2)"
- "Ngày 2025-11-17 (Thứ 2)"
- etc.

✅ **Remaining count**: "... và 39 ngày khác" (if more than 5)

✅ **Existing registration reference**: "Đăng ký trùng: #102 (APPROVED)"

✅ **NOT the generic error**: Should NOT say just "Quota exceeded" or vague message

---

## 📊 Final Verification (After All Tests)

### Run Summary Verification Query

```sql
-- Connect to database
docker exec -it postgres-dental psql -U root -d dental_clinic_db

-- Run comprehensive verification
SELECT 'Overlapping Approved Registrations' as check_name, COUNT(*) as violations
FROM (
    SELECT r1.registration_id, r2.registration_id
    FROM part_time_registrations r1
    JOIN part_time_registrations r2 
        ON r1.employee_id = r2.employee_id 
        AND r1.part_time_slot_id = r2.part_time_slot_id
        AND r1.registration_id < r2.registration_id
    WHERE (r1.status IN ('APPROVED', 'PENDING') AND r2.status IN ('APPROVED', 'PENDING'))
      AND (r1.effective_from, r1.effective_to) OVERLAPS (r2.effective_from, r2.effective_to)
) overlaps
UNION ALL
SELECT 'Duplicate Employee Shifts' as check_name, COUNT(*) as violations
FROM (
    SELECT employee_id, work_date, work_shift_id, COUNT(*) as cnt
    FROM employee_shifts
    WHERE work_date >= '2025-11-01'
    GROUP BY employee_id, work_date, work_shift_id
    HAVING COUNT(*) > 1
) duplicates;
```

**Expected Result:**
```
check_name                          | violations
------------------------------------|-----------
Overlapping Approved Registrations  |     0
Duplicate Employee Shifts           |     0
```

---

## 🎯 Overall Success Criteria

### Bug #1 (Overlapping Dates) - FIXED ✅
- [x] System detects overlapping date ranges
- [x] Second overlapping approval is rejected
- [x] Error message is specific and clear
- [x] No overlapping APPROVED registrations in database

### Bug #2 (Unclear Messages) - FIXED ✅
- [x] Error message shows specific conflict type
- [x] Error message lists conflicting dates (first 5)
- [x] Error message shows remaining count ("và X ngày khác")
- [x] Error message references existing registration (#ID + status)
- [x] NO generic "Quota exceeded" message

### Bug #3 (Duplicate Shifts) - FIXED ✅
- [x] System detects pre-existing employee shifts
- [x] Approval is rejected when shift exists
- [x] Error message mentions "ca làm việc trùng lặp"
- [x] Error message shows specific date and shift name
- [x] No duplicate shifts created in database

---

## 🧹 Cleanup (Optional)

```sql
-- Delete all test registrations
DELETE FROM part_time_registration_dates 
WHERE registration_id IN (
    SELECT registration_id 
    FROM part_time_registrations 
    WHERE employee_id IN (1, 2)  -- EMP001, EMP002
    AND created_at >= '2025-11-07'
);

DELETE FROM part_time_registrations 
WHERE employee_id IN (1, 2)
AND created_at >= '2025-11-07';

-- Delete test employee shifts
DELETE FROM employee_shifts 
WHERE employee_id IN (1, 2)
AND work_date >= '2025-11-07'
AND notes LIKE '%Bug%testing%';
```

---

## 📝 Notes

1. **Token Expiration**: If you get 401 errors, login again to get a fresh JWT token
2. **Employee IDs**: Adjust `employeeId` values based on your actual database (use queries from test_part_time_bugs.sql)
3. **Slot IDs**: Adjust `partTimeSlotId` based on your active slots
4. **Date Overlap Count**: The "44 ngày" count may vary based on actual working days calculation
5. **Vietnamese Messages**: All error messages should be in Vietnamese as specified in the fix

---

## 🚀 Quick Start Commands

```powershell
# 1. Start Docker database
cd c:\Users\ADMiN_KN\Desktop\DenTeeth\PDCMS_BE
docker-compose up -d postgres

# 2. Wait and verify
Start-Sleep -Seconds 5
docker ps

# 3. Connect to database
docker exec -it postgres-dental psql -U root -d dental_clinic_db

# 4. Run setup queries from test_part_time_bugs.sql (STEP 1 & 2)

# 5. Start Spring Boot application (if not already running)
# Then use Postman/curl to execute API calls in order:
#    - Test Scenario 3 (Bug #3)
#    - Test Scenario 1 (Bug #1) 
#    - Test Scenario 2 (Bug #2 - verified via Scenario 1 error messages)
```
