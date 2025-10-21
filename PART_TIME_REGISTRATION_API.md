# Part-Time Employee Shift Registration API

## Overview

This document describes the implementation of the Part-Time Employee Shift Registration feature (API 14: POST /api/v1/registrations).

## Business Rules

### 1. Employment Type Validation

- **Only PART_TIME employees can create shift registrations**
- Full-time employees attempting to create registrations will receive a 403 Forbidden error
- The system checks the `employment_type` field in the `employees` table

### 2. Date Validation

- **Effective From Date**: Cannot be in the past
- **Effective To Date**: Must be greater than or equal to the Effective From date
- Invalid dates will result in a 400 Bad Request error

### 3. Work Shift Validation

- The `work_shift_id` must exist in the `work_shifts` table
- The work shift must be active (`is_active = true`)
- Non-existent or inactive work shifts will result in a 404 Not Found error

### 4. Conflict Detection

- The system prevents duplicate registrations for the same employee, work shift, and day of week
- Checks for overlapping active registrations
- Conflicts will result in a 409 Conflict error with details about the existing registration

## API Endpoint

### POST /api/v1/registrations

**Authorization**: Requires `CREATE_REGISTRATION` permission

**Request Body**:

```json
{
  "employeeId": 123,
  "workShiftId": "SLT-250116-001",
  "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "effectiveFrom": "2025-02-01",
  "effectiveTo": "2025-12-31"
}
```

**Response Codes**:

- `201 Created`: Registration created successfully
- `400 Bad Request`: Invalid request data (missing fields, invalid dates)
- `403 Forbidden`: Employee is not PART_TIME
- `404 Not Found`: Employee or Work Shift not found
- `409 Conflict`: Conflicts with existing active registration

**Success Response (201)**:

```json
{
  "registrationId": "REG-250116-001",
  "employeeId": 123,
  "slotId": "SLT-250116-001",
  "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "effectiveFrom": "2025-02-01",
  "effectiveTo": "2025-12-31",
  "isActive": true
}
```

## Implementation Details

### Service Method: `createRegistration`

The `EmployeeShiftRegistrationService.createRegistration()` method performs the following steps:

1. **Validate Employee Exists**

   - Checks if the employee exists in the database
   - Throws `EmployeeNotFoundException` if not found

2. **Validate Employment Type**

   - Checks if `employee.employmentType == PART_TIME`
   - Throws `InvalidEmploymentTypeException` if employee is FULL_TIME

3. **Validate Work Shift**

   - Checks if work shift exists and is active
   - Throws `WorkShiftNotFoundException` if not found or inactive

4. **Validate Dates**

   - Ensures `effectiveFrom` is not in the past
   - Ensures `effectiveTo >= effectiveFrom` (if provided)
   - Throws `InvalidRegistrationDateException` for invalid dates

5. **Check for Conflicts**

   - Queries for existing active registrations with same employee, slot, and days
   - Throws `RegistrationConflictException` if conflicts are found

6. **Generate Registration ID**

   - Uses `IdGenerator.generateId("REG")` to create a unique ID
   - Format: `REG-YYMMDD-SEQ` (e.g., `REG-250116-001`)

7. **Create and Save Registration**

   - Creates `EmployeeShiftRegistration` entity
   - Saves to `employee_shift_registrations` table

8. **Create and Save Registration Days**

   - Creates `RegistrationDays` entities for each day in `daysOfWeek`
   - Saves to `registration_days` table with composite key

9. **Return Response**
   - Loads the complete registration with days (using `@EntityGraph`)
   - Maps to `ShiftRegistrationResponse` DTO

### Database Tables

#### employee_shift_registrations

```sql
registration_id VARCHAR(20) PRIMARY KEY  -- REG-250116-001
employee_id INT                           -- Foreign key to employees
slot_id VARCHAR(20)                       -- Foreign key to work_shifts
effective_from DATE                       -- Start date
effective_to DATE                         -- End date (nullable)
is_active BOOLEAN DEFAULT TRUE           -- Soft delete flag
```

#### registration_days

```sql
registration_id VARCHAR(20)              -- Foreign key to employee_shift_registrations
day_of_week VARCHAR(10)                  -- MONDAY, TUESDAY, etc.
PRIMARY KEY (registration_id, day_of_week)
```

### Exception Handling

#### InvalidEmploymentTypeException (403 Forbidden)

Thrown when a non-PART_TIME employee attempts to create a registration.

**Vietnamese Message**:

```
Chỉ nhân viên PART_TIME mới được đăng ký ca làm.
Nhân viên này có loại hợp đồng: FULL_TIME
```

#### RegistrationConflictException (409 Conflict)

Thrown when a registration conflicts with an existing active registration.

**Vietnamese Message**:

```
Đã tồn tại đăng ký hoạt động cho nhân viên 123, ca SLT-250116-001 vào các ngày: MONDAY, WEDNESDAY.
Registration ID: REG-250115-005, Hiệu lực từ: 2025-01-15 đến: 2025-12-31
```

#### InvalidRegistrationDateException (400 Bad Request)

Thrown when dates are invalid.

**Vietnamese Messages**:

```
Ngày bắt đầu hiệu lực không thể là quá khứ. Ngày bắt đầu: 2025-01-10, Ngày hiện tại: 2025-01-16

Ngày kết thúc hiệu lực phải sau hoặc bằng ngày bắt đầu.
Ngày bắt đầu: 2025-02-01, Ngày kết thúc: 2025-01-31
```

## Testing

### Test Case 1: Successful Registration

```json
POST /api/v1/registrations
{
  "employeeId": 123,
  "workShiftId": "SLT-250116-001",
  "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "effectiveFrom": "2025-02-01",
  "effectiveTo": "2025-12-31"
}
```

**Expected**: 201 Created with registration details

### Test Case 2: Full-Time Employee Attempt

```json
POST /api/v1/registrations
{
  "employeeId": 456,  // Full-time employee
  "workShiftId": "SLT-250116-001",
  "daysOfWeek": ["MONDAY"],
  "effectiveFrom": "2025-02-01"
}
```

**Expected**: 403 Forbidden - "Chỉ nhân viên PART_TIME mới được đăng ký ca làm"

### Test Case 3: Past Effective Date

```json
POST /api/v1/registrations
{
  "employeeId": 123,
  "workShiftId": "SLT-250116-001",
  "daysOfWeek": ["MONDAY"],
  "effectiveFrom": "2025-01-10"  // Past date
}
```

**Expected**: 400 Bad Request - "Ngày bắt đầu hiệu lực không thể là quá khứ"

### Test Case 4: Conflict with Existing Registration

```json
POST /api/v1/registrations
{
  "employeeId": 123,
  "workShiftId": "SLT-250116-001",
  "daysOfWeek": ["MONDAY"],  // Already registered
  "effectiveFrom": "2025-02-01"
}
```

**Expected**: 409 Conflict with details about the existing registration

## Permission Configuration

Add the following permission to roles that should be able to create registrations:

```java
public static final String CREATE_REGISTRATION = "CREATE_REGISTRATION";
```

**Recommended Role Assignments**:

- RECEPTIONIST: CREATE_REGISTRATION (to register part-time employees for shifts)
- PART_TIME employees: CREATE_REGISTRATION (to register themselves)

## Related APIs

- **API 12**: GET /api/v1/registrations - View all registrations (with permission filtering)
- **API 13**: GET /api/v1/registrations/{id} - View single registration details

## Files Modified/Created

### Service Layer

- `EmployeeShiftRegistrationService.java` - Added `createRegistration()` method

### Controller Layer

- `EmployeeShiftRegistrationController.java` - Added POST endpoint

### Exception Layer

- `InvalidEmploymentTypeException.java` - Added String constructor
- `RegistrationConflictException.java` - Added String constructor
- `EmployeeNotFoundException.java` - Added Integer constructor

### Constants

- `AuthoritiesConstants.java` - Added `CREATE_REGISTRATION` constant

## Notes

- The system uses composite keys for `registration_days` table
- All database operations are transactional
- The `IdGenerator` ensures unique IDs with date-based prefixes
- Conflict detection considers only active registrations (`is_active = true`)
- Days of week are validated at the enum level (MONDAY-SUNDAY)
- The effective_to date is optional (nullable for indefinite registrations)
