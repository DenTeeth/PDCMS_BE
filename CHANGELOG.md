# CHANGELOG - Dental Clinic Management System

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [V21.1] - 2025-11-17

### 🚀 Manager Dashboard - System-Wide Treatment Plan View

Implemented new API to enable managers to view all treatment plans across all patients, resolving frontend blocking issue.

### Added

#### NEW API: Manager View All Treatment Plans

**Problem Solved**: Managers had no way to view all treatment plans system-wide. Existing APIs only supported viewing plans for a specific patient, blocking manager dashboard and approval queue features.

**New Endpoint**: `GET /api/v1/treatment-plans`

- **Permission**: `VIEW_ALL_TREATMENT_PLANS` (NEW - assigned to `ROLE_MANAGER`)
- **Query Parameters**:
  - `approvalStatus` (optional): Filter by DRAFT, PENDING_REVIEW, APPROVED, REJECTED
  - `status` (optional): Filter by PENDING, ACTIVE, COMPLETED, CANCELLED
  - `doctorEmployeeCode` (optional): Filter by doctor who created plan
  - `page`, `size`, `sort`: Standard pagination
- **Response**: Paginated list of lightweight treatment plan summaries
  - Patient summary: code, name, phone
  - Doctor summary: employee code, name
  - Plan info: code, name, status, approval status
  - Financial: total price, final cost
  - Dates: start, end, created, approved
- **Performance**: Uses LEFT JOIN FETCH to avoid N+1 queries
- **Use Cases**:
  - Manager dashboard showing all plans
  - Approval queue (filter by PENDING_REVIEW)
  - Doctor performance tracking
  - Cross-patient reporting

**Files Created**:
- `dto/response/TreatmentPlanSummaryDTO.java` - Lightweight response DTO (123 lines)
- `service/TreatmentPlanListService.java` - Service with permission check (136 lines)
- `migration/V21_add_view_all_treatment_plans_permission.sql` - Database migration script

**Files Modified**:
- `PatientTreatmentPlanRepository.java` - Added `findAllWithFilters()` query with JOIN FETCH
- `TreatmentPlanController.java` - Added `listAllTreatmentPlans()` endpoint with Swagger docs
- `AuthoritiesConstants.java` - Added `VIEW_ALL_TREATMENT_PLANS` constant
- `dental-clinic-seed-data.sql` - Added permission (display_order=266) and role assignment

**Database Changes**:
```sql
-- New permission
INSERT INTO permissions (permission_id, permission_name, module, description, display_order)
VALUES ('VIEW_ALL_TREATMENT_PLANS', 'VIEW_ALL_TREATMENT_PLANS', 'TREATMENT_PLAN', 
        'Xem TẤT CẢ phác đồ điều trị TOÀN HỆ THỐNG (Quản lý - Manager Dashboard)', 266);

-- Assign to manager role
INSERT INTO role_permissions (role_id, permission_id)
VALUES ('ROLE_MANAGER', 'VIEW_ALL_TREATMENT_PLANS');
```

**Testing**: See `FE_NEW_APIS_TESTING_GUIDE.md` for comprehensive test cases

**Documentation**: See `FE_ISSUES_BACKEND_RESPONSE.md` for detailed analysis and implementation plan

---

## [V22] - 2025-11-17

### 🚀 Frontend-Requested APIs - Treatment Plan Workflow Enhancement

Implemented 2 critical missing APIs requested by frontend team to complete treatment plan approval workflow.

### Added

#### API 5.12: Submit Treatment Plan for Review (CRITICAL)

**Problem Solved**: Doctors could not submit DRAFT plans for manager approval, blocking entire approval workflow.

**New Endpoint**: `PATCH /api/v1/patient-treatment-plans/{planCode}/submit-for-review`

- **Permission**: `CREATE_TREATMENT_PLAN` or `UPDATE_TREATMENT_PLAN` (Doctors)
- **Request**: Optional `notes` field (max 1000 chars)
- **Validation**:
  - Plan must exist (404)
  - Plan must be in DRAFT status (409)
  - Plan must have at least 1 phase and 1 item (400)
- **Business Logic**:
  - Changes status: `DRAFT` → `PENDING_REVIEW`
  - Records submitter (current employee)
  - Creates audit log with action: `SUBMITTED_FOR_REVIEW`
- **Response**: Updated plan detail with approval metadata

**Files Created**:

- `SubmitForReviewRequest.java` - Request DTO

**Files Modified**:

- `TreatmentPlanApprovalService.java` - Added `submitForReview()` method
- `TreatmentPlanController.java` - Added submit endpoint with Swagger docs

#### API 6.6: List Treatment Plan Templates (MEDIUM)

**Problem Solved**: Frontend needed scalable way to list templates with filters (only API 5.8 existed for single template).

**New Endpoint**: `GET /api/v1/treatment-plan-templates`

- **Permission**: `CREATE_TREATMENT_PLAN` (same as API 5.8)
- **Query Parameters** (all optional):
  - `isActive` (Boolean) - Filter by active status
  - `specializationId` (Integer) - Filter by specialization
  - `page`, `size`, `sort` - Pagination support
- **Response**: Paginated list of template summaries (lightweight, no phases/services)
- **Use Cases**:
  - Dropdown for template selection
  - Admin template management
  - Filter by department/specialization

**Files Created**:

- `TemplateSummaryDTO.java` - Lightweight response DTO

**Files Modified**:

- `TreatmentPlanTemplateRepository.java` - Added `findAllWithFilters()` query
- `TreatmentPlanTemplateService.java` - Added `getAllTemplates()` method
- `TreatmentPlanController.java` - Added list endpoint with Swagger docs

### Technical Details

#### Audit Trail Enhancement

- All submit actions logged in `plan_audit_logs` table
- Action type: `SUBMITTED_FOR_REVIEW`
- Records: old_status, new_status, performer, notes, timestamp

#### Query Optimization

- `findAllWithFilters()` uses `LEFT JOIN FETCH` for specialization
- Efficient filtering with nullable parameters
- Page-based pagination support

### Documentation

- **Implementation Guide**: `FE_NEW_APIS_IMPLEMENTED.md`
  - Complete API specifications
  - Request/response examples
  - Testing guide with cURL commands
  - Frontend integration workflow
  - Error handling scenarios

### Build Status

```bash
[INFO] BUILD SUCCESS
[INFO] Total time: 01:10 min
[INFO] Compiling 489 source files ✅
```

---

## [V21] - 2024-11-17

### 🎯 Clinical Rules Engine - Automated Service Dependency Validation

Complete implementation of intelligent clinical rules system for service dependencies and workflow automation.

### Added

#### Database Layer

- New ENUM type `dependency_rule_type` with 4 values:
  - `REQUIRES_PREREQUISITE` - Service A must be completed before B
  - `REQUIRES_MIN_DAYS` - Minimum days required between services
  - `EXCLUDES_SAME_DAY` - Services cannot be booked same day
  - `BUNDLES_WITH` - Soft suggestion for service combos
- New table `service_dependencies` with constraints and indexes
- 6 seed data examples covering all rule types

#### Domain Layer

- `DependencyRuleType` enum
- `ServiceDependency` entity with helper methods
- New `PlanItemStatus` value: `WAITING_FOR_PREREQUISITE`

#### Repository Layer

- `ServiceDependencyRepository` with 7 custom @Query methods
- `AppointmentRepository.findCompletedAppointmentsByPatientId()` method

#### Service Layer

- `ClinicalRulesValidationService` - Core validation engine
  - `validateAppointmentServices()` - Main validation entry point
  - `validateNoExclusionConflicts()` - Same-day exclusion validation
  - `validatePrerequisites()` - Prerequisite completion check
  - `validateMinimumDays()` - Minimum days calculation
  - `getBundleSuggestions()` - Bundle recommendations
  - `hasPrerequisites()` - Check if service has prerequisites
  - `getServicesUnlockedBy()` - Get dependent services

#### API Integration

- **API 3.2** - Appointment Creation

  - STEP 7B: Clinical rules validation before appointment creation
  - New error codes: `CLINICAL_RULE_EXCLUSION_VIOLATED`, `CLINICAL_RULE_PREREQUISITE_NOT_MET`, `CLINICAL_RULE_MIN_DAYS_NOT_MET`

- **API 5.9** - Treatment Plan Approval

  - STEP 8B: Auto-set item statuses based on prerequisites
  - Items with prerequisites → `WAITING_FOR_PREREQUISITE`
  - Items without prerequisites → `READY_FOR_BOOKING`

- **API 5.6** - Update Treatment Plan Item Status

  - STEP 6B: Auto-unlock dependent items when prerequisite completed
  - Updated `STATE_TRANSITIONS` map with new status transitions

- **API 6.5** - Get Service Menu (Grouped)
  - Added `bundlesWith` field to `InternalServiceDTO`
  - Returns bundle suggestions for each service

#### Documentation

- `V21_CLINICAL_RULES_FRONTEND_GUIDE.md` - Complete frontend integration guide
- `V21_QUICK_REFERENCE.md` - Quick reference for essential changes
- `V21_TECHNICAL_SUMMARY.md` - Backend implementation details
- Updated `docs/README.md` with V21 section

### Changed

#### Breaking Changes

- **API 3.2** now throws specific error codes for clinical rule violations (see Migration Guide)
- **API 5.6** automatically refreshes dependent items after completion (frontend must reload plan)
- **API 5.9** sets initial item statuses differently based on prerequisites

#### Non-Breaking Changes

- `InternalServiceDTO` schema extended with `bundlesWith` field (backward compatible, nullable)
- `PlanItemStatus` enum extended (existing statuses unchanged)

### Technical Details

#### Performance

- Added 3 database indexes for `service_dependencies` table
- N+1 query prevention with Map-based lookups
- Lazy loading for service dependencies

#### Security

- All validation enforced server-side
- Frontend checks are convenience only (not security)
- Foreign key constraints ensure data integrity

### Migration Guide

**For Backend:**

1. Run V21 schema migration
2. Load seed data (6 clinical rules)
3. Restart application
4. Verify with test cases

**For Frontend:**

1. Add `WAITING_FOR_PREREQUISITE` status to UI
2. Handle 3 new error codes in appointment creation
3. Refresh treatment plan after completing items
4. Display bundle suggestions in service menu
5. See [V21_CLINICAL_RULES_FRONTEND_GUIDE.md](docs/V21_CLINICAL_RULES_FRONTEND_GUIDE.md)

### Testing

#### Test Scenarios Required

1. EXCLUDES_SAME_DAY: Book conflicting services same day → Should block
2. REQUIRES_PREREQUISITE: Book without prerequisite → Should block
3. REQUIRES_MIN_DAYS: Book too soon after prerequisite → Should block
4. BUNDLES_WITH: View service menu → Should show suggestions
5. Auto-unlock: Complete prerequisite → Dependent items unlock

See [V21_QUICK_REFERENCE.md](docs/V21_QUICK_REFERENCE.md) for detailed test cases.

---

## [V20] - 2024-XX-XX

### Added

- Treatment Plan Management APIs (5.1 - 5.11)
- Phase-based treatment planning
- Item status workflow management

### Changed

- Enhanced appointment booking with treatment plan integration

### Fixed

- API 5.10 & 5.11 frontend UX issues (409 Conflict errors)

---

## [V19] - Previous

(Historical changelog entries...)

---

## Legend

- 🎯 Major Feature
- ✨ Enhancement
- 🐛 Bug Fix
- 🔒 Security
- 📚 Documentation
- 🚀 Performance
- ⚠️ Breaking Change
- 🔧 Configuration

---

**For full documentation, see [docs/README.md](docs/README.md)**
