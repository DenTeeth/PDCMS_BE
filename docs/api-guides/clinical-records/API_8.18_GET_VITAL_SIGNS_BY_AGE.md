# API 8.18: Get Vital Signs Reference by Age

## Overview

This API retrieves applicable vital signs reference ranges for a specific patient age. It returns only the reference ranges that apply to the given age, making it easier for doctors to assess vital signs during clinical examinations without searching through all age groups.

## Endpoint

```
GET /api/v1/vital-signs-reference/by-age/{age}
```

## Authentication

- **Required**: Yes (JWT Bearer Token)
- **Roles**: ADMIN, DENTIST (or users with VIEW_VITAL_SIGNS_REFERENCE or WRITE_CLINICAL_RECORD permission)
- **Authorization**: `@PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('VIEW_VITAL_SIGNS_REFERENCE') or hasAuthority('WRITE_CLINICAL_RECORD')")`

## Request

### Headers

```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

### Path Parameters

| Parameter | Type    | Required | Description                       |
| --------- | ------- | -------- | --------------------------------- |
| age       | Integer | Yes      | Patient's age in years (0 to 150) |

### Query Parameters

None

### Request Body

None

## Response

### Success Response (200 OK)

Returns a filtered list of vital signs reference ranges applicable to the specified age.

**Example for age 35 (adult)**:

```json
[
  {
    "referenceId": 3,
    "vitalType": "BLOOD_PRESSURE_SYSTOLIC",
    "ageMin": 18,
    "ageMax": 59,
    "normalMin": 90.0,
    "normalMax": 120.0,
    "lowThreshold": 80.0,
    "highThreshold": 140.0,
    "unit": "mmHg",
    "description": "Huyet ap tam thu - Nguoi lon 18-59 tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  },
  {
    "referenceId": 9,
    "vitalType": "BLOOD_PRESSURE_DIASTOLIC",
    "ageMin": 18,
    "ageMax": 59,
    "normalMin": 60.0,
    "normalMax": 80.0,
    "lowThreshold": 50.0,
    "highThreshold": 90.0,
    "unit": "mmHg",
    "description": "Huyet ap tam truong - Nguoi lon 18-59 tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  },
  {
    "referenceId": 18,
    "vitalType": "HEART_RATE",
    "ageMin": 18,
    "ageMax": 64,
    "normalMin": 60.0,
    "normalMax": 100.0,
    "lowThreshold": 50.0,
    "highThreshold": 120.0,
    "unit": "bpm",
    "description": "Nhip tim - Nguoi lon 18-64 tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  },
  {
    "referenceId": 20,
    "vitalType": "OXYGEN_SATURATION",
    "ageMin": 0,
    "ageMax": null,
    "normalMin": 95.0,
    "normalMax": 100.0,
    "lowThreshold": 90.0,
    "highThreshold": null,
    "unit": "%",
    "description": "Do bao hoa oxy - Tat ca moi do tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  },
  {
    "referenceId": 21,
    "vitalType": "TEMPERATURE",
    "ageMin": 0,
    "ageMax": null,
    "normalMin": 36.1,
    "normalMax": 37.2,
    "lowThreshold": 35.0,
    "highThreshold": 38.0,
    "unit": "C",
    "description": "Nhiet do co the - Tat ca moi do tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  },
  {
    "referenceId": 26,
    "vitalType": "RESPIRATORY_RATE",
    "ageMin": 13,
    "ageMax": null,
    "normalMin": 12.0,
    "normalMax": 20.0,
    "lowThreshold": 10.0,
    "highThreshold": 25.0,
    "unit": "breaths/min",
    "description": "Nhip tho - Tu 13 tuoi tro len",
    "effectiveDate": "2025-01-01",
    "isActive": true
  }
]
```

**Example for age 5 (toddler)**:

```json
[
  {
    "referenceId": 1,
    "vitalType": "BLOOD_PRESSURE_SYSTOLIC",
    "ageMin": 0,
    "ageMax": 12,
    "normalMin": 80.0,
    "normalMax": 110.0,
    "lowThreshold": 70.0,
    "highThreshold": 120.0,
    "unit": "mmHg",
    "description": "Huyet ap tam thu - Tre em 0-12 tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  },
  {
    "referenceId": 7,
    "vitalType": "BLOOD_PRESSURE_DIASTOLIC",
    "ageMin": 0,
    "ageMax": 12,
    "normalMin": 50.0,
    "normalMax": 70.0,
    "lowThreshold": 40.0,
    "highThreshold": 80.0,
    "unit": "mmHg",
    "description": "Huyet ap tam truong - Tre em 0-12 tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  },
  {
    "referenceId": 14,
    "vitalType": "HEART_RATE",
    "ageMin": 2,
    "ageMax": 5,
    "normalMin": 80.0,
    "normalMax": 130.0,
    "lowThreshold": 70.0,
    "highThreshold": 150.0,
    "unit": "bpm",
    "description": "Nhip tim - Tre nho 2-5 tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  },
  {
    "referenceId": 20,
    "vitalType": "OXYGEN_SATURATION",
    "ageMin": 0,
    "ageMax": null,
    "normalMin": 95.0,
    "normalMax": 100.0,
    "lowThreshold": 90.0,
    "highThreshold": null,
    "unit": "%",
    "description": "Do bao hoa oxy - Tat ca moi do tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  },
  {
    "referenceId": 21,
    "vitalType": "TEMPERATURE",
    "ageMin": 0,
    "ageMax": null,
    "normalMin": 36.1,
    "normalMax": 37.2,
    "lowThreshold": 35.0,
    "highThreshold": 38.0,
    "unit": "C",
    "description": "Nhiet do co the - Tat ca moi do tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  },
  {
    "referenceId": 23,
    "vitalType": "RESPIRATORY_RATE",
    "ageMin": 2,
    "ageMax": 5,
    "normalMin": 20.0,
    "normalMax": 30.0,
    "lowThreshold": 15.0,
    "highThreshold": 40.0,
    "unit": "breaths/min",
    "description": "Nhip tho - Tre nho 2-5 tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  }
]
```

### Error Responses

#### 400 Bad Request

```json
{
  "statusCode": 400,
  "message": "Invalid age parameter",
  "timestamp": "2025-12-09T10:30:00"
}
```

#### 401 Unauthorized

```json
{
  "statusCode": 401,
  "message": "Unauthorized - Invalid or missing JWT token",
  "timestamp": "2025-12-09T10:30:00"
}
```

#### 403 Forbidden

```json
{
  "statusCode": 403,
  "message": "Access denied - User does not have required permission",
  "timestamp": "2025-12-09T10:30:00"
}
```

## Field Descriptions

Same as API 8.17. See [API 8.17 Field Descriptions](./API_8.17_GET_VITAL_SIGNS_REFERENCE.md#field-descriptions).

## Age Filtering Logic

The API filters reference ranges where:

- `age >= ageMin` AND (`age <= ageMax` OR `ageMax IS NULL`)

This means:

- If ageMax is NULL, the range applies to all ages from ageMin onwards
- If ageMax is not NULL, the range applies only within the specified bounds

**Examples**:

- Age 25 requesting HEART_RATE: Returns range with ageMin=18, ageMax=64
- Age 70 requesting HEART_RATE: Returns range with ageMin=65, ageMax=NULL
- Age 5 requesting TEMPERATURE: Returns range with ageMin=0, ageMax=NULL (applies to all ages)

## Business Rules

1. **Age Validation**: Age must be a valid integer between 0 and 150
2. **Active References Only**: Only returns records where isActive = true
3. **Complete Coverage**: For each vital type, at least one range should match any valid age
4. **No Gaps**: Age ranges should cover all possible ages without gaps
5. **Automatic Selection**: System automatically selects the most specific range for the given age

## Use Cases

### Use Case 1: Patient Examination Form

```
Doctor opens clinical record for patient (age 35)
-> System calls GET /api/v1/vital-signs-reference/by-age/35
-> Display reference ranges relevant to this patient
-> Doctor enters vital signs: BP 125/82, HR 75, Temp 36.8, SpO2 98%
-> System assesses each value against returned ranges
-> Shows BP systolic is ABOVE_NORMAL (normal: 90-120 for age 35)
-> All other values are NORMAL
```

### Use Case 2: Pediatric Patient

```
Dentist examines child patient (age 7)
-> System calls GET /api/v1/vital-signs-reference/by-age/7
-> Returns children's reference ranges (0-12 age group)
-> Dentist measures: BP 95/60, HR 85, Temp 36.5
-> System shows all values are NORMAL for age 7
-> No need to search through adult reference ranges
```

### Use Case 3: Elderly Patient

```
Doctor checks elderly patient (age 68)
-> System calls GET /api/v1/vital-signs-reference/by-age/68
-> Returns elderly reference ranges (65+ age group for HR, 60+ for BP)
-> Doctor measures: BP 135/80, HR 68
-> System shows BP systolic is NORMAL for elderly (normal: 90-130)
-> HR is NORMAL (normal: 60-100 for 65+)
```

### Use Case 4: Real-time Assessment

```
Nurse enters vital signs in real-time during triage
-> For each vital sign entered, system fetches age-specific reference
-> Immediately displays color-coded status (green=normal, yellow=caution, red=abnormal)
-> Alerts nurse if any value is ABNORMALLY_HIGH or ABNORMALLY_LOW
-> Nurse can quickly identify patients needing immediate attention
```

## Testing

### Test Case 1: Adult Patient (Age 35)

**Setup**: Login as doctor (username: doctor01, password: 123456)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference/by-age/35" \
  -H "Authorization: Bearer <DOCTOR_JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 200 OK
- Response contains exactly 6 reference ranges (one for each vital type)
- BLOOD_PRESSURE_SYSTOLIC: ageMin=18, ageMax=59
- BLOOD_PRESSURE_DIASTOLIC: ageMin=18, ageMax=59
- HEART_RATE: ageMin=18, ageMax=64
- OXYGEN_SATURATION: ageMin=0, ageMax=null
- TEMPERATURE: ageMin=0, ageMax=null
- RESPIRATORY_RATE: ageMin=13, ageMax=null

### Test Case 2: Child Patient (Age 8)

**Setup**: Login as doctor (username: doctor01, password: 123456)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference/by-age/8" \
  -H "Authorization: Bearer <DOCTOR_JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 200 OK
- Response contains exactly 6 reference ranges
- BLOOD_PRESSURE_SYSTOLIC: ageMin=0, ageMax=12
- BLOOD_PRESSURE_DIASTOLIC: ageMin=0, ageMax=12
- HEART_RATE: ageMin=6, ageMax=12
- OXYGEN_SATURATION: ageMin=0, ageMax=null
- TEMPERATURE: ageMin=0, ageMax=null
- RESPIRATORY_RATE: ageMin=6, ageMax=12

### Test Case 3: Newborn (Age 0)

**Setup**: Login as doctor (username: doctor01, password: 123456)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference/by-age/0" \
  -H "Authorization: Bearer <DOCTOR_JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 200 OK
- Response contains exactly 6 reference ranges
- BLOOD_PRESSURE_SYSTOLIC: ageMin=0, ageMax=12
- BLOOD_PRESSURE_DIASTOLIC: ageMin=0, ageMax=12
- HEART_RATE: ageMin=0, ageMax=1 (newborn-specific)
- OXYGEN_SATURATION: ageMin=0, ageMax=null
- TEMPERATURE: ageMin=0, ageMax=null
- RESPIRATORY_RATE: ageMin=0, ageMax=1 (newborn-specific)

### Test Case 4: Elderly Patient (Age 70)

**Setup**: Login as doctor (username: doctor01, password: 123456)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference/by-age/70" \
  -H "Authorization: Bearer <DOCTOR_JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 200 OK
- Response contains exactly 6 reference ranges
- BLOOD_PRESSURE_SYSTOLIC: ageMin=60, ageMax=null
- BLOOD_PRESSURE_DIASTOLIC: ageMin=60, ageMax=null
- HEART_RATE: ageMin=65, ageMax=null
- OXYGEN_SATURATION: ageMin=0, ageMax=null
- TEMPERATURE: ageMin=0, ageMax=null
- RESPIRATORY_RATE: ageMin=13, ageMax=null

### Test Case 5: Adolescent (Age 15)

**Setup**: Login as doctor (username: doctor01, password: 123456)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference/by-age/15" \
  -H "Authorization: Bearer <DOCTOR_JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 200 OK
- Response contains exactly 6 reference ranges
- BLOOD_PRESSURE_SYSTOLIC: ageMin=13, ageMax=17
- BLOOD_PRESSURE_DIASTOLIC: ageMin=13, ageMax=17
- HEART_RATE: ageMin=13, ageMax=17
- OXYGEN_SATURATION: ageMin=0, ageMax=null
- TEMPERATURE: ageMin=0, ageMax=null
- RESPIRATORY_RATE: ageMin=13, ageMax=null

### Test Case 6: Invalid Age (Negative)

**Setup**: Login as doctor (username: doctor01, password: 123456)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference/by-age/-5" \
  -H "Authorization: Bearer <DOCTOR_JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 400 Bad Request
- Error message about invalid age parameter

### Test Case 7: Invalid Age (Too High)

**Setup**: Login as doctor (username: doctor01, password: 123456)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference/by-age/200" \
  -H "Authorization: Bearer <DOCTOR_JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 200 OK (age is valid, just unusual)
- Response contains 6 reference ranges for elderly (ranges with ageMax=null)

### Test Case 8: Unauthorized Access

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference/by-age/35" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 401 Unauthorized
- Error message about missing JWT token

### Test Case 9: Forbidden Access (Patient Role)

**Setup**: Login as patient (username: patient001, password: 123456)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference/by-age/35" \
  -H "Authorization: Bearer <PATIENT_JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 403 Forbidden
- Error message about lacking required permission

## Implementation Notes

### Service Layer

- Method: `VitalSignsReferenceService.getReferencesByAge(Integer age)`
- Transaction: Read-only
- Query: Custom repository method using JPA criteria
- SQL: `SELECT * FROM vital_signs_reference WHERE is_active = true AND age_min <= :age AND (age_max IS NULL OR age_max >= :age)`

### Repository Layer

```java
@Query("SELECT v FROM VitalSignsReference v WHERE v.isActive = true AND v.ageMin <= :age AND (v.ageMax IS NULL OR v.ageMax >= :age)")
List<VitalSignsReference> findAllByAge(@Param("age") Integer age);
```

### Performance

- Indexed query on (vital_type, age_min, age_max) WHERE is_active = true
- Typical response contains 6 records (one per vital type)
- Very fast response time (<20ms)
- Results can be cached per age group

### Edge Cases Handled

1. **Age 0 (Newborn)**: Returns newborn-specific ranges
2. **Age at boundary**: Returns correct range when age equals ageMin or ageMax
3. **Very old age (100+)**: Returns ranges with ageMax=null
4. **Missing ranges**: Should not happen if seed data is complete

## Integration with Clinical Records

When creating or viewing a clinical record, this API is used to:

1. **Display Reference Ranges**: Show doctor the expected ranges for patient's age
2. **Assess Vital Signs**: System uses returned ranges to evaluate entered vital signs
3. **Color Coding**: UI shows green (normal), yellow (borderline), red (abnormal)
4. **Alerts**: Notify doctor if any vital sign is outside acceptable thresholds

### Example Integration Flow

```
1. Doctor opens clinical record form for patient ID 1 (age 34)
2. Frontend calls: GET /api/v1/vital-signs-reference/by-age/34
3. Frontend displays reference ranges next to vital signs input fields
4. Doctor enters: BP 125/85, HR 72, Temp 36.5, SpO2 98%
5. Frontend assesses each value against reference ranges:
   - BP systolic 125: ABOVE_NORMAL (normal 90-120) - Show yellow
   - BP diastolic 85: ABOVE_NORMAL (normal 60-80) - Show yellow
   - HR 72: NORMAL (normal 60-100) - Show green
   - Temp 36.5: NORMAL (normal 36.1-37.2) - Show green
   - SpO2 98%: NORMAL (normal 95-100) - Show green
6. Doctor saves clinical record with vitalSigns object
7. Backend also performs assessment and stores in vitalSignsAssessment field
```

## Related APIs

- **API 8.17**: Get All Vital Signs Reference - Returns all reference ranges (all ages)
- **API 8.1**: Get Clinical Record - Includes vitalSignsAssessment with evaluated status
- **API 8.2**: Create Clinical Record - Accepts vitalSigns and performs assessment
- **API 8.3**: Update Clinical Record - Updates vital signs and re-assesses

## Frontend Integration Example

### React/TypeScript

```typescript
interface VitalSignsReference {
  referenceId: number;
  vitalType: string;
  ageMin: number;
  ageMax: number | null;
  normalMin: number;
  normalMax: number;
  lowThreshold: number;
  highThreshold: number | null;
  unit: string;
  description: string;
  effectiveDate: string;
  isActive: boolean;
}

async function getVitalSignsReferenceByAge(
  age: number,
  token: string
): Promise<VitalSignsReference[]> {
  const response = await fetch(
    `http://localhost:8080/api/v1/vital-signs-reference/by-age/${age}`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    }
  );

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return await response.json();
}

// Usage in clinical record form
function ClinicalRecordForm({ patientAge }: { patientAge: number }) {
  const [references, setReferences] = useState<VitalSignsReference[]>([]);
  const [vitalSigns, setVitalSigns] = useState({
    bloodPressure: "",
    heartRate: "",
    temperature: "",
    oxygenSaturation: "",
  });

  useEffect(() => {
    const token = localStorage.getItem("jwt_token");
    if (token && patientAge) {
      getVitalSignsReferenceByAge(patientAge, token)
        .then((data) => setReferences(data))
        .catch((error) => console.error("Error loading references:", error));
    }
  }, [patientAge]);

  const getStatusColor = (vitalType: string, value: number): string => {
    const ref = references.find((r) => r.vitalType === vitalType);
    if (!ref) return "gray";

    if (
      value < ref.lowThreshold ||
      (ref.highThreshold && value > ref.highThreshold)
    ) {
      return "red"; // Abnormal
    } else if (value < ref.normalMin || value > ref.normalMax) {
      return "yellow"; // Borderline
    } else {
      return "green"; // Normal
    }
  };

  return (
    <form>
      <h3>Vital Signs (Patient Age: {patientAge})</h3>

      {references.map((ref) => (
        <div key={ref.referenceId} className="reference-info">
          <small>
            {ref.vitalType}: Normal {ref.normalMin}-{ref.normalMax} {ref.unit}
          </small>
        </div>
      ))}

      <div>
        <label>Blood Pressure:</label>
        <input
          type="text"
          value={vitalSigns.bloodPressure}
          onChange={(e) =>
            setVitalSigns({ ...vitalSigns, bloodPressure: e.target.value })
          }
          placeholder="120/80"
        />
      </div>

      <div>
        <label>Heart Rate:</label>
        <input
          type="number"
          value={vitalSigns.heartRate}
          onChange={(e) =>
            setVitalSigns({ ...vitalSigns, heartRate: e.target.value })
          }
          placeholder="72"
        />
      </div>

      {/* More input fields... */}
    </form>
  );
}
```

## Changelog

### Version 1.0 (2025-12-09)

- Initial API implementation
- Age-specific filtering of vital signs reference ranges
- Optimized for clinical record forms and patient examination
- Returns only applicable ranges for given age
- Integration with vital signs assessment system

## Notes

1. **Optimized Response**: Returns only 6 records (one per vital type) instead of all 26+ records
2. **Age-Specific**: Automatically selects the correct range for patient's age
3. **Performance**: Very fast queries, suitable for real-time assessment
4. **Clinical Workflow**: Designed for use in clinical record forms and examination screens
5. **No Emoji**: All code and documentation follow project standards
6. **Testing Passwords**: All test accounts use password "123456"
