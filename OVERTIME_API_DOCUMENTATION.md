# 📘 Overtime Request Management API Documentation

> **For Frontend Developers**  
> Version: 1.0  
> Last Updated: October 20, 2025

---

## 📋 Overview

This API allows management of overtime requests for employees in the dental clinic system. It includes creating, viewing, approving, rejecting, and canceling overtime requests with role-based access control.

**Base URL**: `/api/v1/overtime-requests`

---

## 🔐 Authentication

All endpoints require JWT authentication via Bearer token.

```http
Authorization: Bearer <your_jwt_token>
```

Get tokens by logging in at: `POST /api/v1/auth/login`

---

## 👥 Permissions & Roles

### Permission Types:
- `VIEW_OT_ALL` - View all overtime requests (Admin, Manager)
- `VIEW_OT_OWN` - View only own overtime requests (All employees)
- `CREATE_OT` - Create overtime requests (All employees)
- `APPROVE_OT` - Approve overtime requests (Admin, Manager)
- `REJECT_OT` - Reject overtime requests (Admin, Manager)
- `CANCEL_OT_OWN` - Cancel own pending requests (All employees)
- `CANCEL_OT_PENDING` - Cancel any pending request (Admin, Manager)

### Role Capabilities:
| Role | Capabilities |
|------|--------------|
| **ROLE_ADMIN** | Full access to all operations |
| **ROLE_MANAGER** | View all, approve, reject, cancel any pending |
| **ROLE_DOCTOR/NURSE/etc** | View own, create, cancel own pending |

---

## 📡 API Endpoints

### 1. List Overtime Requests

Get a paginated list of overtime requests.

**Endpoint**: `GET /api/v1/overtime-requests`

**Permission**: `VIEW_OT_ALL` or `VIEW_OT_OWN`

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | integer | No | 0 | Page number (0-based) |
| `size` | integer | No | 10 | Items per page |
| `status` | string | No | - | Filter by status: PENDING, APPROVED, REJECTED, CANCELLED |
| `employeeId` | integer | No | - | Filter by employee ID |
| `workDate` | string | No | - | Filter by date (YYYY-MM-DD) |

**Response Format**:
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
        "createdAt": "2025-10-20T15:30:00"
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

**Behavior Notes**:
- Users with `VIEW_OT_ALL` see all requests
- Users with only `VIEW_OT_OWN` see only their own requests
- Filtering is applied server-side

**Example Usage**:
```javascript
// Get all pending requests (page 1, 20 items)
GET /api/v1/overtime-requests?status=PENDING&page=0&size=20

// Get specific employee's requests
GET /api/v1/overtime-requests?employeeId=5

// Get requests for a specific date
GET /api/v1/overtime-requests?workDate=2025-11-20
```

---

### 2. Get Overtime Request Details

Get detailed information about a specific overtime request.

**Endpoint**: `GET /api/v1/overtime-requests/{requestId}`

**Permission**: `VIEW_OT_ALL` or `VIEW_OT_OWN`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `requestId` | string | Yes | Overtime request ID (e.g., OTR251120001) |

**Response Format**:
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
    "createdAt": "2025-10-20T15:30:00",
    "updatedAt": "2025-10-20T15:30:00"
  }
}
```

**Status Codes**:
- `200` - Success
- `404` - Request not found or user doesn't have permission

**Behavior Notes**:
- Returns 404 (not 403) for security reasons when user lacks permission
- Includes full shift details (start/end time)
- Shows approval information if approved/rejected

---

### 3. Create Overtime Request

Create a new overtime request.

**Endpoint**: `POST /api/v1/overtime-requests`

**Permission**: `CREATE_OT`

**Request Body**:
```json
{
  "employeeId": 5,
  "workDate": "2025-11-20",
  "workShiftId": "WKS_NIGHT_01",
  "reason": "Cần tăng ca để hoàn thành dự án khẩn cấp"
}
```

**Field Validation**:
| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `employeeId` | integer | Yes | Must be a valid employee ID |
| `workDate` | string | Yes | YYYY-MM-DD format, cannot be in the past |
| `workShiftId` | string | Yes | Must be a valid work shift ID |
| `reason` | string | Yes | Cannot be blank |

**Response Format** (201 Created):
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
    "createdAt": "2025-10-20T15:30:00"
  }
}
```

**Business Rules**:
1. ✅ Work date must be today or in the future
2. ✅ Cannot create duplicate requests (same employee + date + shift with PENDING/APPROVED status)
3. ✅ Employee and work shift must exist in the system
4. ✅ Request is automatically created with PENDING status
5. ✅ Current user is automatically set as the requester

**Error Responses**:

**400 Bad Request** - Validation error:
```json
{
  "success": false,
  "message": "Work date cannot be in the past",
  "errorCode": "VALIDATION_ERROR"
}
```

**404 Not Found** - Employee or shift not found:
```json
{
  "success": false,
  "message": "Employee with ID 999 not found",
  "errorCode": "EMPLOYEE_NOT_FOUND"
}
```

**409 Conflict** - Duplicate request:
```json
{
  "success": false,
  "message": "An overtime request already exists for this employee, date, and shift",
  "errorCode": "DUPLICATE_OVERTIME_REQUEST"
}
```

---

### 4. Update Overtime Request Status

Approve, reject, or cancel an overtime request.

**Endpoint**: `PATCH /api/v1/overtime-requests/{requestId}`

**Permissions**: 
- `APPROVE_OT` for approve action
- `REJECT_OT` for reject action
- `CANCEL_OT_OWN` or `CANCEL_OT_PENDING` for cancel action

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `requestId` | string | Yes | Overtime request ID (e.g., OTR251120001) |

**Request Body**:
```json
{
  "action": "APPROVE",
  "reason": "Đã xem xét và phê duyệt yêu cầu tăng ca"
}
```

**Field Validation**:
| Field | Type | Required | Values |
|-------|------|----------|--------|
| `action` | string | Yes | APPROVE, REJECT, CANCEL |
| `reason` | string | Yes for REJECT/CANCEL | Explanation for the action |

**Response Format** (200 OK):
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
    "approvedAt": "2025-10-20T16:00:00"
  }
}
```

**Action-Specific Behavior**:

#### **APPROVE**
- Permission: `APPROVE_OT`
- Can only approve PENDING requests
- Sets status to APPROVED
- Records approver and approval time

#### **REJECT**
- Permission: `REJECT_OT`
- Can only reject PENDING requests
- Sets status to REJECTED
- Requires reason

#### **CANCEL**
- Permission: `CANCEL_OT_OWN` (for own requests) or `CANCEL_OT_PENDING` (for any)
- Can only cancel PENDING requests
- Sets status to CANCELLED
- Requires reason
- Employees can only cancel their own requests

**Status Transition Rules**:

```
PENDING → APPROVED ✅
PENDING → REJECTED ✅
PENDING → CANCELLED ✅

APPROVED → * ❌ (Cannot update)
REJECTED → * ❌ (Cannot update)
CANCELLED → * ❌ (Cannot update)
```

**Error Responses**:

**400 Bad Request** - Invalid state transition:
```json
{
  "success": false,
  "message": "Cannot update overtime request with status APPROVED. Only PENDING requests can be updated.",
  "errorCode": "INVALID_STATE_TRANSITION"
}
```

**404 Not Found** - Request not found or no permission:
```json
{
  "success": false,
  "message": "Overtime request with ID OTR251120001 not found",
  "errorCode": "OVERTIME_REQUEST_NOT_FOUND"
}
```

---

## 📊 Data Models

### OvertimeRequestListItem
Used in list responses.

```typescript
interface OvertimeRequestListItem {
  requestId: string;           // Format: OTRyymmddSSS
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  workDate: string;            // YYYY-MM-DD
  workShiftId: string;
  shiftName: string;
  status: "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";
  requestedByName: string;
  createdAt: string;           // ISO 8601 datetime
}
```

### OvertimeRequestDetail
Used in detail and create responses.

```typescript
interface OvertimeRequestDetail {
  requestId: string;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  workDate: string;
  workShiftId: string;
  shiftName: string;
  shiftStartTime?: string;     // HH:mm:ss (only in detail view)
  shiftEndTime?: string;       // HH:mm:ss (only in detail view)
  status: "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";
  reason: string;
  approvalReason?: string | null;
  requestedById: number;
  requestedByName: string;
  approvedById?: number | null;
  approvedByName?: string | null;
  approvedAt?: string | null;  // ISO 8601 datetime
  createdAt: string;
  updatedAt: string;
}
```

### CreateOvertimeRequest
Request body for creating overtime request.

```typescript
interface CreateOvertimeRequest {
  employeeId: number;
  workDate: string;            // YYYY-MM-DD
  workShiftId: string;
  reason: string;
}
```

### UpdateOvertimeStatus
Request body for updating overtime status.

```typescript
interface UpdateOvertimeStatus {
  action: "APPROVE" | "REJECT" | "CANCEL";
  reason?: string;             // Required for REJECT and CANCEL
}
```

### PaginatedResponse
Wrapper for paginated list responses.

```typescript
interface PaginatedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
}
```

---

## 🎯 Frontend Integration Examples

### React/Vue Example - Fetch Overtime Requests

```javascript
async function fetchOvertimeRequests(page = 0, size = 10, filters = {}) {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    ...filters
  });

  const response = await fetch(
    `/api/v1/overtime-requests?${params}`,
    {
      headers: {
        'Authorization': `Bearer ${getToken()}`,
        'Content-Type': 'application/json'
      }
    }
  );

  if (!response.ok) {
    throw new Error('Failed to fetch overtime requests');
  }

  const result = await response.json();
  return result.data; // Contains content, pageable, totalElements, etc.
}

// Usage
const data = await fetchOvertimeRequests(0, 20, { status: 'PENDING' });
console.log(data.content); // Array of requests
console.log(data.totalElements); // Total count
```

### React/Vue Example - Create Overtime Request

```javascript
async function createOvertimeRequest(requestData) {
  const response = await fetch('/api/v1/overtime-requests', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(requestData)
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }

  const result = await response.json();
  return result.data;
}

// Usage
try {
  const newRequest = await createOvertimeRequest({
    employeeId: 5,
    workDate: '2025-11-20',
    workShiftId: 'WKS_NIGHT_01',
    reason: 'Urgent project completion'
  });
  console.log('Created:', newRequest.requestId);
} catch (error) {
  console.error('Error:', error.message);
}
```

### React/Vue Example - Approve/Reject/Cancel

```javascript
async function updateOvertimeStatus(requestId, action, reason) {
  const response = await fetch(`/api/v1/overtime-requests/${requestId}`, {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ action, reason })
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }

  const result = await response.json();
  return result.data;
}

// Usage
await updateOvertimeStatus('OTR251120001', 'APPROVE', 'Approved by manager');
await updateOvertimeStatus('OTR251120002', 'REJECT', 'Insufficient budget');
await updateOvertimeStatus('OTR251120003', 'CANCEL', 'No longer needed');
```

---

## 🔍 Common Use Cases

### Use Case 1: Display Overtime Request List
1. Check user's role to determine which filter to show
2. Call GET `/api/v1/overtime-requests` with pagination
3. Display results in a table with status badges
4. Show action buttons based on user's permissions

### Use Case 2: Create New Overtime Request
1. Show form with date picker (disable past dates), employee selector, shift selector
2. Validate form inputs
3. Call POST `/api/v1/overtime-requests`
4. Handle success: Show success message, redirect to list
5. Handle errors: Display validation errors to user

### Use Case 3: Manager Approval Workflow
1. Fetch pending requests: `?status=PENDING`
2. Display with approve/reject buttons
3. On approve: Call PATCH with `action=APPROVE`
4. On reject: Show reason dialog, then call PATCH with `action=REJECT`
5. Update UI to reflect new status

### Use Case 4: Employee View Own Requests
1. API automatically filters to show only user's requests (VIEW_OT_OWN)
2. Display with status badges
3. Show cancel button only for PENDING requests
4. On cancel: Call PATCH with `action=CANCEL`

---

## ⚠️ Important Notes for Frontend

### 1. **Permission-Based UI**
Always check user's permissions before showing action buttons:
- Show "Approve/Reject" only if user has `APPROVE_OT` or `REJECT_OT`
- Show "Create" button only if user has `CREATE_OT`
- Show "Cancel" only for PENDING status AND (user owns request OR has `CANCEL_OT_PENDING`)

### 2. **Date Validation**
- Disable past dates in date picker
- Validate date format: YYYY-MM-DD
- Show user-friendly error if backend returns date validation error

### 3. **Status Display**
Use color-coded badges:
- 🟡 PENDING - Yellow/Warning
- 🟢 APPROVED - Green/Success
- 🔴 REJECTED - Red/Danger
- ⚫ CANCELLED - Gray/Secondary

### 4. **Error Handling**
The API returns consistent error format:
```json
{
  "success": false,
  "message": "Human-readable error message",
  "errorCode": "ERROR_CODE_CONSTANT"
}
```

Common error codes:
- `VALIDATION_ERROR` - Input validation failed
- `OVERTIME_REQUEST_NOT_FOUND` - 404 error
- `DUPLICATE_OVERTIME_REQUEST` - 409 conflict
- `INVALID_STATE_TRANSITION` - Cannot update non-pending request
- `EMPLOYEE_NOT_FOUND` - Invalid employee ID
- `WORK_SHIFT_NOT_FOUND` - Invalid work shift ID

### 5. **Pagination**
- Use `page` (0-based) and `size` parameters
- Display page numbers and total count
- Default: page=0, size=10

### 6. **Real-time Updates**
Consider implementing:
- Auto-refresh for pending requests list (managers)
- WebSocket notifications for status changes (optional)
- Optimistic UI updates with rollback on error

---

## 📞 Support

For questions or issues:
- Check application logs for detailed error messages
- Verify JWT token is valid and not expired
- Ensure user has required permissions
- Contact backend team for API issues

---

**API Version**: 1.0  
**Last Updated**: October 20, 2025  
**Maintained by**: Backend Team - Dental Clinic Management System
