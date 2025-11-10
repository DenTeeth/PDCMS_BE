# Treatment Plan API Documentation

## Overview

Module này quản lý các "hợp đồng" điều trị dài hạn của bệnh nhân (ví dụ: niềng răng, implant). Cho phép Bác sĩ tạo, xem, và theo dõi tiến độ của các Lộ trình điều trị.

## Permissions

| Permission                | Role                    | Description                                                |
| ------------------------- | ----------------------- | ---------------------------------------------------------- |
| `VIEW_TREATMENT_PLAN_ALL` | Lễ tân, Bác sĩ, Quản lý | Xem tất cả lộ trình của mọi bệnh nhân                      |
| `VIEW_TREATMENT_PLAN_OWN` | Bệnh nhân               | Chỉ xem lộ trình của chính mình                            |
| `CREATE_TREATMENT_PLAN`   | Bác sĩ                  | Tạo lộ trình điều trị mới                                  |
| `UPDATE_TREATMENT_PLAN`   | Bác sĩ                  | Cập nhật lộ trình (thêm/bớt hạng mục, cập nhật trạng thái) |

## Database Tables

- `patient_treatment_plans` - Bảng "Hợp đồng" chính
- `patient_plan_phases` - Các giai đoạn của hợp đồng
- `patient_plan_items` - Các hạng mục công việc/checklist
- `treatment_plan_templates` - Đọc dữ liệu từ các "gói" mẫu
- `employees` - Lấy thông tin Bác sĩ phụ trách
- `patients` - Lấy thông tin Bệnh nhân

## API Endpoints

### 5.1. Get Treatment Plans by Patient

Lấy danh sách (dạng tóm tắt) tất cả các lộ trình điều trị (hợp đồng) của một bệnh nhân cụ thể.

**Endpoint:** `GET /api/v1/patients/{patientCode}/treatment-plans`

**Required Permissions:**

- `VIEW_TREATMENT_PLAN_ALL` (cho nhân viên) HOẶC
- `VIEW_TREATMENT_PLAN_OWN` (cho bệnh nhân)

**Path Parameters:**

- `patientCode` (string, required): Mã bệnh nhân (ví dụ: `PT-2025-001`)

**RBAC Logic:**

1. Tìm patient trong DB bằng `patientCode`
   - Nếu không thấy → `404 NOT FOUND`
2. Thực hiện kiểm tra RBAC:
   - Nếu User có quyền `VIEW_TREATMENT_PLAN_ALL` → Cho phép
   - Nếu User chỉ có quyền `VIEW_TREATMENT_PLAN_OWN`:
     - Kiểm tra `patient.account_id` có khớp với `account_id` trong Token không
     - Nếu không khớp → `403 FORBIDDEN`
3. Truy vấn `patient_treatment_plans` với `JOIN FETCH employees` (tránh N+1)

**Query Optimization:**

```sql
SELECT DISTINCT p FROM PatientTreatmentPlan p
LEFT JOIN FETCH p.createdBy
WHERE p.patient.patientId = :patientId
ORDER BY p.createdAt DESC
```

**Response (200 OK):**

```json
[
  {
    "patientPlanId": 101,
    "planName": "Lộ trình Niềng răng Mắc cài Kim loại",
    "status": "IN_PROGRESS",
    "doctor": {
      "employeeCode": "DR_AN_KHOA",
      "fullName": "Dr. Le An Khoa"
    },
    "startDate": "2025-10-01",
    "expectedEndDate": "2027-10-01",
    "totalCost": 35000000,
    "discountAmount": 0,
    "finalCost": 35000000,
    "paymentType": "INSTALLMENT"
  },
  {
    "patientPlanId": 102,
    "planName": "Lộ trình Implant 2 răng cửa",
    "status": "COMPLETED",
    "doctor": {
      "employeeCode": "DR_BINH_AN",
      "fullName": "Dr. Tran Binh An"
    },
    "startDate": "2024-05-15",
    "expectedEndDate": "2024-08-20",
    "totalCost": 40000000,
    "discountAmount": 5000000,
    "finalCost": 35000000,
    "paymentType": "FULL"
  }
]
```

**Response Fields:**

| Field                 | Type       | Description                                                    |
| --------------------- | ---------- | -------------------------------------------------------------- |
| `patientPlanId`       | Long       | ID của lộ trình điều trị                                       |
| `planName`            | String     | Tên lộ trình                                                   |
| `status`              | Enum       | Trạng thái: `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `doctor`              | Object     | Thông tin bác sĩ phụ trách                                     |
| `doctor.employeeCode` | String     | Mã nhân viên của bác sĩ                                        |
| `doctor.fullName`     | String     | Tên đầy đủ của bác sĩ                                          |
| `startDate`           | Date       | Ngày bắt đầu (yyyy-MM-dd)                                      |
| `expectedEndDate`     | Date       | Ngày dự kiến kết thúc                                          |
| `totalCost`           | BigDecimal | Tổng chi phí trước giảm giá                                    |
| `discountAmount`      | BigDecimal | Số tiền giảm giá                                               |
| `finalCost`           | BigDecimal | Chi phí sau giảm giá                                           |
| `paymentType`         | Enum       | Hình thức thanh toán: `FULL`, `PHASED`, `INSTALLMENT`          |

**Error Responses:**

**404 Not Found** - Patient not found

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with code: PT-2025-999",
  "path": "/api/v1/patients/PT-2025-999/treatment-plans"
}
```

**403 Forbidden** - Access denied

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You can only view your own treatment plans",
  "path": "/api/v1/patients/PT-2025-001/treatment-plans"
}
```

## Testing Guide

### Test Users (from Seed Data)

**Important:** All test user passwords are: `123456`

#### Staff Users (VIEW_TREATMENT_PLAN_ALL)

| Username | Role              | Employee Code | Full Name       | Permissions                      |
| -------- | ----------------- | ------------- | --------------- | -------------------------------- |
| `bacsi1` | ROLE_DENTIST      | EMP001        | Lê Anh Khoa     | VIEW_ALL, CREATE, UPDATE, DELETE |
| `bacsi2` | ROLE_DENTIST      | EMP002        | Trịnh Công Thái | VIEW_ALL, CREATE, UPDATE, DELETE |
| `letan1` | ROLE_RECEPTIONIST | EMP005        | Đỗ Khánh Thuận  | VIEW_ALL (read-only)             |

#### Patient Users (VIEW_TREATMENT_PLAN_OWN)

| Username    | Patient Code | Account ID | Full Name        | Permissions   |
| ----------- | ------------ | ---------- | ---------------- | ------------- |
| `benhnhan1` | BN-1001      | 12         | Đoàn Thanh Phong | VIEW_OWN only |
| `benhnhan2` | BN-1002      | 13         | Phạm Văn Phong   | VIEW_OWN only |
| `benhnhan3` | BN-1003      | 14         | Nguyễn Tuấn Anh  | VIEW_OWN only |
| `benhnhan4` | BN-1004      | 15         | Mít tơ Bít       | VIEW_OWN only |

### Step 1: Login to Get Token

```bash
# Login as Doctor (has VIEW_TREATMENT_PLAN_ALL)
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bacsi1",
    "password": "123456"
  }'

# Save the access_token from response
# Example response:
# {
#   "access_token": "eyJhbGciOiJIUzI1NiIsInR...",
#   "token_type": "Bearer",
#   "expires_in": 86400
# }
```

```bash
# Login as Patient (has VIEW_TREATMENT_PLAN_OWN)
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "benhnhan1",
    "password": "123456"
  }'
```

```bash
# Login as Receptionist (has VIEW_TREATMENT_PLAN_ALL, read-only)
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "letan1",
    "password": "123456"
  }'
```

### Test Case 1: Doctor Views All Patient Plans (SUCCESS)

**User:** `bacsi1` (Lê Anh Khoa) - ROLE_DENTIST
**Permission:** `VIEW_TREATMENT_PLAN_ALL`
**Target:** View plans of patient BN-1001

```bash
# Replace {TOKEN} with access_token from login response
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected:** `200 OK` with list of all treatment plans for patient BN-1001

### Test Case 2: Receptionist Views All Patient Plans (SUCCESS)

**User:** `letan1` (Đỗ Khánh Thuận) - ROLE_RECEPTIONIST
**Permission:** `VIEW_TREATMENT_PLAN_ALL`
**Target:** View plans of patient BN-1002

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected:** `200 OK` with list of all treatment plans for patient BN-1002

### Test Case 3: Patient Views Own Plans (SUCCESS)

**User:** `benhnhan1` (Đoàn Thanh Phong) - ROLE_PATIENT
**Permission:** `VIEW_TREATMENT_PLAN_OWN`
**Patient Code:** BN-1001 (linked to account_id = 12)
**Target:** View their own plans

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected:** `200 OK` with their own treatment plans (account_id verification passes)

### Test Case 4: Patient Tries to View Another Patient's Plans (FORBIDDEN)

**User:** `benhnhan1` (Đoàn Thanh Phong) - ROLE_PATIENT
**Permission:** `VIEW_TREATMENT_PLAN_OWN`
**Patient Code:** BN-1001 (linked to account_id = 12)
**Target:** Attempt to view BN-1002's plans (account_id = 13)

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected:** `403 FORBIDDEN` with error message:

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You can only view your own treatment plans",
  "path": "/api/v1/patients/BN-1002/treatment-plans"
}
```

### Test Case 5: Patient Without Account Views Plans (FORBIDDEN)

**User:** `benhnhan1` (Đoàn Thanh Phong) - ROLE_PATIENT
**Target:** Patient BN-1005 (exists in DB but no account linked)

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1005/treatment-plans" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected:** `403 FORBIDDEN` (patient has no account_id, cannot match with token)

### Test Case 6: Invalid Patient Code (NOT FOUND)

**User:** `bacsi1` (Doctor with VIEW_TREATMENT_PLAN_ALL)

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-9999/treatment-plans" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected:** `404 NOT FOUND` with error message:

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with code: BN-9999",
  "path": "/api/v1/patients/BN-9999/treatment-plans"
}
```

### Test Case 7: Unauthenticated Request (UNAUTHORIZED)

**User:** None (no token provided)

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Content-Type: application/json"
```

**Expected:** `401 UNAUTHORIZED`

## Implementation Notes

### N+1 Query Prevention

The repository uses `JOIN FETCH` to eagerly load doctor information in a single query:

```java
@Query("SELECT DISTINCT p FROM PatientTreatmentPlan p " +
       "LEFT JOIN FETCH p.createdBy " +
       "WHERE p.patient.patientId = :patientId " +
       "ORDER BY p.createdAt DESC")
List<PatientTreatmentPlan> findByPatientIdWithDoctor(@Param("patientId") Integer patientId);
```

### RBAC Implementation

The service layer implements fine-grained access control:

- Checks for `VIEW_TREATMENT_PLAN_ALL` permission (allows all access)
- Falls back to `VIEW_TREATMENT_PLAN_OWN` (requires account ownership verification)
- Extracts `account_id` from JWT token for comparison

### Security Best Practices

1. Always use path parameter `patientCode` (not query param) for RESTful design
2. Verify token `account_id` matches patient `account_id` for own-view access
3. Return `403 FORBIDDEN` (not 404) when access denied to prevent information disclosure
4. Log all access attempts for audit trail

## Status Lifecycle

### Treatment Plan Status Flow

```
PENDING → IN_PROGRESS → COMPLETED
                      ↘ CANCELLED
```

### Phase Status Flow

```
PENDING → IN_PROGRESS → COMPLETED
```

### Item Status Flow

```
READY_FOR_BOOKING → SCHEDULED → IN_PROGRESS → COMPLETED
```

## Future Enhancements

- Add pagination support for large lists
- Add filtering by status, date range
- Add sorting options
- Add detailed plan view endpoint (5.2)
- Add create plan endpoint (5.3)
- Add update plan endpoint (5.4)
