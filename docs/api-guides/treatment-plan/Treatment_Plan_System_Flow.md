# Treatment Plan Module - System Flow & Architecture

## Table of Contents

1. [API Request Flow](#api-request-flow)
2. [RBAC Flow](#rbac-flow)
3. [Data Flow - API 5.2](#data-flow---api-52)
4. [Database Query Flow](#database-query-flow)
5. [DTO Transformation Flow](#dto-transformation-flow)
6. [Error Handling Flow](#error-handling-flow)
7. [Complete Architecture Diagram](#complete-architecture-diagram)

---

## API Request Flow

### High-Level Request Flow

```
┌─────────────┐
│   Client    │
│ (Browser/   │
│  Mobile)    │
└──────┬──────┘
       │ HTTP Request
       │ GET /api/v1/patients/{patientCode}/treatment-plans/{planCode}
       │ Header: Authorization: Bearer {JWT}
       ↓
┌─────────────────────────────────────────────────────────┐
│              Spring Boot Application                    │
│                                                         │
│  ┌────────────────────────────────────────────────┐    │
│  │  Security Filter Chain                         │    │
│  │  - JWT Validation                              │    │
│  │  - Extract account_id from token               │    │
│  │  - Set Authentication in SecurityContext       │    │
│  └────────────────┬───────────────────────────────┘    │
│                   │                                     │
│                   ↓                                     │
│  ┌────────────────────────────────────────────────┐    │
│  │  Controller Layer                              │    │
│  │  TreatmentPlanController                       │    │
│  │                                                 │    │
│  │  @GetMapping("/{planCode}")                    │    │
│  │  @PreAuthorize("hasAnyAuthority(...)")         │    │
│  └────────────────┬───────────────────────────────┘    │
│                   │                                     │
│                   ↓                                     │
│  ┌────────────────────────────────────────────────┐    │
│  │  Service Layer                                 │    │
│  │  TreatmentPlanDetailService                    │    │
│  │                                                 │    │
│  │  1. verifyPatientAccessPermission()            │    │
│  │     - Find patient by code                     │    │
│  │     - Check VIEW_ALL or VIEW_OWN               │    │
│  │     - Verify account_id if VIEW_OWN            │    │
│  │                                                 │    │
│  │  2. Repository.findDetail...()                 │    │
│  │     - Single JPQL query                        │    │
│  │                                                 │    │
│  │  3. buildNestedResponse()                      │    │
│  │     - Group flat DTOs (O(n))                   │    │
│  │                                                 │    │
│  │  4. calculateProgressSummary()                 │    │
│  │     - Count phases/items                       │    │
│  └────────────────┬───────────────────────────────┘    │
│                   │                                     │
│                   ↓                                     │
│  ┌────────────────────────────────────────────────┐    │
│  │  Repository Layer                              │    │
│  │  PatientTreatmentPlanRepository                │    │
│  │                                                 │    │
│  │  @Query with Constructor Expression            │    │
│  │  - JOIN 5 tables                               │    │
│  │  - Return List<TreatmentPlanDetailDTO>        │    │
│  └────────────────┬───────────────────────────────┘    │
│                   │                                     │
│                   ↓                                     │
│  ┌────────────────────────────────────────────────┐    │
│  │  JPA / Hibernate                               │    │
│  │  - Execute SQL query                           │    │
│  │  - Map ResultSet to DTOs                       │    │
│  └────────────────┬───────────────────────────────┘    │
│                   │                                     │
└───────────────────┼─────────────────────────────────────┘
                    │
                    ↓
          ┌─────────────────┐
          │   PostgreSQL    │
          │    Database     │
          │                 │
          │  - patients     │
          │  - patient_     │
          │    treatment_   │
          │    plans        │
          │  - patient_plan_│
          │    phases       │
          │  - patient_plan_│
          │    items        │
          │  - appointments │
          │  - appointment_ │
          │    plan_items   │
          └─────────────────┘
```

---

## RBAC Flow

### Permission Verification Flow

```
┌──────────────────────────────────────────────────────────┐
│  Service: verifyPatientAccessPermission()                │
└──────────────────────────────────────────────────────────┘
                       │
                       ↓
         ┌─────────────────────────────┐
         │  Find Patient by Code       │
         │  patientRepository.find...  │
         └─────────────┬───────────────┘
                       │
            ┌──────────┴──────────┐
            │ Patient found?      │
            └──────────┬──────────┘
                       │
         ┌─────────────┴─────────────┐
         │                           │
        YES                         NO
         │                           │
         ↓                           ↓
   ┌─────────────┐          ┌──────────────────┐
   │ Continue    │          │ 404 NOT FOUND    │
   └──────┬──────┘          │ "Patient not     │
          │                 │  found with      │
          ↓                 │  code: XX"       │
 ┌────────────────────┐    └──────────────────┘
 │ Get Authentication │
 │ from SecurityCtx   │
 └────────┬───────────┘
          │
          ↓
 ┌─────────────────────────────────────────┐
 │ Check: Has VIEW_TREATMENT_PLAN_ALL?     │
 └────────┬────────────────────────────────┘
          │
    ┌─────┴─────┐
    │           │
   YES         NO
    │           │
    ↓           ↓
┌─────────┐  ┌─────────────────────────────────────┐
│ GRANTED │  │ Check: Has VIEW_TREATMENT_PLAN_OWN? │
│ (Staff) │  └────────┬────────────────────────────┘
└─────────┘           │
                ┌─────┴─────┐
                │           │
               YES         NO
                │           │
                ↓           ↓
     ┌──────────────────┐  ┌───────────────────┐
     │ Extract          │  │ 403 FORBIDDEN     │
     │ account_id       │  │ "No permission"   │
     │ from JWT token   │  └───────────────────┘
     └────────┬─────────┘
              │
              ↓
     ┌────────────────────────────────┐
     │ Get patient.account_id         │
     └────────┬───────────────────────┘
              │
              ↓
     ┌─────────────────────────────────────┐
     │ Compare: token.account_id ==        │
     │          patient.account_id ?       │
     └────────┬────────────────────────────┘
              │
        ┌─────┴─────┐
        │           │
       YES         NO
        │           │
        ↓           ↓
   ┌─────────┐  ┌──────────────────────┐
   │ GRANTED │  │ 403 FORBIDDEN        │
   │ (Own    │  │ "You can only view   │
   │  Plan)  │  │  your own plans"     │
   └─────────┘  └──────────────────────┘
```

### Permission Matrix

```
┌──────────────────────────────────────────────────────────────┐
│                    RBAC Permission Matrix                    │
├──────────────┬─────────────────┬──────────────┬──────────────┤
│ User Role    │ Permission      │ Own Patient  │ Other Patient│
├──────────────┼─────────────────┼──────────────┼──────────────┤
│ Doctor       │ VIEW_ALL        │ ✅ ALLOW     │ ✅ ALLOW     │
│ Receptionist │ VIEW_ALL        │ ✅ ALLOW     │ ✅ ALLOW     │
│ Patient      │ VIEW_OWN        │ ✅ ALLOW     │ ❌ DENY 403  │
│ Anonymous    │ None            │ ❌ DENY 401  │ ❌ DENY 401  │
└──────────────┴─────────────────┴──────────────┴──────────────┘
```

---

## Data Flow - API 5.2

### Complete Data Flow (Single Request)

```
┌────────────────────────────────────────────────────────────────┐
│  Client Request                                                │
│  GET /api/v1/patients/BN-1001/treatment-plans/PLAN-2025...    │
└──────────────────────────────┬─────────────────────────────────┘
                               │
                               ↓
┌────────────────────────────────────────────────────────────────┐
│  STEP 1: RBAC Verification                                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ • Find patient by code: BN-1001                          │ │
│  │ • Check permissions: VIEW_ALL or VIEW_OWN                │ │
│  │ • Verify account_id if VIEW_OWN                          │ │
│  │                                                          │ │
│  │ Result: ✅ Access GRANTED                                │ │
│  └──────────────────────────────────────────────────────────┘ │
└──────────────────────────────┬─────────────────────────────────┘
                               │
                               ↓
┌────────────────────────────────────────────────────────────────┐
│  STEP 2: Database Query (Single JPQL)                         │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ SELECT new TreatmentPlanDetailDTO(                      │ │
│  │   p.planId, p.planCode, ...,                            │ │
│  │   emp.employeeCode, CONCAT(...),                        │ │
│  │   pat.patientCode, ...,                                 │ │
│  │   phase.phaseId, phase.phaseNumber, ...,                │ │
│  │   item.itemId, item.sequenceNumber, ...,                │ │
│  │   apt.appointmentCode, apt.scheduledDate, ...           │ │
│  │ )                                                        │ │
│  │ FROM PatientTreatmentPlan p                             │ │
│  │ INNER JOIN p.createdBy emp                              │ │
│  │ INNER JOIN p.patient pat                                │ │
│  │ LEFT JOIN p.phases phase                                │ │
│  │ LEFT JOIN phase.items item                              │ │
│  │ LEFT JOIN AppointmentPlanItemBridge bridge              │ │
│  │   ON bridge.id.itemId = item.itemId                     │ │
│  │ LEFT JOIN Appointment apt                               │ │
│  │   ON apt.appointmentId = bridge.id.appointmentId        │ │
│  │ WHERE pat.patientCode = 'BN-1001'                       │ │
│  │   AND p.planCode = 'PLAN-20251001-001'                  │ │
│  │ ORDER BY phase.phaseNumber, item.sequenceNumber         │ │
│  │                                                          │ │
│  │ Result: List<TreatmentPlanDetailDTO> (Flat, 30 rows)   │ │
│  └──────────────────────────────────────────────────────────┘ │
└──────────────────────────────┬─────────────────────────────────┘
                               │
                               ↓
┌────────────────────────────────────────────────────────────────┐
│  STEP 3: Transform Flat → Nested (O(n) Grouping)              │
│                                                                │
│  Input: List<TreatmentPlanDetailDTO> (30 rows)                │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Row 1: Plan, EMP001, BN-1001, Phase1, Item1, Apt1       │ │
│  │ Row 2: Plan, EMP001, BN-1001, Phase1, Item2, Apt2       │ │
│  │ Row 3: Plan, EMP001, BN-1001, Phase1, Item3, Apt3       │ │
│  │ Row 4: Plan, EMP001, BN-1001, Phase2, Item4, null       │ │
│  │ Row 5: Plan, EMP001, BN-1001, Phase2, Item5, null       │ │
│  │ ...                                                      │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  Processing:                                                   │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ 1. Extract plan metadata from row 1                     │ │
│  │    (all rows have same plan data)                       │ │
│  │                                                          │ │
│  │ 2. Group by phaseId                                     │ │
│  │    Map<Long, List<DTO>>:                                │ │
│  │    - Phase 1 → [Row1, Row2, Row3]                       │ │
│  │    - Phase 2 → [Row4, Row5, ...]                        │ │
│  │    - Phase 3 → [Row10, Row11, ...]                      │ │
│  │                                                          │ │
│  │ 3. For each phase:                                      │ │
│  │    a) Extract phase metadata                            │ │
│  │    b) Group by itemId                                   │ │
│  │       Map<Long, List<DTO>>:                             │ │
│  │       - Item 1 → [Row1]                                 │ │
│  │       - Item 2 → [Row2]                                 │ │
│  │       - Item 3 → [Row3]                                 │ │
│  │                                                          │ │
│  │    c) For each item:                                    │ │
│  │       - Extract item metadata                           │ │
│  │       - Collect appointments (filter nulls)             │ │
│  │       - Build ItemDetailDTO                             │ │
│  │                                                          │ │
│  │    d) Sort items by sequenceNumber                      │ │
│  │    e) Build PhaseDetailDTO                              │ │
│  │                                                          │ │
│  │ 4. Sort phases by phaseNumber                           │ │
│  │                                                          │ │
│  │ 5. Calculate progressSummary:                           │ │
│  │    - totalPhases = 3                                    │ │
│  │    - completedPhases = 1 (where status=COMPLETED)       │ │
│  │    - totalItems = 12                                    │ │
│  │    - completedItems = 5 (where status=COMPLETED)        │ │
│  │    - readyForBookingItems = 7 (where status=READY...)   │ │
│  │                                                          │ │
│  │ 6. Build TreatmentPlanDetailResponse                    │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  Output: TreatmentPlanDetailResponse (Nested JSON)            │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ {                                                        │ │
│  │   planCode: "PLAN-20251001-001",                        │ │
│  │   status: "IN_PROGRESS",                                │ │
│  │   progressSummary: {                                    │ │
│  │     totalPhases: 3,                                     │ │
│  │     completedPhases: 1,                                 │ │
│  │     totalItems: 12,                                     │ │
│  │     completedItems: 5                                   │ │
│  │   },                                                    │ │
│  │   phases: [                                             │ │
│  │     {                                                   │ │
│  │       phaseNumber: 1,                                   │ │
│  │       status: "COMPLETED",                              │ │
│  │       items: [                                          │ │
│  │         {                                               │ │
│  │           sequenceNumber: 1,                            │ │
│  │           status: "COMPLETED",                          │ │
│  │           linkedAppointments: [                         │ │
│  │             { code: "APT-001", ... }                    │ │
│  │           ]                                             │ │
│  │         },                                              │ │
│  │         ...                                             │ │
│  │       ]                                                 │ │
│  │     },                                                  │ │
│  │     { phaseNumber: 2, ... },                            │ │
│  │     { phaseNumber: 3, ... }                             │ │
│  │   ]                                                     │ │
│  │ }                                                       │ │
│  └──────────────────────────────────────────────────────────┘ │
└──────────────────────────────┬─────────────────────────────────┘
                               │
                               ↓
┌────────────────────────────────────────────────────────────────┐
│  STEP 4: Return Response                                       │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ HTTP 200 OK                                              │ │
│  │ Content-Type: application/json                           │ │
│  │                                                          │ │
│  │ Body: TreatmentPlanDetailResponse (JSON)                │ │
│  └──────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────┘
```

---

## Database Query Flow

### Tables Joined in Single Query

```
┌─────────────────────────────────────────────────────────────┐
│                   Database Schema                           │
└─────────────────────────────────────────────────────────────┘

┌───────────────────────┐
│ patient_treatment_    │
│ plans (p)             │
│                       │
│ PK: plan_id           │
│ UK: plan_code         │◄─────────┐
│ FK: patient_id        │          │
│ FK: created_by        │          │ INNER JOIN
└───────┬───────────────┘          │
        │                          │
        │ INNER JOIN         ┌─────────────────┐
        │                    │ employees (emp) │
        ↓                    │                 │
┌───────────────────┐        │ PK: employee_id │
│ patients (pat)    │        │ UK: employee_   │
│                   │        │     code        │
│ PK: patient_id    │        └─────────────────┘
│ UK: patient_code  │
│ FK: account_id    │
└───────────────────┘
        ↑
        │
        │ Used in WHERE clause
        │ (business key lookup)
        │

┌───────────────────────┐
│ patient_plan_phases   │
│ (phase)               │
│                       │
│ PK: patient_phase_id  │
│ FK: plan_id           │◄─── LEFT JOIN (1:N)
│ phase_number          │
│ status                │
└───────┬───────────────┘
        │
        │ LEFT JOIN (1:N)
        ↓
┌───────────────────────┐
│ patient_plan_items    │
│ (item)                │
│                       │
│ PK: item_id           │
│ FK: phase_id          │
│ sequence_number       │
│ status                │
│ service_id            │
└───────┬───────────────┘
        │
        │ LEFT JOIN via bridge table (N:N)
        ↓
┌───────────────────────────────┐
│ appointment_plan_items        │
│ (bridge)                      │
│                               │
│ PK: (appointment_id, item_id) │
│ FK: appointment_id            │◄───┐
│ FK: item_id                   │    │
└───────────────────────────────┘    │
                                     │ LEFT JOIN
                                     │
                            ┌────────────────┐
                            │ appointments   │
                            │ (apt)          │
                            │                │
                            │ PK:            │
                            │  appointment_id│
                            │ UK:            │
                            │  appointment_  │
                            │  code          │
                            └────────────────┘

Query Result:
┌─────────────────────────────────────────────────────────────┐
│ Each row represents one item-appointment relationship       │
│                                                             │
│ Row 1: Plan + Employee + Patient + Phase1 + Item1 + Apt1   │
│ Row 2: Plan + Employee + Patient + Phase1 + Item2 + Apt2   │
│ Row 3: Plan + Employee + Patient + Phase1 + Item3 + null   │
│ Row 4: Plan + Employee + Patient + Phase2 + Item4 + null   │
│ ...                                                         │
│                                                             │
│ Items without appointments have null apt fields             │
└─────────────────────────────────────────────────────────────┘
```

### Query Performance Characteristics

```
┌──────────────────────────────────────────────────────────┐
│ Performance Metrics                                      │
├──────────────────────────────────────────────────────────┤
│ Query Type:           Single JPQL with Constructor Exp.  │
│ Tables Joined:        7 tables                           │
│ Query Complexity:     O(1) - Single query execution      │
│ Join Types:           2 INNER, 5 LEFT                    │
│ Expected Time:        < 100ms                            │
│ Rows Returned:        Variable (depends on items × apts) │
│                                                          │
│ Example:                                                 │
│ - Plan with 3 phases                                     │
│ - 12 items total                                         │
│ - 5 items have appointments                              │
│ - Result: ~15-20 rows returned                           │
│                                                          │
│ Service Processing:   O(n) grouping                      │
│ Total Complexity:     O(1) query + O(n) processing       │
└──────────────────────────────────────────────────────────┘
```

---

## DTO Transformation Flow

### Flat DTO → Nested Response Transformation

```
┌────────────────────────────────────────────────────────────────┐
│  Input: List<TreatmentPlanDetailDTO> (Flat)                   │
│                                                                │
│  Example: 10 rows for plan with 2 phases, 3 items             │
└────────────────────────────┬───────────────────────────────────┘
                             │
                             ↓
┌────────────────────────────────────────────────────────────────┐
│  Grouping Algorithm (O(n))                                     │
│                                                                │
│  STEP 1: Extract Plan Metadata                                │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ TreatmentPlanDetailDTO firstRow = flatDTOs.get(0);      │ │
│  │                                                          │ │
│  │ planId = firstRow.getPlanId();                          │ │
│  │ planCode = firstRow.getPlanCode();                      │ │
│  │ planName = firstRow.getPlanName();                      │ │
│  │ planStatus = firstRow.getPlanStatus();                  │ │
│  │ ...                                                      │ │
│  │                                                          │ │
│  │ All rows have same plan data, use first row only        │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  STEP 2: Group by PhaseID                                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Map<Long, List<DTO>> rowsByPhase =                      │ │
│  │   flatDTOs.stream()                                     │ │
│  │     .filter(dto -> dto.getPhaseId() != null)            │ │
│  │     .collect(groupingBy(DTO::getPhaseId));              │ │
│  │                                                          │ │
│  │ Result:                                                  │ │
│  │ phaseId=1 → [row1, row2, row3, row4]                    │ │
│  │ phaseId=2 → [row5, row6, ...]                           │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  STEP 3: For Each Phase → Group by ItemID                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ for (phaseEntry : rowsByPhase) {                        │ │
│  │   List<DTO> phaseRows = phaseEntry.getValue();          │ │
│  │   DTO firstPhaseRow = phaseRows.get(0);                 │ │
│  │                                                          │ │
│  │   // Extract phase metadata                             │ │
│  │   phaseId = firstPhaseRow.getPhaseId();                 │ │
│  │   phaseNumber = firstPhaseRow.getPhaseNumber();         │ │
│  │   phaseName = firstPhaseRow.getPhaseName();             │ │
│  │   ...                                                    │ │
│  │                                                          │ │
│  │   // Group by itemId within this phase                  │ │
│  │   Map<Long, List<DTO>> rowsByItem =                     │ │
│  │     phaseRows.stream()                                  │ │
│  │       .filter(dto -> dto.getItemId() != null)           │ │
│  │       .collect(groupingBy(DTO::getItemId));             │ │
│  │                                                          │ │
│  │   Result for Phase 1:                                   │ │
│  │   itemId=1 → [row1]                                     │ │
│  │   itemId=2 → [row2]                                     │ │
│  │   itemId=3 → [row3, row4] (item has 2 appointments)     │ │
│  │ }                                                        │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  STEP 4: For Each Item → Collect Appointments                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ for (itemEntry : rowsByItem) {                          │ │
│  │   List<DTO> itemRows = itemEntry.getValue();            │ │
│  │   DTO firstItemRow = itemRows.get(0);                   │ │
│  │                                                          │ │
│  │   // Extract item metadata                              │ │
│  │   itemId = firstItemRow.getItemId();                    │ │
│  │   sequenceNumber = firstItemRow.getSequenceNumber();    │ │
│  │   itemName = firstItemRow.getItemName();                │ │
│  │   ...                                                    │ │
│  │                                                          │ │
│  │   // Collect appointments (filter nulls)                │ │
│  │   List<LinkedAppointmentDTO> appointments =             │ │
│  │     itemRows.stream()                                   │ │
│  │       .filter(dto -> dto.getAptCode() != null)          │ │
│  │       .map(dto -> new LinkedAppointmentDTO(             │ │
│  │         dto.getAptCode(),                               │ │
│  │         dto.getAptScheduledDate(),                      │ │
│  │         dto.getAptStatus()                              │ │
│  │       ))                                                │ │
│  │       .distinct() // Remove duplicates                  │ │
│  │       .collect(toList());                               │ │
│  │                                                          │ │
│  │   // Build ItemDetailDTO                                │ │
│  │   ItemDetailDTO item = ItemDetailDTO.builder()          │ │
│  │     .itemId(itemId)                                     │ │
│  │     .sequenceNumber(sequenceNumber)                     │ │
│  │     ...                                                  │ │
│  │     .linkedAppointments(appointments)                   │ │
│  │     .build();                                           │ │
│  │ }                                                        │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  STEP 5: Sort Items & Build PhaseDetailDTO                    │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ items.sort(comparing(ItemDetailDTO::getSequenceNumber));│ │
│  │                                                          │ │
│  │ PhaseDetailDTO phase = PhaseDetailDTO.builder()         │ │
│  │   .phaseId(phaseId)                                     │ │
│  │   .phaseNumber(phaseNumber)                             │ │
│  │   ...                                                    │ │
│  │   .items(items)                                         │ │
│  │   .build();                                             │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  STEP 6: Sort Phases & Calculate Progress                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ phases.sort(comparing(PhaseDetailDTO::getPhaseNumber)); │ │
│  │                                                          │ │
│  │ // Calculate progress summary                           │ │
│  │ int totalPhases = phases.size();                        │ │
│  │ long completedPhases = phases.stream()                  │ │
│  │   .filter(p -> "COMPLETED".equals(p.getStatus()))       │ │
│  │   .count();                                             │ │
│  │                                                          │ │
│  │ List<ItemDetailDTO> allItems = phases.stream()          │ │
│  │   .flatMap(p -> p.getItems().stream())                  │ │
│  │   .collect(toList());                                   │ │
│  │                                                          │ │
│  │ int totalItems = allItems.size();                       │ │
│  │ long completedItems = allItems.stream()                 │ │
│  │   .filter(i -> "COMPLETED".equals(i.getStatus()))       │ │
│  │   .count();                                             │ │
│  │ ...                                                      │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  STEP 7: Build Final Response                                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ return TreatmentPlanDetailResponse.builder()            │ │
│  │   .planId(planId)                                       │ │
│  │   .planCode(planCode)                                   │ │
│  │   ...                                                    │ │
│  │   .progressSummary(progressSummary)                     │ │
│  │   .phases(phases)                                       │ │
│  │   .build();                                             │ │
│  └──────────────────────────────────────────────────────────┘ │
└────────────────────────────┬───────────────────────────────────┘
                             │
                             ↓
┌────────────────────────────────────────────────────────────────┐
│  Output: TreatmentPlanDetailResponse (Nested)                 │
│                                                                │
│  {                                                             │
│    planCode: "PLAN-...",                                       │
│    progressSummary: { ... },                                   │
│    phases: [                                                   │
│      {                                                         │
│        phaseNumber: 1,                                         │
│        items: [                                                │
│          {                                                     │
│            sequenceNumber: 1,                                  │
│            linkedAppointments: [ {...}, {...} ]                │
│          }                                                     │
│        ]                                                       │
│      }                                                         │
│    ]                                                           │
│  }                                                             │
└────────────────────────────────────────────────────────────────┘
```

---

## Error Handling Flow

### Exception Handling Strategy

```
┌────────────────────────────────────────────────────────────┐
│  Request Processing                                        │
└────────────────┬───────────────────────────────────────────┘
                 │
                 ↓
         ┌───────────────┐
         │ Try Execute   │
         └───────┬───────┘
                 │
    ┌────────────┴────────────┐
    │                         │
 SUCCESS                   EXCEPTION
    │                         │
    ↓                         ↓
┌─────────┐          ┌─────────────────┐
│ Return  │          │ Catch Exception │
│ 200 OK  │          └────────┬────────┘
└─────────┘                   │
                    ┌─────────┴─────────────────────────┐
                    │                                   │
          ┌─────────▼──────────┐              ┌────────▼────────┐
          │ Patient Not Found  │              │ Access Denied   │
          │ (from repo)        │              │ (from RBAC)     │
          └─────────┬──────────┘              └────────┬────────┘
                    │                                  │
                    ↓                                  ↓
          ┌──────────────────┐              ┌──────────────────┐
          │ 404 NOT FOUND    │              │ 403 FORBIDDEN    │
          │                  │              │                  │
          │ {                │              │ {                │
          │   status: 404,   │              │   status: 403,   │
          │   error: "..."   │              │   error: "..."   │
          │   message:       │              │   message:       │
          │     "Patient not │              │     "You can     │
          │      found with  │              │      only view   │
          │      code: XX"   │              │      your own    │
          │ }                │              │      plans"      │
          └──────────────────┘              │ }                │
                                           └──────────────────┘
                    │
          ┌─────────▼──────────┐
          │ Plan Not Found     │
          │ (from repo)        │
          └─────────┬──────────┘
                    │
                    ↓
          ┌──────────────────────┐
          │ 404 NOT FOUND        │
          │                      │
          │ {                    │
          │   status: 404,       │
          │   error: "..."       │
          │   message:           │
          │     "Treatment plan  │
          │      'XX' not found  │
          │      for patient     │
          │      'YY'"           │
          │ }                    │
          └──────────────────────┘

┌────────────────────────────────────────────────────────────┐
│ Global Exception Handler                                   │
│                                                            │
│ @ControllerAdvice handles:                                 │
│ - IllegalArgumentException → 404 NOT FOUND                 │
│ - AccessDeniedException → 403 FORBIDDEN                    │
│ - AuthenticationException → 401 UNAUTHORIZED               │
│ - Generic Exception → 500 INTERNAL SERVER ERROR            │
└────────────────────────────────────────────────────────────┘
```

---

## Complete Architecture Diagram

### Full System Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                                     │
│                                                                          │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                 │
│  │   Browser   │    │   Mobile    │    │   Postman   │                 │
│  │     App     │    │     App     │    │     API     │                 │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘                 │
│         │                  │                   │                         │
│         └──────────────────┴───────────────────┘                         │
│                            │                                             │
│                            │ HTTP/HTTPS                                  │
│                            │ JSON                                        │
└────────────────────────────┼─────────────────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────────────┐
│                            ↓                                             │
│                   SPRING BOOT APPLICATION                                │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                     SECURITY LAYER                                 │ │
│  │  ┌──────────────────────────────────────────────────────────────┐ │ │
│  │  │  Spring Security Filter Chain                                │ │ │
│  │  │  - JwtAuthenticationFilter                                   │ │ │
│  │  │  - OAuth2ResourceServerConfigurer                            │ │ │
│  │  │  - @PreAuthorize("hasAuthority(...)")                        │ │ │
│  │  └──────────────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                             │                                            │
│                             ↓                                            │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                    CONTROLLER LAYER                                │ │
│  │  ┌──────────────────────────────────────────────────────────────┐ │ │
│  │  │  TreatmentPlanController                                     │ │ │
│  │  │                                                              │ │ │
│  │  │  @RestController                                             │ │ │
│  │  │  @RequestMapping("/api/v1/patients/{patientCode}/...")      │ │ │
│  │  │                                                              │ │ │
│  │  │  Endpoints:                                                  │ │ │
│  │  │  - GET /treatment-plans          (API 5.1 - List)           │ │ │
│  │  │  - GET /treatment-plans/{code}   (API 5.2 - Detail)         │ │ │
│  │  └──────────────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                             │                                            │
│                             ↓                                            │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                     SERVICE LAYER                                  │ │
│  │  ┌──────────────────────────────────────────────────────────────┐ │ │
│  │  │  TreatmentPlanService                                        │ │ │
│  │  │  - getTreatmentPlansByPatient()                              │ │ │
│  │  └──────────────────────────────────────────────────────────────┘ │ │
│  │  ┌──────────────────────────────────────────────────────────────┐ │ │
│  │  │  TreatmentPlanDetailService                                  │ │ │
│  │  │  - getTreatmentPlanDetail()                                  │ │ │
│  │  │  - verifyPatientAccessPermission()  (RBAC)                   │ │ │
│  │  │  - buildNestedResponse()            (O(n) grouping)          │ │ │
│  │  │  - calculateProgressSummary()       (Count stats)            │ │ │
│  │  └──────────────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                             │                                            │
│                             ↓                                            │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                   REPOSITORY LAYER                                 │ │
│  │  ┌──────────────────────────────────────────────────────────────┐ │ │
│  │  │  PatientTreatmentPlanRepository                              │ │ │
│  │  │  extends JpaRepository                                       │ │ │
│  │  │                                                              │ │ │
│  │  │  Methods:                                                    │ │ │
│  │  │  - findByPatientIdWithDoctor()  (API 5.1)                   │ │ │
│  │  │  - findDetailByPatientCodeAndPlanCode()  (API 5.2)          │ │ │
│  │  │    └─ @Query with Constructor Expression                    │ │ │
│  │  │       JOIN 5 tables, return List<FlatDTO>                   │ │ │
│  │  └──────────────────────────────────────────────────────────────┘ │ │
│  │  ┌──────────────────────────────────────────────────────────────┐ │ │
│  │  │  PatientRepository                                           │ │ │
│  │  │  - findOneByPatientCode()                                    │ │ │
│  │  └──────────────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                             │                                            │
│                             ↓                                            │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                   PERSISTENCE LAYER                                │ │
│  │  ┌──────────────────────────────────────────────────────────────┐ │ │
│  │  │  JPA / Hibernate                                             │ │ │
│  │  │  - Entity Manager                                            │ │ │
│  │  │  - Query Execution                                           │ │ │
│  │  │  - ResultSet Mapping                                         │ │ │
│  │  └──────────────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                             │                                            │
└─────────────────────────────┼──────────────────────────────────────────── ┘
                              │
                              │ JDBC
                              ↓
┌────────────────────────────────────────────────────────────────────────┐
│                         DATABASE LAYER                                 │
│                                                                        │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │  PostgreSQL Database                                           │   │
│  │                                                                │   │
│  │  Tables:                                                       │   │
│  │  ┌──────────────────────┐  ┌──────────────────────┐           │   │
│  │  │ patients             │  │ employees            │           │   │
│  │  │ - patient_id (PK)    │  │ - employee_id (PK)   │           │   │
│  │  │ - patient_code (UK)  │  │ - employee_code (UK) │           │   │
│  │  │ - account_id (FK)    │  └──────────────────────┘           │   │
│  │  └──────────────────────┘                                     │   │
│  │                                                                │   │
│  │  ┌──────────────────────────────────────────────────┐         │   │
│  │  │ patient_treatment_plans                          │         │   │
│  │  │ - plan_id (PK)                                   │         │   │
│  │  │ - plan_code (UK)                                 │         │   │
│  │  │ - patient_id (FK → patients)                     │         │   │
│  │  │ - created_by (FK → employees)                    │         │   │
│  │  │ - status (ENUM)                                  │         │   │
│  │  │ - total_price, discount_amount, final_cost       │         │   │
│  │  └──────────────────────────────────────────────────┘         │   │
│  │                        │                                       │   │
│  │                        │ 1:N                                   │   │
│  │                        ↓                                       │   │
│  │  ┌──────────────────────────────────────────────────┐         │   │
│  │  │ patient_plan_phases                              │         │   │
│  │  │ - patient_phase_id (PK)                          │         │   │
│  │  │ - plan_id (FK)                                   │         │   │
│  │  │ - phase_number                                   │         │   │
│  │  │ - phase_name                                     │         │   │
│  │  │ - status (ENUM)                                  │         │   │
│  │  │ - start_date, completion_date                    │         │   │
│  │  └──────────────────────────────────────────────────┘         │   │
│  │                        │                                       │   │
│  │                        │ 1:N                                   │   │
│  │                        ↓                                       │   │
│  │  ┌──────────────────────────────────────────────────┐         │   │
│  │  │ patient_plan_items                               │         │   │
│  │  │ - item_id (PK)                                   │         │   │
│  │  │ - phase_id (FK)                                  │         │   │
│  │  │ - service_id                                     │         │   │
│  │  │ - sequence_number                                │         │   │
│  │  │ - item_name                                      │         │   │
│  │  │ - status (ENUM)                                  │         │   │
│  │  │ - price (snapshot), estimated_time_minutes       │         │   │
│  │  │ - completed_at                                   │         │   │
│  │  └──────────────────────────────────────────────────┘         │   │
│  │                        │                                       │   │
│  │                        │ N:N (via bridge)                      │   │
│  │                        ↓                                       │   │
│  │  ┌─────────────────────────────┐    ┌──────────────────────┐ │   │
│  │  │ appointment_plan_items      │    │ appointments         │ │   │
│  │  │ (Bridge Table)              │    │ - appointment_id (PK)│ │   │
│  │  │ - appointment_id (PK, FK)   │───→│ - appointment_code   │ │   │
│  │  │ - item_id (PK, FK)          │    │ - status             │ │   │
│  │  └─────────────────────────────┘    │ - scheduled_date     │ │   │
│  │                                      └──────────────────────┘ │   │
│  └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Summary

This flow document provides complete visualization of:

1. ✅ API request flow from client to database
2. ✅ RBAC permission verification flow
3. ✅ Data transformation (flat → nested)
4. ✅ Database query structure and performance
5. ✅ DTO grouping algorithm (O(n))
6. ✅ Error handling strategy
7. ✅ Complete system architecture

**Use Cases:**

- Architecture review
- Onboarding new developers
- Performance optimization analysis
- Security audit
- API documentation
