# Treatment Plan Detail API Documentation (API 5.2)

## Overview

API này cung cấp thông tin chi tiết đầy đủ của một lộ trình điều trị, bao gồm:

- ✅ **Metadata**: Plan code, tên, trạng thái, ngày tháng, tài chính
- ✅ **Participant Info**: Thông tin bác sĩ và bệnh nhân
- ✅ **Progress Summary**: Tóm tắt tiến độ (số phases/items đã hoàn thành)
- ✅ **Nested Structure**: Phases → Items → Linked Appointments

**Designed for:** Chi tiết checklist điều trị, theo dõi tiến độ, đặt lịch hẹn

## API Endpoint

### 5.2. Get Treatment Plan Detail (REVISED V18)

**Endpoint:** `GET /api/v1/patients/{patientCode}/treatment-plans/{planCode}`

**Required Permissions:**

- `VIEW_TREATMENT_PLAN_ALL` (Staff: Bác sĩ, Lễ tân, Quản lý) HOẶC
- `VIEW_TREATMENT_PLAN_OWN` (Bệnh nhân chỉ xem của mình)

**Path Parameters:**

- `patientCode` (string, required): Mã bệnh nhân (ví dụ: `BN-1001`)
- `planCode` (string, required): Mã lộ trình điều trị (ví dụ: `PLAN-20251001-001`)

## Key Features

| Feature                      | Description                                                         |
| ---------------------------- | ------------------------------------------------------------------- |
| **Single-query Performance** | JPQL với constructor expression JOIN 5 bảng                         |
| **Business Key URLs**        | Dùng `patientCode` và `planCode` thay vì database IDs               |
| **Nested Structure**         | Plan → Phases → Items → Appointments                                |
| **Progress Tracking**        | Tự động tính số phases/items hoàn thành                             |
| **RBAC Support**             | VIEW_ALL vs VIEW_OWN với account_id verification                    |
| **Multiple Appointments**    | Items hỗ trợ **array** of appointments (không phải single object)   |
| **Snapshot Pricing**         | Items lưu giá tại thời điểm tạo plan (không cập nhật theo bảng giá) |

## RBAC Logic

Giống như API 5.1, service thực hiện kiểm tra phân quyền chặt chẽ:

1. **Tìm Patient** bằng `patientCode`:

   - Nếu không tìm thấy → `404 NOT FOUND`

2. **Kiểm tra Permission**:

   - ✅ `VIEW_TREATMENT_PLAN_ALL`: Cho phép xem tất cả patients
   - ✅ `VIEW_TREATMENT_PLAN_OWN`: Verify `patient.account_id == token.account_id`
   - ❌ Không khớp account → `403 FORBIDDEN`

3. **Tìm Treatment Plan** bằng `planCode`:
   - Nếu không tìm thấy → `404 NOT FOUND` với message cụ thể

## Database Query (Performance Optimization)

### Single Query with Constructor Expression

Query JOIN 5 bảng trong một lần truy vấn:

```sql
SELECT new com.dental.clinic.management.treatment_plans.dto.TreatmentPlanDetailDTO(
    p.planId, p.planCode, p.planName, p.status, p.startDate, p.expectedEndDate,
    p.totalPrice, p.discountAmount, p.finalCost, p.paymentType, p.createdAt,
    emp.employeeCode, CONCAT(emp.firstName, ' ', emp.lastName),
    pat.patientCode, CONCAT(pat.firstName, ' ', pat.lastName),
    phase.patientPhaseId, phase.phaseNumber, phase.phaseName, phase.status,
    phase.startDate, phase.completionDate,
    item.itemId, item.serviceId, item.sequenceNumber, item.itemName, item.status,
    item.estimatedTimeMinutes, item.price, item.completedAt,
    apt.appointmentCode, apt.appointmentStartTime, apt.status
)
FROM PatientTreatmentPlan p
INNER JOIN p.createdBy emp
INNER JOIN p.patient pat
LEFT JOIN p.phases phase
LEFT JOIN phase.items item
LEFT JOIN AppointmentPlanItemBridge bridge ON bridge.id.itemId = item.itemId
LEFT JOIN Appointment apt ON apt.appointmentId = bridge.id.appointmentId
WHERE pat.patientCode = :patientCode AND p.planCode = :planCode
ORDER BY phase.phaseNumber, item.sequenceNumber
```

### Tables Involved

1. `patient_treatment_plans` (p) - Plan chính
2. `employees` (emp) - Bác sĩ tạo plan
3. `patients` (pat) - Bệnh nhân
4. `patient_plan_phases` (phase) - Các giai đoạn
5. `patient_plan_items` (item) - Các hạng mục checklist
6. `appointment_plan_items` (bridge) - Bridge table N-N
7. `appointments` (apt) - Lịch hẹn liên kết

### Query Result

- Trả về **List<TreatmentPlanDetailDTO>** (flat structure)
- Mỗi row = 1 item-appointment relationship
- Items không có appointment → appointment fields = null
- Service layer sẽ group O(n) thành nested structure

## Service Layer Processing

### Algorithm (O(n) Complexity)

```
1. Execute single JPQL query
   ↓
2. Receive List<TreatmentPlanDetailDTO> (flat)
   ↓
3. Group by phaseId → Map<Long, List<DTO>>
   ↓
4. For each phase:
   ├─ Extract phase metadata
   ├─ Group by itemId → Map<Long, List<DTO>>
   ├─ For each item:
   │  ├─ Extract item metadata
   │  └─ Collect appointments (filter nulls)
   └─ Build PhaseDetailDTO with items
   ↓
5. Calculate progressSummary:
   ├─ Count totalPhases, completedPhases
   └─ Count totalItems, completedItems, readyForBookingItems
   ↓
6. Build TreatmentPlanDetailResponse (nested)
```

### Grouping Logic

```java
// Step 1: Group by phase
Map<Long, List<TreatmentPlanDetailDTO>> rowsByPhase = flatDTOs.stream()
    .filter(dto -> dto.getPhaseId() != null)
    .collect(Collectors.groupingBy(TreatmentPlanDetailDTO::getPhaseId));

// Step 2: For each phase, group by item
Map<Long, List<TreatmentPlanDetailDTO>> rowsByItem = phaseRows.stream()
    .filter(dto -> dto.getItemId() != null)
    .collect(Collectors.groupingBy(TreatmentPlanDetailDTO::getItemId));

// Step 3: For each item, collect appointments
List<LinkedAppointmentDTO> appointments = itemRows.stream()
    .filter(dto -> dto.getAppointmentCode() != null)
    .map(dto -> LinkedAppointmentDTO.builder()
        .code(dto.getAppointmentCode())
        .scheduledDate(dto.getAppointmentScheduledDate())
        .status(dto.getAppointmentStatus())
        .build())
    .distinct()
    .collect(Collectors.toList());
```

## Response Structure

### Example Response (200 OK)

```json
{
  "planId": 1,
  "planCode": "PLAN-20251001-001",
  "planName": "Lộ trình Niềng răng Mắc cài Kim loại",
  "status": "IN_PROGRESS",
  "doctor": {
    "employeeCode": "EMP001",
    "fullName": "Lê Anh Khoa"
  },
  "patient": {
    "patientCode": "BN-1001",
    "fullName": "Đoàn Thanh Phong"
  },
  "startDate": "2025-10-01",
  "expectedEndDate": "2027-10-01",
  "totalPrice": 35000000,
  "discountAmount": 0,
  "finalCost": 35000000,
  "paymentType": "INSTALLMENT",
  "createdAt": "2025-10-01T08:00:00",
  "progressSummary": {
    "totalPhases": 3,
    "completedPhases": 1,
    "totalItems": 12,
    "completedItems": 4,
    "readyForBookingItems": 3
  },
  "phases": [
    {
      "phaseId": 1,
      "phaseNumber": 1,
      "phaseName": "Giai đoạn 1: Chuẩn bị",
      "status": "COMPLETED",
      "startDate": "2025-10-01",
      "completionDate": "2025-11-15",
      "items": [
        {
          "itemId": 1,
          "sequenceNumber": 1,
          "itemName": "Khám và chụp X-quang tổng quát",
          "serviceId": 101,
          "price": 500000,
          "estimatedTimeMinutes": 30,
          "status": "COMPLETED",
          "completedAt": "2025-10-05T10:00:00",
          "linkedAppointments": [
            {
              "code": "APT-20251005-001",
              "scheduledDate": "2025-10-05T10:00:00",
              "status": "COMPLETED"
            }
          ]
        },
        {
          "itemId": 2,
          "sequenceNumber": 2,
          "itemName": "Lấy dấu hàm và lập kế hoạch chi tiết",
          "serviceId": 102,
          "price": 800000,
          "estimatedTimeMinutes": 45,
          "status": "COMPLETED",
          "completedAt": "2025-10-12T14:00:00",
          "linkedAppointments": [
            {
              "code": "APT-20251012-002",
              "scheduledDate": "2025-10-12T14:00:00",
              "status": "COMPLETED"
            }
          ]
        },
        {
          "itemId": 3,
          "sequenceNumber": 3,
          "itemName": "Nhổ răng khôn (nếu cần)",
          "serviceId": 103,
          "price": 2000000,
          "estimatedTimeMinutes": 90,
          "status": "COMPLETED",
          "completedAt": "2025-10-25T09:00:00",
          "linkedAppointments": [
            {
              "code": "APT-20251025-003",
              "scheduledDate": "2025-10-25T09:00:00",
              "status": "COMPLETED"
            }
          ]
        },
        {
          "itemId": 4,
          "sequenceNumber": 4,
          "itemName": "Trám răng sâu",
          "serviceId": 104,
          "price": 1500000,
          "estimatedTimeMinutes": 60,
          "status": "COMPLETED",
          "completedAt": "2025-11-08T14:00:00",
          "linkedAppointments": [
            {
              "code": "APT-20251108-004",
              "scheduledDate": "2025-11-08T14:00:00",
              "status": "COMPLETED"
            }
          ]
        }
      ]
    },
    {
      "phaseId": 2,
      "phaseNumber": 2,
      "phaseName": "Giai đoạn 2: Niềng răng chủ động",
      "status": "IN_PROGRESS",
      "startDate": "2025-11-16",
      "completionDate": null,
      "items": [
        {
          "itemId": 5,
          "sequenceNumber": 1,
          "itemName": "Gắn mắc cài",
          "serviceId": 105,
          "price": 15000000,
          "estimatedTimeMinutes": 120,
          "status": "COMPLETED",
          "completedAt": "2025-11-20T09:00:00",
          "linkedAppointments": [
            {
              "code": "APT-20251120-005",
              "scheduledDate": "2025-11-20T09:00:00",
              "status": "COMPLETED"
            }
          ]
        },
        {
          "itemId": 6,
          "sequenceNumber": 2,
          "itemName": "Tái khám và điều chỉnh lần 1",
          "serviceId": 106,
          "price": 800000,
          "estimatedTimeMinutes": 30,
          "status": "READY_FOR_BOOKING",
          "completedAt": null,
          "linkedAppointments": []
        },
        {
          "itemId": 7,
          "sequenceNumber": 3,
          "itemName": "Tái khám và điều chỉnh lần 2",
          "serviceId": 106,
          "price": 800000,
          "estimatedTimeMinutes": 30,
          "status": "READY_FOR_BOOKING",
          "completedAt": null,
          "linkedAppointments": []
        },
        {
          "itemId": 8,
          "sequenceNumber": 4,
          "itemName": "Tái khám và điều chỉnh lần 3",
          "serviceId": 106,
          "price": 800000,
          "estimatedTimeMinutes": 30,
          "status": "READY_FOR_BOOKING",
          "completedAt": null,
          "linkedAppointments": []
        }
      ]
    },
    {
      "phaseId": 3,
      "phaseNumber": 3,
      "phaseName": "Giai đoạn 3: Duy trì kết quả",
      "status": "PENDING",
      "startDate": null,
      "completionDate": null,
      "items": [
        {
          "itemId": 11,
          "sequenceNumber": 1,
          "itemName": "Tháo mắc cài",
          "serviceId": 111,
          "price": 2000000,
          "estimatedTimeMinutes": 60,
          "status": "READY_FOR_BOOKING",
          "completedAt": null,
          "linkedAppointments": []
        },
        {
          "itemId": 12,
          "sequenceNumber": 2,
          "itemName": "Làm hàm duy trì (retainer)",
          "serviceId": 112,
          "price": 3000000,
          "estimatedTimeMinutes": 90,
          "status": "READY_FOR_BOOKING",
          "completedAt": null,
          "linkedAppointments": []
        }
      ]
    }
  ]
}
```

### Response Fields Reference

| Field Path                             | Type       | Description                                                             |
| -------------------------------------- | ---------- | ----------------------------------------------------------------------- |
| **Plan Level**                         |            |                                                                         |
| `planId`                               | Long       | Database ID                                                             |
| `planCode`                             | String     | Business key (unique, ví dụ: PLAN-20251001-001)                         |
| `planName`                             | String     | Tên lộ trình                                                            |
| `status`                               | String     | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`                      |
| `doctor`                               | Object     | Bác sĩ tạo plan                                                         |
| `doctor.employeeCode`                  | String     | Mã nhân viên                                                            |
| `doctor.fullName`                      | String     | Tên đầy đủ                                                              |
| `patient`                              | Object     | Bệnh nhân                                                               |
| `patient.patientCode`                  | String     | Mã bệnh nhân                                                            |
| `patient.fullName`                     | String     | Tên đầy đủ                                                              |
| `startDate`                            | Date       | Ngày bắt đầu (yyyy-MM-dd)                                               |
| `expectedEndDate`                      | Date       | Ngày dự kiến hoàn thành                                                 |
| `totalPrice`                           | BigDecimal | Tổng chi phí                                                            |
| `discountAmount`                       | BigDecimal | Số tiền giảm giá                                                        |
| `finalCost`                            | BigDecimal | Chi phí sau giảm giá                                                    |
| `paymentType`                          | String     | `FULL`, `PHASED`, `INSTALLMENT`                                         |
| `createdAt`                            | DateTime   | Thời gian tạo plan (yyyy-MM-dd'T'HH:mm:ss)                              |
| **Progress Summary**                   |            |                                                                         |
| `progressSummary`                      | Object     | Tóm tắt tiến độ                                                         |
| `progressSummary.totalPhases`          | Integer    | Tổng số giai đoạn                                                       |
| `progressSummary.completedPhases`      | Integer    | Số giai đoạn COMPLETED                                                  |
| `progressSummary.totalItems`           | Integer    | Tổng số hạng mục                                                        |
| `progressSummary.completedItems`       | Integer    | Số hạng mục COMPLETED                                                   |
| `progressSummary.readyForBookingItems` | Integer    | Số hạng mục READY_FOR_BOOKING                                           |
| **Phases Array**                       |            |                                                                         |
| `phases`                               | Array      | Danh sách giai đoạn (sorted by phaseNumber)                             |
| `phases[].phaseId`                     | Long       | Database ID                                                             |
| `phases[].phaseNumber`                 | Integer    | Số thứ tự (1, 2, 3...)                                                  |
| `phases[].phaseName`                   | String     | Tên giai đoạn                                                           |
| `phases[].status`                      | String     | `PENDING`, `IN_PROGRESS`, `COMPLETED`                                   |
| `phases[].startDate`                   | Date       | Ngày bắt đầu (nullable)                                                 |
| `phases[].completionDate`              | Date       | Ngày hoàn thành (nullable)                                              |
| **Items Array**                        |            |                                                                         |
| `phases[].items`                       | Array      | Hạng mục trong giai đoạn (sorted by sequenceNumber)                     |
| `items[].itemId`                       | Long       | Database ID                                                             |
| `items[].sequenceNumber`               | Integer    | Số thứ tự trong phase (1, 2, 3...)                                      |
| `items[].itemName`                     | String     | Tên hạng mục                                                            |
| `items[].serviceId`                    | Integer    | ID dịch vụ liên kết                                                     |
| `items[].price`                        | BigDecimal | **Snapshot price** (giá tại thời điểm tạo plan)                         |
| `items[].estimatedTimeMinutes`         | Integer    | Thời gian dự kiến (phút)                                                |
| `items[].status`                       | String     | `READY_FOR_BOOKING`, `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `SKIPPED` |
| `items[].completedAt`                  | DateTime   | Thời gian hoàn thành (nullable)                                         |
| **Linked Appointments Array**          |            |                                                                         |
| `items[].linkedAppointments`           | Array      | **Danh sách** lịch hẹn liên kết (có thể empty [])                       |
| `linkedAppointments[].code`            | String     | Mã lịch hẹn (ví dụ: APT-20251005-001)                                   |
| `linkedAppointments[].scheduledDate`   | DateTime   | Thời gian hẹn                                                           |
| `linkedAppointments[].status`          | String     | Trạng thái lịch hẹn                                                     |

### Important Notes về Response

✅ **Items có thể có nhiều appointments** (array, không phải single object)

- Ví dụ: Item "Trám răng" có thể liên kết 2 lịch hẹn (hẹn lần 1 không xong, phải hẹn lần 2)

✅ **Items chưa đặt lịch có empty array**

- `linkedAppointments: []` (không phải null)

✅ **Phases và Items được sort**

- Phases: `phaseNumber` ASC
- Items: `sequenceNumber` ASC

✅ **Giá trong items là snapshot price**

- Không cập nhật theo bảng giá hiện tại
- Lưu giá tại thời điểm tạo plan

❌ **Không có quantity/unitPrice fields**

- Chỉ có `price` (snapshot)
- Đã bỏ thiết kế cũ với quantity × unitPrice

## Error Responses

### 404 Not Found - Patient not found

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with code: BN-9999",
  "path": "/api/v1/patients/BN-9999/treatment-plans/PLAN-20251001-001"
}
```

### 404 Not Found - Treatment plan not found

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Treatment plan 'PLAN-99999999-999' not found for patient 'BN-1001'",
  "path": "/api/v1/patients/BN-1001/treatment-plans/PLAN-99999999-999"
}
```

### 403 Forbidden - Access denied

```json
{
  "timestamp": "2025-11-10T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You can only view your own treatment plans",
  "path": "/api/v1/patients/BN-1002/treatment-plans/PLAN-20240515-001"
}
```

## Testing Guide

### Test Users (from Seed Data)

**Password:** All users have password `123456`

| Username    | Role              | Employee/Patient Code | Account ID | Permissions             |
| ----------- | ----------------- | --------------------- | ---------- | ----------------------- |
| `bacsi1`    | ROLE_DENTIST      | EMP001                | -          | VIEW_TREATMENT_PLAN_ALL |
| `bacsi2`    | ROLE_DENTIST      | EMP002                | -          | VIEW_TREATMENT_PLAN_ALL |
| `letan1`    | ROLE_RECEPTIONIST | EMP005                | -          | VIEW_TREATMENT_PLAN_ALL |
| `benhnhan1` | ROLE_PATIENT      | BN-1001               | 12         | VIEW_TREATMENT_PLAN_OWN |
| `benhnhan2` | ROLE_PATIENT      | BN-1002               | 13         | VIEW_TREATMENT_PLAN_OWN |
| `benhnhan3` | ROLE_PATIENT      | BN-1003               | 14         | VIEW_TREATMENT_PLAN_OWN |

### Test Data (Seed Data)

| Plan Code         | Patient | Status      | Phases | Items | Completed Items |
| ----------------- | ------- | ----------- | ------ | ----- | --------------- |
| PLAN-20251001-001 | BN-1001 | IN_PROGRESS | 3      | 12    | 4               |
| PLAN-20240515-001 | BN-1002 | COMPLETED   | 3      | 8     | 8               |
| PLAN-20251105-001 | BN-1003 | PENDING     | 0      | 0     | 0               |

### Step 1: Login to Get Token

```bash
# Login as Doctor
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "bacsi1", "password": "123456"}'

# Save access_token from response
```

### Test Case 1: Doctor Views Plan Detail (SUCCESS) ✅

**User:** `bacsi1` (Has VIEW_TREATMENT_PLAN_ALL)
**Target:** Plan PLAN-20251001-001 of patient BN-1001

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
  -H "Authorization: Bearer {DOCTOR_TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected Result:**

- ✅ `200 OK`
- ✅ Full nested structure với 3 phases
- ✅ Phase 1 (COMPLETED): 4 items đã hoàn thành, có linkedAppointments
- ✅ Phase 2 (IN_PROGRESS): 1 item completed, 3 items READY_FOR_BOOKING (linkedAppointments: [])
- ✅ Phase 3 (PENDING): 2 items READY_FOR_BOOKING, startDate và completionDate null
- ✅ progressSummary: `totalPhases: 3, completedPhases: 1, totalItems: 12, completedItems: 4, readyForBookingItems: 3`

**Verify Points:**

```bash
# Check response structure
jq '.phases | length' # Should be 3
jq '.progressSummary.completedPhases' # Should be 1
jq '.progressSummary.totalItems' # Should be 12
jq '.phases[0].items | length' # Phase 1 should have 4 items
jq '.phases[0].items[0].linkedAppointments | length' # Should have 1 appointment
jq '.phases[1].items[1].linkedAppointments | length' # Should be 0 (empty array)
```

### Test Case 2: Patient Views Own Plan (SUCCESS) ✅

**User:** `benhnhan1` (BN-1001, account_id=12, Has VIEW_TREATMENT_PLAN_OWN)
**Target:** Their own plan PLAN-20251001-001

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
  -H "Authorization: Bearer {PATIENT_TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected Result:**

- ✅ `200 OK` - Same response as Test Case 1
- ✅ Patient can view their own plan details

### Test Case 3: Receptionist Views Plan (SUCCESS) ✅

**User:** `letan1` (Has VIEW_TREATMENT_PLAN_ALL)
**Target:** Plan PLAN-20240515-001 of patient BN-1002 (Completed implant)

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans/PLAN-20240515-001" \
  -H "Authorization: Bearer {RECEPTIONIST_TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected Result:**

- ✅ `200 OK`
- ✅ `status: "COMPLETED"`
- ✅ All 8 items có status "COMPLETED"
- ✅ All items có `completedAt` timestamp
- ✅ `progressSummary.completedItems: 8`

### Test Case 4: Patient Tries to View Another Patient's Plan (FORBIDDEN) ❌

**User:** `benhnhan1` (BN-1001, account_id=12)
**Target:** BN-1002's plan (account_id=13) - Different account!

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans/PLAN-20240515-001" \
  -H "Authorization: Bearer {PATIENT_BN1001_TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected Result:**

- ❌ `403 FORBIDDEN`
- Error message: "You can only view your own treatment plans"

### Test Case 5: View Pending Plan (No Phases Yet) ✅

**User:** `bacsi1` (Doctor)
**Target:** PLAN-20251105-001 (BN-1003, status PENDING, no phases created)

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1003/treatment-plans/PLAN-20251105-001" \
  -H "Authorization: Bearer {DOCTOR_TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected Result:**

- ✅ `200 OK`
- ✅ `status: "PENDING"`
- ✅ `phases: []` (empty array, not null)
- ✅ `progressSummary.totalPhases: 0`
- ✅ `progressSummary.totalItems: 0`

### Test Case 6: Invalid Plan Code (NOT FOUND) ❌

**User:** `bacsi1` (Doctor)

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-99999999-999" \
  -H "Authorization: Bearer {DOCTOR_TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected Result:**

- ❌ `404 NOT FOUND`
- Error message: "Treatment plan 'PLAN-99999999-999' not found for patient 'BN-1001'"

### Test Case 7: Invalid Patient Code (NOT FOUND) ❌

**User:** `bacsi1` (Doctor)

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-9999/treatment-plans/PLAN-20251001-001" \
  -H "Authorization: Bearer {DOCTOR_TOKEN}" \
  -H "Content-Type: application/json"
```

**Expected Result:**

- ❌ `404 NOT FOUND`
- Error message: "Patient not found with code: BN-9999"

### Test Case 8: Unauthenticated Request (UNAUTHORIZED) ❌

```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
  -H "Content-Type: application/json"
```

**Expected Result:**

- ❌ `401 UNAUTHORIZED`

## Performance Testing

### Query Performance Benchmark

```bash
# Measure API response time
time curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -o /dev/null -s -w "Time: %{time_total}s\n"
```

**Expected Performance:**

- ✅ Single database query (check logs for JPQL execution)
- ✅ Response time < 100ms for plan with:
  - 3 phases
  - 12 items
  - 4 linked appointments
- ✅ No N+1 query problem
- ✅ O(n) grouping complexity in service layer

### Database Query Count

```yaml
# Enable query logging in application.yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

**Expected Log Output:**

```
# Should see ONLY 1 query executing:
Hibernate:
    SELECT new com.dental.clinic.management.treatment_plans.dto.TreatmentPlanDetailDTO(...)
    FROM PatientTreatmentPlan p
    INNER JOIN p.createdBy emp
    INNER JOIN p.patient pat
    LEFT JOIN p.phases phase
    LEFT JOIN phase.items item
    LEFT JOIN AppointmentPlanItemBridge bridge ON ...
    LEFT JOIN Appointment apt ON ...
    WHERE pat.patientCode = ? AND p.planCode = ?
    ORDER BY phase.phaseNumber, item.sequenceNumber
```

## Implementation Architecture

### Layer Breakdown

```
┌─────────────────────────────────────────┐
│   Controller                            │
│   TreatmentPlanController.java          │
│                                         │
│   GET /patients/{code}/plans/{code}    │
└────────────────┬────────────────────────┘
                 │
                 ↓
┌─────────────────────────────────────────┐
│   Service                               │
│   TreatmentPlanDetailService.java       │
│                                         │
│   1. verifyPatientAccessPermission()    │
│      - RBAC check                       │
│      - VIEW_ALL vs VIEW_OWN             │
│                                         │
│   2. Repository.findDetail...()         │
│      - Single JPQL query                │
│                                         │
│   3. buildNestedResponse()              │
│      - Group by phaseId (O(n))          │
│      - Group by itemId (O(n))           │
│      - Collect appointments             │
│                                         │
│   4. calculateProgressSummary()         │
│      - Count by status                  │
└────────────────┬────────────────────────┘
                 │
                 ↓
┌─────────────────────────────────────────┐
│   Repository                            │
│   PatientTreatmentPlanRepository.java   │
│                                         │
│   @Query with Constructor Expression    │
│   - JOIN 5 tables                       │
│   - Return List<FlatDTO>                │
└────────────────┬────────────────────────┘
                 │
                 ↓
┌─────────────────────────────────────────┐
│   DTOs                                  │
│                                         │
│   TreatmentPlanDetailDTO (Flat)         │
│   ├─ For JPQL constructor expression    │
│   └─ 31 fields from 5 tables            │
│                                         │
│   TreatmentPlanDetailResponse (Nested)  │
│   ├─ PhaseDetailDTO                     │
│   │  └─ ItemDetailDTO                   │
│   │     └─ LinkedAppointmentDTO         │
│   └─ ProgressSummaryDTO                 │
└─────────────────────────────────────────┘
```

### Database Indexes (Recommended)

```sql
-- For patient lookup by business key
CREATE INDEX idx_patients_patient_code ON patients(patient_code);

-- For plan lookup by business key
CREATE INDEX idx_plans_plan_code ON patient_treatment_plans(plan_code);

-- For appointment-item bridge join
CREATE INDEX idx_bridge_item_id ON appointment_plan_items(item_id);
CREATE INDEX idx_bridge_appointment_id ON appointment_plan_items(appointment_id);

-- For phase ordering
CREATE INDEX idx_phases_plan_id ON patient_plan_phases(patient_plan_id);

-- For item ordering
CREATE INDEX idx_items_phase_id ON patient_plan_items(patient_phase_id);
```

## Business Rules

### Item Status Lifecycle

```
READY_FOR_BOOKING → SCHEDULED → IN_PROGRESS → COMPLETED
                                              ↘ SKIPPED
```

- **READY_FOR_BOOKING**: Item sẵn sàng để đặt lịch hẹn
- **SCHEDULED**: Đã có lịch hẹn linked (có thể nhiều appointments)
- **IN_PROGRESS**: Đang thực hiện trong appointment
- **COMPLETED**: Đã hoàn thành (có `completedAt` timestamp)
- **SKIPPED**: Bỏ qua (ví dụ: không cần nhổ răng khôn)

### Phase Status Auto-Update Rules

Phase status được tính dựa trên items:

- **PENDING**: Chưa có item nào COMPLETED
- **IN_PROGRESS**: Có ít nhất 1 item COMPLETED, nhưng chưa hoàn thành hết
- **COMPLETED**: Tất cả items COMPLETED hoặc SKIPPED

### Plan Status Auto-Update Rules

Plan status được tính dựa trên phases:

- **PENDING**: Chưa bắt đầu (no phases hoặc all phases PENDING)
- **IN_PROGRESS**: Có ít nhất 1 phase IN_PROGRESS hoặc COMPLETED
- **COMPLETED**: Tất cả phases COMPLETED
- **CANCELLED**: Hủy bỏ plan (manual action)

## Security Best Practices

1. ✅ **Business key URLs**: Không expose database IDs
2. ✅ **RBAC verification**: Kiểm tra permissions before query
3. ✅ **Account verification**: VIEW_OWN requires account_id match
4. ✅ **Detailed error messages**: "Plan X not found for patient Y" (clear context)
5. ✅ **403 vs 404**: Return 403 for access denied (không phải 404)
6. ✅ **Audit logging**: Log all access attempts with user info

## Common Issues & Troubleshooting

### Issue 1: Empty linkedAppointments array

**Symptom:** Items có `linkedAppointments: []` dù đã có appointment

**Possible Causes:**

1. Bridge table `appointment_plan_items` chưa có record
2. Appointment status = CANCELLED (filter in query?)
3. Item chưa được link với appointment

**Solution:** Check bridge table:

```sql
SELECT * FROM appointment_plan_items WHERE item_id = {itemId};
```

### Issue 2: Performance slow với large plan

**Symptom:** Response time > 500ms

**Possible Causes:**

1. Missing indexes on business key columns
2. Too many phases/items (> 100)
3. N+1 query problem (check Hibernate logs)

**Solution:**

- Add indexes: `patient_code`, `plan_code`
- Enable query logging, verify only 1 query
- Consider pagination for large plans

### Issue 3: progressSummary không chính xác

**Symptom:** Số lượng completedItems không khớp với UI

**Possible Causes:**

1. Status chưa cập nhật trong DB
2. Grouping logic bug (duplicate items)
3. Filtering logic sai (distinct không hoạt động)

**Solution:**

- Query DB directly: `SELECT status, COUNT(*) FROM patient_plan_items WHERE ... GROUP BY status`
- Check service layer grouping logic
- Verify `completedAt` timestamp có set đúng không

## Future Enhancements

- [ ] Add caching layer (Redis) cho frequently accessed plans
- [ ] WebSocket notifications khi plan status changes
- [ ] Export plan to PDF (với progress chart)
- [ ] Timeline view (Gantt chart) cho phases
- [ ] Drag-and-drop reorder items trong phase
- [ ] Bulk update items status
- [ ] Plan comparison (actual vs estimated timeline)

## Related APIs

- **API 5.1**: `GET /api/v1/patients/{patientCode}/treatment-plans` - List all plans (summary)
- **API 5.3**: `POST /api/v1/patients/{patientCode}/treatment-plans` - Create new plan
- **API 5.4**: `PATCH /api/v1/patients/{patientCode}/treatment-plans/{planCode}` - Update plan
- **Appointment API**: Link items to appointments via bridge table

## Revision History

| Version | Date       | Author | Changes                                                   |
| ------- | ---------- | ------ | --------------------------------------------------------- |
| V18     | 2025-11-11 | Team   | ✅ REVISED API SPEC - Business key URLs, nested structure |
| V17     | 2025-11-10 | Team   | Added progressSummary, linkedAppointments array           |
| V16     | 2025-11-09 | Team   | Initial API design with database IDs (deprecated)         |
