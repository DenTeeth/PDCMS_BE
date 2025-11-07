# Part-Time Registration Bug Fixes - Testing Guide

## 🎯 Test Objectives

Verify that all 3 critical bugs in Part-Time Flex Registration approval are fixed:
1. **Bug #1:** Prevent overlapping date range approvals
2. **Bug #2:** Show clear error messages for conflicts
3. **Bug #3:** Prevent duplicate employee shifts

---

## 📋 Prerequisites

- Application is running successfully
- You have admin/manager credentials
- Database has:
  - At least one active part-time slot
  - At least one employee
  - Test work shifts configured

---

## 🧪 Test Scenarios

### Test Case 1: Overlapping Date Range Validation (Bug #1)

**Objective:** Verify system rejects overlapping registrations for same employee + same slot

**Steps:**

1. **Setup:** Create and approve first registration
   ```http
   POST /api/v1/part-time-registrations
   {
     "employeeId": 10,
     "slotId": 1,
     "effectiveFrom": "2025-11-09",
     "effectiveTo": "2025-12-21",
     "reason": "Test registration A"
   }
   ```
   - Note the `registrationId` (e.g., 100)

2. **Approve** the first registration:
   ```http
   POST /api/v1/part-time-registrations/{registrationId}/approve
   {
     "approvedBy": 1,  // Your manager employee ID
     "reason": "Approved for testing"
   }
   ```
   - **Expected:** Status 200, registration APPROVED ✅

3. **Create overlapping registration** (same employee, same slot):
   ```http
   POST /api/v1/part-time-registrations
   {
     "employeeId": 10,  // Same employee
     "slotId": 1,       // Same slot
     "effectiveFrom": "2025-11-07",  // Overlaps with 11/09
     "effectiveTo": "2026-01-07",    // Overlaps with 12/21
     "reason": "Test registration B - should be blocked"
   }
   ```
   - Note the second `registrationId` (e.g., 101)

4. **Try to approve** the overlapping registration:
   ```http
   POST /api/v1/part-time-registrations/{registrationId}/approve
   {
     "approvedBy": 1,
     "reason": "Attempting to approve overlap"
   }
   ```

**Expected Result:** ❌ **REJECTION**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Đăng ký bị trung lặp với đăng ký đã tồn tại (APPROVED/PENDING) cho cùng slot.\n\nPhát hiện 44 ngày làm việc trung lặp:\n- Ngày 2025-11-10 (Thứ 2)\n- Ngày 2025-11-17 (Thứ 2)\n- Ngày 2025-11-24 (Thứ 2)\n- Ngày 2025-12-01 (Thứ 2)\n- Ngày 2025-12-08 (Thứ 2)\n... và 39 ngày khác\n\nĐăng ký trùng: #100 (APPROVED)",
  "path": "/api/v1/part-time-registrations/101/approve"
}
```

**Pass Criteria:**
- ✅ Request rejected with status 409 (Conflict)
- ✅ Error message clearly explains the overlap
- ✅ Shows conflicting dates (first 5 + count)
- ✅ Shows existing registration ID and status

---

### Test Case 2: Clear Error Messages (Bug #2)

**Objective:** Verify error messages are clear and specific

**Steps:**

1. **Create identical registration** (exact same dates):
   ```http
   POST /api/v1/part-time-registrations
   {
     "employeeId": 10,
     "slotId": 1,
     "effectiveFrom": "2025-11-09",  // Exact same as registration A
     "effectiveTo": "2025-12-21",    // Exact same as registration A
     "reason": "Test duplicate registration"
   }
   ```

2. **Try to approve:**
   ```http
   POST /api/v1/part-time-registrations/{registrationId}/approve
   {
     "approvedBy": 1,
     "reason": "Testing duplicate"
   }
   ```

**Expected Result:** ❌ **CLEAR ERROR MESSAGE**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Đăng ký bị trung lặp với đăng ký đã tồn tại (APPROVED/PENDING) cho cùng slot.\n\nPhát hiện 44 ngày làm việc trung lặp:\n- Ngày 2025-11-10 (Thứ 2)\n- Ngày 2025-11-17 (Thứ 2)\n- Ngày 2025-11-24 (Thứ 2)\n- Ngày 2025-12-01 (Thứ 2)\n- Ngày 2025-12-08 (Thứ 2)\n... và 39 ngày khác\n\nĐăng ký trung: #100 (APPROVED)"
}
```

**Pass Criteria:**
- ✅ Error message is in Vietnamese
- ✅ Clearly states "trung lặp" (duplicate/conflict)
- ✅ Shows exact conflicting dates
- ✅ References existing registration ID
- ✅ Shows status (APPROVED/PENDING)
- ✅ NOT just "Quota exceeded" - specific conflict reason

---

### Test Case 3: Pre-existing Employee Shift Validation (Bug #3)

**Objective:** Verify system prevents creating duplicate employee shifts

**Setup Steps:**

1. **Manually create an employee shift** (simulate existing shift):
   ```http
   POST /api/v1/employee-shifts
   {
     "employeeId": 15,
     "workDate": "2025-11-15",
     "workShiftId": "MORNING_8_12",  // 08:00-12:00
     "status": "SCHEDULED",
     "notes": "Pre-existing shift for testing"
   }
   ```

2. **Verify shift exists:**
   ```http
   GET /api/v1/employee-shifts?employeeId=15&workDate=2025-11-15
   ```
   - Should return the shift you just created

**Test Steps:**

3. **Create part-time registration** that includes the date with existing shift:
   ```http
   POST /api/v1/part-time-registrations
   {
     "employeeId": 15,  // Same employee with existing shift
     "slotId": 2,       // Slot that uses MORNING_8_12 shift
     "effectiveFrom": "2025-11-10",
     "effectiveTo": "2025-11-20",  // Includes 11/15 (existing shift date)
     "reason": "Testing pre-existing shift conflict"
   }
   ```

4. **Try to approve the registration:**
   ```http
   POST /api/v1/part-time-registrations/{registrationId}/approve
   {
     "approvedBy": 1,
     "reason": "Testing duplicate shift prevention"
   }
   ```

**Expected Result:** ❌ **REJECTION WITH CLEAR ERROR**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Không thể duyệt đăng ký vì nhân viên đã có ca làm việc trùng lặp.\n\nPhát hiện 1 ngày có ca làm việc sẵn:\n- 2025-11-15 (Thứ 6) - Ca MORNING_8_12\n\nVui lòng kiểm tra lịch làm việc của nhân viên hoặc xóa các ca làm việc trùng trước khi duyệt."
}
```

**Pass Criteria:**
- ✅ Request rejected with status 409
- ✅ Error clearly states "ca làm việc trùng lặp" (duplicate shifts)
- ✅ Lists conflicting dates with shift names
- ✅ Provides remediation steps
- ✅ **No duplicate shifts created in database**

**Verification:**
```http
GET /api/v1/employee-shifts?employeeId=15&workDate=2025-11-15
```
- Should return **only 1 shift** (the original one)
- Should **NOT** have duplicate

---

## 🔍 Database Verification Queries

After running tests, verify in database:

### Check for duplicate employee shifts:
```sql
SELECT 
    employee_id, 
    work_date, 
    work_shift_id, 
    COUNT(*) as count
FROM employee_shifts
WHERE employee_id = 15 
  AND work_date = '2025-11-15'
GROUP BY employee_id, work_date, work_shift_id
HAVING COUNT(*) > 1;
```
**Expected:** 0 rows (no duplicates)

### Check registration status:
```sql
SELECT 
    registration_id,
    employee_id,
    part_time_slot_id,
    effective_from,
    effective_to,
    status,
    processed_at
FROM part_time_registrations
WHERE employee_id IN (10, 15)
ORDER BY created_at DESC;
```

### Check overlapping approvals:
```sql
SELECT 
    r1.registration_id as reg1,
    r2.registration_id as reg2,
    r1.employee_id,
    r1.part_time_slot_id,
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
    AND r1.registration_id != r2.registration_id
WHERE (r1.status IN ('APPROVED', 'PENDING') 
   AND r2.status IN ('APPROVED', 'PENDING'))
  AND (r1.effective_from, r1.effective_to) OVERLAPS (r2.effective_from, r2.effective_to);
```
**Expected:** 0 rows (no overlapping APPROVED/PENDING registrations)

---

## ✅ Success Criteria Summary

All tests should demonstrate:

1. **Bug #1 Fixed:**
   - ✅ Cannot approve overlapping registrations
   - ✅ Validation happens before quota checking
   - ✅ Works for all overlap scenarios (partial, full, nested)

2. **Bug #2 Fixed:**
   - ✅ Error messages are clear and specific
   - ✅ Shows Vietnamese text explaining conflict
   - ✅ Lists conflicting dates (up to 5, then count)
   - ✅ References existing registration ID and status

3. **Bug #3 Fixed:**
   - ✅ Cannot approve if employee already has shift on those dates
   - ✅ Validation checks `employee_shifts` table
   - ✅ Error message explains the issue and solution
   - ✅ No duplicate shifts created in database

---

## 🐛 If Tests Fail

If any test fails, check:

1. **Application logs** - Look for validation errors
2. **Database state** - Use verification queries above
3. **Request payload** - Ensure correct employee/slot IDs
4. **Slot configuration** - Ensure slot is active and has quota

**Key files to check:**
- `PartTimeRegistrationApprovalService.java` - Validation logic
- `EmployeeShiftService.java` - Shift creation logic
- `RegistrationConflictException.java` - Error messages

---

## 📝 Test Checklist

- [ ] Test Case 1: Overlapping date ranges rejected
- [ ] Test Case 2: Clear error messages displayed
- [ ] Test Case 3: Pre-existing shifts prevent approval
- [ ] Database verification: No duplicate shifts
- [ ] Database verification: No overlapping approvals
- [ ] Error messages in Vietnamese
- [ ] Error messages show conflicting dates
- [ ] Error messages reference existing registrations

---

## 🎉 Expected Outcome

All three bugs should be **FIXED** and **VERIFIED**:
- ✅ Overlapping registrations **blocked**
- ✅ Error messages **clear and specific**
- ✅ Duplicate employee shifts **prevented**

The Part-Time Flex Registration system is now **robust and safe**! 🚀
