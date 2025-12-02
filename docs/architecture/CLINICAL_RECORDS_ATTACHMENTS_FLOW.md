# Clinical Records Attachments Management - Architecture & Flow

## I. Tong Quan (Overview)

Module quan ly file dinh kem (X-quang, anh chup, ket qua xet nghiem, don dong y, ...) cho ho so kham benh (Clinical Records). He thong ho tro upload, xem danh sach, va xoa file voi RBAC (Role-Based Access Control) day du.

### 1.1 Features

- **Upload file**: Ho tro JPG, PNG, GIF, PDF (toi da 10MB)
- **Categorization**: 6 loai attachment (XRAY, PHOTO_BEFORE, PHOTO_AFTER, LAB_RESULT, CONSENT_FORM, OTHER)
- **Access Control**: Kiem soat quyen truy cap theo role (Dentist/Nurse/Patient/Admin)
- **File Management**: Luu tru local filesystem voi duong dan co cau truc
- **Audit Trail**: Theo doi nguoi upload va thoi gian upload

### 1.2 API Endpoints

| Endpoint                                          | Method | Description   | Permission        |
| ------------------------------------------------- | ------ | ------------- | ----------------- |
| `/api/v1/clinical-records/{recordId}/attachments` | POST   | Upload file   | UPLOAD_ATTACHMENT |
| `/api/v1/clinical-records/{recordId}/attachments` | GET    | Lay danh sach | VIEW_ATTACHMENT   |
| `/api/v1/attachments/{attachmentId}`              | DELETE | Xoa file      | DELETE_ATTACHMENT |

---

## II. Database Schema

### 2.1 ENUM Type

```sql
CREATE TYPE attachment_type_enum AS ENUM (
    'XRAY',              -- Phim chup X-quang
    'PHOTO_BEFORE',      -- Anh truoc dieu tri
    'PHOTO_AFTER',       -- Anh sau dieu tri
    'LAB_RESULT',        -- Ket qua xet nghiem
    'CONSENT_FORM',      -- Don dong y dieu tri
    'OTHER'              -- Loai khac
);
```

### 2.2 Table: clinical_record_attachments

```sql
CREATE TABLE clinical_record_attachments (
    attachment_id       SERIAL PRIMARY KEY,
    clinical_record_id  INTEGER NOT NULL REFERENCES clinical_records(clinical_record_id) ON DELETE CASCADE,
    file_name           VARCHAR(255) NOT NULL,
    file_path           VARCHAR(500) NOT NULL,
    file_size           BIGINT NOT NULL,
    mime_type           VARCHAR(100) NOT NULL,
    attachment_type     attachment_type_enum NOT NULL,
    description         TEXT,
    uploaded_by         INTEGER REFERENCES employees(employee_id),
    uploaded_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_attachments_clinical_record ON clinical_record_attachments(clinical_record_id);
CREATE INDEX idx_attachments_type ON clinical_record_attachments(attachment_type);
CREATE INDEX idx_attachments_uploaded_by ON clinical_record_attachments(uploaded_by);
```

**Key Design Decisions**:

- `ON DELETE CASCADE`: Khi xoa clinical record, tu dong xoa tat ca attachments lien quan
- `uploaded_by`: NULL-able vi co the la system upload (employee_id = 0)
- `file_path`: Luu duong dan tuyet doi de truy xuat file

---

## III. File Storage Architecture

### 3.1 Local Storage (Current Implementation)

```
uploads/
└── clinical-records/
    └── {clinical_record_id}/
        ├── 20251202_093045_xray_panoramic.jpg
        ├── 20251202_095120_photo_before.png
        └── 20251203_141530_consent_form.pdf
```

**Directory Structure**:

- **Base Path**: `uploads/clinical-records/`
- **Record Folder**: `{clinical_record_id}/`
- **File Naming**: `{yyyyMMdd_HHmmss}_{sanitized_filename}`

**File Sanitization**:

- Loai bo ky tu dac biet (`/`, `\`, `..`, NULL bytes)
- Giu lai extension goc
- Them timestamp prefix de tranh trung lap ten file

### 3.2 S3 Migration Plan (Future - TODO)

```java
// TODO: Migrate to AWS S3 in production
// Configuration:
// - Bucket: dental-clinic-attachments
// - Region: ap-southeast-1 (Singapore)
// - Encryption: AES-256
// - Lifecycle: Move to Glacier after 2 years
// - CDN: CloudFront for faster delivery
//
// Environment variables needed:
// AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, S3_BUCKET_NAME
```

**Migration Strategy**:

1. Tao abstract interface `StorageService` voi 2 implementations:
   - `LocalFileStorageService` (current)
   - `S3StorageService` (future)
2. Dung Spring Profile de switch:
   - `dev`, `test`: Local storage
   - `production`: S3 storage
3. Migrate du lieu cu:
   - Script upload tat ca file hien co len S3
   - Cap nhat `file_path` trong database
   - Keep backward compatibility (check prefix `uploads/` vs `s3://`)

---

## IV. Service Layer Architecture

### 4.1 FileStorageService

**Responsibility**: Low-level file operations

```java
public class FileStorageService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "application/pdf"
    );

    public String storeFile(MultipartFile file, Integer recordId)
    public void deleteFile(String filePath)
    public void validateFile(MultipartFile file)
    private String sanitizeFilename(String filename)
}
```

**Validation Rules**:

- File size: Max 10MB (enforced by Tomcat + custom validation)
- MIME types: JPG, PNG, GIF, PDF only
- Filename: Remove dangerous characters (path traversal prevention)

### 4.2 ClinicalRecordAttachmentService

**Responsibility**: Business logic + RBAC

```java
public class ClinicalRecordAttachmentService {
    public UploadAttachmentResponse uploadAttachment(Integer recordId, UploadAttachmentRequest request)
    public List<AttachmentResponse> getAttachments(Integer recordId)
    public void deleteAttachment(Integer attachmentId)

    // RBAC Helper Methods
    private void checkAccessPermission(Appointment appointment)
    private void checkDeletePermission(ClinicalRecordAttachment attachment)
    private Integer getCurrentEmployeeId()
    private Integer getCurrentPatientId()
}
```

**RBAC Logic** (Reuses API 8.1 Pattern):

1. **Admin**: Full access to all records
2. **VIEW_APPOINTMENT_ALL**: Access all records (Receptionist, Manager)
3. **VIEW_APPOINTMENT_OWN**: Access only if:
   - User is primary doctor of appointment
   - User is participant doctor (AppointmentParticipant)
   - User is the patient

**Delete Permission**:

- Admin can delete any attachment
- Regular users can only delete their own uploads

---

## V. API Flow Diagrams

### 5.1 Upload Flow (API 8.11)

```
Client Request
    |
    v
Controller (@PreAuthorize + Multipart)
    |
    v
Service Layer
    |
    +---> validateFile() (size, MIME type)
    |
    +---> getClinicalRecord() (check exists)
    |
    +---> getAppointment() via record
    |
    +---> checkAccessPermission() (RBAC)
    |
    +---> storeFile() (save to disk)
    |
    +---> save to database (attachment metadata)
    |
    v
Return UploadAttachmentResponse (201 CREATED)
```

**Error Scenarios**:

- File too large: 500 (Tomcat rejects before reaching controller)
- Invalid MIME: 400 INVALID_FILE_TYPE
- Record not found: 404 RECORD_NOT_FOUND
- No permission: 403 FORBIDDEN

### 5.2 Get Attachments Flow (API 8.12)

```
Client Request
    |
    v
Controller (@PreAuthorize)
    |
    v
Service Layer
    |
    +---> getClinicalRecord() (check exists)
    |
    +---> getAppointment() via record
    |
    +---> checkAccessPermission() (RBAC)
    |
    +---> findByClinicalRecord() (query attachments, ordered by uploadedAt DESC)
    |
    +---> map to AttachmentResponse DTOs (with uploader info)
    |
    v
Return List<AttachmentResponse> (200 OK)
```

**Error Scenarios**:

- Record not found: 404 RECORD_NOT_FOUND
- No permission: 403 FORBIDDEN

### 5.3 Delete Flow (API 8.13)

```
Client Request
    |
    v
Controller (@PreAuthorize)
    |
    v
Service Layer
    |
    +---> findById() (check attachment exists)
    |
    +---> checkDeletePermission() (Admin or owner only)
    |
    +---> deleteFile() (remove from disk)
    |
    +---> delete from database
    |
    v
Return 204 NO CONTENT
```

**Error Scenarios**:

- Attachment not found: 404 ATTACHMENT_NOT_FOUND
- Not owner: 403 FORBIDDEN (You can only delete attachments that you uploaded)
- File delete fails: Log error but continue (prevent orphan DB records)

---

## VI. Security Considerations

### 6.1 File Upload Security

| Threat            | Mitigation                                |
| ----------------- | ----------------------------------------- |
| Path Traversal    | Sanitize filename (remove `..`, `/`, `\`) |
| Malicious Files   | MIME type validation (whitelist only)     |
| File Bombs        | Size limit 10MB (Tomcat + custom)         |
| XSS via Filename  | Sanitize filename, never render raw       |
| Directory Listing | Store files outside webroot               |

### 6.2 RBAC Security

- **Principle of Least Privilege**: Each role only gets necessary permissions
- **Ownership Check**: Users can only delete their own uploads (except Admin)
- **Access Control**: Reuses proven API 8.1 logic for consistency
- **Audit Trail**: Track who uploaded what and when

### 6.3 Authentication

- All endpoints require `Authorization: Bearer {token}`
- Spring Security `@PreAuthorize` enforces permissions before method execution
- Role hierarchy: `ROLE_ADMIN` > `ROLE_DENTIST` > `ROLE_NURSE` > `ROLE_PATIENT`

---

## VII. Performance & Limitations

### 7.1 Current Limitations

| Limitation         | Value           | Rationale                               |
| ------------------ | --------------- | --------------------------------------- |
| Max file size      | 10 MB           | Balance between quality and storage     |
| Allowed MIME types | 4 types         | Reduce attack surface                   |
| Storage            | Local FS        | Simple for MVP, TODO: S3                |
| Pagination         | None            | Assume reasonable file count per record |
| File serving       | Not implemented | TODO: Secure download endpoint          |

### 7.2 Performance Characteristics

- **Upload**: O(1) database insert + disk I/O
- **List**: O(n) where n = attachments per record (typically < 20)
- **Delete**: O(1) database delete + disk I/O
- **Indexes**: 3 indexes (clinical_record_id, attachment_type, uploaded_by) for fast queries

### 7.3 Scalability Considerations

**Current (Local Storage)**:

- Single server limitation
- No CDN support
- Manual backup required

**Future (S3)**:

- Horizontal scaling (S3 handles replication)
- CDN integration (CloudFront)
- Automatic backups (S3 versioning)
- Cost optimization (Glacier for old files)

---

## VIII. Testing Results

### 8.1 API 8.11 (Upload) - 6/6 Passed

| Test Case | Scenario                        | Expected              | Result |
| --------- | ------------------------------- | --------------------- | ------ |
| 1         | Upload X-ray as Doctor          | 201 CREATED           | PASS   |
| 2         | Upload 11MB file                | 500 (Tomcat reject)   | PASS   |
| 3         | Upload .txt file                | 400 INVALID_FILE_TYPE | PASS   |
| 4         | Upload to non-existent record   | 404 RECORD_NOT_FOUND  | PASS   |
| 5         | Upload to other doctor's record | 403 FORBIDDEN         | PASS   |
| 6         | Upload as Admin                 | 201 CREATED           | PASS   |

### 8.2 API 8.12 (Get List) - 5/7 Passed

| Test Case | Scenario                    | Expected         | Result  |
| --------- | --------------------------- | ---------------- | ------- |
| 1         | Get attachments as Doctor   | 200 OK with list | PASS    |
| 2         | Get empty list              | 200 OK with []   | SKIPPED |
| 3         | Get as Patient (own record) | 200 OK with list | PASS    |
| 4         | Get as Nurse (participant)  | 200 OK           | SKIPPED |
| 5         | Get non-existent record     | 404 NOT FOUND    | PASS    |
| 6         | Get as wrong patient        | 403 FORBIDDEN    | PASS    |
| 7         | Get as Admin                | 200 OK           | PASS    |

### 8.3 API 8.13 (Delete) - 5/6 Passed

| Test Case | Scenario                          | Expected       | Result  |
| --------- | --------------------------------- | -------------- | ------- |
| 1         | Delete own attachment             | 204 NO CONTENT | PASS    |
| 2         | Delete other's attachment         | 403 FORBIDDEN  | PASS    |
| 3         | Delete as Admin                   | 204 NO CONTENT | PASS    |
| 4         | Delete non-existent attachment    | 404 NOT FOUND  | PASS    |
| 5         | Delete without permission (Nurse) | 403 FORBIDDEN  | SKIPPED |
| 6         | Re-upload after delete            | 201 CREATED    | PASS    |

---

## IX. Implementation Checklist

### 9.1 Completed

- [x] Database schema (ENUM + table + indexes)
- [x] Entity classes (AttachmentTypeEnum, ClinicalRecordAttachment)
- [x] Repository (ClinicalRecordAttachmentRepository)
- [x] DTOs (AttachmentResponse, UploadAttachmentRequest, UploadAttachmentResponse)
- [x] FileStorageService (validation, storage, deletion)
- [x] ClinicalRecordAttachmentService (business logic, RBAC)
- [x] Controller (3 REST endpoints)
- [x] Permissions (3 permissions, 5 role assignments in seed-data.sql)
- [x] API Documentation (3 comprehensive MD files with test scenarios)
- [x] Testing (All 3 APIs tested with multiple scenarios)
- [x] JdbcTypeCode annotation fix (PostgreSQL ENUM support)

### 9.2 Future TODOs

- [ ] Migrate to AWS S3 (see Section III.2)
- [ ] Add file download endpoint (secure with token, prevent hotlinking)
- [ ] Implement pagination for large attachment lists
- [ ] Add image thumbnail generation (for photos)
- [ ] Support file versioning (keep history when replacing)
- [ ] Add virus scanning (ClamAV integration)
- [ ] Implement attachment preview (PDF.js, image viewer)
- [ ] Add bulk upload support (multiple files at once)
- [ ] Create attachment audit log (who viewed what file when)
- [ ] Add file compression (auto-compress images before storage)

---

## X. Code References

### 10.1 Key Files

| File                                      | Purpose            | Lines of Code |
| ----------------------------------------- | ------------------ | ------------- |
| `AttachmentTypeEnum.java`                 | 6 enum values      | ~20           |
| `ClinicalRecordAttachment.java`           | JPA entity         | ~80           |
| `ClinicalRecordAttachmentRepository.java` | Data access        | ~15           |
| `FileStorageService.java`                 | File operations    | ~150          |
| `ClinicalRecordAttachmentService.java`    | Business logic     | ~250          |
| `ClinicalRecordAttachmentController.java` | REST endpoints     | ~80           |
| `dental-clinic-seed-data.sql`             | ENUM + permissions | +10 lines     |
| `schema.sql`                              | Table definition   | +25 lines     |

### 10.2 Dependencies

- Spring Boot Starter Web (multipart upload)
- Spring Boot Starter Data JPA (database)
- Spring Security (authentication, authorization)
- PostgreSQL JDBC Driver (database connection)
- Lombok (reduce boilerplate)

---

## XI. Maintenance & Operations

### 11.1 Monitoring

**Key Metrics**:

- Upload success rate (target: > 99%)
- Average upload time (target: < 3s for 5MB file)
- Storage usage (alert at 80% capacity)
- Failed delete operations (should be 0)

**Log Monitoring**:

```bash
# Check upload failures
grep "Failed to store file" backend.log

# Check RBAC denials
grep "FORBIDDEN.*attachment" backend.log

# Check file size violations
grep "INVALID_FILE.*size" backend.log
```

### 11.2 Backup Strategy

**Local Storage (Current)**:

- Daily backup of `uploads/clinical-records/` directory
- Retention: 30 days
- Backup to external drive or network storage

**Database**:

- Daily backup of `clinical_record_attachments` table
- Include in existing PostgreSQL backup routine

### 11.3 Disaster Recovery

**Scenario: Disk Failure**

1. Restore files from latest backup
2. Verify `file_path` in database matches restored files
3. Test upload/download functionality

**Scenario: Corrupted File**

1. Identify via checksum (TODO: add MD5 hash column)
2. Restore from backup
3. Update database if path changed

---

## XII. Related Documentation

- [API 8.11 - Upload Attachment Test Guide](../api-guides/clinical-records/API_8.11_UPLOAD_ATTACHMENT.md)
- [API 8.12 - Get Attachments Test Guide](../api-guides/clinical-records/API_8.12_GET_ATTACHMENTS.md)
- [API 8.13 - Delete Attachment Test Guide](../api-guides/clinical-records/API_8.13_DELETE_ATTACHMENT.md)
- [Clinical Records API Overview](../API_DOCUMENTATION.md#8-clinical-records)

---

**Last Updated**: 2025-12-02
**Author**: GitHub Copilot
**Status**: Production Ready (Local Storage), S3 Migration Pending
