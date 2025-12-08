# Patient Images API Test Guide (API 9.1 - 9.6)

## Overview

Module quản lý hình ảnh bệnh nhân với Cloudinary integration. FE upload ảnh lên Cloudinary, sau đó gửi metadata về Backend để lưu database.

**Cloudinary Folder Structure:**

```
patients/
  patient_{patientId}/
    xray/
    photo/
    before_treatment/
    after_treatment/
    scan/
    other/
```

**Image Types:**

- `XRAY` - X-quang
- `PHOTO` - Ảnh chụp thông thường
- `BEFORE_TREATMENT` - Trước điều trị
- `AFTER_TREATMENT` - Sau điều trị
- `SCAN` - Scan tài liệu
- `OTHER` - Khác

**Permissions:**

- `PATIENT_IMAGE_CREATE` - Tạo hình ảnh (Dentist, Admin)
- `PATIENT_IMAGE_READ` - Xem hình ảnh (Dentist, Admin, Receptionist)
- `PATIENT_IMAGE_UPDATE` - Cập nhật metadata (Dentist, Admin)
- `PATIENT_IMAGE_DELETE` - Xóa hình ảnh (Dentist, Admin)

---

## API 9.1: Create Patient Image

**Endpoint:** `POST /api/v1/patient-images`

**Description:** Tạo record hình ảnh sau khi FE đã upload lên Cloudinary.

**Permission:** `PATIENT_IMAGE_CREATE`

**Request Headers:**

```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**

```json
{
  "patientId": 1,
  "clinicalRecordId": null,
  "imageUrl": "https://res.cloudinary.com/demo/image/upload/v1/patients/patient_1/xray/image123.jpg",
  "cloudinaryPublicId": "patients/patient_1/xray/image123",
  "imageType": "XRAY",
  "description": "X-quang răng số 6",
  "capturedDate": "2025-12-08"
}
```

**Response:** `201 Created`

```json
{
  "imageId": 1,
  "patientId": 1,
  "patientName": "Nguyen Van A",
  "clinicalRecordId": null,
  "imageUrl": "https://res.cloudinary.com/demo/image/upload/v1/patients/patient_1/xray/image123.jpg",
  "cloudinaryPublicId": "patients/patient_1/xray/image123",
  "imageType": "XRAY",
  "description": "X-quang răng số 6",
  "capturedDate": "2025-12-08",
  "uploadedBy": 1,
  "uploaderName": "Le Anh Khoa",
  "createdAt": "2025-12-08T10:30:00",
  "updatedAt": "2025-12-08T10:30:00"
}
```

**Curl Command:**

```bash
curl -X POST http://localhost:8080/api/v1/patient-images \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "imageUrl": "https://res.cloudinary.com/demo/image/upload/v1/patients/patient_1/xray/test1.jpg",
    "cloudinaryPublicId": "patients/patient_1/xray/test1",
    "imageType": "XRAY",
    "description": "X-quang test",
    "capturedDate": "2025-12-08"
  }'
```

**Error Responses:**

- `400 Bad Request` - Invalid data, missing required fields
- `404 Not Found` - Patient not found
- `403 Forbidden` - Không có quyền PATIENT_IMAGE_CREATE

---

## API 9.2: Get Patient Images (with Filters)

**Endpoint:** `GET /api/v1/patient-images/patient/{patientId}`

**Description:** Lấy danh sách hình ảnh của bệnh nhân với filter và pagination.

**Permission:** `PATIENT_IMAGE_READ`

**Query Parameters:**

- `imageType` (optional) - Filter by image type: XRAY, PHOTO, etc.
- `clinicalRecordId` (optional) - Filter by clinical record
- `fromDate` (optional) - Filter from date (format: YYYY-MM-DD)
- `toDate` (optional) - Filter to date (format: YYYY-MM-DD)
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size

**Request Headers:**

```
Authorization: Bearer {accessToken}
```

**Response:** `200 OK`

```json
{
  "images": [
    {
      "imageId": 1,
      "patientId": 1,
      "patientName": "Nguyen Van A",
      "clinicalRecordId": null,
      "imageUrl": "https://res.cloudinary.com/.../test1.jpg",
      "cloudinaryPublicId": "patients/patient_1/xray/test1",
      "imageType": "XRAY",
      "description": "X-quang test",
      "capturedDate": "2025-12-08",
      "uploadedBy": 1,
      "uploaderName": "Le Anh Khoa",
      "createdAt": "2025-12-08T10:30:00",
      "updatedAt": "2025-12-08T10:30:00"
    }
  ],
  "currentPage": 0,
  "totalPages": 1,
  "totalElements": 1,
  "pageSize": 20
}
```

**Curl Commands:**

**Test Case 1: Get all images of patient**

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Test Case 2: Filter by image type**

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1?imageType=XRAY" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Test Case 3: Filter by date range**

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1?fromDate=2025-12-01&toDate=2025-12-31" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Test Case 4: Pagination**

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Error Responses:**

- `400 Bad Request` - Patient ID is required
- `404 Not Found` - Patient not found
- `403 Forbidden` - Không có quyền PATIENT_IMAGE_READ

---

## API 9.3: Get Patient Image by ID

**Endpoint:** `GET /api/v1/patient-images/{imageId}`

**Description:** Lấy chi tiết một hình ảnh.

**Permission:** `PATIENT_IMAGE_READ`

**Request Headers:**

```
Authorization: Bearer {accessToken}
```

**Response:** `200 OK`

```json
{
  "imageId": 1,
  "patientId": 1,
  "patientName": "Nguyen Van A",
  "clinicalRecordId": null,
  "imageUrl": "https://res.cloudinary.com/.../test1.jpg",
  "cloudinaryPublicId": "patients/patient_1/xray/test1",
  "imageType": "XRAY",
  "description": "X-quang test",
  "capturedDate": "2025-12-08",
  "uploadedBy": 1,
  "uploaderName": "Le Anh Khoa",
  "createdAt": "2025-12-08T10:30:00",
  "updatedAt": "2025-12-08T10:30:00"
}
```

**Curl Command:**

```bash
curl -X GET http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Error Responses:**

- `404 Not Found` - Image not found
- `403 Forbidden` - Không có quyền PATIENT_IMAGE_READ

---

## API 9.4: Update Patient Image Metadata

**Endpoint:** `PUT /api/v1/patient-images/{imageId}`

**Description:** Cập nhật metadata của hình ảnh (không upload lại file).

**Permission:** `PATIENT_IMAGE_UPDATE`

**Request Headers:**

```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:** (All fields optional)

```json
{
  "imageType": "BEFORE_TREATMENT",
  "description": "Ảnh trước điều trị - Updated",
  "capturedDate": "2025-12-07",
  "clinicalRecordId": 5
}
```

**Response:** `200 OK`

```json
{
  "imageId": 1,
  "patientId": 1,
  "patientName": "Nguyen Van A",
  "clinicalRecordId": 5,
  "imageUrl": "https://res.cloudinary.com/.../test1.jpg",
  "cloudinaryPublicId": "patients/patient_1/xray/test1",
  "imageType": "BEFORE_TREATMENT",
  "description": "Ảnh trước điều trị - Updated",
  "capturedDate": "2025-12-07",
  "uploadedBy": 1,
  "uploaderName": "Le Anh Khoa",
  "createdAt": "2025-12-08T10:30:00",
  "updatedAt": "2025-12-08T11:45:00"
}
```

**Curl Command:**

```bash
curl -X PUT http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "imageType": "BEFORE_TREATMENT",
    "description": "Updated description"
  }'
```

**Error Responses:**

- `404 Not Found` - Image not found
- `403 Forbidden` - Không có quyền PATIENT_IMAGE_UPDATE
- `400 Bad Request` - Clinical record not belongs to patient

---

## API 9.5: Delete Patient Image

**Endpoint:** `DELETE /api/v1/patient-images/{imageId}`

**Description:** Xóa hình ảnh khỏi database (Note: Cloudinary file phải được FE xóa riêng).

**Permission:** `PATIENT_IMAGE_DELETE`

**Request Headers:**

```
Authorization: Bearer {accessToken}
```

**Response:** `204 No Content`

**Curl Command:**

```bash
curl -X DELETE http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Error Responses:**

- `404 Not Found` - Image not found
- `403 Forbidden` - Không có quyền PATIENT_IMAGE_DELETE

---

## API 9.6: Get Images by Clinical Record

**Endpoint:** `GET /api/v1/patient-images/clinical-record/{clinicalRecordId}`

**Description:** Lấy tất cả hình ảnh liên quan đến một clinical record.

**Permission:** `PATIENT_IMAGE_READ`

**Request Headers:**

```
Authorization: Bearer {accessToken}
```

**Response:** `200 OK`

```json
[
  {
    "imageId": 2,
    "patientId": 1,
    "patientName": "Nguyen Van A",
    "clinicalRecordId": 5,
    "imageUrl": "https://res.cloudinary.com/.../photo1.jpg",
    "cloudinaryPublicId": "patients/patient_1/photo/photo1",
    "imageType": "PHOTO",
    "description": "Ảnh sau điều trị",
    "capturedDate": "2025-12-08",
    "uploadedBy": 1,
    "uploaderName": "Le Anh Khoa",
    "createdAt": "2025-12-08T14:00:00",
    "updatedAt": "2025-12-08T14:00:00"
  }
]
```

**Curl Command:**

```bash
curl -X GET http://localhost:8080/api/v1/patient-images/clinical-record/5 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Error Responses:**

- `404 Not Found` - Clinical record not found
- `403 Forbidden` - Không có quyền PATIENT_IMAGE_READ

---

## Complete Test Flow

### Step 1: Login as Dentist (bacsi1)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bacsi1",
    "password": "123456"
  }'
```

Save the `accessToken` from response.

### Step 2: Create Patient Image

```bash
TOKEN="YOUR_ACCESS_TOKEN"

curl -X POST http://localhost:8080/api/v1/patient-images \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "imageUrl": "https://res.cloudinary.com/demo/patients/patient_1/xray/test123.jpg",
    "cloudinaryPublicId": "patients/patient_1/xray/test123",
    "imageType": "XRAY",
    "description": "X-quang test flow",
    "capturedDate": "2025-12-08"
  }'
```

Save the `imageId` from response (e.g., `1`).

### Step 3: Get All Images of Patient

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1" \
  -H "Authorization: Bearer $TOKEN"
```

Expected: List with 1 image

### Step 4: Get Image by ID

```bash
curl -X GET http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer $TOKEN"
```

Expected: Image details

### Step 5: Update Image Metadata

```bash
curl -X PUT http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated via test flow",
    "imageType": "PHOTO"
  }'
```

Expected: Updated image data

### Step 6: Get Images with Filter

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1?imageType=PHOTO" \
  -H "Authorization: Bearer $TOKEN"
```

Expected: List with filtered images

### Step 7: Delete Image

```bash
curl -X DELETE http://localhost:8080/api/v1/patient-images/1 \
  -H "Authorization: Bearer $TOKEN"
```

Expected: 204 No Content

### Step 8: Verify Deletion

```bash
curl -X GET "http://localhost:8080/api/v1/patient-images/patient/1" \
  -H "Authorization: Bearer $TOKEN"
```

Expected: Empty list

---

## Test Data from Seed

**Patients:**

- Patient ID: 1 - Nguyen Van A (BN001)
- Patient ID: 2 - Tran Thi B (BN002)

**Employees (Dentists):**

- Employee ID: 1 - Le Anh Khoa (bacsi1)
- Employee ID: 2 - Trinh Cong Thai (bacsi2)

**Credentials:**

- Username: `bacsi1`, Password: `123456`
- Username: `bacsi2`, Password: `123456`
- Username: `admin`, Password: `123456`

---

## Notes

1. **Cloudinary Integration:** Backend chỉ lưu metadata. FE phải handle:

   - Upload file lên Cloudinary
   - Tạo folder structure: `patients/patient_{id}/{imageType}/`
   - Lấy `imageUrl` và `cloudinaryPublicId`
   - Gọi Backend API để tạo record

2. **Delete Flow:** Khi xóa ảnh:

   - Backend xóa record trong database
   - FE nên xóa file trên Cloudinary (optional, có thể để Cloudinary tự cleanup)

3. **Clinical Record Link:** Optional. Có thể link ảnh với clinical record sau bằng UPDATE API.

4. **Permissions:**

   - Dentist: Full CRUD
   - Admin: Full CRUD
   - Receptionist: Read only

5. **Pagination:** Default page size = 20. Adjust theo nhu cầu FE.

---

Last updated: December 8, 2025
