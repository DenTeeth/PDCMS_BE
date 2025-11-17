# V21 Clinical Rules Engine - Frontend Integration Guide

## 📋 Overview

**Version:** V21
**Feature:** Clinical Rules Engine - Automated service dependency validation and workflow management
**Branch:** feat/BE-501-manage-treatment-plans
**Release Date:** TBD

---

## 🎯 What's New in V21?

V21 introduces an intelligent clinical rules system that automatically enforces service dependencies and provides smart suggestions to improve booking efficiency and clinical safety.

### Key Features

1. **Hard Rules (Blocking)** - Prevent invalid bookings

   - REQUIRES_PREREQUISITE: Service A must be completed before Service B can be booked
   - REQUIRES_MIN_DAYS: Minimum days must pass between two services
   - EXCLUDES_SAME_DAY: Two services cannot be booked on the same day

2. **Soft Rules (Suggestions)** - Help optimize bookings

   - BUNDLES_WITH: Services that work well together (show as suggestions)

3. **Automatic Item Unlocking** - Smart treatment plan management
   - Items automatically unlock when prerequisites are completed
   - New status: `WAITING_FOR_PREREQUISITE`

---

## 🔄 API Changes

### 1. **API 3.2 - Create Appointment** ⚠️ BREAKING CHANGE

**Endpoint:** `POST /api/v1/appointments`

#### What Changed?

- Backend now validates clinical rules **before** creating appointment
- New error responses for rule violations

#### New Error Responses

```json
// Error 1: Same-day Exclusion Violated
{
  "timestamp": "2024-11-17T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Không thể đặt lịch các dịch vụ sau cùng ngày: [EXTRACT_WISDOM_L2, BLEACH_INOFFICE]",
  "errorCode": "CLINICAL_RULE_EXCLUSION_VIOLATED",
  "path": "/api/v1/appointments"
}

// Error 2: Prerequisite Not Met
{
  "timestamp": "2024-11-17T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Dịch vụ FILLING_COMP yêu cầu hoàn thành GEN_EXAM trước",
  "errorCode": "CLINICAL_RULE_PREREQUISITE_NOT_MET",
  "path": "/api/v1/appointments"
}

// Error 3: Minimum Days Not Met
{
  "timestamp": "2024-11-17T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Cần chờ tối thiểu 7 ngày sau khi hoàn thành EXTRACT_WISDOM_L2 trước khi đặt SURG_CHECKUP",
  "errorCode": "CLINICAL_RULE_MIN_DAYS_NOT_MET",
  "path": "/api/v1/appointments"
}
```

#### Frontend Action Required

```javascript
// BEFORE V21
try {
  const response = await createAppointment(appointmentData);
  showSuccess("Đặt lịch thành công!");
} catch (error) {
  if (error.status === 409) {
    showError("Xung đột lịch hẹn"); // Generic error
  }
}

// AFTER V21 - Handle specific error codes
try {
  const response = await createAppointment(appointmentData);
  showSuccess("Đặt lịch thành công!");
} catch (error) {
  if (error.status === 409) {
    switch (error.errorCode) {
      case "CLINICAL_RULE_EXCLUSION_VIOLATED":
        showError(error.message, {
          title: "⚠️ Xung đột dịch vụ",
          suggestion: "Vui lòng chọn ngày khác hoặc bỏ một trong hai dịch vụ",
        });
        break;

      case "CLINICAL_RULE_PREREQUISITE_NOT_MET":
        showError(error.message, {
          title: "⚠️ Thiếu dịch vụ tiên quyết",
          suggestion: "Bệnh nhân cần hoàn thành dịch vụ tiên quyết trước",
        });
        break;

      case "CLINICAL_RULE_MIN_DAYS_NOT_MET":
        showError(error.message, {
          title: "⚠️ Chưa đủ thời gian chờ",
          suggestion: "Vui lòng chọn ngày khác phù hợp với quy định",
        });
        break;

      case "APPOINTMENT_TIME_CONFLICT":
        // Existing conflict handling
        showError("Xung đột thời gian đặt lịch");
        break;

      default:
        showError(error.message);
    }
  }
}
```

---

### 2. **API 5.6 - Update Treatment Plan Item Status** ✨ ENHANCEMENT

**Endpoint:** `PUT /api/v1/treatment-plans/patients/{patientCode}/items/{itemId}/status`

#### What Changed?

- When item status changed to `COMPLETED`, backend automatically unlocks dependent items
- Dependent items with status `WAITING_FOR_PREREQUISITE` → automatically changed to `READY_FOR_BOOKING`

#### Example Workflow

**Scenario:** Treatment plan has 2 items:

- Item 1: GEN_EXAM (READY_FOR_BOOKING)
- Item 2: FILLING_COMP (WAITING_FOR_PREREQUISITE) - requires GEN_EXAM

**Step 1:** Complete GEN_EXAM appointment

```json
PUT /api/v1/treatment-plans/patients/P001/items/101/status
{
  "newStatus": "COMPLETED"
}
```

**Step 2:** Backend Response - GEN_EXAM marked COMPLETED

```json
{
  "itemId": 101,
  "serviceCode": "GEN_EXAM",
  "status": "COMPLETED",
  "completedAt": "2024-11-17T10:30:00"
}
```

**Step 3:** Backend Automatically Unlocks Item 2 (behind the scenes)

- Item 2 status: `WAITING_FOR_PREREQUISITE` → `READY_FOR_BOOKING`

#### Frontend Action Required

```javascript
// AFTER completing an item, refresh the entire treatment plan
async function completeItem(patientCode, itemId) {
  try {
    // Mark item as COMPLETED
    await updateItemStatus(patientCode, itemId, "COMPLETED");

    // ✨ NEW: Refresh entire plan to show auto-unlocked items
    const updatedPlan = await getTreatmentPlan(patientCode);

    // Show success with info about unlocked items
    const unlockedItems = updatedPlan.phases
      .flatMap((phase) => phase.items)
      .filter((item) => item.justUnlocked); // You can detect status change

    if (unlockedItems.length > 0) {
      showSuccess(
        `Hoàn thành! ${unlockedItems.length} dịch vụ khác đã được mở khóa.`,
        { autoUnlocked: unlockedItems }
      );
    } else {
      showSuccess("Hoàn thành dịch vụ!");
    }

    // Update UI with fresh data
    setTreatmentPlan(updatedPlan);
  } catch (error) {
    showError(error.message);
  }
}
```

---

### 3. **API 5.9 - Approve Treatment Plan** ✨ ENHANCEMENT

**Endpoint:** `PUT /api/v1/treatment-plans/patients/{patientCode}/approval`

#### What Changed?

- When plan approved, backend analyzes all items and sets appropriate initial status:
  - Items **with prerequisites** → `WAITING_FOR_PREREQUISITE`
  - Items **without prerequisites** → `READY_FOR_BOOKING`

#### Example Response

**Before V21:**

```json
{
  "planId": 1,
  "status": "APPROVED",
  "phases": [
    {
      "items": [
        {
          "itemId": 1,
          "serviceCode": "GEN_EXAM",
          "status": "READY_FOR_BOOKING"
        },
        {
          "itemId": 2,
          "serviceCode": "FILLING_COMP",
          "status": "READY_FOR_BOOKING"
        }
      ]
    }
  ]
}
```

**After V21:**

```json
{
  "planId": 1,
  "status": "APPROVED",
  "phases": [
    {
      "items": [
        {
          "itemId": 1,
          "serviceCode": "GEN_EXAM",
          "status": "READY_FOR_BOOKING" // No prerequisites
        },
        {
          "itemId": 2,
          "serviceCode": "FILLING_COMP",
          "status": "WAITING_FOR_PREREQUISITE", // ✨ NEW STATUS
          "waitingFor": "GEN_EXAM" // Optional: shows what it's waiting for
        }
      ]
    }
  ]
}
```

#### Frontend Action Required

**1. Update Item Status Display:**

```javascript
// Add new status type
const STATUS_CONFIG = {
  PENDING: {
    label: "Chờ duyệt",
    color: "gray",
    icon: "⏳",
  },
  READY_FOR_BOOKING: {
    label: "Sẵn sàng đặt lịch",
    color: "blue",
    icon: "📅",
  },
  // ✨ NEW STATUS
  WAITING_FOR_PREREQUISITE: {
    label: "Chờ dịch vụ tiên quyết",
    color: "orange",
    icon: "🔒",
    tooltip: "Cần hoàn thành dịch vụ tiên quyết trước",
  },
  SCHEDULED: {
    label: "Đã đặt lịch",
    color: "green",
    icon: "✅",
  },
  COMPLETED: {
    label: "Hoàn thành",
    color: "success",
    icon: "✔️",
  },
  CANCELLED: {
    label: "Đã hủy",
    color: "red",
    icon: "❌",
  },
  SKIPPED: {
    label: "Bỏ qua",
    color: "gray",
    icon: "⊘",
  },
};

function renderItemStatus(item) {
  const config = STATUS_CONFIG[item.status];
  return (
    <Badge color={config.color} tooltip={config.tooltip}>
      {config.icon} {config.label}
    </Badge>
  );
}
```

**2. Disable Booking Button for Locked Items:**

```javascript
function renderBookingButton(item) {
  const isLocked = item.status === "WAITING_FOR_PREREQUISITE";

  return (
    <Button
      disabled={isLocked}
      onClick={() => bookAppointment(item)}
      tooltip={
        isLocked ? "Cần hoàn thành dịch vụ tiên quyết trước" : "Đặt lịch hẹn"
      }
    >
      {isLocked ? "🔒 Đang khóa" : "📅 Đặt lịch"}
    </Button>
  );
}
```

**3. Show Prerequisites Information:**

```jsx
function TreatmentPlanItem({ item }) {
  return (
    <div className="treatment-item">
      <h4>{item.serviceName}</h4>
      <StatusBadge status={item.status} />

      {/* ✨ NEW: Show prerequisite info */}
      {item.status === "WAITING_FOR_PREREQUISITE" && item.waitingFor && (
        <Alert type="info" icon="🔒">
          <strong>Chờ dịch vụ tiên quyết:</strong>
          <p>
            Cần hoàn thành <code>{item.waitingFor}</code> trước
          </p>
        </Alert>
      )}

      <ActionButtons item={item} />
    </div>
  );
}
```

---

### 4. **API 6.5 - Get Service Menu (Grouped)** ✨ NEW FEATURE

**Endpoint:** `GET /api/v1/services/grouped`

#### What Changed?

- Each service now includes `bundlesWith` field
- Shows list of service codes that work well together (soft suggestions)

#### Response Structure

**Before V21:**

```json
[
  {
    "category": {
      "categoryId": 1,
      "categoryCode": "GENERAL",
      "categoryName": "Dịch vụ tổng quát"
    },
    "services": [
      {
        "serviceId": 1,
        "serviceCode": "GEN_EXAM",
        "serviceName": "Khám tổng quát",
        "price": 200000,
        "durationMinutes": 30
      }
    ]
  }
]
```

**After V21:**

```json
[
  {
    "category": {
      "categoryId": 1,
      "categoryCode": "GENERAL",
      "categoryName": "Dịch vụ tổng quát"
    },
    "services": [
      {
        "serviceId": 1,
        "serviceCode": "GEN_EXAM",
        "serviceName": "Khám tổng quát",
        "price": 200000,
        "durationMinutes": 30,
        "bundlesWith": ["SCALING_L1"] // ✨ NEW: Bundle suggestions
      },
      {
        "serviceId": 5,
        "serviceCode": "SCALING_L1",
        "serviceName": "Cạo vôi răng - Mức 1",
        "price": 300000,
        "durationMinutes": 45,
        "bundlesWith": ["GEN_EXAM"] // ✨ NEW: Bundle suggestions
      }
    ]
  }
]
```

#### Frontend Action Required

**1. Show Bundle Suggestions in Service Selection:**

```jsx
function ServiceSelector({ selectedServices, onServiceToggle }) {
  const [services, setServices] = useState([]);
  const [suggestions, setSuggestions] = useState([]);

  useEffect(() => {
    // Load services with bundle info
    fetchServicesGrouped().then((data) => setServices(data));
  }, []);

  useEffect(() => {
    // Calculate bundle suggestions based on selected services
    const allSuggestions = selectedServices
      .flatMap((service) => service.bundlesWith || [])
      .filter((code) => !selectedServices.find((s) => s.serviceCode === code));

    setSuggestions([...new Set(allSuggestions)]);
  }, [selectedServices]);

  return (
    <div>
      <h3>Chọn dịch vụ</h3>
      <ServiceList
        services={services}
        selected={selectedServices}
        onToggle={onServiceToggle}
      />

      {/* ✨ NEW: Show bundle suggestions */}
      {suggestions.length > 0 && (
        <div className="bundle-suggestions">
          <h4>💡 Gợi ý combo dịch vụ</h4>
          <p>Các dịch vụ sau thường được đặt cùng nhau:</p>
          <div className="suggestion-chips">
            {suggestions.map((serviceCode) => {
              const service = findServiceByCode(services, serviceCode);
              return (
                <Chip
                  key={serviceCode}
                  label={service.serviceName}
                  icon="➕"
                  onClick={() => onServiceToggle(service)}
                  color="success"
                />
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
```

**2. Smart Service Selection UI:**

```jsx
function ServiceCard({ service, isSelected, onToggle, showBundleBadge }) {
  return (
    <Card
      className={isSelected ? "selected" : ""}
      onClick={() => onToggle(service)}
    >
      <CardHeader>
        <h4>{service.serviceName}</h4>
        {showBundleBadge && service.bundlesWith?.length > 0 && (
          <Badge color="info" tooltip="Có gợi ý combo">
            💡 Combo
          </Badge>
        )}
      </CardHeader>

      <CardBody>
        <p className="price">{formatCurrency(service.price)}</p>
        <p className="duration">⏱️ {service.durationMinutes} phút</p>

        {service.bundlesWith?.length > 0 && (
          <div className="bundle-info">
            <small>Thường đặt cùng: {service.bundlesWith.join(", ")}</small>
          </div>
        )}
      </CardBody>
    </Card>
  );
}
```

---

## 📊 New Data Models

### PlanItemStatus Enum - NEW VALUE

```typescript
enum PlanItemStatus {
  PENDING = "PENDING", // Chờ duyệt
  READY_FOR_BOOKING = "READY_FOR_BOOKING", // Sẵn sàng đặt lịch
  WAITING_FOR_PREREQUISITE = "WAITING_FOR_PREREQUISITE", // ✨ NEW - Chờ dịch vụ tiên quyết
  SCHEDULED = "SCHEDULED", // Đã đặt lịch
  COMPLETED = "COMPLETED", // Hoàn thành
  CANCELLED = "CANCELLED", // Đã hủy
  SKIPPED = "SKIPPED", // Bỏ qua
}
```

### State Transitions - Updated

```
PENDING → READY_FOR_BOOKING (plan approved, no prerequisites)
PENDING → WAITING_FOR_PREREQUISITE (plan approved, has prerequisites)

WAITING_FOR_PREREQUISITE → READY_FOR_BOOKING (prerequisites completed) ✨ AUTO
WAITING_FOR_PREREQUISITE → SKIPPED (user skips)

READY_FOR_BOOKING → SCHEDULED (appointment booked)
READY_FOR_BOOKING → SKIPPED (user skips)

SCHEDULED → COMPLETED (appointment completed)
SCHEDULED → CANCELLED (appointment cancelled)
SCHEDULED → READY_FOR_BOOKING (rescheduling)

COMPLETED → [final state]
CANCELLED → READY_FOR_BOOKING (rebook)
CANCELLED → SKIPPED (skip)
SKIPPED → [final state]
```

---

## 🧪 Testing Scenarios

### Scenario 1: Exclusion Rule (EXCLUDES_SAME_DAY)

**Setup:**

- Rule: EXTRACT_WISDOM_L2 ↔ BLEACH_INOFFICE (cannot book same day)

**Test Steps:**

1. Create appointment for patient
2. Select services: EXTRACT_WISDOM_L2 + BLEACH_INOFFICE
3. Choose same date
4. Submit

**Expected Result:**

```json
Status: 409 Conflict
{
  "errorCode": "CLINICAL_RULE_EXCLUSION_VIOLATED",
  "message": "Không thể đặt lịch các dịch vụ sau cùng ngày: [EXTRACT_WISDOM_L2, BLEACH_INOFFICE]"
}
```

**Frontend Should:**

- Show error message
- Suggest: "Vui lòng chọn ngày khác hoặc bỏ một trong hai dịch vụ"
- Highlight conflicting services in red

---

### Scenario 2: Prerequisite Rule (REQUIRES_PREREQUISITE)

**Setup:**

- Rule: GEN_EXAM → FILLING_COMP (must complete GEN_EXAM first)

**Test Steps:**

1. Patient has NOT completed GEN_EXAM
2. Try to book appointment with FILLING_COMP
3. Submit

**Expected Result:**

```json
Status: 409 Conflict
{
  "errorCode": "CLINICAL_RULE_PREREQUISITE_NOT_MET",
  "message": "Dịch vụ FILLING_COMP yêu cầu hoàn thành GEN_EXAM trước"
}
```

**Frontend Should:**

- Show error message
- Suggest: "Bệnh nhân cần hoàn thành dịch vụ GEN_EXAM trước"
- Show link to book GEN_EXAM first

---

### Scenario 3: Minimum Days Rule (REQUIRES_MIN_DAYS)

**Setup:**

- Rule: EXTRACT_WISDOM_L2 → SURG_CHECKUP (7 days minimum)

**Test Steps:**

1. Patient completed EXTRACT_WISDOM_L2 on 2024-11-10
2. Try to book SURG_CHECKUP on 2024-11-13 (only 3 days later)
3. Submit

**Expected Result:**

```json
Status: 409 Conflict
{
  "errorCode": "CLINICAL_RULE_MIN_DAYS_NOT_MET",
  "message": "Cần chờ tối thiểu 7 ngày sau khi hoàn thành EXTRACT_WISDOM_L2 trước khi đặt SURG_CHECKUP"
}
```

**Frontend Should:**

- Show error message
- Calculate and show earliest available date: 2024-11-17
- Offer date picker with minimum date highlighted

---

### Scenario 4: Bundle Suggestions (BUNDLES_WITH)

**Setup:**

- Rule: GEN_EXAM ↔ SCALING_L1 (work well together)

**Test Steps:**

1. Open service selection
2. Select GEN_EXAM

**Expected Result:**

- UI shows suggestion: "💡 Gợi ý: Thêm SCALING_L1 (thường đặt cùng)"
- Click suggestion → SCALING_L1 added to selection

**Frontend Should:**

- Show non-intrusive suggestion banner/chip
- Allow one-click to add suggested service
- Track if user accepts suggestions (analytics)

---

### Scenario 5: Auto-Unlock Workflow

**Setup:**

- Treatment plan with 2 items:
  - Item 1: GEN_EXAM (READY_FOR_BOOKING)
  - Item 2: FILLING_COMP (WAITING_FOR_PREREQUISITE)

**Test Steps:**

1. Book and complete GEN_EXAM appointment
2. Mark item 1 as COMPLETED
3. Reload treatment plan

**Expected Result:**

- Item 1: status = COMPLETED ✅
- Item 2: status = READY_FOR_BOOKING (auto-unlocked) 🔓

**Frontend Should:**

- Show notification: "Đã mở khóa 1 dịch vụ: FILLING_COMP"
- Highlight newly unlocked item with animation
- Enable booking button for item 2

---

## 🎨 UI/UX Recommendations

### 1. Status Icons & Colors

```javascript
const STATUS_STYLES = {
  PENDING: {
    icon: "⏳",
    color: "#9CA3AF", // Gray
    bgColor: "#F3F4F6",
  },
  READY_FOR_BOOKING: {
    icon: "📅",
    color: "#3B82F6", // Blue
    bgColor: "#EFF6FF",
  },
  WAITING_FOR_PREREQUISITE: {
    icon: "🔒",
    color: "#F59E0B", // Orange/Amber
    bgColor: "#FEF3C7",
    animation: "pulse", // Add subtle pulse animation
  },
  SCHEDULED: {
    icon: "✅",
    color: "#10B981", // Green
    bgColor: "#D1FAE5",
  },
  COMPLETED: {
    icon: "✔️",
    color: "#059669", // Dark green
    bgColor: "#A7F3D0",
  },
  CANCELLED: {
    icon: "❌",
    color: "#EF4444", // Red
    bgColor: "#FEE2E2",
  },
  SKIPPED: {
    icon: "⊘",
    color: "#6B7280", // Gray
    bgColor: "#E5E7EB",
  },
};
```

### 2. Error Message Styling

```jsx
function ClinicalRuleError({ error }) {
  const errorConfig = {
    CLINICAL_RULE_EXCLUSION_VIOLATED: {
      icon: "⚠️",
      color: "warning",
      title: "Xung đột dịch vụ",
      actionText: "Chọn ngày khác",
    },
    CLINICAL_RULE_PREREQUISITE_NOT_MET: {
      icon: "🔒",
      color: "info",
      title: "Thiếu dịch vụ tiên quyết",
      actionText: "Xem yêu cầu",
    },
    CLINICAL_RULE_MIN_DAYS_NOT_MET: {
      icon: "📅",
      color: "warning",
      title: "Chưa đủ thời gian chờ",
      actionText: "Chọn ngày phù hợp",
    },
  };

  const config = errorConfig[error.errorCode];

  return (
    <Alert severity={config.color} icon={config.icon}>
      <AlertTitle>{config.title}</AlertTitle>
      <p>{error.message}</p>
      <Button size="small">{config.actionText}</Button>
    </Alert>
  );
}
```

### 3. Bundle Suggestion Component

```jsx
function BundleSuggestions({ selectedServices, allServices, onAddService }) {
  const suggestions = useMemo(() => {
    const suggested = selectedServices
      .flatMap((s) => s.bundlesWith || [])
      .filter((code) => !selectedServices.find((s) => s.serviceCode === code));

    return [...new Set(suggested)]
      .map((code) => allServices.find((s) => s.serviceCode === code))
      .filter(Boolean);
  }, [selectedServices, allServices]);

  if (suggestions.length === 0) return null;

  return (
    <div className="bundle-suggestions">
      <div className="header">
        <span className="icon">💡</span>
        <h4>Gợi ý combo dịch vụ</h4>
      </div>
      <p className="description">
        Các dịch vụ sau thường được đặt cùng để tối ưu chi phí và thời gian
      </p>
      <div className="suggestions-grid">
        {suggestions.map((service) => (
          <div key={service.serviceCode} className="suggestion-card">
            <div className="service-info">
              <h5>{service.serviceName}</h5>
              <p className="price">{formatCurrency(service.price)}</p>
            </div>
            <Button
              size="small"
              variant="outlined"
              onClick={() => onAddService(service)}
            >
              ➕ Thêm
            </Button>
          </div>
        ))}
      </div>
    </div>
  );
}
```

---

## 📱 Mobile Considerations

### Compact Status Display

```jsx
// Desktop: Full text + icon
<Badge>🔒 Chờ dịch vụ tiên quyết</Badge>

// Mobile: Icon only with tooltip
<IconBadge
  icon="🔒"
  tooltip="Chờ dịch vụ tiên quyết"
  color="warning"
/>
```

### Touch-Friendly Error Messages

```jsx
// Use bottom sheet for error details on mobile
function MobileErrorSheet({ error }) {
  return (
    <BottomSheet open={Boolean(error)}>
      <div className="error-content">
        <div className="error-icon">⚠️</div>
        <h3>Không thể đặt lịch</h3>
        <p>{error.message}</p>
        <Button fullWidth onClick={handleAction}>
          Chọn ngày khác
        </Button>
      </div>
    </BottomSheet>
  );
}
```

---

## 🔧 Migration Checklist

### Phase 1: Preparation (Before Deployment)

- [ ] Update TypeScript interfaces with new status `WAITING_FOR_PREREQUISITE`
- [ ] Add new error codes to error handling constants
- [ ] Update API client to handle `bundlesWith` field
- [ ] Create UI components for:
  - [ ] WAITING_FOR_PREREQUISITE status badge
  - [ ] Clinical rule error alerts
  - [ ] Bundle suggestion cards
- [ ] Update state management (Redux/Context) for new status

### Phase 2: Core Features (Day 1)

- [ ] Update appointment creation error handling
- [ ] Update treatment plan item status display
- [ ] Add logic to disable booking button for locked items
- [ ] Test all error scenarios in dev environment

### Phase 3: Enhanced UX (Week 1)

- [ ] Implement bundle suggestions UI
- [ ] Add auto-refresh after item completion
- [ ] Show unlock notifications
- [ ] Add prerequisite information tooltips

### Phase 4: Polish (Week 2)

- [ ] Add animations for status changes
- [ ] Implement analytics tracking
- [ ] Add user education (tooltips, guides)
- [ ] Mobile optimization

---

## 🐛 Troubleshooting

### Issue 1: Not Seeing Bundle Suggestions

**Symptom:** `bundlesWith` field is empty or undefined

**Solution:**

- Verify you're calling `/api/v1/services/grouped` (internal auth endpoint)
- Check that you have `VIEW_SERVICE` permission
- Verify V21 seed data is loaded in database

### Issue 2: Auto-Unlock Not Working

**Symptom:** Items stay in WAITING_FOR_PREREQUISITE after completing prerequisite

**Solution:**

- After calling update item status API, **refresh entire treatment plan**
- Don't rely on WebSocket/real-time updates (not implemented yet)
- Check that appointment was marked as COMPLETED, not just item

### Issue 3: Error Codes Not Matching

**Symptom:** Generic 409 errors without specific error codes

**Solution:**

- Check backend logs for actual error
- Verify you're running V21 backend version
- Ensure schema migration V21 was applied

---

## 📞 Support & Questions

**Backend Team:** Dental Clinic Backend Team
**Branch:** `feat/BE-501-manage-treatment-plans`
**Swagger Docs:** `http://localhost:8080/swagger-ui.html`

**For questions about:**

- API changes → Check this document first
- New error codes → See "New Error Responses" section
- UI/UX implementation → See "UI/UX Recommendations" section

---

## 📚 Related Documentation

- [Treatment Plan Management Guide](./api-guides/treatment-plan/README.md)
- [Appointment Booking Guide](./api-guides/booking/appointment/README.md)
- [Service Management Guide](./api-guides/service/README.md)

---

**Last Updated:** November 17, 2024
**Version:** V21
**Author:** Backend Team
