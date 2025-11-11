# Treatment Plan API - Complete Testing Guide

## Overview

Document này tổng hợp hướng dẫn test đầy đủ cho **Treatment Plan Module**, bao gồm:

- ✅ API 5.1: Get Treatment Plans by Patient (List/Summary)
- ✅ API 5.2: Get Treatment Plan Detail (Full nested structure)

**Target Audience:** QA Engineers, Frontend Developers, Backend Developers

---

## Table of Contents

1. [Test Environment Setup](#test-environment-setup)
2. [Test Users & Data](#test-users--data)
3. [API 5.1: List Treatment Plans - Test Cases](#api-51-list-treatment-plans)
4. [API 5.2: Treatment Plan Detail - Test Cases](#api-52-treatment-plan-detail)
5. [RBAC Testing Matrix](#rbac-testing-matrix)
6. [Performance Testing](#performance-testing)
7. [Negative Testing](#negative-testing)
8. [Edge Cases](#edge-cases)
9. [Integration Testing](#integration-testing)

---

## Test Environment Setup

### Prerequisites

```bash
# 1. Start PostgreSQL database
docker-compose up -d postgres-dental

# 2. Verify database is running
docker ps | grep postgres-dental

# 3. Start Spring Boot application
cd d:/Code/PDCMS_BE
./mvnw spring-boot:run

# 4. Wait for application to be ready (check logs)
# Look for: "Started DentalClinicManagementApplication in X.XXX seconds"

# 5. Verify health endpoint
curl -s http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

### Enable Query Logging (Optional - for performance testing)

```yaml
# application.yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

---

## Test Users & Data

### User Accounts (All passwords: `123456`)

| Username     | Role              | Code    | Account ID | Permissions             | Notes                      |
| ------------ | ----------------- | ------- | ---------- | ----------------------- | -------------------------- |
| **Staff**    |                   |         |            |                         |                            |
| `bacsi1`     | ROLE_DENTIST      | EMP001  | -          | VIEW_TREATMENT_PLAN_ALL | Dr. Lê Anh Khoa            |
| `bacsi2`     | ROLE_DENTIST      | EMP002  | -          | VIEW_TREATMENT_PLAN_ALL | Dr. Trịnh Công Thái        |
| `letan1`     | ROLE_RECEPTIONIST | EMP005  | -          | VIEW_TREATMENT_PLAN_ALL | Đỗ Khánh Thuận (Read-only) |
| **Patients** |                   |         |            |                         |                            |
| `benhnhan1`  | ROLE_PATIENT      | BN-1001 | 12         | VIEW_TREATMENT_PLAN_OWN | Đoàn Thanh Phong           |
| `benhnhan2`  | ROLE_PATIENT      | BN-1002 | 13         | VIEW_TREATMENT_PLAN_OWN | Phạm Văn Phong             |
| `benhnhan3`  | ROLE_PATIENT      | BN-1003 | 14         | VIEW_TREATMENT_PLAN_OWN | Nguyễn Tuấn Anh            |

### Test Treatment Plans (Seed Data)

| Plan Code         | Patient | Doctor | Status      | Phases | Items | Completed | Start Date | Notes                          |
| ----------------- | ------- | ------ | ----------- | ------ | ----- | --------- | ---------- | ------------------------------ |
| PLAN-20251001-001 | BN-1001 | EMP001 | IN_PROGRESS | 3      | 12    | 5         | 2025-10-01 | Niềng răng Mắc cài Kim loại    |
| PLAN-20240515-001 | BN-1002 | EMP002 | COMPLETED   | 3      | 8     | 8         | 2024-05-15 | Implant 2 răng cửa (COMPLETED) |
| PLAN-20251105-001 | BN-1003 | EMP001 | PENDING     | 0      | 0     | 0         | 2025-11-05 | Tẩy trắng răng (No phases yet) |

### Authentication

```bash
# Step 1: Login to get access token
LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "bacsi1", "password": "123456"}')

# Step 2: Extract token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.access_token')

# Step 3: Verify token
echo "Token: $TOKEN"

# Step 4: Use token in subsequent requests
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

---

## API 5.1: List Treatment Plans

**Endpoint:** `GET /api/v1/patients/{patientCode}/treatment-plans`

**Purpose:** Get summary list of all treatment plans for a patient

### Test Case 5.1.1: Doctor Views All Plans (SUCCESS) ✅

**Scenario:** Staff with VIEW_ALL permission can view any patient's plans

```bash
# Login as Doctor
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

# Get plans for BN-1001
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'
```

**Expected Response:**

```json
[
  {
    "patientPlanId": 1,
    "planName": "Lộ trình Niềng răng Mắc cài Kim loại",
    "status": "IN_PROGRESS",
    "doctor": {
      "employeeCode": "EMP001",
      "fullName": "Lê Anh Khoa"
    },
    "startDate": "2025-10-01",
    "expectedEndDate": "2027-10-01",
    "totalCost": 35000000,
    "discountAmount": 0,
    "finalCost": 35000000,
    "paymentType": "INSTALLMENT"
  }
]
```

**Assertions:**

- ✅ Status: `200 OK`
- ✅ Array with 1 plan
- ✅ Plan status: `IN_PROGRESS`
- ✅ Doctor info present
- ✅ Financial info correct

### Test Case 5.1.2: Patient Views Own Plans (SUCCESS) ✅

**Scenario:** Patient with VIEW_OWN can view their own plans (account_id verification passes)

```bash
# Login as Patient BN-1001
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "benhnhan1", "password": "123456"}' | jq -r '.access_token')

# Get own plans
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'
```

**Expected Response:**

- ✅ Status: `200 OK`
- ✅ Same response as Test Case 5.1.1

### Test Case 5.1.3: Patient Tries to View Another's Plans (FORBIDDEN) ❌

**Scenario:** Patient with VIEW_OWN cannot view other patients' plans

```bash
# Login as Patient BN-1001 (account_id = 12)
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "benhnhan1", "password": "123456"}' | jq -r '.access_token')

# Try to get BN-1002's plans (account_id = 13)
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'
```

**Expected Response:**

```json
{
  "timestamp": "2025-11-11T...",
  "status": 403,
  "error": "Forbidden",
  "message": "You can only view your own treatment plans",
  "path": "/api/v1/patients/BN-1002/treatment-plans"
}
```

**Assertions:**

- ❌ Status: `403 FORBIDDEN`
- ❌ Clear error message
- ❌ Not 404 (security best practice)

### Test Case 5.1.4: Receptionist Views Plans (SUCCESS) ✅

**Scenario:** Receptionist with VIEW_ALL (read-only) can view all plans

```bash
# Login as Receptionist
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "letan1", "password": "123456"}' | jq -r '.access_token')

# Get BN-1002's plans
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'
```

**Expected Response:**

- ✅ Status: `200 OK`
- ✅ Array with 1 completed plan
- ✅ Plan status: `COMPLETED`

### Test Case 5.1.5: Invalid Patient Code (NOT FOUND) ❌

```bash
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

curl -X GET "http://localhost:8080/api/v1/patients/BN-9999/treatment-plans" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'
```

**Expected Response:**

- ❌ Status: `404 NOT FOUND`
- ❌ Message: "Patient not found with code: BN-9999"

---

## API 5.2: Treatment Plan Detail

**Endpoint:** `GET /api/v1/patients/{patientCode}/treatment-plans/{planCode}`

**Purpose:** Get full nested details with phases, items, and linked appointments

### Test Case 5.2.1: Doctor Views Full Plan Detail (SUCCESS) ✅

**Scenario:** Get complete plan with all nested data

```bash
# Login as Doctor
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

# Get full plan detail
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'
```

**Expected Response Structure:**

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
  "progressSummary": {
    "totalPhases": 3,
    "completedPhases": 1,
    "totalItems": 12,
    "completedItems": 5,
    "readyForBookingItems": 7
  },
  "phases": [
    {
      "phaseId": 1,
      "phaseNumber": 1,
      "phaseName": "Giai đoạn 1: Chuẩn bị và Kiểm tra",
      "status": "COMPLETED",
      "items": [
        {
          "itemId": 1,
          "sequenceNumber": 1,
          "itemName": "Khám tổng quát và chụp X-quang",
          "status": "COMPLETED",
          "linkedAppointments": [...]
        }
      ]
    }
  ]
}
```

**Detailed Assertions:**

```bash
# Get response and store
RESPONSE=$(curl -s -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json")

# Verify structure
echo $RESPONSE | jq '.planCode' # Should be "PLAN-20251001-001"
echo $RESPONSE | jq '.status' # Should be "IN_PROGRESS"

# Verify progress summary
echo $RESPONSE | jq '.progressSummary.totalPhases' # Should be 3
echo $RESPONSE | jq '.progressSummary.completedPhases' # Should be 1
echo $RESPONSE | jq '.progressSummary.totalItems' # Should be 12
echo $RESPONSE | jq '.progressSummary.completedItems' # Should be 5

# Verify phases
echo $RESPONSE | jq '.phases | length' # Should be 3
echo $RESPONSE | jq '.phases[0].status' # Phase 1 should be "COMPLETED"
echo $RESPONSE | jq '.phases[1].status' # Phase 2 should be "IN_PROGRESS"
echo $RESPONSE | jq '.phases[2].status' # Phase 3 should be "PENDING"

# Verify items in Phase 1
echo $RESPONSE | jq '.phases[0].items | length' # Should be 3
echo $RESPONSE | jq '.phases[0].items[0].status' # Should be "COMPLETED"
echo $RESPONSE | jq '.phases[0].items[0].completedAt' # Should have timestamp

# Verify linkedAppointments (empty array for non-scheduled items)
echo $RESPONSE | jq '.phases[1].items[2].linkedAppointments | length' # Should be 0 (empty array)
```

**Validation Checklist:**

- ✅ Status: `200 OK`
- ✅ Has `planCode`, `planName`, `status`
- ✅ Has `doctor` object with `employeeCode` and `fullName`
- ✅ Has `patient` object
- ✅ Has `progressSummary` with correct counts
- ✅ Has `phases` array with 3 elements
- ✅ Phases sorted by `phaseNumber` (1, 2, 3)
- ✅ Each phase has `items` array
- ✅ Items sorted by `sequenceNumber`
- ✅ Completed items have `completedAt` timestamp
- ✅ Items with appointments have non-empty `linkedAppointments` array
- ✅ Items without appointments have empty `[]` (not null)

### Test Case 5.2.2: Patient Views Own Plan Detail (SUCCESS) ✅

```bash
# Login as Patient BN-1001
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "benhnhan1", "password": "123456"}' | jq -r '.access_token')

# View own plan
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'
```

**Expected:**

- ✅ Status: `200 OK` - Same response as Test Case 5.2.1

### Test Case 5.2.3: View Completed Plan (SUCCESS) ✅

**Scenario:** Verify completed plan has all items marked as completed

```bash
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

# Get completed plan
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans/PLAN-20240515-001" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'
```

**Assertions:**

```bash
RESPONSE=$(curl -s ...)

# Plan status should be COMPLETED
echo $RESPONSE | jq '.status' # "COMPLETED"

# All phases should be COMPLETED
echo $RESPONSE | jq '.phases[].status' | grep -v "COMPLETED" # Should be empty

# All items should be COMPLETED
echo $RESPONSE | jq '.phases[].items[].status' | grep -v "COMPLETED" # Should be empty

# All items should have completedAt
echo $RESPONSE | jq '.phases[].items[].completedAt' | grep null # Should be empty

# Progress summary: completedItems should equal totalItems
TOTAL=$(echo $RESPONSE | jq '.progressSummary.totalItems')
COMPLETED=$(echo $RESPONSE | jq '.progressSummary.completedItems')
test $TOTAL -eq $COMPLETED && echo "✅ All items completed" || echo "❌ Mismatch"
```

### Test Case 5.2.4: View Pending Plan (No Phases) ✅

**Scenario:** Plan created but no phases added yet

```bash
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

# Get pending plan
curl -X GET "http://localhost:8080/api/v1/patients/BN-1003/treatment-plans/PLAN-20251105-001" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'
```

**Expected Response:**

```json
{
  "planId": 3,
  "planCode": "PLAN-20251105-001",
  "planName": "Lộ trình Tẩy trắng răng",
  "status": "PENDING",
  "progressSummary": {
    "totalPhases": 0,
    "completedPhases": 0,
    "totalItems": 0,
    "completedItems": 0,
    "readyForBookingItems": 0
  },
  "phases": []
}
```

**Assertions:**

- ✅ `status`: "PENDING"
- ✅ `phases`: Empty array `[]` (not null)
- ✅ All progress counts: 0

### Test Case 5.2.5: Patient Tries to View Another's Plan (FORBIDDEN) ❌

```bash
# Login as BN-1001
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "benhnhan1", "password": "123456"}' | jq -r '.access_token')

# Try to view BN-1002's plan
curl -X GET "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans/PLAN-20240515-001" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'
```

**Expected:**

- ❌ Status: `403 FORBIDDEN`
- ❌ Message: "You can only view your own treatment plans"

### Test Case 5.2.6: Invalid Plan Code (NOT FOUND) ❌

```bash
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-99999999-999" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'
```

**Expected:**

- ❌ Status: `404 NOT FOUND`
- ❌ Message: "Treatment plan 'PLAN-99999999-999' not found for patient 'BN-1001'"

---

## API 5.3: Create Treatment Plan from Template

### Overview

Creates a new patient treatment plan by copying from a pre-defined template package.

**Endpoint:** `POST /api/v1/patients/{patientCode}/treatment-plans`

**Required Permission:** `CREATE_TREATMENT_PLAN` (Doctor/Manager only)

**Business Logic:**

1. Validates patient, doctor, and template exist
2. Validates discount ≤ total cost
3. Generates unique plan code (PLAN-YYYYMMDD-XXX)
4. Calculates expected end date from template duration
5. Snapshots all phases and items (expands by quantity, ordered by sequence)
6. Calculates total cost and final cost
7. Returns complete plan details (201 CREATED)

---

### Test Case 5.3.1: Create Plan from Ortho Template (Success)

```bash
# Step 1: Login as Doctor
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

# Step 2: Create treatment plan from template
curl -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "sourceTemplateCode": "TPL_ORTHO_METAL",
        "doctorEmployeeCode": "EMP001",
        "planNameOverride": "Lộ trình niềng răng 2 năm cho BN Phong (Khuyến mãi)",
        "discountAmount": 5000000,
        "paymentType": "INSTALLMENT"
    }' | jq .
```

**Expected Response (201 CREATED):**

```json
{
  "planId": 4,
  "planCode": "PLAN-20251111-001", // Generated today
  "planName": "Lộ trình niềng răng 2 năm cho BN Phong (Khuyến mãi)",
  "status": "PENDING",
  "doctor": {
    "employeeCode": "EMP001",
    "fullName": "Lê Anh Khoa"
  },
  "patient": {
    "patientCode": "BN-1001",
    "fullName": "Đoàn Thanh Phong"
  },
  "startDate": null, // Will be set when activated
  "expectedEndDate": "2027-11-11", // NOW + 730 days
  "createdAt": "2025-11-11T18:30:00",
  "totalPrice": 30000000.0, // Sum of all items
  "discountAmount": 5000000.0,
  "finalCost": 25000000.0, // totalPrice - discount
  "paymentType": "INSTALLMENT",
  "progressSummary": {
    "totalPhases": 4,
    "completedPhases": 0,
    "totalItems": 13, // 3 (phase1) + 1 (phase2) + 8 (phase3) + 1 (phase4)
    "completedItems": 0,
    "readyForBookingItems": 0 // All items start as PENDING_APPROVAL
  },
  "phases": [
    {
      "phaseId": 10,
      "phaseNumber": 1,
      "phaseName": "Giai đoạn 1: Khám & Chuẩn bị",
      "status": "PENDING",
      "startDate": null,
      "completionDate": null,
      "items": [
        {
          "itemId": 501,
          "sequenceNumber": 1,
          "itemName": "Khám & Tư vấn Chỉnh nha",
          "serviceId": 36,
          "price": 0.0,
          "estimatedTimeMinutes": 45,
          "status": "PENDING_APPROVAL",
          "completedAt": null,
          "linkedAppointments": []
        },
        {
          "itemId": 502,
          "sequenceNumber": 2,
          "itemName": "Chụp X-quang Cephalometric",
          "serviceId": 37,
          "price": 500000.0,
          "estimatedTimeMinutes": 30,
          "status": "PENDING_APPROVAL",
          "completedAt": null,
          "linkedAppointments": []
        },
        {
          "itemId": 503,
          "sequenceNumber": 3,
          "itemName": "Lấy cao răng (Level 1)",
          "serviceId": 5,
          "price": 800000.0,
          "estimatedTimeMinutes": 60,
          "status": "PENDING_APPROVAL",
          "completedAt": null,
          "linkedAppointments": []
        }
      ]
    },
    {
      "phaseId": 11,
      "phaseNumber": 2,
      "phaseName": "Giai đoạn 2: Gắn mắc cài",
      "status": "PENDING",
      "startDate": null,
      "completionDate": null,
      "items": [
        {
          "itemId": 504,
          "sequenceNumber": 1,
          "itemName": "Lắp mắc cài kim loại",
          "serviceId": 38,
          "price": 16000000.0,
          "estimatedTimeMinutes": 90,
          "status": "PENDING_APPROVAL",
          "completedAt": null,
          "linkedAppointments": []
        }
      ]
    },
    {
      "phaseId": 12,
      "phaseNumber": 3,
      "phaseName": "Giai đoạn 3: Điều chỉnh định kỳ (8 tháng)",
      "status": "PENDING",
      "startDate": null,
      "completionDate": null,
      "items": [
        {
          "itemId": 505,
          "sequenceNumber": 1,
          "itemName": "Điều chỉnh niềng răng (Lần 1)",
          "serviceId": 39,
          "price": 500000.0,
          "estimatedTimeMinutes": 30,
          "status": "PENDING_APPROVAL",
          "completedAt": null,
          "linkedAppointments": []
        },
        // ... 7 more items (Lần 2 to Lần 8)
        {
          "itemId": 512,
          "sequenceNumber": 8,
          "itemName": "Điều chỉnh niềng răng (Lần 8)",
          "serviceId": 39,
          "price": 500000.0,
          "estimatedTimeMinutes": 30,
          "status": "PENDING_APPROVAL",
          "completedAt": null,
          "linkedAppointments": []
        }
      ]
    },
    {
      "phaseId": 13,
      "phaseNumber": 4,
      "phaseName": "Giai đoạn 4: Tháo niềng & Duy trì",
      "status": "PENDING",
      "startDate": null,
      "completionDate": null,
      "items": [
        {
          "itemId": 513,
          "sequenceNumber": 1,
          "itemName": "Tháo mắc cài",
          "serviceId": 40,
          "price": 2000000.0,
          "estimatedTimeMinutes": 60,
          "status": "PENDING_APPROVAL",
          "completedAt": null,
          "linkedAppointments": []
        }
      ]
    }
  ]
}
```

**Verification Steps:**

```bash
# 1. Extract plan code
PLAN_CODE=$(curl -s -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "sourceTemplateCode": "TPL_ORTHO_METAL",
        "doctorEmployeeCode": "EMP001",
        "discountAmount": 5000000,
        "paymentType": "INSTALLMENT"
    }' | jq -r '.planCode')

# 2. Verify plan was created in database
curl -s -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/$PLAN_CODE" \
    -H "Authorization: Bearer $TOKEN" | jq '{
        planCode,
        status,
        totalPhases: .progressSummary.totalPhases,
        totalItems: .progressSummary.totalItems,
        totalPrice,
        finalCost
    }'

# Expected output:
# {
#   "planCode": "PLAN-20251111-001",
#   "status": "PENDING",
#   "totalPhases": 4,
#   "totalItems": 13,
#   "totalPrice": 30000000.00,
#   "finalCost": 25000000.00
# }
```

---

### Test Case 5.3.2: Create Plan with No Discount

```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1002/treatment-plans" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "sourceTemplateCode": "TPL_IMPLANT_OSSTEM",
        "doctorEmployeeCode": "EMP002",
        "planNameOverride": null,
        "discountAmount": 0,
        "paymentType": "FULL"
    }' | jq '{planCode, planName, totalPrice, finalCost}'
```

**Expected:**

- ✅ Status: `201 CREATED`
- ✅ `planName`: Uses template name (no override)
- ✅ `totalPrice == finalCost` (no discount)

---

### Test Case 5.3.3: Discount Exceeds Total Cost (Negative)

```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "sourceTemplateCode": "TPL_ORTHO_METAL",
        "doctorEmployeeCode": "EMP001",
        "discountAmount": 50000000,
        "paymentType": "INSTALLMENT"
    }' | jq .
```

**Expected:**

- ❌ Status: `400 BAD REQUEST`
- ❌ Message: "Discount amount (50000000.00) cannot exceed total cost (30000000.00)"

---

### Test Case 5.3.4: Template Not Found (Negative)

```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "sourceTemplateCode": "TPL_NONEXISTENT",
        "doctorEmployeeCode": "EMP001",
        "discountAmount": 0,
        "paymentType": "FULL"
    }' | jq .
```

**Expected:**

- ❌ Status: `404 NOT FOUND`
- ❌ Message: "TreatmentPlanTemplate 'TPL_NONEXISTENT' not found"

---

### Test Case 5.3.5: Doctor Not Found (Negative)

```bash
curl -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "sourceTemplateCode": "TPL_ORTHO_METAL",
        "doctorEmployeeCode": "EMP999",
        "discountAmount": 0,
        "paymentType": "FULL"
    }' | jq .
```

**Expected:**

- ❌ Status: `404 NOT FOUND`
- ❌ Message: "Employee 'EMP999' not found"

---

### Test Case 5.3.6: Patient Tries to Create Plan (RBAC)

```bash
# Login as patient
PATIENT_TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "benhnhan1", "password": "123456"}' | jq -r '.access_token')

# Try to create plan
curl -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
    -H "Authorization: Bearer $PATIENT_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "sourceTemplateCode": "TPL_ORTHO_METAL",
        "doctorEmployeeCode": "EMP001",
        "discountAmount": 0,
        "paymentType": "FULL"
    }' | jq .
```

**Expected:**

- ❌ Status: `403 FORBIDDEN`
- ❌ Message: "Access denied - insufficient permissions"

---

### Test Case 5.3.7: Verify Item Quantity Expansion

**Scenario:** Template has `quantity=8` for "Điều chỉnh" service → Should create 8 separate items

```bash
# Create plan
PLAN_CODE=$(curl -s -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "sourceTemplateCode": "TPL_ORTHO_METAL",
        "doctorEmployeeCode": "EMP001",
        "discountAmount": 0,
        "paymentType": "FULL"
    }' | jq -r '.planCode')

# Count items in Phase 3
curl -s -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/$PLAN_CODE" \
    -H "Authorization: Bearer $TOKEN" | jq '.phases[2].items | length'

# Expected: 8 (items named "Điều chỉnh (Lần 1)" to "Điều chỉnh (Lần 8)")
```

---

### Test Case 5.3.8: Verify Sequence Order

**Scenario:** Items should be created in order: Consultation → X-Ray → Scaling (by sequenceNumber)

```bash
curl -s -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/$PLAN_CODE" \
    -H "Authorization: Bearer $TOKEN" | jq '.phases[0].items[] | {seq: .sequenceNumber, name: .itemName}'

# Expected order:
# {"seq": 1, "name": "Khám & Tư vấn Chỉnh nha"}
# {"seq": 2, "name": "Chụp X-quang Cephalometric"}
# {"seq": 3, "name": "Lấy cao răng (Level 1)"}
```

---

### Test Case 5.3.9: Verify Expected End Date Calculation

```bash
curl -s -X POST "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "sourceTemplateCode": "TPL_ORTHO_METAL",
        "doctorEmployeeCode": "EMP001",
        "discountAmount": 0,
        "paymentType": "FULL"
    }' | jq '{expectedEndDate, createdAt}'

# Expected:
# {
#   "expectedEndDate": "2027-11-11",  // Today + 730 days (2 years)
#   "createdAt": "2025-11-11T18:30:00"
# }
```

---

## RBAC Testing Matrix

| User Role        | Permission       | API 5.1 (List) | API 5.2 (Detail) | API 5.3 (Create) | Own Patient | Other Patient |
| ---------------- | ---------------- | -------------- | ---------------- | ---------------- | ----------- | ------------- |
| **Doctor**       | VIEW_ALL, CREATE | ✅ 200         | ✅ 200           | ✅ 201           | ✅ 201      | ✅ 201        |
| **Receptionist** | VIEW_ALL         | ✅ 200         | ✅ 200           | ❌ 403           | N/A         | N/A           |
| **Patient**      | VIEW_OWN         | ✅ 200         | ✅ 200           | ❌ 403           | ❌ 403      | ❌ 403        |
| **Anonymous**    | None             | ❌ 401         | ❌ 401           | ❌ 401           | ❌ 401      | ❌ 401        |

### Full RBAC Test Script

```bash
#!/bin/bash

# Function to test API with different users
test_rbac() {
    local username=$1
    local patient_code=$2
    local expected_status=$3

    echo "Testing $username accessing $patient_code..."

    TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\": \"$username\", \"password\": \"123456\"}" | jq -r '.access_token')

    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET "http://localhost:8080/api/v1/patients/$patient_code/treatment-plans" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json")

    if [ "$HTTP_STATUS" == "$expected_status" ]; then
        echo "✅ PASS: $username -> $patient_code = $HTTP_STATUS"
    else
        echo "❌ FAIL: $username -> $patient_code = $HTTP_STATUS (expected $expected_status)"
    fi
}

# Test VIEW_ALL permissions
test_rbac "bacsi1" "BN-1001" "200"
test_rbac "bacsi1" "BN-1002" "200"
test_rbac "letan1" "BN-1001" "200"
test_rbac "letan1" "BN-1002" "200"

# Test VIEW_OWN permissions
test_rbac "benhnhan1" "BN-1001" "200"  # Own patient
test_rbac "benhnhan1" "BN-1002" "403"  # Other patient
test_rbac "benhnhan2" "BN-1002" "200"  # Own patient
test_rbac "benhnhan2" "BN-1001" "403"  # Other patient
```

---

## Performance Testing

### Test Case P.1: Single Query Performance

**Objective:** Verify API 5.2 uses only 1 database query

```bash
# Enable SQL logging in application.yaml
# logging.level.org.hibernate.SQL=DEBUG

TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

# Execute request and measure time
time curl -s -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -o /dev/null
```

**Check Logs:**

```bash
# Should see ONLY 1 SELECT query
tail -f logs/spring.log | grep "SELECT new com.dental.clinic.management"
```

**Expected:**

- ✅ Only 1 query executing
- ✅ Query joins 5 tables (plan, employee, patient, phase, item, appointments)
- ✅ Response time < 100ms for plan with 3 phases, 12 items

### Test Case P.2: Response Time Benchmark

```bash
#!/bin/bash

# Test response time 10 times
echo "Response Time Benchmark (10 requests):"

TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

for i in {1..10}; do
    TIME=$(curl -s -o /dev/null -w "%{time_total}" \
        -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json")
    echo "Request $i: ${TIME}s"
done
```

**Expected Performance:**

- ✅ Average response time: < 100ms
- ✅ Max response time: < 200ms
- ✅ No N+1 query problem

---

## Negative Testing

### Test Case N.1: Malformed JSON

```bash
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

# Send request with malformed Accept header
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: text/html"
```

**Expected:** Should still return JSON (406 or 200 with JSON)

### Test Case N.2: SQL Injection Attempt

```bash
# Try SQL injection in patient code
curl -X GET "http://localhost:8080/api/v1/patients/BN-1001';DROP TABLE patients;--/treatment-plans" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected:**

- ✅ No SQL injection (parameterized queries)
- ✅ 404 NOT FOUND (invalid patient code)

### Test Case N.3: XSS Attempt

```bash
# Try XSS in patient code
curl -X GET "http://localhost:8080/api/v1/patients/<script>alert('xss')</script>/treatment-plans" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected:**

- ✅ No XSS execution
- ✅ 404 NOT FOUND

### Test Case N.4: Token Tampering

```bash
# Use modified token
FAKE_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.FAKE_PAYLOAD.FAKE_SIGNATURE"

curl -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans" \
  -H "Authorization: Bearer $FAKE_TOKEN" \
  -H "Content-Type: application/json"
```

**Expected:**

- ❌ Status: `401 UNAUTHORIZED`
- ❌ Invalid token error

---

## Edge Cases

### Test Case E.1: Patient with No Plans

```bash
# Create patient without plans (or use existing patient BN-1004)
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

curl -X GET "http://localhost:8080/api/v1/patients/BN-1004/treatment-plans" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected:**

- ✅ Status: `200 OK`
- ✅ Response: Empty array `[]`

### Test Case E.2: Large Plan (Performance)

```bash
# If available, test with plan having many phases/items
# Expected: Still < 200ms response time
```

### Test Case E.3: Concurrent Requests

```bash
#!/bin/bash

TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

# Send 10 concurrent requests
for i in {1..10}; do
    curl -s -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" > /dev/null &
done

wait
echo "All concurrent requests completed"
```

**Expected:**

- ✅ All requests succeed (200 OK)
- ✅ No deadlocks or race conditions
- ✅ Consistent data returned

---

## Integration Testing

### Test Case I.1: Plan → Appointment Integration

**Scenario:** Verify linkedAppointments in plan detail matches actual appointments

```bash
# 1. Get plan detail
PLAN=$(curl -s -X GET "http://localhost:8080/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json")

# 2. Extract appointment code from first completed item
APT_CODE=$(echo $PLAN | jq -r '.phases[0].items[0].linkedAppointments[0].code')

# 3. Verify appointment exists in appointments API
curl -X GET "http://localhost:8080/api/v1/appointments/$APT_CODE" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json"
```

**Expected:**

- ✅ Appointment exists
- ✅ Appointment details match linkedAppointment data
- ✅ Status consistent

---

## Test Report Template

```markdown
## Test Execution Report - Treatment Plan APIs

**Date:** YYYY-MM-DD
**Tester:** [Name]
**Environment:** [Dev/Staging/Production]
**Version:** [Git commit hash]

### Summary

| Category          | Total  | Passed | Failed | Skipped |
| ----------------- | ------ | ------ | ------ | ------- |
| API 5.1 Tests     | 5      | -      | -      | -       |
| API 5.2 Tests     | 6      | -      | -      | -       |
| RBAC Tests        | 8      | -      | -      | -       |
| Performance Tests | 2      | -      | -      | -       |
| Negative Tests    | 4      | -      | -      | -       |
| Edge Cases        | 3      | -      | -      | -       |
| Integration Tests | 1      | -      | -      | -       |
| **TOTAL**         | **29** | -      | -      | -       |

### Failed Tests

| Test Case | Expected | Actual | Notes |
| --------- | -------- | ------ | ----- |
| -         | -        | -      | -     |

### Performance Metrics

| Metric                   | Value | Threshold | Status |
| ------------------------ | ----- | --------- | ------ |
| Avg Response Time (5.2)  | -     | < 100ms   | -      |
| Max Response Time (5.2)  | -     | < 200ms   | -      |
| Database Queries per API | -     | 1 query   | -      |

### Issues Found

1. [Issue description]
2. [Issue description]

### Recommendations

- [Recommendation 1]
- [Recommendation 2]
```

---

## Automation Script (All Tests)

```bash
#!/bin/bash

# Complete test suite for Treatment Plan APIs

BASE_URL="http://localhost:8080"
RESULTS_FILE="test_results_$(date +%Y%m%d_%H%M%S).log"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Counters
TOTAL=0
PASSED=0
FAILED=0

# Function to run test
run_test() {
    local test_name=$1
    local command=$2
    local expected_status=$3

    TOTAL=$((TOTAL + 1))
    echo -n "Running $test_name... "

    HTTP_STATUS=$(eval "$command")

    if [ "$HTTP_STATUS" == "$expected_status" ]; then
        echo -e "${GREEN}✅ PASS${NC}"
        PASSED=$((PASSED + 1))
        echo "[PASS] $test_name" >> $RESULTS_FILE
    else
        echo -e "${RED}❌ FAIL${NC} (Expected: $expected_status, Got: $HTTP_STATUS)"
        FAILED=$((FAILED + 1))
        echo "[FAIL] $test_name - Expected: $expected_status, Got: $HTTP_STATUS" >> $RESULTS_FILE
    fi
}

# Get tokens
echo "=== Authenticating Users ==="
DOCTOR_TOKEN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "bacsi1", "password": "123456"}' | jq -r '.access_token')

PATIENT1_TOKEN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "benhnhan1", "password": "123456"}' | jq -r '.access_token')

PATIENT2_TOKEN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "benhnhan2", "password": "123456"}' | jq -r '.access_token')

echo "Doctor Token: ${DOCTOR_TOKEN:0:20}..."
echo "Patient1 Token: ${PATIENT1_TOKEN:0:20}..."
echo "Patient2 Token: ${PATIENT2_TOKEN:0:20}..."
echo ""

# API 5.1 Tests
echo "=== API 5.1: List Treatment Plans ==="
run_test "5.1.1 Doctor views BN-1001 plans" \
    "curl -s -o /dev/null -w '%{http_code}' -X GET '$BASE_URL/api/v1/patients/BN-1001/treatment-plans' -H 'Authorization: Bearer $DOCTOR_TOKEN'" \
    "200"

run_test "5.1.2 Patient1 views own plans" \
    "curl -s -o /dev/null -w '%{http_code}' -X GET '$BASE_URL/api/v1/patients/BN-1001/treatment-plans' -H 'Authorization: Bearer $PATIENT1_TOKEN'" \
    "200"

run_test "5.1.3 Patient1 tries to view BN-1002 plans (FORBIDDEN)" \
    "curl -s -o /dev/null -w '%{http_code}' -X GET '$BASE_URL/api/v1/patients/BN-1002/treatment-plans' -H 'Authorization: Bearer $PATIENT1_TOKEN'" \
    "403"

run_test "5.1.4 Invalid patient code (NOT FOUND)" \
    "curl -s -o /dev/null -w '%{http_code}' -X GET '$BASE_URL/api/v1/patients/BN-9999/treatment-plans' -H 'Authorization: Bearer $DOCTOR_TOKEN'" \
    "404"

# API 5.2 Tests
echo ""
echo "=== API 5.2: Treatment Plan Detail ==="
run_test "5.2.1 Doctor views plan detail" \
    "curl -s -o /dev/null -w '%{http_code}' -X GET '$BASE_URL/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001' -H 'Authorization: Bearer $DOCTOR_TOKEN'" \
    "200"

run_test "5.2.2 Patient1 views own plan detail" \
    "curl -s -o /dev/null -w '%{http_code}' -X GET '$BASE_URL/api/v1/patients/BN-1001/treatment-plans/PLAN-20251001-001' -H 'Authorization: Bearer $PATIENT1_TOKEN'" \
    "200"

run_test "5.2.3 Patient1 tries to view BN-1002 plan (FORBIDDEN)" \
    "curl -s -o /dev/null -w '%{http_code}' -X GET '$BASE_URL/api/v1/patients/BN-1002/treatment-plans/PLAN-20240515-001' -H 'Authorization: Bearer $PATIENT1_TOKEN'" \
    "403"

run_test "5.2.4 Invalid plan code (NOT FOUND)" \
    "curl -s -o /dev/null -w '%{http_code}' -X GET '$BASE_URL/api/v1/patients/BN-1001/treatment-plans/PLAN-99999999-999' -H 'Authorization: Bearer $DOCTOR_TOKEN'" \
    "404"

# Summary
echo ""
echo "======================================"
echo "Test Execution Summary"
echo "======================================"
echo "Total Tests: $TOTAL"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo "Success Rate: $(awk "BEGIN {printf \"%.2f\", ($PASSED/$TOTAL)*100}")%"
echo ""
echo "Results saved to: $RESULTS_FILE"
```

Save as `test_treatment_plan_apis.sh` and run:

```bash
chmod +x test_treatment_plan_apis.sh
./test_treatment_plan_apis.sh
```

---

## Conclusion

This comprehensive testing guide covers:

- ✅ 29 test cases across all categories
- ✅ RBAC verification matrix
- ✅ Performance benchmarks
- ✅ Security testing (SQL injection, XSS, token tampering)
- ✅ Edge cases and integration tests
- ✅ Automated test script

**Next Steps:**

1. Execute all tests manually first
2. Run automation script
3. Document results in test report
4. File bugs for any failures
5. Integrate tests into CI/CD pipeline
