# P7: Shift Renewal Management - API Test Guide

## 📋 **Overview**

**Module**: Phản hồi Gia hạn Lịch Cố định
**Tables**: `shift_renewal_requests`, `fixed_shift_registrations`
**Actor**: Employee (FULL_TIME hoặc PART_TIME_FIXED)
**Jira**: BE-307

**Mục đích**: Job P8 tìm các lịch Cố định (`fixed_shift_registrations`) sắp hết hạn (`effective_to` sắp đến) và tạo yêu cầu gia hạn (lời mời). API này cho phép nhân viên phản hồi lời mời đó.

---

## 🏗️ **Architecture Context**

### **Two Scheduling Streams (Luồng)**

| Stream              | Employee Type              | Registration Table          | Renewal Support |
| ------------------- | -------------------------- | --------------------------- | --------------- |
| **Luồng 1 (Fixed)** | FULL_TIME, PART_TIME_FIXED | `fixed_shift_registrations` | ✅ YES (P7)     |
| **Luồng 2 (Flex)**  | PART_TIME_FLEX             | `part_time_registrations`   | ❌ NO           |

### **P7 Business Logic - Two-Step Workflow**

| Phase                    | Actor    | Action                                        | Status                   | Registration Changed?         |
| ------------------------ | -------- | --------------------------------------------- | ------------------------ | ----------------------------- |
| **1. Creation**          | Job P8   | Auto-create renewal 14 days before expiration | `PENDING_ACTION`         | ❌ No                         |
| **2. Employee Response** | Employee | CONFIRMED or DECLINED                         | `CONFIRMED` / `DECLINED` | ❌ No (chỉ cập nhật status)   |
| **3. Admin Finalize**    | Admin    | Specify custom effective_to date              | `FINALIZED`              | ✅ YES (tạo registration mới) |

**Workflow Flow**:

```
1. Employee CONFIRMED → Chỉ cập nhật status, chờ Admin
2. Admin Finalize with custom date (3 months / 1 year / custom) → Tạo registration mới → DONE
```

**Lý do Two-Step Process**:

- Hỗ trợ thời hạn gia hạn linh hoạt (3 tháng thử việc, 6 tháng dự án, 1 năm chuẩn)
- Admin kiểm soát trước khi commit thay đổi database
- Yêu cầu phê duyệt quản lý cho quyết định business

---

## 🔑 **RBAC Permissions**

| Permission                   | Description                                              | Roles                    | Endpoint                      |
| ---------------------------- | -------------------------------------------------------- | ------------------------ | ----------------------------- |
| `VIEW_RENEWAL_OWN`           | Xem yêu cầu gia hạn của chính mình                       | ROLE_DOCTOR, ROLE_NURSE  | GET /renewals/pending         |
| `RESPOND_RENEWAL_OWN`        | Phản hồi (đồng ý/từ chối) yêu cầu gia hạn                | ROLE_DOCTOR, ROLE_NURSE  | PATCH /renewals/{id}/respond  |
| `VIEW_RENEWAL_ALL`           | Xem tất cả yêu cầu gia hạn (dashboard)                   | ROLE_ADMIN, ROLE_MANAGER | (Future API)                  |
| `MANAGE_FIXED_REGISTRATIONS` | Chốt (finalize) yêu cầu gia hạn đã được nhân viên đồng ý | ROLE_ADMIN, ROLE_MANAGER | POST /admin/renewals/finalize |

**Seed Data Location**: `src/main/resources/db/dental-clinic-seed-data_postgres_v2.sql`

---

## 📡 **API Endpoints**

### **1. GET /api/v1/registrations/renewals/pending**

Get all pending renewal requests for authenticated employee.

**Authorization**: Bearer Token (JWT)
**Required Permission**: `VIEW_RENEWAL_OWN`

#### **Request**

```http
GET /api/v1/registrations/renewals/pending
Authorization: Bearer <employee_token>
```

#### **Success Response (200 OK)**

```json
[
  {
    "renewalId": "SRR_20251022_00001",
    "expiringRegistrationId": 123,
    "employeeId": 5,
    "employeeName": "Nguyễn Văn A",
    "status": "PENDING_ACTION",
    "expiresAt": "2025-12-31T23:59:59",
    "confirmedAt": null,
    "createdAt": "2025-12-17T10:30:00",
    "declineReason": null,
    "effectiveFrom": "2024-01-01",
    "effectiveTo": "2025-12-31",
    "workShiftName": "Ca Sáng Hành Chính",
    "shiftDetails": {
      "workShiftId": "SANG_HC",
      "startTime": "07:30:00",
      "endTime": "11:30:00",
      "shiftType": "MORNING"
    },
    "message": "Lịch đăng ký ca [Ca Sáng Hành Chính] của bạn sẽ hết hạn vào ngày 2025-12-31. Bạn có muốn gia hạn không?"
  }
]
```

#### **Field Descriptions**

| Field                    | Type       | Description                                                 |
| ------------------------ | ---------- | ----------------------------------------------------------- |
| `renewalId`              | String(20) | Format: `SRR_YYYYMMDD_XXXXX`                                |
| `expiringRegistrationId` | Integer    | ID of `fixed_shift_registration` about to expire            |
| `status`                 | Enum       | `PENDING_ACTION`, `CONFIRMED`, `DECLINED`, `EXPIRED`        |
| `expiresAt`              | DateTime   | Deadline to respond (same as registration's `effective_to`) |
| `declineReason`          | String     | NULL for PENDING_ACTION/CONFIRMED, populated if DECLINED    |
| `message`                | String     | Dynamic message generated by mapper                         |

#### **Error Responses**

| Code | Scenario                 | Response                               |
| ---- | ------------------------ | -------------------------------------- |
| 401  | No token                 | `Unauthorized`                         |
| 403  | Insufficient permissions | `Forbidden - Missing VIEW_RENEWAL_OWN` |

---

### **2. PATCH /api/v1/registrations/renewals/{renewal_id}/respond**

Respond to a renewal request (CONFIRMED or DECLINED).

**Authorization**: Bearer Token (JWT)
**Required Permission**: `RESPOND_RENEWAL_OWN`

#### **Request**

**URL**: `/api/v1/registrations/renewals/SRR_20251022_00001/respond`

**Headers**:

```http
Authorization: Bearer <employee_token>
Content-Type: application/json
```

**Body (Đồng ý)**:

```json
{
  "action": "CONFIRMED"
}
```

**Body (Từ chối)**:

```json
{
  "action": "DECLINED",
  "declineReason": "Tôi dự định chuyển chỗ ở vào năm tới."
}
```

#### **Request Body Validation**

| Field           | Type   | Required    | Constraints                                                       |
| --------------- | ------ | ----------- | ----------------------------------------------------------------- |
| `action`        | String | ✓ YES       | Must be `CONFIRMED` or `DECLINED` (case-sensitive)                |
| `declineReason` | String | Conditional | **REQUIRED** if `action = DECLINED`, must not be empty/whitespace |

#### **Success Response (200 OK) - CONFIRMED**

```json
{
  "renewalId": "SRR_20251022_00001",
  "expiringRegistrationId": 123,
  "employeeId": 5,
  "employeeName": "Nguyễn Văn A",
  "status": "CONFIRMED",
  "expiresAt": "2025-12-31T23:59:59",
  "confirmedAt": "2025-12-20T14:25:30",
  "createdAt": "2025-12-17T10:30:00",
  "declineReason": null,
  "effectiveFrom": "2024-01-01",
  "effectiveTo": "2025-12-31",
  "workShiftName": "Ca Sáng Hành Chính",
  "shiftDetails": { ... },
  "message": "Bạn đã xác nhận đồng ý gia hạn. Đang chờ Admin chốt thời hạn cuối cùng."
}
```

**Background Actions (CONFIRMED)**:

1. Update renewal: `status = CONFIRMED`, `confirmed_at = NOW()`
2. **QUAN TRỌNG**: KHÔNG tự động gia hạn. Chỉ cập nhật trạng thái.
3. Bản ghi `fixed_shift_registrations` gốc vẫn giữ nguyên (`is_active = TRUE`)
4. Chờ Admin gọi Finalize API với `newEffectiveTo` tùy chỉnh

#### **Success Response (200 OK) - DECLINED**

```json
{
  "renewalId": "SRR_20251022_00001",
  "expiringRegistrationId": 123,
  "employeeId": 5,
  "employeeName": "Nguyễn Văn A",
  "status": "DECLINED",
  "expiresAt": "2025-12-31T23:59:59",
  "confirmedAt": "2025-12-20T14:28:45",
  "createdAt": "2025-12-17T10:30:00",
  "declineReason": "Tôi muốn nghỉ việc vào cuối năm nay",
  "effectiveFrom": "2024-01-01",
  "effectiveTo": "2025-12-31",
  "workShiftName": "Ca Sáng Hành Chính",
  "shiftDetails": { ... },
  "message": "Bạn đã từ chối gia hạn. Lý do: Tôi muốn nghỉ việc vào cuối năm nay"
}
```

**Background Actions (DECLINED)**:

1. Validate `declineReason` bắt buộc (throw `DeclineReasonRequiredException` if null/empty)
2. Update renewal: `status = DECLINED`, `confirmed_at = NOW()`, `decline_reason = <reason>`
3. Không thay đổi `fixed_shift_registration` (sẽ hết hạn tự nhiên)

#### **Error Responses**

| Code | Error Code          | Scenario                                        | Response Example                                                                                                    |
| ---- | ------------------- | ----------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| 400  | `REASON_REQUIRED`   | action=DECLINED but declineReason is null/empty | `{"title": "Decline Reason Required", "detail": "Vui lòng cung cấp lý do từ chối gia hạn (declineReason)"}`         |
| 403  | `NOT_OWNER`         | Employee ID in token ≠ renewal's employee_id    | `{"title": "Not Renewal Owner", "detail": "Bạn không phải chủ sở hữu của yêu cầu gia hạn SRR_20251022_00001"}`      |
| 404  | `RENEWAL_NOT_FOUND` | Renewal ID doesn't exist                        | `{"title": "Renewal Not Found", "detail": "Không tìm thấy yêu cầu gia hạn với ID: SRR_20251022_00001"}`             |
| 409  | `INVALID_STATE`     | status ≠ PENDING_ACTION (already responded)     | `{"title": "Invalid Renewal State", "detail": "Yêu cầu đang ở trạng thái CONFIRMED (chỉ cho phép PENDING_ACTION)"}` |
| 409  | `REQUEST_EXPIRED`   | expires_at < NOW()                              | `{"title": "Renewal Request Expired", "detail": "Yêu cầu gia hạn đã hết hạn vào 2025-12-31T23:59:59"}`              |

---

### **3. POST /api/v1/admin/registrations/renewals/finalize**

**Admin chốt (finalize) yêu cầu gia hạn với thời hạn tùy chỉnh**

**Authorization**: Bearer Token (JWT - Admin)
**Required Permission**: `MANAGE_FIXED_REGISTRATIONS`

**Miêu tả**: Admin (sau khi đã thỏa thuận với nhân viên) chính thức chốt thời hạn gia hạn mới.

#### **Prerequisites**

- Renewal status **MUST be CONFIRMED** (nhân viên đã đồng ý)
- Admin đã thỏa thuận với nhân viên về thời hạn gia hạn

#### **Request**

**Headers**:

```http
Authorization: Bearer <admin_token>
Content-Type: application/json
```

**Body**:

```json
{
  "renewalRequestId": "SRR_20251022_00001",
  "newEffectiveTo": "2026-03-31"
}
```

**Examples of newEffectiveTo**:

- `"2026-03-31"` - 3-month extension
- `"2026-06-30"` - 6-month extension
- `"2026-12-31"` - 1-year extension

#### **Request Body Validation**

| Field              | Type   | Required | Constraints                                                   |
| ------------------ | ------ | -------- | ------------------------------------------------------------- |
| `renewalRequestId` | String | ✓ YES    | Format: `SRR_YYYYMMDD_XXXXX`, must exist and status=CONFIRMED |
| `newEffectiveTo`   | Date   | ✓ YES    | Must be **AFTER** old registration's `effective_to`           |

#### **Success Response (200 OK)**

```json
{
  "renewalId": "SRR_20251022_00001",
  "expiringRegistrationId": 123,
  "employeeId": 5,
  "employeeName": "Nguyễn Văn A",
  "status": "FINALIZED",
  "expiresAt": "2025-12-31T23:59:59",
  "confirmedAt": "2025-12-20T14:25:30",
  "createdAt": "2025-12-17T10:30:00",
  "declineReason": null,
  "effectiveFrom": "2024-01-01",
  "effectiveTo": "2025-12-31",
  "workShiftName": "Ca Sáng Hành Chính",
  "shiftDetails": { ... },
  "message": "Admin đã chốt gia hạn thành công! Đăng ký mới có hiệu lực từ 2026-01-01 đến 2026-03-31."
}
```

**Background Actions (FINALIZED)**:

1. Validate `status = CONFIRMED` (throw `NotConfirmedByEmployeeException` nếu không)
2. Validate `newEffectiveTo > old_effective_to` (throw `InvalidEffectiveToException` nếu không hợp lệ)
3. Lock old `fixed_shift_registration` (ID=123) với `SELECT FOR UPDATE`
4. Verify `is_active = TRUE` (throw `RegistrationInactiveException` nếu false)
5. Deactivate old: `SET is_active = FALSE`
6. Insert new registration:
   - `effective_from = 2026-01-01` (old `effective_to + 1 day`)
   - `effective_to = 2026-03-31` (admin-specified date)
   - `work_shift_id`, `employee_id` copied from old
   - `is_active = TRUE`
7. Copy all `fixed_registration_days` từ old sang new (ví dụ: MONDAY, WEDNESDAY, FRIDAY)
8. Update renewal: `status = FINALIZED`

#### **Error Responses**

| Code | Error Code                  | Scenario                                       | Response Example                                                                                                                                   |
| ---- | --------------------------- | ---------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| 400  | `INVALID_EFFECTIVE_TO`      | newEffectiveTo <= old effective_to             | `{"title": "Invalid Effective To Date", "detail": "Ngày kết thúc mới (2025-11-30) phải sau ngày kết thúc cũ (2025-12-31)"}`                        |
| 403  | `FORBIDDEN`                 | Missing MANAGE_FIXED_REGISTRATIONS permission  | `Forbidden`                                                                                                                                        |
| 404  | `RENEWAL_NOT_FOUND`         | Renewal ID doesn't exist                       | `{"title": "Renewal Not Found", "detail": "Không tìm thấy yêu cầu gia hạn với ID: SRR_20251022_00001"}`                                            |
| 409  | `NOT_CONFIRMED_BY_EMPLOYEE` | status ≠ CONFIRMED (employee didn't agree yet) | `{"title": "Not Confirmed By Employee", "detail": "Nhân viên chưa xác nhận đồng ý. Yêu cầu đang ở trạng thái PENDING_ACTION (yêu cầu CONFIRMED)"}` |
| 409  | `REGISTRATION_INACTIVE`     | Old registration is_active=FALSE               | `{"title": "Registration Inactive", "detail": "Đăng ký ca ID 123 đã bị vô hiệu hóa"}`                                                              |

---

## 🧪 **Test Scenarios**

### **Scenario 1: GET Pending Renewals - Happy Path**

**Given**: Employee has 2 pending renewals (not expired, status=PENDING_ACTION)

**Steps**:

1. Login as employee (get JWT token)
2. Call `GET /api/v1/registrations/renewals/pending`

**Expected**:

- ✅ HTTP 200 OK
- ✅ Returns array with 2 renewal objects
- ✅ Each has `status = "PENDING_ACTION"`
- ✅ `expiresAt > NOW()`
- ✅ `declineReason = null`
- ✅ Dynamic `message` field populated

---

### **Scenario 2: GET Pending Renewals - No Results**

**Given**: Employee has no pending renewals (all expired or already responded)

**Expected**:

- ✅ HTTP 200 OK
- ✅ Returns empty array `[]`

---

### **Scenario 3: CONFIRMED - Employee Agrees**

**Given**: Employee has renewal `SRR_20251022_00001` for registration ID=123

**Steps**:

1. Login as employee
2. Call `PATCH /api/v1/registrations/renewals/SRR_20251022_00001/respond`
   ```json
   { "action": "CONFIRMED" }
   ```

**Expected**:

- HTTP 200 OK
- `status = "CONFIRMED"`, `confirmedAt` populated
- Old registration (123) KHÔNG THAY ĐỔI (vẫn `is_active=TRUE`)
- KHÔNG có registration mới được tạo
- Chờ Admin finalize với `newEffectiveTo` tùy chỉnh

**Verification Queries**:

```sql
-- Check renewal status
SELECT status, confirmed_at FROM shift_renewal_requests WHERE renewal_id = 'SRR_20251022_00001';
-- Expected: status='CONFIRMED', confirmed_at=NOW()

-- Check old registration STILL ACTIVE
SELECT is_active FROM fixed_shift_registrations WHERE registration_id = 123;
-- Expected: TRUE (không thay đổi)

-- Check NO new registration created
SELECT COUNT(*) FROM fixed_shift_registrations
WHERE employee_id = 5 AND is_active = TRUE AND effective_from > '2025-12-31';
-- Expected: 0 (sẽ được tạo bởi Admin Finalize API)
```

---

### **Scenario 3B: Admin Finalize - 3 Month Extension**

**Given**:

- Renewal `SRR_20251022_00001` has `status=CONFIRMED` (nhân viên đã đồng ý)
- Old registration (123): `effective_from=2024-01-01`, `effective_to=2025-12-31`

**Steps**:

1. Login as Admin
2. Thỏa thuận với nhân viên → Đồng ý gia hạn thử 3 tháng
3. Call `POST /api/v1/admin/registrations/renewals/finalize`
   ```json
   {
     "renewalRequestId": "SRR_20251022_00001",
     "newEffectiveTo": "2026-03-31"
   }
   ```

**Expected**:

- HTTP 200 OK
- `status = "FINALIZED"`
- **Database Changes**:
  - Old registration (123): `is_active = FALSE`
  - New registration (ví dụ: 456):
    - `effective_from = 2026-01-01` (old_to + 1 day)
    - `effective_to = 2026-03-31` (admin-specified)
    - `is_active = TRUE`
  - `fixed_registration_days` được copy

**Verification Queries**:

```sql
-- Check old registration deactivated
SELECT is_active FROM fixed_shift_registrations WHERE registration_id = 123;
-- Expected: FALSE

-- Check new registration created with custom date
SELECT registration_id, effective_from, effective_to, is_active
FROM fixed_shift_registrations
WHERE employee_id = 5 AND is_active = TRUE;
-- Expected: 1 row với dates 2026-01-01 to 2026-03-31

-- Check days copied
SELECT day_of_week FROM fixed_registration_days WHERE registration_id = <new_id>;
-- Expected: Same days as old registration

-- Check renewal finalized
SELECT status FROM shift_renewal_requests WHERE renewal_id = 'SRR_20251022_00001';
-- Expected: 'FINALIZED'
```

---

### **Scenario 3C: Admin Finalize - 1 Year Extension (Standard)**

**Given**: Same as Scenario 3B

**Steps**:

```json
{
  "renewalRequestId": "SRR_20251022_00001",
  "newEffectiveTo": "2026-12-31"
}
```

**Expected**: Same as 3B, nhưng `effective_to = 2026-12-31` (gia hạn 1 năm)

---

### **Scenario 4: DECLINED with Reason**

**Steps**:

1. Call `PATCH /api/v1/registrations/renewals/SRR_20251022_00002/respond`
   ```json
   {
     "action": "DECLINED",
     "declineReason": "Sẽ chuyển đến chi nhánh khác vào tháng 1/2026"
   }
   ```

**Expected**:

- ✅ HTTP 200 OK
- ✅ `status = "DECLINED"`, `confirmedAt` populated
- ✅ `declineReason = "Sẽ chuyển đến chi nhánh khác vào tháng 1/2026"`
- ✅ Old registration **unchanged** (will expire at `effective_to`)

**Verification**:

```sql
SELECT status, decline_reason FROM shift_renewal_requests WHERE renewal_id = 'SRR_20251022_00002';
-- Expected: status='DECLINED', decline_reason='Sẽ chuyển...'
```

---

### **Scenario 5: DECLINED without Reason (Error)**

**Steps**:

```json
{ "action": "DECLINED", "declineReason": null }
```

**Expected**:

- ✅ HTTP 400 Bad Request
- ✅ Error code: `REASON_REQUIRED`
- ✅ Message: "Vui lòng cung cấp lý do từ chối gia hạn (declineReason)"

---

### **Scenario 6: Not Owner Error**

**Given**: Renewal `SRR_20251022_00003` belongs to employee_id=10

**Steps**:

1. Login as employee_id=5
2. Try to respond to `SRR_20251022_00003`

**Expected**:

- ✅ HTTP 403 Forbidden
- ✅ Error code: `NOT_OWNER`
- ✅ Message: "Bạn không phải chủ sở hữu..."

---

### **Scenario 7: Double Respond (Already Confirmed)**

**Given**: Renewal already has `status = CONFIRMED`

**Steps**:

1. Try to respond again with `action = CONFIRMED`

**Expected**:

- ✅ HTTP 409 Conflict
- ✅ Error code: `INVALID_STATE`
- ✅ Message: "Yêu cầu đang ở trạng thái CONFIRMED (chỉ cho phép PENDING_ACTION)"

---

### **Scenario 8: Expired Renewal**

**Given**: Renewal has `expires_at = 2025-01-01T23:59:59` (past)

**Steps**:

1. Try to respond on 2025-01-02

**Expected**:

- ✅ HTTP 409 Conflict
- ✅ Error code: `REQUEST_EXPIRED`
- ✅ Message: "Yêu cầu gia hạn đã hết hạn vào 2025-01-01T23:59:59"

---

### **Scenario 9: Registration Already Inactive**

**Given**: Old registration (ID=123) has `is_active = FALSE` (đã bị deactivate bởi admin)

**Steps**:

1. Try to CONFIRM renewal

**Expected**:

- HTTP 409 Conflict
- Error code: `REGISTRATION_INACTIVE`
- Message: "Đăng ký ca ID 123 đã bị vô hiệu hóa"

---

### **Scenario 10: Admin Finalize - Invalid newEffectiveTo**

**Given**: Old registration `effective_to = 2025-12-31`

**Steps**:

1. Login as Admin
2. Call Finalize API with `newEffectiveTo = "2025-11-30"` (trước ngày cũ!)

**Expected**:

- HTTP 400 Bad Request
- Error code: `INVALID_EFFECTIVE_TO`
- Message: "Ngày kết thúc mới (2025-11-30) phải sau ngày kết thúc cũ (2025-12-31)"

---

### **Scenario 11: Admin Finalize - Not Confirmed Yet**

**Given**: Renewal has `status = PENDING_ACTION` (nhân viên chưa phản hồi)

**Steps**:

1. Admin tries to finalize

**Expected**:

- HTTP 409 Conflict
- Error code: `NOT_CONFIRMED_BY_EMPLOYEE`
- Message: "Nhân viên chưa xác nhận đồng ý. Yêu cầu đang ở trạng thái PENDING_ACTION (yêu cầu CONFIRMED)"

---

### **Scenario 12: Admin Finalize - Already Finalized (Idempotency Test)**

**Given**: Renewal already has `status = FINALIZED` (Admin đã xử lý rồi)

**Steps**:

1. Admin tries to finalize again

**Expected**:

- HTTP 409 Conflict
- Error code: `NOT_CONFIRMED_BY_EMPLOYEE`
- Message: "Yêu cầu đang ở trạng thái FINALIZED (yêu cầu CONFIRMED)"

---

### **Scenario 13: Concurrent Admin Finalize (Race Condition)**

**Given**: 2 Admin users cùng finalize một CONFIRMED renewal

**Steps**:

1. Admin A: Call POST /admin/renewals/finalize
2. Admin B: Call POST /admin/renewals/finalize (cùng lúc)

**Expected**:

- **One succeeds** (HTTP 200, tạo registration mới, status=FINALIZED)
- **One fails** (HTTP 409 NOT_CONFIRMED_BY_EMPLOYEE - đã FINALIZED)
- Chỉ **MỘT registration mới** được tạo (không duplicate)
- **PESSIMISTIC_WRITE** lock ngăn double-creation

**Verification**:

```sql
-- Only 1 new registration should exist
SELECT COUNT(*) FROM fixed_shift_registrations
WHERE employee_id = 5 AND is_active = TRUE AND effective_from = '2026-01-01';
-- Expected: 1 (not 2)

-- Only 1 renewal with FINALIZED status
SELECT status FROM shift_renewal_requests WHERE renewal_id = 'SRR_20251022_00001';
-- Expected: 'FINALIZED' (only one Admin succeeded)
```

---

## 📊 **Database Schema Reference**

### **shift_renewal_requests**

```sql
CREATE TABLE shift_renewal_requests (
    renewal_id VARCHAR(20) PRIMARY KEY,  -- SRR_YYYYMMDD_XXXXX
    expiring_registration_id INTEGER NOT NULL REFERENCES fixed_shift_registrations(registration_id),
    employee_id INTEGER NOT NULL REFERENCES employees(employee_id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING_ACTION', 'CONFIRMED', 'FINALIZED', 'DECLINED', 'EXPIRED')),
    expires_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    decline_reason TEXT,  -- Required when status=DECLINED
    created_at TIMESTAMP DEFAULT NOW(),
    INDEX idx_renewal_employee_status (employee_id, status),
    INDEX idx_renewal_expires_at (expires_at)
);
```

### **fixed_shift_registrations**

```sql
CREATE TABLE fixed_shift_registrations (
    registration_id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employees(employee_id),
    work_shift_id VARCHAR(20) NOT NULL REFERENCES work_shifts(work_shift_id),
    effective_from DATE NOT NULL,
    effective_to DATE,  -- NULL = permanent (FULL_TIME), set for PART_TIME_FIXED
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);
```

### **fixed_registration_days**

```sql
CREATE TABLE fixed_registration_days (
    registration_id INTEGER REFERENCES fixed_shift_registrations(registration_id),
    day_of_week VARCHAR(10) CHECK (day_of_week IN ('MONDAY', 'TUESDAY', ...)),
    PRIMARY KEY (registration_id, day_of_week)
);
```

---

## 🔍 **Verification Checklist**

### **Employee API (GET & PATCH /renewals)**

- [ ] GET /renewals/pending returns only PENDING_ACTION status
- [ ] GET filters out expired renewals (expires_at <= NOW())
- [ ] PATCH CONFIRMED updates status to CONFIRMED only (NO auto-extension)
- [ ] PATCH CONFIRMED does NOT create new registration
- [ ] PATCH CONFIRMED does NOT deactivate old registration
- [ ] PATCH CONFIRMED message: "Đang chờ Admin chốt thời hạn"
- [ ] PATCH DECLINED requires decline_reason (400 if missing)
- [ ] PATCH DECLINED does NOT modify old registration
- [ ] Ownership validation works (403 if wrong employee)
- [ ] State validation works (409 if already responded)
- [ ] Expiry validation works (409 if expired)

### **Admin API (POST /admin/renewals/finalize)**

- [ ] POST finalize requires status=CONFIRMED (409 if PENDING_ACTION/FINALIZED)
- [ ] POST finalize validates newEffectiveTo > old effective_to (400 if invalid)
- [ ] POST finalize creates new registration with admin-specified date
- [ ] POST finalize deactivates old registration (is_active=FALSE)
- [ ] POST finalize copies all fixed_registration_days
- [ ] POST finalize updates renewal status to FINALIZED
- [ ] POST finalize requires MANAGE_FIXED_REGISTRATIONS permission (403 if missing)
- [ ] Concurrent finalize race condition handled (PESSIMISTIC_WRITE lock prevents double-creation)
- [ ] Custom durations work (3-month, 1-year tested)

### **Database Integrity**

- [ ] shift_renewal_requests enum includes FINALIZED status
- [ ] Status transitions valid: PENDING_ACTION → CONFIRMED → FINALIZED
- [ ] Status transitions valid: PENDING_ACTION → DECLINED (terminal)
- [ ] Old registration remains active until Admin finalize (employee can continue working)
- [ ] New registration only created when status=FINALIZED (not CONFIRMED)
- [ ] Indexes on (employee_id, status) and (expires_at) perform well
- [ ] Permissions enforced (VIEW_RENEWAL_OWN, RESPOND_RENEWAL_OWN, MANAGE_FIXED_REGISTRATIONS)

---

## 📝 **Notes**

1. **Two-Step Process Logic**:

   - **Employee API**: CONFIRMED status chỉ cập nhật trạng thái (không tạo registration)
   - **Admin API**: FINALIZED status tạo registration mới với thời hạn admin chỉ định
   - **Lý do**: Hỗ trợ thời hạn gia hạn linh hoạt (3 tháng thử việc, 1 năm chuẩn, custom) cần phê duyệt quản lý

2. **Audit Trail**: Không bao giờ xóa registrations cũ, luôn set `is_active=FALSE` để lưu lịch sử

3. **Concurrency**:

   - Employee API: PESSIMISTIC_WRITE lock trên `shift_renewal_requests` ngăn double-response
   - Admin API: PESSIMISTIC_WRITE lock trên cả `shift_renewal_requests` và `fixed_shift_registrations` ngăn double-creation

4. **Validation Order**: Ownership → State → Expiry → Business logic (tránh database queries không cần thiết)

5. **Decline Reason**: Validated trong Service layer (không phải @Valid annotation) cho yêu cầu có điều kiện khi status=DECLINED

6. **Status Lifecycle**:

   - PENDING_ACTION → CONFIRMED → FINALIZED (happy path)
   - PENDING_ACTION → DECLINED (employee rejects)
   - PENDING_ACTION → EXPIRED (employee ignores)

7. **Backward Compatibility**: Employee API signature không đổi, chỉ behavior thay đổi (no breaking changes cho frontend)

---

## 🛠️ **Tools for Testing**

- **Postman**: Import collection with pre-configured requests
- **cURL**: Command-line testing
- **JUnit**: Automated integration tests (see `ShiftRenewalServiceTest.java`)
- **PostgreSQL Client**: Manual database verification

---

**Document Version**: 1.0
**Last Updated**: 2025-12-20
**Author**: AI Assistant
**Related**: BE-307, P7 Shift Renewal Management
