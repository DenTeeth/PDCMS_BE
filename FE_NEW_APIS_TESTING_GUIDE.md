# Testing Guide - New Treatment Plan APIs

**Version**: V21  
**Date**: November 17, 2025  
**Branch**: `feat/BE-501-manage-treatment-plans`  
**Status**: Ready for Testing ✅

---

## 📋 Overview

This guide covers testing for 3 new APIs that resolve frontend issues:

1. **API 5.12**: Submit Treatment Plan for Review (DRAFT → PENDING_REVIEW)
2. **API 6.6**: List Treatment Plan Templates (with filters)
3. **NEW API**: Manager View All Treatment Plans (system-wide)

---

## 🔧 Prerequisites

### 1. Database Setup
Run the migration script to add new permission:
```sql
-- Option A: Run migration script
\i src/main/resources/db/migration/V21_add_view_all_treatment_plans_permission.sql

-- Option B: Manual execution
INSERT INTO permissions (permission_id, permission_name, module, description, display_order, parent_permission_id, is_active, created_at)
VALUES ('VIEW_ALL_TREATMENT_PLANS', 'VIEW_ALL_TREATMENT_PLANS', 'TREATMENT_PLAN', 
        'Xem TẤT CẢ phác đồ điều trị TOÀN HỆ THỐNG (Quản lý - Manager Dashboard)', 
        266, NULL, TRUE, NOW())
ON CONFLICT (permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
VALUES ('ROLE_MANAGER', 'VIEW_ALL_TREATMENT_PLANS')
ON CONFLICT (role_id, permission_id) DO NOTHING;
```

### 2. Test Users
You'll need accounts with these roles:
- **Doctor** (ROLE_DOCTOR): Can create and submit plans
- **Manager** (ROLE_MANAGER): Can approve plans and view all plans
- **Receptionist** (ROLE_RECEPTIONIST): Can view plans

### 3. Test Data
Ensure you have:
- At least 3-5 treatment plans in different statuses
- At least 2-3 treatment plan templates
- Plans from different doctors
- Plans with different approval statuses

---

## 🧪 Test Cases

---

## **API 5.12: Submit Treatment Plan for Review**

### Endpoint
```
PATCH /api/v1/patient-treatment-plans/{planCode}/submit-for-review
```

### Test Case 1: Successful Submission (Happy Path)
**Preconditions:**
- Login as Doctor (ROLE_DOCTOR)
- Have a treatment plan in DRAFT status
- Plan must have at least 1 phase with items

**Request:**
```bash
curl -X PATCH 'http://localhost:8080/api/v1/patient-treatment-plans/TP-2024-00001/submit-for-review' \
  -H 'Authorization: Bearer {doctor_token}' \
  -H 'Content-Type: application/json' \
  -d '{
    "notes": "Đã hoàn thành phác đồ, xin phê duyệt"
  }'
```

**Expected Result:**
- ✅ Status: `200 OK`
- ✅ Response includes updated plan with:
  - `approvalStatus`: `PENDING_REVIEW`
  - `submittedAt`: Current timestamp
  - `submittedBy`: Doctor's employee code
  - Plan details unchanged

**Sample Response:**
```json
{
  "planCode": "TP-2024-00001",
  "approvalStatus": "PENDING_REVIEW",
  "submittedAt": "2025-11-17T10:30:00",
  "submittedBy": "NV-2001",
  "notes": "Đã hoàn thành phác đồ, xin phê duyệt"
}
```

---

### Test Case 2: Submit Without Notes (Optional Field)
**Request:**
```bash
curl -X PATCH 'http://localhost:8080/api/v1/patient-treatment-plans/TP-2024-00002/submit-for-review' \
  -H 'Authorization: Bearer {doctor_token}' \
  -H 'Content-Type: application/json'
```

**Expected Result:**
- ✅ Status: `200 OK`
- ✅ Plan submitted successfully without notes

---

### Test Case 3: Submit Plan Not in DRAFT Status (Error)
**Preconditions:**
- Plan is already in PENDING_REVIEW, APPROVED, or REJECTED status

**Expected Result:**
- ❌ Status: `409 Conflict`
- ❌ Error message: "Chỉ có thể gửi phác đồ ở trạng thái DRAFT"

---

### Test Case 4: Submit Empty Plan (Error)
**Preconditions:**
- Plan is in DRAFT but has no phases or no items

**Expected Result:**
- ❌ Status: `400 Bad Request`
- ❌ Error message: "Phác đồ phải có ít nhất một phase và item"

---

### Test Case 5: Submit Non-Existent Plan (Error)
**Request:**
```bash
curl -X PATCH 'http://localhost:8080/api/v1/patient-treatment-plans/INVALID-CODE/submit-for-review' \
  -H 'Authorization: Bearer {doctor_token}'
```

**Expected Result:**
- ❌ Status: `404 Not Found`
- ❌ Error message: "Treatment plan not found"

---

### Test Case 6: Permission Check
**Scenario A: Patient tries to submit (should fail)**
```bash
curl -X PATCH 'http://localhost:8080/api/v1/patient-treatment-plans/TP-2024-00001/submit-for-review' \
  -H 'Authorization: Bearer {patient_token}'
```
**Expected:** ❌ `403 Forbidden`

**Scenario B: Manager tries to submit (should succeed)**
```bash
curl -X PATCH 'http://localhost:8080/api/v1/patient-treatment-plans/TP-2024-00001/submit-for-review' \
  -H 'Authorization: Bearer {manager_token}'
```
**Expected:** ✅ `200 OK` (Manager has CREATE_TREATMENT_PLAN permission)

---

## **API 6.6: List Treatment Plan Templates**

### Endpoint
```
GET /api/v1/treatment-plan-templates
```

### Test Case 1: Get All Active Templates (Default)
**Request:**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plan-templates?page=0&size=10' \
  -H 'Authorization: Bearer {doctor_token}'
```

**Expected Result:**
- ✅ Status: `200 OK`
- ✅ Response contains paginated list of templates
- ✅ Only active templates returned (isActive=true)
- ✅ Each template includes:
  - `templateId`, `templateCode`, `templateName`
  - `description`, `totalCost`, `estimatedDuration`
  - `specialization` info
  - `isActive`, `createdAt`

**Sample Response:**
```json
{
  "content": [
    {
      "templateId": 1,
      "templateCode": "TPL-001",
      "templateName": "Implant + Niềng răng",
      "description": "Phác đồ kết hợp implant và niềng răng",
      "totalCost": 50000000,
      "estimatedDuration": 12,
      "isActive": true,
      "specialization": {
        "specializationId": 1,
        "specializationName": "Nha khoa tổng quát"
      },
      "createdAt": "2024-01-15T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 15,
  "totalPages": 2
}
```

---

### Test Case 2: Filter by Active Status
**Request:**
```bash
# Get only active templates
curl -X GET 'http://localhost:8080/api/v1/treatment-plan-templates?isActive=true' \
  -H 'Authorization: Bearer {doctor_token}'

# Get only inactive templates
curl -X GET 'http://localhost:8080/api/v1/treatment-plan-templates?isActive=false' \
  -H 'Authorization: Bearer {doctor_token}'
```

**Expected Result:**
- ✅ First request returns only active templates
- ✅ Second request returns only inactive templates

---

### Test Case 3: Filter by Specialization
**Request:**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plan-templates?specializationId=1&page=0&size=10' \
  -H 'Authorization: Bearer {doctor_token}'
```

**Expected Result:**
- ✅ Returns only templates for specialization ID 1
- ✅ All templates have matching `specialization.specializationId`

---

### Test Case 4: Combined Filters
**Request:**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plan-templates?isActive=true&specializationId=2&page=0&size=20&sort=templateName,asc' \
  -H 'Authorization: Bearer {doctor_token}'
```

**Expected Result:**
- ✅ Returns active templates for specialization 2
- ✅ Results sorted alphabetically by name
- ✅ Page size = 20

---

### Test Case 5: Pagination
**Request:**
```bash
# First page
curl -X GET 'http://localhost:8080/api/v1/treatment-plan-templates?page=0&size=5' \
  -H 'Authorization: Bearer {doctor_token}'

# Second page
curl -X GET 'http://localhost:8080/api/v1/treatment-plan-templates?page=1&size=5' \
  -H 'Authorization: Bearer {doctor_token}'
```

**Expected Result:**
- ✅ First request returns items 0-4
- ✅ Second request returns items 5-9
- ✅ `totalElements` same in both responses
- ✅ `pageNumber` different (0 vs 1)

---

### Test Case 6: Permission Check
**Scenario: Patient tries to list templates (should fail)**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plan-templates' \
  -H 'Authorization: Bearer {patient_token}'
```
**Expected:** ❌ `403 Forbidden` (Requires CREATE_TREATMENT_PLAN permission)

---

## **NEW API: Manager View All Treatment Plans**

### Endpoint
```
GET /api/v1/treatment-plans
```

### Test Case 1: Manager Gets All Plans (Happy Path)
**Request:**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plans?page=0&size=10' \
  -H 'Authorization: Bearer {manager_token}'
```

**Expected Result:**
- ✅ Status: `200 OK`
- ✅ Returns plans from ALL patients (not filtered by patient)
- ✅ Each plan includes:
  - Plan info: `planCode`, `planName`, `status`, `approvalStatus`
  - Patient summary: `patientCode`, `fullName`, `phone`
  - Doctor summary: `employeeCode`, `fullName`
  - Financial: `totalPrice`, `finalCost`
  - Dates: `startDate`, `expectedEndDate`, `createdAt`
  - Approval info: `approvedByName`, `approvedAt`

**Sample Response:**
```json
{
  "content": [
    {
      "planCode": "TP-2024-00001",
      "planName": "Implant + Niềng răng",
      "patient": {
        "patientCode": "BN-1001",
        "fullName": "Nguyễn Văn A",
        "phone": "0901234567"
      },
      "doctor": {
        "employeeCode": "NV-2001",
        "fullName": "BS. Trần Thị B"
      },
      "status": "ACTIVE",
      "approvalStatus": "APPROVED",
      "totalPrice": 50000000,
      "finalCost": 48000000,
      "startDate": "2024-11-01",
      "expectedEndDate": "2025-05-01",
      "createdAt": "2024-11-01T10:00:00",
      "approvedByName": "Quản lý C",
      "approvedAt": "2024-11-02T09:30:00"
    }
  ],
  "totalElements": 45,
  "totalPages": 5,
  "number": 0,
  "size": 10
}
```

---

### Test Case 2: Filter by Approval Status
**Scenario A: Get pending approvals (Approval Queue)**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plans?approvalStatus=PENDING_REVIEW&page=0&size=20' \
  -H 'Authorization: Bearer {manager_token}'
```
**Expected:** ✅ Only plans with `approvalStatus=PENDING_REVIEW`

**Scenario B: Get approved plans**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plans?approvalStatus=APPROVED' \
  -H 'Authorization: Bearer {manager_token}'
```
**Expected:** ✅ Only plans with `approvalStatus=APPROVED`

**Scenario C: Get rejected plans**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plans?approvalStatus=REJECTED' \
  -H 'Authorization: Bearer {manager_token}'
```
**Expected:** ✅ Only plans with `approvalStatus=REJECTED`

---

### Test Case 3: Filter by Plan Status
**Request:**
```bash
# Get active plans
curl -X GET 'http://localhost:8080/api/v1/treatment-plans?status=ACTIVE' \
  -H 'Authorization: Bearer {manager_token}'

# Get completed plans
curl -X GET 'http://localhost:8080/api/v1/treatment-plans?status=COMPLETED' \
  -H 'Authorization: Bearer {manager_token}'
```

**Expected Result:**
- ✅ First request returns only ACTIVE plans
- ✅ Second request returns only COMPLETED plans

---

### Test Case 4: Filter by Doctor
**Request:**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plans?doctorEmployeeCode=NV-2001&page=0&size=10' \
  -H 'Authorization: Bearer {manager_token}'
```

**Expected Result:**
- ✅ Returns only plans created by doctor NV-2001
- ✅ All plans have `doctor.employeeCode = "NV-2001"`

---

### Test Case 5: Combined Filters (Real-World Scenario)
**Scenario: Manager wants to see all pending approvals from a specific doctor**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plans?approvalStatus=PENDING_REVIEW&doctorEmployeeCode=NV-2001&page=0&size=20&sort=createdAt,desc' \
  -H 'Authorization: Bearer {manager_token}'
```

**Expected Result:**
- ✅ Returns pending plans from doctor NV-2001
- ✅ Sorted by creation date (newest first)
- ✅ Perfect for approval workflow

---

### Test Case 6: Pagination and Sorting
**Request:**
```bash
# Sort by creation date (newest first)
curl -X GET 'http://localhost:8080/api/v1/treatment-plans?page=0&size=10&sort=createdAt,desc' \
  -H 'Authorization: Bearer {manager_token}'

# Sort by total price (highest first)
curl -X GET 'http://localhost:8080/api/v1/treatment-plans?page=0&size=10&sort=totalPrice,desc' \
  -H 'Authorization: Bearer {manager_token}'
```

**Expected Result:**
- ✅ First request: Plans sorted by date
- ✅ Second request: Plans sorted by price

---

### Test Case 7: Permission Check - Critical!
**Scenario A: Doctor tries to access (should fail)**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plans' \
  -H 'Authorization: Bearer {doctor_token}'
```
**Expected:** ❌ `403 Forbidden` (Doctor doesn't have VIEW_ALL_TREATMENT_PLANS)

**Scenario B: Receptionist tries to access (should fail)**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plans' \
  -H 'Authorization: Bearer {receptionist_token}'
```
**Expected:** ❌ `403 Forbidden`

**Scenario C: Manager accesses (should succeed)**
```bash
curl -X GET 'http://localhost:8080/api/v1/treatment-plans' \
  -H 'Authorization: Bearer {manager_token}'
```
**Expected:** ✅ `200 OK` with all plans

---

## 🔍 Verification Checklist

### API 5.12: Submit for Review
- [ ] Can submit DRAFT plan with notes
- [ ] Can submit DRAFT plan without notes
- [ ] Cannot submit non-DRAFT plan (409 error)
- [ ] Cannot submit empty plan (400 error)
- [ ] Plan status changes to PENDING_REVIEW
- [ ] Audit log created with action "SUBMITTED_FOR_REVIEW"
- [ ] Only users with CREATE_TREATMENT_PLAN or UPDATE_TREATMENT_PLAN can submit
- [ ] Patients cannot submit (403 error)

### API 6.6: List Templates
- [ ] Can list all active templates
- [ ] Can filter by isActive
- [ ] Can filter by specializationId
- [ ] Can combine filters
- [ ] Pagination works correctly
- [ ] Sorting works correctly
- [ ] Response is lightweight (no phase/service details)
- [ ] Only users with CREATE_TREATMENT_PLAN can access

### NEW API: Manager View All Plans
- [ ] Manager can view all plans across all patients
- [ ] Can filter by approvalStatus (PENDING_REVIEW, APPROVED, REJECTED)
- [ ] Can filter by status (ACTIVE, COMPLETED, etc.)
- [ ] Can filter by doctorEmployeeCode
- [ ] Can combine multiple filters
- [ ] Pagination works correctly
- [ ] Sorting works correctly
- [ ] Response includes patient and doctor summaries
- [ ] Response is lightweight (no phase/item details)
- [ ] **CRITICAL**: Only ROLE_MANAGER can access (403 for others)

---

## 🐛 Common Issues & Troubleshooting

### Issue 1: 403 Forbidden on Manager API
**Symptom:** Manager gets 403 when calling `/treatment-plans`

**Solution:**
```sql
-- Verify permission exists
SELECT * FROM permissions WHERE permission_id = 'VIEW_ALL_TREATMENT_PLANS';

-- Verify role assignment
SELECT * FROM role_permissions WHERE permission_id = 'VIEW_ALL_TREATMENT_PLANS';

-- If missing, run migration script
\i src/main/resources/db/migration/V21_add_view_all_treatment_plans_permission.sql
```

### Issue 2: Empty Patient/Doctor Info
**Symptom:** Patient or doctor fields are null in response

**Solution:** Check database joins. Query uses LEFT JOIN FETCH to load related entities.

### Issue 3: Slow Performance
**Symptom:** API takes >2 seconds to respond

**Solution:** 
- Check database indexes on foreign keys
- Verify LEFT JOIN FETCH is working (should be 1 query, not N+1)
- Review pagination size (keep under 50 items per page)

---

## 📊 Performance Benchmarks

Expected response times:
- **API 5.12** (Submit): < 500ms
- **API 6.6** (List Templates): < 300ms
- **Manager API** (List All Plans): < 800ms (with 10 items/page)

If response times exceed 2x these values, investigate database performance.

---

## ✅ Sign-Off Checklist

Before marking as complete:
- [ ] All test cases pass
- [ ] Permissions work correctly
- [ ] Error messages are user-friendly
- [ ] Response format matches documentation
- [ ] Performance is acceptable
- [ ] Swagger documentation is accurate
- [ ] Frontend team has tested integration

---

## 📞 Support

**Questions?** Contact:
- Backend Team: Check `FE_ISSUES_BACKEND_RESPONSE.md` for detailed analysis
- Implementation Details: See `FE_NEW_APIS_IMPLEMENTED.md`
- Database Schema: Check `src/main/resources/db/schema.sql`

**Found a bug?** Report with:
1. API endpoint and method
2. Request payload (if applicable)
3. Expected vs actual response
4. User role and permissions
5. Server logs (if available)

---

**Last Updated**: November 17, 2025  
**Tested By**: _[Your Name]_  
**Status**: ⏳ Pending Testing
