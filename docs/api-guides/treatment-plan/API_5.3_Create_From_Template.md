# API 5.3 - Create Treatment Plan from Template

**Module**: Treatment Plan Management
**Version**: V1.0
**Status**: ✅ Production Ready
**Last Updated**: 2025-11-12
**Source**: `TreatmentPlanController.java`, `CreateTreatmentPlanRequest.java`, `TreatmentPlanCreationService.java`

---

## ⚠️ CRITICAL WARNING: Correct Endpoint Path

**CORRECT**: `POST /api/v1/patients/{patientCode}/treatment-plans` (**PLURAL** `treatment-plans`)

**WRONG**: `POST /api/v1/patients/{patientCode}/treatment-plan` (singular) ❌

**Common Mistake**:

```bash
# ❌ WRONG - Returns 404 Error
POST /api/v1/patients/BN-1001/treatment-plan

# ✅ CORRECT
POST /api/v1/patients/BN-1001/treatment-plans
```

**Why This Matters**: RESTful convention uses plural nouns for resource collections. Using singular will result in `404 Not Found` error.

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [API Specification](#api-specification)
3. [Request Model](#request-model)
4. [Response Model](#response-model)
5. [Business Logic](#business-logic)
6. [Testing Guide](#testing-guide)
7. [Error Handling](#error-handling)

---

## Overview

API 5.3 creates a patient treatment plan by copying structure from a pre-defined template package.

**Key Features**:

- ✅ Automatic phase & item cloning from template
- ✅ Auto-generate unique plan code (PLAN-YYYYMMDD-XXX)
- ✅ Calculate expected end date from template duration
- ✅ Support custom plan name override
- ✅ Discount validation
- ✅ Transactional safety (rollback on error)

**Use Case**: Doctor selects a standardized treatment package (e.g., "Niềng răng 2 năm") and applies it to patient.

---

## API Specification

### Endpoint

```
POST /api/v1/patients/{patientCode}/treatment-plans
```

**IMPORTANT**: Endpoint path uses `{patientCode}` as **path variable**, NOT in request body!

### Path Parameters

| Parameter     | Type   | Required | Description           | Example |
| ------------- | ------ | -------- | --------------------- | ------- |
| `patientCode` | String | Yes      | Patient business code | BN-1001 |

### Request Headers

```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

### Security & Permissions

**@PreAuthorize Annotation**:

```java
@PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('CREATE_TREATMENT_PLAN')")
```

**Allowed Roles**:

- ✅ **Admin** - Full access (always allowed via `hasRole('ROLE_ADMIN')`)
- ✅ **Manager** - Has `CREATE_TREATMENT_PLAN` permission
- ✅ **Dentist** - Has `CREATE_TREATMENT_PLAN` permission
- ❌ **Receptionist** - No permission (read-only access)
- ❌ **Patient** - No permission

**Permission Check Logic**:

1. First checks if user has `ROLE_ADMIN` role → Immediate access
2. If not admin, checks if user has `CREATE_TREATMENT_PLAN` authority
3. Returns `403 Forbidden` if neither condition is met

### Request Body

```json
{
  "sourceTemplateCode": "TPL_ORTHO_METAL",
  "doctorEmployeeCode": "EMP-001",
  "planNameOverride": "Lộ trình niềng răng tùy chỉnh cho BN Phong",
  "discountAmount": 5000000,
  "paymentType": "INSTALLMENT"
}
```

### Complete Example Request

```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "sourceTemplateCode": "TPL_ORTHO_METAL",
    "doctorEmployeeCode": "EMP-001",
    "planNameOverride": null,
    "discountAmount": 0,
    "paymentType": "INSTALLMENT"
  }'
```

---

## Request Model

### CreateTreatmentPlanRequest

**File**: `CreateTreatmentPlanRequest.java`

| Field                | Type       | Required | Constraints                      | Default | Description                                   |
| -------------------- | ---------- | -------- | -------------------------------- | ------- | --------------------------------------------- |
| `sourceTemplateCode` | String     | **Yes**  | Max 50 chars, not blank          | -       | Template code to copy from                    |
| `doctorEmployeeCode` | String     | **Yes**  | Max 20 chars, not blank          | -       | Doctor in charge of this plan                 |
| `planNameOverride`   | String     | No       | Max 255 chars                    | null    | Custom plan name. If null, uses template name |
| `discountAmount`     | BigDecimal | No       | >= 0, max 10 digits + 2 decimals | 0       | Discount amount (validated <= totalCost)      |
| `paymentType`        | String     | **Yes**  | FULL, PHASED, or INSTALLMENT     | -       | Payment method                                |

### Field Validations

#### sourceTemplateCode

- ✅ Must exist in `treatment_plan_templates` table
- ✅ Template must be active (`is_active = true`)
- ❌ Validation fails if template not found or inactive

**Valid Values** (from seed data):

- `TPL_ORTHO_METAL` - Niềng răng mắc cài kim loại 2 năm (30,000,000đ)
- `TPL_IMPLANT_OSSTEM` - Cấy ghép Implant Osstem (19,000,000đ)
- `TPL_CROWN_CERCON` - Bọc răng sứ Cercon HT (5,000,000đ)

#### doctorEmployeeCode

- ✅ Must exist in `employees` table
- ✅ Employee must be active
- ❌ Validation fails if employee not found or inactive

**Valid Values** (from seed data):

- `EMP-001` - Bác sĩ Nguyễn Văn A
- `EMP-002` - Bác sĩ Trần Thị B
- `EMP-003` - Bác sĩ Phạm Văn C

#### planNameOverride

- Optional field
- If `null` or empty → System uses template name
- If provided → Uses this custom name

**Example**:

```json
// Uses template name "Niềng răng mắc cài kim loại trọn gói 2 năm"
"planNameOverride": null

// Uses custom name
"planNameOverride": "Lộ trình niềng răng cho BN Phong (Khuyến mãi 20%)"
```

#### discountAmount

- Must be >= 0
- Must be <= total cost (validated in service layer)
- Default: 0

**Example**:

```json
// No discount
"discountAmount": 0

// 5 million VND discount
"discountAmount": 5000000
```

#### paymentType

- **FULL**: Pay all at once upfront
- **PHASED**: Pay by phases (when completing each phase)
- **INSTALLMENT**: Pay in monthly/custom installments

---

## Response Model

### Response (201 CREATED)

Same structure as **API 5.2 - Get Treatment Plan Detail**

```json
{
  "planId": 11,
  "planCode": "PLAN-20251112-001",
  "planName": "Niềng răng mắc cài kim loại trọn gói 2 năm",
  "status": "PENDING",
  "doctor": {
    "employeeCode": "EMP-001",
    "fullName": "Bác sĩ Nguyễn Văn A"
  },
  "patient": {
    "patientCode": "BN-1001",
    "fullName": "Đoàn Thanh Phong"
  },
  "startDate": null,
  "expectedEndDate": null,
  "createdAt": "2025-11-12T10:30:00",
  "totalPrice": 30000000,
  "discountAmount": 0,
  "finalCost": 30000000,
  "paymentType": "INSTALLMENT",
  "progressSummary": {
    "totalPhases": 4,
    "completedPhases": 0,
    "totalItems": 31,
    "completedItems": 0,
    "progressPercentage": 0.0
  },
  "phases": [
    {
      "patientPhaseId": 101,
      "phaseNumber": 1,
      "phaseName": "Giai đoạn 1: Khám & Chuẩn bị",
      "status": "PENDING",
      "startDate": null,
      "completionDate": null,
      "estimatedDurationDays": 14,
      "items": [
        {
          "itemId": 201,
          "sequenceNumber": 1,
          "itemName": "Khám tổng quát và chụp X-quang",
          "status": "PENDING",
          "estimatedTimeMinutes": 30,
          "price": 500000,
          "completedAt": null,
          "linkedAppointments": []
        },
        {
          "itemId": 202,
          "sequenceNumber": 2,
          "itemName": "Lấy cao răng",
          "status": "PENDING",
          "estimatedTimeMinutes": 45,
          "price": 800000,
          "completedAt": null,
          "linkedAppointments": []
        }
        // ... more items
      ]
    },
    {
      "patientPhaseId": 102,
      "phaseNumber": 2,
      "phaseName": "Giai đoạn 2: Gắn mắc cài",
      "status": "PENDING",
      "startDate": null,
      "completionDate": null,
      "estimatedDurationDays": 1,
      "items": [
        {
          "itemId": 203,
          "sequenceNumber": 1,
          "itemName": "Lắp mắc cài kim loại hàm trên",
          "status": "PENDING",
          "estimatedTimeMinutes": 90,
          "price": 8000000,
          "completedAt": null,
          "linkedAppointments": []
        }
        // ... more items
      ]
    }
    // ... 2 more phases
  ]
}
```

### Key Response Fields

| Field        | Description                                             |
| ------------ | ------------------------------------------------------- |
| `planCode`   | Auto-generated unique code (PLAN-YYYYMMDD-XXX)          |
| `status`     | Always "PENDING" for newly created plans                |
| `startDate`  | `null` (not started yet - will be set when plan starts) |
| `totalPrice` | Copied from template's `total_price`                    |
| `finalCost`  | `totalPrice - discountAmount`                           |
| `phases`     | Cloned from template phases                             |
| `items`      | All items status = "PENDING"                            |

---

## Business Logic

### Step 1: Validate Patient

```java
Patient patient = patientRepository.findByPatientCode(patientCode)
  .orElseThrow(() -> new NotFoundException("PATIENT_NOT_FOUND"));

if (!patient.getIsActive()) {
  throw new BadRequestException("PATIENT_INACTIVE");
}
```

### Step 2: Validate Template

```java
TreatmentPlanTemplate template = templateRepository
  .findByTemplateCode(sourceTemplateCode)
  .orElseThrow(() -> new NotFoundException("TEMPLATE_NOT_FOUND"));

if (!template.getIsActive()) {
  throw new BadRequestException("TEMPLATE_INACTIVE");
}
```

### Step 3: Validate Doctor

```java
Employee doctor = employeeRepository.findByEmployeeCode(doctorEmployeeCode)
  .orElseThrow(() -> new NotFoundException("EMPLOYEE_NOT_FOUND"));

if (!doctor.getIsActive()) {
  throw new BadRequestException("EMPLOYEE_INACTIVE");
}
```

### Step 4: Validate Discount

```java
BigDecimal totalPrice = template.getTotalPrice();
BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;

if (discount.compareTo(totalPrice) > 0) {
  throw new BadRequestException("DISCOUNT_EXCEEDS_TOTAL");
}

BigDecimal finalCost = totalPrice.subtract(discount);
```

### Step 5: Generate Plan Code

```java
String planCode = generatePlanCode();
// Format: PLAN-YYYYMMDD-XXX
// Example: PLAN-20251112-001, PLAN-20251112-002, ...
```

**Logic**:

1. Get current date in format YYYYMMDD (e.g., 20251112)
2. Count existing plans created today
3. Increment counter (001, 002, 003, ...)
4. Combine: `PLAN-{YYYYMMDD}-{counter}`

### Step 6: Create Plan Record

```java
PatientTreatmentPlan plan = new PatientTreatmentPlan();
plan.setPlanCode(planCode);
plan.setPlanName(planNameOverride != null ? planNameOverride : template.getTemplateName());
plan.setPatient(patient);
plan.setCreatedBy(doctor);
plan.setStatus(TreatmentPlanStatus.PENDING);
plan.setTotalPrice(template.getTotalPrice());
plan.setDiscountAmount(discount);
plan.setFinalCost(finalCost);
plan.setPaymentType(paymentType);
plan.setExpectedEndDate(null); // Will be calculated from phase durations
plan = patientTreatmentPlanRepository.save(plan);
```

### Step 7: Clone Phases

```java
for (TemplatePhase templatePhase : template.getPhases()) {
  PatientPlanPhase phase = new PatientPlanPhase();
  phase.setPlan(plan);
  phase.setPhaseNumber(templatePhase.getPhaseNumber());
  phase.setPhaseName(templatePhase.getPhaseName());
  phase.setStatus(PhaseStatus.PENDING);
  phase.setEstimatedDurationDays(templatePhase.getEstimatedDurationDays());
  phase = patientPlanPhaseRepository.save(phase);

  // Clone items for this phase
  clonePhaseItems(phase, templatePhase);
}
```

### Step 8: Clone Items (with Quantity Expansion)

```java
void clonePhaseItems(PatientPlanPhase phase, TemplatePhase templatePhase) {
  int sequenceCounter = 1;

  for (TemplatePhaseService tps : templatePhase.getServices()) {
    int quantity = tps.getQuantity();

    for (int i = 1; i <= quantity; i++) {
      PatientPlanItem item = new PatientPlanItem();
      item.setPhase(phase);
      item.setService(tps.getService());
      item.setSequenceNumber(sequenceCounter++);

      // Item name logic
      if (quantity > 1) {
        item.setItemName(tps.getService().getServiceName() + " (Lần " + i + ")");
      } else {
        item.setItemName(tps.getService().getServiceName());
      }

      item.setStatus(PlanItemStatus.PENDING);
      item.setPrice(tps.getService().getPrice());
      item.setEstimatedTimeMinutes(tps.getService().getEstimatedTimeMinutes());

      patientPlanItemRepository.save(item);
    }
  }
}
```

**Example - Quantity Expansion**:

Template has:

- Service: "Điều chỉnh niềng răng" (quantity = 24)

System creates 24 items:

1. Điều chỉnh niềng răng (Lần 1)
2. Điều chỉnh niềng răng (Lần 2)
3. Điều chỉnh niềng răng (Lần 3)
   ...
4. Điều chỉnh niềng răng (Lần 24)

---

## Testing Guide

### Test 1: Create Plan from Template (Basic)

**Setup**: Use seed data template `TPL_ORTHO_METAL`

**Request**:

```bash
POST http://localhost:8080/api/v1/patients/BN-1001/treatment-plans
Authorization: Bearer {doctor_token}
Content-Type: application/json

{
  "sourceTemplateCode": "TPL_ORTHO_METAL",
  "doctorEmployeeCode": "EMP-001",
  "discountAmount": 0,
  "paymentType": "INSTALLMENT"
}
```

**Expected Response**:

- ✅ Status: 201 CREATED
- ✅ `planCode`: PLAN-20251112-XXX (auto-generated)
- ✅ `planName`: "Niềng răng mắc cài kim loại trọn gói 2 năm" (from template)
- ✅ `status`: "PENDING"
- ✅ `totalPrice`: 30000000
- ✅ `discountAmount`: 0
- ✅ `finalCost`: 30000000
- ✅ `phases`: 4 phases
- ✅ `items`: ~31 items total (depends on template structure)
- ✅ All items `status`: "PENDING"

**Database Verification**:

```sql
-- 1 plan record
SELECT * FROM patient_treatment_plans WHERE plan_code LIKE 'PLAN-20251112-%';

-- 4 phase records
SELECT * FROM patient_plan_phases WHERE plan_id = {new_plan_id};

-- 31 item records
SELECT COUNT(*) FROM patient_plan_items
WHERE phase_id IN (SELECT patient_phase_id FROM patient_plan_phases WHERE plan_id = {new_plan_id});
```

### Test 2: Create Plan with Discount

**Request**:

```json
{
  "sourceTemplateCode": "TPL_ORTHO_METAL",
  "doctorEmployeeCode": "EMP-001",
  "discountAmount": 5000000,
  "paymentType": "INSTALLMENT"
}
```

**Expected**:

- ✅ `totalPrice`: 30000000
- ✅ `discountAmount`: 5000000
- ✅ `finalCost`: 25000000

### Test 3: Create Plan with Custom Name

**Request**:

```json
{
  "sourceTemplateCode": "TPL_ORTHO_METAL",
  "doctorEmployeeCode": "EMP-001",
  "planNameOverride": "Lộ trình niềng răng cho BN Phong (Ưu đãi 20%)",
  "discountAmount": 6000000,
  "paymentType": "INSTALLMENT"
}
```

**Expected**:

- ✅ `planName`: "Lộ trình niềng răng cho BN Phong (Ưu đãi 20%)" (custom name used)
- ✅ NOT "Niềng răng mắc cài kim loại..." (template name ignored)

### Test 4: Invalid - Discount Exceeds Total

**Request**:

```json
{
  "sourceTemplateCode": "TPL_ORTHO_METAL",
  "doctorEmployeeCode": "EMP-001",
  "discountAmount": 35000000,
  "paymentType": "FULL"
}
```

**Expected**:

- ❌ Status: 400 BAD REQUEST
- Error: "DISCOUNT_EXCEEDS_TOTAL"

### Test 5: Invalid - Template Not Found

**Request**:

```json
{
  "sourceTemplateCode": "INVALID_TEMPLATE",
  "doctorEmployeeCode": "EMP-001",
  "discountAmount": 0,
  "paymentType": "FULL"
}
```

**Expected**:

- ❌ Status: 404 NOT FOUND
- Error: "TEMPLATE_NOT_FOUND"

### Test 6: Invalid - Patient Not Found

**Request**:

```bash
POST http://localhost:8080/api/v1/patients/INVALID-CODE/treatment-plans
```

**Expected**:

- ❌ Status: 404 NOT FOUND
- Error: "PATIENT_NOT_FOUND"

### Test 7: Invalid - Doctor Not Found

**Request**:

```json
{
  "sourceTemplateCode": "TPL_ORTHO_METAL",
  "doctorEmployeeCode": "INVALID-DOCTOR",
  "discountAmount": 0,
  "paymentType": "FULL"
}
```

**Expected**:

- ❌ Status: 404 NOT FOUND
- Error: "EMPLOYEE_NOT_FOUND"

---

## Error Handling

### Common Errors

| HTTP | Error Code             | Description                             |
| ---- | ---------------------- | --------------------------------------- |
| 404  | PATIENT_NOT_FOUND      | Patient code not found                  |
| 404  | TEMPLATE_NOT_FOUND     | Template code not found                 |
| 404  | EMPLOYEE_NOT_FOUND     | Doctor employee code not found          |
| 400  | PATIENT_INACTIVE       | Patient account is inactive             |
| 400  | TEMPLATE_INACTIVE      | Template is inactive/archived           |
| 400  | EMPLOYEE_INACTIVE      | Doctor account is inactive              |
| 400  | DISCOUNT_EXCEEDS_TOTAL | Discount amount > total price           |
| 403  | ACCESS_DENIED          | User doesn't have CREATE_TREATMENT_PLAN |
| 401  | UNAUTHORIZED           | Missing or invalid JWT token            |

### Error Response Format

```json
{
  "type": "https://www.jhipster.tech/problem/problem-with-message",
  "title": "Bad Request",
  "status": 400,
  "detail": "Discount amount cannot exceed total price",
  "path": "/api/v1/patients/BN-1001/treatment-plans",
  "message": "error.DISCOUNT_EXCEEDS_TOTAL",
  "errorCode": "DISCOUNT_EXCEEDS_TOTAL"
}
```

---

## Available Templates (Seed Data)

### TPL_ORTHO_METAL - Niềng răng mắc cài kim loại

**Price**: 30,000,000đ
**Duration**: 730 days (2 years)
**Phases**: 4

**Structure**:

1. Giai đoạn 1: Khám & Chuẩn bị (14 days)

   - Khám tổng quát và chụp X-quang
   - Lấy cao răng

2. Giai đoạn 2: Gắn mắc cài (1 day)

   - Lắp mắc cài kim loại hàm trên
   - Lắp mắc cài kim loại hàm dưới

3. Giai đoạn 3: Điều chỉnh định kỳ (715 days)

   - Điều chỉnh niềng răng (quantity = 24 → 24 items)

4. Giai đoạn 4: Tháo niềng & Duy trì (0 days)
   - Tháo niềng
   - Làm hàm duy trì

### TPL_IMPLANT_OSSTEM - Cấy ghép Implant

**Price**: 19,000,000đ
**Duration**: 180 days (6 months)
**Phases**: 3

### TPL_CROWN_CERCON - Bọc răng sứ

**Price**: 5,000,000đ
**Duration**: 7 days
**Phases**: 2

---

**Document Version**: 1.0
**Last Updated**: 2025-11-12
**Author**: Dental Clinic Development Team
**Verified Against**: TreatmentPlanController.java (line 159), CreateTreatmentPlanRequest.java, dental-clinic-seed-data.sql (lines 1703-1753)
