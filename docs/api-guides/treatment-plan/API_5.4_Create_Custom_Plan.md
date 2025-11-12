# API 5.4 Implementation Summary - Create Custom Treatment Plan

**Implementation Date**: 2025-01-12  
**Version**: V19  
**Status**: ✅ COMPLETE - Ready for Testing

---

## 📋 Overview

Implemented **API 5.4 - Create Custom Treatment Plan** with full quantity expansion, price override validation, and approval workflow features. This allows doctors to create customized treatment plans from scratch without using templates.

---

## 🎯 Key Features Implemented

### 1. **Quantity Expansion (P0 FIX)**
- Backend automatically expands `quantity: 5` into 5 separate `patient_plan_items`
- Auto-increments sequence numbers: 1, 2, 3, 4, 5 (fixes conflict issue)
- Adds suffix to item names: "Orthodontic Adjustment (Lần 1)", "Lần 2", etc.

**Code Example**:
```java
for (int i = 1; i <= itemReq.getQuantity(); i++) {
    String itemName = service.getServiceName();
    if (itemReq.getQuantity() > 1) {
        itemName += " (Lần " + i + ")";
    }
    PatientPlanItem item = PatientPlanItem.builder()
        .sequenceNumber(currentSequence++)  // ✅ Auto-increment fix
        .itemName(itemName)
        .price(itemReq.getPrice())
        .status(PlanItemStatus.PENDING)
        .build();
}
```

### 2. **Price Override with Validation (P0 FIX)**
- Allows doctors to customize prices (required flexibility)
- **Validation**: Price must be within 50%-150% of service default price
- Prevents both undercharging abuse and overcharging patients
- If out of range → throws `BadRequestAlertException` with clear message

**Code Example**:
```java
BigDecimal minPrice = servicePrice.multiply(new BigDecimal("0.5")); // 50%
BigDecimal maxPrice = servicePrice.multiply(new BigDecimal("1.5")); // 150%

if (requestPrice.compareTo(minPrice) < 0 || requestPrice.compareTo(maxPrice) > 0) {
    throw new BadRequestAlertException(
        String.format("Price for service %s (%s) is out of allowed range (%s - %s)",
            serviceCode, requestPrice, minPrice, maxPrice)
    );
}
```

### 3. **Approval Workflow (V19)**
- All custom plans created with `approval_status = DRAFT`
- Requires manager approval (API 5.9 - future work) before activation
- Provides control mechanism for price overrides

### 4. **Phase Duration Support (P1 FIX)**
- Added `estimatedDurationDays` field to phases
- Used for timeline calculation and patient communication

### 5. **Comprehensive Validations (P0 + P1 FIXES)**
- ✅ Patient exists and is active
- ✅ Doctor exists and is active
- ✅ Services exist and are active
- ✅ **Unique phase numbers** (no duplicates)
- ✅ **Each phase has at least 1 item** (no empty phases)
- ✅ **Quantity 1-100** (@Min, @Max annotations)
- ✅ **Discount ≤ total cost** (prevents negative final cost)
- ✅ **Price within 50%-150% range** (prevents abuse)

---

## 📁 Files Created/Modified

### ✅ New Files

1. **ApprovalStatus.java** (Enum)
   - Path: `.../treatment_plans/domain/ApprovalStatus.java`
   - Values: DRAFT, PENDING_REVIEW, APPROVED, REJECTED
   - Purpose: Workflow states for price approval

2. **V19__add_approval_workflow_and_phase_duration.sql** (Migration)
   - Path: `.../db/migration/V19__add_approval_workflow_and_phase_duration.sql`
   - Lines: 350+ lines with 8 steps
   - Changes:
     - Added `approval_status` ENUM type
     - Added 5 approval columns to `patient_treatment_plans`
     - Added `estimated_duration_days` to `patient_plan_phases`
     - Added `sequence_number` to `template_phase_services` (bug fix)
     - Updated `plan_item_status` ENUM (PENDING_APPROVAL → PENDING)
     - Backward compatibility migration for existing data

3. **CreateCustomPlanRequest.java** (DTO)
   - Path: `.../dto/request/CreateCustomPlanRequest.java`
   - Lines: 180+ lines
   - Structure: Main DTO + 2 nested classes (PhaseRequest, ItemRequest)
   - Key Fields:
     - `quantity` (1-100) - enables expansion
     - `estimatedDurationDays` (V19)
     - Full @Valid cascade validation

4. **CustomTreatmentPlanService.java** (Service Layer)
   - Path: `.../service/CustomTreatmentPlanService.java`
   - Lines: 320+ lines
   - Core Method: `createCustomPlan()`
   - Features:
     - Quantity expansion loop
     - Sequence number auto-increment
     - Price validation (50%-150%)
     - Discount validation
     - Unique phase number check
     - Batch insert support via Hibernate
     - Comprehensive error handling

### ✅ Modified Files

1. **PatientTreatmentPlan.java** (Entity)
   - Added 5 V19 fields:
     ```java
     private ApprovalStatus approvalStatus = ApprovalStatus.DRAFT;
     private LocalDateTime patientConsentDate;
     private Employee approvedBy;
     private LocalDateTime approvedAt;
     private String rejectionReason;
     ```

2. **PatientPlanPhase.java** (Entity)
   - Added 1 V19 field:
     ```java
     private Integer estimatedDurationDays; // V19 - for timeline calculation
     ```

3. **TreatmentPlanController.java** (Controller)
   - Added API 5.4 endpoint:
     ```java
     @PostMapping("/patients/{patientCode}/treatment-plans/custom")
     public ResponseEntity<TreatmentPlanDetailResponse> createCustomTreatmentPlan(
         @PathVariable String patientCode,
         @RequestBody @Valid CreateCustomPlanRequest request
     )
     ```
   - Comprehensive Swagger documentation
   - Example use cases in @Operation description

---

## 🔧 API Specification

### Endpoint
```
POST /api/v1/patients/{patientCode}/treatment-plans/custom
```

### Request Headers
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

### Request Body Example
```json
{
  "planName": "Custom Orthodontics Treatment",
  "doctorEmployeeCode": "EMP001",
  "discountAmount": 500000,
  "paymentType": "PHASED",
  "startDate": "2025-01-15",
  "expectedEndDate": "2026-01-15",
  "phases": [
    {
      "phaseNumber": 1,
      "phaseName": "Initial Consultation & X-rays",
      "estimatedDurationDays": 7,
      "items": [
        {
          "serviceCode": "ORTHO_CONSULT",
          "price": 300000,
          "sequenceNumber": 1,
          "quantity": 1
        },
        {
          "serviceCode": "XRAY_PANORAMIC",
          "price": 200000,
          "sequenceNumber": 2,
          "quantity": 1
        }
      ]
    },
    {
      "phaseNumber": 2,
      "phaseName": "Braces Adjustment",
      "estimatedDurationDays": 360,
      "items": [
        {
          "serviceCode": "ORTHO_ADJUST",
          "price": 500000,
          "sequenceNumber": 1,
          "quantity": 12
        }
      ]
    }
  ]
}
```

**Note**: `quantity: 12` will create 12 separate items with:
- Names: "Orthodontic Adjustment (Lần 1)", "Lần 2", ..., "Lần 12"
- Sequence numbers: 1, 2, 3, ..., 12
- Status: PENDING

### Response (201 CREATED)
```json
{
  "planCode": "PLAN-20250112-001",
  "planName": "Custom Orthodontics Treatment",
  "patient": {
    "patientCode": "BN-1001",
    "fullName": "Nguyễn Văn An"
  },
  "doctor": {
    "employeeCode": "EMP001",
    "fullName": "Dr. Trần Văn Bình"
  },
  "status": "PENDING",
  "approvalStatus": "DRAFT",
  "totalPrice": 6500000,
  "discountAmount": 500000,
  "finalCost": 6000000,
  "paymentType": "PHASED",
  "startDate": "2025-01-15",
  "expectedEndDate": "2026-01-15",
  "phases": [
    {
      "phaseNumber": 1,
      "phaseName": "Initial Consultation & X-rays",
      "status": "PENDING",
      "estimatedDurationDays": 7,
      "items": [
        {
          "sequenceNumber": 1,
          "itemName": "Orthodontic Consultation",
          "status": "PENDING",
          "price": 300000
        },
        {
          "sequenceNumber": 2,
          "itemName": "Panoramic X-ray",
          "status": "PENDING",
          "price": 200000
        }
      ]
    },
    {
      "phaseNumber": 2,
      "phaseName": "Braces Adjustment",
      "status": "PENDING",
      "estimatedDurationDays": 360,
      "items": [
        {
          "sequenceNumber": 1,
          "itemName": "Orthodontic Adjustment (Lần 1)",
          "status": "PENDING",
          "price": 500000
        },
        {
          "sequenceNumber": 2,
          "itemName": "Orthodontic Adjustment (Lần 2)",
          "status": "PENDING",
          "price": 500000
        },
        // ... items 3-11 omitted for brevity
        {
          "sequenceNumber": 12,
          "itemName": "Orthodontic Adjustment (Lần 12)",
          "status": "PENDING",
          "price": 500000
        }
      ]
    }
  ],
  "progressSummary": {
    "totalPhases": 2,
    "completedPhases": 0,
    "totalItems": 14,
    "completedItems": 0,
    "completionPercentage": 0
  },
  "createdAt": "2025-01-12T10:30:00"
}
```

---

## ✅ Validations Implemented

### Request Validation (@Valid Annotations)
| Field | Validation | Error Message |
|-------|-----------|---------------|
| planName | @NotBlank | "Plan name is required" |
| phases | @NotEmpty | "Must have at least 1 phase" |
| quantity | @Min(1), @Max(100) | "Quantity must be 1-100" |
| price | @DecimalMin("0.01") | "Price must be positive" |
| discountAmount | @DecimalMin("0") | "Discount cannot be negative" |

### Business Logic Validation
| Validation | Implementation | Error Code |
|-----------|----------------|-----------|
| Patient exists | `patientRepository.findOneByPatientCode()` | `patientNotFound` |
| Doctor exists & active | `employeeRepository.findOneByEmployeeCode()` + `isActive` check | `doctorNotFound` / `doctorInactive` |
| Service exists & active | `serviceRepository.findByServiceCode()` + `isActive` check | `serviceNotFound` / `serviceInactive` |
| Unique phase numbers | `HashSet.add()` duplicate check | `duplicatePhaseNumber` |
| Phase has items | `phase.getItems().isEmpty()` check | `phaseWithoutItems` |
| Price range | `50% <= price <= 150%` of service default | `priceOutOfRange` |
| Discount validation | `discountAmount <= totalCost` | `discountExceedsCost` |

---

## 🔒 Security & Permissions

### Required Permission
```java
@PreAuthorize("hasAuthority('CREATE_TREATMENT_PLAN')")
```

### Typical Roles with Permission
- **ROLE_DOCTOR** ✅ (assigned to patients)
- **ROLE_MANAGER** ✅ (full access)
- **ROLE_ADMIN** ✅ (full access)
- **ROLE_PATIENT** ❌ (cannot create plans)
- **ROLE_RECEPTIONIST** ❌ (view only)

---

## 📊 Database Schema Changes (V19)

### Table: `patient_treatment_plans`
```sql
-- Approval workflow columns (NEW)
approval_status        approval_status NOT NULL DEFAULT 'DRAFT',
patient_consent_date   TIMESTAMP NULL,
approved_by            INTEGER NULL,  -- FK to employees
approved_at            TIMESTAMP NULL,
rejection_reason       TEXT NULL
```

### Table: `patient_plan_phases`
```sql
-- Duration for timeline calculation (NEW)
estimated_duration_days  INTEGER NULL
```

### Table: `template_phase_services`
```sql
-- Bug fix: sequence number for template items (NEW)
sequence_number  INTEGER NOT NULL DEFAULT 0
```

### Enum Updates
```sql
-- NEW ENUM TYPE
CREATE TYPE approval_status AS ENUM ('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED');

-- UPDATED ENUM (safe migration)
-- OLD: READY_FOR_BOOKING, SCHEDULED, PENDING_APPROVAL, COMPLETED, CANCELLED
-- NEW: READY_FOR_BOOKING, SCHEDULED, PENDING, IN_PROGRESS, COMPLETED
-- Removed: PENDING_APPROVAL, CANCELLED
```

---

## 🧪 Testing Scenarios

### Test Case 1: Basic Custom Plan (Success)
```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/custom" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "planName": "Simple Checkup Plan",
    "doctorEmployeeCode": "EMP001",
    "discountAmount": 0,
    "paymentType": "FULL",
    "phases": [{
      "phaseNumber": 1,
      "phaseName": "Checkup",
      "estimatedDurationDays": 1,
      "items": [{
        "serviceCode": "CHECKUP_GENERAL",
        "price": 200000,
        "sequenceNumber": 1,
        "quantity": 1
      }]
    }]
  }'
```

**Expected**: Status 201, plan created with approval_status=DRAFT

### Test Case 2: Quantity Expansion (Success)
```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/custom" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "planName": "Orthodontics Adjustment Plan",
    "doctorEmployeeCode": "EMP001",
    "discountAmount": 500000,
    "paymentType": "PHASED",
    "phases": [{
      "phaseNumber": 1,
      "phaseName": "Monthly Adjustments",
      "estimatedDurationDays": 365,
      "items": [{
        "serviceCode": "ORTHO_ADJUST",
        "price": 500000,
        "sequenceNumber": 1,
        "quantity": 12
      }]
    }]
  }'
```

**Expected**: Status 201, 12 items created with sequence 1-12, names "Lần 1" to "Lần 12"

### Test Case 3: Duplicate Phase Number (Validation Error)
```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/custom" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "planName": "Invalid Plan",
    "doctorEmployeeCode": "EMP001",
    "discountAmount": 0,
    "paymentType": "FULL",
    "phases": [
      {"phaseNumber": 1, "phaseName": "Phase 1", "estimatedDurationDays": 7, "items": [...]},
      {"phaseNumber": 1, "phaseName": "Phase 2", "estimatedDurationDays": 7, "items": [...]}
    ]
  }'
```

**Expected**: Status 400, error "Duplicate phase number: 1"

### Test Case 4: Price Out of Range (Validation Error)
```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/custom" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "planName": "Expensive Plan",
    "doctorEmployeeCode": "EMP001",
    "discountAmount": 0,
    "paymentType": "FULL",
    "phases": [{
      "phaseNumber": 1,
      "phaseName": "Checkup",
      "estimatedDurationDays": 1,
      "items": [{
        "serviceCode": "CHECKUP_GENERAL",
        "price": 1000000,
        "sequenceNumber": 1,
        "quantity": 1
      }]
    }]
  }'
```

**Expected**: Status 400, error "Price for service CHECKUP_GENERAL (1000000) is out of allowed range (100000 - 300000)"
*(Assuming service default price is 200000)*

### Test Case 5: Discount Exceeds Total (Validation Error)
```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/custom" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "planName": "Over-discounted Plan",
    "doctorEmployeeCode": "EMP001",
    "discountAmount": 5000000,
    "paymentType": "FULL",
    "phases": [{
      "phaseNumber": 1,
      "phaseName": "Checkup",
      "estimatedDurationDays": 1,
      "items": [{
        "serviceCode": "CHECKUP_GENERAL",
        "price": 200000,
        "sequenceNumber": 1,
        "quantity": 1
      }]
    }]
  }'
```

**Expected**: Status 400, error "Discount amount (5000000) cannot exceed total cost (200000)"

---

## 🔄 Workflow Integration

### Current API 5.4 Position in Full Workflow

```
┌─────────────────────────────────────────────────────────────┐
│                 Treatment Plan Lifecycle                    │
└─────────────────────────────────────────────────────────────┘

1. ✅ [API 5.3] Create from Template (status=PENDING, approval=APPROVED)
   OR
2. ✅ [API 5.4] Create Custom (status=PENDING, approval=DRAFT) ← CURRENT

3. ⏳ [API 5.9] Manager Approval (DRAFT → APPROVED)           ← FUTURE

4. ⏳ [API 5.5] Activate Plan (PENDING → ACTIVE)             ← EXISTING

5. ⏳ [API 5.6] Schedule Items (link to appointments)         ← EXISTING

6. ⏳ [API 5.7] Update Progress (mark items COMPLETED)        ← EXISTING

7. ⏳ [API 5.8] Complete Plan (all items done → COMPLETED)    ← EXISTING
```

### Next Required API: **5.9 - Approve/Reject Custom Plan**

**Endpoint**: `POST /api/v1/treatment-plans/{planCode}/approval`

**Request Body**:
```json
{
  "action": "APPROVE",  // or "REJECT"
  "patientConsentDate": "2025-01-12T10:00:00",
  "rejectionReason": null  // Required if action=REJECT
}
```

**Business Logic**:
- Check `approval_status = PENDING_REVIEW`
- Validate user has `APPROVE_TREATMENT_PLAN` permission (Manager only)
- If APPROVE: Set `approved_by`, `approved_at`, `approval_status=APPROVED`
- If REJECT: Set `rejection_reason`, `approval_status=REJECTED`, `status=CANCELLED`

---

## 📈 Performance Considerations

### Batch Insert Optimization
- Uses Hibernate cascade with `CascadeType.ALL`
- Single transaction saves: plan → phases → items
- Hibernate batches SQL INSERTs automatically (configured in application.yaml)

### Current Configuration (Check `application.yaml`):
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20  # Batch 20 INSERT statements
        order_inserts: true
        order_updates: true
```

### Performance Test Results (Expected):
| Scenario | Items | Queries (without batch) | Queries (with batch) |
|---------|-------|--------------------------|---------------------|
| Small Plan | 5 items | ~11 queries | ~3 queries |
| Medium Plan | 20 items | ~41 queries | ~5 queries |
| Large Plan | 100 items | ~201 queries | ~10 queries |

---

## 🐛 Known Issues & Limitations

### Current Limitations
1. **No RBAC for assigned doctor check**: Currently any doctor with `CREATE_TREATMENT_PLAN` can create plan for any patient. P1 fix pending.
2. **No manager notification**: When plan is created with approval_status=DRAFT, no notification sent to managers. Feature pending.
3. **No price history tracking**: Price overrides are logged but not stored in separate audit table. Future enhancement.

### Future Enhancements (Not in Scope)
- **API 5.9**: Approval/Rejection endpoint
- **API 5.10**: Edit custom plan (while approval_status=DRAFT)
- **API 5.11**: Clone plan to another patient
- **Price History Audit Table**: Track all price changes
- **Manager Dashboard**: List all pending approval plans

---

## ✅ Checklist - Implementation Complete

- [x] Created ApprovalStatus enum (4 states)
- [x] Created V19 migration script (8 steps, 350+ lines)
- [x] Updated PatientTreatmentPlan entity (5 new fields)
- [x] Updated PatientPlanPhase entity (1 new field)
- [x] Created CreateCustomPlanRequest DTO (3 nested classes, full validation)
- [x] Implemented CustomTreatmentPlanService (320+ lines)
  - [x] Quantity expansion loop with sequence auto-increment (P0 fix)
  - [x] Price validation 50%-150% (P0 fix)
  - [x] Unique phase number validation (P0 fix)
  - [x] Discount validation (P0 fix)
  - [x] Empty phase validation (P1 fix)
  - [x] Patient/Doctor/Service existence checks
  - [x] Approval workflow integration (approval_status=DRAFT)
  - [x] Comprehensive error handling
  - [x] Batch insert support
- [x] Added Controller endpoint with Swagger docs
- [x] All P0 fixes implemented
- [x] All P1 fixes implemented
- [x] Compile errors resolved
- [x] Documentation created

---

## 📝 Next Steps

### Immediate (Testing Phase)
1. **Run V19 Migration**
   ```bash
   docker exec -i postgres-dental psql -U root -d dental_clinic_db \
     < src/main/resources/db/migration/V19__add_approval_workflow_and_phase_duration.sql
   ```

2. **Restart Application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. **Test API 5.4**
   - Test Case 1: Basic plan (1 phase, 1 item)
   - Test Case 2: Quantity expansion (quantity=5)
   - Test Case 3: Multiple phases
   - Test Case 4: Validation errors (duplicate phase, price out of range)

4. **Verify Database**
   ```sql
   -- Check plan created
   SELECT plan_code, plan_name, approval_status FROM patient_treatment_plans 
   WHERE plan_code LIKE 'PLAN-%' ORDER BY created_at DESC LIMIT 5;

   -- Check items expanded
   SELECT ppt.plan_code, ppp.phase_number, ppi.sequence_number, ppi.item_name, ppi.price
   FROM patient_plan_items ppi
   JOIN patient_plan_phases ppp ON ppi.phase_id = ppp.phase_id
   JOIN patient_treatment_plans ppt ON ppp.plan_id = ppt.plan_id
   WHERE ppt.plan_code = 'PLAN-20250112-001'
   ORDER BY ppp.phase_number, ppi.sequence_number;
   ```

### Medium-term (API 5.9 Implementation)
1. Create `ApproveTreatmentPlanRequest` DTO
2. Add `approvePlan()` method to service
3. Add approval endpoint to controller
4. Implement manager permission check
5. Add notification system (email/in-app)

### Long-term (Enhancements)
1. Add price history audit table
2. Implement edit custom plan feature
3. Create manager approval dashboard
4. Add plan cloning feature
5. Performance monitoring and optimization

---

**Document Version**: 1.0  
**Last Updated**: 2025-01-12  
**Author**: GitHub Copilot  
**Review Status**: Ready for QA Testing
