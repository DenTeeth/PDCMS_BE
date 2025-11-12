# API 5.3 - Create Treatment Plan from Template

**Module**: Treatment Plan Management  
**Version**: V1.0  
**Status**: ✅ Production Ready  
**Last Updated**: 2025-11-12

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [API Specification](#api-specification)
3. [Template System](#template-system)
4. [Business Logic](#business-logic)
5. [Response Models](#response-models)
6. [Error Handling](#error-handling)
7. [Testing Guide](#testing-guide)

---

## Overview

API 5.3 allows creating a patient treatment plan by applying a pre-defined template. The system automatically clones the template structure (phases → services) into patient-specific plan items.

### Permissions

| Permission              | Role          | Description                             |
|-------------------------|---------------|-----------------------------------------|
| `CREATE_TREATMENT_PLAN` | Doctor, Staff | Create treatment plans for patients     |

### Key Features

✅ Template-based plan creation (standardized treatments)  
✅ Automatic phase and item generation from template  
✅ Quantity expansion: `quantity: 24` → 24 separate items  
✅ Price customization with validation (50%-150% range)  
✅ Approval workflow (DRAFT status)  
✅ Transactional safety (rollback on errors)  

---

## API Specification

### Endpoint

```
POST /api/v1/patient-treatment-plans/from-template
```

### Request Body

```json
{
  "patientCode": "BN-1001",
  "templateId": 1,
  "startDate": "2025-11-01",
  "totalPrice": 35000000,
  "discountAmount": 0,
  "paymentType": "INSTALLMENT",
  "notes": "Patient requested metal braces, 24-month payment plan"
}
```

### Request Fields

| Field            | Type       | Required | Constraints           | Description                          |
|------------------|------------|----------|-----------------------|--------------------------------------|
| `patientCode`    | String     | Yes      | Valid patient code    | Patient business code                |
| `templateId`     | Long       | Yes      | Must exist, active    | Treatment plan template ID           |
| `startDate`      | LocalDate  | Yes      | Not in past           | Treatment start date                 |
| `totalPrice`     | BigDecimal | No       | ≥ 0                   | Override template price (optional)   |
| `discountAmount` | BigDecimal | No       | ≥ 0, ≤ totalPrice     | Discount amount                      |
| `paymentType`    | String     | Yes      | FULL/INSTALLMENT      | Payment method                       |
| `notes`          | String     | No       | Max 1000 chars        | Additional notes                     |

### Response (201 CREATED)

```json
{
  "planId": 1,
  "planCode": "PLAN-20251101-001",
  "planName": "Lộ trình Niềng răng Mắc cài Kim loại",
  "status": "IN_PROGRESS",
  "approvalStatus": "APPROVED",
  "startDate": "2025-11-01",
  "expectedEndDate": "2027-11-01",
  "totalPrice": 35000000,
  "discountAmount": 0,
  "finalCost": 35000000,
  "paymentType": "INSTALLMENT",
  "patient": {
    "patientId": 1,
    "patientName": "Đoàn Thanh Phong",
    "patientCode": "BN-1001"
  },
  "sourceTemplate": {
    "templateId": 1,
    "templateName": "Niềng răng Mắc cài Kim loại",
    "templateCode": "TPL-ORTHO-01"
  },
  "phases": [
    {
      "patientPhaseId": 1,
      "phaseNumber": 1,
      "phaseName": "Giai đoạn 1: Chuẩn bị và Kiểm tra",
      "status": "PENDING",
      "estimatedDurationDays": 7,
      "itemCount": 3
    },
    {
      "patientPhaseId": 2,
      "phaseNumber": 2,
      "phaseName": "Giai đoạn 2: Lắp Mắc cài",
      "status": "PENDING",
      "estimatedDurationDays": 60,
      "itemCount": 4
    },
    {
      "patientPhaseId": 3,
      "phaseNumber": 3,
      "phaseName": "Giai đoạn 3: Điều chỉnh định kỳ (24 tháng)",
      "status": "PENDING",
      "estimatedDurationDays": 720,
      "itemCount": 24
    }
  ],
  "createdBy": {
    "employeeId": 1,
    "employeeName": "Bác sĩ Nguyễn Văn A",
    "employeeCode": "EMP-1"
  },
  "createdAt": "2025-11-01T10:00:00"
}
```

---

## Template System

### Template Structure

```
Treatment Plan Template
├── Template Metadata (name, price, duration)
├── Phase 1
│   ├── Service A (quantity: 1)
│   └── Service B (quantity: 2)
├── Phase 2
│   └── Service C (quantity: 24)
└── Phase 3
    └── Service D (quantity: 1)
```

### Example Template: Orthodontics (Niềng răng)

**Template ID**: 1  
**Name**: Niềng răng Mắc cài Kim loại  
**Duration**: 730 days (24 months)  
**Price**: 35,000,000 VNĐ  

**Phase 1: Chuẩn bị (7 days)**
- Khám tổng quát × 1 (500,000đ)
- Lấy cao răng × 1 (800,000đ)
- Hàn trám răng sâu × 1 (1,500,000đ)

**Phase 2: Lắp mắc cài (1 day)**
- Lắp mắc cài hàm trên × 1 (8,000,000đ)
- Lắp mắc cài hàm dưới × 1 (8,000,000đ)

**Phase 3: Điều chỉnh định kỳ (720 days)**
- Siết niềng × 24 (500,000đ each)

**Phase 4: Hoàn tất (1 day)**
- Tháo niềng × 1 (1,000,000đ)
- Làm hàm duy trì × 1 (3,000,000đ)

**Total Items**: 31 items  
**Total Cost**: 35,000,000đ

### Cloning Process

When template is applied:

1. **Create Plan Record**
   - Generate plan_code (PLAN-YYYYMMDD-XXX)
   - Copy template name, description
   - Set status = IN_PROGRESS
   - Set approval_status = APPROVED (for templates)

2. **Clone Phases**
   ```sql
   INSERT INTO patient_plan_phases (
     plan_id, phase_number, phase_name, 
     estimated_duration_days, status
   )
   SELECT 
     {new_plan_id}, phase_number, phase_name,
     estimated_duration_days, 'PENDING'
   FROM template_phases
   WHERE template_id = {template_id}
   ```

3. **Expand Services into Items**
   ```java
   // For each template_phase_service
   for (int i = 1; i <= quantity; i++) {
     PatientPlanItem item = new PatientPlanItem();
     item.setItemName(serviceName + " (Lần " + i + ")");
     item.setSequenceNumber(currentSequence++);
     item.setStatus(PlanItemStatus.PENDING);
     item.setPrice(servicePrice);
     // ... save item
   }
   ```

4. **Calculate End Date**
   ```java
   LocalDate endDate = startDate.plusDays(totalEstimatedDays);
   ```

---

## Business Logic

### Validation Flow

**STEP 1: Validate Patient**
```java
Patient patient = patientRepository.findByPatientCode(patientCode)
  .orElseThrow(() -> new NotFoundException("PATIENT_NOT_FOUND"));

if (!patient.getIsActive()) {
  throw new BadRequestException("PATIENT_INACTIVE");
}
```

**STEP 2: Validate Template**
```java
TreatmentPlanTemplate template = templateRepository.findById(templateId)
  .orElseThrow(() -> new NotFoundException("TEMPLATE_NOT_FOUND"));

if (!template.getIsActive()) {
  throw new BadRequestException("TEMPLATE_INACTIVE");
}
```

**STEP 3: Validate Financial Data**
```java
BigDecimal finalPrice = totalPrice != null ? totalPrice : template.getTotalPrice();
BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;

if (discount.compareTo(finalPrice) > 0) {
  throw new BadRequestException("DISCOUNT_EXCEEDS_TOTAL");
}

BigDecimal finalCost = finalPrice.subtract(discount);
if (finalCost.compareTo(BigDecimal.ZERO) < 0) {
  throw new BadRequestException("INVALID_FINAL_COST");
}
```

**STEP 4: Check for Active Plans**
```java
// Optional: Prevent duplicate active plans for same patient
long activePlans = planRepository.countByPatientIdAndStatus(
  patient.getPatientId(), 
  TreatmentPlanStatus.IN_PROGRESS
);

if (activePlans > 0) {
  log.warn("Patient {} already has {} active plan(s)", patientCode, activePlans);
  // Allow or reject based on business rules
}
```

**STEP 5: Generate Plan Code**
```java
String planCode = generatePlanCode(startDate);
// Format: PLAN-YYYYMMDD-XXX (auto-increment XXX within same day)
```

**STEP 6: Create Plan + Clone Structure**
```java
@Transactional
public TreatmentPlanDTO createFromTemplate(CreateFromTemplateRequest request) {
  // Create plan record
  PatientTreatmentPlan plan = new PatientTreatmentPlan();
  plan.setPlanCode(planCode);
  plan.setPatient(patient);
  plan.setSourceTemplate(template);
  plan.setStatus(TreatmentPlanStatus.IN_PROGRESS);
  plan.setApprovalStatus(ApprovalStatus.APPROVED); // Auto-approved for templates
  plan = planRepository.save(plan);
  
  // Clone phases
  for (TemplatePhase templatePhase : template.getPhases()) {
    PatientPlanPhase phase = clonePhase(plan, templatePhase);
    phaseRepository.save(phase);
    
    // Expand services into items
    for (TemplatePhaseService tps : templatePhase.getServices()) {
      expandServiceIntoItems(phase, tps);
    }
  }
  
  return mapToDTO(plan);
}
```

### Quantity Expansion Logic

**Example**: Service "Siết niềng" with quantity = 24

**Template Phase Service**:
```json
{
  "serviceId": 39,
  "serviceName": "Điều chỉnh niềng răng",
  "quantity": 24,
  "price": 500000
}
```

**Expanded Patient Plan Items** (24 records):
```json
[
  {
    "itemId": 101,
    "sequenceNumber": 1,
    "itemName": "Điều chỉnh niềng răng (Lần 1)",
    "status": "PENDING",
    "price": 500000
  },
  {
    "itemId": 102,
    "sequenceNumber": 2,
    "itemName": "Điều chỉnh niềng răng (Lần 2)",
    "status": "PENDING",
    "price": 500000
  },
  // ... 22 more items ...
  {
    "itemId": 124,
    "sequenceNumber": 24,
    "itemName": "Điều chỉnh niềng răng (Lần 24)",
    "status": "PENDING",
    "price": 500000
  }
]
```

**Benefits**:
- ✅ Each appointment can be tracked individually
- ✅ Clear progress visualization (3/24 completed)
- ✅ Flexible scheduling (book any available session)
- ✅ Accurate completion percentage calculation

---

## Response Models

### CreateFromTemplateRequest

```java
{
  "patientCode": String,         // Required
  "templateId": Long,             // Required
  "startDate": LocalDate,         // Required
  "totalPrice": BigDecimal,       // Optional (defaults to template price)
  "discountAmount": BigDecimal,   // Optional (defaults to 0)
  "paymentType": String,          // Required: FULL or INSTALLMENT
  "notes": String                 // Optional
}
```

### TreatmentPlanDTO (Response)

```java
{
  "planId": Long,
  "planCode": String,
  "planName": String,
  "status": String,
  "approvalStatus": String,
  "startDate": LocalDate,
  "expectedEndDate": LocalDate,
  "totalPrice": BigDecimal,
  "discountAmount": BigDecimal,
  "finalCost": BigDecimal,
  "paymentType": String,
  "patient": PatientSummaryDTO,
  "sourceTemplate": TemplateSummaryDTO,
  "phases": List<PhaseSummaryDTO>,
  "createdBy": EmployeeSummaryDTO,
  "createdAt": LocalDateTime
}
```

---

## Error Handling

### Common Errors

| HTTP | Error Code              | Description                              |
|------|-------------------------|------------------------------------------|
| 404  | PATIENT_NOT_FOUND       | Patient code not found                   |
| 404  | TEMPLATE_NOT_FOUND      | Template ID not found                    |
| 400  | PATIENT_INACTIVE        | Patient account is inactive              |
| 400  | TEMPLATE_INACTIVE       | Template is inactive/archived            |
| 400  | DISCOUNT_EXCEEDS_TOTAL  | Discount > total price                   |
| 400  | INVALID_START_DATE      | Start date is in the past                |
| 400  | MISSING_TEMPLATE_PHASES | Template has no phases                   |
| 409  | PLAN_CODE_CONFLICT      | Plan code already exists (rare)          |

### Error Response Example

```json
{
  "type": "https://www.jhipster.tech/problem/problem-with-message",
  "title": "Bad Request",
  "status": 400,
  "detail": "Discount amount cannot exceed total price",
  "path": "/api/v1/patient-treatment-plans/from-template",
  "message": "error.DISCOUNT_EXCEEDS_TOTAL",
  "errorCode": "DISCOUNT_EXCEEDS_TOTAL"
}
```

---

## Testing Guide

### Test Scenario 1: Create Plan from Template

**Setup**:
- Patient BN-1001 exists and is active
- Template ID 1 (Niềng răng) exists with 4 phases, 31 items

**Steps**:
```bash
curl -X POST "http://localhost:8080/api/v1/patient-treatment-plans/from-template" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <doctor-token>" \
  -d '{
    "patientCode": "BN-1001",
    "templateId": 1,
    "startDate": "2025-11-01",
    "paymentType": "INSTALLMENT",
    "notes": "Patient prefers installment payment"
  }'
```

**Verify**:
1. Response 201 CREATED
2. Database checks:
   ```sql
   -- 1 plan record
   SELECT * FROM patient_treatment_plans WHERE plan_code = 'PLAN-20251101-001';
   
   -- 4 phase records
   SELECT * FROM patient_plan_phases WHERE plan_id = {new_plan_id};
   
   -- 31 item records (all status = PENDING)
   SELECT * FROM patient_plan_items 
   WHERE phase_id IN (SELECT patient_phase_id FROM patient_plan_phases WHERE plan_id = {new_plan_id});
   ```

**Expected**:
- ✅ Plan created with status = IN_PROGRESS
- ✅ Approval status = APPROVED (auto-approved for templates)
- ✅ 31 items with status = PENDING
- ✅ Expected end date = start date + 730 days

### Test Scenario 2: Price Override

**Steps**:
```bash
curl -X POST ".../from-template" \
  -d '{
    "patientCode": "BN-1001",
    "templateId": 1,
    "startDate": "2025-11-01",
    "totalPrice": 32000000,
    "discountAmount": 2000000,
    "paymentType": "INSTALLMENT"
  }'
```

**Expected**:
- ✅ Plan created with totalPrice = 32,000,000đ
- ✅ Discount = 2,000,000đ
- ✅ Final cost = 30,000,000đ

### Test Scenario 3: Invalid Discount

**Steps**:
```bash
curl -X POST ".../from-template" \
  -d '{
    "patientCode": "BN-1001",
    "templateId": 1,
    "totalPrice": 30000000,
    "discountAmount": 35000000,
    "paymentType": "FULL"
  }'
```

**Expected**:
- ❌ 400 BAD_REQUEST
- Error: "DISCOUNT_EXCEEDS_TOTAL"

### Test Scenario 4: Inactive Patient

**Setup**:
- Patient BN-1002 has `is_active = false`

**Steps**:
```bash
curl -X POST ".../from-template" \
  -d '{
    "patientCode": "BN-1002",
    "templateId": 1,
    "startDate": "2025-11-01",
    "paymentType": "FULL"
  }'
```

**Expected**:
- ❌ 400 BAD_REQUEST
- Error: "PATIENT_INACTIVE"

### Test Scenario 5: Quantity Expansion Verification

**Setup**:
- Template phase 3 has service with quantity = 24

**Steps**:
1. Create plan from template
2. Query items:
   ```sql
   SELECT item_id, sequence_number, item_name, status
   FROM patient_plan_items
   WHERE phase_id = {phase_3_id}
   ORDER BY sequence_number;
   ```

**Expected**:
- ✅ 24 items returned
- ✅ Sequence numbers: 1, 2, 3, ..., 24
- ✅ Item names: "Service (Lần 1)", "Service (Lần 2)", ..., "Service (Lần 24)"
- ✅ All status = PENDING

### Test Scenario 6: Transactional Rollback

**Setup**:
- Template has valid phases
- Database trigger will fail during item creation (simulate error)

**Steps**:
1. Create plan (will fail mid-transaction)
2. Verify database state

**Expected**:
- ❌ 500 INTERNAL_SERVER_ERROR
- ✅ No plan record created (rollback)
- ✅ No phase records created (rollback)
- ✅ No item records created (rollback)
- ✅ Database remains consistent

---

## Integration Notes

### Appointment Booking Integration

After plan creation, items are ready for booking:

```bash
# Step 1: Get bookable items
GET /api/v1/patient-treatment-plans/{planId}/bookable-items

# Step 2: Book appointment with item IDs
POST /api/v1/appointments
{
  "patientCode": "BN-1001",
  "patientPlanItemIds": [101, 102],  // Book first 2 adjustments
  "employeeCode": "EMP-1",
  "roomCode": "RM-01",
  "appointmentStartTime": "2025-11-15T14:00:00"
}
```

### Status Transitions

```
Plan Created → All items PENDING
                ↓ (After approval - for templates: instant)
              Items → READY_FOR_BOOKING
                ↓ (When appointment booked)
              Items → SCHEDULED
                ↓ (When appointment starts)
              Items → IN_PROGRESS
                ↓ (When appointment completes)
              Items → COMPLETED
```

---

## Performance Considerations

**Database Queries**:
- 1 query: Validate patient
- 1 query: Validate template
- 1 query: Insert plan
- N queries: Insert phases (where N = number of phases)
- M queries: Insert items (where M = total items with quantity expansion)

**Optimization**:
- Use batch insert for items (JDBC batch size = 50)
- Transaction boundary: Entire operation (all-or-nothing)
- Expected execution time: < 500ms for typical plan (4 phases, 30 items)

**Resource Usage**:
- Memory: ~1MB per plan creation request
- CPU: Minimal (mostly database I/O)
- Database locks: Row-level locks on plan/phase/item tables

---

**Document Version**: 1.0  
**Last Updated**: 2025-11-12  
**Author**: Dental Clinic Development Team
