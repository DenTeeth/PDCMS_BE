# Part-Time Employee Shift Registration - Update & Delete APIs

## Overview

This document describes the implementation of the Update and Delete operations for Part-Time Employee Shift Registration feature:

- **API 15**: PATCH /api/v1/registrations/{registration_id} - Partial update
- **API 16**: PUT /api/v1/registrations/{registration_id} - Full replacement
- **API 17**: DELETE /api/v1/registrations/{registration_id} - Soft delete

---

## API 15: PATCH - Partial Update

### PATCH /api/v1/registrations/{registration_id}

**Description**: Cập nhật một phần thông tin đăng ký ca làm. Chỉ các trường được cung cấp sẽ được cập nhật.

**Authorization**:

- `UPDATE_REGISTRATION_ALL` (Admin): Cập nhật bất kỳ registration nào
- `UPDATE_REGISTRATION_OWN`: Chỉ cập nhật của chính mình

**Request Body** (All fields optional):

```json
{
  "workShiftId": "SLT-250116-002",
  "daysOfWeek": ["TUESDAY", "THURSDAY"],
  "effectiveFrom": "2025-03-01",
  "effectiveTo": "2025-12-31",
  "isActive": true
}
```

**Business Logic**:

1. Kiểm tra quyền sở hữu (ownership check)
2. Nếu `workShiftId` thay đổi:
   - Validate work shift tồn tại và `is_active = true`
   - Trigger conflict check
3. Nếu `daysOfWeek` thay đổi:
   - Xóa các `registration_days` cũ
   - Tạo các `registration_days` mới
   - Trigger conflict check
4. Nếu `effectiveFrom` thay đổi:
   - Validate không phải là quá khứ
5. Nếu `effectiveTo` thay đổi:
   - Validate `effectiveTo >= effectiveFrom`
6. Conflict check (nếu cần):
   - Loại trừ registration hiện tại
   - Kiểm tra không trùng với các registration khác đang active

**Response Codes**:

- `200 OK`: Cập nhật thành công
- `400 Bad Request`: Dữ liệu không hợp lệ (ngày quá khứ, ngày kết thúc < ngày bắt đầu)
- `404 Not Found`: Registration không tồn tại hoặc không có quyền cập nhật
- `409 Conflict`: Xung đột với registration đang active

**Success Response (200)**:

```json
{
  "registrationId": "REG-250116-001",
  "employeeId": 123,
  "slotId": "SLT-250116-002",
  "daysOfWeek": ["TUESDAY", "THURSDAY"],
  "effectiveFrom": "2025-03-01",
  "effectiveTo": "2025-12-31",
  "isActive": true
}
```

**Example Use Cases**:

#### 1. Update only days of week

```json
PATCH /api/v1/registrations/REG-250116-001
{
  "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"]
}
```

#### 2. Update only effective dates

```json
PATCH /api/v1/registrations/REG-250116-001
{
  "effectiveFrom": "2025-02-15",
  "effectiveTo": "2025-08-31"
}
```

#### 3. Deactivate registration

```json
PATCH /api/v1/registrations/REG-250116-001
{
  "isActive": false
}
```

---

## API 16: PUT - Full Replacement

### PUT /api/v1/registrations/{registration_id}

**Description**: Thay thế toàn bộ thông tin đăng ký ca làm. Yêu cầu tất cả các trường.

**Authorization**:

- `UPDATE_REGISTRATION_ALL` (Admin): Cập nhật bất kỳ registration nào
- `UPDATE_REGISTRATION_OWN`: Chỉ cập nhật của chính mình

**Request Body** (All fields required except effectiveTo):

```json
{
  "workShiftId": "SLT-250116-002",
  "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "effectiveFrom": "2025-02-01",
  "effectiveTo": "2025-12-31",
  "isActive": true
}
```

**Business Logic**:

1. Kiểm tra quyền sở hữu (ownership check)
2. Validate work shift tồn tại và `is_active = true`
3. Validate dates:
   - `effectiveFrom` không phải quá khứ
   - `effectiveTo >= effectiveFrom` (nếu có)
4. Check conflicts với các registration khác (loại trừ registration hiện tại)
5. Thay thế tất cả các fields
6. Xóa tất cả `registration_days` cũ
7. Tạo `registration_days` mới theo `daysOfWeek`

**Response Codes**:

- `200 OK`: Thay thế thành công
- `400 Bad Request`: Dữ liệu không hợp lệ hoặc thiếu trường bắt buộc
- `404 Not Found`: Registration không tồn tại hoặc không có quyền cập nhật
- `409 Conflict`: Xung đột với registration đang active

**Success Response (200)**:

```json
{
  "registrationId": "REG-250116-001",
  "employeeId": 123,
  "slotId": "SLT-250116-002",
  "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "effectiveFrom": "2025-02-01",
  "effectiveTo": "2025-12-31",
  "isActive": true
}
```

**Difference from PATCH**:

- **PUT**: Requires ALL fields (except optional effectiveTo), replaces entire resource
- **PATCH**: Accepts any subset of fields, updates only provided fields

---

## API 17: DELETE - Soft Delete

### DELETE /api/v1/registrations/{registration_id}

**Description**: Hủy đăng ký ca làm (xóa mềm - set `is_active = false`).

**Authorization**:

- `DELETE_REGISTRATION_ALL` (Admin): Xóa bất kỳ registration nào
- `DELETE_REGISTRATION_OWN`: Chỉ xóa của chính mình

**Request**: No body required

**Business Logic**:

1. Kiểm tra quyền sở hữu (ownership check)
2. Set `is_active = false` (KHÔNG xóa record vĩnh viễn)
3. Giữ nguyên tất cả dữ liệu khác và `registration_days`

**Response Codes**:

- `204 No Content`: Xóa thành công
- `404 Not Found`: Registration không tồn tại hoặc không có quyền xóa

**Success Response (204)**: No content

**Example**:

```bash
DELETE /api/v1/registrations/REG-250116-001
```

**Note**: Soft delete allows:

- Truy xuất lại lịch sử đăng ký
- Audit trail for compliance
- Có thể "undelete" bằng cách PATCH `isActive: true`

---

## Permission Matrix

| Operation | Permission Required       | Scope                  |
| --------- | ------------------------- | ---------------------- |
| PATCH     | `UPDATE_REGISTRATION_ALL` | All registrations      |
| PATCH     | `UPDATE_REGISTRATION_OWN` | Own registrations only |
| PUT       | `UPDATE_REGISTRATION_ALL` | All registrations      |
| PUT       | `UPDATE_REGISTRATION_OWN` | Own registrations only |
| DELETE    | `DELETE_REGISTRATION_ALL` | All registrations      |
| DELETE    | `DELETE_REGISTRATION_OWN` | Own registrations only |

**Recommended Role Assignments**:

- **Admin**: All \_ALL permissions
- **Receptionist**: UPDATE_REGISTRATION_ALL, DELETE_REGISTRATION_ALL
- **Part-time Employee**: UPDATE_REGISTRATION_OWN, DELETE_REGISTRATION_OWN

---

## Ownership Validation

The `loadRegistrationWithOwnershipCheck()` helper method implements:

1. **Admin or \_ALL permission**: Can access any registration
2. **\_OWN permission**:
   - Extract username from security context
   - Get employee_id from account
   - Only load registrations where `employee_id` matches
   - Return 404 if registration doesn't exist OR doesn't belong to user

This ensures:

- Users with \_OWN permission cannot see error messages that reveal existence of other registrations
- Consistent 404 response for both "not found" and "not authorized" cases

---

## Conflict Detection Logic

For PATCH and PUT, conflict check is performed when:

- `workShiftId` changes (PATCH only)
- `daysOfWeek` changes (both PATCH and PUT)

**Conflict Definition**: Active registration with:

- Same `employee_id`
- Same `slot_id` (work_shift_id)
- Overlapping `day_of_week`
- `is_active = true`

**Important**: Current registration is excluded from conflict check:

```java
conflicts = conflicts.stream()
    .filter(c -> !c.getRegistrationId().equals(registrationId))
    .toList();
```

---

## Error Messages (Vietnamese)

### 400 Bad Request - Invalid Dates

```
Ngày bắt đầu hiệu lực không thể là quá khứ. Ngày bắt đầu: 2025-01-10, Ngày hiện tại: 2025-01-20
```

```
Ngày kết thúc hiệu lực phải sau hoặc bằng ngày bắt đầu. Ngày bắt đầu: 2025-02-01, Ngày kết thúc: 2025-01-31
```

### 404 Not Found

```
Registration with ID 'REG-250116-999' not found or you don't have permission to modify it
```

### 409 Conflict

```
Đã tồn tại đăng ký hoạt động cho nhân viên 123, ca SLT-250116-001 vào các ngày: MONDAY, WEDNESDAY.
Registration ID: REG-250115-005, Hiệu lực từ: 2025-01-15 đến: 2025-12-31
```

---

## Test Cases

### PATCH - Test Case 1: Update Days Only

```json
PATCH /api/v1/registrations/REG-250116-001
{
  "daysOfWeek": ["TUESDAY", "THURSDAY", "SATURDAY"]
}
```

**Expected**: 200 OK with updated days

### PATCH - Test Case 2: Change to Conflicting Slot

```json
PATCH /api/v1/registrations/REG-250116-001
{
  "workShiftId": "SLT-250116-002",
  "daysOfWeek": ["MONDAY"]
}
```

**Expected**: 409 Conflict if another active registration exists for same employee, slot, day

### PATCH - Test Case 3: Not Owner

```json
PATCH /api/v1/registrations/REG-250116-999
{
  "isActive": false
}
```

**Expected**: 404 Not Found (user with \_OWN permission trying to update someone else's registration)

### PUT - Test Case 1: Replace Entire Registration

```json
PUT /api/v1/registrations/REG-250116-001
{
  "workShiftId": "SLT-250116-003",
  "daysOfWeek": ["FRIDAY", "SATURDAY", "SUNDAY"],
  "effectiveFrom": "2025-03-01",
  "effectiveTo": "2025-09-30",
  "isActive": true
}
```

**Expected**: 200 OK with completely replaced registration

### PUT - Test Case 2: Missing Required Field

```json
PUT /api/v1/registrations/REG-250116-001
{
  "workShiftId": "SLT-250116-003",
  "daysOfWeek": ["FRIDAY"],
  "effectiveFrom": "2025-03-01"
  // Missing isActive
}
```

**Expected**: 400 Bad Request - "Active status is required"

### DELETE - Test Case 1: Soft Delete Own Registration

```bash
DELETE /api/v1/registrations/REG-250116-001
```

**Expected**: 204 No Content, `is_active` set to false

### DELETE - Test Case 2: Verify Soft Delete

```bash
GET /api/v1/registrations/REG-250116-001
```

**Expected**: 200 OK with `isActive: false` (if user has view permission)

---

## Database Operations

### PATCH Operation

1. **SELECT**: Load existing registration
2. **UPDATE**: Update `employee_shift_registrations` table (only changed fields)
3. **DELETE + INSERT** (if daysOfWeek changed):
   - Delete from `registration_days`
   - Insert new rows to `registration_days`
4. **SELECT**: Reload complete registration for response

### PUT Operation

1. **SELECT**: Load existing registration
2. **UPDATE**: Update ALL fields in `employee_shift_registrations`
3. **DELETE**: Delete all from `registration_days` for this registration
4. **INSERT**: Insert all new `registration_days`
5. **SELECT**: Reload complete registration for response

### DELETE Operation

1. **SELECT**: Load existing registration
2. **UPDATE**: Set `is_active = false` in `employee_shift_registrations`

---

## Files Modified/Created

### DTOs

- `UpdateShiftRegistrationRequest.java` - PATCH request DTO (all fields optional)
- `ReplaceShiftRegistrationRequest.java` - PUT request DTO (all fields required)

### Service Layer

- `EmployeeShiftRegistrationService.java`:
  - `updateRegistration()` - PATCH logic
  - `replaceRegistration()` - PUT logic
  - `deleteRegistration()` - DELETE logic
  - `loadRegistrationWithOwnershipCheck()` - Helper for ownership validation

### Controller Layer

- `EmployeeShiftRegistrationController.java`:
  - PATCH endpoint
  - PUT endpoint
  - DELETE endpoint

### Constants

- `AuthoritiesConstants.java`:
  - `UPDATE_REGISTRATION_ALL`
  - `UPDATE_REGISTRATION_OWN`
  - `DELETE_REGISTRATION_ALL`
  - `DELETE_REGISTRATION_OWN`

---

## Summary of All Registration APIs

| #   | Method | Endpoint                   | Description              | Permission                    |
| --- | ------ | -------------------------- | ------------------------ | ----------------------------- |
| 12  | GET    | /api/v1/registrations      | List all registrations   | VIEW_REGISTRATION_ALL / OWN   |
| 13  | GET    | /api/v1/registrations/{id} | Get registration details | VIEW_REGISTRATION_ALL / OWN   |
| 14  | POST   | /api/v1/registrations      | Create new registration  | CREATE_REGISTRATION           |
| 15  | PATCH  | /api/v1/registrations/{id} | Partial update           | UPDATE_REGISTRATION_ALL / OWN |
| 16  | PUT    | /api/v1/registrations/{id} | Full replacement         | UPDATE_REGISTRATION_ALL / OWN |
| 17  | DELETE | /api/v1/registrations/{id} | Soft delete              | DELETE_REGISTRATION_ALL / OWN |

**Feature Complete**: All CRUD operations for Part-Time Employee Shift Registrations ✅
