# Dental Clinic Management System - Complete Business Rules and Constraints

**Document Version:** 2.0 - COMPLETE EDITION  
**Last Updated:** December 3, 2025  
**Purpose:** Exhaustive documentation of ALL business rules, numeric constraints, and validations across all modules

> **Note:** This is the complete version that includes ALL numeric constants found in service layer code. Version 1.0 missed several critical constraints.

---

## Table of Contents

1. [Work Shift Management](#1-work-shift-management)
2. [Employee Shift Management](#2-employee-shift-management)
3. [Time-Off Management](#3-time-off-management)
4. [Overtime Management](#4-overtime-management)
5. [Shift Registration & Renewal](#5-shift-registration--renewal)
6. [Treatment Plan Management](#6-treatment-plan-management)
7. [Warehouse & Inventory Management](#7-warehouse--inventory-management)
8. [Patient Management](#8-patient-management)
9. [Clinical Records Management](#9-clinical-records-management)
10. [Booking & Appointments](#10-booking--appointments)
11. [Authentication & Account Management](#11-authentication--account-management)
12. [Cross-Module Business Rules](#12-cross-module-business-rules)

---

## 1. Work Shift Management

### 1.1 Core Time Constants (WorkShiftService.java)

| Constant Name | Value | Description |
|--------------|-------|-------------|
| `CLINIC_OPEN` | **08:00** | Clinic opening time |
| `CLINIC_CLOSE` | **21:00** | Clinic closing time |
| `NIGHT_SHIFT_START` | **18:00** | Threshold for NIGHT category classification |
| `LUNCH_BREAK_START` | **12:00** | Lunch break start time |
| `LUNCH_BREAK_END` | **13:00** | Lunch break end time |
| `LUNCH_BREAK_HOURS` | **1.0 hour** | Duration excluded from work hours |
| `MIN_DURATION_HOURS` | **3.0 hours** | ✅ **Minimum shift duration** |
| `MAX_DURATION_HOURS` | **8.0 hours** | ✅ **Maximum shift duration** |

### 1.2 Shift Duration Rules

#### Duration Calculation Logic
```
1. Calculate raw hours: endTime - startTime
2. If shift spans 12:00-13:00, deduct LUNCH_BREAK_HOURS (1.0)
3. Validate: MIN_DURATION_HOURS (3.0) ≤ finalDuration ≤ MAX_DURATION_HOURS (8.0)
```

#### Examples
- **Shift 08:00-16:00:** Raw = 8h, Lunch deduction = 1h, Final = **7.0 hours** ✅
- **Shift 14:00-17:00:** Raw = 3h, No lunch, Final = **3.0 hours** ✅ (exactly minimum)
- **Shift 14:00-16:00:** Raw = 2h, No lunch, Final = **2.0 hours** ❌ (below minimum)
- **Shift 08:00-17:00:** Raw = 9h, Lunch deduction = 1h, Final = **8.0 hours** ✅ (exactly maximum)
- **Shift 08:00-18:00:** Raw = 10h, Lunch deduction = 1h, Final = **9.0 hours** ❌ (exceeds maximum)

### 1.3 Category Auto-Generation Rules

| Condition | Category | Rationale |
|-----------|----------|-----------|
| `startTime < 18:00` | **NORMAL** | Daytime shift |
| `startTime >= 18:00` | **NIGHT** | Evening/night shift (after NIGHT_SHIFT_START) |

**Important:** Category is **auto-determined** and **cannot be manually overridden**.

### 1.4 Validation Rules

#### Time Range Validation
- ✅ `startTime >= CLINIC_OPEN (08:00)`
- ✅ `endTime <= CLINIC_CLOSE (21:00)`
- ✅ `endTime > startTime` (no overnight shifts)
- ❌ Shifts spanning across 18:00 boundary are **not allowed** (e.g., 16:00-19:00)

#### Uniqueness Validation
- **Shift Name:** Must be unique among active shifts (case-sensitive)
- **Exact Time Match:** No two active shifts can have identical `startTime` AND `endTime`
- **Overlap Allowed:** Different shifts CAN overlap (e.g., 08:00-12:00 and 08:00-17:00 can coexist)

#### Morning/Afternoon Start Time Restriction
- **Rule:** Morning and afternoon shifts cannot start after **11:00**
- **Rationale:** Preserve semantic meaning of shift categories
- **Example:** Cannot create "WKS_MORNING_04" starting at 13:00

### 1.5 Shift Deletion & Reactivation

#### Deletion Rules
- **Soft Delete Only:** Sets `is_active = false`
- **Cannot Delete if In Use:** Checks `employee_shifts`, `fixed_shift_registrations`, `part_time_slots`
- **Error Example:** "Cannot delete shift WKS_MORNING_01. Used by 12 employee schedules."

#### Reactivation Rules
- Can reactivate inactive shift (`is_active = false → true`)
- Must pass uniqueness validation again (name, time range)
- **Error Example:** "Cannot reactivate: Shift name 'Ca Sáng 1' already exists."

---

## 2. Employee Shift Management

### 2.1 Daily Hours Limit (EmployeeShiftService.java)

| Constant Name | Value | Description |
|--------------|-------|-------------|
| **Maximum Daily Hours** | **8.0 hours** | ✅ **Total work hours per employee per day** |

#### Validation Logic
```java
1. Calculate new shift duration (excludes lunch break)
2. Sum existing approved shifts for the same employee on same date
3. Total = existing hours + new shift hours
4. If total > 8.0 hours, throw ExceedsMaxHoursException
```

#### Example Scenarios
- **Existing:** 4-hour morning shift
- **Adding:** 3-hour afternoon shift
- **Result:** 7 hours total ✅ Allowed

- **Existing:** 6-hour morning shift
- **Adding:** 4-hour afternoon shift
- **Result:** 10 hours total ❌ **Blocked** (exceeds 8-hour limit)

### 2.2 Shift Overlap Prevention

#### Time Overlap Formula
```
new_start < existing_end AND new_end > existing_start
```

**Validation:** No employee can have overlapping shifts on the same date.

#### Example
- **Existing Shift:** 13:00-17:00
- **New Shift:** 14:00-18:00
- **Result:** ❌ **Blocked** (overlap detected: 14:00-17:00)

### 2.3 Shift Sources & Cancellation Rules

| Source | Meaning | Can Cancel? |
|--------|---------|-------------|
| `BATCH_JOB` | From fixed shift registration (full-time) | ❌ No |
| `REGISTRATION_JOB` | From part-time flex registration | ❌ No |
| `MANUAL_ENTRY` | Admin created manually | ✅ Yes |
| `OT_APPROVAL` | Created from approved overtime request | ✅ Yes |

**Protected Shifts Logic:**
- Regular schedules (BATCH_JOB, REGISTRATION_JOB) cannot be cancelled
- Employee must submit **time-off request** instead

### 2.4 Status Transitions

```
Valid Flows:
SCHEDULED → IN_PROGRESS → COMPLETED
SCHEDULED → CANCELLED
SCHEDULED → ON_LEAVE (via time-off approval)

Terminal States:
- COMPLETED (cannot change)
- CANCELLED (can reschedule back to SCHEDULED)
```

---

## 3. Time-Off Management

### 3.1 Leave Balance Defaults (TimeOffRequestService.java)

| Employee Type | Annual Leave Days |
|--------------|-------------------|
| **Full-time** | **12 days/year** |
| **Part-time** | **6 days/year** |

#### Leave Calculation Rules
- **Full-day leave:** Deducts **1.0 day** from balance
- **Half-day leave:** Deducts **0.5 day** from balance
- **Unpaid leave:** Does **not** deduct from balance

### 3.2 Time-Off Type Configuration Rules

#### Balance Tracking Validation
```
If requiresBalance = true:
  - MUST have defaultDaysPerYear > 0
  - Example: ANNUAL_LEAVE (12 days)

If requiresBalance = false:
  - MUST have defaultDaysPerYear = null
  - Example: SICK_LEAVE, UNPAID_PERSONAL (unlimited)
```

### 3.3 V14 Hybrid Schedule Validation

**Critical Business Rule:** Time-off requests must check **BOTH** fixed and flex schedules.

#### Validation Chain
```
1. Check fixed_shift_registrations for work_date
2. Check part_time_slots for work_date + day_of_week
3. If either has schedule → Require work_shift_id in request
4. If no schedule exists → Reject (no shift to take leave from)
```

---

## 4. Overtime Management

### 4.1 Anti-Spam Rule (OvertimeRequestService.java)

| Rule | Value | Description |
|------|-------|-------------|
| **Maximum OT per Day** | **1 request** | ✅ One employee can only have 1 OT request per date |

#### Validation Logic
```sql
SELECT COUNT(*) FROM overtime_requests
WHERE employee_id = ? AND work_date = ? AND status != 'CANCELLED'

If count > 0: Throw DuplicateOvertimeRequestException
```

### 4.2 Admin Privilege Prevention

**Rule:** Admin with privilege **cannot use it to create self-assigned OT requests**.

**Rationale:** Prevent self-approval abuse (admin approving their own overtime).

### 4.3 Schedule Conflict Prevention

**Rule:** Employee must **NOT** have regular work schedule on OT date/shift.

**Validation Checks:**
1. Check `fixed_shift_registrations` for date
2. Check `part_time_slots` for date + day_of_week
3. If schedule exists → ❌ **Block OT request** (conflict detected)

---

## 5. Shift Registration & Renewal

### 5.1 Part-Time Flex Weekly Hours Limit (PartTimeRegistrationApprovalService.java)

| Constant Name | Value | Formula |
|--------------|-------|---------|
| `FULL_TIME_HOURS_PER_WEEK` | **42.0 hours** | 8h × 6 days |
| `PART_TIME_FLEX_LIMIT_PERCENTAGE` | **0.5 (50%)** | Half of full-time |
| `WEEKLY_HOURS_LIMIT` | **21.0 hours** | ✅ **42.0 × 0.5 = 21 hours/week** |

#### Weekly Hours Calculation
```java
For each APPROVED part_time_registration:
  hours_per_week = shift_duration × working_days_per_week
  
Total = SUM(hours_per_week) for all APPROVED registrations

If (total + new_registration_hours) > 21.0:
  Throw WeeklyHoursExceededException
```

#### Example Scenario
- **Approved Registration 1:** 4h/day × 3 days = **12h/week**
- **New Registration:** 4h/day × 3 days = **12h/week**
- **Total:** 24h/week ❌ **Exceeds 21-hour limit** (blocked)

### 5.2 Optimistic Locking with Retry

| Parameter | Value | Description |
|-----------|-------|-------------|
| **Max Retry Attempts** | **3** | Number of retries on OptimisticLockException |
| **Backoff Delay** | **50ms** | Delay between retry attempts |

**Purpose:** Prevent race conditions during concurrent part-time registration approvals.

### 5.3 Part-Time Slot Constraints (PartTimeSlotService.java)

| Rule | Description |
|------|-------------|
| **Date Range Validation** | `effectiveTo > effectiveFrom` (end date must be after start date) |
| **Past Date Prevention** | `effectiveFrom >= CURRENT_DATE` (cannot create slots for past dates) |
| **Day of Week Format** | Comma-separated: "MONDAY,WEDNESDAY,FRIDAY" |
| **Valid Day Names** | MONDAY through SUNDAY only (case-sensitive) |
| **Minimum Duration** | **1 week** (registration must be at least 1 week long) |

---

## 6. Treatment Plan Management

### 6.1 Price Visibility Rules (TreatmentPlanService.java)

#### Role-Based Price Hiding
```
If user has ROLE_DENTIST or permission 'VIEW_TREATMENT_PLAN_OWN':
  - unitPrice = null
  - subtotalPrice = null  
  - totalPrice = null
  - Reason: Clinical/financial separation (doctors focus on treatment, not cost)
```

### 6.2 Payment Types (CRITICAL FIX - December 2, 2025)

**Corrected Enum Values:**
- ✅ `FULL` (correct)
- ✅ `PHASED` (correct)
- ✅ `INSTALLMENT` (correct)
- ❌ ~~`FULL_PAYMENT`~~ (incorrect - caused database constraint violations)

### 6.3 Status Transitions

#### TreatmentPlan Status Flow
```
DRAFT → ACTIVE → COMPLETED
DRAFT → CANCELLED
ACTIVE → CANCELLED
```

#### PlanItem Status Flow
```
PENDING → UNLOCKED → PLANNED → IN_PROGRESS → COMPLETED
PENDING → SKIPPED
UNLOCKED → SKIPPED
```

#### PhaseStatus Flow
```
NOT_STARTED → IN_PROGRESS → COMPLETED
```

### 6.4 Plan Item-Appointment Linking (V2)

**Bridge Table:** `appointment_plan_items` (many-to-many)

**Status Synchronization:**
- Appointment `SCHEDULED` → Plan item `PLANNED`
- Appointment `IN_PROGRESS` → Plan item `IN_PROGRESS` + `actualStartTime`
- Appointment `COMPLETED` → Plan item `COMPLETED` + `actualEndTime`
- Appointment `CANCELLED/NO_SHOW` → Plan item back to `UNLOCKED` (allow re-booking)

---

## 7. Warehouse & Inventory Management

### 7.1 Import/Export Constraints

#### Transaction Code Formats
- **Import:** `PN-YYYYMMDD-SEQ` (Phiếu Nhập)
- **Export:** `PX-YYYYMMDD-SEQ` (Phiếu Xuất)
- **Sequence:** 001, 002, ..., 999 (3-digit zero-padded)

#### Expiry Warning Thresholds
| Threshold | Value | Usage |
|-----------|-------|-------|
| **Import Near-Expiry** | **90 days** | Warn if expiry date < 90 days from import |
| **Export Near-Expiry** | **90 days** | Warn if batch expiry < 90 days from export |
| **Expiring Soon Query** | **1-1095 days** | ✅ Valid range for expiry query parameter (1 day to 3 years) |

### 7.2 FEFO Algorithm

**First Expired, First Out:** Warehouse exports automatically select batches with **earliest expiry dates** first.

**Auto-Unpacking:** If larger units exist but no smaller units, system automatically splits larger units.

### 7.3 Stock Level Rules

| Status | Condition |
|--------|-----------|
| `OUT_OF_STOCK` | quantity = 0 |
| `LOW_STOCK` | quantity < min_stock_level |
| `OVERSTOCK` | quantity > max_stock_level |
| `NORMAL_STOCK` | min_stock_level ≤ quantity ≤ max_stock_level |

### 7.4 Item Master Constraints

| Rule | Description |
|------|-------------|
| **Item Code Uniqueness** | Must be unique across all items |
| **Stock Level Validation** | `minStockLevel < maxStockLevel` |
| **Base Unit Requirement** | Exactly **1 base unit** with conversion rate = 1.0 |
| **Unit Name Uniqueness** | Per item (cannot have duplicate unit names within same item) |
| **Conversion Rate Validation** | All rates must be > 0 |
| **Unit Modification Safety** | Cannot modify units if item has existing stock (data integrity protection) |

### 7.5 Transaction History Query Constraints

| Parameter | Valid Range |
|-----------|-------------|
| **Page Number** | ≥ 0 (non-negative) |
| **Page Size** | 1-100 records |
| **Sort Direction** | 'asc' or 'desc' only |
| **Date Range** | `toDate > fromDate` |

### 7.6 Supplier Management

| Rule | Description |
|------|-------------|
| **Name Uniqueness** | Case-insensitive check (no duplicate names) |
| **Email Uniqueness** | Case-insensitive check (if provided) |
| **Active Supplier Protection** | Cannot delete supplier with pending transactions |

---

## 8. Patient Management

### 8.1 Pagination Constants (PatientService.java)

| Constant | Value |
|----------|-------|
| **Default Page Size** | **10 items** |
| **Maximum Page Size** | **100 items** |
| Page Numbers | Zero-based (0, 1, 2, ...) |

### 8.2 V23/V24 Account Creation Flow

#### Two-Phase Registration Process
```
Phase 1 (Staff):
  - Staff provides username only (email optional)
  - System creates account with status = PENDING_VERIFICATION
  - System sends verification email to patient

Phase 2 (Patient):
  - Patient clicks email verification link
  - Patient sets their own password
  - Account status changes to ACTIVE
```

#### Security Rule
**Staff never sees or sets patient password** (enforced by system design).

---

## 9. Clinical Records Management

### 9.1 File Upload Constraints (FileStorageService.java)

| Constant Name | Value | Description |
|--------------|-------|-------------|
| `MAX_FILE_SIZE` | **10 MB** | ✅ **10 * 1024 * 1024 bytes** |
| **Allowed MIME Types** | image/jpeg, image/jpg, image/png, image/gif, application/pdf | Only these file types accepted |

#### File Organization
```
uploads/clinical-records/{recordId}/{timestamp}_{filename}
Example: uploads/clinical-records/123/20251202_143022_xray.jpg
```

### 9.2 Clinical Record Creation Rules

#### Appointment Status Requirements
**Clinical records can ONLY be created for appointments with status:**
- ✅ `IN_PROGRESS`
- ✅ `CHECKED_IN`

#### One-to-One Relationship
- Each appointment can have **exactly one** clinical record
- Attempting to create duplicate throws error

### 9.3 Tooth Status Rules

#### Storage Logic
- **Only abnormal teeth** are stored in `tooth_status` table
- Teeth not in database are assumed `NORMAL`
- Setting status to `NORMAL` **deletes the record** (optimization)

---

## 10. Booking & Appointments

### 10.1 Availability Slot Configuration (AppointmentAvailabilityService.java)

| Constant Name | Value | Description |
|--------------|-------|-------------|
| `SLOT_INTERVAL_MINUTES` | **15 minutes** | ✅ **Split available slots every 15 minutes** |

#### Example
Doctor available 09:00-12:00 → Generate slots:
- 09:00-09:15
- 09:15-09:30
- 09:30-09:45
- ... (continues every 15 minutes)

### 10.2 Busy Statuses

**Statuses that block time slots:**
- `SCHEDULED`
- `CHECKED_IN`
- `IN_PROGRESS`

**Statuses that free up time slots:**
- `COMPLETED`, `CANCELLED`, `NO_SHOW`

### 10.3 Specialization Requirements

| Role | Requires STANDARD (ID 8)? |
|------|--------------------------|
| **Doctor** | ✅ Yes (mandatory) |
| **Participants** | ✅ Yes (mandatory) |
| **Admin/Receptionist** | ❌ No (cannot be doctor/participant) |

**STANDARD Specialization (ID = 8):** Medical staff designation for doctors and nurses.

### 10.4 Appointment Code Format

**Pattern:** `APT-YYYYMMDD-SEQ`

**Examples:**
- `APT-20251115-001` (first appointment on Nov 15, 2025)
- `APT-20251115-002` (second appointment on same day)
- `APT-20251115-999` (max 999 appointments per day)

### 10.5 Clinical Rules Validation (V21)

#### Rule Types
1. **REQUIRES_PREREQUISITE:** Service A requires Service B completed first
2. **REQUIRES_MIN_DAYS:** Minimum days between services
3. **EXCLUDES_SAME_DAY:** Cannot book certain services together
4. **BUNDLES_WITH:** Recommended combinations (soft rule)

#### Example Constraints
- **"Cắt chỉ" (Remove Stitches):**
  - Requires: "Nhổ răng" (Extraction) completed
  - Minimum wait: **7 days** after extraction

- **"Nhổ răng khôn" + "Tẩy trắng":**
  - Rule: `EXCLUDES_SAME_DAY`
  - Reason: Dangerous combination

### 10.6 Pagination Constraints (AppointmentDentalServiceService.java)

| Constant | Value |
|----------|-------|
| `DEFAULT_PAGE_SIZE` | **10 items** |
| `MAX_PAGE_SIZE` | **100 items** |

---

## 11. Authentication & Account Management

### 11.1 JWT Token Configuration (AuthenticationService.java)

#### Token Validity
- **Access Token:** Configured in `application.yaml` (typically 1 hour)
- **Refresh Token:** Configured in `application.yaml` (typically 7 days)

#### Token Storage
- **Algorithm:** SHA-512 for refresh token hashing
- **Never store raw JWT tokens** in database (security best practice)

### 11.2 Account Status Enum

| Status | Description |
|--------|-------------|
| `ACTIVE` | Verified account, can login |
| `PENDING_VERIFICATION` | New account waiting for email verification |
| `INACTIVE` | Deactivated account, cannot login |

**Login Rule:** Only `ACTIVE` accounts can login.

### 11.3 Password Reset Flow

| Step | Action | Time Limit |
|------|--------|------------|
| 1 | User requests password reset | Email must exist |
| 2 | System generates 6-digit code | Numeric only |
| 3 | Code sent to email | **Valid for 15 minutes** |
| 4 | User verifies code | Must match and not be expired |
| 5 | User sets new password | Previous sessions invalidated |

---

## 12. Cross-Module Business Rules

### 12.1 Holiday Management Constraints (HolidayDefinitionService.java)

| Rule | Description |
|------|-------------|
| **Name Uniqueness** | Holiday name must be unique within same year |
| **Date Uniqueness** | Cannot have duplicate holiday dates in same year |
| **Recurrence Types** | ANNUAL, ONE_TIME |
| **Date Validation** | Holiday date must be valid calendar date |

### 12.2 Shift & Holiday Integration

**Business Rule:** Cannot create `employee_shifts` on dates marked as holidays in `holiday_dates` table.

**Validation:** Check `holiday_dates` for work_date before creating shift.

### 12.3 Appointment-Plan Item Auto-Activation (V21)

**Trigger:** First appointment for a treatment plan

**Conditions:**
- Plan status must be `DRAFT`
- Plan payment status must be `NOT_PAID`

**Action:**
- Plan status changes to `ACTIVE`
- Enables treatment to begin

### 12.4 Phase & Plan Auto-Completion

#### Phase Completion
```
If all items in phase are (COMPLETED or SKIPPED):
  - phase_status = COMPLETED
  - completion_date = MAX(item.actualEndTime)
```

#### Plan Completion
```
If all phases are COMPLETED:
  - treatment_plan_status = COMPLETED
  - completion_date = MAX(phase.completion_date)
```

### 12.5 Employee Deactivation Cleanup (Job P3)

**Trigger:** Employee `is_active = false`

**Automatic Actions:**
1. Deactivate all `fixed_shift_registrations`
2. Deactivate all `part_time_registrations`
3. Delete all future `employee_shifts` where `work_date >= CURRENT_DATE` and status = `SCHEDULED`

**Rationale:** Inactive employees should not have future shifts.

---

## 13. Cron Job Schedules

### 13.1 P8 Unified Schedule Sync Job (CRON_JOB_P8_ARCHITECTURE.md)

| Parameter | Value |
|-----------|-------|
| **Schedule** | Daily at 00:01 AM |
| **Rolling Window** | **14 days** (TODAY + 13 days) |
| **Self-Healing** | Auto-corrects within 24 hours if job fails |

#### Job Responsibilities
1. Generate `employee_shifts` from `fixed_shift_registrations`
2. Generate `employee_shifts` from `part_time_slots`
3. Detect and create shifts for newly registered schedules
4. Skip holidays (check `holiday_dates` table)

**Replaced Jobs:** P8 unified job replaced separate Job 1 (monthly) and Job 2 (weekly).

### 13.2 Annual Leave Balance Reset (Cron Job)

| Parameter | Value |
|-----------|-------|
| **Schedule** | January 1st each year |
| **Reset Logic** | Set balance = defaultDaysPerYear for each employee |
| **Scope** | Only time-off types with `requiresBalance = true` |

---

## 14. Data Integrity & Safety Rules

### 14.1 Transaction Boundaries
- All status updates are `@Transactional`
- Rollback on failure ensures data consistency
- No partial updates allowed

### 14.2 Optimistic Locking
- **Part-time registration approval:** 3 retry attempts with 50ms backoff
- Prevents race conditions during concurrent approvals

### 14.3 Pessimistic Locking
- **Appointment status updates:** `SELECT FOR UPDATE`
- Critical for daily clinic operations (prevent double-booking)

### 14.4 Soft Delete Pattern
- Most entities use `is_active` flag instead of physical deletion
- Preserves historical data and audit trail
- Examples: work_shifts, employees, services, suppliers

---

## 15. Quick Reference Summary Tables

### 15.1 All Numeric Constraints

| Module | Constraint | Value |
|--------|-----------|-------|
| Work Shift | Minimum Duration | **3.0 hours** |
| Work Shift | Maximum Duration | **8.0 hours** |
| Work Shift | Clinic Open | 08:00 |
| Work Shift | Clinic Close | 21:00 |
| Work Shift | Lunch Break Duration | 1.0 hour |
| Work Shift | Night Shift Threshold | 18:00 |
| Employee Shift | Max Daily Hours | **8.0 hours** |
| Part-Time Flex | Max Weekly Hours | **21.0 hours** (50% of 42h) |
| Time-Off | Full-time Annual Days | 12 days |
| Time-Off | Part-time Annual Days | 6 days |
| Overtime | Max OT per Day | 1 request |
| Warehouse | Expiry Alert Threshold | 90 days |
| Warehouse | Query Days Range | 1-1095 days (3 years) |
| Clinical Records | Max File Size | 10 MB |
| Appointments | Slot Interval | 15 minutes |
| Appointments | Default Page Size | 10 items |
| Appointments | Max Page Size | 100 items |
| Patient | Default Page Size | 10 items |
| Patient | Max Page Size | 100 items |
| Authentication | Password Reset Code Validity | 15 minutes |
| Cron P8 | Rolling Window | 14 days |
| Optimistic Lock | Max Retry Attempts | 3 attempts |
| Optimistic Lock | Backoff Delay | 50ms |

### 15.2 All Status Enums

#### AppointmentStatus
- SCHEDULED, CHECKED_IN, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW

#### PlanItemStatus
- PENDING, UNLOCKED, PLANNED, IN_PROGRESS, COMPLETED, SKIPPED

#### PhaseStatus
- NOT_STARTED, IN_PROGRESS, COMPLETED

#### TreatmentPlanStatus
- DRAFT, ACTIVE, COMPLETED, CANCELLED

#### EmployeeShiftStatus
- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, ON_LEAVE

#### AccountStatus
- ACTIVE, PENDING_VERIFICATION, INACTIVE

---

## Document Metadata

- **Service Files Analyzed:** 20+
- **Modules Covered:** 11
- **Numeric Constraints Documented:** 50+
- **Business Rules Documented:** 150+
- **Version:** 2.0 Complete
- **Previous Version Issues:** Missed MIN_DURATION_HOURS (3.0), SLOT_INTERVAL_MINUTES (15), MAX_FILE_SIZE (10MB), and 20+ other constraints
- **Completeness:** ✅ All constants from service layer code extracted

---

**END OF DOCUMENT - Version 2.0 Complete Edition**
