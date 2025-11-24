#  Fixed Registration API - Common Errors Guide (For Frontend Team)

**API Endpoint**: `POST /api/v1/fixed-registrations`

---

##  **ISSUE #1: SEQUENCE DESYNC (FIXED!)**

### Error Message:

```
ERROR: duplicate key value violates unique constraint "fixed_shift_registrations_pkey"
Detail: Key (registration_id)=(2) already exists.
```

### Status: ** FIXED AUTOMATICALLY**

- Sequence đã được reset về giá trị đúng (8)
- Không còn xảy ra lỗi này nữa

---

## ️ **POTENTIAL ERRORS THAT MAY OCCUR**

### **Error #2: Employee Not Found**

**HTTP Status**: `404 Not Found`

**Request**:

```json
{
  "employeeId": 999, // ← Employee không tồn tại
  "workShiftId": "WKS_MORNING_02",
  "daysOfWeek": [1, 2, 3],
  "effectiveFrom": "2025-11-01"
}
```

**Response**:

```json
{
  "statusCode": 404,
  "error": "error.resource_not_found",
  "message": "Nhân viên không tồn tại",
  "data": null
}
```

**Fix**: Kiểm tra `employeeId` có tồn tại trong database không.

---

### **Error #3: Work Shift Not Found**

**HTTP Status**: `404 Not Found`

**Request**:

```json
{
  "employeeId": 1,
  "workShiftId": "INVALID_SHIFT", // ← Work shift không tồn tại
  "daysOfWeek": [1, 2, 3],
  "effectiveFrom": "2025-11-01"
}
```

**Response**:

```json
{
  "statusCode": 404,
  "error": "error.resource_not_found",
  "message": "Ca làm việc không tồn tại",
  "data": null
}
```

**Fix**: Kiểm tra `workShiftId` có tồn tại. Các work shift hợp lệ:

- `WKS_MORNING_01`
- `WKS_MORNING_02`
- `WKS_AFTERNOON_01`
- `WKS_AFTERNOON_02`
- etc.

---

### **Error #4: Invalid Employee Type**

**HTTP Status**: `400 Bad Request`

**Scenario**: Tạo fixed registration cho PART_TIME_FLEX employee

**Request**:

```json
{
  "employeeId": 10, // ← Employee type = PART_TIME_FLEX
  "workShiftId": "WKS_MORNING_02",
  "daysOfWeek": [1, 2, 3],
  "effectiveFrom": "2025-11-01"
}
```

**Response**:

```json
{
  "statusCode": 400,
  "error": "error.invalid_employee_type",
  "message": "Chỉ nhân viên FULL_TIME hoặc PART_TIME_FIXED mới được tạo lịch cố định",
  "data": null
}
```

**Fix**: Chỉ cho phép employee với `employmentType` = `FULL_TIME` hoặc `PART_TIME_FIXED`.

---

### **Error #5: Effective Date in the Past**

**HTTP Status**: `400 Bad Request`

**Request**:

```json
{
  "employeeId": 1,
  "workShiftId": "WKS_MORNING_02",
  "daysOfWeek": [1, 2, 3],
  "effectiveFrom": "2025-10-01" // ← Ngày trong quá khứ
}
```

**Response**:

```json
{
  "statusCode": 400,
  "error": "error.invalid_argument",
  "message": "Ngày bắt đầu không được là quá khứ",
  "data": null
}
```

**Fix**: `effectiveFrom` phải >= ngày hiện tại.

---

### **Error #6: Empty Days of Week**

**HTTP Status**: `400 Bad Request`

**Request**:

```json
{
  "employeeId": 1,
  "workShiftId": "WKS_MORNING_02",
  "daysOfWeek": [], // ← Array rỗng
  "effectiveFrom": "2025-11-01"
}
```

**Response**:

```json
{
  "statusCode": 400,
  "error": "error.invalid_argument",
  "message": "Danh sách ngày làm việc không được rỗng",
  "data": null
}
```

**Fix**: `daysOfWeek` phải có ít nhất 1 ngày.

---

### **Error #7: Invalid Day of Week Value**

**HTTP Status**: `400 Bad Request`

**Request**:

```json
{
  "employeeId": 1,
  "workShiftId": "WKS_MORNING_02",
  "daysOfWeek": [1, 2, 8], // ← 8 không hợp lệ (chỉ 1-7)
  "effectiveFrom": "2025-11-01"
}
```

**Response**:

```json
{
  "statusCode": 400,
  "error": "error.invalid_argument",
  "message": "Ngày làm việc phải từ 1 (Thứ 2) đến 7 (Chủ nhật): 8",
  "data": null
}
```

**Fix**: `daysOfWeek` chỉ chấp nhận giá trị 1-7:

- `1` = Monday (Thứ 2)
- `2` = Tuesday (Thứ 3)
- `3` = Wednesday (Thứ 4)
- `4` = Thursday (Thứ 5)
- `5` = Friday (Thứ 6)
- `6` = Saturday (Thứ 7)
- `7` = Sunday (Chủ nhật)

---

### **Error #8: Duplicate Registration**

**HTTP Status**: `409 Conflict`

**Scenario**: Employee đã có registration ACTIVE cho cùng work shift

**Request**:

```json
{
  "employeeId": 2,
  "workShiftId": "WKS_MORNING_01", // ← Employee 2 đã có registration này (is_active=true)
  "daysOfWeek": [1, 2, 3],
  "effectiveFrom": "2025-11-01"
}
```

**Response**:

```json
{
  "statusCode": 409,
  "error": "error.duplicate_registration",
  "message": "Nhân viên đã có đăng ký ca [Ca Sáng Hành Chính] đang hoạt động",
  "data": null
}
```

**Fix**:

- Kiểm tra employee đã có registration nào ACTIVE chưa
- Nếu muốn thay đổi, phải DELETE/deactivate registration cũ trước

**Check SQL**:

```sql
SELECT * FROM fixed_shift_registrations
WHERE employee_id = 2
  AND work_shift_id = 'WKS_MORNING_01'
  AND is_active = true;
```

---

### **Error #9: Missing Required Fields (Validation)**

**HTTP Status**: `400 Bad Request`

**Request**:

```json
{
  "employeeId": null, // ← Required field
  "workShiftId": "WKS_MORNING_02",
  "daysOfWeek": [1, 2, 3]
  // effectiveFrom missing
}
```

**Response**:

```json
{
  "statusCode": 400,
  "error": "error.validation",
  "message": "Validation failed",
  "data": {
    "employeeId": "Employee ID is required",
    "effectiveFrom": "Effective from date is required"
  }
}
```

**Fix**: Đảm bảo tất cả required fields:

-  `employeeId` (required)
-  `workShiftId` (required)
-  `daysOfWeek` (required, not empty)
-  `effectiveFrom` (required)
- ️ `effectiveTo` (optional - null = permanent for FULL_TIME)

---

### **Error #10: Unauthorized (Missing Token)**

**HTTP Status**: `401 Unauthorized`

**Scenario**: Gọi API không có JWT token

**Response**:

```json
{
  "statusCode": 401,
  "error": "error.unauthorized",
  "message": "Unauthorized",
  "data": null
}
```

**Fix**: Thêm Bearer token vào header:

```http
Authorization: Bearer <your_jwt_token>
```

---

### **Error #11: Forbidden (Missing Permission)**

**HTTP Status**: `403 Forbidden`

**Scenario**: User không có permission `MANAGE_FIXED_REGISTRATIONS`

**Response**:

```json
{
  "statusCode": 403,
  "error": "error.forbidden",
  "message": "Forbidden",
  "data": null
}
```

**Fix**: User phải có role ADMIN hoặc MANAGER với permission `MANAGE_FIXED_REGISTRATIONS`.

---

##  **Quick Checklist Before Calling API**

###  **Request Validation**

```javascript
// Frontend validation
const request = {
  employeeId: 1, //  Not null
  workShiftId: "WKS_MORNING_02", //  Valid format
  daysOfWeek: [1, 2, 3], //  Not empty, values 1-7
  effectiveFrom: "2025-11-01", //  >= today
};

// Validate
if (!request.employeeId) return "Employee ID required";
if (!request.workShiftId) return "Work shift ID required";
if (!request.daysOfWeek || request.daysOfWeek.length === 0)
  return "Days of week required";
if (!request.effectiveFrom) return "Effective from required";

// Validate date not in past
const today = new Date().toISOString().split("T")[0];
if (request.effectiveFrom < today)
  return "Effective from cannot be in the past";

// Validate days of week
if (request.daysOfWeek.some((d) => d < 1 || d > 7))
  return "Invalid day of week";
```

###  **Headers Required**

```http
Content-Type: application/json
Authorization: Bearer <token>
```

---

##  **How to Debug Errors**

### **Step 1: Check HTTP Status Code**

- `400` → Validation error (check request body)
- `401` → Missing/invalid token
- `403` → No permission
- `404` → Resource not found (employee/work shift)
- `409` → Duplicate registration
- `500` → Server error (check backend logs)

### **Step 2: Read Error Message**

```json
{
  "statusCode": 400,
  "error": "error.validation",
  "message": "Ngày làm việc phải từ 1 (Thứ 2) đến 7 (Chủ nhật): 8",
  "data": null
}
```

→ Message rõ ràng cho biết vấn đề là gì

### **Step 3: Common Fixes**

| Error                    | Quick Fix                          |
| ------------------------ | ---------------------------------- |
| `duplicate key`          |  Fixed! Sequence đã được reset   |
| `Employee not found`     | Kiểm tra `employeeId` tồn tại      |
| `Work shift not found`   | Kiểm tra `workShiftId` đúng format |
| `Invalid employee type`  | Chỉ dùng FULL_TIME/PART_TIME_FIXED |
| `Past date`              | Dùng ngày hiện tại hoặc tương lai  |
| `Duplicate registration` | DELETE registration cũ trước       |

---

##  **Valid Request Example**

```json
{
  "employeeId": 1,
  "workShiftId": "WKS_MORNING_02",
  "daysOfWeek": [1, 3, 5],
  "effectiveFrom": "2025-11-01",
  "effectiveTo": "2026-10-31"
}
```

**Expected Success Response**:

```json
{
  "statusCode": 201,
  "message": "Created",
  "data": {
    "registrationId": 8,
    "employeeId": 1,
    "employeeName": "Nguyễn Văn A",
    "workShiftId": "WKS_MORNING_02",
    "workShiftName": "Ca Sáng 2",
    "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
    "effectiveFrom": "2025-11-01",
    "effectiveTo": "2026-10-31",
    "isActive": true,
    "createdAt": "2025-10-31T12:00:00"
  }
}
```

---

## ️ **Database Health Check**

**Run this to verify everything is OK**:

```sql
-- 1. Check sequence is correct
SELECT last_value FROM fixed_shift_registrations_registration_id_seq;
-- Should be: 8 (or higher)

-- 2. Check max ID in table
SELECT MAX(registration_id) FROM fixed_shift_registrations;
-- Should be: 7 (or less than sequence)

-- 3. Check employees exist
SELECT employee_id, full_name, employment_type FROM employees WHERE employee_id IN (1, 2, 3);

-- 4. Check work shifts exist
SELECT work_shift_id, shift_name FROM work_shifts;
```

---

**Document Version**: 1.0
**Last Updated**: 2025-10-31
**Status**:  Sequence Issue Fixed - Ready for Testing
