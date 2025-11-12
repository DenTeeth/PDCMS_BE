# Treatment Plan API Documentation

**Module**: Treatment Plan Management  
**Version**: V20  
**Last Updated**: 2025-11-12  
**Status**: ✅ Production Ready

---

## 📋 Overview

The Treatment Plan module manages long-term treatment contracts for patients (e.g., orthodontics, implants). It provides a complete workflow from template creation to patient plan management with booking integration.

### Key Capabilities

✅ **Template-based Plans**: Standardize common treatments  
✅ **Custom Plans**: Create unique treatment plans from scratch  
✅ **Progress Tracking**: Monitor phase and item completion  
✅ **RBAC**: Role-based access (Admin, Doctor, Patient)  
✅ **Appointment Integration**: Direct booking from plan items  
✅ **Approval Workflow**: Price validation and manager approval  

---

## 📚 API Documentation

### Core APIs

| API | Title | File | Status |
|-----|-------|------|--------|
| **5.1** | Get Treatment Plans by Patient | [`API_5.1_5.2_Get_Treatment_Plans.md`](./API_5.1_5.2_Get_Treatment_Plans.md) | ✅ Production |
| **5.2** | Get Treatment Plan Details | [`API_5.1_5.2_Get_Treatment_Plans.md`](./API_5.1_5.2_Get_Treatment_Plans.md) | ✅ Production |
| **5.3** | Create Plan from Template | [`API_5.3_Create_From_Template.md`](./API_5.3_Create_From_Template.md) | ✅ Production |
| **5.4** | Create Custom Plan | [`API_5.4_Create_Custom_Plan.md`](./API_5.4_Create_Custom_Plan.md) | ✅ Production |
| **5.5** | Get All Plans with RBAC | [`API_5.5_Get_All_With_RBAC.md`](./API_5.5_Get_All_With_RBAC.md) | ✅ Production |

---

## 🚀 Quick Start

### 1. View Patient's Plans

Get all treatment plans for a specific patient with progress tracking:

```bash
GET /api/v1/patients/{patientCode}/treatment-plans
```

**Use Cases**:
- Patient dashboard showing treatment progress
- Doctor reviewing patient history
- Receptionist checking active plans

**Read more**: [API 5.1 & 5.2 Documentation](./API_5.1_5.2_Get_Treatment_Plans.md)

---

### 2. Create Plan from Template

Apply a pre-defined template to a patient (most common workflow):

```bash
POST /api/v1/patient-treatment-plans/from-template

{
  "patientCode": "BN-1001",
  "templateId": 1,
  "startDate": "2025-11-01",
  "paymentType": "INSTALLMENT"
}
```

**Use Cases**:
- Standard treatments (orthodontics, implants)
- Fast plan creation with pre-configured phases
- Consistent pricing and timelines

**Features**:
- ✅ Automatic quantity expansion (24 adjustments → 24 items)
- ✅ Price customization with validation
- ✅ Phase and item auto-generation

**Read more**: [API 5.3 Documentation](./API_5.3_Create_From_Template.md)

---

### 3. Create Custom Plan

Create a unique treatment plan from scratch without templates:

```bash
POST /api/v1/patient-treatment-plans/custom

{
  "patientCode": "BN-1001",
  "planName": "Custom Orthodontics + Implant Combo",
  "startDate": "2025-11-01",
  "phases": [
    {
      "phaseNumber": 1,
      "phaseName": "Phase 1: Preparation",
      "estimatedDurationDays": 14,
      "items": [
        {
          "serviceCode": "EXAM-GEN",
          "quantity": 1,
          "price": 500000
        }
      ]
    }
  ],
  "totalPrice": 45000000,
  "discountAmount": 5000000,
  "paymentType": "INSTALLMENT"
}
```

**Use Cases**:
- Complex multi-service treatments
- Custom pricing negotiations
- Treatments not covered by templates

**Features**:
- ✅ Full flexibility (any combination of services)
- ✅ Quantity expansion (qty 5 → 5 separate items)
- ✅ Price override with 50%-150% validation
- ✅ Approval workflow (DRAFT status)

**Read more**: [API 5.4 Documentation](./API_5.4_Create_Custom_Plan.md)

---

### 4. Get All Plans (Admin/Doctor Dashboard)

Unified endpoint with smart RBAC filtering:

```bash
GET /api/v1/patient-treatment-plans
  ?status=IN_PROGRESS
  &approvalStatus=APPROVED
  &startDateFrom=2024-01-01
  &searchTerm=orthodontics
  &page=0&size=20
```

**Use Cases**:
- **Admin Dashboard**: View all plans, filter by doctor/patient
- **Doctor Dashboard**: Auto-filtered to show only own plans
- **Patient Dashboard**: Auto-filtered to show only own plans

**Advanced Filters**:
- Status: PENDING, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD
- Approval: DRAFT, PENDING_REVIEW, APPROVED, REJECTED
- Date ranges: startDate, createdAt
- Search: Plan name, patient name (case-insensitive)
- Admin-only: doctorEmployeeCode, patientCode

**Read more**: [API 5.5 Documentation](./API_5.5_Get_All_With_RBAC.md)

---

## 🗂️ Database Schema

### Core Tables

```
treatment_plan_templates          → Template definitions (reusable)
├── template_phases               → Template phases
    └── template_phase_services   → Services in each phase (with quantity)

patient_treatment_plans           → Patient contracts (instances)
├── patient_plan_phases           → Patient-specific phases
    └── patient_plan_items        → Individual work items (expanded from quantity)
        └── appointment_plan_items → Link to appointments (N-N)
```

### Key Relationships

```sql
-- Template to Patient Plan (many-to-many)
patient_treatment_plans.source_template_id → treatment_plan_templates.template_id

-- Plan to Patient (many-to-one)
patient_treatment_plans.patient_id → patients.patient_id

-- Plan to Doctor (many-to-one)
patient_treatment_plans.created_by → employees.employee_id

-- Items to Appointments (many-to-many)
appointment_plan_items → patient_plan_items.item_id, appointments.appointment_id
```

---

## 🔐 Permissions

| Permission                | Role             | Description                         |
|---------------------------|------------------|-------------------------------------|
| `VIEW_TREATMENT_PLAN_ALL` | Staff, Admin     | View all patient treatment plans    |
| `VIEW_TREATMENT_PLAN_OWN` | Patient          | View only own plans                 |
| `CREATE_TREATMENT_PLAN`   | Doctor, Admin    | Create new treatment plans          |
| `UPDATE_TREATMENT_PLAN`   | Doctor, Admin    | Update plans (items, status)        |
| `APPROVE_TREATMENT_PLAN`  | Manager          | Approve custom plans (future)       |

---

## 📊 Status Workflow

### Plan Item Status

```
PENDING → READY_FOR_BOOKING → SCHEDULED → IN_PROGRESS → COMPLETED
   ↓                                          ↓
CANCELLED                                 CANCELLED
```

**Status Meanings**:
- `PENDING`: Awaiting approval (custom plans only)
- `READY_FOR_BOOKING`: Can schedule appointment
- `SCHEDULED`: Appointment booked
- `IN_PROGRESS`: Appointment started
- `COMPLETED`: Finished
- `CANCELLED`: Plan cancelled

### Approval Status (V19)

```
DRAFT → PENDING_REVIEW → APPROVED
   ↓                        ↓
REJECTED                  REJECTED
```

**Approval Meanings**:
- `DRAFT`: Just created, not submitted
- `PENDING_REVIEW`: Waiting for manager approval
- `APPROVED`: Can proceed with treatment
- `REJECTED`: Needs revision (see rejection_reason)

---

## 🧪 Testing Quick Reference

### Test Scenario: End-to-End Workflow

**1. Create Plan from Template**
```bash
POST /api/v1/patient-treatment-plans/from-template
# Response: planId = 1, plan_code = "PLAN-20251101-001"
```

**2. View Plan Details**
```bash
GET /api/v1/patients/BN-1001/treatment-plans/PLAN-20251101-001
# Verify: 31 items with status = PENDING → READY_FOR_BOOKING
```

**3. Get Bookable Items**
```bash
GET /api/v1/patient-treatment-plans/1/bookable-items
# Response: Items with status = READY_FOR_BOOKING
```

**4. Book Appointment with Items**
```bash
POST /api/v1/appointments
{
  "patientCode": "BN-1001",
  "patientPlanItemIds": [101, 102],  # Book 2 items
  "employeeCode": "EMP-1",
  "roomCode": "RM-01",
  "appointmentStartTime": "2025-11-15T14:00:00"
}
# Items 101, 102 → status changes to SCHEDULED
```

**5. Complete Appointment**
```bash
PATCH /api/v1/appointments/{appointmentId}/complete
# Items 101, 102 → status changes to COMPLETED
```

**6. Check Progress**
```bash
GET /api/v1/patients/BN-1001/treatment-plans
# Response: progress.completedItems = 2, progress.percentageComplete = 6.45%
```

---

## 🔗 Integration Points

### Booking Module (BE/#4)

**API 3.2 - Create Appointment** supports Treatment Plan items:

```json
{
  "patientCode": "BN-1001",
  "patientPlanItemIds": [101, 102],  // ✅ Luồng 2: Treatment Plan Booking
  "employeeCode": "EMP-1",
  "roomCode": "RM-01",
  "appointmentStartTime": "2025-11-15T14:00:00"
}
```

**XOR Rule**: Must provide EITHER `serviceCodes` OR `patientPlanItemIds`, not both.

**Status Update**: When appointment created, items change from `READY_FOR_BOOKING` → `SCHEDULED`.

---

## 📈 Performance Optimization

**Query Optimization**:
- `JOIN FETCH` to prevent N+1 queries
- Indexes on: plan_code, patient_id, status, created_by, approval_status
- Lazy loading for phases/items (only load when needed)

**Expected Response Times**:
- API 5.1 (List): < 100ms (1 query)
- API 5.2 (Details): < 200ms (3 queries with JOINs)
- API 5.3 (Create from Template): < 500ms (batch insert)
- API 5.4 (Create Custom): < 700ms (validation + batch insert)
- API 5.5 (Get All): < 150ms (1 query with Specification)

---

## 🐛 Common Issues & Solutions

### Issue 1: Duplicate plan_code

**Error**: `409 CONFLICT - Plan code already exists`

**Cause**: Multiple plans created on same date

**Solution**: Plan code uses auto-increment suffix (PLAN-20251101-001, 002, 003...)

---

### Issue 2: Items not READY_FOR_BOOKING

**Error**: `400 BAD_REQUEST - PLAN_ITEMS_NOT_READY`

**Cause**: Trying to book items with status ≠ READY_FOR_BOOKING

**Solution**: 
1. Check item status: `GET .../treatment-plans/{planId}`
2. Wait for approval (custom plans)
3. Ensure items not already SCHEDULED

---

### Issue 3: Price Override Rejected

**Error**: `400 BAD_REQUEST - PRICE_OUT_OF_RANGE`

**Cause**: Custom price outside 50%-150% of service default price

**Solution**:
```java
// If service price = 1,000,000đ
// Allowed range: 500,000đ - 1,500,000đ
```

Use prices within validation range or request manager override.

---

### Issue 4: Patient Permission Denied

**Error**: `403 FORBIDDEN`

**Cause**: Patient trying to access another patient's plans

**Solution**: Ensure JWT `account_id` matches patient's `account_id`

---

## 📝 Migration History

| Version | File | Description | Date |
|---------|------|-------------|------|
| **V19** | `V19__add_approval_workflow_and_phase_duration.sql` | Added approval workflow (approval_status, approved_by, etc.) and estimated_duration_days | 2025-01-12 |
| **V20** | Consolidated in `dental-clinic-seed-data.sql` | Added 7 test treatment plans with comprehensive status coverage | 2025-01-12 |

---

## 📚 Additional Resources

### Seed Data
- **Location**: `src/main/resources/db/dental-clinic-seed-data.sql`
- **Test Plans**: 10 plans with various statuses (PENDING, IN_PROGRESS, COMPLETED)
- **Coverage**: 5 patients, 3 doctors, date range 2024-05 to 2025-11

### SQL Consolidation
- **Summary**: [`V19_V20_CONSOLIDATION_SUMMARY.md`](../../../V19_V20_CONSOLIDATION_SUMMARY.md) (root)
- **Test Script**: `test-consolidation.bat` / `test-consolidation.sh`

### Architecture
- **Cron Jobs**: [`docs/architecture/CRON_JOB_P8_ARCHITECTURE.md`](../../architecture/CRON_JOB_P8_ARCHITECTURE.md)

---

## 🎯 Future Enhancements

### Planned Features (V21+)

- [ ] **API 5.6**: Update Treatment Plan (add/remove items)
- [ ] **API 5.7**: Cancel Treatment Plan (with refund logic)
- [ ] **API 5.8**: Get Plan Progress Timeline (Gantt chart data)
- [ ] **API 5.9**: Approve/Reject Custom Plan (Manager workflow)
- [ ] **API 5.10**: Clone Existing Plan (reuse for repeat treatments)

### Integration Enhancements

- [ ] **Payment Integration**: Track payments by phase
- [ ] **Notification System**: Alert patient when items ready for booking
- [ ] **Recommendation Engine**: Suggest next appointment date
- [ ] **Template Versioning**: Update templates without affecting existing plans

---

## 📞 Support

For questions or issues:
1. Check this documentation first
2. Review API-specific documentation (5.1-5.5)
3. Check error codes in Error Handling sections
4. Contact development team with:
   - API endpoint
   - Request payload
   - Error response
   - Expected behavior

---

**Last Updated**: 2025-11-12  
**Module Owner**: Dental Clinic Development Team  
**Repository**: PDCMS_BE (Branch: feat/BE-501-manage-treatment-plans)
