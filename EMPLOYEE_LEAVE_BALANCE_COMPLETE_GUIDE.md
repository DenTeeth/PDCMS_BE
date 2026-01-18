# Employee Leave Balance API - Complete Implementation Guide

**Date:** 2026-01-17  
**Component:** Leave Balance Management - Employee Self-Service  
**Priority:** Medium  
**Status:** ✅ RESOLVED  
**Assigned To:** Backend Team  
**Resolved Date:** 2026-01-18

---

## 📋 Table of Contents

1. [Issue Summary](#issue-summary)
2. [Original Problem](#original-problem)
3. [Solution Implemented](#solution-implemented)
4. [API Specification](#api-specification)
5. [Technical Implementation](#technical-implementation)
6. [Frontend Integration Guide](#frontend-integration-guide)
7. [Testing Guide](#testing-guide)
8. [Security & Best Practices](#security--best-practices)
9. [Resolution Details](#resolution-details)

---

## Issue Summary

The frontend has implemented permission-based access control for employees to view their own leave balances using the `VIEW_LEAVE_OWN` permission, but the backend API endpoint does not exist. Currently, the only available endpoint requires `VIEW_LEAVE_ALL` permission (admin-only), making it impossible for employees to view their own leave balances.

**Resolution:** ✅ New endpoint implemented at `GET /api/v1/employee/leave-balances`

---

## Original Problem

---

## Original Problem

### Current Situation

### Frontend Implementation

**File:** `src/app/employee/leave-balances/page.tsx`

The frontend has been implemented to support both:
- `VIEW_LEAVE_ALL` - Admin/Manager can view all employees' balances
- `VIEW_LEAVE_OWN` - Employee can view their own balance

**Permission Check:**
```typescript
const canViewAllBalances = user?.permissions?.includes('VIEW_LEAVE_ALL') || false;
const canViewOwnBalances = user?.permissions?.includes('VIEW_LEAVE_OWN') || false;
const canViewBalances = canViewAllBalances || canViewOwnBalances;
```

**Current Logic:**
```typescript
const loadEmployees = async () => {
  try {
    if (canViewAllBalances) {
      // Admin: Load all employees
      const data = await employeeService.getEmployees();
      const empList = data.content || [];
      setEmployees(empList);
      setFilteredEmployees(empList);
      if (empList.length > 0 && !selectedEmployeeId) {
        setSelectedEmployeeId(empList[0].employeeId);
      }
    } else if (canViewOwnBalances && user?.employeeId) {
      // Employee: Load only own employee data
      const ownEmployeeId = Number(user.employeeId);
      if (!isNaN(ownEmployeeId) && ownEmployeeId > 0) {
        const ownEmployee = await employeeService.getEmployeeById(ownEmployeeId);
        setEmployees([ownEmployee]);
        setFilteredEmployees([ownEmployee]);
        setSelectedEmployeeId(ownEmployeeId);
      } else {
        toast.error('Không tìm thấy thông tin nhân viên của bạn.');
      }
    }
  } catch (error: any) {
    handleApiError(error, 'Không thể tải danh sách nhân viên');
  }
};
```

**Problem:** When `loadBalances()` is called, it uses `LeaveBalanceService.getEmployeeBalances()` which calls the admin endpoint requiring `VIEW_LEAVE_ALL` permission.

---

## Current API Service

**File:** `src/services/leaveBalanceService.ts`

```typescript
export class LeaveBalanceService {
  private static readonly BASE_URL = '/admin';

  /**
   * Lấy số dư phép của một nhân viên theo năm
   * 
   * GET /api/v1/admin/employees/{employee_id}/leave-balances?cycle_year=2025
   * 
   * Requires: VIEW_LEAVE_ALL permission (AdminLeaveBalanceController line 135)
   */
  static async getEmployeeBalances(
    employeeId: number,
    cycleYear?: number
  ): Promise<EmployeeLeaveBalancesResponse> {
    const axios = apiClient.getAxiosInstance();
    try {
      const response = await axios.get<EmployeeLeaveBalancesResponse>(
        `${this.BASE_URL}/employees/${employeeId}/leave-balances`,
        {
          params: {
            cycle_year: cycleYear || new Date().getFullYear()
          }
        }
      );
      const { extractApiResponse } = await import('@/utils/apiResponse');
      return extractApiResponse<any>(response);
    } catch (error: any) {
      // Error handling...
      throw error;
    }
  }
}
```

**Current Endpoint:**
- **URL:** `GET /api/v1/admin/employees/{employee_id}/leave-balances?cycle_year={year}`
- **Permission Required:** `VIEW_LEAVE_ALL` (Admin only)
- **Base Path:** `/admin`

---

## Required Solution

### Option 1: New Employee Endpoint (Recommended)

Create a new endpoint specifically for employees to view their own leave balances:

**Proposed Endpoint:**
```
GET /api/v1/employee/leave-balances?cycle_year={year}
```

**Requirements:**
- **Permission:** `VIEW_LEAVE_OWN`
- **Authorization:** Employee can only view their own balance (auto-extracted from JWT token)
- **Response Format:** Same as admin endpoint
- **Error Codes:**
  - `403 FORBIDDEN` - Missing `VIEW_LEAVE_OWN` permission
  - `404 NOT_FOUND` - No balance records found for the employee

**Controller Location:** Should be in `EmployeeLeaveBalanceController` (not `AdminLeaveBalanceController`)

**Example Request:**
```http
GET /api/v1/employee/leave-balances?cycle_year=2026
Authorization: Bearer {jwt_token}
```

**Expected Response:**
```json
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "employee_id": 123,
    "employee_name": "Nguyễn Văn A",
    "cycle_year": 2026,
    "balances": [
      {
        "balance_id": 1,
        "time_off_type": {
          "type_id": "ANNUAL_LEAVE",
          "type_name": "Nghỉ phép năm",
          "is_paid": true
        },
        "total_days_allowed": 12.0,
        "days_taken": 3.5,
        "days_remaining": 8.5
      }
    ]
  }
}
```

### Option 2: Modify Existing Admin Endpoint (Alternative)

Modify the existing admin endpoint to support both permissions:
- If user has `VIEW_LEAVE_ALL`: Can view any employee's balance (current behavior)
- If user has `VIEW_LEAVE_OWN`: Can only view their own balance (auto-filter by JWT)

**Note:** This approach is less secure and less RESTful, but requires less code changes.

---

## Frontend Changes Required (After BE Implementation)

Once the backend endpoint is available, update `src/services/leaveBalanceService.ts`:

```typescript
export class LeaveBalanceService {
  private static readonly BASE_URL = '/admin';
  private static readonly EMPLOYEE_BASE_URL = '/employee';

  /**
   * Lấy số dư phép của một nhân viên theo năm (Admin endpoint)
   * Requires: VIEW_LEAVE_ALL permission
   */
  static async getEmployeeBalances(
    employeeId: number,
    cycleYear?: number
  ): Promise<EmployeeLeaveBalancesResponse> {
    // ... existing implementation ...
  }

  /**
   * Lấy số dư phép của nhân viên hiện tại (Employee endpoint)
   * Requires: VIEW_LEAVE_OWN permission
   * Employee ID is auto-extracted from JWT token
   */
  static async getOwnBalances(
    cycleYear?: number
  ): Promise<EmployeeLeaveBalancesResponse> {
    const axios = apiClient.getAxiosInstance();
    try {
      const response = await axios.get<EmployeeLeaveBalancesResponse>(
        `${this.EMPLOYEE_BASE_URL}/leave-balances`,
        {
          params: {
            cycle_year: cycleYear || new Date().getFullYear()
          }
        }
      );
      const { extractApiResponse } = await import('@/utils/apiResponse');
      return extractApiResponse<any>(response);
    } catch (error: any) {
      if (process.env.NODE_ENV === 'development') {
        console.error(' LeaveBalanceService.getOwnBalances error:', {
          cycleYear: cycleYear || new Date().getFullYear(),
          status: error.response?.status,
          statusText: error.response?.statusText,
          data: error.response?.data,
          url: error.config?.url,
          message: error.message
        });
      }
      throw error;
    }
  }
}
```

Then update `src/app/employee/leave-balances/page.tsx`:

```typescript
const loadBalances = async () => {
  if (!selectedEmployeeId) return;

  try {
    setLoading(true);
    setError(null);

    let balances: EmployeeLeaveBalancesResponse;
    
    if (canViewAllBalances) {
      // Admin: Use admin endpoint with employeeId
      balances = await LeaveBalanceService.getEmployeeBalances(
        selectedEmployeeId,
        selectedYear
      );
    } else if (canViewOwnBalances) {
      // Employee: Use employee endpoint (auto-extracts employeeId from JWT)
      balances = await LeaveBalanceService.getOwnBalances(selectedYear);
    } else {
      throw new Error('Không có quyền xem số dư nghỉ phép');
    }

    setLeaveBalances(balances);
    // ... rest of the logic ...
  } catch (error: any) {
    // ... error handling ...
  } finally {
    setLoading(false);
  }
};
```

---

## Business Context

### Why This Feature is Important

1. **Employee Self-Service:** Employees should be able to check their own leave balances without needing to contact HR
2. **Transparency:** Employees can see how many days they have remaining before requesting time off
3. **User Experience:** The frontend UI is already built and ready, but cannot function without the backend API
4. **Permission Alignment:** The `VIEW_LEAVE_OWN` permission exists in the system but has no corresponding API endpoint

### Current Workaround

Currently, employees with `VIEW_LEAVE_OWN` permission cannot view their leave balances. The page will show an error when trying to load balances because:
- The frontend tries to call the admin endpoint
- The admin endpoint requires `VIEW_LEAVE_ALL` permission
- Employees only have `VIEW_LEAVE_OWN` permission
- Result: `403 FORBIDDEN` error

---

## Related Files

### Frontend Files:
- `src/app/employee/leave-balances/page.tsx` - Employee leave balance page
- `src/app/admin/leave-balances/page.tsx` - Admin leave balance page (works correctly)
- `src/services/leaveBalanceService.ts` - API service layer
- `src/types/leaveBalance.ts` - TypeScript types

### Backend Files (Expected):
- `EmployeeLeaveBalanceController.java` - Should contain the new employee endpoint
- `LeaveBalanceService.java` - Business logic for retrieving balances
- `EmployeeLeaveBalanceRepository.java` - Data access layer

---

## Testing Checklist

Once the backend endpoint is implemented, please verify:

- [ ] Endpoint returns `200 OK` for employees with `VIEW_LEAVE_OWN` permission
- [ ] Endpoint returns `403 FORBIDDEN` for users without `VIEW_LEAVE_OWN` permission
- [ ] Endpoint returns `404 NOT_FOUND` when employee has no balance records
- [ ] Employee can only view their own balance (cannot access other employees' balances)
- [ ] Response format matches the admin endpoint format
- [ ] `cycle_year` parameter works correctly (defaults to current year if not provided)
- [ ] JWT token is properly validated and employee ID is extracted correctly

---

## Additional Notes

1. **Security Consideration:** The employee endpoint should automatically extract the employee ID from the JWT token. Do NOT allow employees to specify `employee_id` in the request, as this would allow them to view other employees' balances.

2. **Response Format:** The response format should match the admin endpoint to maintain consistency and allow the frontend to reuse the same UI components.

3. **Error Messages:** Error messages should be in Vietnamese to match the rest of the application.

4. **Permission Check:** The endpoint should check for `VIEW_LEAVE_OWN` permission. If the user has `VIEW_LEAVE_ALL`, they should use the admin endpoint instead.

---

## API Specification (Implemented)

**Frontend Team Contact:**
- Issue created by: Frontend Development Team
- Date: 2026-01-17
- Related Issue: Employee self-service leave balance viewing

**Backend Team:**
- Please implement the endpoint and update this document with the final API specification
- Once implemented, change status to "✅ RESOLVED" and add resolution details

---

## Status Updates

### 2026-01-18 - ✅ RESOLVED
- **Implementation completed** by Backend Team
- **Created new controller:** `EmployeeLeaveBalanceController.java`
- **New endpoint:** `GET /api/v1/employee/leave-balances?cycle_year={year}`
- **Permission required:** `VIEW_LEAVE_OWN`
- **Security:** Employee ID automatically extracted from JWT token
- **Response format:** Matches admin endpoint format (reuses `EmployeeLeaveBalanceResponse`)
- **Service layer:** Reuses existing `AdminLeaveBalanceService.getEmployeeLeaveBalances()` method
- **Compilation:** ✅ Successful, no errors

**Technical Details:**
- Controller path: `src/main/java/com/dental/clinic/management/working_schedule/controller/EmployeeLeaveBalanceController.java`
- Uses `SecurityUtil.getCurrentUserLogin()` to extract username from JWT
- Uses `AccountRepository.findOneByUsername()` to get employee ID
- Prevents employees from viewing other employees' balances
- Returns 403 FORBIDDEN if user lacks `VIEW_LEAVE_OWN` permission
- Returns 404 NOT_FOUND if employee or balance records not found
- Defaults to current year if `cycle_year` parameter not provided

**Next Steps for Frontend:**
1. Implement `LeaveBalanceService.getOwnBalances()` method as specified in the original issue
2. Update `src/app/employee/leave-balances/page.tsx` to use the new endpoint for employees with `VIEW_LEAVE_OWN` permission
3. Test the integration with both admin and employee roles

### 2026-01-17
- Issue created
- Frontend implementation documented
- Backend requirements specified
- Waiting for backend team response

