# PERMISSION SYSTEM OPTIMIZATION ANALYSIS

## For Small-Medium Private Dental Clinic

**Date**: December 19, 2025
**Objective**: Optimize 169 permissions to match REAL dental clinic operations

---

## PART 1: CURRENT STATE ANALYSIS

### Total Permissions: **169 permissions** across 16 modules

#### Module Breakdown:

1. **ACCOUNT** (4 perms) - Quản lý tài khoản
2. **EMPLOYEE** (6 perms) - Quản lý nhân viên
3. **PATIENT** (4 perms) - Quản lý bệnh nhân
4. **TREATMENT** (4 perms) - Quản lý điều trị (old)
5. **APPOINTMENT** (8 perms) - Quản lý lịch hẹn
6. **CUSTOMER_MANAGEMENT** (8 perms) - Liên hệ khách hàng
7. **SCHEDULE_MANAGEMENT** (27 perms) - Quản lý ca làm việc ⚠️ **TOO COMPLEX!**
8. **LEAVE_MANAGEMENT** (14 perms) - Nghỉ phép & tăng ca
9. **SYSTEM_CONFIGURATION** (12 perms) - Role/Permission/Specialization
10. **HOLIDAY** (4 perms) - Ngày nghỉ lễ
11. **ROOM_MANAGEMENT** (5 perms) - Quản lý phòng/ghế
12. **SERVICE_MANAGEMENT** (4 perms) - Quản lý dịch vụ
13. **TREATMENT_PLAN** (8 perms) - Phác đồ điều trị (new)
14. **WAREHOUSE** (19 perms) - Quản lý kho ⚠️ **TOO GRANULAR!**
15. **PATIENT_IMAGES** (8 perms) - Hình ảnh bệnh nhân
16. **NOTIFICATION** (3 perms) - Thông báo
17. **CLINICAL_RECORDS** (5 perms) - Bệnh án

---

## PART 2: PROBLEMS IDENTIFIED

### 🔴 **CRITICAL ISSUES**

#### 1. SCHEDULE_MANAGEMENT (27 permissions) - **OVER-ENGINEERED!**

```
❌ TOO MANY: Có đến 27 quyền chỉ để quản lý ca làm việc!
- VIEW_WORK_SHIFTS, CREATE_WORK_SHIFTS, UPDATE_WORK_SHIFTS, DELETE_WORK_SHIFTS (4)
- MANAGE_WORK_SLOTS, VIEW_AVAILABLE_SLOTS, MANAGE_PART_TIME_REGISTRATIONS (3)
- VIEW_REGISTRATION_ALL, VIEW_REGISTRATION_OWN (2)
- CREATE_REGISTRATION, UPDATE_REGISTRATION, UPDATE_REGISTRATIONS_ALL, UPDATE_REGISTRATION_OWN (4)
- CANCEL_REGISTRATION_OWN, DELETE_REGISTRATION, DELETE_REGISTRATION_ALL, DELETE_REGISTRATION_OWN (4)
- VIEW_RENEWAL_OWN, RESPOND_RENEWAL_OWN (2)
- VIEW_SHIFTS_ALL, VIEW_SHIFTS_OWN, VIEW_SHIFTS_SUMMARY (3)
- CREATE_SHIFTS, UPDATE_SHIFTS, DELETE_SHIFTS (3)
- MANAGE_FIXED_REGISTRATIONS, VIEW_FIXED_REGISTRATIONS_ALL, VIEW_FIXED_REGISTRATIONS_OWN (3)

🤔 REALITY CHECK: Phòng khám nha khoa nhỏ (~10-20 nhân viên):
- Manager/Admin tự quản lý ca làm việc thủ công
- Không cần phức tạp như hệ thống công ty lớn
- Nhân viên chỉ cần: XEM ca của mình + ĐĂNG KÝ ca mới + HỦY đăng ký

✅ SHOULD BE: ~8-10 permissions MAX!
- MANAGE_WORK_SHIFTS (Admin/Manager): Tạo/sửa/xóa mẫu ca
- VIEW_SCHEDULE_ALL (Manager): Xem lịch toàn bộ nhân viên
- VIEW_SCHEDULE_OWN (Employee): Xem lịch của mình
- CREATE_SHIFT_REGISTRATION (Employee): Đăng ký ca làm việc
- CANCEL_SHIFT_REGISTRATION (Employee): Hủy đăng ký
- APPROVE_SHIFT_REGISTRATION (Manager): Duyệt/từ chối đăng ký
```

#### 2. WAREHOUSE (19 permissions) - **TOO GRANULAR!**

```
❌ TOO DETAILED: 19 quyền riêng lẻ cho quản lý kho!
- VIEW_ITEMS, VIEW_MEDICINES, VIEW_WAREHOUSE (3 VIEW different things!)
- CREATE_ITEMS, UPDATE_ITEMS, CREATE_WAREHOUSE, UPDATE_WAREHOUSE, DELETE_WAREHOUSE (5 CRUD)
- VIEW_WAREHOUSE_COST (1 special view)
- IMPORT_ITEMS, EXPORT_ITEMS, DISPOSE_ITEMS (3 transaction types)
- APPROVE_TRANSACTION, CANCEL_WAREHOUSE (2 workflow)
- MANAGE_SUPPLIERS, MANAGE_CONSUMABLES, MANAGE_WAREHOUSE (3 MANAGE overlaps!)

🤔 REALITY CHECK: Phòng khám nhỏ (~5-10 người quản lý kho):
- Inventory Manager: Toàn quyền quản lý (nhập/xuất/thanh lý/duyệt)
- Dentist/Nurse: CHỈ CẦN xem danh sách vật tư để dùng
- Accountant: Xem giá trị kho
- Admin: Toàn quyền hệ thống

✅ SHOULD BE: ~6-8 permissions MAX!
- VIEW_INVENTORY (Everyone): Xem danh sách vật tư/thuốc
- VIEW_INVENTORY_COST (Accountant/Admin): Xem giá trị kho
- MANAGE_INVENTORY_ITEMS (Inventory Manager): CRUD vật tư/danh mục/NCC
- CREATE_INVENTORY_TRANSACTION (Inventory Manager): Tạo phiếu nhập/xuất/thanh lý
- APPROVE_INVENTORY_TRANSACTION (Manager/Admin): Duyệt phiếu kho
- MANAGE_INVENTORY_CONSUMABLES (Inventory Manager): Quản lý định mức tiêu hao
```

#### 3. EMPLOYEE (6 permissions) - **CONFUSING NAMING!**

```
❌ REDUNDANT & CONFUSING:
- VIEW_EMPLOYEE (Xem danh sách nhân viên)
- READ_ALL_EMPLOYEES (Đọc tất cả thông tin nhân viên) ← What's the difference with VIEW?!
- READ_EMPLOYEE_BY_CODE (Đọc thông tin nhân viên theo mã) ← Chỉ khác cách query!
- CREATE_EMPLOYEE, UPDATE_EMPLOYEE, DELETE_EMPLOYEE

🤔 WHY 3 "VIEW/READ" permissions?! Same functionality!

✅ SHOULD BE: 4 permissions
- VIEW_EMPLOYEE (Everyone with permission): Xem danh sách + chi tiết
- CREATE_EMPLOYEE (Admin/Manager): Tạo nhân viên mới
- UPDATE_EMPLOYEE (Admin/Manager): Cập nhật thông tin
- DELETE_EMPLOYEE (Admin only): Xóa/vô hiệu hóa nhân viên
```

#### 4. TREATMENT vs TREATMENT_PLAN - **DUPLICATE MODULES!**

```
❌ CONFUSION: Có 2 modules cho cùng 1 việc!

MODULE: TREATMENT (4 perms):
- VIEW_TREATMENT, CREATE_TREATMENT, UPDATE_TREATMENT, ASSIGN_DOCTOR_TO_ITEM

MODULE: TREATMENT_PLAN (8 perms):
- VIEW_TREATMENT_PLAN_ALL, VIEW_ALL_TREATMENT_PLANS (2 VIEW giống nhau?!)
- VIEW_TREATMENT_PLAN_OWN
- CREATE_TREATMENT_PLAN, UPDATE_TREATMENT_PLAN, DELETE_TREATMENT_PLAN
- APPROVE_TREATMENT_PLAN, MANAGE_PLAN_PRICING

🤔 REALITY: Chỉ có 1 khái niệm "PHÁC ĐỒ ĐIỀU TRỊ" trong phòng khám!

✅ SHOULD MERGE: Keep TREATMENT_PLAN, remove TREATMENT
- VIEW_TREATMENT_PLAN_ALL (Staff): Xem tất cả phác đồ
- VIEW_TREATMENT_PLAN_OWN (Patient): Chỉ xem phác đồ của mình
- MANAGE_TREATMENT_PLAN (Dentist/Manager): Tạo/sửa/xóa phác đồ
- APPROVE_TREATMENT_PLAN (Manager): Duyệt/từ chối phác đồ
- MANAGE_PLAN_PRICING (Accountant/Manager): Điều chỉnh giá/chiết khấu
```

#### 5. APPOINTMENT (8 permissions) - **OVERLY SPECIFIC ACTIONS!**

```
❌ TOO MANY separate actions:
- VIEW_APPOINTMENT_ALL, VIEW_APPOINTMENT_OWN (OK - RBAC)
- CREATE_APPOINTMENT (OK)
- UPDATE_APPOINTMENT (General update)
- UPDATE_APPOINTMENT_STATUS (Specific update!) ← Redundant!
- DELAY_APPOINTMENT (Specific update!) ← Redundant!
- CANCEL_APPOINTMENT (Specific update!) ← Redundant!
- DELETE_APPOINTMENT (Hard delete)

🤔 REALITY: UPDATE_APPOINTMENT_STATUS, DELAY_APPOINTMENT, CANCEL_APPOINTMENT
           đều là UPDATE operations! Tại sao tách ra?!

✅ SHOULD BE: 5 permissions
- VIEW_APPOINTMENT_ALL (Receptionist/Manager): Xem tất cả
- VIEW_APPOINTMENT_OWN (Dentist/Patient): Xem lịch liên quan
- MANAGE_APPOINTMENT (Receptionist/Manager): Tạo/sửa/hủy/hoãn lịch hẹn
- UPDATE_APPOINTMENT_STATUS (Receptionist/Dentist): Check-in, In-progress, Completed
- DELETE_APPOINTMENT (Admin only): Xóa vĩnh viễn (hard delete)
```

#### 6. CUSTOMER_MANAGEMENT (8 permissions) - **OVERKILL FOR SMALL CLINIC!**

```
❌ SEPARATE CRUD for Contact & Contact History:
- VIEW_CONTACT, CREATE_CONTACT, UPDATE_CONTACT, DELETE_CONTACT (4)
- VIEW_CONTACT_HISTORY, CREATE_CONTACT_HISTORY, UPDATE_CONTACT_HISTORY, DELETE_CONTACT_HISTORY (4)

🤔 REALITY: Phòng khám nhỏ (~5-20 liên hệ/ngày):
- Receptionist quản lý liên hệ đơn giản
- Không cần phân quyền chi tiết đến từng operation
- Contact History thường auto-generated, ít khi UPDATE/DELETE

✅ SHOULD BE: 2-3 permissions
- MANAGE_CUSTOMER_CONTACT (Receptionist/Manager): Full CRUD contact + history
- VIEW_CUSTOMER_CONTACT (Other staff): Chỉ xem (nếu cần)
```

#### 7. PATIENT_IMAGES (8 permissions) - **TOO FRAGMENTED!**

```
❌ SEPARATE permissions for Image & Comment:
- PATIENT_IMAGE_CREATE, PATIENT_IMAGE_READ, PATIENT_IMAGE_UPDATE, PATIENT_IMAGE_DELETE (4)
- PATIENT_IMAGE_COMMENT_CREATE, PATIENT_IMAGE_COMMENT_READ, PATIENT_IMAGE_COMMENT_UPDATE, PATIENT_IMAGE_COMMENT_DELETE (4)

🤔 REALITY: Comment là feature phụ của Image, không cần tách riêng!

✅ SHOULD BE: 2 permissions
- MANAGE_PATIENT_IMAGES (Dentist/Nurse): Tạo/sửa/xóa hình ảnh + comment
- VIEW_PATIENT_IMAGES (All staff): Xem hình ảnh + comments
```

#### 8. LEAVE_MANAGEMENT - **MIXING OLD & NEW PERMISSIONS!**

```
❌ DUPLICATE OT (Overtime) permissions:
- VIEW_LEAVE_ALL, VIEW_LEAVE_OWN (For both time-off & overtime)
- VIEW_OT_ALL, VIEW_OT_OWN (Specific for overtime only!) ← WHY?!

🤔 REALITY: VIEW_LEAVE_ALL should cover BOTH time-off & overtime!
           Tại sao lại tạo VIEW_OT_ALL riêng?!

✅ SHOULD BE: Keep VIEW_LEAVE_ALL/OWN, remove VIEW_OT_ALL/OWN
```

### 🟡 **MINOR ISSUES**

#### 9. ACCOUNT (4 permissions) - **RARELY USED!**

```
❌ LOW VALUE: Account = User Authentication (auto-created với Employee/Patient)
- VIEW_ACCOUNT, CREATE_ACCOUNT, UPDATE_ACCOUNT, DELETE_ACCOUNT

🤔 REALITY: Trong phòng khám nhỏ:
- Account creation: Auto khi tạo Employee/Patient
- Account update: Reset password, lock/unlock
- Ít khi cần VIEW_ACCOUNT danh sách riêng

✅ COULD MERGE: Gộp vào EMPLOYEE/PATIENT management
- CREATE_EMPLOYEE → Auto create account
- UPDATE_EMPLOYEE → Can reset password/lock account
- DELETE_EMPLOYEE → Auto disable account
```

#### 10. SYSTEM_CONFIGURATION (12 permissions) - **ADMIN-ONLY MODULE!**

```
✅ OK: 12 permissions for Role/Permission/Specialization management
- Chỉ Admin sử dụng
- Ít thay đổi
- Keep as is (low priority for optimization)
```

---

## PART 3: DENTAL CLINIC REALITY CHECK

### 🏥 **Small-Medium Private Dental Clinic Profile:**

- **Size**: 10-30 employees total
- **Roles**:
  - 1-2 Admins (Owner/IT)
  - 1-2 Managers (Clinic Manager)
  - 3-5 Dentists
  - 2-4 Nurses
  - 1-2 Receptionists
  - 1 Accountant
  - 1 Inventory Manager
  - 0-1 Intern
- **Operations**:
  - 20-50 appointments/day
  - 5-15 new patients/week
  - Simple shift scheduling (not enterprise-level)
  - Basic inventory (not complex warehouse)
  - Focus on PATIENT CARE, not administrative overhead!

### 🎯 **Permission Philosophy for Small Clinic:**

1. **SIMPLICITY > GRANULARITY**: Prefer `MANAGE_X` over `CREATE_X + UPDATE_X + DELETE_X`
2. **ROLE-BASED > TASK-BASED**: Focus on WHO can do, not WHAT specific action
3. **PRACTICAL > THEORETICAL**: If permission rarely used, remove or merge
4. **TRUST > CONTROL**: Small team = more trust, less micromanagement

---

## PART 4: OPTIMIZATION STRATEGY

### ✅ **MERGE RULES:**

#### Rule 1: CRUD Consolidation

```
BEFORE:
- CREATE_X, UPDATE_X, DELETE_X (3 permissions)

AFTER:
- MANAGE_X (1 permission) = Create + Update + Delete
- VIEW_X (1 permission) = Read-only

APPLIES TO:
- EMPLOYEE, PATIENT, ROOM, SERVICE, HOLIDAY, SPECIALIZATION
- CUSTOMER_CONTACT, PATIENT_IMAGES
```

#### Rule 2: View All vs View Own

```
KEEP:
- VIEW_X_ALL (Manager/Admin): Xem tất cả records
- VIEW_X_OWN (Employee/Patient): Chỉ xem của mình

APPLIES TO:
- APPOINTMENT, TREATMENT_PLAN, LEAVE_MANAGEMENT, SCHEDULE_MANAGEMENT
```

#### Rule 3: Workflow Simplification

```
BEFORE:
- UPDATE_X_STATUS, DELAY_X, CANCEL_X, APPROVE_X, REJECT_X (5 separate permissions)

AFTER:
- MANAGE_X_WORKFLOW (1 permission) = All status changes + approvals
OR
- UPDATE_X (1 permission) = All updates including status
- APPROVE_X (1 permission) = Approval workflow only

APPLIES TO:
- APPOINTMENT, WAREHOUSE, TREATMENT_PLAN
```

### ✅ **REMOVAL RULES:**

#### Rule 1: Unused/Redundant Permissions

```
REMOVE:
- READ_ALL_EMPLOYEES, READ_EMPLOYEE_BY_CODE → Merge to VIEW_EMPLOYEE
- VIEW_ALL_TREATMENT_PLANS vs VIEW_TREATMENT_PLAN_ALL → Keep one
- CREATE_WAREHOUSE, UPDATE_WAREHOUSE → Merge to MANAGE_INVENTORY_ITEMS
```

#### Rule 2: Over-Granular Actions

```
REMOVE:
- PATIENT_IMAGE_COMMENT_* (4 perms) → Merge to MANAGE_PATIENT_IMAGES
- DELETE_REGISTRATION_ALL, DELETE_REGISTRATION_OWN → Merge to MANAGE_REGISTRATIONS
- UPDATE_REGISTRATIONS_ALL, UPDATE_REGISTRATION_OWN → Merge to MANAGE_REGISTRATIONS
```

#### Rule 3: Module Duplication

```
REMOVE:
- TREATMENT module (4 perms) → Keep TREATMENT_PLAN only
```

---

## PART 5: OPTIMIZED PERMISSION LIST

### 🎯 **TARGET: ~80-100 permissions** (down from 169)

### Module-by-Module Optimization:

#### ✅ **1. ACCOUNT (4 → 2 permissions)**

```
BEFORE: VIEW_ACCOUNT, CREATE_ACCOUNT, UPDATE_ACCOUNT, DELETE_ACCOUNT
AFTER:
- MANAGE_ACCOUNT (Admin/Manager): Full CRUD accounts
- VIEW_ACCOUNT (Admin/Manager): View account list
```

#### ✅ **2. EMPLOYEE (6 → 4 permissions)**

```
BEFORE: VIEW_EMPLOYEE, READ_ALL_EMPLOYEES, READ_EMPLOYEE_BY_CODE, CREATE_EMPLOYEE, UPDATE_EMPLOYEE, DELETE_EMPLOYEE
AFTER:
- VIEW_EMPLOYEE (Manager/Admin/Receptionist): View employee list + details
- MANAGE_EMPLOYEE (Admin/Manager): Create/Update employees
- DELETE_EMPLOYEE (Admin only): Delete/disable employee
- MANAGE_EMPLOYEE_SCHEDULE (Manager): Assign shifts to employees
```

#### ✅ **3. PATIENT (4 → 3 permissions)**

```
BEFORE: VIEW_PATIENT, CREATE_PATIENT, UPDATE_PATIENT, DELETE_PATIENT
AFTER:
- VIEW_PATIENT (All staff): View patient records
- MANAGE_PATIENT (Receptionist/Dentist/Manager): Create/Update patient info
- DELETE_PATIENT (Admin only): Delete patient (rare operation)
```

#### ✅ **4. TREATMENT → REMOVED** (merge to TREATMENT_PLAN)

#### ✅ **5. APPOINTMENT (8 → 5 permissions)**

```
BEFORE: VIEW_APPOINTMENT_ALL, VIEW_APPOINTMENT_OWN, CREATE_APPOINTMENT, UPDATE_APPOINTMENT,
        UPDATE_APPOINTMENT_STATUS, DELAY_APPOINTMENT, CANCEL_APPOINTMENT, DELETE_APPOINTMENT
AFTER:
- VIEW_APPOINTMENT_ALL (Receptionist/Manager): View all appointments
- VIEW_APPOINTMENT_OWN (Dentist/Nurse/Patient): View related appointments
- MANAGE_APPOINTMENT (Receptionist/Manager): Create/Update/Cancel/Delay appointments
- UPDATE_APPOINTMENT_STATUS (Receptionist/Dentist/Nurse): Check-in, Start, Complete
- DELETE_APPOINTMENT (Admin only): Hard delete
```

#### ✅ **6. CUSTOMER_MANAGEMENT (8 → 2 permissions)**

```
BEFORE: 4 for CONTACT + 4 for CONTACT_HISTORY
AFTER:
- MANAGE_CUSTOMER_CONTACT (Receptionist/Manager): Full CRUD contact + history
- VIEW_CUSTOMER_CONTACT (Other staff): View only
```

#### ✅ **7. SCHEDULE_MANAGEMENT (27 → 10 permissions)** ⭐ **MAJOR REDUCTION!**

```
BEFORE: 27 fragmented permissions across work shifts, registrations, renewals, fixed shifts
AFTER:
- VIEW_SCHEDULE_ALL (Manager): View all employee schedules
- VIEW_SCHEDULE_OWN (Employee): View own schedule
- MANAGE_WORK_SHIFTS (Admin/Manager): Create/Update/Delete shift templates
- MANAGE_EMPLOYEE_SHIFTS (Manager): Assign/Remove shifts to employees
- CREATE_SHIFT_REGISTRATION (Employee): Register for available shifts (part-time)
- CANCEL_SHIFT_REGISTRATION (Employee): Cancel own registration
- APPROVE_SHIFT_REGISTRATION (Manager): Approve/Reject shift registrations
- MANAGE_FIXED_SHIFTS (Manager): Setup recurring shifts for full-time employees
- VIEW_SCHEDULE_SUMMARY (Manager): View schedule statistics
- MANAGE_PART_TIME_SLOTS (Manager): Create/Manage part-time work slots
```

#### ✅ **8. LEAVE_MANAGEMENT (14 → 10 permissions)**

```
BEFORE: VIEW_LEAVE_ALL, VIEW_LEAVE_OWN, VIEW_OT_ALL, VIEW_OT_OWN, + 10 actions
AFTER:
- VIEW_LEAVE_ALL (Manager): View all time-off & overtime requests
- VIEW_LEAVE_OWN (Employee): View own requests
- CREATE_TIME_OFF (Employee): Request time-off
- APPROVE_TIME_OFF (Manager): Approve/Reject time-off
- CANCEL_TIME_OFF (Employee): Cancel pending time-off
- CREATE_OVERTIME (Employee): Request overtime
- APPROVE_OVERTIME (Manager): Approve/Reject overtime
- CANCEL_OVERTIME (Employee): Cancel pending overtime
- MANAGE_LEAVE_TYPE (Admin/Manager): Manage leave type categories
- MANAGE_LEAVE_BALANCE (Admin/Manager): Adjust employee leave balances
```

#### ✅ **9. SYSTEM_CONFIGURATION (12 → 8 permissions)**

```
BEFORE: 4 for ROLE + 4 for PERMISSION + 4 for SPECIALIZATION
AFTER:
- VIEW_SYSTEM_CONFIG (Admin): View roles/permissions/specializations
- MANAGE_ROLE (Admin): Create/Update/Delete roles
- MANAGE_PERMISSION (Admin): Create/Update/Delete permissions
- MANAGE_SPECIALIZATION (Admin): Create/Update/Delete specializations
- ASSIGN_ROLE_PERMISSIONS (Admin): Assign permissions to roles
- ASSIGN_USER_ROLE (Admin/Manager): Assign role to user
- VIEW_ROLE (Manager): View role list (for assignment)
- VIEW_SPECIALIZATION (All staff): View specialization list
```

#### ✅ **10. HOLIDAY (4 → 2 permissions)**

```
BEFORE: VIEW_HOLIDAY, CREATE_HOLIDAY, UPDATE_HOLIDAY, DELETE_HOLIDAY
AFTER:
- VIEW_HOLIDAY (All staff): View holiday list
- MANAGE_HOLIDAY (Admin/Manager): Create/Update/Delete holidays
```

#### ✅ **11. ROOM_MANAGEMENT (5 → 3 permissions)**

```
BEFORE: VIEW_ROOM, CREATE_ROOM, UPDATE_ROOM, DELETE_ROOM, UPDATE_ROOM_SERVICES
AFTER:
- VIEW_ROOM (All staff): View room/chair list + assigned services
- MANAGE_ROOM (Admin/Manager): Create/Update/Delete rooms
- ASSIGN_ROOM_SERVICES (Manager): Link services to rooms
```

#### ✅ **12. SERVICE_MANAGEMENT (4 → 2 permissions)**

```
BEFORE: VIEW_SERVICE, CREATE_SERVICE, UPDATE_SERVICE, DELETE_SERVICE
AFTER:
- VIEW_SERVICE (All users): View service list + details
- MANAGE_SERVICE (Admin/Manager): Create/Update/Delete services
```

#### ✅ **13. TREATMENT_PLAN (8 → 6 permissions)**

```
BEFORE: VIEW_TREATMENT_PLAN_ALL, VIEW_ALL_TREATMENT_PLANS (duplicate!), VIEW_TREATMENT_PLAN_OWN,
        CREATE_TREATMENT_PLAN, UPDATE_TREATMENT_PLAN, DELETE_TREATMENT_PLAN,
        APPROVE_TREATMENT_PLAN, MANAGE_PLAN_PRICING
AFTER:
- VIEW_TREATMENT_PLAN_ALL (Dentist/Receptionist/Manager): View all patient plans
- VIEW_TREATMENT_PLAN_OWN (Patient): View own treatment plan
- MANAGE_TREATMENT_PLAN (Dentist/Manager): Create/Update/Delete treatment plans
- APPROVE_TREATMENT_PLAN (Manager): Approve/Reject plans requiring approval
- MANAGE_PLAN_PRICING (Accountant/Manager): Adjust pricing/discounts
- VIEW_TREATMENT_PLAN_SUMMARY (Manager): Statistics & reports
```

#### ✅ **14. WAREHOUSE (19 → 8 permissions)** ⭐ **MAJOR REDUCTION!**

```
BEFORE: 19 granular permissions for items, transactions, suppliers, consumables
AFTER:
- VIEW_INVENTORY (All staff): View items/medicines list
- VIEW_INVENTORY_COST (Accountant/Admin): View cost/value data
- MANAGE_INVENTORY_ITEMS (Inventory Manager): Create/Update/Delete items + categories + suppliers
- CREATE_INVENTORY_TRANSACTION (Inventory Manager): Create import/export/dispose transactions
- APPROVE_INVENTORY_TRANSACTION (Manager/Admin): Approve/Reject transactions
- CANCEL_INVENTORY_TRANSACTION (Inventory Manager): Cancel pending transactions
- MANAGE_CONSUMABLES (Inventory Manager): Manage item consumption rates (BOM)
- VIEW_INVENTORY_REPORTS (Manager/Accountant): View inventory reports
```

#### ✅ **15. PATIENT_IMAGES (8 → 3 permissions)**

```
BEFORE: 4 for IMAGE + 4 for COMMENT
AFTER:
- VIEW_PATIENT_IMAGES (All staff): View patient images + comments
- MANAGE_PATIENT_IMAGES (Dentist/Nurse): Upload/Update/Delete images + Add/Edit/Delete comments
- DELETE_PATIENT_IMAGES (Admin/Uploader): Delete images permanently
```

#### ✅ **16. NOTIFICATION (3 → 3 permissions)** ✅ **KEEP AS IS**

```
OK: VIEW_NOTIFICATION, DELETE_NOTIFICATION, MANAGE_NOTIFICATION (Admin only)
```

#### ✅ **17. CLINICAL_RECORDS (5 → 4 permissions)**

```
BEFORE: WRITE_CLINICAL_RECORD, UPLOAD_ATTACHMENT, VIEW_ATTACHMENT, DELETE_ATTACHMENT, VIEW_VITAL_SIGNS_REFERENCE
AFTER:
- VIEW_CLINICAL_RECORD (All staff): View clinical records + attachments + vital signs
- WRITE_CLINICAL_RECORD (Dentist/Nurse): Create/Update clinical records
- MANAGE_CLINICAL_ATTACHMENTS (Dentist/Nurse): Upload/Delete X-rays, images, PDFs
- VIEW_VITAL_SIGNS_REFERENCE (All staff): View vital signs reference table
```

---

## PART 6: SUMMARY

### 📊 **Optimization Results:**

| Module                  | Before  | After      | Reduction       |
| ----------------------- | ------- | ---------- | --------------- |
| ACCOUNT                 | 4       | 2          | -50%            |
| EMPLOYEE                | 6       | 4          | -33%            |
| PATIENT                 | 4       | 3          | -25%            |
| TREATMENT               | 4       | 0 (merged) | -100%           |
| APPOINTMENT             | 8       | 5          | -37%            |
| CUSTOMER_MANAGEMENT     | 8       | 2          | -75% ⭐         |
| **SCHEDULE_MANAGEMENT** | **27**  | **10**     | **-63% ⭐⭐⭐** |
| LEAVE_MANAGEMENT        | 14      | 10         | -29%            |
| SYSTEM_CONFIGURATION    | 12      | 8          | -33%            |
| HOLIDAY                 | 4       | 2          | -50%            |
| ROOM_MANAGEMENT         | 5       | 3          | -40%            |
| SERVICE_MANAGEMENT      | 4       | 2          | -50%            |
| TREATMENT_PLAN          | 8       | 6          | -25%            |
| **WAREHOUSE**           | **19**  | **8**      | **-58% ⭐⭐⭐** |
| PATIENT_IMAGES          | 8       | 3          | -62% ⭐         |
| NOTIFICATION            | 3       | 3          | 0% ✅           |
| CLINICAL_RECORDS        | 5       | 4          | -20%            |
| **TOTAL**               | **169** | **87**     | **-49%** 🎉     |

### 🎯 **Key Achievements:**

1. **Reduced from 169 → 87 permissions** (49% reduction!)
2. **Simplified SCHEDULE_MANAGEMENT** from 27 → 10 (most complex module)
3. **Consolidated WAREHOUSE** from 19 → 8 (over-engineered)
4. **Merged TREATMENT into TREATMENT_PLAN** (removed duplicate module)
5. **Applied consistent RBAC pattern** (VIEW_ALL vs VIEW_OWN)
6. **Aligned with small clinic reality** (trust > control, simplicity > granularity)

---

## NEXT STEPS:

1. ✅ Create new optimized `dental-clinic-seed-data-optimized.sql`
2. ✅ Update ALL controllers to use new permission names
3. ✅ Update `AuthoritiesConstants.java` with new constants
4. ✅ Create migration guide for existing deployments
5. ✅ Test all APIs with new permission system
6. ✅ Document RBAC logic for each role

**Status**: ✅ **ANALYSIS COMPLETE - READY FOR IMPLEMENTATION**
