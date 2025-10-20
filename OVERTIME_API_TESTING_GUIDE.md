# 🧪 Overtime Request API Testing Guide

## 📋 Prerequisites

### 1. Application Status
- ✅ Application is running successfully
- ✅ Database seed data loaded (permissions + ROLE_MANAGER)
- ✅ Port: Check your `application.yaml` (typically 8080 or 8081)

### 2. Authentication Required
You need JWT tokens for different roles:
- **Admin Token** (ROLE_ADMIN) - Full access
- **Manager Token** (ROLE_MANAGER) - Full overtime management
- **Employee Token** (ROLE_DOCTOR/NURSE/etc) - Limited access

### 3. Test Data Required
From your seed data, you should have:
- Employee IDs (e.g., 5, 6, 7...)
- Work Shift IDs (e.g., "WKS_NIGHT_01", "WKS_MORNING_01")

---

## 🔐 Step 1: Get Authentication Tokens

### Login as Admin
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "your_admin_password"
}
```

### Login as Manager
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "manager_username",
  "password": "manager_password"
}
```

### Login as Employee
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "employee_username",
  "password": "employee_password"
}
```

**Save the JWT token** from the response for use in subsequent requests.

---

## 🧪 Step 2: Test API Endpoints

### Test 1: Create Overtime Request ✅

**Endpoint**: `POST /api/v1/overtime-requests`  
**Permission**: `CREATE_OT`  
**Who can test**: Admin, Manager, Employees

```http
POST http://localhost:8080/api/v1/overtime-requests
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "employeeId": 5,
  "workDate": "2025-11-20",
  "workShiftId": "WKS_NIGHT_01",
  "reason": "Cần tăng ca để hoàn thành dự án khẩn cấp"
}
```

**Expected Response** (201 Created):
```json
{
  "success": true,
  "message": "Overtime request created successfully",
  "data": {
    "requestId": "OTR251120001",
    "employeeId": 5,
    "employeeCode": "EMP005",
    "employeeName": "Nguyen Van Minh",
    "workDate": "2025-11-20",
    "workShiftId": "WKS_NIGHT_01",
    "shiftName": "Ca tối",
    "status": "PENDING",
    "reason": "Cần tăng ca để hoàn thành dự án khẩn cấp",
    "requestedById": 1,
    "requestedByName": "Admin User",
    "createdAt": "2025-10-20T..."
  }
}
```

**Test Cases**:
- ✅ Valid request → Should succeed
- ❌ Past date → Should fail (400)
- ❌ Duplicate request → Should fail (409)
- ❌ Invalid employee → Should fail (404)
- ❌ Invalid work shift → Should fail (404)

---

### Test 2: List All Overtime Requests ✅

**Endpoint**: `GET /api/v1/overtime-requests`  
**Permission**: `VIEW_OT_ALL` or `VIEW_OT_OWN`  
**Who can test**: Everyone with permissions

```http
GET http://localhost:8080/api/v1/overtime-requests?page=0&size=10
Authorization: Bearer <your_jwt_token>
```

**Query Parameters** (Optional):
- `page` - Page number (default: 0)
- `size` - Page size (default: 10)
- `status` - Filter by status (PENDING, APPROVED, REJECTED, CANCELLED)
- `employeeId` - Filter by employee
- `workDate` - Filter by date (YYYY-MM-DD)

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Overtime requests retrieved successfully",
  "data": {
    "content": [
      {
        "requestId": "OTR251120001",
        "employeeId": 5,
        "employeeCode": "EMP005",
        "employeeName": "Nguyen Van Minh",
        "workDate": "2025-11-20",
        "workShiftId": "WKS_NIGHT_01",
        "shiftName": "Ca tối",
        "status": "PENDING",
        "requestedByName": "Admin User",
        "createdAt": "2025-10-20T..."
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

**Permission Test**:
- **Admin/Manager** (VIEW_OT_ALL): Should see ALL overtime requests
- **Employee** (VIEW_OT_OWN): Should see ONLY their own requests

---

### Test 3: Get Overtime Request Details ✅

**Endpoint**: `GET /api/v1/overtime-requests/{requestId}`  
**Permission**: `VIEW_OT_ALL` or `VIEW_OT_OWN`  
**Who can test**: Everyone with permissions

```http
GET http://localhost:8080/api/v1/overtime-requests/OTR251120001
Authorization: Bearer <your_jwt_token>
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Overtime request retrieved successfully",
  "data": {
    "requestId": "OTR251120001",
    "employeeId": 5,
    "employeeCode": "EMP005",
    "employeeName": "Nguyen Van Minh",
    "workDate": "2025-11-20",
    "workShiftId": "WKS_NIGHT_01",
    "shiftName": "Ca tối",
    "shiftStartTime": "18:00:00",
    "shiftEndTime": "22:00:00",
    "status": "PENDING",
    "reason": "Cần tăng ca để hoàn thành dự án khẩn cấp",
    "approvalReason": null,
    "requestedById": 1,
    "requestedByName": "Admin User",
    "approvedById": null,
    "approvedByName": null,
    "approvedAt": null,
    "createdAt": "2025-10-20T...",
    "updatedAt": "2025-10-20T..."
  }
}
```

**Test Cases**:
- ✅ Valid request ID → Should succeed
- ❌ Non-existent ID → Should return 404
- ❌ Employee accessing another's request → Should return 404 (not 403 for security)

---

### Test 4: Approve Overtime Request ✅

**Endpoint**: `PATCH /api/v1/overtime-requests/{requestId}`  
**Permission**: `APPROVE_OT`  
**Who can test**: Admin, Manager

```http
PATCH http://localhost:8080/api/v1/overtime-requests/OTR251120001
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "action": "APPROVE",
  "reason": "Đã xem xét và phê duyệt yêu cầu tăng ca"
}
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Overtime request approved successfully",
  "data": {
    "requestId": "OTR251120001",
    "status": "APPROVED",
    "approvalReason": "Đã xem xét và phê duyệt yêu cầu tăng ca",
    "approvedById": 2,
    "approvedByName": "Manager Name",
    "approvedAt": "2025-10-20T..."
  }
}
```

**Test Cases**:
- ✅ PENDING → APPROVED → Should succeed
- ❌ APPROVED → APPROVED → Should fail (400 - already approved)
- ❌ REJECTED → APPROVED → Should fail (400 - invalid transition)
- ❌ Employee trying to approve → Should return 404

---

### Test 5: Reject Overtime Request ✅

**Endpoint**: `PATCH /api/v1/overtime-requests/{requestId}`  
**Permission**: `REJECT_OT`  
**Who can test**: Admin, Manager

```http
PATCH http://localhost:8080/api/v1/overtime-requests/OTR251120001
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "action": "REJECT",
  "reason": "Không đủ ngân sách cho tăng ca trong tháng này"
}
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Overtime request rejected successfully",
  "data": {
    "requestId": "OTR251120001",
    "status": "REJECTED",
    "approvalReason": "Không đủ ngân sách cho tăng ca trong tháng này",
    "approvedById": 2,
    "approvedByName": "Manager Name",
    "approvedAt": "2025-10-20T..."
  }
}
```

---

### Test 6: Cancel Own Overtime Request ✅

**Endpoint**: `PATCH /api/v1/overtime-requests/{requestId}`  
**Permission**: `CANCEL_OT_OWN`  
**Who can test**: Any employee (for their own requests)

```http
PATCH http://localhost:8080/api/v1/overtime-requests/OTR251120001
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "action": "CANCEL",
  "reason": "Không cần tăng ca nữa"
}
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Overtime request cancelled successfully",
  "data": {
    "requestId": "OTR251120001",
    "status": "CANCELLED",
    "approvalReason": "Không cần tăng ca nữa"
  }
}
```

**Test Cases**:
- ✅ Owner cancelling own PENDING request → Should succeed
- ❌ Owner cancelling APPROVED request → Should fail (400)
- ❌ Employee cancelling another's request → Should return 404

---

### Test 7: Manager Cancel Any Pending Request ✅

**Endpoint**: `PATCH /api/v1/overtime-requests/{requestId}`  
**Permission**: `CANCEL_OT_PENDING`  
**Who can test**: Admin, Manager

```http
PATCH http://localhost:8080/api/v1/overtime-requests/OTR251120001
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "action": "CANCEL",
  "reason": "Hủy theo yêu cầu của ban quản lý"
}
```

---

## 🎯 Testing Checklist

### Functional Tests
- [ ] Create overtime request with valid data
- [ ] Create overtime request with past date (should fail)
- [ ] Create duplicate overtime request (should fail)
- [ ] List all overtime requests (pagination works)
- [ ] Get overtime request details
- [ ] Approve pending request
- [ ] Reject pending request
- [ ] Cancel own pending request
- [ ] Manager cancel any pending request

### Permission Tests
- [ ] Admin can view all requests (VIEW_OT_ALL)
- [ ] Manager can view all requests (VIEW_OT_ALL)
- [ ] Employee can only view own requests (VIEW_OT_OWN)
- [ ] Employee can create overtime request (CREATE_OT)
- [ ] Manager can approve requests (APPROVE_OT)
- [ ] Manager can reject requests (REJECT_OT)
- [ ] Employee can cancel own request (CANCEL_OT_OWN)
- [ ] Manager can cancel any pending request (CANCEL_OT_PENDING)
- [ ] Employee cannot approve/reject requests (should return 404)

### Edge Case Tests
- [ ] Update already approved request (should fail)
- [ ] Update already rejected request (should fail)
- [ ] Update already cancelled request (should fail)
- [ ] Access non-existent request (should return 404)
- [ ] Employee access another's request (should return 404, not 403)
- [ ] Create request for non-existent employee (should fail)
- [ ] Create request for non-existent work shift (should fail)

### Data Validation Tests
- [ ] Missing required fields (should return 400)
- [ ] Invalid date format (should return 400)
- [ ] Empty reason for reject/cancel (should return 400)
- [ ] Invalid action type (should return 400)

---

## 📊 Expected Database State After Tests

After running all tests, check your database:

```sql
-- Check overtime requests table
SELECT * FROM overtime_requests;

-- Check permissions exist
SELECT * FROM permissions WHERE module = 'OVERTIME';

-- Check role permissions
SELECT rp.role_id, rp.permission_id, p.description
FROM role_permissions rp
JOIN permissions p ON rp.permission_id = p.permission_id
WHERE p.module = 'OVERTIME'
ORDER BY rp.role_id;

-- Check ROLE_MANAGER exists
SELECT * FROM roles WHERE role_id = 'ROLE_MANAGER';
```

---

## 🐛 Common Issues & Solutions

### Issue 1: 401 Unauthorized
**Cause**: Missing or invalid JWT token  
**Solution**: Login first and use the Bearer token

### Issue 2: 404 Not Found
**Cause**: Wrong endpoint or request ID doesn't exist  
**Solution**: Check URL and request ID

### Issue 3: 400 Bad Request
**Cause**: Invalid data or business rule violation  
**Solution**: Check the error message in response

### Issue 4: 409 Conflict
**Cause**: Duplicate overtime request  
**Solution**: This is expected behavior - working correctly

### Issue 5: Permission denied returns 404
**Cause**: Security best practice (don't reveal resource existence)  
**Solution**: This is correct behavior

---

## 📝 Test Report Template

After testing, document your results:

```markdown
## Test Results - Overtime Request API

**Date**: October 20, 2025  
**Tester**: [Your Name]  
**Environment**: Development/Local

### Summary
- Total Tests: __
- Passed: __
- Failed: __
- Blocked: __

### Test Results
| Test Case | Status | Notes |
|-----------|--------|-------|
| Create overtime request | ✅ | Works as expected |
| List all requests (Admin) | ✅ | Shows all requests |
| List own requests (Employee) | ✅ | Only shows own requests |
| ... | | |

### Issues Found
1. [Describe issue if any]

### Recommendations
1. [Your suggestions]
```

---

## 🎉 Next Steps After Testing

1. ✅ Fix any issues found during testing
2. ✅ Write unit tests for service layer
3. ✅ Write integration tests
4. ✅ Update API documentation
5. ✅ Prepare for code review
6. ✅ Deploy to staging environment

---

**Good luck with testing! 🚀**

If you find any issues, check the application logs for detailed error messages.
