# Patient Unban Feature Implementation

**Date**: January 15, 2025  
**Version**: Backend V33  
**Business Rules**: BR-085, BR-086

---

## Overview

This feature allows Receptionists to quickly unban patients who were blocked due to consecutive no-shows, without requiring Manager approval. However, every unban action MUST be logged with a specific reason for accountability.

---

## Business Requirements

### BR-085: Receptionist Unban Authority
- **Problem**: Current system requires Manager approval to unban patients, causing delays
- **Solution**: Grant Receptionists direct authority to unban patients
- **Rationale**: Streamline workflow, reduce waiting time for patients
- **Authorization**: RECEPTIONIST, MANAGER, ADMIN roles

### BR-086: Mandatory Reason Logging
- **Problem**: Need accountability for unban actions to prevent abuse
- **Solution**: Require Receptionists to input a specific reason for every unban
- **Validation**: Reason must be 10-500 characters
- **Audit**: All unban actions logged with timestamp, performer, role, reason, previous state

---

## Implementation Details

### 1. Database Schema

**Table**: `patient_unban_audit_logs`

```sql
CREATE TABLE patient_unban_audit_logs (
    audit_id BIGSERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL REFERENCES patients(patient_id) ON DELETE CASCADE,
    previous_no_show_count INTEGER NOT NULL DEFAULT 0,
    performed_by VARCHAR(100) NOT NULL,
    performed_by_role VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Indexes**:
- `idx_unban_audit_patient` on `patient_id`
- `idx_unban_audit_performer` on `performed_by`
- `idx_unban_audit_timestamp` on `timestamp`
- `idx_unban_audit_role` on `performed_by_role`

### 2. Entity Classes

**PatientUnbanAuditLog.java**
- JPA entity with `@Table(name = "patient_unban_audit_logs")`
- Lombok builders for clean object creation
- Composite index on `(patient_id, timestamp)` for history queries

### 3. Repository Layer

**PatientUnbanAuditLogRepository.java**
- Spring Data JPA repository
- Query methods:
  - `findByPatientIdOrderByTimestampDesc(Integer patientId)`: Get patient history
  - `findByPerformedBy(String username)`: Get staff unban history
  - `findByTimestampBetween(LocalDateTime start, LocalDateTime end)`: Date range filter
  - `findByPerformedByRole(String role)`: Manager review by role

### 4. Service Layer

**PatientUnbanService.java**

Key Methods:

**`unbanPatient(Integer patientId, String reason)`**
- `@PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER', 'ADMIN')")`: Security enforcement
- Validates reason (10-500 chars, non-empty)
- Checks if patient is actually blocked
- Resets patient state:
  - `consecutiveNoShows = 0`
  - `isBookingBlocked = false`
  - `bookingBlockReason = null`
  - `blockedAt = null`
- Creates audit log with:
  - Username from `SecurityContextHolder`
  - Role extracted from authorities
  - Previous no-show count
  - Timestamp
  - Reason
- Returns `UnbanPatientResponse` DTO

**`getPatientUnbanHistory(Integer patientId)`**
- Returns list of `AuditLogResponse` DTOs
- Includes patient name, performer details, timestamp
- Ordered by timestamp descending (newest first)

**`isPatientBlocked(Integer patientId)`**
- Utility method to check block status

### 5. Controller Layer

**PatientController.java**

**New Endpoints**:

**`POST /api/v1/patients/{id}/unban`**
- Request Body: `UnbanPatientRequest` with `reason` field
- Validation: `@Valid` annotation triggers JSR-303 validation
- Authorization: `@PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER', 'ADMIN')")` at controller level
- Response: `UnbanPatientResponse` with unban details
- Success: 200 OK
- Error Cases:
  - 400 BAD_REQUEST: Reason too short/long/empty, patient not blocked
  - 403 FORBIDDEN: Insufficient permissions
  - 404 NOT_FOUND: Patient does not exist

**`GET /api/v1/patients/{id}/unban-history`**
- No request body
- Authorization: `@PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER', 'ADMIN')")`
- Response: `List<AuditLogResponse>` with full audit history
- Success: 200 OK
- Error Cases:
  - 403 FORBIDDEN: Insufficient permissions
  - 404 NOT_FOUND: Patient does not exist

### 6. DTOs

**UnbanPatientRequest.java**
```java
{
  "reason": "Khách trình bày lý do ốm, cam kết không tái phạm"
}
```
- Validation: `@NotBlank`, `@Size(min = 10, max = 500)`

**UnbanPatientResponse.java**
```json
{
  "message": "Mở khóa bệnh nhân thành công",
  "patientId": 123,
  "patientName": "Nguyễn Văn A",
  "previousNoShowCount": 3,
  "newNoShowCount": 0,
  "unbanBy": "receptionist01",
  "unbanByRole": "RECEPTIONIST",
  "unbanAt": "2025-01-15T14:30:00"
}
```

**AuditLogResponse.java**
```json
{
  "auditId": 1,
  "patientId": 123,
  "patientName": "Nguyễn Văn A",
  "previousNoShowCount": 3,
  "performedBy": "receptionist01",
  "performedByRole": "RECEPTIONIST",
  "reason": "Khách trình bày lý do ốm, cam kết không tái phạm",
  "timestamp": "2025-01-15T14:30:00"
}
```

---

## Security Implementation

### Authorization Flow

1. **Spring Security Filter Chain**: Validates JWT token
2. **SecurityContextHolder**: Extracts `Authentication` object
3. **@PreAuthorize Annotation**: Checks user role against allowed roles
4. **Service Method**: Executes business logic if authorized
5. **Audit Log**: Records username and role from authentication context

### Permission Matrix

| Role | POST /unban | GET /unban-history | View Own Logs | View All Logs |
|------|-------------|-------------------|---------------|---------------|
| RECEPTIONIST | ✅ | ✅ | ✅ | ❌ |
| MANAGER | ✅ | ✅ | ✅ | ✅ |
| ADMIN | ✅ | ✅ | ✅ | ✅ |
| DENTIST | ❌ | ❌ | ❌ | ❌ |
| PATIENT | ❌ | ❌ | ❌ | ❌ |

---

## Validation Rules

### Reason Validation (BR-086)

1. **Non-Empty**: `reason.trim()` must not be empty
   - Error: `400 BAD_REQUEST` - "Lễ tân bắt buộc phải nhập lý do mở khóa"

2. **Minimum Length**: 10 characters
   - Error: `400 BAD_REQUEST` - "Lý do mở khóa phải có ít nhất 10 ký tự để đảm bảo tính minh bạch"
   - Example (invalid): "Xin lỗi" (8 chars)
   - Example (valid): "Khách xin lỗi vì ốm đột xuất" (32 chars)

3. **Maximum Length**: 500 characters
   - Error: `400 BAD_REQUEST` - "Lý do mở khóa không được vượt quá 500 ký tự"

### Patient State Validation

1. **Patient Exists**: Check `patientRepository.findById()`
   - Error: `404 NOT_FOUND` - "Không tìm thấy bệnh nhân với ID: {id}"

2. **Patient Actually Blocked**: Check `isBookingBlocked == true` OR `consecutiveNoShows > 0`
   - Error: `400 BAD_REQUEST` - "Bệnh nhân này chưa bị chặn đặt lịch. Không cần mở khóa."
   - Prevents unnecessary unban actions on already-active patients

---

## Error Handling

Uses Spring Framework 6.x `ProblemDetail` (RFC 7807) for standardized error responses:

```json
{
  "type": "about:blank",
  "title": "Reason Required",
  "status": 400,
  "detail": "Lễ tân bắt buộc phải nhập lý do mở khóa (VD: Khách trình bày lý do ốm, Khách cam kết không tái phạm...)",
  "instance": "/api/v1/patients/123/unban"
}
```

### Error Codes

- `400 BAD_REQUEST`: Validation failure (reason, patient state)
- `403 FORBIDDEN`: Insufficient permissions (role check failed)
- `404 NOT_FOUND`: Patient does not exist
- `500 INTERNAL_SERVER_ERROR`: Database error, unexpected exception

---

## Testing Scenarios

### 1. Happy Path - Receptionist Unban

**Given**: Patient ID 123 is blocked (`consecutiveNoShows = 3`, `isBookingBlocked = true`)  
**When**: Receptionist posts valid reason ("Khách trình bày lý do ốm, cam kết không tái phạm")  
**Then**:
- Patient state: `consecutiveNoShows = 0`, `isBookingBlocked = false`
- Audit log created with reason, username, role, timestamp
- Response: `200 OK` with unban details

### 2. Validation Failure - Reason Too Short

**Given**: Patient is blocked  
**When**: Receptionist posts reason "Xin lỗi" (8 chars)  
**Then**: Response: `400 BAD_REQUEST` - "Lý do mở khóa phải có ít nhất 10 ký tự"

### 3. Business Logic Failure - Patient Not Blocked

**Given**: Patient is NOT blocked (`consecutiveNoShows = 0`, `isBookingBlocked = false`)  
**When**: Receptionist attempts unban  
**Then**: Response: `400 BAD_REQUEST` - "Bệnh nhân này chưa bị chặn đặt lịch"

### 4. Authorization Failure - Dentist Role

**Given**: User has `ROLE_DENTIST`  
**When**: Dentist attempts to unban patient  
**Then**: Response: `403 FORBIDDEN` - Access denied

### 5. Audit Log Query - Manager Review

**Given**: Multiple unban actions by different Receptionists  
**When**: Manager queries `/api/v1/patients/123/unban-history`  
**Then**: Returns all unban logs for patient, ordered by timestamp DESC

---

## Frontend Integration Guide

### 1. Unban Patient Button

**Location**: Patient detail page, appointment booking page (when patient is blocked)

**UI Flow**:
1. Show "Bệnh nhân bị chặn đặt lịch" warning banner
2. Display "Mở khóa bệnh nhân" button (only for RECEPTIONIST/MANAGER/ADMIN)
3. On click: Show modal/dialog with reason textarea
4. Validate reason length (10-500 chars) client-side
5. On submit: POST to `/api/v1/patients/{id}/unban`
6. On success: Show success toast, refresh patient data
7. On error: Display error message from `detail` field

**Example Code** (React/TypeScript):
```typescript
const unbanPatient = async (patientId: number, reason: string) => {
  try {
    const response = await api.post(`/api/v1/patients/${patientId}/unban`, {
      reason: reason.trim()
    });
    
    toast.success(response.data.message);
    refetchPatientData();
  } catch (error) {
    if (error.response?.status === 400) {
      toast.error(error.response.data.detail);
    } else {
      toast.error('Có lỗi xảy ra khi mở khóa bệnh nhân');
    }
  }
};
```

### 2. Unban History Viewer

**Location**: Patient detail page, under "Lịch sử chặn/mở khóa" tab

**UI Components**:
- Table with columns: Timestamp, Performed By, Role, Reason, Previous No-Shows
- Sort by timestamp DESC (newest first)
- Filter by date range (optional)
- Export to CSV (optional for MANAGER)

**API Call**:
```typescript
const fetchUnbanHistory = async (patientId: number) => {
  const response = await api.get(`/api/v1/patients/${patientId}/unban-history`);
  return response.data; // List<AuditLogResponse>
};
```

### 3. Patient Status Badge

**Display Logic**:
```typescript
const getPatientStatusBadge = (patient: Patient) => {
  if (patient.isBookingBlocked) {
    return (
      <Badge color="red">
        Bị chặn ({patient.consecutiveNoShows} lần no-show)
      </Badge>
    );
  }
  return <Badge color="green">Hoạt động</Badge>;
};
```

---

## Manager Review Workflow

### 1. Monitoring Unban Actions

**Objective**: Detect patterns of abuse or inappropriate unbans

**Approach**:
- Query audit logs by `performed_by_role = 'RECEPTIONIST'`
- Filter by date range (e.g., last 7 days)
- Look for patterns:
  - Same Receptionist unbanning same patient multiple times
  - Generic/short reasons (though validation blocks < 10 chars)
  - High frequency of unbans by one Receptionist

**API Query** (Future Enhancement):
```
GET /api/v1/audit-logs/patient-unban?role=RECEPTIONIST&startDate=2025-01-01&endDate=2025-01-15
```

### 2. Disciplinary Action

If Manager finds abuse:
1. Review audit logs for evidence
2. Discuss with Receptionist
3. Document incident in HR system (outside this module)
4. Optionally revoke RECEPTIONIST role's unban permission (system-wide config change)

---

## System Configuration

### application.yaml
No new configuration required. Uses existing:
- `spring.jpa.hibernate.ddl-auto: update` - Auto-creates audit table
- `spring.jpa.show-sql: true` - Log SQL for debugging
- Spring Security JWT configuration (existing)

### Database Migration
Schema file updated: `src/main/resources/db/schema.sql` (V33)

**Manual Migration** (if using existing database):
```sql
CREATE TABLE patient_unban_audit_logs (
    audit_id BIGSERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL REFERENCES patients(patient_id) ON DELETE CASCADE,
    previous_no_show_count INTEGER NOT NULL DEFAULT 0,
    performed_by VARCHAR(100) NOT NULL,
    performed_by_role VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_unban_audit_patient ON patient_unban_audit_logs(patient_id);
CREATE INDEX idx_unban_audit_performer ON patient_unban_audit_logs(performed_by);
CREATE INDEX idx_unban_audit_timestamp ON patient_unban_audit_logs(timestamp);
CREATE INDEX idx_unban_audit_role ON patient_unban_audit_logs(performed_by_role);
```

---

## Performance Considerations

### Database Indexes
- `patient_id`: Fast lookup for patient history (B-tree index)
- `performed_by`: Manager review queries (B-tree index)
- `timestamp`: Date range filtering (B-tree index)
- `performed_by_role`: Role-based filtering (B-tree index)

### Query Optimization
- Use `findByPatientIdOrderByTimestampDesc()` with `LIMIT` for pagination (future)
- Composite index on `(patient_id, timestamp)` covers most queries
- No JOIN required for basic queries (denormalized `performed_by` field)

### Scalability
- Audit table grows linearly with unban actions
- Estimated growth: ~100-500 records/month for medium clinic
- Archive strategy: Move records older than 1 year to cold storage (future)

---

## Compliance & Audit Trail

### Data Retention Policy
- Audit logs retained indefinitely (or per clinic policy)
- Comply with local healthcare data regulations (HIPAA/GDPR equivalent in Vietnam)
- Manager can export audit logs for external review

### Audit Questions Answered
1. **Who unbanned patient X?** → Query by `patient_id`
2. **When was patient X unbanned?** → `timestamp` field
3. **Why was patient X unbanned?** → `reason` field
4. **How many times has Receptionist Y unbanned patients?** → Query by `performed_by`
5. **What was the patient's no-show count before unban?** → `previous_no_show_count` field

---

## Future Enhancements

### 1. Unban Approval Workflow (Optional)
- Add `approval_status` column (`PENDING`, `APPROVED`, `REJECTED`)
- Require Manager approval for high no-show counts (e.g., > 5)
- Notification system for pending approvals

### 2. Reason Templates
- Provide predefined reason templates for Receptionists:
  - "Khách trình bày lý do ốm đột xuất"
  - "Khách gặp tai nạn giao thông"
  - "Khách cam kết không tái phạm"
- Allow custom text addition

### 3. Analytics Dashboard
- Chart: Unban frequency by month
- Chart: Top Receptionists by unban count
- Chart: Most common unban reasons (text analysis)
- Alert: Spike in unban actions (anomaly detection)

### 4. Patient Re-Block Logic
- Auto re-block patient if they no-show again within 30 days after unban
- Require Manager approval for second unban

---

## Contact & Support

**Implemented By**: GitHub Copilot  
**Date**: January 15, 2025  
**Backend Version**: V33  
**Related Business Rules**: BR-085, BR-086

For questions or issues, refer to:
- `PatientUnbanService.java` - Service layer documentation
- `PatientController.java` - API endpoint documentation
- `schema.sql` - Database schema V33

---

**End of Document**
