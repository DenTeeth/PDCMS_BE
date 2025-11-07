# 🎯 Quick Test Execution - Part-Time Registration Bugs

## ✅ Prerequisites Complete
- ✅ Database: postgres-dental (running on port 5432)
- ✅ Login: quanli1 / 123456
- ✅ Application: Should be running on port 8080

---

## 📋 Test Execution Order

### **STEP 1: Get JWT Token**
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "quanli1",
  "password": "123456"
}
```
**Copy the token from response and use it in all subsequent requests!**

---

### **STEP 2: Connect to Database and Setup**
```powershell
# Connect to PostgreSQL
docker exec -it postgres-dental psql -U root -d dental_clinic_db
```

**Run these SQL queries to setup test data:**

```sql
-- Get employee IDs
SELECT employee_id, employee_code, first_name, last_name
FROM employees
WHERE is_active = true
ORDER BY employee_id
LIMIT 5;

-- Get slot ID for testing
SELECT slot_id, work_shift_id, day_of_week, quota
FROM part_time_slots
WHERE day_of_week = 'MONDAY' AND is_active = true
LIMIT 1;

-- Get shift ID for MORNING shift
SELECT work_shift_id, shift_name, start_time, end_time
FROM work_shifts
WHERE category = 'NORMAL' AND start_time = '08:00:00'
LIMIT 1;

-- Clean up old test data
DELETE FROM part_time_registration_dates WHERE registration_id IN (
    SELECT registration_id FROM part_time_registrations 
    WHERE created_at >= '2025-11-07'
);
DELETE FROM part_time_registrations WHERE created_at >= '2025-11-07';
DELETE FROM employee_shifts WHERE work_date >= '2025-11-07' AND notes LIKE '%test%';
```

**📝 Write down these IDs (example values):**
- Employee 1 (EMP001): `employee_id = 1`
- Employee 2 (EMP002): `employee_id = 2`
- Monday Slot: `slot_id = 5`
- Morning Shift: `work_shift_id = 1`

---

### **TEST 3: Pre-existing Employee Shift (Bug #3)** ⭐ START HERE

#### 3.1. Create pre-existing shift in database
```sql
-- Insert existing shift on 2025-11-17
INSERT INTO employee_shifts (employee_id, work_date, work_shift_id, status, notes, created_at)
VALUES (2, '2025-11-17', 1, 'SCHEDULED', 'Pre-existing shift for Bug #3 test', NOW())
ON CONFLICT DO NOTHING;

-- Verify it exists
SELECT es.shift_id, e.employee_code, es.work_date, ws.shift_name
FROM employee_shifts es
JOIN employees e ON es.employee_id = e.employee_id
JOIN work_shifts ws ON es.work_shift_id = ws.work_shift_id
WHERE e.employee_id = 2 AND es.work_date = '2025-11-17';
```

#### 3.2. Create registration via API
```http
POST http://localhost:8080/api/v1/part-time/registrations
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{
  "employeeId": 2,
  "partTimeSlotId": 5,
  "effectiveFrom": "2025-11-10",
  "effectiveTo": "2025-11-24",
  "reason": "Test Bug #3"
}
```
**Expected: 200 OK, note the registrationId (e.g., 101)**

#### 3.3. Try to approve (SHOULD FAIL ❌)
```http
PUT http://localhost:8080/api/v1/part-time/registrations/101/approve
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{"notes": "Test Bug #3"}
```

**✅ Expected: 409 Conflict with message:**
```
Không thể duyệt đăng ký vì nhân viên đã có ca làm việc trùng lặp:

- Ngày 2025-11-17 (Thứ 2) - Ca MORNING (08:00 - 17:00)

Vui lòng kiểm tra lịch làm việc...
```

#### 3.4. Verify no duplicates
```sql
SELECT e.employee_code, es.work_date, COUNT(*) as shift_count
FROM employee_shifts es
JOIN employees e ON es.employee_id = e.employee_id
WHERE e.employee_id = 2 AND es.work_date = '2025-11-17'
GROUP BY e.employee_code, es.work_date
HAVING COUNT(*) > 1;
-- Expected: 0 rows (no duplicates)
```

---

### **TEST 1: Overlapping Date Range (Bug #1)**

#### 1.1. Create first registration
```http
POST http://localhost:8080/api/v1/part-time/registrations
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{
  "employeeId": 1,
  "partTimeSlotId": 5,
  "effectiveFrom": "2025-11-09",
  "effectiveTo": "2025-12-21",
  "reason": "First registration"
}
```
**Expected: 200 OK, note registrationId (e.g., 102)**

#### 1.2. Approve first registration (SHOULD SUCCEED ✅)
```http
PUT http://localhost:8080/api/v1/part-time/registrations/102/approve
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{"notes": "Approve first"}
```
**Expected: 200 OK, status = APPROVED**

#### 1.3. Create overlapping registration
```http
POST http://localhost:8080/api/v1/part-time/registrations
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{
  "employeeId": 1,
  "partTimeSlotId": 5,
  "effectiveFrom": "2025-11-07",
  "effectiveTo": "2026-01-07",
  "reason": "Overlapping registration"
}
```
**Expected: 200 OK, note registrationId (e.g., 103)**

#### 1.4. Try to approve overlapping (SHOULD FAIL ❌)
```http
PUT http://localhost:8080/api/v1/part-time/registrations/103/approve
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{"notes": "Test overlap"}
```

**✅ Expected: 409 Conflict with message:**
```
Đăng ký bị trung lặp với các đăng ký đã có:

Phát hiện 44 ngày làm việc trung lặp:
- Ngày 2025-11-10 (Thứ 2)
- Ngày 2025-11-17 (Thứ 2)
- Ngày 2025-11-24 (Thứ 2)
- Ngày 2025-12-01 (Thứ 2)
- Ngày 2025-12-08 (Thứ 2)
... và 39 ngày khác

Đăng ký trùng: #102 (APPROVED)
```

#### 1.5. Verify database
```sql
SELECT r1.registration_id, r1.effective_from, r1.effective_to, r1.status,
       r2.registration_id, r2.effective_from, r2.effective_to, r2.status
FROM part_time_registrations r1
JOIN part_time_registrations r2 
  ON r1.employee_id = r2.employee_id 
  AND r1.registration_id < r2.registration_id
WHERE r1.employee_id = 1
  AND r1.status = 'APPROVED' AND r2.status = 'APPROVED'
  AND (r1.effective_from, r1.effective_to) OVERLAPS (r2.effective_from, r2.effective_to);
-- Expected: 0 rows (no overlapping APPROVED)
```

---

### **TEST 2: Clear Error Messages (Bug #2)** ✅ Already verified in Test 1!

**Check the error message from Test 1.4 includes:**
- ✅ "Đăng ký bị trung lặp với các đăng ký đã có"
- ✅ "44 ngày làm việc trung lặp" (specific count)
- ✅ Shows 5 dates with day-of-week
- ✅ "... và 39 ngày khác" (remaining count)
- ✅ "Đăng ký trùng: #102 (APPROVED)" (reference)

---

## 📊 Final Verification

```sql
-- Run this to verify ALL bugs are fixed
SELECT 'Overlapping Registrations' as issue, COUNT(*) as violations
FROM (
    SELECT r1.registration_id, r2.registration_id
    FROM part_time_registrations r1, part_time_registrations r2
    WHERE r1.employee_id = r2.employee_id 
      AND r1.part_time_slot_id = r2.part_time_slot_id
      AND r1.registration_id < r2.registration_id
      AND r1.status IN ('APPROVED', 'PENDING')
      AND r2.status IN ('APPROVED', 'PENDING')
      AND (r1.effective_from, r1.effective_to) OVERLAPS (r2.effective_from, r2.effective_to)
) o
UNION ALL
SELECT 'Duplicate Shifts' as issue, COUNT(*) as violations
FROM (
    SELECT employee_id, work_date, work_shift_id
    FROM employee_shifts
    WHERE work_date >= '2025-11-01'
    GROUP BY employee_id, work_date, work_shift_id
    HAVING COUNT(*) > 1
) d;

-- Expected: Both violations = 0
```

---

## 🎯 Success Checklist

### Bug #3 (Duplicate Shifts)
- [ ] Pre-existing shift blocks approval
- [ ] Error shows "ca làm việc trùng lặp"
- [ ] Error shows date and shift name
- [ ] Database has only 1 shift (not 2)

### Bug #1 (Overlapping Dates)
- [ ] First registration approved
- [ ] Second overlapping rejected
- [ ] Error shows specific date count
- [ ] Database has no overlapping APPROVED

### Bug #2 (Clear Messages)
- [ ] Error message in Vietnamese
- [ ] Shows first 5 dates
- [ ] Shows remaining count
- [ ] References existing registration #ID
- [ ] NOT generic "Quota exceeded"

---

## 🚨 Troubleshooting

**401 Unauthorized**: Token expired, login again
**404 Not Found**: Check endpoint URL and registration ID
**500 Server Error**: Check application logs
**Connection refused**: Ensure Spring Boot app is running

**Exit database**: Type `\q` and press Enter
**Reconnect**: `docker exec -it postgres-dental psql -U root -d dental_clinic_db`
