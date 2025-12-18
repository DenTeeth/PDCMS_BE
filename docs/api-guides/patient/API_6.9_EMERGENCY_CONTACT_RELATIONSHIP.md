# API 6.9: Emergency Contact Relationship Field

## Overview

The `emergency_contact_relationship` field has been added to the Patient entity to allow frontend applications to capture and display the relationship between the patient and their emergency contact person.

## Field Specifications

### Database Column

- **Table**: `patients`
- **Column Name**: `emergency_contact_relationship`
- **Data Type**: VARCHAR(100)
- **Nullable**: Yes (optional field)
- **Description**: Stores the relationship description between the patient and emergency contact person

### Common Values

- `Cha` - Father
- `Me` - Mother
- `Vo` - Wife
- `Chong` - Husband
- `Con` - Child
- `Anh` / `Chi` / `Em` - Siblings
- `Ban` - Friend
- `Bo` - Father (informal)
- `Ong noi` / `Ba noi` - Grandfather/Grandmother (paternal)
- `Ong ngoai` / `Ba ngoai` - Grandfather/Grandmother (maternal)

## API Integration

### 1. Create Patient (POST)

**Endpoint**: `POST /api/v1/patients`

**Request Body**:

```json
{
  "accountId": 25,
  "patientCode": "BN-1011",
  "firstName": "Nguyen Van",
  "lastName": "A",
  "email": "nguyenvana@email.com",
  "phone": "0901234567",
  "dateOfBirth": "1990-01-15",
  "address": "123 Nguyen Trai, Q1, TPHCM",
  "gender": "MALE",
  "medicalHistory": "Khong co",
  "allergies": "Khong co",
  "emergencyContactName": "Nguyen Thi B",
  "emergencyContactPhone": "0909876543",
  "emergencyContactRelationship": "Vo"
}
```

**Response**: 201 Created

```json
{
  "patientId": 11,
  "accountId": 25,
  "patientCode": "BN-1011",
  "firstName": "Nguyen Van",
  "lastName": "A",
  "email": "nguyenvana@email.com",
  "phone": "0901234567",
  "dateOfBirth": "1990-01-15",
  "address": "123 Nguyen Trai, Q1, TPHCM",
  "gender": "MALE",
  "medicalHistory": "Khong co",
  "allergies": "Khong co",
  "emergencyContactName": "Nguyen Thi B",
  "emergencyContactPhone": "0909876543",
  "emergencyContactRelationship": "Vo",
  "isActive": true,
  "createdAt": "2024-12-18T10:00:00",
  "updatedAt": "2024-12-18T10:00:00"
}
```

### 2. Update Patient (PATCH)

**Endpoint**: `PATCH /api/v1/patients/{patientCode}`

**Request Body** (partial update):

```json
{
  "emergencyContactName": "Tran Van C",
  "emergencyContactPhone": "0908765432",
  "emergencyContactRelationship": "Cha"
}
```

**Response**: 200 OK

```json
{
  "patientId": 1,
  "patientCode": "BN-1001",
  "firstName": "Nguyen Van",
  "lastName": "An",
  "emergencyContactName": "Tran Van C",
  "emergencyContactPhone": "0908765432",
  "emergencyContactRelationship": "Cha",
  "updatedAt": "2024-12-18T10:05:00"
}
```

### 3. Replace Patient (PUT)

**Endpoint**: `PUT /api/v1/patients/{patientCode}`

**Request Body** (full replacement - all fields required except optional ones):

```json
{
  "accountId": 14,
  "patientCode": "BN-1001",
  "firstName": "Nguyen Van",
  "lastName": "An",
  "email": "an.nv@email.com",
  "phone": "0901111111",
  "dateOfBirth": "1990-01-15",
  "address": "123 Le Loi, Q1, TPHCM",
  "gender": "MALE",
  "medicalHistory": "Tien su benh tim mach",
  "allergies": "Di ung penicillin",
  "emergencyContactName": "Nguyen Thi Hong",
  "emergencyContactPhone": "0901111222",
  "emergencyContactRelationship": "Me"
}
```

**Response**: 200 OK

```json
{
  "patientId": 1,
  "accountId": 14,
  "patientCode": "BN-1001",
  "firstName": "Nguyen Van",
  "lastName": "An",
  "email": "an.nv@email.com",
  "phone": "0901111111",
  "dateOfBirth": "1990-01-15",
  "address": "123 Le Loi, Q1, TPHCM",
  "gender": "MALE",
  "medicalHistory": "Tien su benh tim mach",
  "allergies": "Di ung penicillin",
  "emergencyContactName": "Nguyen Thi Hong",
  "emergencyContactPhone": "0901111222",
  "emergencyContactRelationship": "Me",
  "isActive": true,
  "updatedAt": "2024-12-18T10:10:00"
}
```

### 4. Get Patient Details (GET)

**Endpoint**: `GET /api/v1/patients/{patientCode}`

**Response**: 200 OK

```json
{
  "patientId": 1,
  "accountId": 14,
  "patientCode": "BN-1001",
  "firstName": "Nguyen Van",
  "lastName": "An",
  "email": "an.nv@email.com",
  "phone": "0901111111",
  "dateOfBirth": "1990-01-15",
  "address": "123 Le Loi, Q1, TPHCM",
  "gender": "MALE",
  "medicalHistory": "Tien su benh tim mach",
  "allergies": "Di ung penicillin",
  "emergencyContactName": "Nguyen Thi Hong",
  "emergencyContactPhone": "0901111222",
  "emergencyContactRelationship": "Me",
  "guardianName": null,
  "guardianPhone": null,
  "guardianRelationship": null,
  "guardianCitizenId": null,
  "consecutiveNoShows": 0,
  "isBookingBlocked": false,
  "isActive": true,
  "createdAt": "2024-12-01T10:00:00",
  "updatedAt": "2024-12-18T10:10:00"
}
```

### 5. Get All Patients (GET)

**Endpoint**: `GET /api/v1/patients?page=0&size=10`

**Response**: 200 OK

```json
{
  "content": [
    {
      "patientId": 1,
      "patientCode": "BN-1001",
      "firstName": "Nguyen Van",
      "lastName": "An",
      "phone": "0901111111",
      "email": "an.nv@email.com",
      "emergencyContactName": "Nguyen Thi Hong",
      "emergencyContactPhone": "0901111222",
      "emergencyContactRelationship": "Me",
      "isActive": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

## Validation Rules

1. **Maximum Length**: 100 characters
2. **Optional Field**: Can be null or empty
3. **No Format Restrictions**: Free text field to allow flexibility
4. **Encoding**: UTF-8 to support Vietnamese characters

## Permissions

The `emergency_contact_relationship` field uses existing patient permissions:

- **CREATE_PATIENT**: Required to set relationship during patient creation
- **UPDATE_PATIENT**: Required to modify relationship
- **VIEW_PATIENT**: Required to view relationship information

No additional permissions needed.

## Frontend Integration Guidelines

### Form Input

```html
<div class="form-group">
  <label for="emergencyContactRelationship">Moi quan he voi benh nhan</label>
  <input
    type="text"
    id="emergencyContactRelationship"
    name="emergencyContactRelationship"
    maxlength="100"
    placeholder="Vi du: Cha, Me, Vo, Chong, Con, Ban..."
    class="form-control"
  />
  <small class="form-text text-muted">
    Nhap moi quan he giua nguoi lien he khan cap va benh nhan
  </small>
</div>
```

### Display in Patient Card

```html
<div class="patient-emergency-contact">
  <h4>Thong tin lien he khan cap</h4>
  <p><strong>Ten:</strong> {{ patient.emergencyContactName }}</p>
  <p><strong>So dien thoai:</strong> {{ patient.emergencyContactPhone }}</p>
  <p>
    <strong>Moi quan he:</strong> {{ patient.emergencyContactRelationship ||
    'Chua cap nhat' }}
  </p>
</div>
```

## Testing

### Test Cases

1. **Create patient with relationship**: POST with emergencyContactRelationship field
2. **Create patient without relationship**: POST without emergencyContactRelationship (should accept null)
3. **Update only relationship**: PATCH with only emergencyContactRelationship
4. **Replace patient data**: PUT with full patient data including relationship
5. **Retrieve patient**: GET should return relationship in response
6. **Max length validation**: POST/PATCH with 101 characters should fail validation

### Sample Test Data

- BN-1001: emergencyContactRelationship = "Cha"
- BN-1002: emergencyContactRelationship = "Vo"
- BN-1003: emergencyContactRelationship = "Vo"
- BN-1006: emergencyContactRelationship = "Cha"
- BN-1008: emergencyContactRelationship = "Chong"
- BN-1010: emergencyContactRelationship = "Bo"

## Implementation Notes

1. Field added to Patient entity with JPA annotation: `@Column(name = "emergency_contact_relationship", length = 100)`
2. Validation annotation in DTOs: `@Size(max = 100)`
3. PatientMapper updated to map field in all conversion methods
4. PatientService response builder includes the field
5. Seed data populated with sample relationships

## Migration Notes

- **Database Schema**: Hibernate DDL auto-generation will create the column automatically
- **Existing Data**: Existing patients will have NULL value for this field until updated
- **Backward Compatibility**: Frontend applications not using this field will continue to work normally

## Changelog

- **2024-12-18**: Initial implementation of emergency_contact_relationship field
  - Added to Patient entity
  - Updated all DTOs (Create, Update, Replace, Response)
  - Updated PatientMapper and PatientService
  - Added seed data with sample values
  - Created API documentation
