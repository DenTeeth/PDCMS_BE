# API 5.1 & 5.2 - Get Treatment Plans

**Module**: Treatment Plan Management  
**Version**: V1.0  
**Status**: ✅ Production Ready  
**Last Updated**: 2025-11-12

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [API 5.1 - Get Plans by Patient](#api-51-get-plans-by-patient)
3. [API 5.2 - Get Plan Details](#api-52-get-plan-details)
4. [Response Models](#response-models)
5. [Error Handling](#error-handling)
6. [Testing Guide](#testing-guide)

---

## Overview

These APIs allow viewing treatment plan summaries and details with proper RBAC controls.

### Permissions

| Permission                | Role             | Description                           |
|---------------------------|------------------|---------------------------------------|
| `VIEW_TREATMENT_PLAN_ALL` | Staff, Admin     | View all patient treatment plans      |
| `VIEW_TREATMENT_PLAN_OWN` | Patient          | View only own treatment plans         |

### Key Features

✅ Patient-specific plan listing with progress tracking  
✅ Detailed plan view with phase and item breakdown  
✅ RBAC validation (staff can view all, patients view own only)  
✅ N+1 query prevention with JOIN FETCH optimization  
✅ Status tracking: Phase completion, item progress  

---

## API 5.1: Get Plans by Patient

### Endpoint

```
GET /api/v1/patients/{patientCode}/treatment-plans
```

### Purpose

Get a summary list of all treatment plans for a specific patient. Includes progress tracking and current phase information.

### Path Parameters

| Parameter     | Type   | Required | Description                    |
|---------------|--------|----------|--------------------------------|
| `patientCode` | String | Yes      | Patient business code (e.g., BN-1001) |

### RBAC Logic

1. **Find Patient**: Lookup patient by `patientCode`
   - If not found → `404 NOT_FOUND`

2. **Permission Check**:
   - **Staff/Admin** with `VIEW_TREATMENT_PLAN_ALL`: ✅ Access granted
   - **Patient** with `VIEW_TREATMENT_PLAN_OWN`:
     - Check if `patient.account_id` matches JWT `account_id`
     - If mismatch → `403 FORBIDDEN`

3. **Query Plans**:
   - JOIN FETCH employees (doctor) to avoid N+1 queries
   - Filter by `patient_id`
   - Order by `created_at DESC`

### Response (200 OK)

```json
{
  "plans": [
    {
      "planId": 1,
      "planCode": "PLAN-20251001-001",
      "planName": "Lộ trình Niềng răng Mắc cài Kim loại",
      "status": "IN_PROGRESS",
      "approvalStatus": "APPROVED",
      "startDate": "2025-10-01",
      "expectedEndDate": "2027-10-01",
      "totalPrice": 35000000,
      "discountAmount": 0,
      "finalCost": 35000000,
      "paymentType": "INSTALLMENT",
      "progress": {
        "totalPhases": 3,
        "completedPhases": 1,
        "totalItems": 15,
        "completedItems": 3,
        "percentageComplete": 20.0
      },
      "currentPhase": {
        "phaseNumber": 2,
        "phaseName": "Giai đoạn 2: Lắp Mắc cài và Điều chỉnh ban đầu",
        "status": "IN_PROGRESS"
      },
      "createdBy": {
        "employeeId": 1,
        "employeeName": "Bác sĩ Nguyễn Văn A",
        "employeeCode": "EMP-1"
      },
      "sourceTemplate": {
        "templateId": 1,
        "templateName": "Niềng răng Mắc cài Kim loại"
      },
      "createdAt": "2025-10-01T10:00:00",
      "updatedAt": "2025-10-15T14:30:00"
    }
  ]
}
```

### Response Fields Explained

**Progress Calculation**:
- `percentageComplete` = (completedItems / totalItems) × 100
- `completedPhases` = Count of phases with status = COMPLETED
- `completedItems` = Count of items with status = COMPLETED

**Current Phase**:
- First phase with status = IN_PROGRESS
- If all phases COMPLETED → last phase
- If all phases PENDING → first phase

### Example Requests

**Staff viewing patient plans**:
```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer <staff-token>"
```

**Patient viewing own plans**:
```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer <patient-bn1001-token>"
```

**Patient trying to view another patient's plans** (403):
```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans" \
  -H "Authorization: Bearer <patient-bn1001-token>"
```

---

## API 5.2: Get Plan Details

### Endpoint

```
GET /api/v1/patients/{patientCode}/treatment-plans/{planCode}
```

### Purpose

Get complete details of a specific treatment plan including all phases, items, and appointments.

### Path Parameters

| Parameter     | Type   | Required | Description                    |
|---------------|--------|----------|--------------------------------|
| `patientCode` | String | Yes      | Patient business code          |
| `planCode`    | String | Yes      | Treatment plan code            |

### RBAC Logic

Same as API 5.1 (patient ownership validation).

### Response (200 OK)

```json
{
  "planId": 1,
  "planCode": "PLAN-20251001-001",
  "planName": "Lộ trình Niềng răng Mắc cài Kim loại",
  "status": "IN_PROGRESS",
  "approvalStatus": "APPROVED",
  "patientConsentDate": "2025-10-01T08:30:00",
  "approvedBy": {
    "employeeId": 3,
    "employeeName": "Quản lý Nguyễn Văn C"
  },
  "approvedAt": "2025-10-02T09:00:00",
  "startDate": "2025-10-01",
  "expectedEndDate": "2027-10-01",
  "totalPrice": 35000000,
  "discountAmount": 0,
  "finalCost": 35000000,
  "paymentType": "INSTALLMENT",
  "patient": {
    "patientId": 1,
    "patientName": "Đoàn Thanh Phong",
    "patientCode": "BN-1001",
    "phoneNumber": "0901234567"
  },
  "createdBy": {
    "employeeId": 1,
    "employeeName": "Bác sĩ Nguyễn Văn A",
    "employeeCode": "EMP-1"
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
      "status": "COMPLETED",
      "startDate": "2025-10-01",
      "completionDate": "2025-10-06",
      "estimatedDurationDays": 7,
      "items": [
        {
          "itemId": 1,
          "sequenceNumber": 1,
          "itemName": "Khám tổng quát và chụp X-quang",
          "status": "COMPLETED",
          "price": 500000,
          "estimatedTimeMinutes": 30,
          "service": {
            "serviceId": 1,
            "serviceName": "Khám tổng quát",
            "serviceCode": "EXAM-GEN"
          },
          "appointments": [
            {
              "appointmentId": 101,
              "appointmentCode": "APT-20251001-001",
              "appointmentDate": "2025-10-02",
              "startTime": "09:00:00",
              "status": "COMPLETED"
            }
          ],
          "completedAt": "2025-10-02T09:00:00",
          "createdAt": "2025-10-01T10:00:00"
        },
        {
          "itemId": 2,
          "sequenceNumber": 2,
          "itemName": "Lấy cao răng trước niềng",
          "status": "COMPLETED",
          "price": 800000,
          "estimatedTimeMinutes": 45,
          "service": {
            "serviceId": 3,
            "serviceName": "Lấy cao răng",
            "serviceCode": "SCALING-01"
          },
          "appointments": [
            {
              "appointmentId": 102,
              "appointmentCode": "APT-20251003-001",
              "appointmentDate": "2025-10-03",
              "startTime": "10:30:00",
              "status": "COMPLETED"
            }
          ],
          "completedAt": "2025-10-03T10:30:00",
          "createdAt": "2025-10-01T10:00:00"
        }
      ]
    },
    {
      "patientPhaseId": 2,
      "phaseNumber": 2,
      "phaseName": "Giai đoạn 2: Lắp Mắc cài và Điều chỉnh ban đầu",
      "status": "IN_PROGRESS",
      "startDate": "2025-10-15",
      "completionDate": null,
      "estimatedDurationDays": 60,
      "items": [
        {
          "itemId": 4,
          "sequenceNumber": 1,
          "itemName": "Lắp mắc cài kim loại hàm trên",
          "status": "COMPLETED",
          "price": 8000000,
          "estimatedTimeMinutes": 90,
          "service": {
            "serviceId": 38,
            "serviceName": "Lắp mắc cài kim loại",
            "serviceCode": "BRACES-METAL"
          },
          "appointments": [
            {
              "appointmentId": 103,
              "appointmentCode": "APT-20251016-001",
              "appointmentDate": "2025-10-16",
              "startTime": "09:00:00",
              "status": "COMPLETED"
            }
          ],
          "completedAt": "2025-10-16T09:00:00",
          "createdAt": "2025-10-01T10:00:00"
        },
        {
          "itemId": 6,
          "sequenceNumber": 3,
          "itemName": "Điều chỉnh lần 1 (sau 1 tháng)",
          "status": "READY_FOR_BOOKING",
          "price": 500000,
          "estimatedTimeMinutes": 45,
          "service": {
            "serviceId": 39,
            "serviceName": "Điều chỉnh niềng răng",
            "serviceCode": "BRACES-ADJUST"
          },
          "appointments": [],
          "completedAt": null,
          "createdAt": "2025-10-01T10:00:00"
        }
      ]
    }
  ],
  "createdAt": "2025-10-01T10:00:00",
  "updatedAt": "2025-10-16T10:00:00"
}
```

### Item Status Meanings

| Status              | Description                     | Booking Allowed | Color Code |
|---------------------|---------------------------------|-----------------|------------|
| `PENDING`           | Awaiting approval               | No              | Gray       |
| `READY_FOR_BOOKING` | Can schedule appointment        | Yes             | Green      |
| `SCHEDULED`         | Appointment booked              | No              | Blue       |
| `IN_PROGRESS`       | Currently being performed       | No              | Orange     |
| `COMPLETED`         | Finished                        | No              | Green      |

### Example Requests

**Get plan details**:
```bash
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
  -H "Authorization: Bearer <token>"
```

---

## Response Models

### TreatmentPlanSummaryDTO

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
  "progress": {
    "totalPhases": Integer,
    "completedPhases": Integer,
    "totalItems": Integer,
    "completedItems": Integer,
    "percentageComplete": Double
  },
  "currentPhase": {
    "phaseNumber": Integer,
    "phaseName": String,
    "status": String
  },
  "createdBy": EmployeeSummaryDTO,
  "sourceTemplate": TemplateSummaryDTO,
  "createdAt": LocalDateTime,
  "updatedAt": LocalDateTime
}
```

### TreatmentPlanDetailDTO

Extends `TreatmentPlanSummaryDTO` with:
```java
{
  "patient": PatientSummaryDTO,
  "approvedBy": EmployeeSummaryDTO,
  "approvedAt": LocalDateTime,
  "patientConsentDate": LocalDateTime,
  "rejectionReason": String (nullable),
  "phases": List<PhaseDetailDTO>
}
```

### PhaseDetailDTO

```java
{
  "patientPhaseId": Long,
  "phaseNumber": Integer,
  "phaseName": String,
  "status": String,
  "startDate": LocalDate,
  "completionDate": LocalDate (nullable),
  "estimatedDurationDays": Integer,
  "items": List<PlanItemDTO>
}
```

### PlanItemDTO

```java
{
  "itemId": Long,
  "sequenceNumber": Integer,
  "itemName": String,
  "status": String,
  "price": BigDecimal,
  "estimatedTimeMinutes": Integer,
  "service": ServiceSummaryDTO,
  "appointments": List<AppointmentSummaryDTO>,
  "completedAt": LocalDateTime,
  "createdAt": LocalDateTime
}
```

---

## Error Handling

### Common Error Codes

| HTTP | Error Code             | Description                          |
|------|------------------------|--------------------------------------|
| 404  | PATIENT_NOT_FOUND      | Patient code not found               |
| 404  | PLAN_NOT_FOUND         | Treatment plan code not found        |
| 403  | FORBIDDEN              | Patient accessing another's plan     |
| 401  | UNAUTHORIZED           | Invalid or missing JWT token         |

### Error Response Format

```json
{
  "type": "https://www.jhipster.tech/problem/problem-with-message",
  "title": "Forbidden",
  "status": 403,
  "detail": "You do not have permission to access this patient's treatment plans",
  "path": "/api/v1/patients/BN-1002/treatment-plans",
  "message": "error.FORBIDDEN",
  "errorCode": "FORBIDDEN"
}
```

---

## Testing Guide

### Test Scenario 1: Staff Views Patient Plans

**Setup**:
- Patient BN-1001 has 2 treatment plans
- User is staff with VIEW_TREATMENT_PLAN_ALL

**Steps**:
1. GET `/api/v1/patients/BN-1001/treatment-plans` with staff token
2. Verify response contains 2 plans
3. Verify progress calculations are correct
4. Verify currentPhase reflects actual phase status

**Expected**: 200 OK with full plan list

### Test Scenario 2: Patient Views Own Plans

**Setup**:
- Patient BN-1001 is logged in
- Patient has 2 treatment plans

**Steps**:
1. GET `/api/v1/patients/BN-1001/treatment-plans` with patient token
2. Verify response contains own plans only

**Expected**: 200 OK

### Test Scenario 3: Patient Tries to View Another's Plans

**Setup**:
- Patient BN-1001 is logged in
- Trying to access BN-1002's plans

**Steps**:
1. GET `/api/v1/patients/BN-1002/treatment-plans` with BN-1001 token
2. Verify rejection

**Expected**: 403 FORBIDDEN

### Test Scenario 4: Plan Details with Appointments

**Setup**:
- Plan has items with SCHEDULED status
- Some items linked to appointments

**Steps**:
1. GET `/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001`
2. Verify phases are ordered by phaseNumber
3. Verify items are ordered by sequenceNumber
4. Verify SCHEDULED items include appointment info
5. Verify READY_FOR_BOOKING items have empty appointments array

**Expected**: 200 OK with full details

### Test Scenario 5: Progress Calculation

**Setup**:
- Plan has 10 total items
- 3 items COMPLETED
- 2 items SCHEDULED
- 5 items READY_FOR_BOOKING

**Steps**:
1. GET plan summary
2. Verify progress.completedItems = 3
3. Verify progress.percentageComplete = 30.0

**Expected**: Accurate progress metrics

---

## Performance Notes

**Optimizations**:
- `JOIN FETCH` for employees, patients (no N+1)
- Lazy loading for phases/items (only load when detail requested)
- Index on `plan_code`, `patient_id`, `status`
- Query result caching for frequently accessed plans

**Query Count**:
- API 5.1 (Summary): 1 query (with JOIN FETCH)
- API 5.2 (Details): 3 queries (plan + phases + items)

---

**Document Version**: 1.0  
**Last Updated**: 2025-11-12  
**Author**: Dental Clinic Development Team
