# API 8.17: Get All Vital Signs Reference Ranges

## Overview

This API retrieves all active vital signs reference ranges used by doctors to assess patient vital signs during clinical examinations. The reference ranges are age-specific and help determine if a patient's vital signs are normal, below normal, above normal, abnormally low, or abnormally high.

## Endpoint

```
GET /api/v1/vital-signs-reference
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

None

### Query Parameters

None

### Request Body

None

## Response

### Success Response (200 OK)

Returns a list of all active vital signs reference ranges, ordered by vital type and age range.

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
    "referenceId": 2,
    "vitalType": "BLOOD_PRESSURE_SYSTOLIC",
    "ageMin": 13,
    "ageMax": 17,
    "normalMin": 90.0,
    "normalMax": 120.0,
    "lowThreshold": 80.0,
    "highThreshold": 130.0,
    "unit": "mmHg",
    "description": "Huyet ap tam thu - Thanh thieu nien 13-17 tuoi",
    "effectiveDate": "2025-01-01",
    "isActive": true
  },
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
    "referenceId": 13,
    "vitalType": "HEART_RATE",
    "ageMin": 0,
    "ageMax": 1,
    "normalMin": 100.0,
    "normalMax": 160.0,
    "lowThreshold": 90.0,
    "highThreshold": 180.0,
    "unit": "bpm",
    "description": "Nhip tim - Tre so sinh",
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
    "referenceId": 22,
    "vitalType": "RESPIRATORY_RATE",
    "ageMin": 0,
    "ageMax": 1,
    "normalMin": 30.0,
    "normalMax": 60.0,
    "lowThreshold": 25.0,
    "highThreshold": 70.0,
    "unit": "breaths/min",
    "description": "Nhip tho - Tre so sinh",
    "effectiveDate": "2025-01-01",
    "isActive": true
  }
]
```

### Error Responses

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

- **referenceId**: Unique identifier for the reference range (Integer, auto-generated)
- **vitalType**: Type of vital sign measurement (String)
  - BLOOD_PRESSURE_SYSTOLIC: Systolic blood pressure (upper number)
  - BLOOD_PRESSURE_DIASTOLIC: Diastolic blood pressure (lower number)
  - HEART_RATE: Heart rate in beats per minute
  - OXYGEN_SATURATION: Blood oxygen saturation percentage
  - TEMPERATURE: Body temperature
  - RESPIRATORY_RATE: Breathing rate per minute
- **ageMin**: Minimum age for this reference range (Integer, inclusive)
- **ageMax**: Maximum age for this reference range (Integer, inclusive, NULL means no upper limit)
- **normalMin**: Lower bound of normal range (Decimal)
- **normalMax**: Upper bound of normal range (Decimal)
- **lowThreshold**: Threshold below which value is considered abnormally low (Decimal)
- **highThreshold**: Threshold above which value is considered abnormally high (Decimal, NULL means no upper threshold)
- **unit**: Unit of measurement (String: mmHg, bpm, %, C, breaths/min)
- **description**: Vietnamese description of the reference range (String)
- **effectiveDate**: Date when this reference became effective (Date: yyyy-MM-dd)
- **isActive**: Whether this reference is currently active (Boolean)

## Vital Sign Status Categories

When a patient's vital sign is assessed against the reference ranges, it falls into one of these categories:

1. **NORMAL**: Value is within normalMin and normalMax
2. **BELOW_NORMAL**: Value is between lowThreshold and normalMin
3. **ABOVE_NORMAL**: Value is between normalMax and highThreshold
4. **ABNORMALLY_LOW**: Value is below lowThreshold
5. **ABNORMALLY_HIGH**: Value is above highThreshold
6. **UNKNOWN**: No reference range found for patient's age

## Business Rules

1. **Age-Specific Ranges**: Each vital type has multiple reference ranges for different age groups
2. **Active References Only**: Only returns records where isActive = true
3. **Audit Support**: effectiveDate and isActive fields support tracking changes over time
4. **NULL ageMax**: When ageMax is NULL, the range applies to all ages from ageMin onwards
5. **Overlapping Prevention**: Age ranges should not overlap for the same vital type
6. **Ordering**: Results are ordered by vitalType (ascending) then ageMin (ascending)

## Use Cases

### Use Case 1: Clinical Record Form

```
Doctor opens clinical record form -> System loads vital signs reference table
-> Doctor enters patient vital signs (BP: 125/85, HR: 72, Temp: 37.0)
-> System shows reference ranges for patient's age
-> Doctor sees BP 125/85 is ABOVE_NORMAL for age 35 (normal: 90-120)
-> Doctor adds note about elevated blood pressure
```

### Use Case 2: Nurse Station Display

```
Nurse checks vital signs reference poster -> Clicks "View Digital Reference"
-> System calls GET /api/v1/vital-signs-reference
-> Display complete table of all reference ranges
-> Nurse can quickly look up normal ranges for any age group
```

### Use Case 3: Mobile App Reference

```
Doctor using mobile app during rounds -> Views patient chart
-> Taps "Vital Signs Guide" -> App calls this API
-> Shows complete reference table for quick lookup
-> Doctor can compare patient values to normal ranges
```

## Testing

### Test Case 1: Get All References (Doctor)

**Setup**: Login as doctor (username: doctor01, password: 123456)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference" \
  -H "Authorization: Bearer <DOCTOR_JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 200 OK
- Response contains multiple reference ranges (at least 26 records based on seed data)
- All returned records have isActive = true
- Records are ordered by vitalType then ageMin
- Includes all vital types: BLOOD_PRESSURE_SYSTOLIC, BLOOD_PRESSURE_DIASTOLIC, HEART_RATE, OXYGEN_SATURATION, TEMPERATURE, RESPIRATORY_RATE

**Verification**:

- Check that BLOOD_PRESSURE_SYSTOLIC has ranges for ages: 0-12, 13-17, 18-59, 60+
- Check that HEART_RATE has ranges for ages: 0-1, 2-5, 6-12, 13-17, 18-64, 65+
- Check that OXYGEN_SATURATION has one range for all ages (ageMin=0, ageMax=null)
- Check that TEMPERATURE has one range for all ages (ageMin=0, ageMax=null)

### Test Case 2: Get All References (Admin)

**Setup**: Login as admin (username: admin, password: 123456)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference" \
  -H "Authorization: Bearer <ADMIN_JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 200 OK
- Same response as doctor role
- Admin has full access to reference data

### Test Case 3: Unauthorized Access (No Token)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 401 Unauthorized
- Error message about missing JWT token

### Test Case 4: Forbidden Access (Patient Role)

**Setup**: Login as patient (username: patient001, password: 123456)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/vital-signs-reference" \
  -H "Authorization: Bearer <PATIENT_JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 403 Forbidden
- Error message about lacking required permission
- Patients should not have access to clinical reference data

### Test Case 5: Verify Age Range Coverage

**Expected Data Validation**:

For BLOOD_PRESSURE_SYSTOLIC, verify these age ranges exist:

- 0-12 years (children)
- 13-17 years (adolescents)
- 18-59 years (adults)
- 60+ years (elderly, ageMax=null)

For HEART_RATE, verify these age ranges exist:

- 0-1 years (newborns)
- 2-5 years (toddlers)
- 6-12 years (children)
- 13-17 years (adolescents)
- 18-64 years (adults)
- 65+ years (elderly, ageMax=null)

For OXYGEN_SATURATION and TEMPERATURE:

- Single range covering all ages (ageMin=0, ageMax=null)

## Implementation Notes

### Service Layer

- Method: `VitalSignsReferenceService.getAllActiveReferences()`
- Transaction: Read-only
- Query: `SELECT * FROM vital_signs_reference WHERE is_active = true ORDER BY vital_type ASC, age_min ASC`
- Mapping: VitalSignsReference entity to VitalSignsReferenceResponse DTO

### Performance

- Indexed query on (vital_type, age_min, age_max) WHERE is_active = true
- Small dataset (typically 20-30 records)
- Fast response time (<50ms)
- Can be cached in frontend for improved performance

### Data Integrity

- Foreign key constraints ensure referential integrity
- Unique constraints prevent duplicate age ranges for same vital type
- Audit trail supported via effectiveDate and isActive fields

## Related APIs

- **API 8.18**: Get Vital Signs Reference by Age - Returns only applicable ranges for specific age
- **API 8.1**: Get Clinical Record - Includes vitalSignsAssessment field with evaluated status
- **API 8.2**: Create Clinical Record - Accepts vitalSigns object for storage
- **API 8.3**: Update Clinical Record - Allows updating vital signs data

## Seed Data

The system is initialized with comprehensive reference ranges based on medical standards:

### Blood Pressure (mmHg)

**Systolic**:

- Children (0-12): 80-110 normal, 70-120 acceptable
- Adolescents (13-17): 90-120 normal, 80-130 acceptable
- Adults (18-59): 90-120 normal, 80-140 acceptable
- Elderly (60+): 90-130 normal, 80-150 acceptable

**Diastolic**:

- Children (0-12): 50-70 normal, 40-80 acceptable
- Adolescents (13-17): 60-80 normal, 50-85 acceptable
- Adults (18-59): 60-80 normal, 50-90 acceptable
- Elderly (60+): 60-85 normal, 50-95 acceptable

### Heart Rate (bpm)

- Newborns (0-1): 100-160 normal, 90-180 acceptable
- Toddlers (2-5): 80-130 normal, 70-150 acceptable
- Children (6-12): 70-110 normal, 60-130 acceptable
- Adolescents (13-17): 60-100 normal, 50-120 acceptable
- Adults (18-64): 60-100 normal, 50-120 acceptable
- Elderly (65+): 60-100 normal, 50-110 acceptable

### Oxygen Saturation (%)

- All ages: 95-100 normal, 90+ acceptable

### Body Temperature (Celsius)

- All ages: 36.1-37.2 normal, 35.0-38.0 acceptable

### Respiratory Rate (breaths/min)

- Newborns (0-1): 30-60 normal, 25-70 acceptable
- Infants (2-5): 20-30 normal, 15-40 acceptable
- Children (6-12): 18-25 normal, 12-35 acceptable
- 13+ years: 12-20 normal, 10-25 acceptable

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

async function getAllVitalSignsReferences(
  token: string
): Promise<VitalSignsReference[]> {
  const response = await fetch(
    "http://localhost:8080/api/v1/vital-signs-reference",
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

// Usage in component
function VitalSignsReferenceTable() {
  const [references, setReferences] = useState<VitalSignsReference[]>([]);

  useEffect(() => {
    const token = localStorage.getItem("jwt_token");
    if (token) {
      getAllVitalSignsReferences(token)
        .then((data) => setReferences(data))
        .catch((error) => console.error("Error loading references:", error));
    }
  }, []);

  return (
    <table>
      <thead>
        <tr>
          <th>Vital Type</th>
          <th>Age Range</th>
          <th>Normal Range</th>
          <th>Unit</th>
        </tr>
      </thead>
      <tbody>
        {references.map((ref) => (
          <tr key={ref.referenceId}>
            <td>{ref.vitalType}</td>
            <td>
              {ref.ageMin} - {ref.ageMax || "+"}
            </td>
            <td>
              {ref.normalMin} - {ref.normalMax}
            </td>
            <td>{ref.unit}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
```

## Changelog

### Version 1.0 (2025-12-09)

- Initial API implementation
- Comprehensive vital signs reference ranges based on medical standards
- Age-specific ranges for 6 vital types
- Support for inactive references (audit trail)
- Integration with clinical record vital signs assessment

## Notes

1. **Medical Standards**: Reference ranges are based on standard medical guidelines
2. **Age-Specific**: Each vital type has multiple ranges for different age groups
3. **Audit Trail**: effectiveDate and isActive support tracking changes over time
4. **Performance**: Small dataset, fast queries, suitable for caching
5. **Security**: Only medical staff can access this data
6. **No Emoji**: All code and documentation follow project standards
