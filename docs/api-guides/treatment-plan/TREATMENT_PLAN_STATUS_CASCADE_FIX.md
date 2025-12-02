# Treatment Plan Status Cascade - Auto-Completion Fix

## Problem Description

**Issue**: When updating treatment plan item status (API 5.6), the phase and plan statuses were not automatically updating even though the cascade logic was implemented.

**Root Cause**: JPA persistence context was serving stale data. When an item status was updated and saved, the phase's collection of items contained outdated entity references. This caused the completion check to evaluate against old statuses instead of the newly saved values.

## Solution

Added entity manager flush and refresh operations at critical points to ensure:

1. Item status changes are immediately persisted to database
2. Phase entity is refreshed before completion check
3. Plan entity is refreshed before completion check

## Technical Changes

### File Modified

`src/main/java/com/dental/clinic/management/treatment_plans/service/TreatmentPlanItemService.java`

### Changes Made

#### 1. Add Flush After Item Save

```java
PatientPlanItem savedItem = itemRepository.save(item);

// CRITICAL: Flush changes to database before checking completion
// This ensures phase.getItems() will reflect the updated item status
entityManager.flush();
```

#### 2. Refresh Phase Before Completion Check

```java
// STEP 7: Refresh phase to get latest item statuses before completion check
// Without this, phase.getItems() may contain stale data
entityManager.refresh(phase);

// STEP 7A: Check and auto-complete phase
checkAndCompletePhase(phase);
```

#### 3. Refresh Plan Before Completion Check

```java
// STEP 7C: V21 - Check and auto-complete plan (if all phases done)
// Need to refresh plan to get updated phase statuses
entityManager.refresh(plan);
checkAndCompletePlan(plan);
```

#### 4. Enhanced Phase Completion Logic

```java
private void checkAndCompletePhase(PatientPlanPhase phase) {
    List<PatientPlanItem> items = phase.getItems();

    if (items.isEmpty()) {
        log.debug("Phase {} has no items, skipping completion check", phase.getPatientPhaseId());
        return;
    }

    boolean allDone = items.stream()
            .allMatch(item -> item.getStatus() == PlanItemStatus.COMPLETED ||
                    item.getStatus() == PlanItemStatus.SKIPPED);

    if (allDone && phase.getStatus() != PhaseStatus.COMPLETED) {
        phase.setStatus(PhaseStatus.COMPLETED);
        phase.setCompletionDate(java.time.LocalDate.now());
        entityManager.merge(phase);
        entityManager.flush(); // Ensure phase status is persisted immediately
        log.info("Phase {} auto-completed: all {} items are done",
                phase.getPatientPhaseId(), items.size());
    }
}
```

#### 5. Enhanced Plan Completion Logic

```java
private void checkAndCompletePlan(PatientTreatmentPlan plan) {
    if (plan.getStatus() != TreatmentPlanStatus.IN_PROGRESS) {
        log.debug("Plan {} not IN_PROGRESS (current: {}), skipping completion check",
                plan.getPlanCode(), plan.getStatus());
        return;
    }

    List<PatientPlanPhase> phases = plan.getPhases();

    if (phases.isEmpty()) {
        log.debug("Plan {} has no phases, skipping completion check", plan.getPlanCode());
        return;
    }

    // Check if ALL phases are COMPLETED
    long completedPhases = phases.stream()
            .filter(phase -> phase.getStatus() == PhaseStatus.COMPLETED)
            .count();

    boolean allPhasesCompleted = completedPhases == phases.size();

    if (allPhasesCompleted) {
        // AUTO-COMPLETE: IN_PROGRESS -> COMPLETED
        plan.setStatus(TreatmentPlanStatus.COMPLETED);
        planRepository.save(plan);

        log.info("Treatment plan {} (code: {}) auto-completed: IN_PROGRESS -> COMPLETED - All {} phases done",
                plan.getPlanId(), plan.getPlanCode(), phases.size());
    } else {
        log.debug("Plan {} not completed yet: {}/{} phases done",
                plan.getPlanCode(), completedPhases, phases.size());
    }
}
```

## Status Cascade Rules

### Rule 1: Item Status Change Triggers Phase Check

When an item status changes to COMPLETED or SKIPPED:

1. Flush item changes to database
2. Refresh phase entity to get latest item statuses
3. Check if ALL items in phase are COMPLETED or SKIPPED
4. If yes, auto-update phase status to COMPLETED

### Rule 2: Phase Completion Triggers Plan Check

When a phase status changes to COMPLETED:

1. Flush phase changes to database
2. Refresh plan entity to get latest phase statuses
3. Check if ALL phases in plan are COMPLETED
4. If yes, auto-update plan status to COMPLETED

### Status Flow Diagram

```
Item 1: PENDING -> COMPLETED
Item 2: PENDING -> COMPLETED
Item 3: PENDING -> COMPLETED
         |
         v
[All items done]
         |
         v
Phase: PENDING -> COMPLETED
         |
         v
[All phases done]
         |
         v
Plan: IN_PROGRESS -> COMPLETED
```

## Test Scenarios

### Scenario 1: Single Phase Plan - All Items Complete

**Setup**:

- Plan with 1 phase containing 3 items
- Plan status: IN_PROGRESS
- Phase status: PENDING
- All items status: PENDING

**Action**:

1. Update Item 1 -> COMPLETED
2. Update Item 2 -> COMPLETED
3. Update Item 3 -> COMPLETED

**Expected Result**:

- After Item 3 completes:
  - Phase status -> COMPLETED
  - Plan status -> COMPLETED

### Scenario 2: Multi-Phase Plan - Sequential Completion

**Setup**:

- Plan with 2 phases, each with 2 items
- Plan status: IN_PROGRESS
- Phase 1 status: PENDING
- Phase 2 status: PENDING

**Action**:

1. Complete all items in Phase 1
2. Complete all items in Phase 2

**Expected Result**:

- After Phase 1 all items complete:
  - Phase 1 status -> COMPLETED
  - Plan status -> IN_PROGRESS (Phase 2 still pending)
- After Phase 2 all items complete:
  - Phase 2 status -> COMPLETED
  - Plan status -> COMPLETED

### Scenario 3: Mixed Complete and Skip

**Setup**:

- Plan with 1 phase containing 3 items
- Plan status: IN_PROGRESS

**Action**:

1. Update Item 1 -> COMPLETED
2. Update Item 2 -> SKIPPED
3. Update Item 3 -> COMPLETED

**Expected Result**:

- After Item 3 completes:
  - Phase status -> COMPLETED (all items done: 2 completed + 1 skipped)
  - Plan status -> COMPLETED

### Scenario 4: Plan Not IN_PROGRESS - No Auto-Complete

**Setup**:

- Plan status: PENDING
- Phase with all items PENDING

**Action**:
Complete all items in phase

**Expected Result**:

- Phase status -> COMPLETED
- Plan status -> PENDING (unchanged, not IN_PROGRESS)

## Error Prevention

### Common Issues Fixed

1. **Stale Entity References**

   - Problem: Phase.getItems() returns cached collection
   - Fix: entityManager.refresh(phase) before checking

2. **Uncommitted Changes**

   - Problem: Item save not flushed to DB
   - Fix: entityManager.flush() after save

3. **Cascade Timing**
   - Problem: Plan check before phase status persisted
   - Fix: Flush phase status before refreshing plan

### Logging Strategy

All status changes are logged with clear indicators:

```
INFO: Item 123 status changed: PENDING -> COMPLETED
INFO: Phase 45 auto-completed: all 3 items are done
INFO: Treatment plan PLAN-20251201-001 (code: PLAN-20251201-001) auto-completed: IN_PROGRESS -> COMPLETED - All 2 phases done
```

## Database Impact

No schema changes required. Only service layer logic updated.

## API Endpoints Affected

- **API 5.6**: Update Item Status (`PATCH /api/v1/treatment-plans/items/{itemId}/status`)
  - Now properly cascades status changes to phase and plan

## RBAC Considerations

No permission changes required. Existing RBAC logic in `TreatmentPlanItemService` remains unchanged:

- Employees can only update items in plans they created
- Admin/Finance can update all plans (if needed)

## Testing Checklist

- [ ] Test single-phase plan completion
- [ ] Test multi-phase plan completion
- [ ] Test mixed COMPLETED and SKIPPED items
- [ ] Test plan NOT in IN_PROGRESS status
- [ ] Verify logs show cascade progression
- [ ] Verify database shows correct final statuses
- [ ] Test concurrent updates (pessimistic locking)

## Related APIs

- **API 5.6**: Update Item Status (primary API affected)
- **API 5.1/5.2**: Get Treatment Plans (shows updated statuses)
- **API 5.5**: Get All Plans with RBAC (includes status filtering)

## Notes

- This fix applies to manual item status updates via API 5.6
- Appointment completion also triggers cascade via `AppointmentStatusService`
- Both paths now use consistent entity refresh logic
- No breaking changes - only bug fix for existing functionality

---

**Date**: 2025-12-01
**Issue**: Treatment plan status not auto-updating when items complete
**Resolution**: Added entity manager flush/refresh at critical cascade points
