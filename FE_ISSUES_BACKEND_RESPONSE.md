# 🔍 Frontend Issues - Backend Response

**Date**: November 17, 2025
**Branch**: `feat/BE-501-manage-treatment-plans`
**Status**: 2/3 Issues Resolved, 1 Issue Needs Clarification

---

## 📋 Summary

| Issue                                            | Status                         | Solution                                                               |
| ------------------------------------------------ | ------------------------------ | ---------------------------------------------------------------------- |
| **Issue 1**: Employee Code Missing in JWT        | ✅ **ALREADY RESOLVED**        | `employee_code` claim already exists in JWT token (implemented in v20) |
| **Issue 2**: Zero Price Service Validation       | ⏳ **NEEDS BUSINESS DECISION** | Awaiting decision: Option 1 or Option 2                                |
| **Issue 4**: Manager Permission - View All Plans | 🔴 **IN PROGRESS**             | Will implement permission fix                                          |

---

## ✅ ISSUE 1: Employee Code in JWT Token - ALREADY RESOLVED

### Status: ✅ **NO ACTION NEEDED**

**Good news**: `employee_code` claim **đã có sẵn** trong JWT token từ version 20!

### Implementation Details

**File**: `SecurityUtil.java` (line 52-80)

```java
public String createAccessToken(String username, List<String> roles, List<String> permissions,
        Integer accountId, String patientCode, String employeeCode) {

    // ... other claims ...

    // Add employeeCode if present (FE Issue 3.3 fix)
    if (employeeCode != null) {
        claimsBuilder.claim("employee_code", employeeCode);
    }

    // Returns JWT with employee_code claim
}
```

**Token Payload Example**:

```json
{
  "sub": "bacsi1",
  "iat": 1234567890,
  "exp": 1234571490,
  "roles": ["ROLE_DENTIST"],
  "permissions": ["CREATE_TREATMENT_PLAN", "UPDATE_TREATMENT_PLAN"],
  "account_id": 456,
  "patient_code": null,
  "employee_code": "EMP001" // ← ĐÃ CÓ SẴN
}
```

### How to Extract in Frontend

**JavaScript/TypeScript**:

```typescript
import { jwtDecode } from "jwt-decode";

interface JwtPayload {
  sub: string;
  employee_code?: string;
  employee_id?: number;
  roles: string[];
  permissions: string[];
}

const token = localStorage.getItem("authToken");
const decoded = jwtDecode<JwtPayload>(token);

console.log("Employee Code:", decoded.employee_code); // ← Extract this
```

### Test Verification

**Step 1**: Login as doctor

```bash
POST /api/v1/auth/login
{
  "username": "bacsi1",
  "password": "your_password"
}
```

**Step 2**: Decode the returned token

- Use https://jwt.io to decode
- Look for `employee_code` claim
- Should see value like "EMP001"

### Frontend Implementation

**Auto-fill Doctor Example**:

```typescript
// In CreateTreatmentPlanModal.tsx
useEffect(() => {
  const token = localStorage.getItem("authToken");
  if (token) {
    const decoded = jwtDecode<JwtPayload>(token);

    // Check if user is doctor
    if (decoded.roles?.includes("ROLE_DENTIST")) {
      // Auto-fill with current doctor
      setDoctorEmployeeCode(decoded.employee_code);
      setIsReadOnly(true); // Make field read-only
    }
  }
}, []);
```

---

## ⏳ ISSUE 2: Zero Price Service Validation - NEEDS BUSINESS DECISION

### Status: ⏳ **AWAITING BUSINESS DECISION**

### Problem Analysis

**Current Validation Logic** (in `CustomTreatmentPlanService.java`):

```java
private void validatePriceOverride(BigDecimal requestPrice, BigDecimal servicePrice, String serviceCode) {
    BigDecimal minPrice = servicePrice.multiply(new BigDecimal("0.5")); // 50%
    BigDecimal maxPrice = servicePrice.multiply(new BigDecimal("1.5")); // 150%

    // If servicePrice = 0:
    // minPrice = 0 * 0.5 = 0
    // maxPrice = 0 * 1.5 = 0
    // Range = [0, 0] ← Problem: Cannot enter any positive price!

    if (requestPrice.compareTo(minPrice) < 0 || requestPrice.compareTo(maxPrice) > 0) {
        throw new BadRequestAlertException("Price out of range");
    }
}
```

**Conflict Scenario**:

1. Service "Lấy dấu Implant" has `price = 0` in database
2. User tries to create plan with this service
3. Must set price > 0 (DTO validation: `@DecimalMin("0.01")`)
4. But validation logic rejects any price > 0 because range is [0, 0]
5. **Result**: Cannot use this service in treatment plans

### 🔍 Database Check Needed

**Query to find zero-price services**:

```sql
SELECT service_id, service_code, service_name, price, is_active
FROM dental_services
WHERE price = 0 OR price IS NULL
ORDER BY service_code;
```

**Questions for Business Team**:

1. Có services nào có price = 0 không?
2. Nếu có, có bao nhiêu services?
3. Các services này có đang được sử dụng không?
4. Use case cho services miễn phí là gì? (Consultation? Follow-up?)

### 💡 Proposed Solutions

#### Option 1: Không cho phép service có price = 0 (Recommended)

**Philosophy**: "Every service has value, minimum price must be > 0"

**Implementation**:

**Step 1**: Add database constraint

```sql
-- Add CHECK constraint
ALTER TABLE dental_services
ADD CONSTRAINT chk_price_positive
CHECK (price > 0);
```

**Step 2**: Update existing zero-price services

```sql
-- Example: Set minimum price 1,000 VND
UPDATE dental_services
SET price = 1000,
    updated_at = NOW()
WHERE price = 0 OR price IS NULL;
```

**Step 3**: Add entity validation (optional backup)

```java
@Entity
@Table(name = "dental_services")
public class DentalService {

    @Column(name = "price", nullable = false)
    @Min(value = 1, message = "Service price must be at least 1 VND")
    private BigDecimal price;
}
```

**Pros**:

- ✅ Simple, clear business rule
- ✅ Prevents validation conflicts
- ✅ Ensures data consistency
- ✅ Aligns with real-world pricing (every service has cost)

**Cons**:

- ❌ Need to update existing data
- ❌ May conflict with business need for "free" services

#### Option 2: Allow price = 0 with special handling

**Philosophy**: "Some services are free, but when used in plans must have assigned value"

**Implementation**:

**Update validation logic**:

```java
private void validatePriceOverride(BigDecimal requestPrice, BigDecimal servicePrice, String serviceCode) {
    // Special case: If service has default price = 0
    if (servicePrice.compareTo(BigDecimal.ZERO) == 0) {
        // For zero-price services:
        // 1. Request price must still be > 0 (cannot add free items to plan)
        if (requestPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestAlertException(
                "Service with zero default price must be assigned a value when added to plan",
                "TreatmentPlan",
                "zeroPrice NotAllowed");
        }

        // 2. No upper limit for zero-price services (or set reasonable max like 10M VND)
        BigDecimal maxAllowed = new BigDecimal("10000000"); // 10M VND limit
        if (requestPrice.compareTo(maxAllowed) > 0) {
            throw new BadRequestAlertException(
                String.format("Price exceeds maximum allowed (%s VND)", maxAllowed),
                "TreatmentPlan",
                "priceExceedsMax");
        }

        log.info("Zero-price service {} assigned value: {}", serviceCode, requestPrice);
        return; // Skip normal ±50% validation
    }

    // Normal case: Service has price > 0, apply ±50% rule
    BigDecimal minPrice = servicePrice.multiply(new BigDecimal("0.5"));
    BigDecimal maxPrice = servicePrice.multiply(new BigDecimal("1.5"));

    if (requestPrice.compareTo(minPrice) < 0 || requestPrice.compareTo(maxPrice) > 0) {
        throw new BadRequestAlertException(
            String.format("Price out of range [%s - %s]", minPrice, maxPrice),
            "TreatmentPlan",
            "priceOutOfRange");
    }
}
```

**Frontend validation update**:

```typescript
const validatePrice = (servicePrice: number, requestPrice: number): boolean => {
  if (servicePrice === 0) {
    // Zero-price service: Must enter value > 0, max 10M
    if (requestPrice <= 0) {
      alert("Dịch vụ này cần được định giá khi thêm vào lộ trình");
      return false;
    }
    if (requestPrice > 10000000) {
      alert("Giá vượt quá giới hạn cho phép (10,000,000 VND)");
      return false;
    }
    return true;
  }

  // Normal service: Apply ±50% rule
  const minPrice = servicePrice * 0.5;
  const maxPrice = servicePrice * 1.5;
  return requestPrice >= minPrice && requestPrice <= maxPrice;
};
```

**Pros**:

- ✅ Flexible for business needs
- ✅ Allows "free" services in service catalog
- ✅ Forces value assignment when used in plans
- ✅ No data migration needed

**Cons**:

- ❌ More complex logic
- ❌ Special case handling
- ❌ Potential confusion (why is "free" service priced in plan?)

### 🎯 Recommendation

**I recommend Option 1** for these reasons:

1. **Business Logic**: In a dental clinic, every service has operational cost:

   - Materials cost
   - Labor cost
   - Equipment depreciation
   - Even "free consultation" has cost

2. **Simplicity**: Simpler code, easier to maintain

3. **Data Integrity**: Prevents confusion about service pricing

4. **Approval Workflow**: Current approval logic already requires price > 0:

   ```java
   // In TreatmentPlanApprovalService.java
   if (item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
       throw new BadRequestException("Cannot approve: Item has price 0đ");
   }
   ```

5. **Industry Standard**: Most healthcare systems have minimum pricing

### 🔧 Implementation Plan (if Option 1 chosen)

**Phase 1: Data Migration** (5 minutes)

```sql
-- Backup current data
CREATE TABLE dental_services_backup AS SELECT * FROM dental_services;

-- Find zero-price services
SELECT * FROM dental_services WHERE price = 0 OR price IS NULL;

-- Update to minimum price (adjust based on business input)
UPDATE dental_services
SET price =
    CASE
        WHEN service_code LIKE 'CONSULT%' THEN 50000   -- Consultation: 50k
        WHEN service_code LIKE 'FOLLOWUP%' THEN 0      -- Keep follow-up free (if needed)
        ELSE 1000                                       -- Others: 1k minimum
    END,
    updated_at = NOW()
WHERE price = 0 OR price IS NULL;
```

**Phase 2: Add Constraint** (1 minute)

```sql
ALTER TABLE dental_services
ADD CONSTRAINT chk_price_positive CHECK (price > 0);
```

**Phase 3: Test** (10 minutes)

- Try creating plan with updated services
- Verify price validation works
- Test frontend form

**Total Time**: ~20 minutes

---

## 🔴 ISSUE 4: Manager Permission - View All Treatment Plans

### Status: 🔴 **IN PROGRESS - Will Fix**

### Problem

Manager không có quyền xem danh sách tất cả treatment plans.

**Current**: API `/api/v1/patient/{patientCode}/treatment-plans` requires patient code
**Needed**: API to list ALL treatment plans (for manager oversight)

### Root Cause Analysis

**Missing API**: No endpoint exists to list ALL treatment plans across all patients

**Current APIs**:

1. ✅ `/api/v1/patient/{patientCode}/treatment-plans` - List plans for ONE patient
2. ✅ `/api/v1/patient/{patientCode}/treatment-plans/{planCode}` - Get ONE plan detail
3. ❌ `/api/v1/treatment-plans` - **MISSING** - List ALL plans (needed for manager)

### 💡 Solution: Implement API to List All Treatment Plans

#### New API Specification

**Endpoint**: `GET /api/v1/treatment-plans`

**Method**: GET

**Permission**: `VIEW_ALL_TREATMENT_PLANS` (new permission for managers)

**Query Parameters**:
| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `approvalStatus` | String | Filter by approval status | `DRAFT`, `PENDING_REVIEW`, `APPROVED` |
| `status` | String | Filter by plan status | `PENDING`, `ACTIVE`, `COMPLETED` |
| `doctorEmployeeCode` | String | Filter by doctor | `EMP001` |
| `page` | Integer | Page number (0-based) | `0` |
| `size` | Integer | Items per page | `20` |
| `sort` | String | Sort criteria | `createdAt,desc` |

**Response** (200 OK):

```json
{
  "content": [
    {
      "planCode": "PLAN-20251117-001",
      "planName": "Gói Niềng Răng Mắc Cài Kim Loại",
      "patient": {
        "patientCode": "PAT-0001",
        "fullName": "Nguyễn Văn A"
      },
      "doctor": {
        "employeeCode": "EMP001",
        "fullName": "Dr. Nguyễn Văn B"
      },
      "status": "ACTIVE",
      "approvalStatus": "APPROVED",
      "totalPrice": 30000000,
      "finalCost": 28000000,
      "startDate": "2025-11-01",
      "expectedEndDate": "2027-11-01",
      "createdAt": "2025-11-01T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 45,
  "totalPages": 3
}
```

#### Implementation Steps

**Step 1**: Create DTO

```java
// TreatmentPlanSummaryDTO.java
@Data
@Builder
public class TreatmentPlanSummaryDTO {
    private String planCode;
    private String planName;
    private PatientSummary patient;
    private EmployeeSummary doctor;
    private TreatmentPlanStatus status;
    private ApprovalStatus approvalStatus;
    private BigDecimal totalPrice;
    private BigDecimal finalCost;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class PatientSummary {
        private String patientCode;
        private String fullName;
    }

    @Data
    @Builder
    public static class EmployeeSummary {
        private String employeeCode;
        private String fullName;
    }
}
```

**Step 2**: Add Repository Query

```java
// TreatmentPlanRepository.java
@Query("SELECT DISTINCT p FROM PatientTreatmentPlan p " +
       "LEFT JOIN FETCH p.patient " +
       "LEFT JOIN FETCH p.createdBy " +
       "WHERE (:approvalStatus IS NULL OR p.approvalStatus = :approvalStatus) " +
       "AND (:status IS NULL OR p.status = :status) " +
       "AND (:doctorCode IS NULL OR p.createdBy.employeeCode = :doctorCode)")
Page<PatientTreatmentPlan> findAllWithFilters(
    @Param("approvalStatus") ApprovalStatus approvalStatus,
    @Param("status") TreatmentPlanStatus status,
    @Param("doctorCode") String doctorCode,
    Pageable pageable
);
```

**Step 3**: Add Service Method

```java
// TreatmentPlanListService.java (new file)
@Service
@RequiredArgsConstructor
public class TreatmentPlanListService {

    private final PatientTreatmentPlanRepository planRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_ALL_TREATMENT_PLANS')")
    public Page<TreatmentPlanSummaryDTO> listAllPlans(
            ApprovalStatus approvalStatus,
            TreatmentPlanStatus status,
            String doctorCode,
            Pageable pageable) {

        Page<PatientTreatmentPlan> plansPage = planRepository.findAllWithFilters(
            approvalStatus, status, doctorCode, pageable
        );

        return plansPage.map(this::mapToSummary);
    }

    private TreatmentPlanSummaryDTO mapToSummary(PatientTreatmentPlan plan) {
        // Map entity to DTO
    }
}
```

**Step 4**: Add Controller Endpoint

```java
// TreatmentPlanController.java
@GetMapping("/treatment-plans")
@PreAuthorize("hasAuthority('VIEW_ALL_TREATMENT_PLANS')")
public ResponseEntity<Page<TreatmentPlanSummaryDTO>> listAllPlans(
        @RequestParam(required = false) ApprovalStatus approvalStatus,
        @RequestParam(required = false) TreatmentPlanStatus status,
        @RequestParam(required = false) String doctorEmployeeCode,
        @ParameterObject Pageable pageable) {

    Page<TreatmentPlanSummaryDTO> plans = listService.listAllPlans(
        approvalStatus, status, doctorEmployeeCode, pageable
    );

    return ResponseEntity.ok(plans);
}
```

**Step 5**: Add Permission

```sql
-- Add new permission
INSERT INTO permissions (permission_id, permission_name, description)
VALUES (nextval('permissions_seq'), 'VIEW_ALL_TREATMENT_PLANS',
        'View all treatment plans across all patients');

-- Assign to ROLE_MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_MANAGER'
  AND p.permission_name = 'VIEW_ALL_TREATMENT_PLANS';
```

#### Testing Examples

**Example 1**: List all plans (manager view)

```bash
GET /api/v1/treatment-plans?page=0&size=20&sort=createdAt,desc
Authorization: Bearer {manager_token}
```

**Example 2**: Filter by approval status

```bash
GET /api/v1/treatment-plans?approvalStatus=PENDING_REVIEW
```

**Example 3**: Filter by doctor

```bash
GET /api/v1/treatment-plans?doctorEmployeeCode=EMP001
```

**Example 4**: Combined filters

```bash
GET /api/v1/treatment-plans?approvalStatus=DRAFT&status=PENDING&page=0&size=10
```

---

## 📊 Implementation Timeline

| Issue                       | Estimated Time              | Priority  |
| --------------------------- | --------------------------- | --------- |
| Issue 1: Employee Code      | ✅ 0 minutes (already done) | -         |
| Issue 2: Zero Price         | ⏳ Awaiting decision        | 🟡 MEDIUM |
| Issue 2 - Option 1          | 20 minutes (if chosen)      | -         |
| Issue 2 - Option 2          | 45 minutes (if chosen)      | -         |
| Issue 4: Manager Permission | 2 hours                     | 🔴 HIGH   |

**Total**: ~2-3 hours (depending on Issue 2 decision)

---

## 🔧 Action Items

### For Business Team

**Issue 2 Decision Needed**:

1. Run this query to check zero-price services:

   ```sql
   SELECT service_code, service_name, price, is_active
   FROM dental_services
   WHERE price = 0 OR price IS NULL;
   ```

2. Answer these questions:

   - Do we need services with price = 0?
   - If yes, what are the use cases?
   - Should these services be priced when added to treatment plans?

3. Choose solution:
   - **Option 1**: No services with price = 0 (recommended)
   - **Option 2**: Allow zero-price services with special handling

### For Backend Team

**Immediate Action**:

- ✅ Issue 1: No action needed (already implemented)
- ⏳ Issue 2: Wait for business decision
- 🔴 Issue 4: Implement LIST ALL treatment plans API

**Next Steps for Issue 4**:

1. Create `TreatmentPlanSummaryDTO`
2. Add repository query with filters
3. Create `TreatmentPlanListService`
4. Add controller endpoint
5. Add `VIEW_ALL_TREATMENT_PLANS` permission
6. Assign permission to managers
7. Test with manager account

### For Frontend Team

**Issue 1** (Employee Code):

- ✅ Code already extracts `employee_code` from token
- ✅ Auto-fill logic already implemented
- 💡 Just need to verify JWT decoding is working

**Issue 2** (Zero Price):

- ⏳ Wait for backend decision
- Update validation logic based on chosen option

**Issue 4** (List All Plans):

- Wait for backend API implementation
- Create manager view for all treatment plans
- Add filters: approval status, plan status, doctor
- Add pagination controls

---

## 📞 Contact

**Questions or Clarifications?**

- Backend Lead: [Your Name]
- Business Team: [Business Lead]
- Frontend Team: [Frontend Lead]

**Document Version**: 1.0
**Last Updated**: November 17, 2025

---

## 📎 Appendix

### A. JWT Token Structure (Current)

```json
{
  "sub": "bacsi1",
  "iat": 1700000000,
  "exp": 1700003600,
  "roles": ["ROLE_DENTIST"],
  "permissions": [
    "CREATE_TREATMENT_PLAN",
    "UPDATE_TREATMENT_PLAN",
    "VIEW_TREATMENT_PLAN"
  ],
  "account_id": 456,
  "patient_code": null,
  "employee_code": "EMP001" // ← Available since v20
}
```

### B. Current Treatment Plan Permissions

| Permission                 | Description                | Assigned to                |
| -------------------------- | -------------------------- | -------------------------- |
| `CREATE_TREATMENT_PLAN`    | Create new treatment plans | ROLE_DENTIST               |
| `UPDATE_TREATMENT_PLAN`    | Update existing plans      | ROLE_DENTIST               |
| `VIEW_TREATMENT_PLAN`      | View plan details          | ROLE_DENTIST, ROLE_MANAGER |
| `APPROVE_TREATMENT_PLAN`   | Approve/reject plans       | ROLE_MANAGER               |
| `VIEW_ALL_TREATMENT_PLANS` | **NEW** - List all plans   | ROLE_MANAGER               |

### C. Related Files

**Issue 1**:

- `SecurityUtil.java` - JWT token generation
- `AuthenticationService.java` - Login logic

**Issue 2**:

- `CustomTreatmentPlanService.java` - Price validation
- `TreatmentPlanItemAdditionService.java` - Price validation
- `TreatmentPlanApprovalService.java` - Approval validation

**Issue 4**:

- `TreatmentPlanController.java` - Will add new endpoint
- `PatientTreatmentPlanRepository.java` - Will add new query
- (New) `TreatmentPlanListService.java` - New service for listing
- (New) `TreatmentPlanSummaryDTO.java` - New response DTO
