# BE/ #5 - TREATMENT PLAN MODULE

> **Module**: Treatment Plan Management
> **Version**: V1.0
> **Author**: Dental Clinic Development Team
> **Last Updated**: November 7, 2025

---

## 📋 TABLE OF CONTENTS

1. [Module Overview](#1-module-overview)
2. [Database Schema](#2-database-schema)
3. [Business Logic](#3-business-logic)
4. [API Specifications](#4-api-specifications)
5. [Integration with Booking Module](#5-integration-with-booking-module)
6. [Sample Data](#6-sample-data)
7. [Testing Guide](#7-testing-guide)

---

## 1. MODULE OVERVIEW

### 1.1 Purpose

Treatment Plan Module quản lý **Gói điều trị (Treatment Plan Templates)** và **Lộ trình điều trị bệnh nhân (Patient Treatment Plans)**.

**Business Context:**

- **Doctors** tạo template cho các gói điều trị phổ biến (VD: Niềng răng 2 năm, Implant trọn gói)
- **Receptionists** áp dụng template cho bệnh nhân → tạo Patient Treatment Plan
- **Patients** được theo dõi tiến độ điều trị qua các giai đoạn (Phases)
- **Appointments** được đặt lịch dựa trên các hạng mục công việc (Patient Plan Items)

### 1.2 Key Features

✅ **Template Management**: CRUD operations cho Treatment Plan Templates
✅ **Patient Plan Assignment**: Áp dụng template cho bệnh nhân cụ thể
✅ **Status Tracking**: Theo dõi trạng thái từng hạng mục (READY_FOR_BOOKING → SCHEDULED → IN_PROGRESS → COMPLETED)
✅ **Appointment Integration**: Đặt lịch trực tiếp từ Patient Plan Items
✅ **Progress Monitoring**: Tính % hoàn thành của mỗi giai đoạn và toàn bộ plan

### 1.3 Scope Separation

| Module                      | Scope                                           | APIs               |
| --------------------------- | ----------------------------------------------- | ------------------ |
| **BE/ #5 (Treatment Plan)** | Template CRUD, Patient Plan CRUD, GET endpoints | API 5.1 - 5.X      |
| **BE/ #4 (Booking)**        | Appointment creation with Treatment Plan items  | API 3.2 (upgraded) |

**⚠️ Important**: API 3.2 (Create Appointment) belongs to Booking Module but supports Treatment Plan integration.

---

## 2. DATABASE SCHEMA

### 2.1 Entity Relationship Diagram

```
treatment_plan_templates (Gói điều trị mẫu)
├── template_phases (Giai đoạn trong gói)
│   └── template_phase_services (Dịch vụ trong từng giai đoạn)
│
patient_treatment_plans (Hợp đồng điều trị bệnh nhân)
├── patient_plan_phases (Giai đoạn thực tế của bệnh nhân)
│   └── patient_plan_items (Hạng mục công việc)
│       └── appointment_plan_items (N-N with appointments)
```

### 2.2 Core Tables

#### 2.2.1 `treatment_plan_templates`

**Purpose**: Gói điều trị mẫu do bác sĩ định nghĩa sẵn (VD: Niềng răng 2 năm)

| Column                    | Type               | Description                                                |
| ------------------------- | ------------------ | ---------------------------------------------------------- |
| `template_id`             | BIGSERIAL PK       | Unique identifier                                          |
| `template_code`           | VARCHAR(50) UNIQUE | Business code (VD: TPL_ORTHO_METAL)                        |
| `template_name`           | VARCHAR(255)       | Tên gói (VD: "Niềng răng mắc cài kim loại trọn gói 2 năm") |
| `description`             | TEXT               | Mô tả chi tiết                                             |
| `estimated_duration_days` | INTEGER            | Thời gian điều trị (VD: 730 ngày = 2 năm)                  |
| `total_price`             | NUMERIC(12,2)      | Giá trọn gói (VD: 30.000.000 VND)                          |
| `is_active`               | BOOLEAN            | Soft delete flag                                           |
| `created_at`              | TIMESTAMP          | Timestamp                                                  |

**Indexes**: `template_code` (UNIQUE), `is_active` (for filtering)

#### 2.2.2 `template_phases`

**Purpose**: Các giai đoạn trong template (VD: Giai đoạn 1: Khám & Chuẩn bị)

| Column                    | Type         | Description                   |
| ------------------------- | ------------ | ----------------------------- |
| `phase_id`                | BIGSERIAL PK | Unique identifier             |
| `template_id`             | BIGINT FK    | Reference to template         |
| `phase_number`            | INTEGER      | Thứ tự giai đoạn (1, 2, 3...) |
| `phase_name`              | VARCHAR(255) | Tên giai đoạn                 |
| `estimated_duration_days` | INTEGER      | Thời gian dự kiến             |
| `created_at`              | TIMESTAMP    | Timestamp                     |

**Composite Unique**: (`template_id`, `phase_number`)

#### 2.2.3 `template_phase_services`

**Purpose**: Dịch vụ trong từng giai đoạn (VD: Giai đoạn 3 có 24 lần "Siết niềng")

| Column                   | Type         | Description                 |
| ------------------------ | ------------ | --------------------------- |
| `phase_service_id`       | BIGSERIAL PK | Unique identifier           |
| `phase_id`               | BIGINT FK    | Reference to phase          |
| `service_id`             | INTEGER FK   | Reference to services table |
| `quantity`               | INTEGER      | Số lần thực hiện (VD: 24)   |
| `estimated_time_minutes` | INTEGER      | Thời gian mỗi lần           |
| `created_at`             | TIMESTAMP    | Timestamp                   |

**Composite Unique**: (`phase_id`, `service_id`)

#### 2.2.4 `patient_treatment_plans`

**Purpose**: Hợp đồng điều trị thực tế của bệnh nhân (clone từ template)

| Column              | Type               | Description                             |
| ------------------- | ------------------ | --------------------------------------- |
| `plan_id`           | BIGSERIAL PK       | Unique identifier                       |
| `patient_id`        | INTEGER FK         | Reference to patients                   |
| `template_id`       | BIGINT FK          | Template gốc (nullable - có thể custom) |
| `plan_code`         | VARCHAR(50) UNIQUE | Business code (VD: PLAN-20251107-001)   |
| `plan_name`         | VARCHAR(255)       | Tên plan (copy từ template)             |
| `start_date`        | DATE               | Ngày bắt đầu điều trị                   |
| `expected_end_date` | DATE               | Ngày kết thúc dự kiến                   |
| `total_price`       | NUMERIC(12,2)      | Tổng giá trị (có thể điều chỉnh)        |
| `status`            | VARCHAR(20)        | IN_PROGRESS / COMPLETED / CANCELLED     |
| `created_by`        | INTEGER FK         | Employee đã tạo                         |
| `created_at`        | TIMESTAMP          | Timestamp                               |

**Indexes**: `plan_code` (UNIQUE), `patient_id`, `status`

#### 2.2.5 `patient_plan_phases`

**Purpose**: Giai đoạn thực tế của bệnh nhân (clone từ template_phases)

| Column             | Type         | Description                          |
| ------------------ | ------------ | ------------------------------------ |
| `patient_phase_id` | BIGSERIAL PK | Unique identifier                    |
| `plan_id`          | BIGINT FK    | Reference to patient_treatment_plans |
| `phase_number`     | INTEGER      | Thứ tự giai đoạn                     |
| `phase_name`       | VARCHAR(255) | Tên giai đoạn                        |
| `start_date`       | DATE         | Ngày bắt đầu thực tế                 |
| `completion_date`  | DATE         | Ngày hoàn thành thực tế              |
| `status`           | VARCHAR(20)  | PENDING / IN_PROGRESS / COMPLETED    |
| `created_at`       | TIMESTAMP    | Timestamp                            |

**Composite Unique**: (`plan_id`, `phase_number`)

#### 2.2.6 `patient_plan_items` ⭐

**Purpose**: Hạng mục công việc cụ thể (VD: "Lần 3/24: Siết niềng")

| Column                   | Type          | Description                                                 |
| ------------------------ | ------------- | ----------------------------------------------------------- |
| `item_id`                | BIGSERIAL PK  | Unique identifier                                           |
| `phase_id`               | BIGINT FK     | Reference to patient_plan_phases                            |
| `service_id`             | INTEGER FK    | Reference to services                                       |
| `item_name`              | VARCHAR(255)  | Tên công việc (VD: "Lần 3/24: Siết niềng")                  |
| `sequence_number`        | INTEGER       | Thứ tự trong giai đoạn (1, 2, 3...)                         |
| `status`                 | VARCHAR(30)   | **READY_FOR_BOOKING** / SCHEDULED / IN_PROGRESS / COMPLETED |
| `price`                  | NUMERIC(10,2) | Giá dịch vụ (tại thời điểm tạo)                             |
| `estimated_time_minutes` | INTEGER       | Thời gian dự kiến                                           |
| `completed_at`           | TIMESTAMP     | Timestamp hoàn thành                                        |
| `created_at`             | TIMESTAMP     | Timestamp                                                   |

**Indexes**: `status` (for filtering bookable items), `phase_id` (for phase progress)

**Status Flow:**

```
READY_FOR_BOOKING → SCHEDULED → IN_PROGRESS → COMPLETED
```

#### 2.2.7 `appointment_plan_items` (Bridge Table)

**Purpose**: N-N relationship between appointments and patient_plan_items

| Column           | Type      | Description                     |
| ---------------- | --------- | ------------------------------- |
| `appointment_id` | BIGINT FK | Reference to appointments       |
| `item_id`        | BIGINT FK | Reference to patient_plan_items |
| `created_at`     | TIMESTAMP | Timestamp                       |

**Composite PK**: (`appointment_id`, `item_id`)

**Business Rule**: Khi tạo appointment với `patientPlanItemIds`, bridge records được tạo và items chuyển từ READY_FOR_BOOKING → SCHEDULED.

---

## 3. BUSINESS LOGIC

### 3.1 Template Creation Workflow

**Role**: Doctor / Admin

1. **Create Template**: POST `/api/v1/treatment-plan-templates`
   - Doctor định nghĩa gói điều trị mẫu (tên, thời gian, giá)
2. **Add Phases**: POST `/api/v1/treatment-plan-templates/{templateId}/phases`
   - Chia gói thành các giai đoạn (Phase 1, 2, 3...)
3. **Add Services to Phase**: POST `/api/v1/template-phases/{phaseId}/services`
   - Thêm dịch vụ vào từng giai đoạn (với quantity)

**Example**: Gói "Niềng răng 2 năm" có:

- **Phase 1**: Khám (1 lần), Chụp phim (1 lần), Cạo vôi (1 lần)
- **Phase 2**: Gắn mắc cài (1 lần)
- **Phase 3**: Tái khám (24 lần)
- **Phase 4**: Tháo niềng (1 lần), Làm hàm duy trì (1 lần)

### 3.2 Patient Plan Assignment Workflow

**Role**: Receptionist / Doctor

1. **Select Template**: GET `/api/v1/treatment-plan-templates?isActive=true`
2. **Apply to Patient**: POST `/api/v1/patient-treatment-plans`
   ```json
   {
     "patientId": 123,
     "templateId": 1,
     "startDate": "2025-11-08",
     "totalPrice": 30000000
   }
   ```
3. **System clones**:
   - Template Phases → Patient Plan Phases
   - Template Phase Services → Patient Plan Items (với status = READY_FOR_BOOKING)

**Result**: Patient có plan với 28 items (1 + 1 + 1 + 1 + 24 + 1 + 1), tất cả đều READY_FOR_BOOKING.

### 3.3 Appointment Booking with Treatment Plan

**Role**: Receptionist

**Traditional Booking (Luồng 1 - Đặt lẻ):**

```json
POST /api/v1/appointments
{
  "patientCode": "P001",
  "serviceCodes": ["SCALING_L1", "FILLING_COMP"],
  "employeeCode": "E001",
  ...
}
```

**Treatment Plan Booking (Luồng 2 - Đặt theo lộ trình):**

```json
POST /api/v1/appointments
{
  "patientCode": "P001",
  "patientPlanItemIds": [101, 102], // Item "Lần 3: Siết niềng" và "Lần 4: Siết niềng"
  "employeeCode": "E001",
  ...
}
```

**Validation Rules** (in AppointmentCreationService):

1. **XOR**: Must provide EITHER `serviceCodes` OR `patientPlanItemIds`, not both
2. **Status Check**: All items must have status = READY_FOR_BOOKING
3. **Ownership**: All items must belong to the patient in request
4. **Service Extraction**: Extract serviceId from items to validate doctor specializations, room compatibility

**After Appointment Created**:

- Insert bridge records: `appointment_plan_items`
- Update item status: READY_FOR_BOOKING → SCHEDULED
- Patient can see "Đã đặt lịch" in UI

### 3.4 Status Flow Management

#### Patient Plan Item Status

| Status              | Meaning                    | Allowed Transitions                         |
| ------------------- | -------------------------- | ------------------------------------------- |
| `READY_FOR_BOOKING` | Hạng mục sẵn sàng đặt lịch | → SCHEDULED (when appointment created)      |
| `SCHEDULED`         | Đã đặt lịch hẹn            | → IN_PROGRESS (when appointment checked-in) |
| `IN_PROGRESS`       | Đang thực hiện             | → COMPLETED (when appointment completed)    |
| `COMPLETED`         | Hoàn thành                 | (Final state)                               |

#### Patient Plan Phase Status

**Calculation**: Based on item completion

```sql
-- Phase is IN_PROGRESS if ANY item is IN_PROGRESS or SCHEDULED
-- Phase is COMPLETED if ALL items are COMPLETED
-- Phase is PENDING if ALL items are READY_FOR_BOOKING
```

#### Patient Plan Status

**Calculation**: Based on phase completion

```sql
-- Plan is IN_PROGRESS if ANY phase is IN_PROGRESS
-- Plan is COMPLETED if ALL phases are COMPLETED
-- Plan can be CANCELLED by doctor/admin
```

---

## 4. API SPECIFICATIONS

### 4.1 GET Treatment Plan Templates

**Endpoint**: `GET /api/v1/treatment-plan-templates`

**Purpose**: Lấy danh sách các template có sẵn (for receptionist to apply)

**Query Parameters**:

- `isActive` (Boolean, optional): Filter by active status (default: true)
- `page` (Integer, optional): Page number (default: 0)
- `size` (Integer, optional): Page size (default: 20)

**Response** (200 OK):

```json
{
  "content": [
    {
      "templateId": 1,
      "templateCode": "TPL_ORTHO_METAL",
      "templateName": "Niềng răng mắc cài kim loại trọn gói 2 năm",
      "description": "Gói điều trị chỉnh nha toàn diện...",
      "estimatedDurationDays": 730,
      "totalPrice": 30000000,
      "phaseCount": 4,
      "totalServiceCount": 28,
      "isActive": true,
      "createdAt": "2025-11-07T10:00:00"
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

**Business Rules**:

- Only active templates shown by default
- Sorted by `createdAt DESC`
- Include summary counts (phaseCount, totalServiceCount)

---

### 4.2 GET Template Details

**Endpoint**: `GET /api/v1/treatment-plan-templates/{templateId}`

**Purpose**: Xem chi tiết template (including phases and services)

**Path Parameters**:

- `templateId` (Long, required): Template ID

**Response** (200 OK):

```json
{
  "templateId": 1,
  "templateCode": "TPL_ORTHO_METAL",
  "templateName": "Niềng răng mắc cài kim loại trọn gói 2 năm",
  "description": "Gói điều trị chỉnh nha toàn diện với mắc cài kim loại, bao gồm 24 lần tái khám siết niềng định kỳ.",
  "estimatedDurationDays": 730,
  "totalPrice": 30000000,
  "isActive": true,
  "phases": [
    {
      "phaseId": 1,
      "phaseNumber": 1,
      "phaseName": "Giai đoạn 1: Khám & Chuẩn bị",
      "estimatedDurationDays": 14,
      "services": [
        {
          "serviceId": 38,
          "serviceCode": "ORTHO_CONSULT",
          "serviceName": "Khám & Tư vấn Chỉnh nha",
          "quantity": 1,
          "estimatedTimeMinutes": 45,
          "pricePerUnit": 0
        },
        {
          "serviceId": 39,
          "serviceCode": "ORTHO_FILMS",
          "serviceName": "Chụp Phim Chỉnh nha (Pano, Ceph)",
          "quantity": 1,
          "estimatedTimeMinutes": 30,
          "pricePerUnit": 500000
        }
      ]
    },
    {
      "phaseId": 3,
      "phaseNumber": 3,
      "phaseName": "Giai đoạn 3: Điều chỉnh định kỳ (24 tháng)",
      "estimatedDurationDays": 715,
      "services": [
        {
          "serviceId": 41,
          "serviceCode": "ORTHO_ADJUST",
          "serviceName": "Tái khám Chỉnh nha / Siết niềng",
          "quantity": 24,
          "estimatedTimeMinutes": 30,
          "pricePerUnit": 500000
        }
      ]
    }
  ],
  "createdAt": "2025-11-07T10:00:00"
}
```

**Business Rules**:

- Phases must be ordered by `phase_number`
- Services within phase ordered by creation time
- If template not found → 404 with errorCode "TEMPLATE_NOT_FOUND"

---

### 4.3 GET Patient Treatment Plans

**Endpoint**: `GET /api/v1/patient-treatment-plans`

**Purpose**: Lấy danh sách plans của bệnh nhân (for tracking progress)

**Query Parameters**:

- `patientId` (Integer, required): Patient ID
- `status` (String, optional): Filter by status (IN_PROGRESS, COMPLETED, CANCELLED)

**Response** (200 OK):

```json
{
  "plans": [
    {
      "planId": 101,
      "planCode": "PLAN-20251107-001",
      "planName": "Niềng răng mắc cài kim loại trọn gói 2 năm",
      "templateCode": "TPL_ORTHO_METAL",
      "startDate": "2025-11-08",
      "expectedEndDate": "2027-11-08",
      "totalPrice": 30000000,
      "status": "IN_PROGRESS",
      "progress": {
        "completedItems": 5,
        "totalItems": 28,
        "percentage": 17.86
      },
      "currentPhase": {
        "phaseNumber": 3,
        "phaseName": "Giai đoạn 3: Điều chỉnh định kỳ (24 tháng)"
      },
      "createdAt": "2025-11-07T14:30:00"
    }
  ]
}
```

**Business Rules**:

- Calculate progress based on completed items
- `currentPhase` = first IN_PROGRESS phase (or last COMPLETED if all done)
- Only show plans where `patient_treatment_plans.patient_id = {patientId}`

---

### 4.4 GET Patient Plan Details

**Endpoint**: `GET /api/v1/patient-treatment-plans/{planId}`

**Purpose**: Xem chi tiết plan của bệnh nhân (including all items with status)

**Path Parameters**:

- `planId` (Long, required): Plan ID

**Response** (200 OK):

```json
{
  "planId": 101,
  "planCode": "PLAN-20251107-001",
  "planName": "Niềng răng mắc cài kim loại trọn gói 2 năm",
  "patientId": 1,
  "patientName": "Nguyễn Văn A",
  "templateCode": "TPL_ORTHO_METAL",
  "startDate": "2025-11-08",
  "expectedEndDate": "2027-11-08",
  "totalPrice": 30000000,
  "status": "IN_PROGRESS",
  "phases": [
    {
      "patientPhaseId": 201,
      "phaseNumber": 1,
      "phaseName": "Giai đoạn 1: Khám & Chuẩn bị",
      "startDate": "2025-11-08",
      "completionDate": "2025-11-22",
      "status": "COMPLETED",
      "items": [
        {
          "itemId": 301,
          "itemName": "Khám & Tư vấn Chỉnh nha",
          "serviceCode": "ORTHO_CONSULT",
          "sequenceNumber": 1,
          "status": "COMPLETED",
          "price": 0,
          "estimatedTimeMinutes": 45,
          "completedAt": "2025-11-08T09:00:00"
        },
        {
          "itemId": 302,
          "itemName": "Chụp Phim Chỉnh nha",
          "serviceCode": "ORTHO_FILMS",
          "sequenceNumber": 2,
          "status": "COMPLETED",
          "price": 500000,
          "estimatedTimeMinutes": 30,
          "completedAt": "2025-11-08T10:00:00"
        }
      ]
    },
    {
      "patientPhaseId": 203,
      "phaseNumber": 3,
      "phaseName": "Giai đoạn 3: Điều chỉnh định kỳ (24 tháng)",
      "startDate": "2025-11-20",
      "completionDate": null,
      "status": "IN_PROGRESS",
      "items": [
        {
          "itemId": 305,
          "itemName": "Lần 1/24: Siết niềng",
          "serviceCode": "ORTHO_ADJUST",
          "sequenceNumber": 1,
          "status": "COMPLETED",
          "price": 500000,
          "estimatedTimeMinutes": 30,
          "completedAt": "2025-11-20T14:00:00"
        },
        {
          "itemId": 306,
          "itemName": "Lần 2/24: Siết niềng",
          "serviceCode": "ORTHO_ADJUST",
          "sequenceNumber": 2,
          "status": "SCHEDULED",
          "appointmentCode": "APT-20251208-001",
          "scheduledDate": "2025-12-08",
          "price": 500000,
          "estimatedTimeMinutes": 30,
          "completedAt": null
        },
        {
          "itemId": 307,
          "itemName": "Lần 3/24: Siết niềng",
          "serviceCode": "ORTHO_ADJUST",
          "sequenceNumber": 3,
          "status": "READY_FOR_BOOKING",
          "price": 500000,
          "estimatedTimeMinutes": 30,
          "completedAt": null
        }
      ]
    }
  ],
  "createdAt": "2025-11-07T14:30:00",
  "createdBy": {
    "employeeId": 5,
    "employeeName": "Nguyễn Văn Lễ Tân"
  }
}
```

**Business Rules**:

- Phases ordered by `phase_number`
- Items within phase ordered by `sequence_number`
- For SCHEDULED items, include `appointmentCode` and `scheduledDate` (join with appointments)
- Calculate phase status:
  - COMPLETED if all items COMPLETED
  - IN_PROGRESS if any item SCHEDULED/IN_PROGRESS
  - PENDING if all items READY_FOR_BOOKING

---

### 4.5 GET Bookable Items

**Endpoint**: `GET /api/v1/patient-treatment-plans/{planId}/bookable-items`

**Purpose**: Lấy danh sách items sẵn sàng đặt lịch (for receptionist to select in booking UI)

**Path Parameters**:

- `planId` (Long, required): Plan ID

**Query Parameters**:

- `phaseNumber` (Integer, optional): Filter by phase number

**Response** (200 OK):

```json
{
  "planId": 101,
  "planCode": "PLAN-20251107-001",
  "patientId": 1,
  "patientName": "Nguyễn Văn A",
  "bookableItems": [
    {
      "itemId": 307,
      "itemName": "Lần 3/24: Siết niềng",
      "serviceId": 41,
      "serviceCode": "ORTHO_ADJUST",
      "serviceName": "Tái khám Chỉnh nha / Siết niềng",
      "phaseNumber": 3,
      "phaseName": "Giai đoạn 3: Điều chỉnh định kỳ",
      "sequenceNumber": 3,
      "price": 500000,
      "estimatedTimeMinutes": 30
    },
    {
      "itemId": 308,
      "itemName": "Lần 4/24: Siết niềng",
      "serviceId": 41,
      "serviceCode": "ORTHO_ADJUST",
      "serviceName": "Tái khám Chỉnh nha / Siết niềng",
      "phaseNumber": 3,
      "phaseName": "Giai đoạn 3: Điều chỉnh định kỳ",
      "sequenceNumber": 4,
      "price": 500000,
      "estimatedTimeMinutes": 30
    }
  ]
}
```

**Business Rules**:

- Only return items with `status = READY_FOR_BOOKING`
- Include serviceId for doctor specialization lookup
- Ordered by phaseNumber ASC, sequenceNumber ASC
- If plan not found or belongs to different patient → 404

**Frontend Usage**:

```javascript
// Step 1: Fetch bookable items
const response = await fetch(
  `/api/v1/patient-treatment-plans/${planId}/bookable-items`
);
const data = await response.json();

// Step 2: Receptionist selects items (e.g., "Lần 3" and "Lần 4")
const selectedItemIds = [307, 308];

// Step 3: Create appointment with Treatment Plan mode
await fetch("/api/v1/appointments", {
  method: "POST",
  body: JSON.stringify({
    patientCode: "P001",
    patientPlanItemIds: selectedItemIds, // Luồng 2: Treatment Plan Booking
    employeeCode: "E001",
    roomCode: "P-01",
    appointmentStartTime: "2025-12-08T14:00:00",
    participantCodes: [],
    notes: "Tái khám niềng răng lần 3 và 4",
  }),
});
```

---

## 5. INTEGRATION WITH BOOKING MODULE

### 5.1 API 3.2 Upgrade (in Booking Module)

**File**: `docs/api-guides/booking/appointment/Appointment.md`

**Changes to CreateAppointmentRequest**:

```java
@Data
public class CreateAppointmentRequest {
    // EXISTING: Luồng 1 - Đặt lẻ
    private List<String> serviceCodes;

    // NEW V2: Luồng 2 - Đặt theo lộ trình
    private List<Long> patientPlanItemIds;

    // XOR Validation
    @AssertTrue(message = "Must provide either serviceCodes or patientPlanItemIds, not both")
    private boolean isValidBookingType() {
        boolean hasServiceCodes = serviceCodes != null && !serviceCodes.isEmpty();
        boolean hasPlanItems = patientPlanItemIds != null && !patientPlanItemIds.isEmpty();
        return hasServiceCodes ^ hasPlanItems; // XOR: exactly one must be true
    }

    // ... other existing fields
}
```

### 5.2 Validation Flow in AppointmentCreationService

**STEP 2B: Validate Plan Items (NEW)**

```java
private List<PatientPlanItem> validatePlanItems(List<Long> itemIds, Integer patientId) {
    // Check 1: All items exist
    List<PatientPlanItem> items = patientPlanItemRepository.findAllById(itemIds);
    if (items.size() != itemIds.size()) {
        throw new BadRequestAlertException("Some plan items not found", ENTITY_NAME, "PLAN_ITEMS_NOT_FOUND");
    }

    // Check 2: All items belong to this patient
    boolean allBelongToPatient = items.stream()
        .allMatch(item -> item.getPhase().getPlan().getPatientId().equals(patientId));
    if (!allBelongToPatient) {
        throw new BadRequestAlertException("Plan items do not belong to this patient", ENTITY_NAME, "PLAN_ITEMS_WRONG_PATIENT");
    }

    // Check 3: All items are ready for booking
    boolean allReady = items.stream()
        .allMatch(item -> item.getStatus() == PlanItemStatus.READY_FOR_BOOKING);
    if (!allReady) {
        throw new BadRequestAlertException("Some plan items are not ready for booking", ENTITY_NAME, "PLAN_ITEMS_NOT_READY");
    }

    return items;
}
```

**STEP 2 Modified Logic**:

```java
List<DentalService> services;
boolean isBookingFromPlan = request.getPatientPlanItemIds() != null && !request.getPatientPlanItemIds().isEmpty();

if (isBookingFromPlan) {
    // Luồng 2: Treatment Plan Booking
    List<PatientPlanItem> items = validatePlanItems(request.getPatientPlanItemIds(), patient.getPatientId());
    services = items.stream().map(PatientPlanItem::getService).distinct().collect(Collectors.toList());
} else {
    // Luồng 1: Standalone Booking (existing)
    services = validateServices(request.getServiceCodes());
}
// Continue with existing validation (specializations, room, shifts, conflicts)
```

**STEP 8 Modified Logic** (after insertAppointmentParticipants):

```java
if (isBookingFromPlan) {
    // Insert bridge table records
    insertAppointmentPlanItems(appointment, request.getPatientPlanItemIds());

    // Update item status: READY_FOR_BOOKING → SCHEDULED
    updatePlanItemsStatus(request.getPatientPlanItemIds(), PlanItemStatus.SCHEDULED);
}
```

### 5.3 New Methods in AppointmentCreationService

```java
private void insertAppointmentPlanItems(Appointment appointment, List<Long> itemIds) {
    for (Long itemId : itemIds) {
        AppointmentPlanItem api = new AppointmentPlanItem();
        AppointmentPlanItemId id = new AppointmentPlanItemId();
        id.setAppointmentId(appointment.getAppointmentId());
        id.setItemId(itemId);
        api.setId(id);
        appointmentPlanItemRepository.save(api);
    }
}

private void updatePlanItemsStatus(List<Long> itemIds, PlanItemStatus newStatus) {
    try {
        List<PatientPlanItem> items = patientPlanItemRepository.findAllById(itemIds);
        items.forEach(item -> item.setStatus(newStatus));
        patientPlanItemRepository.saveAll(items);
    } catch (Exception e) {
        log.error("Failed to update plan items status. Transaction will rollback.", e);
        throw new RuntimeException("Failed to update plan items status", e);
    }
}
```

### 5.4 Rollback Safety

**Transaction Scope**: Entire `createAppointment()` method is `@Transactional`

**Rollback Scenarios**:

1. **Doctor conflict detected** → Items remain READY_FOR_BOOKING
2. **Room conflict detected** → Items remain READY_FOR_BOOKING
3. **Patient conflict detected** → Items remain READY_FOR_BOOKING
4. **Status update fails** → Entire transaction rolls back (appointment not created)

**Key Rule**: Items chỉ chuyển sang SCHEDULED khi appointment đã được INSERT thành công vào database.

---

## 6. SAMPLE DATA

### 6.1 Seeded Templates

| Template Code      | Template Name                                 | Duration | Price   | Phases |
| ------------------ | --------------------------------------------- | -------- | ------- | ------ |
| TPL_ORTHO_METAL    | Niềng răng mắc cài kim loại trọn gói 2 năm    | 730 days | 30M VND | 4      |
| TPL_IMPLANT_OSSTEM | Cấy ghép Implant Hàn Quốc (Osstem) - Trọn gói | 180 days | 19M VND | 3      |
| TPL_CROWN_CERCON   | Bọc răng sứ Cercon HT - 1 răng                | 7 days   | 5M VND  | 2      |

### 6.2 Template Breakdown: TPL_ORTHO_METAL

**Phase 1: Khám & Chuẩn bị (14 days)**

- ORTHO_CONSULT × 1 (45 min, 0 VND)
- ORTHO_FILMS × 1 (30 min, 500K VND)
- SCALING_L1 × 1 (60 min, 300K VND)

**Phase 2: Gắn mắc cài (1 day)**

- ORTHO_BRACES_ON × 1 (120 min, 5M VND)

**Phase 3: Điều chỉnh định kỳ (715 days = ~24 months)**

- ORTHO_ADJUST × 24 (30 min each, 500K VND each)

**Phase 4: Tháo niềng & Duy trì (0 days)**

- ORTHO_BRACES_OFF × 1 (75 min, 1M VND)
- ORTHO_RETAINER_REMOV × 1 (45 min, 1M VND)

**Total**: 28 items, 30M VND

### 6.3 Sample Patient Plan (After Applying Template)

**Patient**: Nguyễn Văn A (ID: 1)
**Plan Code**: PLAN-20251107-001
**Start Date**: 2025-11-08
**Expected End**: 2027-11-08

**Status**: IN_PROGRESS (5/28 items completed = 17.86%)

**Current Phase**: Phase 3 (Điều chỉnh định kỳ)

- Item 305 (Lần 1/24): ✅ COMPLETED (2025-11-20)
- Item 306 (Lần 2/24): 📅 SCHEDULED (APT-20251208-001)
- Item 307 (Lần 3/24): 🟢 READY_FOR_BOOKING
- Item 308 (Lần 4/24): 🟢 READY_FOR_BOOKING
- ...
- Item 328 (Lần 24/24): 🟢 READY_FOR_BOOKING

---

## 7. TESTING GUIDE

### 7.1 Test Scenario 1: Create Patient Plan from Template

**Steps**:

1. GET `/api/v1/treatment-plan-templates?isActive=true` → Select TPL_ORTHO_METAL
2. POST `/api/v1/patient-treatment-plans`:
   ```json
   {
     "patientId": 1,
     "templateId": 1,
     "startDate": "2025-11-08",
     "totalPrice": 30000000
   }
   ```
3. Verify database:
   - `patient_treatment_plans`: 1 row (status = IN_PROGRESS)
   - `patient_plan_phases`: 4 rows
   - `patient_plan_items`: 28 rows (all READY_FOR_BOOKING)

### 7.2 Test Scenario 2: Book Appointment with Treatment Plan Items

**Precondition**: Patient has plan PLAN-20251107-001 with items ready

**Steps**:

1. GET `/api/v1/patient-treatment-plans/101/bookable-items` → Get item IDs
2. POST `/api/v1/appointments`:
   ```json
   {
     "patientCode": "P001",
     "patientPlanItemIds": [307, 308],
     "employeeCode": "E001",
     "roomCode": "P-01",
     "appointmentStartTime": "2025-12-08T14:00:00",
     "participantCodes": [],
     "notes": "Tái khám lần 3 và 4"
   }
   ```
3. Verify:
   - Appointment created with `appointment_services` containing ORTHO_ADJUST
   - `appointment_plan_items`: 2 rows (appointment_id → 307, 308)
   - `patient_plan_items`: Items 307, 308 status changed to SCHEDULED

### 7.3 Test Scenario 3: XOR Validation

**Test Case 3.1**: Both serviceCodes and patientPlanItemIds provided

```json
POST /api/v1/appointments
{
  "serviceCodes": ["SCALING_L1"],
  "patientPlanItemIds": [307],
  ...
}
```

**Expected**: 400 Bad Request with message "Must provide either serviceCodes or patientPlanItemIds, not both"

**Test Case 3.2**: Neither serviceCodes nor patientPlanItemIds provided

```json
POST /api/v1/appointments
{
  "patientCode": "P001",
  ...
}
```

**Expected**: 400 Bad Request with XOR validation error

### 7.4 Test Scenario 4: Plan Item Status Validation

**Precondition**: Item 306 is already SCHEDULED

**Steps**:

1. Try to book item 306 again:
   ```json
   POST /api/v1/appointments
   {
     "patientPlanItemIds": [306],
     ...
   }
   ```
2. **Expected**: 400 Bad Request with errorCode "PLAN_ITEMS_NOT_READY"

### 7.5 Test Scenario 5: Wrong Patient Validation

**Steps**:

1. Patient A (ID: 1) has plan with item 307
2. Try to book for Patient B (ID: 2):
   ```json
   POST /api/v1/appointments
   {
     "patientCode": "P002",
     "patientPlanItemIds": [307],
     ...
   }
   ```
3. **Expected**: 400 Bad Request with errorCode "PLAN_ITEMS_WRONG_PATIENT"

---

## 8. ERROR HANDLING

### 8.1 Common Error Codes

| Error Code                 | HTTP Status | Description                                  |
| -------------------------- | ----------- | -------------------------------------------- |
| `TEMPLATE_NOT_FOUND`       | 404         | Template ID không tồn tại                    |
| `PLAN_NOT_FOUND`           | 404         | Patient Plan ID không tồn tại                |
| `PLAN_ITEMS_NOT_FOUND`     | 400         | Một hoặc nhiều item IDs không tồn tại        |
| `PLAN_ITEMS_WRONG_PATIENT` | 400         | Items không thuộc về bệnh nhân trong request |
| `PLAN_ITEMS_NOT_READY`     | 400         | Items không ở trạng thái READY_FOR_BOOKING   |
| `INVALID_BOOKING_TYPE`     | 400         | Vi phạm XOR rule (cả hai hoặc không có gì)   |

### 8.2 Error Response Format

```json
{
  "type": "https://www.jhipster.tech/problem/problem-with-message",
  "title": "Bad Request",
  "status": 400,
  "detail": "Some plan items are not ready for booking",
  "path": "/api/v1/appointments",
  "message": "error.PLAN_ITEMS_NOT_READY",
  "errorCode": "PLAN_ITEMS_NOT_READY"
}
```

---

## 9. FRONTEND INTEGRATION NOTES

### 9.1 Two Booking Flows in UI

**Standalone Booking (Legacy)**:

- Receptionist manually selects services from dropdown
- No pre-existing plan required

**Treatment Plan Booking (New)**:

- Receptionist views patient's plan
- Clicks "Đặt lịch" on READY_FOR_BOOKING items
- System pre-fills serviceCode from item
- Receptionist only needs to select doctor, room, time

### 9.2 UI Components Needed

1. **Patient Plan Dashboard**: Show progress bar, phase timeline
2. **Bookable Items List**: Filter by READY_FOR_BOOKING, checkbox selection
3. **Booking Modal**: Show selected items, calculate total duration
4. **Appointment History**: Link appointments to plan items (show "Lần 3/24" in appointment card)

---

## 10. FUTURE ENHANCEMENTS

### 10.1 V2 Features (Not in Current Scope)

- [ ] **Payment Integration**: Track payment by phase (partial payments)
- [ ] **Doctor Recommendations**: Suggest next appointment date based on phase timeline
- [ ] **Progress Notifications**: Notify patient when items become READY_FOR_BOOKING
- [ ] **Template Versioning**: Allow updating templates without affecting existing plans
- [ ] **Custom Plans**: Allow doctors to create one-off plans without template

---

## 11. SUMMARY

✅ **Treatment Plan Module** provides structured workflow for multi-phase treatments
✅ **Template System** standardizes common procedures (Niềng răng, Implant, Bọc sứ)
✅ **Patient Plans** track progress from start to completion
✅ **Appointment Integration** allows booking directly from plan items
✅ **Status Management** ensures items flow correctly (READY → SCHEDULED → IN_PROGRESS → COMPLETED)
✅ **XOR Validation** enforces clear separation between standalone and plan-based bookings
✅ **Rollback Safety** prevents partial updates in case of conflicts

**Key Benefit**: Receptionist không cần nhớ "Lần này đánh răng hay siết niềng?" - Hệ thống tự động theo dõi!

---

**Document Version**: 1.0
**Last Updated**: November 7, 2025
**Next Review**: After API 3.2 implementation completed
