# Issue #40 Fix Verification - Phase and Plan Auto-Complete

**Date**: 2025-12-02
**Fixed By**: AI Assistant
**Issue**: API 5.4 - Phase va Plan khong auto-complete do lazy loading issue (REGRESSION)

## Problem Description

Frontend reported regression where phase and plan status no longer auto-complete when all items are completed:

- When all items in phase completed -> phase.status stays PENDING/null in database
- When all phases completed -> plan.status stays null in database
- List view shows incorrect status
- Frontend had to implement workaround to calculate status from items

## Root Cause

JPA lazy loading collections not refreshed by `entityManager.refresh()`:

1. In `checkAndCompletePhase()`:

   - Line 207: `entityManager.refresh(phase)` only refreshes phase entity
   - `phase.getItems()` returns empty/stale lazy collection
   - Early return when `items.isEmpty()` prevents auto-complete

2. In `checkAndCompletePlan()`:
   - Line 217: `entityManager.refresh(plan)` only refreshes plan entity
   - `plan.getPhases()` returns empty/stale lazy collection
   - Early return when `phases.isEmpty()` prevents auto-complete

## Solution Implemented

Direct database queries to bypass lazy loading:

### 1. Added Repository Methods

**PatientPlanItemRepository.java**:

```java
@Query("SELECT i FROM PatientPlanItem i WHERE i.phase.patientPhaseId = :phaseId")
List<PatientPlanItem> findByPhase_PatientPhaseId(@Param("phaseId") Long phaseId);
```

**PatientPlanPhaseRepository.java**:

```java
import java.util.List; // Added missing import

@Query("SELECT p FROM PatientPlanPhase p WHERE p.treatmentPlan.planId = :planId ORDER BY p.phaseNumber")
List<PatientPlanPhase> findByTreatmentPlan_PlanId(@Param("planId") Long planId);
```

### 2. Modified Service Methods

**TreatmentPlanItemService.java**:

**Before (broken)**:

```java
private void checkAndCompletePhase(PatientPlanPhase phase) {
    List<PatientPlanItem> items = phase.getItems(); // Lazy loading - may be empty/stale
    if (items.isEmpty()) {
        return; // Early return - phase never completes
    }
    // ...
}

private void checkAndCompletePlan(PatientTreatmentPlan plan) {
    List<PatientPlanPhase> phases = plan.getPhases(); // Lazy loading - may be empty/stale
    if (phases.isEmpty()) {
        return; // Early return - plan never completes
    }
    // ...
}
```

**After (fixed)**:

```java
private void checkAndCompletePhase(PatientPlanPhase phase) {
    // Direct query from database - always fresh data
    List<PatientPlanItem> items = itemRepository.findByPhase_PatientPhaseId(phase.getPatientPhaseId());
    if (items.isEmpty()) {
        return;
    }
    // ... rest works correctly with fresh data
}

private void checkAndCompletePlan(PatientTreatmentPlan plan) {
    // Direct query from database - always fresh data
    List<PatientPlanPhase> phases = phaseRepository.findByTreatmentPlan_PlanId(plan.getPlanId());
    if (phases.isEmpty()) {
        return;
    }
    // ... rest works correctly with fresh data
}
```

## Test Results

### Test Data: PLAN-20251001-001 (Patient BN-1001)

Treatment plan structure:

- Phase 1: 3 items (already COMPLETED)
- Phase 2: 4 items (items 4-7)
- Phase 3: 8 items (items 8-15)

### Test 1: Phase Auto-Complete (Phase 2)

**Steps**:

1. Completed item 6: `PATCH /api/v1/patient-plan-items/6/status` with `status=COMPLETED`
2. Completed item 7 (last item): `PATCH /api/v1/patient-plan-items/7/status` with `status=COMPLETED`

**Backend Logs**:

```
2025-12-02T23:05:50.387-08:00 INFO Phase 2 auto-completed: all 4 items are done
```

**Database Verification**:

```sql
SELECT patient_phase_id, phase_number, phase_name, status
FROM patient_plan_phases
WHERE plan_id = (SELECT plan_id FROM patient_treatment_plans WHERE plan_code = 'PLAN-20251001-001')
ORDER BY phase_number;

 patient_phase_id | phase_number | phase_name                               | status
------------------+--------------+------------------------------------------+-----------
                1 |            1 | Giai doan 1: Chuan bi va Kiem tra        | COMPLETED
                2 |            2 | Giai doan 2: Lap Mac cai va Dieu chinh... | COMPLETED
                3 |            3 | Giai doan 3: Dieu chinh dinh ky (8 thang)| PENDING
```

**Result**: PASS - Phase 2 status auto-completed to COMPLETED in database

### Test 2: Plan Auto-Complete (Complete Phase 3)

**Steps**:

1. Completed items 8-14 sequentially
2. Completed item 15 (last item in Phase 3): `PATCH /api/v1/patient-plan-items/15/status`

**Backend Logs**:

```
2025-12-02T23:08:46.387-08:00 INFO Phase 3 auto-completed: all 8 items are done
2025-12-02T23:08:46.760-08:00 INFO Treatment plan 1 (code: PLAN-20251001-001) auto-completed: IN_PROGRESS -> COMPLETED - All 3 phases done
```

**Database Verification - Phases**:

```sql
SELECT patient_phase_id, phase_number, phase_name, status
FROM patient_plan_phases
WHERE plan_id = (SELECT plan_id FROM patient_treatment_plans WHERE plan_code = 'PLAN-20251001-001')
ORDER BY phase_number;

 patient_phase_id | phase_number | phase_name                               | status
------------------+--------------+------------------------------------------+-----------
                1 |            1 | Giai doan 1: Chuan bi va Kiem tra        | COMPLETED
                2 |            2 | Giai doan 2: Lap Mac cai va Dieu chinh... | COMPLETED
                3 |            3 | Giai doan 3: Dieu chinh dinh ky (8 thang)| COMPLETED
```

**Database Verification - Plan**:

```sql
SELECT plan_code, status
FROM patient_treatment_plans
WHERE plan_code = 'PLAN-20251001-001';

    plan_code     |  status
------------------+-----------
 PLAN-20251001-001 | COMPLETED
```

**Result**: PASS - Phase 3 and Plan both auto-completed to COMPLETED in database

### Test 3: List API Shows Correct Status

**Request**:

```bash
GET /api/v1/treatment-plans?patientCode=BN-1001&page=0&size=1
```

**Response**:

```json
{
  "content": [{
    "planCode": "PLAN-20251001-001",
    "planName": "Lo trinh Nieng rang Mac cai Kim loai",
    "patient": {"patientCode": "BN-1001", "fullName": "Doan Thanh Phong"},
    "status": "COMPLETED",
    ...
  }]
}
```

**Result**: PASS - List API correctly shows `"status":"COMPLETED"`

## Summary

**ALL TESTS PASSED**

The lazy loading fix successfully resolves the regression:

- Phase auto-complete: Works correctly
- Plan auto-complete: Works correctly
- Database persistence: Status persisted correctly
- List API: Shows correct status immediately

**Files Modified**:

1. `PatientPlanItemRepository.java` - Added findByPhase_PatientPhaseId()
2. `PatientPlanPhaseRepository.java` - Added findByTreatmentPlan_PlanId() + missing import
3. `TreatmentPlanItemService.java` - Modified checkAndCompletePhase() and checkAndCompletePlan()

**Commit**: Ready for commit with message "Fix Issue #40: Resolve lazy loading regression in phase/plan auto-complete"

## Impact Analysis

**Before Fix**:

- Phase/plan status never auto-completes
- Database shows incorrect status (PENDING/null)
- List API shows incorrect status
- Frontend must calculate status client-side (workaround)

**After Fix**:

- Phase/plan status auto-completes immediately when all items/phases done
- Database shows correct status (COMPLETED)
- List API shows correct status
- Frontend can trust backend status (no workaround needed)

**Backward Compatibility**:

- No breaking changes to API contracts
- Existing data unaffected
- Solution works with current database schema

## Technical Notes

**Why entityManager.refresh() Failed**:

- JPA's `refresh()` method only refreshes simple properties and eager associations
- Lazy collections (`@OneToMany` with `fetch = FetchType.LAZY`) are not refreshed
- Collections remain in original state from when entity was first loaded
- Solution: Query collections directly from database using repository methods

**Alternative Solutions Considered**:

1. Eager loading: Would cause performance issues (N+1 queries)
2. JOIN FETCH in refresh: Not supported by JPA refresh()
3. Manual collection reload: Repository query is cleaner and more maintainable

**Recommended Best Practice**:
When checking entity state after flush/refresh, always query collections from database rather than accessing lazy collections directly.
