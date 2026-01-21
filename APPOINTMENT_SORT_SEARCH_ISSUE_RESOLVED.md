# Issue: Appointment Sort & Search Not Working

**Date Reported:** 20/01/2026  
**Date Resolved:** 21/01/2026  
**Status:** ✅ **RESOLVED**  
**Priority:** High

---

## ✅ RESOLUTION SUMMARY

**Fixed on:** 21/01/2026

Both issues were **Backend bugs** in `AppointmentRepository.java`:

### Issue 1: Sort Not Working ✅ FIXED
**Root Cause:** The `findByFilters` query had hardcoded `ORDER BY a.appointment_start_time` at line 348, which completely ignored the dynamic sort parameters (`sortBy` and `sortDirection`) sent via Pageable.

**Fix Applied:**
- **File:** `src/main/java/com/dental/clinic/management/booking_appointment/repository/AppointmentRepository.java`
- **Line:** ~348
- **Change:** Removed hardcoded `ORDER BY a.appointment_start_time` from the native query
- **Result:** Query now respects the Pageable sort parameters, enabling dynamic sorting by any field (appointmentId, appointmentStartTime, etc.) in both ASC and DESC order

### Issue 2: Search Not Matching Appointment Code ✅ FIXED
**Root Cause:** The `findBySearchCode` query searched patient/employee/room/service codes and names, but **did NOT include `appointment_code`** in the search conditions.

**Fix Applied:**
- **File:** `src/main/java/com/dental/clinic/management/booking_appointment/repository/AppointmentRepository.java`
- **Line:** ~500
- **Change:** Added `a.appointment_code ILIKE '%' || :searchCode || '%'` as the first search condition
- **Result:** Users can now search appointments by appointment code (e.g., "APT-20260102-001", "APT-001")

### Testing Status
- ✅ Code changes completed
- ⏳ Awaiting deployment and FE team verification

---

## 🚨 Problem Description

**User Report:**
> "Hiện tại trên UI đang không thể sort hoặc search trên danh sách đều không hoạt động"

**Affected Pages:**
- `/admin/booking/appointments`
- `/employee/booking/appointments`

**Symptoms:**
1. ❌ Sort dropdown không hoạt động - chọn sort field không thay đổi kết quả
2. ❌ Search input không hoạt động - nhập text không filter kết quả
3. ❌ Sort direction buttons (ASC/DESC) không hoạt động

---

## 🔍 FE Code Analysis

### 1. Sort Functionality

**Component:** `AppointmentFilters.tsx`

**Sort Dropdown Handler:**
```typescript
onClick={() => {
  onFiltersChange({ 
    ...filters, 
    sortBy: option.value,
    sortDirection: filters.sortDirection || 'ASC',
  });
  setIsSortDropdownOpen(false);
}}
```

**Direction Buttons Handler:**
```typescript
onClick={() => {
  onFiltersChange({ 
    ...filters, 
    sortBy: filters.sortBy || 'appointmentStartTime',
    sortDirection: 'ASC' // or 'DESC'
  });
}}
```

**Page Handler:**
```typescript
const handleFiltersChange = useCallback((newFilters: Partial<AppointmentFilterCriteria>) => {
  setFilters((prev) => ({ ...prev, ...newFilters }));
  setCurrentPage(0);
}, []);
```

**API Call:**
```typescript
const criteria: AppointmentFilterCriteria = {
  ...filters,
  page: currentPage,
  size: pageSize,
  sortBy: filters.sortBy || 'appointmentStartTime',
  sortDirection: filters.sortDirection || 'ASC',
};

const response = await appointmentService.getAppointmentsPage(criteria);
```

**Service Layer:**
```typescript
if (criteria?.sortBy) {
    params.append('sortBy', criteria.sortBy);
}
if (criteria?.sortDirection) {
    params.append('sortDirection', criteria.sortDirection);
}
```

### 2. Search Functionality

**Component:** `AppointmentFilters.tsx`

**Search Handler (Debounced):**
```typescript
useEffect(() => {
  const searchValue = debouncedSearch.trim();
  
  if (searchValue) {
    onFiltersChangeRef.current({
      searchCode: searchValue,
      patientName: undefined,
      patientPhone: undefined,
      employeeCode: undefined,
      roomCode: undefined,
      serviceCode: undefined,
    });
  } else {
    if (currentSearchCode) {
      onFiltersChangeRef.current({ searchCode: undefined });
    }
  }
}, [debouncedSearch]);
```

**Service Layer:**
```typescript
if (criteria?.searchCode) {
    params.append('searchCode', criteria.searchCode);
}
```

---

## 🧪 Testing Checklist

### FE Testing

- [ ] Check browser console for errors
- [ ] Check Network tab - verify API calls are made with correct params
- [ ] Verify `handleFiltersChange` is called when sort/search changes
- [ ] Verify `filters` state updates correctly
- [ ] Verify `useEffect` dependencies trigger API call
- [ ] Check if `searchCode` param is sent to API
- [ ] Check if `sortBy` and `sortDirection` params are sent to API

### BE Testing (Required)

- [ ] Verify API endpoint accepts `searchCode` parameter
- [ ] Verify API endpoint accepts `sortBy` parameter
- [ ] Verify API endpoint accepts `sortDirection` parameter
- [ ] Test API directly with Postman/curl:
  ```
  GET /api/v1/appointments?page=0&size=10&sortBy=appointmentId&sortDirection=DESC&searchCode=APT-001
  ```
- [ ] Check BE logs for incoming requests
- [ ] Verify BE returns filtered/sorted results

---

## 📋 Expected API Request Format

### Sort Request
```
GET /api/v1/appointments?page=0&size=10&sortBy=appointmentId&sortDirection=DESC
```

### Search Request
```
GET /api/v1/appointments?page=0&size=10&searchCode=APT-001
```

### Combined Request
```
GET /api/v1/appointments?page=0&size=10&sortBy=appointmentStartTime&sortDirection=ASC&searchCode=Nguyen
```

---

## 🔍 Potential Issues

### FE Issues (Possible)

1. **State Merge Issue:**
   - `handleFiltersChange` uses `{ ...prev, ...newFilters }`
   - If `newFilters` contains `undefined` values, they might not clear existing filters
   - **Fix:** Explicitly handle undefined values

2. **Debounce Issue:**
   - Search uses 1000ms debounce
   - Complex ref-based logic might cause race conditions
   - **Fix:** Simplify debounce logic

3. **Dependencies Issue:**
   - `useEffect` depends on `filters` object
   - Object reference might not change if nested properties change
   - **Fix:** Use specific filter properties as dependencies

### BE Issues (Possible)

1. **Parameter Not Accepted:**
   - BE might not accept `searchCode` parameter
   - BE might not accept certain `sortBy` values
   - **Fix:** Check BE controller method signature

2. **Parameter Name Mismatch:**
   - FE sends `searchCode` but BE expects different name
   - FE sends `sortBy` but BE expects different name
   - **Fix:** Align parameter names

3. **Backend Logic Error:**
   - BE accepts params but doesn't apply them
   - BE applies filters incorrectly
   - **Fix:** Check BE service implementation

---

## 🛠️ Debugging Steps

### Step 1: Check FE Console
```javascript
// Add to AppointmentFilters component
console.log('Filters changed:', newFilters);
console.log('Current filters:', filters);

// Add to page component
console.log('API criteria:', criteria);
console.log('API URL:', url);
```

### Step 2: Check Network Tab
1. Open browser DevTools → Network tab
2. Filter by "appointments"
3. Click on request → Check "Payload" or "Query String Parameters"
4. Verify params are sent correctly

### Step 3: Test API Directly
```bash
# Test sort
curl "http://localhost:8080/api/v1/appointments?page=0&size=10&sortBy=appointmentId&sortDirection=DESC" \
  -H "Authorization: Bearer <token>"

# Test search
curl "http://localhost:8080/api/v1/appointments?page=0&size=10&searchCode=APT-001" \
  -H "Authorization: Bearer <token>"
```

### Step 4: Check BE Logs
- Check BE console for incoming requests
- Verify params are received correctly
- Check if filters are applied in service layer

---

## 📝 FE Code to Verify

### File: `src/app/admin/booking/appointments/page.tsx`

**Line 154-157:**
```typescript
const handleFiltersChange = useCallback((newFilters: Partial<AppointmentFilterCriteria>) => {
  setFilters((prev) => ({ ...prev, ...newFilters }));
  setCurrentPage(0);
}, []);
```

**Line 87-93:**
```typescript
const criteria: AppointmentFilterCriteria = {
  ...filters,
  page: currentPage,
  size: pageSize,
  sortBy: filters.sortBy || 'appointmentStartTime',
  sortDirection: filters.sortDirection || 'ASC',
};
```

### File: `src/components/appointments/AppointmentFilters.tsx`

**Line 209-220:**
```typescript
onClick={() => {
  onFiltersChange({ 
    ...filters, 
    sortBy: option.value,
    sortDirection: filters.sortDirection || 'ASC',
  });
  setIsSortDropdownOpen(false);
}}
```

**Line 95-102:**
```typescript
onFiltersChangeRef.current({
  searchCode: searchValue,
  patientName: undefined,
  patientPhone: undefined,
  employeeCode: undefined,
  roomCode: undefined,
  serviceCode: undefined,
});
```

---

## ✅ Expected Behavior

### Sort
1. User clicks sort dropdown
2. User selects "ID lịch hẹn"
3. `handleFiltersChange` called with `{ sortBy: 'appointmentId' }`
4. `filters` state updates
5. `useEffect` triggers API call
6. API receives `sortBy=appointmentId&sortDirection=ASC`
7. BE returns sorted results
8. UI updates with sorted list

### Search
1. User types "APT-001" in search box
2. After 1000ms debounce, `handleFiltersChange` called
3. `filters` state updates with `{ searchCode: 'APT-001' }`
4. `useEffect` triggers API call
5. API receives `searchCode=APT-001`
6. BE returns filtered results
7. UI updates with filtered list

---

## 🎯 Next Steps

1. **FE Team:**
   - [ ] Add console.logs to verify state updates
   - [ ] Check Network tab to verify API calls
   - [ ] Test with different sort/search values
   - [ ] Fix any FE issues found

2. **BE Team:**
   - [ ] Verify API accepts `searchCode` parameter
   - [ ] Verify API accepts `sortBy` and `sortDirection` parameters
   - [ ] Test API directly with Postman
   - [ ] Check BE logs for incoming requests
   - [ ] Verify BE applies filters correctly

3. **Both Teams:**
   - [ ] Compare expected vs actual API requests
   - [ ] Verify parameter names match
   - [ ] Test end-to-end flow together

---

## 📞 Contact

- **FE Team:** Check FE code and browser console
- **BE Team:** Check BE controller and service implementation
- **Both:** Coordinate to identify root cause

---

---

## 🔍 ACTUAL TESTING RESULTS (From Console Logs)

### ✅ FE Side - Working Correctly

**Console Logs Show:**
1. ✅ `[handleFiltersChange]` is called when sort/search changes
2. ✅ `[API Call] Criteria` shows correct sort/search params
3. ✅ `[API Request] URL` shows params are sent correctly:
   - `GET /appointments?page=0&size=10&sortBy=appointmentId&sortDirection=DESC`
   - `GET /appointments?page=0&size=10&sortBy=appointmentId&sortDirection=ASC`
   - `GET /appointments?page=0&size=10&sortBy=appointmentStartTime&sortDirection=DESC`
4. ✅ `[API Request] Params` shows all params are included:
   - `{page: '0', size: '10', sortBy: 'appointmentId', sortDirection: 'DESC'}`
5. ✅ API response received with 7 items

### ❌ Problem: UI Not Updating Despite Correct API Calls

**Observation:**
- FE sends correct params to BE
- BE returns response (7 items)
- **BUT:** UI shows same order regardless of sort params
- **BUT:** Search does not filter by appointment code

**Conclusion:**
- ✅ FE code is working correctly (params sent, state updated)
- ❌ **BE is NOT applying sort** - returns same order for different sortBy/sortDirection
- ❌ **BE searchCode does NOT search by appointmentCode** - only searches patient/doctor/employee/room/service

---

## 🐛 ROOT CAUSE ANALYSIS

### Issue 1: Sort Not Working

**Evidence:**
- FE sends: `sortBy=appointmentId&sortDirection=DESC`
- FE sends: `sortBy=appointmentId&sortDirection=ASC`
- FE sends: `sortBy=appointmentStartTime&sortDirection=DESC`
- **All return same 7 items in same order**

**Root Cause:** 🔴 **BE ISSUE**
- BE receives sort params correctly (confirmed by FE logs)
- BE does NOT apply sorting to query results
- BE returns unsorted data regardless of sortBy/sortDirection params

**Expected Behavior:**
- `sortBy=appointmentId&sortDirection=DESC` → Should return appointments sorted by ID descending (newest first)
- `sortBy=appointmentId&sortDirection=ASC` → Should return appointments sorted by ID ascending (oldest first)
- `sortBy=appointmentStartTime&sortDirection=DESC` → Should return appointments sorted by start time descending (latest first)

**Actual Behavior:**
- All requests return same order (appears to be default order, not sorted)

### Issue 2: Search Not Working for Appointment Code

**Evidence:**
- User types "APT-20260102-001" in search box
- FE sends: `searchCode=APT-20260102-001`
- **No filtering occurs**

**Root Cause:** 🔴 **BE ISSUE**
- BE `searchCode` parameter only searches:
  - Patient code/name
  - Employee/doctor code/name
  - Room code
  - Service code
- **BE does NOT search by `appointmentCode`**

**Expected Behavior:**
- `searchCode=APT-20260102-001` → Should return appointment with code "APT-20260102-001"
- `searchCode=APT-001` → Should return all appointments with code containing "APT-001"

**Actual Behavior:**
- Search does not match appointment codes
- Only searches patient/doctor/room/service fields

---

## 📋 BE TEAM ACTION ITEMS

### 1. Fix Sort Functionality

**Problem:** Sort params are received but not applied to query

**Required Fix:**
- Verify `AppointmentListService` applies `sortBy` and `sortDirection` to database query
- Check if Pageable/Sort is correctly configured
- Test with Postman:
  ```
  GET /api/v1/appointments?page=0&size=10&sortBy=appointmentId&sortDirection=DESC
  GET /api/v1/appointments?page=0&size=10&sortBy=appointmentId&sortDirection=ASC
  ```
- Verify returned order is different between DESC and ASC

**Expected Test Results:**
- `sortBy=appointmentId&sortDirection=DESC` → First item should have highest appointmentId
- `sortBy=appointmentId&sortDirection=ASC` → First item should have lowest appointmentId
- `sortBy=appointmentStartTime&sortDirection=DESC` → First item should have latest start time

### 2. Fix Search to Include Appointment Code

**Problem:** `searchCode` does not search by `appointmentCode`

**Required Fix:**
- Update `AppointmentListService` to include `appointmentCode` in search logic
- Current search fields: patient code/name, employee code/name, room code, service code
- **Add:** `appointmentCode` to search fields

**Expected Behavior:**
- `searchCode=APT-20260102-001` → Returns appointment with exact code match
- `searchCode=APT-001` → Returns all appointments with code containing "APT-001"
- `searchCode=20260102` → Returns all appointments with code containing "20260102"

**Test Cases:**
```
GET /api/v1/appointments?page=0&size=10&searchCode=APT-20260102-001
→ Should return 1 appointment

GET /api/v1/appointments?page=0&size=10&searchCode=APT-001
→ Should return all appointments with code containing "APT-001"
```

---

## ✅ FE TEAM VERIFICATION

### Confirmed Working:
- ✅ State management (filters update correctly)
- ✅ API call construction (params sent correctly)
- ✅ Network requests (URL and params verified)
- ✅ Response handling (data received and displayed)

### No FE Issues Found:
- State updates correctly when sort/search changes
- API calls are made with correct parameters
- UI receives and displays response data
- **Problem is in BE response - not sorted/filtered correctly**

---

## 🧪 TESTING INSTRUCTIONS FOR BE TEAM

### Test 1: Sort by Appointment ID
```bash
# Test DESC
curl "http://localhost:8080/api/v1/appointments?page=0&size=10&sortBy=appointmentId&sortDirection=DESC" \
  -H "Authorization: Bearer <token>"

# Test ASC
curl "http://localhost:8080/api/v1/appointments?page=0&size=10&sortBy=appointmentId&sortDirection=ASC" \
  -H "Authorization: Bearer <token>"

# Expected: Different order between DESC and ASC
# DESC: Highest appointmentId first
# ASC: Lowest appointmentId first
```

### Test 2: Sort by Start Time
```bash
# Test DESC
curl "http://localhost:8080/api/v1/appointments?page=0&size=10&sortBy=appointmentStartTime&sortDirection=DESC" \
  -H "Authorization: Bearer <token>"

# Test ASC
curl "http://localhost:8080/api/v1/appointments?page=0&size=10&sortBy=appointmentStartTime&sortDirection=ASC" \
  -H "Authorization: Bearer <token>"

# Expected: Different order between DESC and ASC
# DESC: Latest start time first
# ASC: Earliest start time first
```

### Test 3: Search by Appointment Code
```bash
# Test exact match
curl "http://localhost:8080/api/v1/appointments?page=0&size=10&searchCode=APT-20260102-001" \
  -H "Authorization: Bearer <token>"

# Expected: Returns 1 appointment with code "APT-20260102-001"

# Test partial match
curl "http://localhost:8080/api/v1/appointments?page=0&size=10&searchCode=APT-001" \
  -H "Authorization: Bearer <token>"

# Expected: Returns all appointments with code containing "APT-001"
```

---

## 📊 SUMMARY

| Issue | FE Status | BE Status | Root Cause |
|-------|-----------|-----------|------------|
| **Sort not working** | ✅ Working (sends params correctly) | ❌ **NOT applying sort** | **BE Issue** - Sort params received but not applied to query |
| **Search by appointment code** | ✅ Working (sends searchCode) | ❌ **NOT searching appointmentCode** | **BE Issue** - searchCode only searches patient/doctor/room/service, not appointmentCode |

**Conclusion:** 🔴 **Both issues are BE problems**
- FE is working correctly
- BE receives params but doesn't apply them correctly
- BE needs to fix sort logic and add appointmentCode to search

---

**Last Updated:** 21/01/2026  
**Status:** ✅ **RESOLVED** - Both backend issues fixed in AppointmentRepository.java

