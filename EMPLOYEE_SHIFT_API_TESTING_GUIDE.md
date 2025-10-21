# 📋 **Employee Shift Management API Testing Guide**

Application is running on: **http://localhost:8080**  
Swagger UI: **http://localhost:8080/swagger-ui/index.html**

---

## 🔐 **STEP 1: Login to Get JWT Token**

### **Swagger:**
1. Go to **authentication-controller** → `POST /api/v1/auth/login`
2. Click "Try it out"
3. Request body:
```json
{
  "username": "admin",
  "password": "123456"
}
```
4. Click "Execute"
5. **Copy the `accessToken`** from response

### **Postman:**
```
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

Body:
{
  "username": "admin",
  "password": "123456"
}
```

### **Other Test Accounts:**
- **Doctor 1**: `nhasi1` / `123456` (employee_id = 2)
- **Doctor 2**: `nhasi2` / `123456` (employee_id = 3)  
- **Nurse**: `yta` / `123456` (employee_id = 6)
- **Receptionist**: `letan` / `123456`

---

## 🔑 **STEP 2: Authorize in Swagger**

1. Click the **🔒 Authorize** button at top right
2. Enter: `Bearer YOUR_ACCESS_TOKEN` (replace with actual token)
3. Click "Authorize"
4. Click "Close"

---

## 📋 **STEP 3: Test 6 Employee Shift APIs**

### **API 1: GET /api/v1/shifts - Get Shifts List**

**Purpose:** Lấy danh sách ca làm việc trong khoảng thời gian

**Swagger:**
1. Go to **employee-shift-controller** → `GET /api/v1/shifts`
2. Parameters:
   - `startDate`: `2025-10-21`
   - `endDate`: `2025-10-31`
   - `employeeId`: (leave empty to see all, or enter `2` for Doctor 1)
3. Execute

**Postman:**
```
GET http://localhost:8080/api/v1/shifts?startDate=2025-10-21&endDate=2025-10-31
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:** List of shifts with employee and work shift details

```json
[
  {
    "employeeShiftId": "uuid-here",
    "workDate": "2025-10-21",
    "status": "SCHEDULED",
    "employee": {
      "employeeId": 2,
      "fullName": "Nguyễn Văn Minh"
    },
    "workShift": {
      "workShiftId": "WKS_MORNING_01",
      "shiftName": "Ca sáng 8h-12h"
    },
    "notes": "Khám bệnh buổi sáng",
    "source": "MANUAL_ENTRY",
    "isOvertime": false
  }
]
```

---

### **API 2: GET /api/v1/shifts/summary - Get Summary**

**Purpose:** Lấy báo cáo tổng hợp (placeholder)

**Swagger:**
1. Go to `GET /api/v1/shifts/summary`
2. Parameters:
   - `startDate`: `2025-10-21`
   - `endDate`: `2025-10-31`
3. Execute

**Postman:**
```
GET http://localhost:8080/api/v1/shifts/summary?startDate=2025-10-21&endDate=2025-10-31
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:**
```
"Summary endpoint - To be implemented"
```

---

### **API 3: GET /api/v1/shifts/{employeeShiftId} - Get Shift Details**

**Purpose:** Lấy chi tiết 1 ca làm việc

**Get a Shift ID first:** From API 1 response, copy an `employeeShiftId`

**Swagger:**
1. Go to `GET /api/v1/shifts/{employeeShiftId}`
2. Parameter: Paste the UUID
3. Execute

**Postman:**
```
GET http://localhost:8080/api/v1/shifts/{uuid-from-api1}
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:** Single shift details (same structure as API 1)

---

### **API 4: POST /api/v1/shifts - Create New Shift**

**Purpose:** Tạo ca làm việc mới cho nhân viên

**Swagger:**
1. Go to `POST /api/v1/shifts`
2. Request body:
```json
{
  "employeeId": 2,
  "workShiftId": "WKS_AFTERNOON_01",
  "workDate": "2025-10-26",
  "notes": "Ca chiều mới tạo",
  "source": "MANUAL_ENTRY",
  "isOvertime": false
}
```
3. Execute

**Postman:**
```
POST http://localhost:8080/api/v1/shifts
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

Body:
{
  "employeeId": 2,
  "workShiftId": "WKS_AFTERNOON_01",
  "workDate": "2025-10-26",
  "notes": "Ca chiều mới tạo",
  "source": "MANUAL_ENTRY",
  "isOvertime": false
}
```

**Expected Response:** `201 Created` with shift details

**Test Variations:**
- Try creating duplicate shift (same employee + same date + same workShiftId) → Should return `409 CONFLICT`
- Try invalid employeeId → Should return `404 NOT_FOUND`
- Try invalid workShiftId → Should return `404 NOT_FOUND`

**Available Work Shifts:**
- `WKS_MORNING_01` - Ca sáng 8h-12h
- `WKS_MORNING_02` - Ca sáng 7h-11h
- `WKS_AFTERNOON_01` - Ca chiều 13h-17h
- `WKS_AFTERNOON_02` - Ca chiều 14h-18h
- `WKS_EVENING_01` - Ca tối 18h-21h
- `WKS_NIGHT_01` - Ca đêm 21h-24h
- `WKS_NIGHT_02` - Ca đêm 22h-02h
- `WKS_OFFICE_01` - Ca hành chính 8h-17h

---

### **API 5: PATCH /api/v1/shifts/{employeeShiftId} - Update Shift**

**Purpose:** Cập nhật ca làm việc (partial update)

**Get a Shift ID first:** From API 1 or API 4 response

**Swagger:**
1. Go to `PATCH /api/v1/shifts/{employeeShiftId}`
2. Path parameter: UUID of shift to update
3. Request body (all fields optional):
```json
{
  "status": "COMPLETED",
  "notes": "Đã hoàn thành ca làm"
}
```
4. Execute

**Postman:**
```
PATCH http://localhost:8080/api/v1/shifts/{uuid-here}
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

Body:
{
  "status": "COMPLETED",
  "notes": "Đã hoàn thành ca làm"
}
```

**Expected Response:** `200 OK` with updated shift details

**Test Variations:**
- Try updating to `ON_LEAVE` status → Should return `400 BAD_REQUEST` (must use leave request system)
- Try updating already COMPLETED or CANCELLED shift → Should return `409 CONFLICT`

**Valid Status Values:**
- `SCHEDULED` - Đã lên lịch
- `COMPLETED` - Đã hoàn thành
- `ABSENT` - Vắng mặt
- `CANCELLED` - Đã hủy
- ~~`ON_LEAVE`~~ - Cannot set manually

---

### **API 6: DELETE /api/v1/shifts/{employeeShiftId} - Cancel Shift**

**Purpose:** Hủy ca làm việc (soft delete, changes status to CANCELLED)

**Get a Shift ID first:** From API 1 response (use a SCHEDULED shift)

**Swagger:**
1. Go to `DELETE /api/v1/shifts/{employeeShiftId}`
2. Path parameter: UUID of shift to cancel
3. Execute

**Postman:**
```
DELETE http://localhost:8080/api/v1/shifts/{uuid-here}
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:** `204 No Content`

**Test Variations:**
- Try canceling already COMPLETED shift → Should return `400 BAD_REQUEST`
- Try canceling shift with `source=BATCH_JOB` → Should return `400 BAD_REQUEST` (cannot cancel default full-time shifts)

---

## 🔒 **Authorization Testing**

### **Test with Different User Roles:**

1. **Admin (has VIEW_SHIFTS_ALL):**
   - ✅ Can view all employees' shifts
   - ✅ Can filter by any employeeId
   - ✅ Can create/update/delete shifts

2. **Doctor/Nurse (has VIEW_SHIFTS_OWN only):**
   - Login as `nhasi1` / `123456`
   - ✅ GET /api/v1/shifts without employeeId → See only own shifts
   - ✅ GET /api/v1/shifts?employeeId=2 → See own shifts
   - ❌ GET /api/v1/shifts?employeeId=3 → Should return 403 FORBIDDEN
   - ❌ POST /api/v1/shifts → Should return 403 FORBIDDEN (no CREATE_SHIFTS permission)

---

## 📊 **Test Data Summary**

**Employees in Database:**
- ID 1: Admin
- ID 2: Nguyễn Văn Minh (Doctor 1) - Has 5 shifts
- ID 3: Trần Thị Lan (Doctor 2) - Has 4 shifts
- ID 4: Lê Thị Mai (Receptionist)
- ID 5: Hoàng Văn Tuấn (Accountant)
- ID 6: Phạm Thị Hoa (Nurse) - Has 3 shifts

**Pre-loaded Shifts:**
- 2025-10-21: Multiple shifts (Doctor 1, Doctor 2, Nurse)
- 2025-10-22: Morning shifts
- 2025-10-23: Doctor 1 afternoon
- 2025-10-24: Doctor 1 evening (overtime)
- 2025-10-25: Doctor 2 office shift

---

## ✅ **Success Criteria Checklist**

- [ ] Successfully login and get JWT token
- [ ] API 1: Get list of shifts with date filter
- [ ] API 1: Filter by specific employee
- [ ] API 2: Get summary (placeholder working)
- [ ] API 3: Get shift details by ID
- [ ] API 4: Create new shift successfully
- [ ] API 4: Validate duplicate shift prevention
- [ ] API 5: Update shift status to COMPLETED
- [ ] API 5: Validate cannot set ON_LEAVE manually
- [ ] API 6: Cancel shift (soft delete)
- [ ] API 6: Validate cannot cancel COMPLETED shift
- [ ] Test VIEW_SHIFTS_OWN vs VIEW_SHIFTS_ALL authorization

---

## 🐛 **Common Issues & Solutions**

**Issue 1: 401 Unauthorized**
- Solution: Check if token is included in Authorization header
- Format: `Authorization: Bearer YOUR_TOKEN`

**Issue 2: 403 Forbidden**
- Solution: Check if user has required permissions
- Admin has all permissions, others have limited access

**Issue 3: 404 Not Found**
- Solution: Check if employeeId and workShiftId exist in database
- Use IDs from test data above

**Issue 4: 409 Conflict**
- Solution: Normal behavior for duplicate shifts or invalid state transitions
- This is expected validation

---

## 📝 **Notes**

- All dates in format: `YYYY-MM-DD`
- Employee IDs are integers (Long type in DB)
- Work Shift IDs are strings (e.g., `WKS_MORNING_01`)
- Employee Shift IDs are UUIDs
- Source can be: `MANUAL_ENTRY`, `BATCH_JOB`, `OVERTIME_REQUEST`
- Status transitions: SCHEDULED → COMPLETED/ABSENT/CANCELLED
- Cannot manually set status to ON_LEAVE (must use leave request system)

---

Happy Testing! 🚀
