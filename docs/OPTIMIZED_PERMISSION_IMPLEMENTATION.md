# OPTIMIZED PERMISSION IMPLEMENTATION PLAN

**Based on actual usage analysis of 47 controllers**

## REALITY CHECK:

- **Current**: 169 permissions defined in seed data
- **Actually Used**: Only 44 permissions in controllers (26%)
- **UNUSED**: 125 permissions (74% waste!)

## IMPLEMENTATION STRATEGY:

### Phase 1: Keep What's Actually Used ✅

Start with the 44 permissions that are ACTUALLY in controllers:

#### Most Used (Keep As-Is):

1. VIEW_WAREHOUSE (22 usages)
2. WRITE_CLINICAL_RECORD (9 usages)
3. MANAGE_PART_TIME_REGISTRATIONS (9 usages)
4. VIEW_HOLIDAY (8 usages)
5. MANAGE_WAREHOUSE (8 usages)
6. MANAGE_NOTIFICATION (7 usages)
7. CREATE_APPOINTMENT (6 usages)
8. VIEW_SERVICE (5 usages)
9. VIEW_NOTIFICATION (5 usages)
10. VIEW_APPOINTMENT_OWN (5 usages)
11. VIEW_APPOINTMENT_ALL (5 usages)

#### Moderate Use (Review for Consolidation):

- PATIENT_IMAGE_READ (4) → Keep separate (RBAC)
- MANAGE_SUPPLIERS (4) → Can merge to MANAGE_WAREHOUSE?
- VIEW_ITEMS (3) → Keep separate (Dentists need this)

#### Single Use (Candidates for Merging):

These 30 permissions are used ONLY ONCE - prime candidates for consolidation:

- CREATE_HOLIDAY, UPDATE_HOLIDAY, DELETE_HOLIDAY → **MANAGE_HOLIDAY**
- CREATE_SERVICE, UPDATE_SERVICE, DELETE_SERVICE → **MANAGE_SERVICE**
- CREATE_WAREHOUSE, UPDATE_WAREHOUSE, DELETE_WAREHOUSE → **MANAGE_WAREHOUSE** (already exists!)
- CREATE_ITEMS, UPDATE_ITEMS → **MANAGE_ITEMS**
- IMPORT_ITEMS, EXPORT_ITEMS, DISPOSE_ITEMS → **MANAGE_INVENTORY_TRANSACTIONS**
- PATIENT_IMAGE_CREATE, PATIENT_IMAGE_UPDATE, PATIENT_IMAGE_DELETE → **MANAGE_PATIENT_IMAGES**
- UPLOAD_ATTACHMENT, DELETE_ATTACHMENT → **MANAGE_ATTACHMENTS**

### Phase 2: Add Missing RBAC Patterns

Some modules need VIEW_ALL vs VIEW_OWN but don't have them yet:

**NEED TO ADD**:

- VIEW_SCHEDULE_ALL, VIEW_SCHEDULE_OWN (for shift management)
- VIEW_LEAVE_ALL, VIEW_LEAVE_OWN (for time-off requests)
- VIEW_PATIENT_IMAGES_ALL, VIEW_PATIENT_IMAGES_OWN (for patient privacy)

### Phase 3: Final Optimized List (~60 permissions)

#### MODULE 1: APPOINTMENT (5 perms)

- VIEW_APPOINTMENT_ALL ✅ (used)
- VIEW_APPOINTMENT_OWN ✅ (used)
- CREATE_APPOINTMENT ✅ (used)
- UPDATE_APPOINTMENT_STATUS ✅ (used)
- MANAGE_APPOINTMENT (merge: DELAY_APPOINTMENT)

#### MODULE 2: CLINICAL_RECORDS (4 perms)

- WRITE_CLINICAL_RECORD ✅ (used)
- VIEW_VITAL_SIGNS_REFERENCE ✅ (used)
- VIEW_ATTACHMENT ✅ (used)
- MANAGE_ATTACHMENTS (merge: UPLOAD_ATTACHMENT, DELETE_ATTACHMENT)

#### MODULE 3: PATIENT_IMAGES (3 perms)

- PATIENT_IMAGE_READ ✅ (used)
- MANAGE_PATIENT_IMAGES (merge: CREATE, UPDATE, DELETE)
- PATIENT_IMAGE_COMMENT (for adding comments)

#### MODULE 4: NOTIFICATION (3 perms) ✅

- VIEW_NOTIFICATION ✅ (used)
- DELETE_NOTIFICATION ✅ (used)
- MANAGE_NOTIFICATION ✅ (used - Admin only)

#### MODULE 5: HOLIDAY (2 perms)

- VIEW_HOLIDAY ✅ (used)
- MANAGE_HOLIDAY (merge: CREATE, UPDATE, DELETE)

#### MODULE 6: SERVICE (2 perms)

- VIEW_SERVICE ✅ (used)
- MANAGE_SERVICE (merge: CREATE, UPDATE, DELETE)

#### MODULE 7: WAREHOUSE (10 perms)

- VIEW_WAREHOUSE ✅ (used 22 times!)
- VIEW_ITEMS ✅ (used)
- VIEW_MEDICINES (keep - for prescription)
- MANAGE_WAREHOUSE ✅ (used)
- MANAGE_ITEMS (merge: CREATE_ITEMS, UPDATE_ITEMS)
- MANAGE_SUPPLIERS ✅ (used)
- IMPORT_ITEMS ✅ (used)
- EXPORT_ITEMS ✅ (used)
- DISPOSE_ITEMS ✅ (used)
- APPROVE_TRANSACTION ✅ (used)

#### MODULE 8: SCHEDULE (6 perms)

- VIEW_SCHEDULE_ALL (new - for managers)
- VIEW_SCHEDULE_OWN (new - for employees)
- MANAGE_WORK_SHIFTS (for shift templates)
- MANAGE_WORK_SLOTS ✅ (used)
- MANAGE_PART_TIME_REGISTRATIONS ✅ (used 9 times!)
- MANAGE_FIXED_REGISTRATIONS ✅ (used)

#### MODULE 9: LEAVE_MANAGEMENT (6 perms)

- VIEW_LEAVE_ALL (new)
- VIEW_LEAVE_OWN (new)
- CREATE_TIME_OFF (for employees)
- APPROVE_TIME_OFF (for managers)
- CREATE_OVERTIME (for employees)
- APPROVE_OVERTIME (for managers)

#### MODULE 10: OTHERS (Keep minimal)

- VIEW_EMPLOYEE, MANAGE_EMPLOYEE, DELETE_EMPLOYEE
- VIEW_PATIENT, MANAGE_PATIENT, DELETE_PATIENT
- VIEW_ROOM, MANAGE_ROOM
- VIEW_TREATMENT_PLAN_ALL, VIEW_TREATMENT_PLAN_OWN, MANAGE_TREATMENT_PLAN
- VIEW_ROLE, MANAGE_ROLE
- VIEW_PERMISSION, MANAGE_PERMISSION
- VIEW_SPECIALIZATION, MANAGE_SPECIALIZATION

### TOTAL OPTIMIZED: ~60 permissions (down from 169)

## IMPLEMENTATION STEPS:

1. ✅ Create this implementation plan
2. ⏳ Create new `dental-clinic-seed-data-optimized.sql`
3. ⏳ Update `AuthoritiesConstants.java`
4. ⏳ Update ALL controllers to use new permissions
5. ⏳ Test app startup + API functionality
6. ⏳ Commit and push to branch

---

**Decision**: Go with PRACTICAL approach - keep what's used, consolidate CRUD operations, add necessary RBAC patterns.
