# API 5.4: Create Custom Treatment Plan

## Overview

Create a custom treatment plan from scratch (without using templates). Supports custom phases, service selection, quantity expansion, and custom pricing.

**Endpoint**: `POST /api/v1/patients/{patientCode}/treatment-plans/custom`

**Permission**: `CREATE_TREATMENT_PLAN`

**Roles**: ADMIN, DENTIST

## Business Context

### What is a Custom Treatment Plan?

- Treatment plan created **without using templates**
- Doctor manually selects services, sets quantities, and defines phases
- Created with `approval_status = DRAFT` (requires approval before activation)
- Items created with `status = PENDING` until plan is approved

### Key Features

1. **Quantity Expansion**:

   - Set `quantity = 5` for follow-up visits
   - Backend creates 5 separate items: "Service (Lan 1)", "Service (Lan 2)", etc.

2. **Custom Pricing**:

   - Optional price override per item
   - If omitted, uses service default price
   - No validation range restrictions (V21.4 update)

3. **Approval Workflow**:

   - Plan created with `approval_status = DRAFT`
   - Requires manager approval via API 5.9
   - After approval, items transition to `READY_FOR_BOOKING`

4. **Phase Management**:
   - Define multiple phases with custom durations
   - Set `estimatedDurationDays` for timeline calculation

## Request

### Path Parameters

| Parameter     | Type   | Required | Description                  |
| ------------- | ------ | -------- | ---------------------------- |
| `patientCode` | String | Yes      | Patient code (e.g., BN-1001) |

### Request Body

```json
{
  "planName": "string",
  "doctorEmployeeCode": "string",
  "discountAmount": 0,
  "paymentType": "FULL|PHASED|INSTALLMENT",
  "startDate": "2025-12-16",
  "expectedEndDate": "2026-06-16",
  "phases": [
    {
      "phaseNumber": 1,
      "phaseName": "string",
      "estimatedDurationDays": 180,
      "items": [
        {
          "serviceCode": "string",
          "sequenceNumber": 1,
          "quantity": 1,
          "price": 2000000
        }
      ]
    }
  ]
}
```

### Field Descriptions

#### Plan Level

- **planName** (required): Plan display name (max 255 chars)
- **doctorEmployeeCode** (required): Doctor employee code (EMP001, EMP002, etc.)
- **discountAmount** (required): Discount in VND (>= 0)
- **paymentType** (required): FULL, PHASED, or INSTALLMENT
- **startDate** (optional): Plan start date
- **expectedEndDate** (optional): Plan expected end date

#### Phase Level

- **phaseNumber** (required): Sequential phase number (1, 2, 3...)
- **phaseName** (required): Phase display name (max 255 chars)
- **estimatedDurationDays** (optional): Phase duration in days (>= 0)
- **items** (required): List of services (min 1 item)

#### Item Level

- **serviceCode** (required): Service code from services table
- **sequenceNumber** (required): Item order within phase (>= 1)
- **quantity** (required): Number of times service is performed (1-100)
- **price** (optional): Custom price override (>= 0, if omitted uses service default)

### Validation Rules

1. Must have at least 1 phase
2. Each phase must have at least 1 item
3. Phase numbers must be unique
4. Discount amount must be <= total cost
5. Quantity: 1 <= quantity <= 100
6. Doctor must exist and be active
7. All service codes must exist

## Response

### Success Response (201 Created)

```json
{
  "planId": 13,
  "planCode": "PLAN-20251216-001",
  "planName": "Custom Orthodontics Plan",
  "patient": {
    "patientId": 60,
    "patientCode": "BN-1001",
    "fullName": "Nguyen Van A"
  },
  "primaryDoctor": {
    "employeeId": 2,
    "employeeCode": "EMP002",
    "fullName": "Dr. Nguyen Thi Lan"
  },
  "approvalStatus": "DRAFT",
  "status": "PENDING",
  "totalCost": 20000000,
  "discountAmount": 0,
  "totalPrice": 20000000,
  "paymentType": "FULL",
  "startDate": "2025-12-16",
  "expectedEndDate": null,
  "actualEndDate": null,
  "phases": [
    {
      "phaseId": 4,
      "phaseNumber": 1,
      "phaseName": "Phase 1: Initial Setup",
      "status": "PENDING",
      "estimatedDurationDays": 30,
      "items": [
        {
          "itemId": 66,
          "sequenceNumber": 1,
          "itemName": "Root Canal Treatment (Lan 1)",
          "serviceCode": "ENDO_TREAT_POST",
          "status": "PENDING",
          "price": 1500000,
          "assignedDoctor": null,
          "completedAt": null
        }
      ]
    }
  ],
  "createdBy": {
    "employeeId": 2,
    "employeeCode": "EMP002",
    "fullName": "Dr. Nguyen Thi Lan"
  },
  "createdAt": "2025-12-16T23:30:32.596",
  "updatedAt": "2025-12-16T23:30:32.596"
}
```

### Error Responses

#### 400 Bad Request

```json
{
  "type": "about:blank",
  "title": "Constraint Violation",
  "status": 400,
  "detail": "planName: Plan name is required",
  "instance": "/api/v1/patients/BN-1001/treatment-plans/custom"
}
```

#### 404 Not Found - Patient

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Patient not found with code: BN-9999",
  "instance": "/api/v1/patients/BN-9999/treatment-plans/custom"
}
```

#### 404 Not Found - Doctor

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Doctor not found with employee code: EMP999",
  "instance": "/api/v1/patients/BN-1001/treatment-plans/custom"
}
```

#### 404 Not Found - Service

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Service not found with code: INVALID_CODE",
  "instance": "/api/v1/patients/BN-1001/treatment-plans/custom"
}
```

#### 500 Internal Server Error - Constraint Violation (Before Fix)

```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "ERROR: new row for relation \"patient_plan_items\" violates check constraint \"patient_plan_items_status_check\"",
  "instance": "/api/v1/patients/BN-1001/treatment-plans/custom"
}
```

**Fix Applied**: DROP CONSTRAINT statement added to `enums.sql` to remove outdated check constraint.

## Test Guide

### Prerequisites

- App running on `http://localhost:8080`
- Login credentials: Any account from seed data (password: `123456`)
- Test with `EMP002` (Dr. Nguyen Thi Lan - Dentist)

### Test Case 1: Basic Custom Plan (1 Phase, 1 Item)

**Request**:

```bash
curl -X POST http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/custom \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "planName": "Custom Root Canal Plan",
    "doctorEmployeeCode": "EMP002",
    "paymentType": "FULL",
    "discountAmount": 0,
    "startDate": "2025-12-16",
    "expectedEndDate": null,
    "phases": [
      {
        "phaseNumber": 1,
        "phaseName": "Root Canal Treatment Phase",
        "estimatedDurationDays": 30,
        "items": [
          {
            "serviceCode": "ENDO_TREAT_POST",
            "sequenceNumber": 1,
            "quantity": 1,
            "price": 2000000
          }
        ]
      }
    ]
  }'
```

**Expected Result**:

- Status: `201 Created`
- Plan created with `approval_status = DRAFT`
- Item created with `status = PENDING`
- PlanCode: `PLAN-YYYYMMDD-XXX`

### Test Case 2: Multiple Phases with Quantity Expansion

**Request**:

```bash
curl -X POST http://localhost:8080/api/v1/patients/BN-1002/treatment-plans/custom \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "planName": "Custom Orthodontics Plan (6 months)",
    "doctorEmployeeCode": "EMP002",
    "paymentType": "PHASED",
    "discountAmount": 500000,
    "startDate": "2025-12-20",
    "expectedEndDate": "2026-06-20",
    "phases": [
      {
        "phaseNumber": 1,
        "phaseName": "Initial Setup",
        "estimatedDurationDays": 30,
        "items": [
          {
            "serviceCode": "GEN_EXAM",
            "sequenceNumber": 1,
            "quantity": 1,
            "price": 500000
          },
          {
            "serviceCode": "ORTHO_BRACES_ON",
            "sequenceNumber": 2,
            "quantity": 1,
            "price": 15000000
          }
        ]
      },
      {
        "phaseNumber": 2,
        "phaseName": "Monthly Adjustments",
        "estimatedDurationDays": 150,
        "items": [
          {
            "serviceCode": "ORTHO_ADJUST",
            "sequenceNumber": 1,
            "quantity": 5,
            "price": 500000
          }
        ]
      }
    ]
  }'
```

**Expected Result**:

- Status: `201 Created`
- Phase 1: 2 items (Exam + Braces)
- Phase 2: 5 items (Adjust 1, Adjust 2, ..., Adjust 5)
- Total items: 7
- Total cost calculation: (500000 + 15000000 + 500000\*5) - 500000 discount

### Test Case 3: Omit Custom Price (Use Service Default)

**Request**:

```bash
curl -X POST http://localhost:8080/api/v1/patients/BN-1003/treatment-plans/custom \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "planName": "Standard Checkup Plan",
    "doctorEmployeeCode": "EMP002",
    "paymentType": "FULL",
    "discountAmount": 0,
    "startDate": "2025-12-16",
    "phases": [
      {
        "phaseNumber": 1,
        "phaseName": "Checkup Phase",
        "estimatedDurationDays": 1,
        "items": [
          {
            "serviceCode": "GEN_EXAM",
            "sequenceNumber": 1,
            "quantity": 1
          }
        ]
      }
    ]
  }'
```

**Expected Result**:

- Status: `201 Created`
- Item price auto-filled from `services.default_price`
- Example: GEN_EXAM default price = 500000 VND

### Test Case 4: Validation Errors

#### Missing Required Fields

```bash
curl -X POST http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/custom \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "planName": "",
    "doctorEmployeeCode": "EMP002",
    "paymentType": "FULL",
    "discountAmount": 0,
    "phases": []
  }'
```

**Expected Result**:

- Status: `400 Bad Request`
- Error: "Plan name is required" and "Must have at least 1 phase"

#### Invalid Service Code

```bash
curl -X POST http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/custom \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "planName": "Invalid Service Plan",
    "doctorEmployeeCode": "EMP002",
    "paymentType": "FULL",
    "discountAmount": 0,
    "phases": [
      {
        "phaseNumber": 1,
        "phaseName": "Test",
        "items": [
          {
            "serviceCode": "INVALID_CODE",
            "sequenceNumber": 1,
            "quantity": 1
          }
        ]
      }
    ]
  }'
```

**Expected Result**:

- Status: `404 Not Found`
- Error: "Service not found with code: INVALID_CODE"

## Database Impact

### Tables Affected

1. **patient_treatment_plans**: 1 new row

   - `approval_status = 'DRAFT'`
   - `status = 'PENDING'`

2. **patient_plan_phases**: N rows (N = number of phases)

   - `status = 'PENDING'`

3. **patient_plan_items**: M rows (M = sum of all quantities)
   - `status = 'PENDING'`
   - Quantity expansion creates multiple rows with names "(Lan 1)", "(Lan 2)", etc.

### Constraint Fix Applied

```sql
-- Added to enums.sql
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'patient_plan_items_status_check') THEN
        ALTER TABLE patient_plan_items DROP CONSTRAINT patient_plan_items_status_check;
    END IF;
END $$;
```

This removes the outdated CHECK constraint that blocked `PENDING`, `WAITING_FOR_PREREQUISITE`, and `SKIPPED` statuses.

## Related APIs

- **API 5.3**: Create Treatment Plan from Template
- **API 5.9**: Approve Treatment Plan (required before activation)
- **API 5.6**: Add Item to Phase
- **API 5.7**: Update Item
- **API 5.8**: Delete Item

## Notes

- Plans created via this API start with `DRAFT` approval status
- Manager must approve via API 5.9 before plan becomes active
- After approval, items transition from `PENDING` to `READY_FOR_BOOKING`
- Quantity expansion simplifies creating repeating services (e.g., monthly checkups)
- Custom pricing is optional - system auto-fills from service defaults
