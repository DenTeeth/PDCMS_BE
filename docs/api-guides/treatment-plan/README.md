# Treatment Plan Management - API Documentation

**Module**: Treatment Plan Management
**Version**: V1.0
**Status**: ✅ Production Ready
**Last Updated**: 2025-11-12

---

## ⚠️ CRITICAL: Endpoint Path Notice

**CORRECT ENDPOINT**: `/api/v1/patients/{patientCode}/treatment-plans` (**PLURAL**)

**WRONG ENDPOINT**: `/api/v1/patients/{patientCode}/treatment-plan` (singular) ❌

**Common Error**:

```
POST /api/v1/patients/BN-1001/treatment-plan  ❌ 404 Error
POST /api/v1/patients/BN-1001/treatment-plans ✅ Correct
```

**Why**: RESTful convention uses plural for resource collections.

---

## 📋 Overview

Treatment Plan Management system manages long-term patient care contracts. Each plan contains multiple phases, and each phase contains multiple service items.

**Key Concepts**:

- **Treatment Plan**: Long-term care contract (e.g., 2-year orthodontics package)
- **Phase**: Logical stage in treatment (e.g., "Preparation", "Installation", "Monthly Adjustments")
- **Item**: Individual service to be performed (can be linked to appointments)

**Features**:

- ✅ Template-based plans (standardized packages)
- ✅ Custom plans (doctor-defined)
- ✅ Quantity expansion (1 item → N trackable items)
- ✅ Price customization
- ✅ Approval workflow (DRAFT → APPROVED)
- ✅ Progress tracking
- ✅ RBAC (Role-Based Access Control)

---

## 🔗 API Index

| API | Method | Endpoint                                      | Description                        | Doc Link                                              |
| --- | ------ | --------------------------------------------- | ---------------------------------- | ----------------------------------------------------- |
| 5.1 | GET    | `/patients/{code}/treatment-plans`            | List plans for patient (paginated) | [API 5.1 & 5.2](./API_5.1_5.2_Get_Treatment_Plans.md) |
| 5.2 | GET    | `/patients/{code}/treatment-plans/{planCode}` | Get plan detail                    | [API 5.1 & 5.2](./API_5.1_5.2_Get_Treatment_Plans.md) |
| 5.3 | POST   | `/patients/{code}/treatment-plans`            | Create from template               | [API 5.3](./API_5.3_Create_From_Template.md)          |
| 5.4 | POST   | `/patients/{code}/treatment-plans/custom`     | Create custom plan                 | [API 5.4](./API_5.4_Create_Custom_Plan.md)            |
| 5.5 | GET    | `/patient-treatment-plans`                    | Get all with RBAC & filters        | [API 5.5](./API_5.5_Get_All_With_RBAC.md)             |

---

## 🚀 Quick Start

### 1. Get Treatment Plans for Patient

```bash
GET http://localhost:8080/api/v1/patients/BN-1001/treatment-plans?page=0&size=20
Authorization: Bearer {jwt_token}
```

### 2. Get Treatment Plan Detail

```bash
GET http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001
Authorization: Bearer {jwt_token}
```

### 3. Create Plan from Template

```bash
POST http://localhost:8080/api/v1/patients/BN-1001/treatment-plans
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "sourceTemplateCode": "TPL_ORTHO_METAL",
  "doctorEmployeeCode": "EMP-001",
  "discountAmount": 0,
  "paymentType": "INSTALLMENT"
}
```

### 4. Create Custom Plan

```bash
POST http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/custom
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "planName": "Lộ trình niềng răng tùy chỉnh",
  "doctorEmployeeCode": "EMP-001",
  "discountAmount": 0,
  "paymentType": "INSTALLMENT",
  "phases": [
    {
      "phaseNumber": 1,
      "phaseName": "Giai đoạn 1: Khám",
      "estimatedDurationDays": 7,
      "items": [
        {
          "serviceCode": "EXAM_GENERAL",
          "price": 500000,
          "sequenceNumber": 1,
          "quantity": 1
        }
      ]
    }
  ]
}
```

### 5. Get All Plans (Admin Dashboard)

```bash
GET http://localhost:8080/api/v1/patient-treatment-plans?status=IN_PROGRESS&approvalStatus=APPROVED&page=0&size=20
Authorization: Bearer {jwt_token}
```

---

## 🔐 Permissions

### Permission Matrix

| Permission                | Role                  | API 5.1 | API 5.2 | API 5.3 | API 5.4 | API 5.5 |
| ------------------------- | --------------------- | ------- | ------- | ------- | ------- | ------- |
| `VIEW_TREATMENT_PLAN_ALL` | Admin, Manager, Staff | ✅ All  | ✅ All  | -       | -       | ✅ All  |
| `VIEW_TREATMENT_PLAN_OWN` | Patient               | ✅ Own  | ✅ Own  | -       | -       | ✅ Own  |
| `CREATE_TREATMENT_PLAN`   | Doctor, Manager       | -       | -       | ✅      | ✅      | -       |

### Role Behavior

**Admin** (Always has full access via `hasRole('ROLE_ADMIN')`):

- Full access to ALL APIs regardless of permissions
- Can view, create, update, delete any treatment plan

**Manager** (`VIEW_TREATMENT_PLAN_ALL` + `CREATE_TREATMENT_PLAN`):

- Can view **any** patient's plans
- Can create plans for patients
- Can filter by doctor/patient in API 5.5

**Dentist** (`VIEW_TREATMENT_PLAN_ALL` + `CREATE_TREATMENT_PLAN`):

- Can view **all** patients' plans
- Can create plans for patients
- Can view plans they created (API 5.5 auto-filtered)

**Receptionist** (`VIEW_TREATMENT_PLAN_ALL`):

- Can view **any** patient's plans (read-only)
- Cannot create or modify plans

**Patient** (`VIEW_TREATMENT_PLAN_OWN`):

- Can view **only** their own plans
- System auto-filters by patient ID

---

## 📊 Data Model

### Treatment Plan Hierarchy

```
PatientTreatmentPlan
├── plan_code: String (e.g., PLAN-20251001-001)
├── plan_name: String
├── status: PENDING | IN_PROGRESS | COMPLETED | CANCELLED
├── approval_status: DRAFT | APPROVED | REJECTED (V19)
├── patient: Patient
├── created_by: Employee (Doctor)
├── total_price: BigDecimal
├── discount_amount: BigDecimal
├── final_cost: BigDecimal
├── payment_type: FULL | PHASED | INSTALLMENT
│
└── phases: List<PatientPlanPhase>
    ├── phase_number: Integer (1, 2, 3, ...)
    ├── phase_name: String
    ├── status: PENDING | IN_PROGRESS | COMPLETED
    ├── estimated_duration_days: Integer (V19)
    │
    └── items: List<PatientPlanItem>
        ├── sequence_number: Integer
        ├── item_name: String
        ├── status: PENDING | READY_FOR_BOOKING | SCHEDULED | IN_PROGRESS | COMPLETED
        ├── service: DentalService
        ├── price: BigDecimal (snapshot)
        ├── estimated_time_minutes: Integer
        │
        └── linked_appointments: List<Appointment>
```

### Status Flows

**Plan Status**:

```
PENDING → IN_PROGRESS → COMPLETED
   ↓
CANCELLED
```

**Approval Status** (V19 - Custom Plans):

```
DRAFT → APPROVED → (can start)
   ↓
REJECTED
```

**Item Status**:

```
PENDING → READY_FOR_BOOKING → SCHEDULED → IN_PROGRESS → COMPLETED
```

---

## 🎯 Key Features

### 1. Template-based Plans (API 5.3)

**What**: Create plan by copying from pre-defined template package
**Example**: "Niềng răng 2 năm - 30 triệu đồng"
**Benefits**:

- ✅ Standardized care
- ✅ Faster creation
- ✅ Auto-approved

**Available Templates** (from seed data):

- `TPL_ORTHO_METAL` - Niềng răng mắc cài kim loại (30,000,000đ, 2 years)
- `TPL_IMPLANT_OSSTEM` - Cấy ghép Implant Osstem (19,000,000đ, 6 months)
- `TPL_CROWN_CERCON` - Bọc răng sứ Cercon HT (5,000,000đ, 7 days)

### 2. Custom Plans (API 5.4)

**What**: Doctor creates unique plan from scratch
**Example**: Custom orthodontics with specific services
**Benefits**:

- ✅ Full flexibility
- ✅ Price customization
- ✅ Unique patient needs

**Difference**: Requires approval (`approval_status = DRAFT`)

### 3. Quantity Expansion

**What**: Single item with `quantity = N` creates N trackable items

**Example**:

```json
{
  "serviceCode": "ORTHO_ADJUST",
  "quantity": 6
}
```

**Result**: 6 items created

- Điều chỉnh niềng răng (Lần 1)
- Điều chỉnh niềng răng (Lần 2)
- ...
- Điều chỉnh niềng răng (Lần 6)

**Benefits**:

- ✅ Track each appointment separately
- ✅ Clear progress (3/6 completed)
- ✅ Flexible scheduling

### 4. Price Customization (Custom Plans)

**Rule**: Price must be within **50%-150%** of service default

**Example**:

- Service default: 1,000,000đ
- ✅ Allowed: 500,000đ - 1,500,000đ
- ❌ Rejected: 400,000đ or 2,000,000đ

### 5. Smart RBAC (API 5.5)

**Admin**: Sees all plans, can filter by anyone
**Doctor**: Sees only plans they created
**Patient**: Sees only their own plans

**No code duplication** - system auto-filters based on JWT claims

---

## 🧪 Testing

### Test Data (Seed Data)

**Patients**:

- `BN-1001` - Đoàn Thanh Phong (has 2 plans)
- `BN-1002` - Phạm Văn Phong
- `BN-1003` - Lê Thị C (has 5 plans)

**Doctors**:

- `EMP-001` - Bác sĩ Nguyễn Văn A
- `EMP-002` - Bác sĩ Trần Thị B
- `EMP-003` - Bác sĩ Phạm Văn C

**Plans**:

- `PLAN-20251001-001` - Niềng răng (Patient BN-1001, Doctor EMP-001)
- `PLAN-20240515-001` - Implant (Patient BN-1002, Doctor EMP-002)
- ... (10 total plans in seed data)

### Test Scenarios

**Scenario 1**: Staff views all plans for patient BN-1001

```bash
GET /api/v1/patients/BN-1001/treatment-plans
# Expected: 2 plans
```

**Scenario 2**: Patient BN-1001 views their plans

```bash
GET /api/v1/patients/BN-1001/treatment-plans
# Expected: 2 plans (same as above)
```

**Scenario 3**: Patient BN-1001 tries to view BN-1002's plans

```bash
GET /api/v1/patients/BN-1002/treatment-plans
# Expected: 403 Forbidden
```

**Scenario 4**: Doctor EMP-001 views their patients' plans

```bash
GET /api/v1/patient-treatment-plans
# Expected: Only plans created by EMP-001
```

**Scenario 5**: Create plan from template

```bash
POST /api/v1/patients/BN-1001/treatment-plans
Body: { "sourceTemplateCode": "TPL_ORTHO_METAL", ... }
# Expected: 201 Created, ~31 items
```

---

## 🔧 Common Issues

### Issue 1: 404 - Endpoint Not Found

**Wrong**:

```bash
POST /api/v1/patient-treatment-plans/from-template  # ❌
```

**Correct**:

```bash
POST /api/v1/patients/BN-1001/treatment-plans  # ✅
```

**Reason**: API 5.3 uses patient-scoped RESTful path

### Issue 2: 403 - Access Denied

**Problem**: Patient trying to access other patient's plan

**Solution**: Use correct patient code matching JWT token

### Issue 3: 400 - Discount Exceeds Total

**Problem**: `discountAmount > totalPrice`

**Solution**: Ensure discount <= total price

### Issue 4: 400 - Price Out of Range (Custom Plans)

**Problem**: Custom price not within 50%-150% of service default

**Solution**: Check service default price and adjust

---

## 📚 Documentation Files

1. **[API 5.1 & 5.2 - Get Treatment Plans](./API_5.1_5.2_Get_Treatment_Plans.md)**

   - GET list (paginated)
   - GET detail (with phases/items)
   - RBAC permissions
   - Response models

2. **[API 5.3 - Create from Template](./API_5.3_Create_From_Template.md)**

   - Template-based creation
   - Auto-generated structure
   - Available templates
   - Discount validation

3. **[API 5.4 - Create Custom Plan](./API_5.4_Create_Custom_Plan.md)**

   - Custom plan creation
   - Quantity expansion logic
   - Price override validation
   - Approval workflow (DRAFT)

4. **[API 5.5 - Get All with RBAC](./API_5.5_Get_All_With_RBAC.md)**
   - Smart RBAC filtering
   - Advanced filters (date, search)
   - Performance optimization
   - Admin/Doctor/Patient modes

---

## 🔄 Integration Notes

### With Appointment Booking

After creating a treatment plan, items can be linked to appointments:

```bash
# Step 1: Get bookable items
GET /api/v1/patient-treatment-plans/{planId}/bookable-items

# Step 2: Book appointment
POST /api/v1/appointments
{
  "patientCode": "BN-1001",
  "patientPlanItemIds": [101, 102],  # Link to treatment plan items
  "employeeCode": "EMP-001",
  "roomCode": "RM-01",
  "appointmentStartTime": "2025-11-15T14:00:00"
}
```

### With Payment

Plans track payment status via `payment_type`:

- **FULL**: Pay all upfront
- **PHASED**: Pay when completing each phase
- **INSTALLMENT**: Monthly payments

---

## 📈 Version History

**V1.0** (2025-11-12):

- ✅ API 5.1-5.5 implemented
- ✅ V19 approval workflow
- ✅ Quantity expansion
- ✅ Smart RBAC
- ✅ Price override validation

**V19** (2025-11-10):

- ✅ Added `approval_status` enum
- ✅ Added `estimated_duration_days` to phases
- ✅ Custom plans require approval (DRAFT)

---

**Document Version**: 1.0
**Last Updated**: 2025-11-12
**Author**: Dental Clinic Development Team
**Source Code**: `com.dental.clinic.management.treatment_plans` package
