# BE - Employee & Patient Stats API Implementation

**Date:** 2026-01-06  
**Issue:** FE had to fetch all pages to calculate stats (total, active, inactive counts)  
**Status:** ✅ **COMPLETED**

---

## 📋 Problem

FE team reported an issue where the UI was showing incorrect stats:
- **Example:** 15 total employees, 10 active, 0 inactive (incorrect)
- **Root cause:** FE was calculating stats from `patients.length` or `employees.length` which only reflected the current page, not all data
- **FE workaround:** Fetching all pages to calculate stats (inefficient)

---

## ✅ Solution

Added dedicated `/stats` endpoints for both Employees and Patients modules to provide accurate counts without needing to fetch all pages.

---

## 🆕 New API Endpoints

### 1. Employee Stats

**Endpoint:** `GET /api/v1/employees/stats`

**Description:** Get total, active, and inactive employee counts

**Authorization:** Admin or users with `VIEW_EMPLOYEE` permission

**Response:**
```json
{
  "totalEmployees": 15,
  "activeEmployees": 10,
  "inactiveEmployees": 5
}
```

**Example:**
```bash
GET /api/v1/employees/stats
Authorization: Bearer <token>
```

---

### 2. Patient Stats

**Endpoint:** `GET /api/v1/patients/stats`

**Description:** Get total, active, and inactive patient counts

**Authorization:** Admin or users with `VIEW_PATIENT` permission

**Response:**
```json
{
  "totalPatients": 120,
  "activePatients": 100,
  "inactivePatients": 20
}
```

**Example:**
```bash
GET /api/v1/patients/stats
Authorization: Bearer <token>
```

---

## 📝 Technical Details

### Backend Changes

#### 1. New DTOs Created
- `EmployeeStatsResponse.java` - Contains totalEmployees, activeEmployees, inactiveEmployees
- `PatientStatsResponse.java` - Contains totalPatients, activePatients, inactivePatients

#### 2. Repository Methods Added
**EmployeeRepository:**
- `countByIsActiveAndNotPatient(boolean isActive)` - Count employees by status (excludes ROLE_PATIENT)
- `countAllExcludingPatient()` - Count all employees (excludes ROLE_PATIENT)

**PatientRepository:**
- `countByIsActive(boolean isActive)` - Count patients by status

#### 3. Service Methods Added
**EmployeeService:**
- `getEmployeeStats()` - Calculates and returns employee stats

**PatientService:**
- `getPatientStats()` - Calculates and returns patient stats

#### 4. Controller Endpoints Added
**EmployeeController:**
- `GET /api/v1/employees/stats` - Returns employee stats

**PatientController:**
- `GET /api/v1/patients/stats` - Returns patient stats

---

## 🔄 Frontend Integration Guide

### Before (Inefficient - Multiple API Calls)
```typescript
// OLD METHOD - DON'T USE
const getAllEmployees = async () => {
  let allEmployees = [];
  let page = 0;
  let hasMore = true;
  
  while (hasMore) {
    const response = await fetch(`/api/v1/employees?page=${page}&size=100`);
    const data = await response.json();
    allEmployees = [...allEmployees, ...data.content];
    hasMore = !data.last;
    page++;
  }
  
  // Calculate stats from all fetched data
  const total = allEmployees.length;
  const active = allEmployees.filter(e => e.isActive).length;
  const inactive = total - active;
};
```

### After (Efficient - Single API Call)
```typescript
// NEW METHOD - USE THIS
const getEmployeeStats = async () => {
  const response = await fetch('/api/v1/employees/stats', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const stats = await response.json();
  
  // stats = { totalEmployees: 15, activeEmployees: 10, inactiveEmployees: 5 }
  setTotalEmployees(stats.totalEmployees);
  setActiveEmployees(stats.activeEmployees);
  setInactiveEmployees(stats.inactiveEmployees);
};
```

### React Example
```typescript
import { useEffect, useState } from 'react';

interface EmployeeStats {
  totalEmployees: number;
  activeEmployees: number;
  inactiveEmployees: number;
}

const EmployeeDashboard = () => {
  const [stats, setStats] = useState<EmployeeStats | null>(null);

  useEffect(() => {
    fetch('/api/v1/employees/stats', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(res => res.json())
      .then(data => setStats(data));
  }, []);

  return (
    <div>
      <StatsCard title="Total Employees" value={stats?.totalEmployees} />
      <StatsCard title="Active" value={stats?.activeEmployees} />
      <StatsCard title="Inactive" value={stats?.inactiveEmployees} />
    </div>
  );
};
```

---

## 🎯 Benefits

1. **Performance:** Single API call instead of fetching all pages
2. **Accuracy:** Counts calculated from database, not UI pagination
3. **Efficiency:** Reduced network traffic and processing time
4. **Simplicity:** Easier FE code without complex pagination loops

---

## ✅ Testing

### Test Employee Stats
```bash
curl -X GET "http://localhost:8080/api/v1/employees/stats" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "totalEmployees": 15,
  "activeEmployees": 10,
  "inactiveEmployees": 5
}
```

### Test Patient Stats
```bash
curl -X GET "http://localhost:8080/api/v1/patients/stats" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "totalPatients": 120,
  "activePatients": 100,
  "inactivePatients": 20
}
```

---

## 📌 Migration Steps for FE

1. **Update Employee Page** (`/admin/accounts/employees/page.tsx`):
   - Remove the "fetch all pages" logic
   - Replace with single call to `/api/v1/employees/stats`
   - Update state variables with response

2. **Update Patient Page** (`/admin/accounts/users/page.tsx`):
   - Remove the "fetch all pages" logic
   - Replace with single call to `/api/v1/patients/stats`
   - Update state variables with response

3. **Test thoroughly:**
   - Verify stats match actual database counts
   - Check active/inactive filters still work correctly
   - Confirm no performance degradation

---

## 🔗 Related Files

**Backend:**
- `EmployeeController.java` - Added GET /stats endpoint
- `EmployeeService.java` - Added getEmployeeStats() method
- `EmployeeRepository.java` - Added count methods
- `EmployeeStatsResponse.java` - New DTO
- `PatientController.java` - Added GET /stats endpoint
- `PatientService.java` - Added getPatientStats() method
- `PatientRepository.java` - Added count methods
- `PatientStatsResponse.java` - New DTO

**Frontend (to be updated):**
- `/admin/accounts/employees/page.tsx`
- `/admin/accounts/users/page.tsx`

---

## 🚀 Deployment Notes

- No database migration required
- No breaking changes to existing endpoints
- Backward compatible (existing FE code will still work)
- Can be deployed immediately

---

**Status:** ✅ **READY FOR FE INTEGRATION**

If you have any questions, please contact the BE team.
