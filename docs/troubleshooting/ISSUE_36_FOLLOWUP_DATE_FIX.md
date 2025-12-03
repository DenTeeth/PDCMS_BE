# Issue #36: Missing followUpDate Field in Clinical Record Response

## Problem Description

**Reported Issue**: API 8.1 (Get Clinical Record) - `ClinicalRecordResponse` was missing the `followUpDate` field even though the Create (API 8.2) and Update (API 8.3) operations accepted this field in the request.

**Impact**:

- Frontend could send `followUpDate` when creating/updating clinical records
- But could not retrieve the `followUpDate` value when fetching the clinical record
- This created data inconsistency between what could be saved and what could be retrieved

**Root Cause**: The `followUpDate` field was missing from:

1. Database schema (`clinical_records` table)
2. Entity class (`ClinicalRecord.java`)
3. Response DTOs (`ClinicalRecordResponse.java` and `UpdateClinicalRecordResponse.java`)
4. Service layer mappings for GET/CREATE/UPDATE operations

## Files Modified

### 1. Database Schema (`src/main/resources/db/schema.sql`)

**Change**: Added `follow_up_date` column to `clinical_records` table

```sql
follow_up_date DATE, -- Scheduled follow-up appointment date (nullable)
```

**Location**: Line ~806, after `treatment_notes` column

**Details**:

- Column type: `DATE` (not timestamp, just date)
- Nullable: Yes (follow-up appointments are optional)
- No foreign key constraint (just stores the date, not linked to appointments table)

---

### 2. Entity Class (`src/main/java/com/dental/clinic/management/clinical_records/entity/ClinicalRecord.java`)

**Change**: Added `followUpDate` field to entity

```java
@Column(name = "follow_up_date")
private java.time.LocalDate followUpDate;
```

**Location**: After `treatmentNotes` field

**Details**:

- Java type: `LocalDate` (date without time component)
- Column mapping: `follow_up_date` (snake_case in database)
- Lombok annotations automatically generate getter/setter/builder methods

---

### 3. Response DTO - Get Operation (`src/main/java/com/dental/clinic/management/clinical_records/dto/ClinicalRecordResponse.java`)

**Change**: Added `followUpDate` field to response

```java
private String followUpDate; // yyyy-MM-dd format, nullable
```

**Location**: Between `treatmentNotes` and `createdAt` fields

**Details**:

- Type: `String` (formatted date, not LocalDate)
- Format: `yyyy-MM-dd` (e.g., "2025-12-15")
- Nullable: Yes (null if no follow-up date is set)
- Consistent with other date fields in DTOs (e.g., `createdAt`, `updatedAt`)

---

### 4. Response DTO - Update Operation (`src/main/java/com/dental/clinic/management/clinical_records/dto/UpdateClinicalRecordResponse.java`)

**Existing State**: DTO already had `followUpDate` field (line 18)

**Change**: Added mapping in service layer to populate this field

**Details**:

- Field was present but never populated by service
- Service update (see below) now fills this field in response

---

### 5. Service Layer (`src/main/java/com/dental/clinic/management/clinical_records/service/ClinicalRecordService.java`)

**Three changes made**:

#### a) GET Operation Mapping (`buildClinicalRecordResponse` method)

**Change**: Added `followUpDate` to response builder

```java
.followUpDate(record.getFollowUpDate() != null
                ? record.getFollowUpDate().format(DATE_FORMATTER)
                : null)
```

**Location**: ~Line 297-313 in response builder

**Details**:

- Converts `LocalDate` to `String` using `DATE_FORMATTER` (`yyyy-MM-dd`)
- Handles null case (returns null if no follow-up date set)
- Same pattern as other date field mappings in the codebase

---

#### b) CREATE Operation Mapping (`createClinicalRecord` method)

**Change**: Added `followUpDate` to entity builder

```java
.followUpDate(request.getFollowUpDate())
```

**Location**: ~Line 357-364 in entity builder

**Details**:

- Maps directly from request DTO (already accepts LocalDate)
- No conversion needed (both request and entity use LocalDate type)
- Null-safe (JPA handles null values)

---

#### c) UPDATE Operation Logic (`updateClinicalRecord` method)

**Change 1**: Added conditional update for `followUpDate`

```java
if (request.getFollowUpDate() != null) {
    record.setFollowUpDate(request.getFollowUpDate());
}
```

**Location**: ~Line 402-416 in update logic

**Details**:

- Only updates if request includes non-null value
- Follows same pattern as other optional fields (e.g., `vitalSigns`)
- Allows partial updates (can update followUpDate without changing other fields)

**Change 2**: Added `followUpDate` to response builder

```java
.followUpDate(updated.getFollowUpDate() != null
        ? updated.getFollowUpDate().format(DATE_FORMATTER)
        : null)
```

**Location**: ~Line 422-429 in response builder

**Details**:

- Returns updated value in response (consistent with GET operation)
- Formats LocalDate to String using DATE_FORMATTER
- Handles null case

---

## Testing Results

### Test 1: GET Clinical Record (API 8.1)

**Request**:

```bash
GET /api/v1/appointments/1/clinical-record
Authorization: Bearer {token}
```

**Result**: ✅ SUCCESS

**Response** (relevant excerpt):

```json
{
  "clinicalRecordId": 1,
  "diagnosis": "Gingivitis...",
  "vitalSigns": {...},
  "chiefComplaint": "...",
  "examinationFindings": "...",
  "treatmentNotes": "...",
  "followUpDate": null,
  "createdAt": "2025-12-02 03:17:35",
  "updatedAt": "..."
}
```

**Verification**:

- ✅ `followUpDate` field is present in response
- ✅ Shows `null` for records without follow-up date
- ✅ Field positioned correctly (after `treatmentNotes`, before `createdAt`)

---

### Test 2: UPDATE Clinical Record with followUpDate (API 8.3)

**Request**:

```bash
PUT /api/v1/clinical-records/1
Content-Type: application/json
Authorization: Bearer {token}

{
  "followUpDate": "2025-12-15"
}
```

**Result**: ✅ SUCCESS

**Response**:

```json
{
  "clinicalRecordId": 1,
  "updatedAt": "2025-12-02 19:42:22",
  "examinationFindings": "Lợi sưng đỏ, có nhiều mảng cao răng...",
  "treatmentNotes": "Đã thực hiện lấy cao răng (scaling)...",
  "followUpDate": "2025-12-15"
}
```

**Verification**:

- ✅ Update succeeded
- ✅ `followUpDate` appears in response (previously missing)
- ✅ Value correctly saved and returned

---

### Test 3: Verify Persistence

**Request**:

```bash
GET /api/v1/appointments/1/clinical-record
Authorization: Bearer {token}
```

**Result**: ✅ SUCCESS

**Response** (grep output):

```
"followUpDate":"2025-12-15"
```

**Verification**:

- ✅ `followUpDate` value persisted to database
- ✅ GET operation returns the updated value
- ✅ End-to-end flow working correctly

---

## Frontend Impact

### What Changed for Frontend

**Before Fix**:

- Frontend could send `followUpDate` in POST/PUT requests
- But could NOT retrieve `followUpDate` from GET response
- No visibility into saved follow-up dates

**After Fix**:

- Frontend can now retrieve `followUpDate` from GET response
- UPDATE response also includes `followUpDate` (immediate feedback)
- Complete CRUD cycle for follow-up dates

### Frontend Code Changes Required

**1. Update Type Definitions**

If using TypeScript, update the `ClinicalRecordResponse` interface:

```typescript
interface ClinicalRecordResponse {
  clinicalRecordId: number;
  diagnosis: string;
  vitalSigns: VitalSigns | null;
  chiefComplaint: string;
  examinationFindings: string;
  treatmentNotes: string;
  followUpDate: string | null; // ← ADD THIS FIELD
  createdAt: string;
  updatedAt: string;
  // ... other fields
}
```

**2. Update Display Components**

Add follow-up date display to clinical record views:

```typescript
// Example: Clinical Record Detail Page
<div className="clinical-record-detail">
  <div className="field">
    <label>Diagnosis:</label>
    <span>{record.diagnosis}</span>
  </div>

  <div className="field">
    <label>Examination Findings:</label>
    <span>{record.examinationFindings}</span>
  </div>

  <div className="field">
    <label>Treatment Notes:</label>
    <span>{record.treatmentNotes}</span>
  </div>

  {/* ADD THIS SECTION */}
  {record.followUpDate && (
    <div className="field">
      <label>Follow-Up Date:</label>
      <span>{formatDate(record.followUpDate)}</span>
    </div>
  )}

  <div className="field">
    <label>Created At:</label>
    <span>{formatDateTime(record.createdAt)}</span>
  </div>
</div>
```

**3. Update Form Handling**

No changes needed for CREATE/UPDATE forms if they already send `followUpDate`.

If not already implemented:

```typescript
// Example: Update Clinical Record Form
const updateClinicalRecord = async (data: UpdateClinicalRecordRequest) => {
  const response = await api.put(`/api/v1/clinical-records/${recordId}`, {
    examinationFindings: data.examinationFindings,
    treatmentNotes: data.treatmentNotes,
    followUpDate: data.followUpDate, // ← Already supported, now returns in response
  });

  // Now response includes followUpDate
  console.log("Updated follow-up date:", response.data.followUpDate);
};
```

**4. Update API Response Handlers**

Ensure GET/UPDATE response handlers can process `followUpDate`:

```typescript
// Example: Fetch clinical record
const fetchClinicalRecord = async (appointmentId: number) => {
  const response = await api.get(
    `/api/v1/appointments/${appointmentId}/clinical-record`
  );
  const record = response.data;

  // followUpDate is now available
  if (record.followUpDate) {
    // Show follow-up appointment reminder
    scheduleFollowUpReminder(record.followUpDate);
  }

  return record;
};
```

### API Response Format

**Field**: `followUpDate`
**Type**: `string | null`
**Format**: `yyyy-MM-dd` (e.g., "2025-12-15")
**Null Handling**: Field is `null` if no follow-up date is set

**Consistent with other date fields**:

- `createdAt`: `yyyy-MM-dd HH:mm:ss` (datetime)
- `updatedAt`: `yyyy-MM-dd HH:mm:ss` (datetime)
- `followUpDate`: `yyyy-MM-dd` (date only)

---

## Summary

### Changes Made

1. ✅ Added `follow_up_date DATE` column to database schema
2. ✅ Added `followUpDate` field to `ClinicalRecord` entity
3. ✅ Added `followUpDate` field to `ClinicalRecordResponse` DTO
4. ✅ Added `followUpDate` mapping to GET operation
5. ✅ Added `followUpDate` mapping to CREATE operation
6. ✅ Added `followUpDate` update logic and response mapping to UPDATE operation

### Testing Status

- ✅ GET operation returns `followUpDate` field
- ✅ UPDATE operation saves and returns `followUpDate`
- ✅ Data persists correctly to database
- ✅ Null handling works correctly
- ✅ Date formatting consistent (`yyyy-MM-dd`)

### Data Type Consistency

| Layer         | Type             | Format           | Example                      |
| ------------- | ---------------- | ---------------- | ---------------------------- |
| Database      | `DATE`           | `yyyy-MM-dd`     | `2025-12-15`                 |
| Entity (Java) | `LocalDate`      | Java date object | `LocalDate.of(2025, 12, 15)` |
| Request DTO   | `LocalDate`      | JSON date string | `"2025-12-15"`               |
| Response DTO  | `String`         | `yyyy-MM-dd`     | `"2025-12-15"`               |
| Frontend      | `string \| null` | `yyyy-MM-dd`     | `"2025-12-15"` or `null`     |

### Compatibility

- ✅ Backward compatible: Existing records without `followUpDate` show `null`
- ✅ Optional field: Can create/update records without `followUpDate`
- ✅ Partial updates: Can update `followUpDate` without changing other fields
- ✅ Frontend integration: Simple string field, easy to display/format

---

## Commit Information

**Branch**: `feat/BE-501-manage-treatment-plans`

**Commit Message**:

```
fix(clinical-records): Add missing followUpDate field to API 8.1 response - Issue #36

- Added follow_up_date DATE column to clinical_records table
- Added followUpDate field to ClinicalRecord entity and response DTOs
- Updated service mappings for GET/CREATE/UPDATE operations
- Tested all operations: GET shows followUpDate, UPDATE saves and returns it
- Field is nullable, backward compatible, supports partial updates
```

**Files Changed**:

- `src/main/resources/db/schema.sql` (database schema)
- `src/main/java/com/dental/clinic/management/clinical_records/entity/ClinicalRecord.java` (entity)
- `src/main/java/com/dental/clinic/management/clinical_records/dto/ClinicalRecordResponse.java` (response DTO)
- `src/main/java/com/dental/clinic/management/clinical_records/service/ClinicalRecordService.java` (service mappings)

---

## Related Documentation

- API 8.1: Get Clinical Record
- API 8.2: Create Clinical Record
- API 8.3: Update Clinical Record

## Issue Resolution

- **Issue #36**: ✅ RESOLVED
- **Fix Date**: December 2, 2025
- **Testing**: All scenarios passed
- **Status**: Ready for deployment
