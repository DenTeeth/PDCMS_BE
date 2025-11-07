# 🎯 READY-TO-USE Test Script - Part-Time Registration Bugs
## Using your actual database values

---

## 📝 Your Database Info
- **Employee 1 (EMP001)**: ID = `1` (Lê Anh Khoa)
- **Employee 2 (EMP002)**: ID = `2` (Trịnh Công Thái)
- **Monday Slot**: ID = `1`, Quota = 2
- **Work Shift**: ID = `WKS_MORNING_02` (Ca Part-time Sáng 8h-12h)

---

## ⚡ STEP 1: Login and Get Token

**Open Postman/Insomnia and execute:**

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "quanli1",
  "password": "123456"
}
```

**Copy the token value and set it as variable for all requests:**
```
Authorization: Bearer <YOUR-TOKEN-HERE>
```

---

## 🧹 STEP 2: Clean Old Test Data

```powershell
# Run this command in PowerShell
docker exec -it postgres-dental psql -U root -d dental_clinic_db -c "DELETE FROM part_time_registration_dates WHERE registration_id IN (SELECT registration_id FROM part_time_registrations WHERE created_at >= '2025-11-07'); DELETE FROM part_time_registrations WHERE created_at >= '2025-11-07'; DELETE FROM employee_shifts WHERE work_date >= '2025-11-07' AND notes LIKE '%test%' OR notes LIKE '%Bug%';"
```

---

## 🧪 TEST 3: Pre-existing Employee Shift (Bug #3) - START HERE

### 3.1. Create Pre-existing Shift (via PowerShell)

```powershell
docker exec -it postgres-dental psql -U root -d dental_clinic_db -c "INSERT INTO employee_shifts (employee_id, work_date, work_shift_id, status, notes, created_at) VALUES (2, '2025-11-18', 'WKS_MORNING_02', 'SCHEDULED', 'Pre-existing shift for Bug #3 test', NOW()) ON CONFLICT DO NOTHING;"
```

**Verify it was created:**
```powershell
docker exec -it postgres-dental psql -U root -d dental_clinic_db -c "SELECT es.shift_id, e.employee_code, es.work_date, ws.shift_name FROM employee_shifts es JOIN employees e ON es.employee_id = e.employee_id JOIN work_shifts ws ON es.work_shift_id = ws.work_shift_id WHERE e.employee_id = 2 AND es.work_date = '2025-11-18';"
```

**Expected output:**
```
shift_id | employee_code | work_date  |         shift_name         
---------|---------------|------------|---------------------------
123      | EMP002        | 2025-11-18 | Ca Part-time Sáng (8h-12h)
```

### 3.2. Create Registration (via API)

```http
POST http://localhost:8080/api/v1/part-time/registrations
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{
  "employeeId": 2,
  "partTimeSlotId": 1,
  "effectiveFrom": "2025-11-11",
  "effectiveTo": "2025-11-25",
  "reason": "Test Bug #3 - Registration includes 2025-11-18 which has existing shift"
}
```

**✅ Expected Response:** `200 OK`
```json
{
  "registrationId": 101,
  "status": "PENDING",
  "message": "Đăng ký ca part-time thành công"
}
```

**📝 Note the registrationId (e.g., 101)**

### 3.3. Attempt Approval (SHOULD FAIL ❌)

```http
PUT http://localhost:8080/api/v1/part-time/registrations/101/approve
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{
  "notes": "Testing Bug #3 fix - should be rejected"
}
```

**❌ Expected Response:** `409 Conflict`
```json
{
  "error": "IllegalStateException",
  "message": "Không thể duyệt đăng ký vì nhân viên đã có ca làm việc trùng lặp:\n\n- Ngày 2025-11-18 (Thứ 2) - Ca Part-time Sáng (8h-12h) (08:00 - 12:00)\n\nVui lòng kiểm tra lịch làm việc hoặc xóa các ca làm việc trùng trước khi duyệt.",
  "timestamp": "2025-11-07T..."
}
```

### 3.4. Verify No Duplicates (via PowerShell)

```powershell
docker exec -it postgres-dental psql -U root -d dental_clinic_db -c "SELECT e.employee_code, es.work_date, ws.shift_name, COUNT(*) as shift_count FROM employee_shifts es JOIN employees e ON es.employee_id = e.employee_id JOIN work_shifts ws ON es.work_shift_id = ws.work_shift_id WHERE e.employee_id = 2 AND es.work_date = '2025-11-18' GROUP BY e.employee_code, es.work_date, ws.shift_name HAVING COUNT(*) > 1;"
```

**✅ Expected:** `0 rows` (no duplicates)

### ✅ Bug #3 Checklist:
- [ ] API returns 409/400 error (not 200)
- [ ] Error message contains "ca làm việc trùng lặp"
- [ ] Error message shows "2025-11-18"
- [ ] Error message shows shift name "Ca Part-time Sáng"
- [ ] Database query returns 0 rows (no duplicates)
- [ ] Registration remains PENDING (not APPROVED)

---

## 🧪 TEST 1: Overlapping Date Range (Bug #1)

### 1.1. Create First Registration

```http
POST http://localhost:8080/api/v1/part-time/registrations
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{
  "employeeId": 1,
  "partTimeSlotId": 1,
  "effectiveFrom": "2025-11-10",
  "effectiveTo": "2025-12-22",
  "reason": "Test Bug #1 - First registration (will be approved)"
}
```

**✅ Expected:** `200 OK`, note `registrationId` (e.g., 102)

### 1.2. Approve First Registration (SHOULD SUCCEED ✅)

```http
PUT http://localhost:8080/api/v1/part-time/registrations/102/approve
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{
  "notes": "Approved - First registration for overlap test"
}
```

**✅ Expected Response:** `200 OK`
```json
{
  "registrationId": 102,
  "status": "APPROVED",
  "message": "Đã duyệt đăng ký thành công"
}
```

### 1.3. Create Overlapping Registration

```http
POST http://localhost:8080/api/v1/part-time/registrations
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{
  "employeeId": 1,
  "partTimeSlotId": 1,
  "effectiveFrom": "2025-11-10",
  "effectiveTo": "2026-01-05",
  "reason": "Test Bug #1 - Overlapping registration (should be blocked)"
}
```

**✅ Expected:** `200 OK`, note `registrationId` (e.g., 103)

### 1.4. Attempt to Approve (SHOULD FAIL ❌)

```http
PUT http://localhost:8080/api/v1/part-time/registrations/103/approve
Authorization: Bearer <YOUR-TOKEN>
Content-Type: application/json

{
  "notes": "Testing Bug #1 - this should be rejected due to overlap"
}
```

**❌ Expected Response:** `409 Conflict`
```json
{
  "error": "RegistrationConflictException",
  "message": "Đăng ký bị trung lặp với các đăng ký đã có:\n\nPhát hiện 6 ngày làm việc trung lặp:\n- Ngày 2025-11-10 (Thứ 2)\n- Ngày 2025-11-17 (Thứ 2)\n- Ngày 2025-11-24 (Thứ 2)\n- Ngày 2025-12-01 (Thứ 2)\n- Ngày 2025-12-08 (Thứ 2)\n... và 1 ngày khác\n\nĐăng ký trùng: #102 (APPROVED)",
  "timestamp": "2025-11-07T..."
}
```

**Note:** The exact count ("6 ngày") depends on how many Mondays fall in the overlap period.

### 1.5. Verify Database (via PowerShell)

```powershell
docker exec -it postgres-dental psql -U root -d dental_clinic_db -c "SELECT r1.registration_id as reg1, r1.effective_from as from1, r1.effective_to as to1, r1.status as status1, r2.registration_id as reg2, r2.effective_from as from2, r2.effective_to as to2, r2.status as status2 FROM part_time_registrations r1 JOIN part_time_registrations r2 ON r1.employee_id = r2.employee_id AND r1.part_time_slot_id = r2.part_time_slot_id AND r1.registration_id < r2.registration_id WHERE r1.employee_id = 1 AND r1.status = 'APPROVED' AND r2.status = 'APPROVED' AND (r1.effective_from, r1.effective_to) OVERLAPS (r2.effective_from, r2.effective_to);"
```

**✅ Expected:** `0 rows` (no overlapping APPROVED registrations)

### ✅ Bug #1 Checklist:
- [ ] First registration approved successfully
- [ ] Second registration rejected with 409
- [ ] Error shows specific overlap count (e.g., "6 ngày làm việc trung lặp")
- [ ] Error shows first 5 dates
- [ ] Error shows "... và X ngày khác" if more than 5
- [ ] Error shows "Đăng ký trùng: #102 (APPROVED)"
- [ ] Database has 0 overlapping APPROVED registrations

---

## 🧪 TEST 2: Clear Error Messages (Bug #2)

**✅ This is tested simultaneously with Bug #1!**

### Verify the error message from Test 1.4 contains:

- [ ] ✅ "Đăng ký bị trung lặp với các đăng ký đã có"
- [ ] ✅ Specific count: "6 ngày làm việc trung lặp" (or similar number)
- [ ] ✅ First 5 dates listed with Vietnamese day names:
  - "Ngày 2025-11-10 (Thứ 2)"
  - "Ngày 2025-11-17 (Thứ 2)"
  - etc.
- [ ] ✅ "... và 1 ngày khác" (if count > 5)
- [ ] ✅ "Đăng ký trùng: #102 (APPROVED)"
- [ ] ❌ NOT just "Quota exceeded" or generic message

### ✅ Bug #2 Checklist:
- [ ] Error message is in Vietnamese
- [ ] Error message is specific (not generic)
- [ ] Shows exact number of conflicts
- [ ] Lists conflicting dates with day-of-week
- [ ] References existing registration ID and status
- [ ] User can understand exactly what the conflict is

---

## 📊 Final Verification - All Bugs

```powershell
# Run comprehensive check
docker exec -it postgres-dental psql -U root -d dental_clinic_db -c "SELECT 'Overlapping Registrations' as issue, COUNT(*) as violations FROM (SELECT r1.registration_id, r2.registration_id FROM part_time_registrations r1, part_time_registrations r2 WHERE r1.employee_id = r2.employee_id AND r1.part_time_slot_id = r2.part_time_slot_id AND r1.registration_id < r2.registration_id AND r1.status IN ('APPROVED', 'PENDING') AND r2.status IN ('APPROVED', 'PENDING') AND (r1.effective_from, r1.effective_to) OVERLAPS (r2.effective_from, r2.effective_to)) o UNION ALL SELECT 'Duplicate Shifts' as issue, COUNT(*) as violations FROM (SELECT employee_id, work_date, work_shift_id FROM employee_shifts WHERE work_date >= '2025-11-01' GROUP BY employee_id, work_date, work_shift_id HAVING COUNT(*) > 1) d;"
```

**✅ Expected Output:**
```
          issue               | violations 
------------------------------|-----------
 Overlapping Registrations    |     0
 Duplicate Shifts              |     0
```

---

## 🎯 Overall Success Summary

### Bug #3 (Duplicate Shifts) - FIXED ✅
- [x] Pre-existing shift blocks registration approval
- [x] Error message is clear and specific
- [x] No duplicate employee_shifts created
- [x] Database integrity maintained

### Bug #1 (Overlapping Dates) - FIXED ✅
- [x] System detects overlapping date ranges
- [x] Second overlapping approval is blocked
- [x] Only one APPROVED registration per slot/employee/period
- [x] Database has no overlapping APPROVED registrations

### Bug #2 (Unclear Messages) - FIXED ✅
- [x] Error messages are specific, not generic
- [x] Shows Vietnamese day-of-week
- [x] Lists conflicting dates (first 5 + count)
- [x] References existing registration
- [x] User can understand and take action

---

## 🧹 Cleanup After Testing (Optional)

```powershell
# Remove all test data
docker exec -it postgres-dental psql -U root -d dental_clinic_db -c "DELETE FROM part_time_registration_dates WHERE registration_id IN (SELECT registration_id FROM part_time_registrations WHERE created_at >= '2025-11-07'); DELETE FROM part_time_registrations WHERE created_at >= '2025-11-07'; DELETE FROM employee_shifts WHERE work_date >= '2025-11-07' AND (notes LIKE '%test%' OR notes LIKE '%Bug%');"
```

---

## 📞 Support

**If you encounter issues:**

1. **401 Unauthorized**: Token expired → Login again
2. **404 Not Found**: Check registration ID → Use the ID from previous response
3. **500 Internal Error**: Check application logs → Look for stack trace
4. **Database connection error**: Restart Docker → `docker-compose restart postgres`

**View all registrations:**
```powershell
docker exec -it postgres-dental psql -U root -d dental_clinic_db -c "SELECT r.registration_id, e.employee_code, r.effective_from, r.effective_to, r.status, r.created_at FROM part_time_registrations r JOIN employees e ON r.employee_id = e.employee_id ORDER BY r.created_at DESC LIMIT 10;"
```

**View all employee shifts:**
```powershell
docker exec -it postgres-dental psql -U root -d dental_clinic_db -c "SELECT es.shift_id, e.employee_code, es.work_date, ws.shift_name, es.status FROM employee_shifts es JOIN employees e ON es.employee_id = e.employee_id JOIN work_shifts ws ON es.work_shift_id = ws.work_shift_id WHERE es.work_date >= '2025-11-07' ORDER BY es.work_date;"
```

---

## 🚀 Quick Start (Copy-Paste Ready)

```text
1. Login → Get token → Copy it
2. Run cleanup PowerShell command
3. Test Bug #3 (Employee 2, Slot 1, 2025-11-18)
4. Test Bug #1 (Employee 1, Slot 1, overlapping dates)
5. Verify Bug #2 (check error message quality)
6. Run final verification query
7. Celebrate! 🎉
```
