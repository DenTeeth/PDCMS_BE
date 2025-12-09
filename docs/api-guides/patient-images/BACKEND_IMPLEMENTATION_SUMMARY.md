# Patient Images Module - Backend Implementation Summary

## Overview

Đã implement module quản lý hình ảnh bệnh nhân với Cloudinary integration. Backend chỉ lưu metadata, FE handle upload file lên Cloudinary.

**7 API Endpoints** đã được implement và document riêng biệt.

## Simplified Design (So với FE docs)

**Điều chỉnh để phù hợp với quy mô đồ án:**

1. **Không có Cloudinary Service trong Backend** - FE tự upload lên Cloudinary
2. **Backend chỉ lưu metadata** - imageUrl, cloudinaryPublicId, imageType, description, capturedDate
3. **Đơn giản hóa Image Types** - Chỉ 6 types thay vì 12 types
4. **Không có batch delete** - Xóa từng ảnh một
5. **Không có statistics API** - Có thể thêm sau nếu cần
6. **Thêm Get by Appointment** - API mới để lấy ảnh theo appointmentId

## Database Schema

### ENUM Type (đã thêm vào seed data)

```sql
CREATE TYPE image_type AS ENUM ('XRAY', 'PHOTO', 'BEFORE_TREATMENT', 'AFTER_TREATMENT', 'SCAN', 'OTHER');
```

### Table Structure

```sql
CREATE TABLE patient_images (
    image_id BIGSERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL REFERENCES patients(patient_id) ON DELETE CASCADE,
    clinical_record_id INTEGER REFERENCES clinical_records(clinical_record_id) ON DELETE SET NULL,
    image_url TEXT NOT NULL,
    cloudinary_public_id VARCHAR(255) NOT NULL,
    image_type VARCHAR(50) NOT NULL,
    description TEXT,
    captured_date DATE,
    uploaded_by INTEGER REFERENCES employees(employee_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_patient_images_patient ON patient_images(patient_id);
CREATE INDEX idx_patient_images_clinical_record ON patient_images(clinical_record_id);
CREATE INDEX idx_patient_images_type ON patient_images(image_type);
CREATE INDEX idx_patient_images_created ON patient_images(created_at);
```

## API Endpoints (7 endpoints)

**Chi tiết từng API xem trong các file riêng:**

| API | Endpoint | Method | Description | Documentation |
|-----|----------|--------|-------------|---------------|
| 9.1 | `/api/v1/patient-images` | POST | Tạo image record | [API_9.1_Create_Patient_Image.md](API_9.1_Create_Patient_Image.md) |
| 9.2 | `/api/v1/patient-images/patient/{id}` | GET | Get images by patient với filters | [API_9.2_Get_Patient_Images_With_Filters.md](API_9.2_Get_Patient_Images_With_Filters.md) |
| 9.3 | `/api/v1/patient-images/{id}` | GET | Get image by ID | [API_9.3_Get_Patient_Image_By_ID.md](API_9.3_Get_Patient_Image_By_ID.md) |
| 9.4 | `/api/v1/patient-images/{id}` | PUT | Update metadata | [API_9.4_Update_Patient_Image_Metadata.md](API_9.4_Update_Patient_Image_Metadata.md) |
| 9.5 | `/api/v1/patient-images/{id}` | DELETE | Delete image | [API_9.5_Delete_Patient_Image.md](API_9.5_Delete_Patient_Image.md) |
| 9.6 | `/api/v1/patient-images/clinical-record/{id}` | GET | Get images by clinical record | [API_9.6_Get_Images_By_Clinical_Record.md](API_9.6_Get_Images_By_Clinical_Record.md) |
| 9.7 | `/api/v1/patient-images/appointment/{id}` | GET | Get images by appointment | [API_9.7_Get_Images_By_Appointment.md](API_9.7_Get_Images_By_Appointment.md) |

### Quick Reference

### 1. POST /api/v1/patient-images

**Description:** Tạo record sau khi FE đã upload lên Cloudinary  
**Details:** See [API_9.1_Create_Patient_Image.md](API_9.1_Create_Patient_Image.md)

**Request:**

```json
{
  "patientId": 1,
  "clinicalRecordId": null,
  "imageUrl": "https://res.cloudinary.com/.../test.jpg",
  "cloudinaryPublicId": "patients/patient_1/xray/test",
  "imageType": "XRAY",
  "description": "X-quang răng số 6",
  "capturedDate": "2025-12-08"
}
```

**Response:** 201 Created

```json
{
  "imageId": 1,
  "patientId": 1,
  "patientName": "Nguyen Van A",
  "imageUrl": "...",
  "cloudinaryPublicId": "...",
  "imageType": "XRAY",
  "description": "...",
  "capturedDate": "2025-12-08",
  "uploadedBy": 1,
  "uploaderName": "Le Anh Khoa",
  "createdAt": "2025-12-08T10:30:00",
  "updatedAt": "2025-12-08T10:30:00"
}
```

### 2. GET /api/v1/patient-images/patient/{patientId}

**Description:** Lấy danh sách ảnh của bệnh nhân với filter  
**Details:** See [API_9.2_Get_Patient_Images_With_Filters.md](API_9.2_Get_Patient_Images_With_Filters.md)

**Query Parameters:**

- `imageType` - Filter by type (XRAY, PHOTO, etc.)
- `clinicalRecordId` - Filter by clinical record
- `fromDate` - Filter from date (YYYY-MM-DD)
- `toDate` - Filter to date (YYYY-MM-DD)
- `page` - Page number (default: 0)
- `size` - Page size (default: 20)

**Response:** 200 OK

```json
{
  "images": [...],
  "currentPage": 0,
  "totalPages": 1,
  "totalElements": 1,
  "pageSize": 20
}
```

### 3. GET /api/v1/patient-images/{imageId}

**Description:** Lấy chi tiết một ảnh  
**Details:** See [API_9.3_Get_Patient_Image_By_ID.md](API_9.3_Get_Patient_Image_By_ID.md)

### 4. PUT /api/v1/patient-images/{imageId}

**Description:** Cập nhật metadata (không upload lại file)  
**Details:** See [API_9.4_Update_Patient_Image_Metadata.md](API_9.4_Update_Patient_Image_Metadata.md)

**Request:** (all fields optional)

```json
{
  "imageType": "PHOTO",
  "description": "Updated",
  "capturedDate": "2025-12-07",
  "clinicalRecordId": 5
}
```

**Response:** 200 OK

### 5. DELETE /api/v1/patient-images/{imageId}

**Description:** Xóa record trong database  
**Details:** See [API_9.5_Delete_Patient_Image.md](API_9.5_Delete_Patient_Image.md)

**Note:** FE nên xóa file trên Cloudinary riêng (hoặc để Cloudinary tự cleanup)

### 6. GET /api/v1/patient-images/clinical-record/{clinicalRecordId}

**Description:** Lấy tất cả ảnh của một clinical record  
**Details:** See [API_9.6_Get_Images_By_Clinical_Record.md](API_9.6_Get_Images_By_Clinical_Record.md)

### 7. GET /api/v1/patient-images/appointment/{appointmentId}

**Description:** Lấy tất cả ảnh của một appointment (qua clinical record)  
**Details:** See [API_9.7_Get_Images_By_Appointment.md](API_9.7_Get_Images_By_Appointment.md)

**New API:** Lấy ảnh theo appointmentId thay vì lấy tất cả ảnh của patient

## Permissions

Đã thêm 4 permissions vào seed data:

- `PATIENT_IMAGE_CREATE` - Tạo ảnh (Dentist, Admin)
- `PATIENT_IMAGE_READ` - Xem ảnh (Dentist, Admin, Receptionist)
- `PATIENT_IMAGE_UPDATE` - Cập nhật metadata (Dentist, Admin)
- `PATIENT_IMAGE_DELETE` - Xóa ảnh (Dentist, Admin)

**Role Assignments:**

- **ROLE_ADMIN**: All 4 permissions
- **ROLE_DENTIST**: All 4 permissions
- **ROLE_RECEPTIONIST**: PATIENT_IMAGE_READ only

## Files Created/Modified

### Backend Source Code

1. `patient/enums/ImageType.java` - Enum với 6 types
2. `patient/domain/PatientImage.java` - Entity với indexes
3. `patient/dto/request/CreatePatientImageRequest.java`
4. `patient/dto/request/UpdatePatientImageRequest.java`
5. `patient/dto/response/PatientImageResponse.java`
6. `patient/dto/response/PatientImageListResponse.java`
7. `patient/repository/PatientImageRepository.java` - JpaRepository + JpaSpecificationExecutor
8. `patient/specification/PatientImageSpecification.java` - Filter logic
9. `patient/service/PatientImageService.java` - Business logic
10. `patient/controller/PatientImageController.java` - REST endpoints

### Database Files

11. `schema.sql` - Added CREATE TABLE patient_images
12. `dental-clinic-seed-data.sql` - Added CREATE TYPE image_type + 4 permissions + role assignments

### Documentation

13. `docs/api-guides/patient-images/API_9.1_Create_Patient_Image.md`
14. `docs/api-guides/patient-images/API_9.2_Get_Patient_Images_With_Filters.md`
15. `docs/api-guides/patient-images/API_9.3_Get_Patient_Image_By_ID.md`
16. `docs/api-guides/patient-images/API_9.4_Update_Patient_Image_Metadata.md`
17. `docs/api-guides/patient-images/API_9.5_Delete_Patient_Image.md`
18. `docs/api-guides/patient-images/API_9.6_Get_Images_By_Clinical_Record.md`
19. `docs/api-guides/patient-images/API_9.7_Get_Images_By_Appointment.md` - NEW
20. `docs/api-guides/patient-images/BACKEND_IMPLEMENTATION_SUMMARY.md` - This file

### Test Scripts

21. `test_patient_images.sh` - Bash test script
22. `test_patient_images.ps1` - PowerShell test script

## Changes from FE Documentation

| FE Proposal                  | Backend Implementation      | Reason                 |
| ---------------------------- | --------------------------- | ---------------------- |
| 12 Image Types               | 6 Image Types               | Đơn giản hóa cho đồ án |
| Backend upload to Cloudinary | FE upload, BE save metadata | Giảm complexity        |
| CloudinaryService class      | No Cloudinary service       | FE handle upload       |
| Delete multiple images API   | Single delete only          | Đơn giản hóa           |
| Statistics API               | Not implemented             | Có thể thêm sau        |
| Batch operations             | Not implemented             | Out of scope           |
| Get by appointment           | **NEW - Implemented**       | **User request - thêm API lấy ảnh theo appointmentId** |

## Frontend Integration Guide

### Workflow

**1. Upload Image:**

```
User chọn file
  → FE upload lên Cloudinary
  → FE nhận {imageUrl, cloudinaryPublicId}
  → FE gọi POST /api/v1/patient-images với metadata
  → BE lưu record vào database
  → FE refresh gallery
```

**2. View Images by Patient:**

```
FE gọi GET /api/v1/patient-images/patient/{patientId}
  → BE query database với filters
  → BE return paginated list
  → FE hiển thị patient gallery
```

**2b. View Images by Appointment (NEW):**

```
FE gọi GET /api/v1/patient-images/appointment/{appointmentId}
  → BE tìm clinical record của appointment
  → BE lấy tất cả ảnh của clinical record đó
  → BE return array of images
  → FE hiển thị ảnh trong appointment detail page
```

**3. Delete Image:**

```
User click Delete
  → FE gọi DELETE /api/v1/patient-images/{imageId}
  → BE xóa record trong database
  → FE xóa file trên Cloudinary (optional)
  → FE refresh gallery
```

### FE Cần Thay Đổi

1. **Cloudinary Upload:** FE tự implement upload logic
2. **Image Types:** Chỉ 6 types thay vì 12
3. **Delete Flow:** Xóa từng ảnh, không có batch delete
4. **No Statistics:** Không có API GET statistics
5. **NEW - API 9.7:** Có thêm endpoint lấy ảnh theo appointment_id

### Cloudinary Folder Structure (FE Handle)

Recommend structure cho FE:

```
patients/
  patient_{patientId}/
    xray/
      patient_1_1733667890_abc123.jpg
    photo/
    before_treatment/
    after_treatment/
    scan/
    other/
```

Format Public ID: `patients/patient_{id}/{type}/patient_{id}_{timestamp}_{random}`

## Test Instructions

### Prerequisites

1. App phải đang chạy: `./mvnw.cmd spring-boot:run`
2. Database đã có seed data (patients, employees)

### Manual Testing

**1. Login:**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bacsi1","password":"123456"}'
```

**2. Create Image:**

```bash
curl -X POST http://localhost:8080/api/v1/patient-images \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "imageUrl": "https://res.cloudinary.com/demo/test.jpg",
    "cloudinaryPublicId": "patients/patient_1/xray/test",
    "imageType": "XRAY",
    "description": "Test image",
    "capturedDate": "2025-12-08"
  }'
```

**3. Get Images:**

```bash
curl -X GET http://localhost:8080/api/v1/patient-images/patient/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**4. Filter:**

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1?imageType=XRAY" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**5. Update:**

```bash
curl -X PUT http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description":"Updated"}'
```

**6. Get Images by Appointment (NEW):**

```bash
curl -X GET http://localhost:8080/api/v1/patient-images/appointment/12 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**7. Delete:**

```bash
curl -X DELETE http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Automated Testing

Run PowerShell script:

```powershell
cd d:\Code\PDCMS_BE
powershell -ExecutionPolicy Bypass -File test_patient_images.ps1
```

Or Bash script (Git Bash/WSL):

```bash
cd /d/Code/PDCMS_BE
bash test_patient_images.sh
```

## Security Notes

1. **Validation:** Backend validates:

   - Patient exists
   - Clinical record belongs to patient (if provided)
   - User has permission

2. **Authorization:**

   - CREATE/UPDATE/DELETE: Dentist, Admin only
   - READ: Dentist, Admin, Receptionist

3. **Data Integrity:**
   - Foreign key constraints
   - Cascade delete when patient deleted
   - Set NULL when clinical record deleted

## Known Limitations

1. **No Cloudinary Integration:** FE must handle upload/delete on Cloudinary
2. **No Batch Operations:** Delete one by one
3. **No Statistics API:** Count manually in FE
4. **Simple Image Types:** Only 6 types (can extend later)

## Future Enhancements (If Needed)

1. Add batch delete API
2. Add statistics/count API
3. Add image annotations
4. Add image comparison (before/after side-by-side)
5. Integrate with AI analysis
6. Add image quality validation

## API Documentation Structure

**Mỗi API có file MD riêng:**

- [API 9.1 - Create Patient Image](API_9.1_Create_Patient_Image.md)
- [API 9.2 - Get Patient Images with Filters](API_9.2_Get_Patient_Images_With_Filters.md)
- [API 9.3 - Get Patient Image by ID](API_9.3_Get_Patient_Image_By_ID.md)
- [API 9.4 - Update Patient Image Metadata](API_9.4_Update_Patient_Image_Metadata.md)
- [API 9.5 - Delete Patient Image](API_9.5_Delete_Patient_Image.md)
- [API 9.6 - Get Images by Clinical Record](API_9.6_Get_Images_By_Clinical_Record.md)
- [API 9.7 - Get Images by Appointment](API_9.7_Get_Images_By_Appointment.md) - **NEW**

---

**Status:** IMPLEMENTED AND READY FOR TESTING

**Date:** December 9, 2025

**Module:** Patient Images (API 9.1 - 9.7)

**Total APIs:** 7 endpoints (1 POST, 5 GET, 1 PUT, 1 DELETE)

**Test Account:** bacsi1 / 123456 (ROLE_DENTIST with full permissions)
