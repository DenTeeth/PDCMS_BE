# V21 Clinical Rules - Quick Reference

## 🎯 TL;DR

V21 adds automatic service dependency validation and smart suggestions. **3 breaking changes + 1 new feature.**

---

## ⚠️ Breaking Changes

### 1. API 3.2 - New Error Codes (Create Appointment)

```javascript
// NEW ERROR CODES to handle:
-CLINICAL_RULE_EXCLUSION_VIOLATED - // Same-day conflict
  CLINICAL_RULE_PREREQUISITE_NOT_MET - // Missing prerequisite
  CLINICAL_RULE_MIN_DAYS_NOT_MET; // Too soon after prerequisite
```

**Action:** Update error handling to show specific messages.

---

### 2. API 5.6 - Auto-Unlock After Complete

```javascript
// NEW BEHAVIOR: Completing item auto-unlocks dependent items

// BEFORE: Just update completed item
await updateItemStatus(itemId, "COMPLETED");

// AFTER: Refresh plan to show unlocked items
await updateItemStatus(itemId, "COMPLETED");
const updatedPlan = await getTreatmentPlan(patientCode); // ← ADD THIS
```

**Action:** Refresh treatment plan after completing any item.

---

### 3. API 5.9 - New Item Status on Approval

```javascript
// NEW STATUS: WAITING_FOR_PREREQUISITE

const STATUS_CONFIG = {
  // ... existing statuses ...
  WAITING_FOR_PREREQUISITE: {
    // ← ADD THIS
    label: "Chờ dịch vụ tiên quyết",
    color: "orange",
    icon: "🔒",
  },
};
```

**Action:**

- Add status to UI components
- Disable booking button when status = WAITING_FOR_PREREQUISITE

---

## ✨ New Features

### 4. API 6.5 - Bundle Suggestions

```javascript
// NEW FIELD: bundlesWith (array of service codes)

{
  "serviceCode": "GEN_EXAM",
  "serviceName": "Khám tổng quát",
  "bundlesWith": ["SCALING_L1"]  // ← NEW: Show as suggestions
}
```

**Action:** Show bundle suggestions when user selects services.

---

## 📋 State Flow

```
Plan APPROVED
    ↓
Has prerequisites?
    ↓ YES                    ↓ NO
WAITING_FOR_PREREQUISITE   READY_FOR_BOOKING
    ↓ (auto)                  ↓
Prerequisite completed       User books
    ↓                         ↓
READY_FOR_BOOKING          SCHEDULED
    ↓                         ↓
User books                 COMPLETED
    ↓
SCHEDULED
    ↓
COMPLETED
```

---

## 🧪 Test Cases

| Rule Type             | Test                                       | Expected Error Code                  |
| --------------------- | ------------------------------------------ | ------------------------------------ |
| EXCLUDES_SAME_DAY     | Book EXTRACT + BLEACH same day             | `CLINICAL_RULE_EXCLUSION_VIOLATED`   |
| REQUIRES_PREREQUISITE | Book FILLING without GEN_EXAM              | `CLINICAL_RULE_PREREQUISITE_NOT_MET` |
| REQUIRES_MIN_DAYS     | Book CHECKUP 3 days after EXTRACT (need 7) | `CLINICAL_RULE_MIN_DAYS_NOT_MET`     |
| BUNDLES_WITH          | Select GEN_EXAM                            | Show SCALING_L1 suggestion           |

---

## 🎨 UI Requirements

### Minimum Changes

```jsx
// 1. Add new status badge
<StatusBadge status="WAITING_FOR_PREREQUISITE" /> {/* Orange + Lock icon */}

// 2. Disable booking button
<Button disabled={item.status === 'WAITING_FOR_PREREQUISITE'}>
  {item.status === 'WAITING_FOR_PREREQUISITE' ? '🔒 Đang khóa' : '📅 Đặt lịch'}
</Button>

// 3. Handle new error codes
catch (error) {
  if (error.errorCode?.startsWith('CLINICAL_RULE_')) {
    showClinicalRuleError(error);  // Custom error display
  }
}

// 4. Show bundle suggestions
{service.bundlesWith?.map(code => (
  <Chip label={code} onClick={addToSelection} />
))}
```

---

## 🚀 Deployment Checklist

- [ ] Update error handling for API 3.2
- [ ] Add WAITING_FOR_PREREQUISITE status to UI
- [ ] Refresh plan after completing items
- [ ] Show bundle suggestions in service picker
- [ ] Test all 4 rule types
- [ ] Update TypeScript types
- [ ] Mobile responsive testing

---

## 📞 Need Help?

**Full Guide:** [V21_CLINICAL_RULES_FRONTEND_GUIDE.md](./V21_CLINICAL_RULES_FRONTEND_GUIDE.md)
**Swagger:** http://localhost:8080/swagger-ui.html
**Backend Branch:** `feat/BE-501-manage-treatment-plans`

---

**Version:** V21 | **Last Updated:** Nov 17, 2024
