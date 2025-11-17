# V21 Migration Guide

## 📋 Overview

This guide helps teams migrate to V21 Clinical Rules Engine safely.

**Timeline:** 1-2 weeks
**Complexity:** Medium
**Impact:** 4 APIs changed, 1 new feature added

---

## 🎯 Pre-Migration Checklist

### Backend Team

- [ ] Review [V21_TECHNICAL_SUMMARY.md](V21_TECHNICAL_SUMMARY.md)
- [ ] Backup production database
- [ ] Test schema migration on staging
- [ ] Verify seed data loads correctly
- [ ] Review all 4 API changes
- [ ] Prepare rollback plan

### Frontend Team

- [ ] Review [V21_CLINICAL_RULES_FRONTEND_GUIDE.md](V21_CLINICAL_RULES_FRONTEND_GUIDE.md)
- [ ] Review [V21_QUICK_REFERENCE.md](V21_QUICK_REFERENCE.md) for TL;DR
- [ ] Identify affected components
- [ ] Plan UI changes for new status
- [ ] Prepare error handling updates
- [ ] Update TypeScript types

### QA Team

- [ ] Review test scenarios in docs
- [ ] Prepare test data
- [ ] Create test appointments with rules
- [ ] Document expected behaviors
- [ ] Plan regression testing

---

## 🔄 Migration Steps

### Phase 1: Backend Deployment (Day 1)

#### Step 1: Database Migration

```bash
# 1. Backup database
pg_dump -U postgres dental_clinic > backup_pre_v21.sql

# 2. Connect to database
psql -U postgres -d dental_clinic

# 3. Check current schema version
SELECT * FROM schema_version ORDER BY installed_on DESC LIMIT 1;

# 4. Run V21 migration
\i src/main/resources/db/migration/schema.sql

# 5. Load seed data
\i src/main/resources/db/seed/dental-clinic-seed-data.sql

# 6. Verify
SELECT COUNT(*) FROM service_dependencies;
-- Expected: 6 rows

SELECT rule_type, COUNT(*)
FROM service_dependencies
GROUP BY rule_type;
-- Expected:
--   REQUIRES_PREREQUISITE: 1
--   REQUIRES_MIN_DAYS: 1
--   EXCLUDES_SAME_DAY: 2
--   BUNDLES_WITH: 2
```

#### Step 2: Application Deployment

```bash
# 1. Pull latest code
git checkout feat/BE-501-manage-treatment-plans
git pull origin feat/BE-501-manage-treatment-plans

# 2. Build
./mvnw clean package -DskipTests

# 3. Run tests
./mvnw test

# 4. Deploy
# (Use your deployment method)

# 5. Verify application started
curl http://localhost:8080/actuator/health
```

#### Step 3: Smoke Testing

```bash
# Test 1: Check service menu includes bundlesWith
curl -X GET "http://localhost:8080/api/v1/services/grouped" \
  -H "Authorization: Bearer {token}" | jq '.[] | .services[] | select(.bundlesWith != null)'

# Test 2: Try booking conflicting services
curl -X POST "http://localhost:8080/api/v1/appointments" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "services": [
      {"serviceId": 8},  # EXTRACT_WISDOM_L2
      {"serviceId": 7}   # BLEACH_INOFFICE
    ],
    "appointmentDate": "2024-11-20",
    "startTime": "10:00"
  }'
# Expected: 409 Conflict with errorCode: CLINICAL_RULE_EXCLUSION_VIOLATED

# Test 3: Approve treatment plan
# (Check items have correct initial statuses)
```

### Phase 2: Frontend Update (Day 2-5)

#### Step 1: Update Dependencies & Types

```bash
# 1. Pull latest API changes
git pull origin main

# 2. Update TypeScript types
```

```typescript
// src/types/appointment.types.ts
export enum PlanItemStatus {
  PENDING = "PENDING",
  READY_FOR_BOOKING = "READY_FOR_BOOKING",
  WAITING_FOR_PREREQUISITE = "WAITING_FOR_PREREQUISITE", // ← ADD
  SCHEDULED = "SCHEDULED",
  COMPLETED = "COMPLETED",
  CANCELLED = "CANCELLED",
  SKIPPED = "SKIPPED",
}

// src/types/errors.types.ts
export enum ClinicalRuleErrorCode {
  EXCLUSION_VIOLATED = "CLINICAL_RULE_EXCLUSION_VIOLATED",
  PREREQUISITE_NOT_MET = "CLINICAL_RULE_PREREQUISITE_NOT_MET",
  MIN_DAYS_NOT_MET = "CLINICAL_RULE_MIN_DAYS_NOT_MET",
}

// src/types/service.types.ts
export interface InternalServiceDTO {
  serviceId: number;
  serviceCode: string;
  serviceName: string;
  price: number;
  durationMinutes: number;
  bundlesWith?: string[]; // ← ADD (optional)
}
```

#### Step 2: Update Error Handling

```typescript
// src/services/appointment.service.ts
export async function createAppointment(data: AppointmentRequest) {
  try {
    const response = await api.post("/appointments", data);
    return response.data;
  } catch (error) {
    if (error.response?.status === 409) {
      const errorCode = error.response.data.errorCode;

      // ← ADD: Handle V21 error codes
      if (errorCode?.startsWith("CLINICAL_RULE_")) {
        throw new ClinicalRuleError(
          errorCode,
          error.response.data.message,
          error.response.data
        );
      }
    }
    throw error;
  }
}
```

#### Step 3: Update UI Components

**Priority 1: Critical (Must Have)**

- [ ] Add WAITING_FOR_PREREQUISITE status badge
- [ ] Disable booking button for locked items
- [ ] Handle clinical rule errors in appointment form

**Priority 2: Important (Should Have)**

- [ ] Refresh plan after completing items
- [ ] Show bundle suggestions in service selector

**Priority 3: Nice to Have**

- [ ] Animations for status changes
- [ ] Prerequisite information tooltips
- [ ] Bundle suggestion analytics

#### Step 4: Component Updates

```typescript
// src/components/TreatmentPlanItem.tsx
function TreatmentPlanItem({ item }: Props) {
  const isLocked = item.status === "WAITING_FOR_PREREQUISITE";

  return (
    <div className="treatment-item">
      <StatusBadge status={item.status} />

      {/* ← ADD: Show lock info */}
      {isLocked && (
        <Alert type="warning">🔒 Cần hoàn thành dịch vụ tiên quyết trước</Alert>
      )}

      {/* ← UPDATE: Disable button if locked */}
      <Button disabled={isLocked} onClick={() => bookItem(item)}>
        {isLocked ? "🔒 Đang khóa" : "📅 Đặt lịch"}
      </Button>
    </div>
  );
}
```

### Phase 3: Testing (Day 6-7)

#### Regression Testing

| Test Case                 | Description           | Expected Result           | Status |
| ------------------------- | --------------------- | ------------------------- | ------ |
| **Existing Flows**        |                       |                           |        |
| Book simple appointment   | No rules involved     | Works as before           | ⬜     |
| Create treatment plan     | No prerequisites      | Items → READY_FOR_BOOKING | ⬜     |
| Complete plan item        | No dependents         | Item marked COMPLETED     | ⬜     |
| **V21 New Flows**         |                       |                           |        |
| Book conflicting services | EXCLUDES_SAME_DAY     | 409 with error code       | ⬜     |
| Book without prerequisite | REQUIRES_PREREQUISITE | 409 with error code       | ⬜     |
| Book too soon             | REQUIRES_MIN_DAYS     | 409 with error code       | ⬜     |
| View service menu         | Has bundle rules      | Shows bundlesWith field   | ⬜     |
| Approve plan with prereqs | Has dependency        | Items → WAITING           | ⬜     |
| Complete prerequisite     | Has dependent items   | Auto-unlocks dependents   | ⬜     |

#### Test Data

```sql
-- Test Patient with history
INSERT INTO patient (patient_code, full_name, phone)
VALUES ('TEST_V21', 'Test Patient V21', '0900000001');

-- Test completed GEN_EXAM (for prerequisite tests)
INSERT INTO appointment (patient_id, start_time, status)
VALUES (
  (SELECT patient_id FROM patient WHERE patient_code = 'TEST_V21'),
  NOW() - INTERVAL '30 days',
  'COMPLETED'
);

-- Add GEN_EXAM service to completed appointment
INSERT INTO appointment_service (appointment_id, service_id, status)
VALUES (LAST_INSERT_ID(), 1, 'COMPLETED');  -- serviceId 1 = GEN_EXAM
```

### Phase 4: Monitoring & Rollback (Day 7+)

#### Monitoring Setup

```bash
# Watch application logs for clinical rule violations
tail -f logs/application.log | grep "CLINICAL_RULE"

# Check error rate in metrics
curl http://localhost:8080/actuator/metrics/clinical.rule.violations
```

#### Success Metrics

- [ ] Zero unhandled errors in production
- [ ] Clinical rule violations logged properly
- [ ] Frontend displays all statuses correctly
- [ ] Users can see bundle suggestions
- [ ] Auto-unlock works correctly

#### Rollback Plan

**If critical issues found:**

```sql
-- 1. Rollback database (ONLY if needed)
psql -U postgres dental_clinic < backup_pre_v21.sql

-- 2. Redeploy previous application version
git checkout main
./mvnw clean package -DskipTests
# Deploy old version

-- 3. Clear cache (if applicable)
redis-cli FLUSHDB
```

**If minor issues found:**

- Keep V21 running
- Fix bugs in hotfix branch
- Deploy hotfix without full rollback

---

## 🐛 Common Issues & Solutions

### Issue 1: Compilation Error - Cannot find symbol

```
Error: cannot find symbol: class ClinicalRulesValidationService
```

**Solution:**

```bash
# Clean and rebuild
./mvnw clean compile
```

### Issue 2: Seed Data Not Loading

```
ERROR: duplicate key value violates unique constraint
```

**Solution:**

```sql
-- Clear existing rules first
DELETE FROM service_dependencies;

-- Reload seed data
\i src/main/resources/db/seed/dental-clinic-seed-data.sql
```

### Issue 3: Frontend Shows Old Status Values

**Solution:**

```typescript
// Clear localStorage/sessionStorage
localStorage.clear();
sessionStorage.clear();

// Hard refresh: Ctrl+Shift+R (Windows) or Cmd+Shift+R (Mac)
```

### Issue 4: Auto-Unlock Not Working

**Solution:**

```typescript
// After completing item, MUST refresh entire plan
await updateItemStatus(itemId, "COMPLETED");

// ← ADD THIS LINE
const updatedPlan = await getTreatmentPlan(patientCode);
setTreatmentPlan(updatedPlan);
```

---

## 📊 Validation Checklist

### Backend Validation

```bash
# 1. Check database schema
psql -U postgres -d dental_clinic -c "\d service_dependencies"

# 2. Verify seed data
psql -U postgres -d dental_clinic -c "SELECT * FROM service_dependencies;"

# 3. Test APIs
# See V21_TECHNICAL_SUMMARY.md for curl examples

# 4. Check logs for errors
grep -i "error" logs/application.log | grep -v "404"
```

### Frontend Validation

```bash
# 1. Check console for errors
# Open browser DevTools → Console

# 2. Verify API responses include new fields
# Network tab → Check /api/v1/services/grouped response

# 3. Test error handling
# Try to book conflicting services → Should show specific error

# 4. Test status display
# Approve plan with prerequisites → Items should show WAITING status
```

---

## 📞 Support Contacts

**Backend Issues:**

- Slack: #backend-support
- Email: backend-team@example.com

**Frontend Issues:**

- Slack: #frontend-support
- Email: frontend-team@example.com

**Database Issues:**

- Slack: #dba-support
- Email: dba@example.com

---

## 📚 Additional Resources

- [V21 Technical Summary](V21_TECHNICAL_SUMMARY.md) - Backend implementation
- [V21 Frontend Guide](V21_CLINICAL_RULES_FRONTEND_GUIDE.md) - Frontend integration
- [V21 Quick Reference](V21_QUICK_REFERENCE.md) - Essential changes
- [CHANGELOG.md](../CHANGELOG.md) - Version history

---

**Migration Prepared By:** Backend Team
**Last Updated:** November 17, 2024
**Version:** V21
