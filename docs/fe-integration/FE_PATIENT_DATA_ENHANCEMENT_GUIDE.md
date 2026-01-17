# Frontend Integration Guide: Patient Data Enhancement (December 2025)

## Overview

This document outlines ALL API changes made to support comprehensive patient information display across the system. FE teams MUST update their implementations according to this guide.

## Critical Changes Summary

### 1. Patient Profile API (NEW)

- **Endpoint**: `GET /api/v1/patients/me/profile`
- **Purpose**: Mobile app patient portal
- **Response**: 24 fields including medical history, allergies, emergency contact, guardian info
- **See**: API_10.1_Get_Patient_Profile_For_Mobile.md

### 2. Appointment APIs - Enhanced Patient Data

- **Affected APIs**: ALL Appointment GET endpoints
- **Change**: `patient` object expanded from 4 fields to 19 fields
- **Old structure** (deprecated):

```json
{
  "patient": {
    "patientCode": "PT-AN",
    "fullName": "Nguyen Van An",
    "phone": "0909123456",
    "dateOfBirth": "1990-05-15"
  }
}
```

- **NEW structure** (all APIs now return):

```json
{
  "patient": {
    "patientId": 1,
    "patientCode": "PT-AN",
    "fullName": "Nguyen Van An",
    "phone": "0909123456",
    "email": "an.nv@email.com",
    "dateOfBirth": "1990-05-15",
    "age": 34,
    "gender": "MALE",
    "address": "123 Le Loi, Quan 1, TP.HCM",
    "medicalHistory": "Tien su viem loi, da dieu tri nam 2020",
    "allergies": "Di ung Penicillin",
    "emergencyContactName": "Doan Van Nam",
    "emergencyContactPhone": "0901111111",
    "guardianName": null,
    "guardianPhone": null,
    "guardianRelationship": null,
    "guardianCitizenId": null,
    "isActive": true,
    "consecutiveNoShows": 0,
    "isBookingBlocked": false,
    "bookingBlockReason": null
  }
}
```

**Affected Appointment Endpoints:**

- `GET /api/v1/appointments` - List all appointments
- `GET /api/v1/appointments/{code}` - Get appointment by code
- `POST /api/v1/appointments` - Create appointment (response changed)
- `PUT /api/v1/appointments/{code}/reschedule` - Reschedule (response changed)
- `PATCH /api/v1/appointments/{code}/delay` - Delay (response changed)

### 3. Treatment Plan APIs - Enhanced Patient Data

- **Affected APIs**: ALL Treatment Plan GET endpoints
- **Change**: Same as Appointment - `patient` object now has 19 fields

**Affected Treatment Plan Endpoints:**

- `GET /api/v1/treatment-plans` - List treatment plans
- `GET /api/v1/treatment-plans/{code}` - Get treatment plan detail
- `POST /api/v1/treatment-plans/from-template` - Create from template (response changed)
- `POST /api/v1/treatment-plans/custom` - Create custom plan (response changed)

### 4. Clinical Record API - Enhanced Patient Data + Vital Signs Assessment

- **Endpoint**: `GET /api/v1/appointments/{appointmentId}/clinical-record`
- **Changes**:
  1. `patient` object expanded to 13 fields (includes medical history, allergies, emergency contact, guardian)
  2. NEW `vitalSignsAssessment` array added

**OLD Response**:

```json
{
  "clinicalRecordId": 1,
  "diagnosis": "Gingivitis",
  "vitalSigns": {
    "blood_pressure": "120/80",
    "heart_rate": "72",
    "temperature": "36.5"
  },
  "patient": {
    "patientId": 1,
    "patientCode": "PT-AN",
    "fullName": "Nguyen Van An",
    "phone": "0909123456",
    "email": "an.nv@email.com",
    "dateOfBirth": "1990-05-15",
    "gender": "MALE"
  }
}
```

**NEW Response** (added fields):

```json
{
  "clinicalRecordId": 1,
  "diagnosis": "Gingivitis",
  "vitalSigns": {
    "blood_pressure": "120/80",
    "heart_rate": "72",
    "temperature": "36.5",
    "oxygen_saturation": "98"
  },
  "vitalSignsAssessment": [
    {
      "vitalType": "BLOOD_PRESSURE_SYSTOLIC",
      "value": 120,
      "unit": "mmHg",
      "status": "NORMAL",
      "normalMin": 90,
      "normalMax": 120,
      "message": "Binh thuong (90-120 mmHg)"
    },
    {
      "vitalType": "BLOOD_PRESSURE_DIASTOLIC",
      "value": 80,
      "unit": "mmHg",
      "status": "NORMAL",
      "normalMin": 60,
      "normalMax": 80,
      "message": "Binh thuong (60-80 mmHg)"
    },
    {
      "vitalType": "HEART_RATE",
      "value": 72,
      "unit": "bpm",
      "status": "NORMAL",
      "normalMin": 60,
      "normalMax": 100,
      "message": "Binh thuong (60-100 bpm)"
    },
    {
      "vitalType": "OXYGEN_SATURATION",
      "value": 98,
      "unit": "%",
      "status": "NORMAL",
      "normalMin": 95,
      "normalMax": 100,
      "message": "Binh thuong (95-100 %)"
    }
  ],
  "patient": {
    "patientId": 1,
    "patientCode": "PT-AN",
    "fullName": "Nguyen Van An",
    "phone": "0909123456",
    "email": "an.nv@email.com",
    "dateOfBirth": "1990-05-15",
    "age": 34,
    "gender": "MALE",
    "address": "123 Le Loi, Quan 1, TP.HCM",
    "medicalHistory": "Tien su viem loi, da dieu tri nam 2020",
    "allergies": "Di ung Penicillin",
    "emergencyContactName": "Doan Van Nam",
    "emergencyContactPhone": "0901111111",
    "guardianName": null,
    "guardianPhone": null,
    "guardianRelationship": null,
    "guardianCitizenId": null
  }
}
```

### 5. Vital Signs Reference API (NEW)

- **Purpose**: Get normal/abnormal thresholds for vital signs by age
- **Endpoints**:
  - `GET /api/v1/vital-signs-reference` - Get all active references
  - `GET /api/v1/vital-signs-reference/by-age/{age}` - Get references for specific age

**Response Example**:

```json
[
  {
    "referenceId": 3,
    "vitalType": "BLOOD_PRESSURE_SYSTOLIC",
    "ageMin": 18,
    "ageMax": 59,
    "normalMin": 90,
    "normalMax": 120,
    "lowThreshold": 80,
    "highThreshold": 140,
    "unit": "mmHg",
    "description": "Huyet ap tam thu - Nguoi lon 18-59 tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  }
]
```

## Frontend Implementation Requirements

### Mobile App (Android/iOS)

#### 1. Patient Profile Screen

**API**: `GET /api/v1/patients/me/profile`

**UI Components Needed**:

- Personal Info Section: name, email, phone, DOB, age, gender, address
- Medical Info Section: **Medical History** (text area), **Allergies** (highlighted box with warning icon)
- Emergency Contact Section: name + phone
- Guardian Section (show only if guardianName != null): name, phone, relationship, citizen ID
- Booking Status: Show warning if `isBookingBlocked == true`

**Sample Implementation (React Native)**:

```jsx
const PatientProfileScreen = () => {
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    const response = await api.get("/patients/me/profile", {
      headers: { Authorization: `Bearer ${token}` },
    });
    setProfile(response.data);
  };

  return (
    <ScrollView>
      <Section title="Thong tin ca nhan">
        <Text>Ten: {profile.fullName}</Text>
        <Text>Tuoi: {profile.age}</Text>
        <Text>Email: {profile.email}</Text>
        <Text>Dien thoai: {profile.phone}</Text>
      </Section>

      {profile.allergies && (
        <AlertBox type="warning">
          <Icon name="warning" />
          <Text>Di ung: {profile.allergies}</Text>
        </AlertBox>
      )}

      {profile.medicalHistory && (
        <Section title="Tien su benh">
          <Text>{profile.medicalHistory}</Text>
        </Section>
      )}

      <Section title="Lien he khan cap">
        <Text>{profile.emergencyContactName}</Text>
        <Text>{profile.emergencyContactPhone}</Text>
      </Section>

      {profile.guardianName && (
        <Section title="Nguoi giam ho">
          <Text>Ho ten: {profile.guardianName}</Text>
          <Text>Quan he: {profile.guardianRelationship}</Text>
          <Text>Dien thoai: {profile.guardianPhone}</Text>
        </Section>
      )}
    </ScrollView>
  );
};
```

#### 2. Appointment List Screen

**API**: `GET /api/v1/appointments`

**Changes**:

- Display patient allergies in appointment card (if present)
- Show age next to patient name
- Add medical history tooltip/expandable section

### Desktop App (React/Vue)

#### 1. Appointment Detail Page

**API**: `GET /api/v1/appointments/{code}`

**UI Changes Needed**:

```jsx
// OLD
<PatientInfo>
  <Text>Patient: {appointment.patient.fullName}</Text>
  <Text>Phone: {appointment.patient.phone}</Text>
</PatientInfo>

// NEW - Add these sections
<PatientInfo>
  <PersonalSection>
    <Text>Patient: {appointment.patient.fullName} (Age: {appointment.patient.age})</Text>
    <Text>Phone: {appointment.patient.phone}</Text>
    <Text>Email: {appointment.patient.email}</Text>
  </PersonalSection>

  {appointment.patient.allergies && (
    <AlertSection type="danger">
      <Icon name="warning" color="red" />
      <Text>ALLERGIES: {appointment.patient.allergies}</Text>
    </AlertSection>
  )}

  {appointment.patient.medicalHistory && (
    <MedicalSection>
      <Label>Medical History:</Label>
      <Text>{appointment.patient.medicalHistory}</Text>
    </MedicalSection>
  )}

  {appointment.patient.guardianName && (
    <GuardianSection>
      <Label>Guardian (Minor Patient):</Label>
      <Text>{appointment.patient.guardianName} ({appointment.patient.guardianRelationship})</Text>
      <Text>Phone: {appointment.patient.guardianPhone}</Text>
    </GuardianSection>
  )}
</PatientInfo>
```

#### 2. Clinical Record Page

**API**: `GET /api/v1/appointments/{appointmentId}/clinical-record`

**NEW: Vital Signs Assessment Display**:

```jsx
const VitalSignsDisplay = ({ vitalSigns, vitalSignsAssessment }) => {
  return (
    <div>
      <h3>Vital Signs</h3>
      <div className="vital-signs-grid">
        {vitalSignsAssessment.map(assessment => (
          <VitalSignCard
            key={assessment.vitalType}
            type={assessment.vitalType}
            value={assessment.value}
            unit={assessment.unit}
            status={assessment.status}
            message={assessment.message}
            className={getStatusClass(assessment.status)}
          />
        ))}
      </div>
    </div>
  );
};

const getStatusClass = (status) => {
  switch(status) {
    case 'NORMAL': return 'vital-normal';
    case 'ABOVE_NORMAL': return 'vital-warning';
    case 'BELOW_NORMAL': return 'vital-warning';
    case 'ABNORMALLY_HIGH': return 'vital-danger';
    case 'ABNORMALLY_LOW': return 'vital-danger';
    default: return 'vital-unknown';
  }
};

// CSS
.vital-normal { background: #d4edda; border-left: 4px solid #28a745; }
.vital-warning { background: #fff3cd; border-left: 4px solid #ffc107; }
.vital-danger { background: #f8d7da; border-left: 4px solid #dc3545; }
```

**Patient Section Enhancement**:

```jsx
<PatientSection>
  <h3>Patient Information</h3>
  <div className="patient-details">
    <div className="basic-info">
      <Text>Name: {clinicalRecord.patient.fullName}</Text>
      <Text>Age: {clinicalRecord.patient.age}</Text>
      <Text>Gender: {clinicalRecord.patient.gender}</Text>
    </div>

    {clinicalRecord.patient.allergies && (
      <AlertBox type="danger">
        <Icon name="alert-triangle" />
        <strong>ALLERGIES:</strong> {clinicalRecord.patient.allergies}
      </AlertBox>
    )}

    {clinicalRecord.patient.medicalHistory && (
      <InfoBox>
        <Label>Medical History:</Label>
        <Text>{clinicalRecord.patient.medicalHistory}</Text>
      </InfoBox>
    )}

    <EmergencyContact>
      <Label>Emergency Contact:</Label>
      <Text>{clinicalRecord.patient.emergencyContactName}</Text>
      <Text>{clinicalRecord.patient.emergencyContactPhone}</Text>
    </EmergencyContact>
  </div>
</PatientSection>
```

#### 3. Treatment Plan Page

**API**: `GET /api/v1/treatment-plans/{code}`

**Similar changes** as Appointment Detail - add medical history, allergies, guardian info display.

## Database Changes

### New Table: vital_signs_reference

```sql
CREATE TABLE vital_signs_reference (
    reference_id SERIAL PRIMARY KEY,
    vital_type VARCHAR(50) NOT NULL,
    age_min INTEGER NOT NULL,
    age_max INTEGER,
    normal_min DECIMAL(10,2),
    normal_max DECIMAL(10,2),
    low_threshold DECIMAL(10,2),
    high_threshold DECIMAL(10,2),
    unit VARCHAR(20) NOT NULL,
    description TEXT,
    effective_date DATE NOT NULL DEFAULT CURRENT_DATE,
    is_active BOOLEAN DEFAULT TRUE
);
```

### Updated Table: patients

**Added columns**:

- `medical_history` TEXT
- `allergies` TEXT
- `emergency_contact_name` VARCHAR(100)
- `emergency_contact_phone` VARCHAR(15)
- `guardian_name` VARCHAR(100)
- `guardian_phone` VARCHAR(15)
- `guardian_relationship` VARCHAR(50)
- `guardian_citizen_id` VARCHAR(20)
- `consecutive_no_shows` INTEGER DEFAULT 0
- `is_booking_blocked` BOOLEAN DEFAULT FALSE
- `booking_block_reason` VARCHAR(500)
- `blocked_at` TIMESTAMP

## Testing Checklist

### Mobile App Testing

- [ ] Login as patient001 (adult patient)
- [ ] View profile - verify all 24 fields display correctly
- [ ] Check allergies show with warning icon
- [ ] Login as patient010 (minor patient age 13)
- [ ] Verify guardian section displays
- [ ] Test appointment list shows patient age

### Desktop App Testing - Appointments

- [ ] Open appointment list
- [ ] Verify patient age displays next to name
- [ ] Click appointment detail
- [ ] Verify allergies show in red alert box
- [ ] Verify medical history section displays
- [ ] For Patient 10 appointments, verify guardian info shows

### Desktop App Testing - Treatment Plans

- [ ] Open treatment plan list
- [ ] Click treatment plan detail
- [ ] Verify patient medical info displays
- [ ] Test with Patient 3 (has allergies: "Di ung Lidocaine")

### Desktop App Testing - Clinical Records

- [ ] Open clinical record for Appointment 1
- [ ] Verify vital signs assessment displays
- [ ] Check blood pressure shows "NORMAL" status with green background
- [ ] Verify allergies display prominently
- [ ] Test reference API: GET /vital-signs-reference/by-age/34
- [ ] Verify correct reference ranges returned

## Migration Strategy

### Phase 1: Backend Deployment (DONE)

- Database schema updated
- APIs enhanced
- Seed data populated

### Phase 2: Frontend Updates (IN PROGRESS)

**Priority 1 (Critical - Safety)**:

1. Update Clinical Record page to display allergies prominently
2. Update Appointment detail to show allergies
3. Update Treatment Plan detail to show allergies

**Priority 2 (High - UX)**: 4. Implement mobile patient profile screen 5. Add vital signs assessment display 6. Update appointment/treatment plan lists

**Priority 3 (Medium)**: 7. Add medical history display across all screens 8. Implement guardian info display for minors 9. Add emergency contact display

### Phase 3: Testing & Validation

- Cross-browser testing
- Mobile device testing (iOS + Android)
- Accessibility testing
- Performance testing with large datasets

## Backward Compatibility

**BREAKING CHANGES**: None - all changes are additive

- Existing fields remain unchanged
- New fields added to existing response objects
- FE can ignore new fields initially
- Gradual migration supported

## Performance Considerations

- Patient data fetched once per appointment/treatment plan
- No additional database queries for new fields (already joined)
- Vital signs assessment computed in-memory (no database lookup per request)
- Reference table cached (43 rows total, rarely changes)

## Security Notes

- Patient Portal API (`/patients/me/profile`) secured with `@PreAuthorize("hasRole('PATIENT')")`
- Patients can ONLY see their own data
- Vital Signs Reference API requires DENTIST/ADMIN/MANAGER role
- All existing appointment/treatment plan authorization logic unchanged

## Support

For questions or issues:

1. Check updated API documentation in `docs/api-guides/`
2. Test with seed data (password: 123456 for all accounts)
3. Verify app running for at least 40 seconds before testing

## Affected API Documentation Files (ALL UPDATED)

**See individual MD files for complete request/response examples:**

- API_10.1_Get_Patient_Profile_For_Mobile.md (NEW)
- API_8.1_GET_CLINICAL_RECORD.md (UPDATED - added vitalSignsAssessment, enhanced patient)
- All Appointment API files (patient object enhanced)
- All Treatment Plan API files (patient object enhanced)
