# Treatment Plan API - Complete Frontend Integration Guide

**Version**: 2.0  
**Date**: 2025-12-02  
**Status**: Production Ready  
**Critical Update**: Fixed all enum values and field names

---

## CRITICAL FIXES - READ THIS FIRST

### Issue #1: PaymentType Enum Values

**WRONG** (Old documentation):
```json
{
  "paymentType": "FULL_PAYMENT"  // ERROR - Will cause 500
}
```

**CORRECT** (Current implementation):
```json
{
  "paymentType": "FULL"  // Valid values: FULL, PHASED, INSTALLMENT
}
```

**Valid Values**:
- `FULL` - Full payment upfront
- `PHASED` - Payment by phases (pay for each phase when completed)
- `INSTALLMENT` - Installment payment (monthly payments)

### Issue #2: Field Name in Create from Template

**WRONG**:
```json
{
  "templateCode": "TPL_ORTHO_METAL"  // Field doesn't exist
}
```

**CORRECT**:
```json
{
  "sourceTemplateCode": "TPL_ORTHO_METAL"  // Correct field name
}
```

### Issue #3: TreatmentPlanStatus Values

**Database Constraint Fixed** - Now supports all status values:
- `PENDING` - Plan created but not yet started
- `IN_PROGRESS` - Plan is currently being executed
- `COMPLETED` - All phases and items completed successfully
- `CANCELLED` - Plan was cancelled before completion

**Previous Issue**: Database constraint was missing `PENDING`, causing 500 errors when creating plans.  
**Status**: FIXED on 2025-12-02

---

## Quick Reference - All APIs

### Core APIs

| API | Method | Endpoint | Purpose |
|-----|--------|----------|---------|
| 5.1 | GET | `/api/v1/treatment-plans` | List all treatment plans (paginated) |
| 5.2 | GET | `/api/v1/patients/{patientCode}/treatment-plans/{planCode}` | Get plan detail with phases/items |
| 5.3 | POST | `/api/v1/patients/{patientCode}/treatment-plans` | Create plan from template |
| 5.4 | POST | `/api/v1/patients/{patientCode}/treatment-plans/custom` | Create custom plan |
| 5.5 | GET | `/api/v1/patient-treatment-plans` | Get all plans with RBAC filtering |
| 5.6 | PATCH | `/api/v1/patient-plan-items/{itemId}/status` | Update item status |
| 5.7 | POST | `/api/v1/patient-plan-phases/{phaseId}/items` | Add items to phase |
| 5.8 | GET | `/api/v1/treatment-plan-templates/{templateCode}` | Get template detail |
| 5.9 | PATCH | `/api/v1/patient-treatment-plans/{planCode}/approval` | Approve/reject plan |
| 5.10 | PATCH | `/api/v1/patient-plan-items/{itemId}` | Update plan item |
| 5.11 | DELETE | `/api/v1/patient-plan-items/{itemId}` | Delete plan item |
| 5.13 | PATCH | `/api/v1/patient-treatment-plans/{planCode}/prices` | Update plan prices |
| 5.14 | PATCH | `/api/v1/patient-plan-phases/{phaseId}/items/reorder` | Reorder items in phase |

### Template APIs

| API | Method | Endpoint | Purpose |
|-----|--------|----------|---------|
| - | GET | `/api/v1/treatment-plan-templates` | List all templates (paginated) |
| 5.8 | GET | `/api/v1/treatment-plan-templates/{templateCode}` | Get specific template detail |

---

## Common Enums Reference

### 1. PaymentType

```typescript
type PaymentType = 'FULL' | 'PHASED' | 'INSTALLMENT';
```

**Values**:
- `FULL` - Full payment upfront
- `PHASED` - Payment by phases
- `INSTALLMENT` - Installment payment

### 2. TreatmentPlanStatus

```typescript
type TreatmentPlanStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
```

**Lifecycle**: `PENDING` → `IN_PROGRESS` → `COMPLETED` or `CANCELLED`

### 3. ApprovalStatus

```typescript
type ApprovalStatus = 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED';
```

**Lifecycle**: `DRAFT` → `PENDING_REVIEW` → `APPROVED` or `REJECTED`

### 4. ItemStatus

```typescript
type ItemStatus = 'PENDING' | 'READY_FOR_BOOKING' | 'SCHEDULED' | 'IN_PROGRESS' | 
                  'COMPLETED' | 'CANCELLED' | 'SKIPPED';
```

**Allowed Transitions**:
- `PENDING` → `READY_FOR_BOOKING`, `SKIPPED`
- `READY_FOR_BOOKING` → `SCHEDULED`, `CANCELLED`, `SKIPPED`
- `SCHEDULED` → `IN_PROGRESS`, `CANCELLED`
- `IN_PROGRESS` → `COMPLETED`, `CANCELLED`
- `COMPLETED` → (final state)
- `CANCELLED` → `READY_FOR_BOOKING`, `SCHEDULED` (can reschedule)
- `SKIPPED` → (final state)

---

## API Details with Correct Payloads

### API 5.1 - Get All Treatment Plans (List View)

**Endpoint**: `GET /api/v1/treatment-plans`

**Query Parameters**:
```
page: number = 0
size: number = 10
patientCode: string (optional) - Filter by patient
doctorEmployeeCode: string (optional) - Filter by doctor
status: TreatmentPlanStatus (optional) - Filter by status
approvalStatus: ApprovalStatus (optional) - Filter by approval status
```

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/treatment-plans?page=0&size=10&status=IN_PROGRESS" \
  -H "Authorization: Bearer {token}"
```

**Response** (200 OK):
```json
{
  "content": [
    {
      "planCode": "PLAN-20241215-001",
      "planName": "Boc rang su tham my 6 rang cua",
      "patient": {
        "patientCode": "BN-1004",
        "fullName": "Mit to Bit",
        "phone": "0974444444"
      },
      "doctor": {
        "employeeCode": "EMP001",
        "fullName": "Le Anh Khoa"
      },
      "status": "IN_PROGRESS",
      "approvalStatus": "APPROVED",
      "totalPrice": 42000000.00,
      "finalCost": 40000000.00,
      "startDate": "2024-12-15",
      "expectedEndDate": "2025-02-15",
      "createdAt": "2024-12-15T14:00:00",
      "approvedByName": "Jimmy Donaldson",
      "approvedAt": "2024-12-16T09:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 7,
  "totalPages": 1
}
```

---

### API 5.2 - Get Treatment Plan Detail

**Endpoint**: `GET /api/v1/patients/{patientCode}/treatment-plans/{planCode}`

**Path Parameters**:
- `patientCode`: Patient code (e.g., BN-1001)
- `planCode`: Plan code (e.g., PLAN-20241215-001)

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1004/treatment-plans/PLAN-20241215-001" \
  -H "Authorization: Bearer {token}"
```

**Response** (200 OK):
```json
{
  "planId": 5,
  "planCode": "PLAN-20241215-001",
  "planName": "Boc rang su tham my 6 rang cua",
  "status": "IN_PROGRESS",
  "approvalStatus": "APPROVED",
  "approvalMetadata": {
    "approvedBy": {
      "employeeCode": "EMP003",
      "fullName": "Jimmy Donaldson"
    },
    "approvedAt": "2024-12-16T09:00:00",
    "notes": null
  },
  "submitNotes": null,
  "doctor": {
    "employeeCode": "EMP001",
    "fullName": "Le Anh Khoa"
  },
  "patient": {
    "patientCode": "BN-1004",
    "fullName": "Mit to Bit"
  },
  "startDate": "2024-12-15",
  "expectedEndDate": "2025-02-15",
  "createdAt": "2024-12-15T14:00:00",
  "totalPrice": 42000000.00,
  "discountAmount": 2000000.00,
  "finalCost": 40000000.00,
  "paymentType": "INSTALLMENT",
  "progressSummary": {
    "totalPhases": 2,
    "completedPhases": 1,
    "totalItems": 9,
    "completedItems": 6,
    "readyForBookingItems": 3
  },
  "phases": [
    {
      "phaseId": 11,
      "phaseNumber": 1,
      "phaseName": "Giai doan 1: Kham va chuan bi",
      "status": "COMPLETED",
      "startDate": "2024-12-15",
      "completionDate": "2024-12-20",
      "estimatedDurationDays": 5,
      "items": [
        {
          "itemId": 29,
          "sequenceNumber": 1,
          "itemName": "Kham tong quat va tu van",
          "serviceId": 1,
          "serviceCode": "ENDO_POST_CORE",
          "price": 500000.00,
          "estimatedTimeMinutes": 30,
          "status": "COMPLETED",
          "completedAt": "2024-12-15T15:00:00",
          "linkedAppointments": []
        }
      ]
    }
  ]
}
```

---

### API 5.3 - Create Treatment Plan from Template

**Endpoint**: `POST /api/v1/patients/{patientCode}/treatment-plans`

**Path Parameters**:
- `patientCode`: Patient code (e.g., BN-1005)

**Request Body**:
```json
{
  "sourceTemplateCode": "TPL_IMPLANT_OSSTEM",
  "doctorEmployeeCode": "EMP001",
  "startDate": "2025-12-10",
  "paymentType": "FULL",
  "totalPrice": 19000000.00,
  "discountAmount": 1000000.00
}
```

**Field Details**:
- `sourceTemplateCode` (required): Template code to copy from
- `doctorEmployeeCode` (required): Doctor managing this plan
- `startDate` (optional): Plan start date (YYYY-MM-DD format)
- `paymentType` (required): Payment method (FULL, PHASED, or INSTALLMENT)
- `totalPrice` (required): Total price before discount
- `discountAmount` (optional): Discount amount, default 0

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1005/treatment-plans" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceTemplateCode": "TPL_IMPLANT_OSSTEM",
    "doctorEmployeeCode": "EMP001",
    "startDate": "2025-12-10",
    "paymentType": "FULL",
    "totalPrice": 19000000.00,
    "discountAmount": 1000000.00
  }'
```

**Response** (201 Created):
```json
{
  "planId": 12,
  "planCode": "PLAN-20251202-001",
  "planName": "Cay ghep Implant Han Quoc (Osstem) - Tron goi",
  "status": "PENDING",
  "approvalStatus": "DRAFT",
  "doctor": {
    "employeeCode": "EMP001",
    "fullName": "Le Anh Khoa"
  },
  "patient": {
    "patientCode": "BN-1005",
    "fullName": "Tran Van Nam"
  },
  "startDate": null,
  "expectedEndDate": "2026-05-31",
  "createdAt": "2025-12-02T23:27:48.683383",
  "totalPrice": 21000000.00,
  "discountAmount": 1000000.00,
  "finalCost": 20000000.00,
  "paymentType": "FULL",
  "progressSummary": {
    "totalPhases": 3,
    "completedPhases": 0,
    "totalItems": 6,
    "completedItems": 0,
    "readyForBookingItems": 0
  },
  "phases": [...]
}
```

---

### API 5.6 - Update Item Status

**Endpoint**: `PATCH /api/v1/patient-plan-items/{itemId}/status`

**Path Parameters**:
- `itemId`: Item ID (numeric)

**Request Body**:
```json
{
  "status": "COMPLETED",
  "notes": "Completed successfully"
}
```

**Field Details**:
- `status` (required): New status (see ItemStatus enum)
- `notes` (optional): Additional notes

**Example Request**:
```bash
curl -X PATCH "http://localhost:8080/api/v1/patient-plan-items/29/status" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED",
    "notes": "Patient completed this item"
  }'
```

**Response** (200 OK):
```json
{
  "itemId": 29,
  "sequenceNumber": 1,
  "itemName": "Kham tong quat va tu van",
  "serviceId": 1,
  "price": 500000.00,
  "estimatedTimeMinutes": 30,
  "status": "COMPLETED",
  "completedAt": "2025-12-02T23:30:00",
  "notes": "Patient completed this item",
  "phaseId": 11,
  "linkedAppointments": []
}
```

---

### API 5.8 - Get Template Detail

**Endpoint**: `GET /api/v1/treatment-plan-templates/{templateCode}`

**Path Parameters**:
- `templateCode`: Template code (e.g., TPL_ORTHO_METAL)

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/treatment-plan-templates/TPL_ORTHO_METAL" \
  -H "Authorization: Bearer {token}"
```

**Response** (200 OK):
```json
{
  "templateId": 1,
  "templateCode": "TPL_ORTHO_METAL",
  "templateName": "Nieng rang mac cai kim loai tron goi 2 nam",
  "description": "Goi dieu tri chinh nha toan dien voi mac cai kim loai, bao gom 24 lan tai kham siet nieng dinh ky.",
  "specialization": {
    "id": 1,
    "name": "Chinh nha"
  },
  "estimatedTotalCost": 30000000.00,
  "estimatedDurationDays": 730,
  "createdAt": "2025-12-02T03:17:32.796246",
  "isActive": true,
  "summary": {
    "totalPhases": 4,
    "totalItemsInTemplate": 6
  },
  "phases": [
    {
      "phaseTemplateId": 1,
      "phaseName": "Giai doan 1: Kham & Chuan bi",
      "stepOrder": 1,
      "itemsInPhase": [
        {
          "serviceCode": "ORTHO_CONSULT",
          "serviceName": "Kham & Tu van Chinh nha",
          "price": 0.00,
          "quantity": 1,
          "sequenceNumber": 1
        }
      ]
    }
  ]
}
```

---

### API 5.9 - Approve/Reject Treatment Plan

**Endpoint**: `PATCH /api/v1/patient-treatment-plans/{planCode}/approval`

**Path Parameters**:
- `planCode`: Plan code (e.g., PLAN-20251202-001)

**Request Body**:
```json
{
  "approvalStatus": "APPROVED",
  "notes": "Plan approved by manager"
}
```

**Field Details**:
- `approvalStatus` (required): Either "APPROVED" or "REJECTED"
- `notes` (optional): Approval/rejection notes (mandatory for REJECTED)

**Important**: Plan must be in `PENDING_REVIEW` status before approval. Use submit-for-review API first if plan is in DRAFT status.

**Example Request - Approve**:
```bash
curl -X PATCH "http://localhost:8080/api/v1/patient-treatment-plans/PLAN-20251202-001/approval" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "approvalStatus": "APPROVED",
    "notes": "Approved by manager"
  }'
```

**Example Request - Reject**:
```bash
curl -X PATCH "http://localhost:8080/api/v1/patient-treatment-plans/PLAN-20251202-001/approval" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "approvalStatus": "REJECTED",
    "notes": "Price too high, needs revision"
  }'
```

**Response** (200 OK):
```json
{
  "message": "Treatment plan approved successfully",
  "planCode": "PLAN-20251202-001",
  "approvalStatus": "APPROVED",
  "approvedBy": "EMP003",
  "approvedAt": "2025-12-02T23:35:00"
}
```

---

## Common Error Responses

### 400 Bad Request - Invalid PaymentType

```json
{
  "statusCode": 400,
  "error": "VALIDATION_ERROR",
  "message": "Cannot deserialize value of type PaymentType from String 'FULL_PAYMENT': not one of the values accepted for Enum class: [PHASED, FULL, INSTALLMENT]"
}
```

**Fix**: Use `FULL` instead of `FULL_PAYMENT`

### 400 Bad Request - Wrong Field Name

```json
{
  "statusCode": 400,
  "error": "VALIDATION_ERROR",
  "message": "sourceTemplateCode: Template code is required"
}
```

**Fix**: Use `sourceTemplateCode` instead of `templateCode`

### 404 Not Found - Patient Not Found

```json
{
  "statusCode": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Patient not found with code: BN-9999"
}
```

**Fix**: Verify patient code exists in system

### 404 Not Found - Template Not Found

```json
{
  "statusCode": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Template not found with code: TPL_INVALID"
}
```

**Fix**: Use valid template code (get list from templates API)

### 403 Forbidden - No Permission

```json
{
  "statusCode": 403,
  "error": "FORBIDDEN",
  "message": "Access denied"
}
```

**Fix**: Login with account that has treatment plan permissions

---

## Testing Checklist

### Prerequisites
1. Backend running on `http://localhost:8080`
2. Valid JWT token obtained from `/api/v1/auth/login`
3. Test patients exist (BN-1001, BN-1002, etc.)
4. Test employees exist (EMP001, EMP003, etc.)
5. Templates exist (TPL_ORTHO_METAL, TPL_IMPLANT_OSSTEM, etc.)

### Test Sequence

#### 1. Get Templates List
```bash
curl -X GET "http://localhost:8080/api/v1/treatment-plan-templates?page=0&size=10" \
  -H "Authorization: Bearer {token}"
```
Expected: 200 OK, list of templates

#### 2. Get Specific Template
```bash
curl -X GET "http://localhost:8080/api/v1/treatment-plan-templates/TPL_ORTHO_METAL" \
  -H "Authorization: Bearer {token}"
```
Expected: 200 OK, template with phases and items

#### 3. Create Plan from Template
```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1005/treatment-plans" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceTemplateCode": "TPL_IMPLANT_OSSTEM",
    "doctorEmployeeCode": "EMP001",
    "paymentType": "FULL",
    "totalPrice": 19000000.00,
    "discountAmount": 1000000.00
  }'
```
Expected: 201 Created, plan with status PENDING and approvalStatus DRAFT

#### 4. Get Plan Detail
```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1005/treatment-plans/PLAN-20251202-001" \
  -H "Authorization: Bearer {token}"
```
Expected: 200 OK, complete plan with all phases and items

#### 5. Approve Plan
```bash
curl -X PATCH "http://localhost:8080/api/v1/patient-treatment-plans/PLAN-20251202-001/approval" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "APPROVE",
    "notes": "Test approval"
  }'
```
Expected: 200 OK, approvalStatus changed to APPROVED

#### 6. Update Item Status
```bash
curl -X PATCH "http://localhost:8080/api/v1/patient-plan-items/64/status" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED"
  }'
```
Expected: 200 OK, item status updated

#### 7. List All Plans
```bash
curl -X GET "http://localhost:8080/api/v1/treatment-plans?page=0&size=10" \
  -H "Authorization: Bearer {token}"
```
Expected: 200 OK, paginated list including newly created plan

---

## Troubleshooting Guide

### Problem: "Cannot deserialize value of type PaymentType"

**Symptom**: 500 error when creating plan
```json
{
  "statusCode": 500,
  "error": "error.internal",
  "message": "Cannot deserialize value of type PaymentType from String 'FULL_PAYMENT'"
}
```

**Root Cause**: Using old enum value `FULL_PAYMENT` instead of `FULL`

**Solution**: Update request body:
```diff
{
-  "paymentType": "FULL_PAYMENT"
+  "paymentType": "FULL"
}
```

### Problem: "sourceTemplateCode: Template code is required"

**Symptom**: 400 validation error
```json
{
  "statusCode": 400,
  "error": "VALIDATION_ERROR",
  "message": "sourceTemplateCode: Template code is required"
}
```

**Root Cause**: Using wrong field name `templateCode`

**Solution**: Update request body:
```diff
{
-  "templateCode": "TPL_ORTHO_METAL"
+  "sourceTemplateCode": "TPL_ORTHO_METAL"
}
```

### Problem: Plan created with status null

**Symptom**: Plan created but status field is null

**Root Cause**: Database constraint was missing PENDING status (FIXED on 2025-12-02)

**Solution**: 
1. Update database: Already fixed in current version
2. Plans now properly default to PENDING status when created

### Problem: Phase/Plan not auto-completing

**Symptom**: All items completed but phase status stays IN_PROGRESS

**Root Cause**: Lazy loading issue with JPA (FIXED in Issue #40)

**Solution**: Already fixed in current version. Phase and plan auto-complete correctly when all items/phases are done.

### Problem: Cannot access treatment plan features

**Symptom**: 403 Forbidden error

**Root Cause**: Missing permissions or not logged in

**Solution**:
1. Verify logged in with valid token
2. Check user role (Admin, Manager, or Dentist have access)
3. Receptionist and Patient roles have read-only access

---

## Database Schema Reference

### patient_treatment_plans

Key fields:
- `plan_id` (BIGINT, PK) - Unique ID
- `plan_code` (VARCHAR(50), UNIQUE) - Business key (PLAN-YYYYMMDD-XXX)
- `plan_name` (VARCHAR(255)) - Display name
- `patient_id` (INT, FK) - Patient reference
- `created_by` (INT, FK) - Doctor reference
- `status` (VARCHAR(20)) - PENDING, IN_PROGRESS, COMPLETED, CANCELLED
- `approval_status` (VARCHAR(20)) - DRAFT, PENDING_REVIEW, APPROVED, REJECTED
- `payment_type` (VARCHAR(20)) - FULL, PHASED, INSTALLMENT
- `total_price` (DECIMAL(12,2)) - Total before discount
- `discount_amount` (DECIMAL(12,2)) - Discount
- `final_cost` (DECIMAL(12,2)) - Total - discount

### patient_plan_phases

Key fields:
- `patient_phase_id` (BIGINT, PK) - Unique ID
- `plan_id` (BIGINT, FK) - Plan reference
- `phase_number` (INT) - Sequence in plan
- `phase_name` (VARCHAR(255)) - Display name
- `status` (VARCHAR(20)) - PENDING, IN_PROGRESS, COMPLETED, CANCELLED

### patient_plan_items

Key fields:
- `item_id` (BIGINT, PK) - Unique ID
- `phase_id` (BIGINT, FK) - Phase reference
- `sequence_number` (INT) - Order in phase
- `item_name` (VARCHAR(255)) - Display name
- `service_id` (INT, FK) - Service reference
- `status` (VARCHAR(50)) - PENDING, READY_FOR_BOOKING, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, SKIPPED
- `price` (DECIMAL(12,2)) - Item price
- `completed_at` (TIMESTAMP) - Completion time

---

## Support

**Backend Repository**: PDCMS_BE  
**Branch**: feat/BE-501-manage-treatment-plans  
**Last Updated**: 2025-12-02  
**Documentation**: `/docs/api-guides/treatment-plan/`

For issues or questions, contact the backend team with:
1. API endpoint used
2. Complete request payload
3. Response received
4. Expected behavior
