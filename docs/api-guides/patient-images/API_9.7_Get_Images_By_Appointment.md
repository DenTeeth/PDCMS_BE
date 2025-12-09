# API 9.7: Get Images by Appointment

## Overview

Lấy tất cả hình ảnh liên quan đến một appointment cụ thể. API này truy vấn qua clinical record để lấy tất cả ảnh của appointment đó. Hữu ích khi cần xem tất cả ảnh của một buổi khám cụ thể.

**Khác biệt với API 9.6:**
- **API 9.6** lấy ảnh theo clinical_record_id
- **API 9.7** lấy ảnh theo appointment_id (một appointment có thể có nhiều ảnh qua clinical record)

**Relationship:**
```
Appointment (1) ----> (1) Clinical Record ----> (N) Patient Images
```

## Endpoint

```
GET /api/v1/patient-images/appointment/{appointmentId}
```

## Permission Required

`PATIENT_IMAGE_READ` - Assigned to ROLE_DENTIST, ROLE_ADMIN, ROLE_RECEPTIONIST

## Request

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| appointmentId | Long | Yes | ID của appointment |

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
    "imageId": 5,
    "patientId": 1,
    "patientName": "Nguyen Van A",
    "clinicalRecordId": 3,
    "imageUrl": "https://res.cloudinary.com/.../xray_appointment_12.jpg",
    "cloudinaryPublicId": "patients/patient_1/xray/xray_appointment_12",
    "imageType": "XRAY",
    "description": "X-quang trong buổi khám ngày 08/12",
    "capturedDate": "2025-12-08",
    "uploadedBy": 1,
    "uploaderName": "Le Anh Khoa",
    "createdAt": "2025-12-08T10:30:00",
    "updatedAt": "2025-12-08T10:30:00"
  },
  {
    "imageId": 6,
    "patientId": 1,
    "patientName": "Nguyen Van A",
    "clinicalRecordId": 3,
    "imageUrl": "https://res.cloudinary.com/.../photo_before.jpg",
    "cloudinaryPublicId": "patients/patient_1/before_treatment/photo_before",
    "imageType": "BEFORE_TREATMENT",
    "description": "Ảnh trước khi điều trị",
    "capturedDate": "2025-12-08",
    "uploadedBy": 1,
    "uploaderName": "Le Anh Khoa",
    "createdAt": "2025-12-08T10:45:00",
    "updatedAt": "2025-12-08T10:45:00"
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
| 404 | Not Found | Clinical record not found for appointment (appointment chưa có clinical record) |

## Curl Command

```bash
curl -X GET http://localhost:8080/api/v1/patient-images/appointment/12 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Frontend Integration

### Get Images for Appointment

```typescript
async function getImagesByAppointment(
  appointmentId: number,
  token: string
): Promise<PatientImage[]> {
  const response = await fetch(
    `http://localhost:8080/api/v1/patient-images/appointment/${appointmentId}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error('No clinical record found for this appointment');
    }
    throw new Error('Failed to fetch images');
  }

  return await response.json();
}
```

### Display in Appointment Detail Page

```typescript
// React/Vue component for Appointment Detail
const AppointmentDetail = ({ appointmentId }) => {
  const [appointment, setAppointment] = useState(null);
  const [images, setImages] = useState([]);
  const [loadingImages, setLoadingImages] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        // Load appointment details
        const appt = await getAppointmentDetails(appointmentId, accessToken);
        setAppointment(appt);

        // Load images for this appointment
        const imgs = await getImagesByAppointment(appointmentId, accessToken);
        setImages(imgs);
      } catch (error) {
        console.error('Failed to load data:', error);
        // If no clinical record yet, images will be empty
        setImages([]);
      } finally {
        setLoadingImages(false);
      }
    }

    loadData();
  }, [appointmentId]);

  return (
    <div>
      <h2>Appointment Details</h2>
      <div>
        <p>Appointment ID: {appointment?.appointmentId}</p>
        <p>Patient: {appointment?.patientName}</p>
        <p>Date: {appointment?.appointmentStartTime}</p>
        <p>Status: {appointment?.status}</p>
      </div>

      <h3>Images from this Appointment ({images.length})</h3>
      {loadingImages ? (
        <p>Loading images...</p>
      ) : images.length > 0 ? (
        <div className="image-gallery">
          {images.map(image => (
            <div key={image.imageId} className="image-card">
              <img src={image.imageUrl} alt={image.description} />
              <p className="image-type">{image.imageType}</p>
              <p className="description">{image.description}</p>
              <p className="date">{image.capturedDate}</p>
            </div>
          ))}
        </div>
      ) : (
        <p>Chưa có ảnh nào cho buổi khám này</p>
      )}
    </div>
  );
};
```

## Use Cases

1. **Appointment Review** - Xem lại tất cả ảnh chụp trong một buổi khám
2. **Treatment Session** - Hiển thị ảnh của session cụ thể
3. **Before/After Comparison** - So sánh ảnh trước và sau trong cùng một appointment
4. **Documentation** - Lưu trữ ảnh theo từng buổi khám
5. **Patient Portal** - Cho bệnh nhân xem ảnh của buổi khám của họ

## Business Rules

1. **Clinical Record Required:** Appointment phải có clinical record mới có thể có ảnh
2. **Sort Order:** Images sorted by created_at DESC (newest first)
3. **All Types:** Trả về tất cả image types
4. **No Pagination:** Full list (giả định mỗi appointment không có quá nhiều ảnh)
5. **Empty Result:** Trả về [] nếu appointment chưa có clinical record hoặc chưa có ảnh

## Data Flow

```
1. Appointment created (API 3.1)
   ↓
2. Clinical Record created for appointment (API 8.2)
   ↓
3. Images uploaded and linked to clinical record (API 9.1)
   ↓
4. Get images by appointment ID (API 9.7) ← THIS API
```

## Comparison with Other APIs

| API | Scope | Use Case |
|-----|-------|----------|
| **API 9.2** | All images of patient | Patient gallery, search across all appointments |
| **API 9.6** | Images of clinical record | Clinical record detail page |
| **API 9.7** | Images of appointment | Appointment detail page, session review |

**When to use API 9.7:**
- Bạn có appointment_id và muốn xem tất cả ảnh của appointment đó
- Hiển thị trong appointment detail page
- Không cần biết clinical_record_id

**When to use API 9.6:**
- Bạn đã có clinical_record_id
- Hiển thị trong clinical record detail page

**When to use API 9.2:**
- Muốn xem tất cả ảnh của bệnh nhân
- Cần filter theo type, date
- Patient image gallery

## Test Scenarios

### Test 1: Get Images for Appointment with Clinical Record and Images

```bash
# Prerequisites:
# 1. Appointment exists (e.g., appointmentId = 12)
# 2. Clinical record created for appointment
# 3. Images linked to clinical record

curl -X GET http://localhost:8080/api/v1/patient-images/appointment/12 \
  -H "Authorization: Bearer $TOKEN"

# Expected: Array with images
```

### Test 2: Get Images for Appointment without Clinical Record

```bash
# Appointment exists but no clinical record yet
curl -X GET http://localhost:8080/api/v1/patient-images/appointment/20 \
  -H "Authorization: Bearer $TOKEN"

# Expected: 404 - Clinical record not found for appointment
```

### Test 3: Get Images for Appointment with Clinical Record but No Images

```bash
# Appointment has clinical record but no images yet
curl -X GET http://localhost:8080/api/v1/patient-images/appointment/15 \
  -H "Authorization: Bearer $TOKEN"

# Expected: 200 OK with empty array []
```

### Test 4: Complete Workflow Test

```bash
TOKEN="YOUR_TOKEN"

# Step 1: Create appointment (assume returns appointmentId = 100)
# ... create appointment using appointment API ...

# Step 2: Create clinical record for appointment
curl -X POST http://localhost:8080/api/v1/clinical-records \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 100,
    "diagnosis": "Test diagnosis",
    "chiefComplaint": "Test complaint"
  }'
# Note clinicalRecordId from response (e.g., 50)

# Step 3: Upload image to Cloudinary (FE handles this)
# Assume get: imageUrl and cloudinaryPublicId

# Step 4: Create image record linked to clinical record
curl -X POST http://localhost:8080/api/v1/patient-images \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "clinicalRecordId": 50,
    "imageUrl": "https://res.cloudinary.com/test.jpg",
    "cloudinaryPublicId": "patients/patient_1/xray/test",
    "imageType": "XRAY",
    "description": "Test image for appointment"
  }'

# Step 5: Get images by appointment
curl -X GET http://localhost:8080/api/v1/patient-images/appointment/100 \
  -H "Authorization: Bearer $TOKEN"

# Expected: Array with the image we just created
```

## Error Handling

```typescript
async function loadAppointmentImages(appointmentId: number, token: string) {
  try {
    const images = await getImagesByAppointment(appointmentId, token);
    return images;
  } catch (error) {
    if (error.message.includes('No clinical record')) {
      // Appointment chưa có clinical record
      console.log('This appointment has no clinical record yet');
      return [];
    }
    // Other errors
    console.error('Failed to load images:', error);
    throw error;
  }
}
```

## Integration with Appointment Module

### Appointment with Images

```typescript
interface AppointmentWithImages {
  // Appointment fields
  appointmentId: number;
  patientId: number;
  patientName: string;
  employeeId: number;
  doctorName: string;
  appointmentStartTime: string;
  appointmentEndTime: string;
  status: string;
  
  // Images
  images: PatientImage[];
  imageCount: number;
}

async function getAppointmentWithImages(
  appointmentId: number,
  token: string
): Promise<AppointmentWithImages> {
  const appointment = await getAppointmentDetails(appointmentId, token);
  const images = await getImagesByAppointment(appointmentId, token);
  
  return {
    ...appointment,
    images,
    imageCount: images.length
  };
}
```

## Test Data

**Appointments (from seed):**
- Create appointments first using Appointment API
- Then create clinical records
- Then create images

**Test Credentials:**
- Username: `bacsi1`, Password: `123456` (ROLE_DENTIST)
- Username: `admin`, Password: `123456` (ROLE_ADMIN)
- Username: `reception1`, Password: `123456` (ROLE_RECEPTIONIST)

## Performance Notes

1. **Two Queries:**
   - First: Find clinical record by appointment_id
   - Second: Find images by clinical_record_id

2. **Indexes Used:**
   - `clinical_records.appointment_id` (unique index)
   - `patient_images.clinical_record_id` (index)

3. **Eager Loading:** Patient name and uploader name loaded in service

## Notes

- **Appointment-Clinical Record:** 1-1 relationship
- **Clinical Record-Images:** 1-N relationship
- **Result:** One appointment → one clinical record → many images
- **404 Error:** Nếu appointment chưa có clinical record, API trả về 404

---

**Module:** Patient Images (API 9.7)  
**Last Updated:** December 9, 2025  
**Version:** 1.0
