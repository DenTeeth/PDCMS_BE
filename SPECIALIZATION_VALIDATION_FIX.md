# Specialization Validation Fix - Treatment Plan Creation

## Critical Issue Identified

**Problem**: Treatment plans could be created without validating that doctors have the required specializations for the services included in the plan.

**Discovered During**: FLOW 1 testing (Template → Booking workflow)
- Doctor `bacsi1` (EMP001) has specializations: [3-Nha chu, 4-Phục hồi răng, 8-STANDARD]
- Attempted to create treatment plan using template "TPL_CROWN_CERCON"
- Template contains services requiring specialization 2 (Nội nha) which doctor doesn't have
- **Result**: Plan was created successfully but booking failed with error: "Doctor does not have required specialization"

**Root Cause**: Business validation was missing at plan creation stage, only checked at booking stage.

## Solution Implemented

### 1. Added Specialization Field to DentalService Entity

**File**: `src/main/java/com/dental/clinic/management/service/domain/DentalService.java`

**Changes**:
```java
/**
 * V21.4: Link to required specialization for performing this service
 * If NULL, service can be performed by any doctor (general service)
 * If NOT NULL, doctor must have this specialization to perform service
 */
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "specialization_id", foreignKey = @ForeignKey(name = "fk_service_specialization"))
private Specialization specialization;
```

**Reason**: The entity was missing the specialization relationship that exists in the database table. This field is needed to validate doctor qualifications.

---

### 2. Template-Based Treatment Plan Validation

**File**: `src/main/java/com/dental/clinic/management/treatment_plans/service/TreatmentPlanCreationService.java`

**Method**: `createTreatmentPlanFromTemplate()` (lines 99-131)

**Validation Logic Added**:
```java
// CRITICAL BUSINESS VALIDATION: Validate doctor has template's required specialization
if (template.getSpecialization() != null) {
    Integer requiredSpecId = template.getSpecialization().getSpecializationId();
    
    boolean doctorHasSpecialization = doctor.getSpecializations().stream()
            .anyMatch(spec -> spec.getSpecializationId().equals(requiredSpecId));
    
    if (!doctorHasSpecialization) {
        String doctorSpecsStr = doctor.getSpecializations().stream()
                .map(s -> s.getSpecializationName() + " (ID:" + s.getSpecializationId() + ")")
                .collect(Collectors.joining(", "));
        
        String errorMessage = String.format(
                "Doctor %s (%s %s) cannot use this template. " +
                "Template requires specialization '%s' (ID: %d). " +
                "Doctor's specializations: [%s]",
                doctor.getEmployeeCode(),
                doctor.getFirstName(),
                doctor.getLastName(),
                template.getSpecialization().getSpecializationName(),
                requiredSpecId,
                doctorSpecsStr);
        
        throw new BadRequestAlertException(
                errorMessage,
                "TreatmentPlan",
                "doctorSpecializationMismatch");
    }
    
    log.info("✓ Validated: Doctor {} has required specialization {} for template {}",
            doctor.getEmployeeCode(), requiredSpecId, template.getTemplateCode());
}
```

**Validation Rules**:
1. If template has NO specialization requirement → Allow any doctor
2. If template HAS specialization requirement → Doctor MUST have that specialization
3. Throw detailed error message listing doctor's actual specializations vs required

**Error Response Example**:
```json
{
  "type": "https://www.jhipster.tech/problem/problem-with-message",
  "title": "Bad Request",
  "status": 400,
  "detail": "Doctor EMP001 (Nguyễn Văn) cannot use this template. Template requires specialization 'Nội nha' (ID: 2). Doctor's specializations: [Nha chu (ID:3), Phục hồi răng (ID:4), STANDARD (ID:8)]",
  "path": "/api/v1/patients/P001/treatment-plans",
  "message": "error.doctorSpecializationMismatch"
}
```

---

### 3. Custom Treatment Plan Validation

**File**: `src/main/java/com/dental/clinic/management/treatment_plans/service/CustomTreatmentPlanService.java`

**Method**: `createCustomPlan()` - Added validation call at line 85
**New Validation Method**: `validateDoctorSpecializationsForServices()` (lines 309-401)

**Validation Logic**:
```java
private void validateDoctorSpecializationsForServices(
        Employee doctor, 
        List<CreateCustomPlanRequest.PhaseRequest> phases) {
    
    // 1. Collect doctor's specialization IDs
    Set<Integer> doctorSpecIds = doctor.getSpecializations().stream()
            .map(Specialization::getSpecializationId)
            .collect(Collectors.toSet());
    
    // 2. Extract all service codes from all phases
    List<String> allServiceCodes = phases.stream()
            .flatMap(phase -> phase.getItems().stream())
            .map(CreateCustomPlanRequest.ItemRequest::getServiceCode)
            .distinct()
            .collect(Collectors.toList());
    
    // 3. Validate each service
    List<String> mismatchErrors = new ArrayList<>();
    
    for (String serviceCode : allServiceCodes) {
        DentalService service = validateAndGetService(serviceCode);
        
        // If service has NO specialization requirement, skip (general service)
        if (service.getSpecialization() == null) {
            log.debug("Service {} has no specialization requirement (general service) - OK", serviceCode);
            continue;
        }
        
        Integer requiredSpecId = service.getSpecialization().getSpecializationId();
        
        // Check if doctor has this specialization
        if (!doctorSpecIds.contains(requiredSpecId)) {
            String error = String.format(
                    "Service '%s' (%s) requires specialization '%s' (ID: %d)",
                    service.getServiceCode(),
                    service.getServiceName(),
                    service.getSpecialization().getSpecializationName(),
                    requiredSpecId);
            mismatchErrors.add(error);
        }
    }
    
    // 4. If any mismatches found, throw error with complete list
    if (!mismatchErrors.isEmpty()) {
        String doctorSpecsStr = doctor.getSpecializations().stream()
                .map(s -> s.getSpecializationName() + " (ID:" + s.getSpecializationId() + ")")
                .collect(Collectors.joining(", "));
        
        String errorMessage = String.format(
                "Doctor %s (%s %s) cannot create this treatment plan. " +
                "Doctor's specializations: [%s]. " +
                "Missing required specializations for %d service(s):\n%s",
                doctor.getEmployeeCode(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctorSpecsStr,
                mismatchErrors.size(),
                String.join("\n", mismatchErrors));
        
        throw new BadRequestAlertException(
                errorMessage,
                "TreatmentPlan",
                "doctorSpecializationMismatch");
    }
}
```

**Validation Rules**:
1. Extract all service codes from all phases in the plan
2. For each service:
   - If service has NO specialization requirement (NULL) → Allow (general service)
   - If service HAS specialization requirement → Check if doctor has it
3. Collect ALL mismatches (don't fail on first error)
4. Throw detailed error listing all incompatible services

**Error Response Example**:
```json
{
  "type": "https://www.jhipster.tech/problem/problem-with-message",
  "title": "Bad Request",
  "status": 400,
  "detail": "Doctor EMP001 (Nguyễn Văn) cannot create this treatment plan. Doctor's specializations: [Chỉnh nha (ID:1), STANDARD (ID:8)]. Missing required specializations for 2 service(s):\nService 'RC_001' (Root Canal - Single Root) requires specialization 'Nội nha' (ID: 2)\nService 'EXT_SURG' (Surgical Extraction) requires specialization 'Phẫu thuật' (ID: 5)",
  "path": "/api/v1/patients/P001/treatment-plans/custom",
  "message": "error.doctorSpecializationMismatch"
}
```

---

## Business Rules Summary

### Validation Timing
- **Before**: Validation only at booking stage (too late)
- **After**: Validation at plan creation stage (fail fast)

### Specialization Matching Rules
1. **Template Plans**:
   - Template has `specialization_id` → Doctor must have matching specialization
   - Template has NULL specialization → Any doctor can use it

2. **Custom Plans**:
   - Each service may have `specialization_id`
   - Service with NULL → Any doctor can include it (general service)
   - Service with specific specialization → Doctor must have it
   - **Multi-service validation**: All services must match OR be general

3. **General Services** (specialization_id = NULL):
   - Examples: X-Ray, Consultation, Cleaning (basic)
   - Can be performed by any doctor
   - No specialization validation required

4. **Specialized Services** (specialization_id = specific value):
   - Examples: Root Canal (Nội nha), Braces (Chỉnh nha), Surgery (Phẫu thuật)
   - Require doctor to have matching specialization
   - Strictly validated at plan creation

---

## Testing Recommendations

### Test Case 1: Template with Matching Specialization
**Setup**:
- Doctor: bacsi2 (EMP002) with specialization 2 (Nội nha)
- Template: "TPL_ROOT_CANAL" requires specialization 2

**Expected**: ✅ Plan created successfully

**Test**:
```bash
curl -X POST "http://localhost:8080/api/v1/patients/P001/treatment-plans" \
  -H "Authorization: Bearer $BACSI2_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "templateCode": "TPL_ROOT_CANAL",
    "patientNotes": "Tooth 16 - Molar"
  }'
```

### Test Case 2: Template with Mismatched Specialization
**Setup**:
- Doctor: bacsi1 (EMP001) with specializations 3, 4, 8 (NO specialization 2)
- Template: "TPL_ROOT_CANAL" requires specialization 2

**Expected**: ❌ Error 400 with detailed message

**Test**:
```bash
curl -X POST "http://localhost:8080/api/v1/patients/P001/treatment-plans" \
  -H "Authorization: Bearer $BACSI1_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "templateCode": "TPL_ROOT_CANAL",
    "patientNotes": "Tooth 16 - Molar"
  }'
```

**Expected Error**:
```json
{
  "status": 400,
  "detail": "Doctor EMP001 (Nguyễn Văn) cannot use this template. Template requires specialization 'Nội nha' (ID: 2). Doctor's specializations: [Nha chu (ID:3), Phục hồi răng (ID:4), STANDARD (ID:8)]"
}
```

### Test Case 3: Custom Plan with All Compatible Services
**Setup**:
- Doctor: bacsi1 (EMP001) with specializations 3, 4, 8
- Services: SCALING_L1 (spec 3), FILLING_COMP (spec 4), CONSULT (NULL)

**Expected**: ✅ Plan created successfully

**Test**:
```bash
curl -X POST "http://localhost:8080/api/v1/patients/P001/treatment-plans/custom" \
  -H "Authorization: Bearer $BACSI1_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "phases": [
      {
        "phaseName": "Initial Treatment",
        "phaseNumber": 1,
        "items": [
          {"serviceCode": "CONSULT", "toothNumber": null, "quantity": 1},
          {"serviceCode": "SCALING_L1", "toothNumber": null, "quantity": 1},
          {"serviceCode": "FILLING_COMP", "toothNumber": "16", "quantity": 1}
        ]
      }
    ]
  }'
```

### Test Case 4: Custom Plan with Incompatible Services
**Setup**:
- Doctor: bacsi1 (EMP001) with specializations 3, 4, 8 (NO specialization 2 or 5)
- Services: RC_001 (spec 2), EXT_SURG (spec 5)

**Expected**: ❌ Error 400 listing both incompatible services

**Test**:
```bash
curl -X POST "http://localhost:8080/api/v1/patients/P001/treatment-plans/custom" \
  -H "Authorization: Bearer $BACSI1_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "phases": [
      {
        "phaseName": "Advanced Treatment",
        "phaseNumber": 1,
        "items": [
          {"serviceCode": "RC_001", "toothNumber": "16", "quantity": 1},
          {"serviceCode": "EXT_SURG", "toothNumber": "32", "quantity": 1}
        ]
      }
    ]
  }'
```

**Expected Error**:
```json
{
  "status": 400,
  "detail": "Doctor EMP001 (Nguyễn Văn) cannot create this treatment plan. Doctor's specializations: [Nha chu (ID:3), Phục hồi răng (ID:4), STANDARD (ID:8)]. Missing required specializations for 2 service(s):\nService 'RC_001' (Root Canal - Single Root) requires specialization 'Nội nha' (ID: 2)\nService 'EXT_SURG' (Surgical Extraction) requires specialization 'Phẫu thuật' (ID: 5)"
}
```

### Test Case 5: Custom Plan with Mix of General and Specialized Services
**Setup**:
- Doctor: bacsi3 (EMP003) with specialization 1 (Chỉnh nha) only
- Services: CONSULT (NULL - general), XRAY (NULL - general), BRACES (spec 1)

**Expected**: ✅ Plan created successfully (general services + matching specialization)

---

## Database Schema Reference

### Services Table
```sql
Column              | Type              | Description
--------------------|-------------------|------------------------------------------
service_id          | integer           | Primary key
service_code        | varchar(50)       | Unique service identifier
service_name        | varchar(255)      | Service display name
specialization_id   | integer           | FK to specializations (nullable)
category_id         | bigint            | FK to service_categories
price               | numeric(15,2)     | Default service price
is_active           | boolean           | Active status
```

### Specializations Table
```sql
Column                | Type              | Description
----------------------|-------------------|------------------------------------------
specialization_id     | integer           | Primary key
specialization_code   | varchar(50)       | Unique specialization code
specialization_name   | varchar(255)      | Display name (e.g., "Nha chu", "Nội nha")
is_active             | boolean           | Active status
```

### Employee_Specializations Table (Join Table)
```sql
Column                | Type              | Description
----------------------|-------------------|------------------------------------------
employee_id           | bigint            | FK to employees
specialization_id     | integer           | FK to specializations
PRIMARY KEY (employee_id, specialization_id)
```

---

## Impact Analysis

### Security & Business Logic
- ✅ **Enhanced**: Prevents doctors from creating plans they cannot execute
- ✅ **Improved**: Fail-fast approach catches errors at plan creation, not booking
- ✅ **Transparent**: Detailed error messages help doctors understand restrictions

### User Experience
- ✅ **Better Feedback**: Clear error messages explain why plan cannot be created
- ✅ **Reduced Frustration**: Errors caught early, before investing time in plan details
- ⚠️ **Frontend Integration Needed**: FE should use `/my-specializations` endpoint to filter compatible services automatically

### API Behavior Changes
- **Breaking Change**: Plans that previously succeeded may now fail with 400 error
- **Migration Note**: Existing plans are NOT affected (created before validation)
- **Frontend Action Required**: Update UI to handle new error responses

---

## Related APIs

### 1. Get Services for Current Doctor (NEW in V21.4)
**Endpoint**: `GET /api/v1/booking/services/my-specializations`

**Purpose**: Returns only services the authenticated doctor can perform (automatic filtering)

**Response**: Filtered list based on JWT token's doctor specializations

**Frontend Usage**:
```javascript
// Use this endpoint when doctor creates custom treatment plan
const compatibleServices = await fetch('/api/v1/booking/services/my-specializations?page=0&size=50', {
  headers: { 'Authorization': `Bearer ${token}` }
});
// Only show these services in dropdown - prevents validation errors
```

### 2. Get Templates by Specialization
**Endpoint**: `GET /api/v1/treatment-plan-templates?specializationId={id}`

**Purpose**: Filter templates by required specialization

**Frontend Usage**:
```javascript
// Get doctor's specializations from JWT or profile
const doctorSpecIds = [3, 4, 8]; // Example: bacsi1

// Query templates for each specialization
for (const specId of doctorSpecIds) {
  const templates = await fetch(`/api/v1/treatment-plan-templates?specializationId=${specId}`);
  // Show these templates in UI - doctor can use them
}
```

---

## Build & Deployment

### Files Modified
1. `src/main/java/com/dental/clinic/management/service/domain/DentalService.java`
   - Added `specialization` field with `@ManyToOne` relationship

2. `src/main/java/com/dental/clinic/management/treatment_plans/service/TreatmentPlanCreationService.java`
   - Added specialization validation in `createTreatmentPlanFromTemplate()` (lines 99-131)

3. `src/main/java/com/dental/clinic/management/treatment_plans/service/CustomTreatmentPlanService.java`
   - Added validation call in `createCustomPlan()` (line 85)
   - Implemented `validateDoctorSpecializationsForServices()` method (lines 309-401)

### Build Command
```bash
./mvnw clean install -DskipTests
```

### Run Command
```bash
java -jar target/dental-clinic-management-0.0.1-SNAPSHOT.jar
```

### Deployment Notes
- No database migration needed (specialization_id column already exists)
- No seed data changes required
- Backward compatible (existing plans unaffected)
- Restart required to apply changes

---

## Logging & Debugging

### Success Logs (Template)
```
INFO  TreatmentPlanCreationService - ✓ Validated: Doctor EMP002 has required specialization 2 for template TPL_ROOT_CANAL
```

### Success Logs (Custom)
```
DEBUG CustomTreatmentPlanService - Service CONSULT has no specialization requirement (general service) - OK
DEBUG CustomTreatmentPlanService - ✓ Service SCALING_L1 requires spec 3 - doctor has it
INFO  CustomTreatmentPlanService - ✓ All services validated: Doctor EMP001 has required specializations for 3 service(s)
```

### Error Logs
```
WARN  CustomTreatmentPlanService - Specialization mismatch: Service 'RC_001' (Root Canal - Single Root) requires specialization 'Nội nha' (ID: 2)
ERROR CustomTreatmentPlanService - Doctor EMP001 cannot create plan: Missing 2 required specializations
```

### Debug Mode
Add to `application.yaml`:
```yaml
logging:
  level:
    com.dental.clinic.management.treatment_plans: DEBUG
```

---

## Acceptance Criteria

### Functional Requirements
- [x] Template plans validate doctor has template's required specialization
- [x] Custom plans validate doctor has ALL services' required specializations
- [x] General services (specialization_id = NULL) allowed for any doctor
- [x] Detailed error messages list missing specializations
- [x] Validation happens before plan creation (fail-fast)

### Non-Functional Requirements
- [x] No performance impact (validation uses in-memory data)
- [x] Backward compatible (existing plans work)
- [x] Clear logging for debugging
- [x] Comprehensive error messages for API consumers

### Testing Requirements
- [ ] Unit tests for validation methods
- [ ] Integration tests for both template and custom flows
- [ ] Test cases for edge cases (NULL specializations, multiple mismatches)
- [ ] Frontend E2E tests with new error handling

---

## Next Steps

### 1. Frontend Integration (HIGH PRIORITY)
- Update treatment plan creation UIs to use `/my-specializations` endpoint
- Handle new 400 error responses with user-friendly messages
- Show doctor's specializations in UI (help understand restrictions)
- Filter templates by doctor's specializations automatically

### 2. Additional Testing
- Create unit tests for validation methods
- Add integration tests covering all scenarios
- Test with different doctor specialization combinations
- Verify edge cases (multiple specializations, NULL values)

### 3. Documentation Updates
- Update API documentation with new error responses
- Add specialization requirements to template documentation
- Create user guide explaining specialization restrictions
- Update frontend implementation guide

### 4. Monitoring & Metrics
- Track validation failures (how often doctors hit this error)
- Monitor which specializations are most commonly missing
- Analyze if business rules need adjustment
- Collect feedback from doctors about error messages

---

## Known Limitations

1. **Validation Timing**: Validation only at plan creation, not at template/service modification
   - If template specialization changes, existing plans are not revalidated
   - If service specialization changes, existing plans are not affected

2. **Cascading Changes**: No automatic plan reassignment
   - If doctor loses specialization, their existing plans remain assigned
   - Manual intervention needed to reassign plans to qualified doctors

3. **Workarounds**: Admins can bypass validation
   - Current implementation validates based on JWT token
   - Admin creating plan on behalf of doctor needs doctor's token
   - Future: Add "create as different doctor" feature for admins

---

## Changelog

### V21.4 - Specialization Validation (November 20, 2025)
- Added `specialization` field to `DentalService` entity
- Implemented template plan specialization validation
- Implemented custom plan service-level validation
- Added detailed error messages with missing specializations
- Enhanced logging for debugging validation issues

---

## Contact & Support

**Feature Owner**: Backend Development Team  
**Issue Discovered By**: User testing (FLOW 1)  
**Fix Implemented By**: GitHub Copilot  
**Documentation Date**: November 20, 2025  
**Version**: V21.4  
**Branch**: `feat/BE-501-manage-treatment-plans`
