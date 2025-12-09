# API 9.6: Get Images by Clinical Record

## Overview

Lấy tất cả hình ảnh liên quan đến một clinical record cụ thể. Hữu ích khi hiển thị ảnh trong clinical record detail page.

## Endpoint

```
GET /api/v1/patient-images/clinical-record/{clinicalRecordId}
```

## Permission Required

`PATIENT_IMAGE_READ` - Assigned to ROLE_DENTIST, ROLE_ADMIN, ROLE_RECEPTIONIST

## Request

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| clinicalRecordId | Long | Yes | ID của clinical record |

### Headers

```
Authorization: Bearer {accessToken}
```

## Response

### Success Response

**Status Code:** `200 OK`

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
  },
  {
    "imageId": 3,
    "patientId": 1,
    "patientName": "Nguyen Van A",
    "clinicalRecordId": 5,
    "imageUrl": "https://res.cloudinary.com/.../xray2.jpg",
    "cloudinaryPublicId": "patients/patient_1/xray/xray2",
    "imageType": "XRAY",
    "description": "X-quang kiểm tra",
    "capturedDate": "2025-12-08",
    "uploadedBy": 1,
    "uploaderName": "Le Anh Khoa",
    "createdAt": "2025-12-08T14:15:00",
    "updatedAt": "2025-12-08T14:15:00"
  }
]
```

### Empty Result

**Status Code:** `200 OK`

```json
[]
```

### Error Responses

| Status Code | Error Message | Description |
|-------------|---------------|-------------|
| 403 | Forbidden | Không có quyền PATIENT_IMAGE_READ |
| 404 | Not Found | Clinical record không tồn tại |

## Curl Command

```bash
curl -X GET http://localhost:8080/api/v1/patient-images/clinical-record/5 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Frontend Integration

### Get Images for Clinical Record

```typescript
async function getImagesByClinicalRecord(
  clinicalRecordId: number,
  token: string
): Promise<PatientImage[]> {
  const response = await fetch(
    `http://localhost:8080/api/v1/patient-images/clinical-record/${clinicalRecordId}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error('Clinical record not found');
    }
    throw new Error('Failed to fetch images');
  }

  return await response.json();
}
```

### Display in Clinical Record Page

```typescript
// React/Vue component for Clinical Record Detail
const ClinicalRecordDetail = ({ recordId }) => {
  const [images, setImages] = useState([]);
  
  useEffect(() => {
    async function loadImages() {
      try {
        const data = await getImagesByClinicalRecord(recordId, accessToken);
        setImages(data);
      } catch (error) {
        console.error('Failed to load images:', error);
      }
    }
    
    loadImages();
  }, [recordId]);

  return (
    <div>
      <h3>Clinical Record #{recordId}</h3>
      
      {/* Clinical record details */}
      
      <h4>Attached Images ({images.length})</h4>
      <div className="image-gallery">
        {images.map(image => (
          <div key={image.imageId} className="image-card">
            <img src={image.imageUrl} alt={image.description} />
            <p>{image.imageType}</p>
            <p>{image.description}</p>
          </div>
        ))}
      </div>
      
      {images.length === 0 && (
        <p>Không có ảnh nào được đính kèm</p>
      )}
    </div>
  );
};
```

## Use Cases

1. **Clinical Record Details** - Hiển thị tất cả ảnh trong clinical record detail page
2. **Treatment Documentation** - Review ảnh liên quan đến treatment session
3. **Progress Tracking** - So sánh ảnh before/after trong cùng một clinical record
4. **Report Generation** - Lấy ảnh để generate treatment report

## Business Rules

1. **Sort Order:** Images sorted by created_at DESC (newest first)
2. **All Types:** Trả về tất cả image types (không filter)
3. **No Pagination:** Trả về full list (giả định không quá nhiều ảnh per clinical record)
4. **Empty Array:** Return [] nếu không có ảnh nào

## Relationship

```
Clinical Record (1) -----> (N) Patient Images
```

- Một clinical record có thể có nhiều images
- Một image chỉ thuộc về một clinical record (hoặc null)
- Clinical record có relationship 1-1 với Appointment

## Comparison with API 9.2

| Feature | API 9.2 (Get by Patient) | API 9.6 (Get by Clinical Record) |
|---------|-------------------------|----------------------------------|
| Scope | Tất cả ảnh của patient | Chỉ ảnh của 1 clinical record |
| Filter | ✅ imageType, date range | ❌ No filters |
| Pagination | ✅ Yes | ❌ No (full list) |
| Use Case | Patient gallery | Clinical record detail |

## Test Scenarios

### Test 1: Get Images for Clinical Record with Images

```bash
# Step 1: Create clinical record image
curl -X POST http://localhost:8080/api/v1/patient-images \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "clinicalRecordId": 5,
    "imageUrl": "https://test.jpg",
    "cloudinaryPublicId": "test",
    "imageType": "PHOTO"
  }'

# Step 2: Get images by clinical record
curl -X GET http://localhost:8080/api/v1/patient-images/clinical-record/5 \
  -H "Authorization: Bearer $TOKEN"

# Expected: Array with at least 1 image
```

### Test 2: Get Images for Clinical Record without Images

```bash
# Assuming clinical record 10 exists but has no images
curl -X GET http://localhost:8080/api/v1/patient-images/clinical-record/10 \
  -H "Authorization: Bearer $TOKEN"

# Expected: Empty array []
```

### Test 3: Get Images for Non-Existent Clinical Record

```bash
curl -X GET http://localhost:8080/api/v1/patient-images/clinical-record/99999 \
  -H "Authorization: Bearer $TOKEN"

# Expected: 404 Not Found
```

## Integration with Clinical Records Module

### Clinical Record Structure

```typescript
interface ClinicalRecord {
  clinicalRecordId: number;
  appointmentId: number;
  diagnosis: string;
  vitalSigns: any;
  chiefComplaint: string;
  examinationFindings: string;
  treatmentNotes: string;
  followUpDate: string;
  
  // Can include images
  images?: PatientImage[];
}
```

### Load Clinical Record with Images

```typescript
async function loadClinicalRecordWithImages(recordId: number, token: string) {
  // Load clinical record details
  const record = await fetch(
    `http://localhost:8080/api/v1/clinical-records/${recordId}`,
    { headers: { 'Authorization': `Bearer ${token}` } }
  ).then(r => r.json());
  
  // Load associated images
  const images = await getImagesByClinicalRecord(recordId, token);
  
  return {
    ...record,
    images
  };
}
```

## Test Data

**Clinical Records (from seed or created):**
- Create clinical records using Clinical Records API first
- Then link images to clinical records
- Use this API to retrieve linked images

**Test Credentials:**
- Username: `bacsi1`, Password: `123456` (ROLE_DENTIST)
- Username: `admin`, Password: `123456` (ROLE_ADMIN)
- Username: `reception1`, Password: `123456` (ROLE_RECEPTIONIST)

## Performance Notes

- Query uses index on `clinical_record_id` column
- Fast lookup for specific clinical record
- No pagination needed (assumed small dataset per record)
- Eager loading of patient and uploader names

## Workflow Example

```
1. Dentist completes appointment
2. Dentist creates clinical record (API 8.2)
3. Dentist uploads images to Cloudinary
4. Dentist creates image records with clinicalRecordId (API 9.1)
5. Later, when viewing clinical record:
   - Load clinical record details (API 8.1)
   - Load associated images (API 9.6)
   - Display everything together
```

---

**Module:** Patient Images (API 9.6)  
**Last Updated:** December 9, 2025  
**Version:** 1.0
