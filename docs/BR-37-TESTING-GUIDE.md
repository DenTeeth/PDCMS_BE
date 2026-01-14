# BR-37 Testing Guide - Weekly Working Hours Limit

## 📋 Test Scenarios for Manual Verification

### Prerequisite
- Start the application
- Login as user with `MANAGE_FIXED_REGISTRATIONS` permission
- Have test employee data ready

---

## Test Case 1: Normal Shift Creation (SHOULD PASS)

### Setup:
- Employee ID: 1 (Dr. Minh)
- Current week: Jan 6-12, 2026
- Existing shifts: 35 hours total

### Steps:
1. POST `/api/employee-shifts/manual`
```json
{
  "employee_id": 1,
  "work_date": "2026-01-10",
  "work_shift_id": "CA001",
  "notes": "Test shift - should pass"
}
```

### Expected Result:
✅ **Success** - Shift created
- Status: 201 Created
- Total hours: 35 + 8 = 43 hours (within 48 limit)

---

## Test Case 2: Exceeding Weekly Limit (SHOULD FAIL)

### Setup:
First, create 5 shifts for the employee:
- Monday Jan 6: 8 hours
- Tuesday Jan 7: 8 hours
- Wednesday Jan 8: 8 hours
- Thursday Jan 9: 8 hours
- Friday Jan 10: 8 hours
- **Total: 40 hours**

### Steps:
1. Try to add Saturday shift:
```json
{
  "employee_id": 1,
  "work_date": "2026-01-11",
  "work_shift_id": "CA001",
  "notes": "This should fail - exceeds 48h"
}
```

2. Try to add another 8-hour shift

### Expected Result:
❌ **Error** - Shift rejected
```json
{
  "title": "Vượt Giới Hạn 48 Giờ/Tuần",
  "status": 400,
  "detail": "⚠️ Cảnh báo: Vượt giới hạn giờ làm việc tuần. Nhân viên đã được xếp lịch 40.0 giờ...",
  "properties": {
    "employeeId": 1,
    "weekStart": "2026-01-06",
    "weekEnd": "2026-01-12",
    "existingHours": 40.0,
    "newShiftHours": 8.0,
    "totalHours": 48.0
  }
}
```

---

## Test Case 3: Exactly at Limit (SHOULD PASS)

### Setup:
- Employee has 40 hours in current week
- Try to add 8-hour shift

### Expected Result:
✅ **Success** - Exactly 48 hours allowed

---

## Test Case 4: New Week Reset

### Setup:
- Employee has 48 hours in Week 1 (Jan 6-12)
- Try to create shift for Monday, Jan 13 (Week 2)

### Steps:
```json
{
  "employee_id": 1,
  "work_date": "2026-01-13",
  "work_shift_id": "CA001",
  "notes": "New week - should pass"
}
```

### Expected Result:
✅ **Success** - New week, counter resets to 0

---

## Test Case 5: Batch Registration with Overflow

### Setup:
Create employee shift registration for Mon-Sat (6 days × 8 hours = 48 hours)

### Steps:
1. Create registration with daysOfWeek: [1,2,3,4,5,6]
2. Check logs for skipped shifts

### Expected Result:
⚠️ **Partial Success**
- Mon-Fri created (40 hours)
- Saturday skipped (would exceed 48 hours)
- Log message: "Skipping shift creation for 2026-01-11 due to weekly limit"

---

## Verification Queries

### Check Employee's Weekly Hours:
```sql
SELECT 
    e.employee_code,
    es.work_date,
    ws.shift_name,
    ws.start_time,
    ws.end_time,
    EXTRACT(EPOCH FROM (ws.end_time - ws.start_time))/3600 AS hours
FROM employee_shifts es
JOIN employees e ON es.employee_id = e.employee_id
JOIN work_shifts ws ON es.work_shift_id = ws.work_shift_id
WHERE e.employee_id = 1
  AND es.work_date BETWEEN '2026-01-06' AND '2026-01-12'
  AND es.status = 'SCHEDULED'
ORDER BY es.work_date;
```

### Calculate Total Hours:
```sql
SELECT 
    e.employee_code,
    SUM(EXTRACT(EPOCH FROM (ws.end_time - ws.start_time))/3600) AS total_hours
FROM employee_shifts es
JOIN employees e ON es.employee_id = e.employee_id
JOIN work_shifts ws ON es.work_shift_id = ws.work_shift_id
WHERE e.employee_id = 1
  AND es.work_date BETWEEN '2026-01-06' AND '2026-01-12'
  AND es.status = 'SCHEDULED'
GROUP BY e.employee_code;
```

---

## API Testing with cURL

### Create Shift (Within Limit):
```bash
curl -X POST http://localhost:8080/api/employee-shifts/manual \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employee_id": 1,
    "work_date": "2026-01-10",
    "work_shift_id": "CA001",
    "notes": "Test shift"
  }'
```

### Expected Success:
```json
{
  "employee_shift_id": "EMS2601100001",
  "employee": { "employee_id": 1, "employee_code": "EMP001" },
  "work_shift": { "work_shift_id": "CA001", "shift_name": "Ca Sáng" },
  "work_date": "2026-01-10",
  "status": "SCHEDULED"
}
```

### Expected Error (Exceeding Limit):
```json
{
  "type": "about:blank",
  "title": "Vượt Giới Hạn 48 Giờ/Tuần",
  "status": 400,
  "detail": "⚠️ Cảnh báo: Vượt giới hạn giờ làm việc tuần..."
}
```

---

## Log Messages to Look For

### Success Case:
```
INFO  WeeklyOvertimeLimitService : Validating weekly working hours limit for employee 1 on 2026-01-10
DEBUG WeeklyOvertimeLimitService : Week boundaries: 2026-01-06 to 2026-01-12
DEBUG WeeklyOvertimeLimitService : Employee 1 week 2026-01-06-2026-01-12: existing=40.0h, new=8.0h, total=48.0h (limit: 48h)
INFO  WeeklyOvertimeLimitService : Weekly working hours validation passed for employee 1
```

### Failure Case:
```
INFO  WeeklyOvertimeLimitService : Validating weekly working hours limit for employee 1 on 2026-01-11
DEBUG WeeklyOvertimeLimitService : Week boundaries: 2026-01-06 to 2026-01-12
DEBUG WeeklyOvertimeLimitService : Employee 1 week 2026-01-06-2026-01-12: existing=48.0h, new=8.0h, total=56.0h (limit: 48h)
ERROR WeeklyOvertimeLimitService : Weekly limit exceeded - throwing error
```

---

## Summary Checklist

- [ ] Test Case 1: Normal shift creation passes
- [ ] Test Case 2: Exceeding limit blocked with proper error
- [ ] Test Case 3: Exactly 48 hours allowed
- [ ] Test Case 4: New week resets counter
- [ ] Test Case 5: Batch creation skips overflow shifts
- [ ] Error message is in Vietnamese
- [ ] Error includes all required properties
- [ ] Logs show detailed calculation
- [ ] Database queries confirm hour totals

**Status**: Ready for Testing ✅
