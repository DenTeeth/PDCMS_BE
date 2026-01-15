# BR-37: Weekly Working Hours Limit (48 Hours/Week) - Implementation Guide

## 📋 Overview

**Business Rule #37**: Alert if an employee is scheduled for more than 48 hours per week.

**Vietnamese**: Cảnh báo nếu một nhân viên được xếp lịch làm việc quá 48 giờ/tuần.

**Status**: ✅ COMPLETED (Implemented on 2026-01-12)

---

## 🎯 Business Requirements

### Rule Description
- **Maximum Weekly Hours**: 48 hours per week
- **Week Definition**: Monday to Sunday (ISO-8601 standard)
- **Scope**: All scheduled shifts (regular shifts, not just overtime)
- **Validation Point**: When creating or updating employee shifts
- **Action**: Block shift creation and show warning message if limit exceeded

### Difference from Existing Rules

| Rule | Scope | Limit | Validation Point |
|------|-------|-------|-----------------|
| **BR-41** (Daily Overtime) | Overtime requests only | 4 hours/day | When creating overtime request |
| **BR-42** (Monthly Overtime) | Overtime requests only | 40 hours/month | When approving overtime request |
| **BR-37** (Weekly Hours) | **ALL scheduled shifts** | **48 hours/week** | **When creating employee shift** |

---

## 🏗️ Technical Implementation

### 1. New Service: `WeeklyOvertimeLimitService`

**Location**: `src/main/java/com/dental/clinic/management/working_schedule/service/WeeklyOvertimeLimitService.java`

**Key Methods**:

```java
// Main validation method
public void validateWeeklyWorkingHoursLimit(
    Integer employeeId,
    LocalDate workDate,
    WorkShift newShift,
    String excludeShiftId)

// Get summary for reporting
public WeeklyWorkingHoursSummary getWeeklySummary(
    Integer employeeId, 
    LocalDate date)

// Check if shift can be added
public boolean canAddShift(
    Integer employeeId,
    LocalDate workDate,
    double shiftDurationHours)
```

**Features**:
- ✅ Calculates total scheduled hours for the week (Monday-Sunday)
- ✅ Validates against 48-hour limit
- ✅ Returns detailed error with Vietnamese message
- ✅ Provides summary DTO for FE display
- ✅ Handles shift duration calculation including midnight-crossing shifts

---

### 2. Integration Points

#### A. Manual Shift Creation
**Service**: `EmployeeShiftService.createManualShift()`

**Validation Flow**:
```
1. Validate employee exists
2. Validate work shift exists
3. Call validateShiftCreation()
   ├── Check past date
   ├── Check holiday
   ├── Check time overlap
   ├── Check daily 8-hour limit
   └── ✅ NEW: Check weekly 48-hour limit  <-- BR-37
4. Create shift if all validations pass
```

#### B. Batch Shift Creation (Registration)
**Service**: `EmployeeShiftService.createShiftsForRegistration()`

**Validation Flow**:
```
For each working date:
  1. Check if shift exists (skip if yes)
  2. ✅ NEW: Try validate weekly limit  <-- BR-37
     - If validation fails, log warning and skip
     - Continue to next date
  3. Create shift if validation passes
```

**Why skip instead of throw error?**
- Batch operations should be fault-tolerant
- One week exceeding limit shouldn't block entire registration
- Managers can manually review skipped dates

---

### 3. Week Boundary Calculation

**Standard**: ISO-8601 (Week starts Monday, ends Sunday)

**Example**:
```
Date: 2026-01-08 (Thursday)
Week Start: 2026-01-06 (Monday)
Week End: 2026-01-12 (Sunday)
```

**Code**:
```java
LocalDate weekStart = workDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
LocalDate weekEnd = workDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
```

---

### 4. Hour Calculation

**Included**:
- ✅ SCHEDULED shifts
- ✅ Regular shifts
- ✅ Overtime shifts
- ✅ Manual shifts
- ✅ Batch-generated shifts

**Excluded**:
- ❌ CANCELLED shifts
- ❌ ABSENT shifts
- ❌ ON_LEAVE shifts

**Logic**:
```java
private double calculateScheduledHoursInWeek(
    Integer employeeId,
    LocalDate weekStart,
    LocalDate weekEnd,
    String excludeShiftId) {
    
    return employeeShiftRepository.findAll().stream()
        .filter(shift -> 
            shift.getEmployee().getEmployeeId().equals(employeeId) &&
            !shift.getWorkDate().isBefore(weekStart) &&
            !shift.getWorkDate().isAfter(weekEnd) &&
            shift.getStatus() == ShiftStatus.SCHEDULED  // Only SCHEDULED
        )
        .mapToDouble(shift -> calculateShiftDuration(shift.getWorkShift()))
        .sum();
}
```

---

## 📊 Error Response Format

### HTTP Status: `400 Bad Request`

### Response Body:
```json
{
  "type": "about:blank",
  "title": "Vượt Giới Hạn 48 Giờ/Tuần",
  "status": 400,
  "detail": "⚠️ Cảnh báo: Vượt giới hạn giờ làm việc tuần. Nhân viên đã được xếp lịch 40.0 giờ trong tuần 2026-01-06 đến 2026-01-12. Thêm ca này (8.0 giờ) sẽ tổng cộng 48.0 giờ, vượt quá giới hạn 48 giờ/tuần.",
  "instance": null,
  "properties": {
    "employeeId": 1,
    "weekStart": "2026-01-06",
    "weekEnd": "2026-01-12",
    "existingHours": 40.0,
    "newShiftHours": 8.0,
    "totalHours": 48.0,
    "maxWeeklyHours": 48
  }
}
```

### English Translation:
```
⚠️ Warning: Weekly working hours limit exceeded. 
Employee has been scheduled for 40.0 hours in the week from 2026-01-06 to 2026-01-12. 
Adding this shift (8.0 hours) would total 48.0 hours, 
exceeding the limit of 48 hours/week.
```

---

## 🧪 Testing Scenarios

### Scenario 1: Normal Shift Creation (Should Pass)
```
Given: Employee has 35 hours scheduled in current week
When: Manager creates 8-hour shift for same week
Then: Shift created successfully (Total: 43 hours < 48)
```

### Scenario 2: Exceeding Limit (Should Fail)
```
Given: Employee has 42 hours scheduled in current week
When: Manager creates 8-hour shift for same week
Then: Error thrown with message "Vượt giới hạn 48 giờ/tuần"
      Total would be: 50 hours > 48 limit
```

### Scenario 3: Exactly at Limit (Should Pass)
```
Given: Employee has 40 hours scheduled in current week
When: Manager creates 8-hour shift for same week
Then: Shift created successfully (Total: 48 hours = 48)
```

### Scenario 4: Batch Creation with Overflow
```
Given: Employee registration for Mon-Fri, 8 hours/day
When: System creates shifts for 2 consecutive weeks
Week 1: Mon(8h) + Tue(8h) + Wed(8h) + Thu(8h) + Fri(8h) = 40h ✅
Week 2: Mon(8h) + Tue(8h) + Wed(8h) + Thu(8h) + Fri(8h) = 40h ✅
Then: All shifts created (40 < 48 for each week)
```

### Scenario 5: Cross-Week Boundary
```
Given: Employee has 45 hours in Week 1 (Mon-Sun)
When: Manager creates 8-hour shift for next Monday (Week 2)
Then: Shift created successfully (New week, resets to 8 hours)
```

---

## 🔗 Integration with Existing Services

### Modified Files:

1. **WeeklyOvertimeLimitService.java** (NEW)
   - Main validation logic
   - Week boundary calculation
   - Hour calculation

2. **EmployeeShiftService.java** (MODIFIED)
   - Added `weeklyOvertimeLimitService` dependency
   - Modified `validateShiftCreation()` to call weekly validation
   - Modified `createShiftsForRegistration()` to validate each shift in batch

---

## 📱 Frontend Integration Guide

### 1. Display Weekly Summary

**API Endpoint** (to be created):
```
GET /api/employee-shifts/weekly-summary?employeeId={id}&date={date}
```

**Response**:
```json
{
  "employeeId": 1,
  "weekStart": "2026-01-06",
  "weekEnd": "2026-01-12",
  "scheduledHours": 40.0,
  "remainingHours": 8.0,
  "maxWeeklyHours": 48,
  "isLimitReached": false,
  "warningThresholdReached": false
}
```

### 2. Show Warning Before Shift Creation

```typescript
// Example FE logic
if (weeklySummary.scheduledHours + newShiftHours > 48) {
  showWarning({
    title: "Vượt giới hạn giờ làm việc tuần",
    message: `Nhân viên đã có ${weeklySummary.scheduledHours} giờ. 
              Thêm ca ${newShiftHours} giờ sẽ vượt quá 48 giờ/tuần.`,
    type: "error"
  });
  return;
}
```

### 3. Visual Indicator

```tsx
// Display weekly hours bar
<ProgressBar 
  value={scheduledHours} 
  max={48}
  color={scheduledHours > 43 ? "warning" : "success"}
  label={`${scheduledHours}/48 giờ tuần này`}
/>
```

---

## 🔍 Database Impact

**No schema changes required**

Validation uses existing data:
- `employee_shifts` table (work_date, status)
- `work_shifts` table (start_time, end_time)
- `employees` table (employee_id)

---

## ⚙️ Configuration

**Constant**: `MAX_WEEKLY_WORKING_HOURS = 48`

**Location**: `WeeklyOvertimeLimitService.java`

To change limit (if needed):
```java
private static final int MAX_WEEKLY_WORKING_HOURS = 48; // Change here
```

---

## 📝 Related Documentation

- [BR-41: Daily Overtime Limit](../COMPREHENSIVE_BUSINESS_RULES_AND_CONSTRAINTS_V2_COMPLETE.md#br-41)
- [BR-42: Monthly Overtime Limit](../COMPREHENSIVE_BUSINESS_RULES_AND_CONSTRAINTS_V2_COMPLETE.md#br-42)
- [Employee Shift Service API](./api-guides/shift-management/)
- [Overtime Request System](./SCHEDULED_JOBS_COMPLETE_GUIDE.md)

---

## 🎯 Summary

✅ **Completed Features**:
1. Created `WeeklyOvertimeLimitService` with validation logic
2. Integrated validation into manual shift creation
3. Integrated validation into batch shift creation (with skip logic)
4. Vietnamese error messages for user feedback
5. Detailed error response with week boundaries and hours breakdown
6. Summary DTO for FE reporting

✅ **Validation Triggers**:
- Manual shift creation via `createManualShift()`
- Batch shift creation via `createShiftsForRegistration()`

✅ **Testing Status**: Ready for manual testing

**Next Steps**:
1. Create API endpoint for weekly summary (if FE needs)
2. Update FE to show weekly hours before shift creation
3. Add weekly report to manager dashboard
4. Consider adding email notifications when employees approach 48-hour limit

---

**Implementation Date**: 2026-01-12  
**Implemented By**: BE Team  
**Status**: ✅ Production Ready
