# Appointment Medical Staff Filter - Visual Guide

## 🎯 Logic Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     APPOINTMENT VALIDATION                       │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │ Check Employee         │
                    │ Has specialization     │
                    │ ID = 8 (STANDARD)?     │
                    └────────────────────────┘
                                 │
                ┌────────────────┴────────────────┐
                │                                 │
                ▼                                 ▼
         ┌─────────────┐                  ┌─────────────┐
         │   HAS ID 8  │                  │ NO ID 8     │
         │             │                  │             │
         │     ✅      │                  │     ❌      │
         └─────────────┘                  └─────────────┘
                │                                 │
                ▼                                 ▼
    ┌───────────────────────┐          ┌────────────────────────┐
    │ Medical Staff         │          │ Non-Medical Staff      │
    │                       │          │                        │
    │ • Doctors             │          │ • Admin                │
    │ • Nurses              │          │ • Receptionist         │
    │ • Medical Interns     │          │ • Receptionist Intern  │
    │   (ID 8 + ID 9)       │          │   (only ID 9)          │
    └───────────────────────┘          └────────────────────────┘
                │                                 │
                ▼                                 ▼
    ┌───────────────────────┐          ┌────────────────────────┐
    │ CAN participate in    │          │ CANNOT participate     │
    │ appointments          │          │ in appointments        │
    │                       │          │                        │
    │ ✅ Doctor role        │          │ ❌ REJECT              │
    │ ✅ Participant role   │          │                        │
    │ ✅ Available times    │          │ Error:                 │
    │                       │          │ EMPLOYEE_NOT_          │
    │                       │          │ MEDICAL_STAFF          │
    └───────────────────────┘          └────────────────────────┘
```

## 📊 Employee Types Matrix

```
┌──────────────────────┬─────────────────┬──────────────┬────────────────┐
│ Employee Type        │ Specializations │ Has ID 8?    │ Can Join Appt? │
├──────────────────────┼─────────────────┼──────────────┼────────────────┤
│ Doctor (Specialist)  │ [1, 7, 8]       │ ✅ YES       │ ✅ YES         │
│ Doctor (General)     │ [8]             │ ✅ YES       │ ✅ YES         │
│ Nurse                │ [8]             │ ✅ YES       │ ✅ YES         │
│ Medical Assistant    │ [8]             │ ✅ YES       │ ✅ YES         │
│ Medical Intern       │ [8, 9]          │ ✅ YES       │ ✅ YES         │
├──────────────────────┼─────────────────┼──────────────┼────────────────┤
│ Admin                │ []              │ ❌ NO        │ ❌ NO          │
│ Receptionist         │ []              │ ❌ NO        │ ❌ NO          │
│ Receptionist Intern  │ [9]             │ ❌ NO        │ ❌ NO          │
│ Hypothetical: Only   │ [1]             │ ❌ NO        │ ❌ NO          │
│ specialist (no base) │                 │              │                │
└──────────────────────┴─────────────────┴──────────────┴────────────────┘
```

## 🔍 Specialization ID Meanings

```
┌────────┬──────────────────┬────────────────────────────────────────────┐
│ ID     │ Code             │ Purpose                                    │
├────────┼──────────────────┼────────────────────────────────────────────┤
│ 1-7    │ SPEC-ORTHO, etc  │ Specific medical specialties               │
│        │                  │ Optional - can be combined with ID 8       │
│        │                  │ CANNOT replace ID 8                        │
├────────┼──────────────────┼────────────────────────────────────────────┤
│ 8      │ SPEC-STANDARD    │ ⭐ BASELINE for ALL medical staff         │
│        │                  │ REQUIRED to participate in appointments    │
│        │                  │ Medical staff definition = HAS ID 8        │
├────────┼──────────────────┼────────────────────────────────────────────┤
│ 9      │ SPEC-INTERN      │ MARKER for trainees (medical or admin)     │
│        │                  │ NOT related to appointment validation      │
│        │                  │ Medical intern = ID 8 + ID 9               │
│        │                  │ Admin intern = only ID 9 (no ID 8)         │
└────────┴──────────────────┴────────────────────────────────────────────┘
```

## 🎓 Intern Types Comparison

```
Medical Intern (Y tế)              vs        Admin Intern (Lễ tân)
────────────────────────────────────────────────────────────────────
Specializations: [8, 9]                      Specializations: [9]

Has ID 8 (STANDARD): ✅ YES                  Has ID 8 (STANDARD): ❌ NO
Has ID 9 (INTERN): ✅ YES                    Has ID 9 (INTERN): ✅ YES

Can join appointment: ✅ YES                 Can join appointment: ❌ NO
  → ID 8 present                               → ID 8 missing

Use case:                                    Use case:
- Learn medical procedures                   - Learn admin/reception tasks
- Assist in appointments                     - Cannot participate in clinical work
- Training to become doctor/nurse            - Training for front desk role
```

## 📝 Code Validation Flow

```java
// STEP 1: Fetch employee
Employee employee = employeeRepository.findByEmployeeCode("EMP002");

// STEP 2: Check specializations
List<Specialization> specializations = employee.getSpecializations();
// Example: [
//   { id: 1, name: "Chỉnh nha" },
//   { id: 7, name: "Răng thẩm mỹ" },
//   { id: 8, name: "Y tế cơ bản" }
// ]

// STEP 3: Validate STANDARD (ID 8) specifically
boolean hasStandard = specializations.stream()
        .anyMatch(spec -> spec.getSpecializationId() == 8);

// STEP 4: Decision
if (hasStandard) {
    // ✅ Employee CAN be assigned to appointment
    return employee;
} else {
    // ❌ REJECT with error
    throw new BadRequestAlertException(
        "Employee must have STANDARD specialization (ID 8)",
        "EMPLOYEE_NOT_MEDICAL_STAFF"
    );
}
```

## 🧪 Test Scenarios Visual

### Scenario 1: Doctor with Specialties

```
Employee: EMP002 - Dr. Tâm Nguyễn Thị
Specializations: [1, 7, 8]

Validation:
  [1] → Ortho ← Not checked
  [7] → Aesthetic ← Not checked
  [8] → STANDARD ← ✅ FOUND!

Result: ✅ PASS - Can be doctor
```

### Scenario 2: Admin

```
Employee: EMP001 - Admin User
Specializations: []

Validation:
  [] → Empty list
  No ID 8 found ← ❌ MISSING!

Result: ❌ FAIL - Cannot be doctor
Error: "Employee must have STANDARD specialization (ID 8)"
```

### Scenario 3: Medical Intern

```
Employee: EMP010 - Medical Intern
Specializations: [8, 9]

Validation:
  [8] → STANDARD ← ✅ FOUND!
  [9] → INTERN ← Ignored (not relevant)

Result: ✅ PASS - Can be participant
```

### Scenario 4: Admin Intern

```
Employee: EMP999 - Reception Intern
Specializations: [9]

Validation:
  [9] → INTERN ← Not ID 8
  No ID 8 found ← ❌ MISSING!

Result: ❌ FAIL - Cannot be participant
Error: "Employee must have STANDARD specialization (ID 8)"
```

## 🏗️ Database Structure

```sql
-- ============================================
-- SPECIALIZATIONS TABLE
-- ============================================
specialization_id | specialization_code | specialization_name
──────────────────┼────────────────────┼────────────────────
1                 | SPEC-ORTHO         | Chỉnh nha
2                 | SPEC-ENDO          | Nội nha
...
7                 | SPEC-AESTHETIC     | Răng thẩm mỹ
8                 | SPEC-STANDARD      | Y tế cơ bản ⭐ KEY
9                 | SPEC-INTERN        | Thực tập sinh

-- ============================================
-- EMPLOYEE_SPECIALIZATIONS TABLE
-- ============================================
employee_id | specialization_id | Meaning
────────────┼──────────────────┼─────────────────────────────
2           | 1                 | Dr. Tâm has Ortho specialty
2           | 7                 | Dr. Tâm has Aesthetic specialty
2           | 8                 | Dr. Tâm is medical staff ⭐
────────────┼──────────────────┼─────────────────────────────
4           | 8                 | Nurse Mai is medical staff ⭐
────────────┼──────────────────┼─────────────────────────────
10          | 8                 | Medical intern is medical staff ⭐
10          | 9                 | Medical intern is trainee
────────────┼──────────────────┼─────────────────────────────
1           | (no rows)         | Admin - NOT medical staff ❌
────────────┼──────────────────┼─────────────────────────────
999         | 9                 | Admin intern - is trainee
            |                   | Admin intern - NOT medical staff ❌
```

## 🚀 API Request/Response Flow

### Request: Create Appointment with Medical Staff

```http
POST /api/v1/appointments
{
  "employeeCode": "EMP002",
  "serviceCodes": ["GEN_EXAM"]
}
```

**Backend Processing**:

```
1. validateDoctor("EMP002")
   ├─ Find employee: ✅ Found
   ├─ Get specializations: [1, 7, 8]
   ├─ Check anyMatch(id == 8): ✅ TRUE
   └─ Return: Employee object

2. Proceed with appointment creation
   └─ Response: 201 CREATED
```

### Request: Create Appointment with Admin

```http
POST /api/v1/appointments
{
  "employeeCode": "EMP001",
  "serviceCodes": ["GEN_EXAM"]
}
```

**Backend Processing**:

```
1. validateDoctor("EMP001")
   ├─ Find employee: ✅ Found
   ├─ Get specializations: []
   ├─ Check anyMatch(id == 8): ❌ FALSE
   └─ Throw: BadRequestAlertException

2. Return error
   └─ Response: 400 BAD REQUEST
      {
        "error": "EMPLOYEE_NOT_MEDICAL_STAFF",
        "message": "Employee must have STANDARD specialization (ID 8)"
      }
```

## 📋 Frontend Implementation Guide

### Step 1: Load Medical Staff List

```javascript
// OLD way (shows everyone)
const employees = await fetch("/api/v1/employees?isActive=true");

// NEW way (only medical staff with ID 8)
const medicalStaff = await fetch("/api/v1/employees/medical-staff");
```

### Step 2: Display in Dropdown

```javascript
// Example response from /medical-staff
[
  {
    employeeCode: "EMP002",
    fullName: "Tâm Nguyễn Thị",
    specializations: [
      { id: 1, name: "Chỉnh nha" },
      { id: 7, name: "Răng thẩm mỹ" },
      { id: 8, name: "Y tế cơ bản" } ← Has ID 8 ✅
    ]
  },
  {
    employeeCode: "EMP004",
    fullName: "Mai Lê Thị",
    specializations: [
      { id: 8, name: "Y tế cơ bản" } ← Has ID 8 ✅
    ]
  }
  // EMP001 (Admin) NOT included ❌
]

// Display in dropdown
<select name="doctor">
  <option value="EMP002">Dr. Tâm Nguyễn Thị - Chỉnh nha, Răng thẩm mỹ</option>
  <option value="EMP004">Y tá Mai Lê Thị</option>
  <!-- Admin NOT shown -->
</select>
```

### Step 3: Handle Validation Errors

```javascript
try {
  await createAppointment({
    employeeCode: selectedDoctor,
    serviceCodes: selectedServices,
  });
} catch (error) {
  if (error.error === "EMPLOYEE_NOT_MEDICAL_STAFF") {
    showError("Chỉ nhân viên y tế (có chuyên môn cơ bản) mới có thể được chọn");
  }
}
```

## 🎯 Key Takeaways

```
┌─────────────────────────────────────────────────────────────────┐
│                      GOLDEN RULES                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. Medical Staff = Has STANDARD (ID 8)                         │
│     → Can participate in appointments                            │
│                                                                  │
│  2. Admin/Receptionist = No ID 8                                │
│     → CANNOT participate in appointments                         │
│                                                                  │
│  3. INTERN (ID 9) is just a MARKER                              │
│     → NOT used for appointment validation                        │
│                                                                  │
│  4. Specific specialties (IDs 1-7) are ADDITIONAL               │
│     → CANNOT replace ID 8                                        │
│                                                                  │
│  5. When creating medical employee                              │
│     → ALWAYS add ID 8 (STANDARD)                                │
│     → Optionally add IDs 1-7 (specialties) or ID 9 (intern)     │
│                                                                  │
│  6. Code checks ONLY: spec.specializationId == 8                │
│     → Hardcoded, specific, no ambiguity                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```
