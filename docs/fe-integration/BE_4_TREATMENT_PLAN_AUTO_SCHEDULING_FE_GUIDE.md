# BE_4: Treatment Plan Auto-Scheduling - Frontend Integration Guide

**Version**: 1.0  
**Date**: December 11, 2025  
**Status**: ✅ Fully Implemented and Tested

---

## Table of Contents

1. [Overview](#overview)
2. [Key Features](#key-features)
3. [Database Schema Changes](#database-schema-changes)
4. [API Endpoints](#api-endpoints)
5. [Service Constraint Fields](#service-constraint-fields)
6. [Holiday Management](#holiday-management)
7. [Integration Examples](#integration-examples)
8. [Validation Rules](#validation-rules)
9. [Error Handling](#error-handling)
10. [Testing Scenarios](#testing-scenarios)

---

## Overview

The treatment plan auto-scheduling feature automatically calculates appointment dates for multi-step treatment plans while:
- ✅ Respecting service-specific timing constraints
- ✅ Skipping holidays (dynamic per year)
- ✅ Enforcing daily appointment limits
- ✅ Ensuring proper recovery time between procedures

---

## Key Features

### 1. **Dynamic Holiday Detection**
- Holidays stored per year (e.g., Tết 2025 vs Tết 2026)
- Automatic skipping of holidays in scheduling
- Admin can add/update holidays for future years

### 2. **Service Timing Constraints**
Each service can have:
- **Minimum Preparation Days**: Days needed before procedure
- **Recovery Days**: Recovery period after procedure
- **Spacing Days**: Minimum days between same service appointments
- **Max Appointments Per Day**: Daily limit for specific services

### 3. **Automatic Scheduling**
- Distributes appointments across estimated treatment duration
- Calculates only working days (skips holidays)
- Respects all service constraints

### 4. **Validation**
- Validates appointment dates against holidays
- Checks service constraints before booking
- Prevents exceeding daily appointment limits

---

## Database Schema Changes

### Services Table - New Columns

```sql
ALTER TABLE services ADD COLUMN minimum_preparation_days INTEGER;
ALTER TABLE services ADD COLUMN recovery_days INTEGER;
ALTER TABLE services ADD COLUMN spacing_days INTEGER;
ALTER TABLE services ADD COLUMN max_appointments_per_day INTEGER;
```

All columns are **nullable** (NULL = no constraint).

### Sample Data

| Service Code | Service Name | Min Prep | Recovery | Spacing | Max/Day |
|--------------|--------------|----------|----------|---------|---------|
| EXAM_GENERAL | General Examination | 0 | 0 | 0 | NULL |
| CLEAN_BASIC | Basic Cleaning | 0 | 0 | 0 | NULL |
| EXTRACT_NORM | Normal Tooth Extraction | 0 | 3 | 0 | NULL |
| EXTRACT_WISDOM_L2 | Wisdom Tooth (Hard) | 0 | 14 | 0 | 2 |
| IMPL_SURGERY_KR | Implant Surgery (Korea) | 7 | 90 | 0 | 1 |
| IMPL_SURGERY_EUUS | Implant Surgery (EU/US) | 7 | 90 | 0 | 1 |
| IMPL_BONE_GRAFT | Bone Grafting | 0 | 14 | 0 | 2 |
| IMPL_SINUS_LIFT | Sinus Lift | 0 | 14 | 0 | 1 |
| ORTHO_BRACKET_ON | Braces Installation | 7 | 0 | 0 | NULL |
| ORTHO_ADJUST | Orthodontic Adjustment | 0 | 0 | 30 | NULL |
| ORTHO_MINI_SCREW | Mini-screw Installation | 0 | 3 | 0 | NULL |
| PERIO_FLAP_SURGERY | Gum Flap Surgery | 0 | 7 | 0 | NULL |

---

## API Endpoints

### 1. Get Service Details (Updated)

**Endpoint**: `GET /api/services/{serviceId}`

**Response** (Updated with constraint fields):
```json
{
  "serviceId": 123,
  "serviceCode": "IMPL_SURGERY_KR",
  "serviceName": "Implant Surgery (Korea)",
  "description": "Single tooth implant using Korean implant system",
  "defaultDurationMinutes": 90,
  "defaultBufferMinutes": 30,
  "price": 15000000.00,
  "specializationId": 1,
  "categoryId": 3,
  "displayOrder": 10,
  "isActive": true,
  
  "minimumPreparationDays": 7,
  "recoveryDays": 90,
  "spacingDays": 0,
  "maxAppointmentsPerDay": 1,
  
  "createdAt": "2025-01-15T10:00:00",
  "updatedAt": "2025-12-11T09:00:00"
}
```

---

### 2. Get All Services (Updated)

**Endpoint**: `GET /api/services`

**Query Parameters**:
- `categoryId` (optional): Filter by category
- `specializationId` (optional): Filter by specialization
- `isActive` (optional): Filter by active status

**Response**:
```json
[
  {
    "serviceId": 123,
    "serviceCode": "IMPL_SURGERY_KR",
    "serviceName": "Implant Surgery (Korea)",
    "price": 15000000.00,
    "minimumPreparationDays": 7,
    "recoveryDays": 90,
    "spacingDays": 0,
    "maxAppointmentsPerDay": 1,
    "isActive": true
  },
  {
    "serviceId": 124,
    "serviceCode": "ORTHO_ADJUST",
    "serviceName": "Orthodontic Adjustment",
    "price": 500000.00,
    "minimumPreparationDays": 0,
    "recoveryDays": 0,
    "spacingDays": 30,
    "maxAppointmentsPerDay": null,
    "isActive": true
  }
]
```

---

### 3. Create/Update Service (Updated)

**Endpoint**: `POST /api/services` or `PUT /api/services/{serviceId}`

**Request Body** (with new fields):
```json
{
  "serviceCode": "IMPL_SURGERY_KR",
  "serviceName": "Implant Surgery (Korea)",
  "description": "Single tooth implant using Korean implant system",
  "defaultDurationMinutes": 90,
  "defaultBufferMinutes": 30,
  "price": 15000000.00,
  "specializationId": 1,
  "categoryId": 3,
  "displayOrder": 10,
  
  "minimumPreparationDays": 7,
  "recoveryDays": 90,
  "spacingDays": 0,
  "maxAppointmentsPerDay": 1,
  
  "isActive": true
}
```

**Validation Rules**:
- `minimumPreparationDays`: Must be ≥ 0 (null = no constraint)
- `recoveryDays`: Must be ≥ 0 (null = no constraint)
- `spacingDays`: Must be ≥ 0 (null = no constraint)
- `maxAppointmentsPerDay`: Must be ≥ 1 (null = no limit)

**Response**: Same as Get Service Details

---

### 4. Check Holiday Status

**Endpoint**: `GET /api/holidays/check`

**Query Parameters**:
- `date` (required): Date to check (format: `YYYY-MM-DD`)

**Example**: `GET /api/holidays/check?date=2025-01-29`

**Response**:
```json
{
  "date": "2025-01-29",
  "isHoliday": true,
  "holidayName": "Tết Nguyên Đán 2025",
  "definitionId": "TET_2025"
}
```

**Response** (Not a holiday):
```json
{
  "date": "2025-01-15",
  "isHoliday": false,
  "holidayName": null,
  "definitionId": null
}
```

---

### 5. Get Holidays in Date Range

**Endpoint**: `GET /api/holidays/range`

**Query Parameters**:
- `startDate` (required): Start date (format: `YYYY-MM-DD`)
- `endDate` (required): End date (format: `YYYY-MM-DD`)

**Example**: `GET /api/holidays/range?startDate=2025-01-01&endDate=2025-12-31`

**Response**:
```json
{
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "totalHolidays": 17,
  "holidays": [
    {
      "date": "2025-01-01",
      "holidayName": "Tết Dương lịch",
      "definitionId": "NEW_YEAR"
    },
    {
      "date": "2025-01-29",
      "holidayName": "Tết Nguyên Đán 2025",
      "definitionId": "TET_2025"
    },
    {
      "date": "2025-01-30",
      "holidayName": "Tết Nguyên Đán 2025",
      "definitionId": "TET_2025"
    },
    {
      "date": "2025-04-30",
      "holidayName": "Ngày Giải phóng miền Nam",
      "definitionId": "LIBERATION_DAY"
    },
    {
      "date": "2025-05-01",
      "holidayName": "Ngày Quốc tế Lao động",
      "definitionId": "LABOR_DAY"
    },
    {
      "date": "2025-09-02",
      "holidayName": "Ngày Quốc khánh",
      "definitionId": "NATIONAL_DAY"
    }
  ]
}
```

---

### 6. Calculate Next Working Day

**Endpoint**: `GET /api/holidays/next-working-day`

**Query Parameters**:
- `date` (required): Starting date (format: `YYYY-MM-DD`)

**Example**: `GET /api/holidays/next-working-day?date=2025-01-29`

**Response**:
```json
{
  "requestedDate": "2025-01-29",
  "nextWorkingDay": "2025-02-05",
  "daysSkipped": 7,
  "holidaysSkipped": [
    "2025-01-29",
    "2025-01-30",
    "2025-01-31",
    "2025-02-01",
    "2025-02-02",
    "2025-02-03",
    "2025-02-04"
  ]
}
```

---

### 7. Validate Appointment Date

**Endpoint**: `POST /api/appointments/validate-constraints`

**Request Body**:
```json
{
  "appointmentDateTime": "2025-12-15T10:00:00",
  "serviceId": 123,
  "patientId": 456
}
```

**Response** (Valid):
```json
{
  "valid": true,
  "message": "Appointment date is valid",
  "details": {
    "isHoliday": false,
    "maxAppointmentsReached": false,
    "constraintViolations": []
  }
}
```

**Response** (Invalid - Holiday):
```json
{
  "valid": false,
  "message": "Cannot create appointment on 2025-01-29 - it is a holiday",
  "details": {
    "isHoliday": true,
    "holidayName": "Tết Nguyên Đán 2025",
    "maxAppointmentsReached": false,
    "constraintViolations": ["HOLIDAY"]
  }
}
```

**Response** (Invalid - Max appointments reached):
```json
{
  "valid": false,
  "message": "Maximum appointments per day reached for service 'Implant Surgery (Korea)' on 2025-12-15 (1/1)",
  "details": {
    "isHoliday": false,
    "maxAppointmentsReached": true,
    "currentCount": 1,
    "maxAllowed": 1,
    "constraintViolations": ["MAX_APPOINTMENTS_PER_DAY"]
  }
}
```

**Response** (Invalid - Recovery period):
```json
{
  "valid": false,
  "message": "Service 'Implant Surgery (Korea)' requires 90 days recovery period. Last appointment was on 2025-10-01 (45 days ago)",
  "details": {
    "isHoliday": false,
    "maxAppointmentsReached": false,
    "constraintViolations": ["RECOVERY_DAYS"],
    "lastAppointmentDate": "2025-10-01",
    "daysSinceLastAppointment": 45,
    "requiredRecoveryDays": 90
  }
}
```

---

### 8. Calculate Treatment Plan Schedule (NEW)

**Endpoint**: `POST /api/treatment-plans/calculate-schedule`

**Request Body**:
```json
{
  "startDate": "2025-12-15",
  "estimatedDurationDays": 180,
  "services": [
    {
      "serviceId": 101,
      "serviceCode": "EXAM_GENERAL",
      "serviceName": "General Examination"
    },
    {
      "serviceId": 123,
      "serviceCode": "IMPL_SURGERY_KR",
      "serviceName": "Implant Surgery (Korea)"
    },
    {
      "serviceId": 145,
      "serviceCode": "IMPL_CROWN_INSTALL",
      "serviceName": "Crown Installation"
    }
  ]
}
```

**Response**:
```json
{
  "startDate": "2025-12-15",
  "endDate": "2026-06-13",
  "estimatedDurationDays": 180,
  "actualWorkingDays": 165,
  "holidaysSkipped": 15,
  "appointmentSchedule": [
    {
      "sequenceNumber": 1,
      "serviceId": 101,
      "serviceCode": "EXAM_GENERAL",
      "serviceName": "General Examination",
      "scheduledDate": "2025-12-15",
      "isWorkingDay": true,
      "notes": "Initial consultation and examination"
    },
    {
      "sequenceNumber": 2,
      "serviceId": 123,
      "serviceCode": "IMPL_SURGERY_KR",
      "serviceName": "Implant Surgery (Korea)",
      "scheduledDate": "2026-01-14",
      "isWorkingDay": true,
      "notes": "7 days preparation time applied, skipped Tết holidays (Jan 29 - Feb 4)"
    },
    {
      "sequenceNumber": 3,
      "serviceId": 145,
      "serviceCode": "IMPL_CROWN_INSTALL",
      "serviceName": "Crown Installation",
      "scheduledDate": "2026-06-13",
      "isWorkingDay": true,
      "notes": "90 days recovery time applied after implant surgery"
    }
  ],
  "warnings": [],
  "metadata": {
    "totalServices": 3,
    "averageIntervalDays": 60,
    "constraintsApplied": [
      {
        "serviceId": 123,
        "constraintType": "MINIMUM_PREPARATION_DAYS",
        "constraintValue": 7
      },
      {
        "serviceId": 123,
        "constraintType": "RECOVERY_DAYS",
        "constraintValue": 90
      }
    ]
  }
}
```

---

## Service Constraint Fields

### Field Descriptions

#### 1. **minimumPreparationDays**
- **Type**: `Integer` (nullable)
- **Default**: `null` (no constraint)
- **Purpose**: Minimum days needed to prepare before the procedure
- **Example**: Implant surgery requires 7 days for lab work, patient preparation
- **Validation**: When scheduling, checks if enough days have passed since last appointment

#### 2. **recoveryDays**
- **Type**: `Integer` (nullable)
- **Default**: `null` (no constraint)
- **Purpose**: Recovery period required after the procedure
- **Example**: Implant surgery needs 90 days for osseointegration
- **Validation**: Prevents scheduling any appointment within recovery period

#### 3. **spacingDays**
- **Type**: `Integer` (nullable)
- **Default**: `null` (no constraint)
- **Purpose**: Minimum days between appointments of the SAME service
- **Example**: Orthodontic adjustments should be 30 days apart
- **Validation**: Checks last appointment with this specific service

#### 4. **maxAppointmentsPerDay**
- **Type**: `Integer` (nullable)
- **Default**: `null` (no limit)
- **Purpose**: Maximum number of appointments for this service per day
- **Example**: Only 1 implant surgery or sinus lift per day (complex procedures)
- **Validation**: Counts existing appointments on the date before allowing new ones

---

## Holiday Management

### Holiday Structure

#### Holiday Definitions Table
```sql
CREATE TABLE holiday_definitions (
    definition_id VARCHAR(50) PRIMARY KEY,
    holiday_name VARCHAR(255) NOT NULL,
    description TEXT
);
```

**Example Data**:
```sql
INSERT INTO holiday_definitions VALUES
('NEW_YEAR', 'Tết Dương lịch', 'New Year (January 1)'),
('TET_2025', 'Tết Nguyên Đán 2025', 'Lunar New Year 2025'),
('TET_2026', 'Tết Nguyên Đán 2026', 'Lunar New Year 2026'),
('LIBERATION_DAY', 'Ngày Giải phóng miền Nam', 'Liberation Day (April 30)'),
('LABOR_DAY', 'Ngày Quốc tế Lao động', 'International Labor Day (May 1)'),
('NATIONAL_DAY', 'Ngày Quốc khánh', 'National Day (September 2)');
```

#### Holiday Dates Table
```sql
CREATE TABLE holiday_dates (
    holiday_date DATE NOT NULL,
    definition_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (holiday_date, definition_id),
    FOREIGN KEY (definition_id) REFERENCES holiday_definitions(definition_id)
);
```

**Example Data** (Different dates per year):
```sql
-- 2025 Holidays
INSERT INTO holiday_dates VALUES
('2025-01-01', 'NEW_YEAR'),
('2025-01-29', 'TET_2025'),
('2025-01-30', 'TET_2025'),
('2025-01-31', 'TET_2025'),
('2025-02-01', 'TET_2025'),
('2025-02-02', 'TET_2025'),
('2025-02-03', 'TET_2025'),
('2025-02-04', 'TET_2025'),
('2025-04-30', 'LIBERATION_DAY'),
('2025-05-01', 'LABOR_DAY'),
('2025-09-02', 'NATIONAL_DAY');

-- 2026 Holidays (Tết dates are different!)
INSERT INTO holiday_dates VALUES
('2026-01-01', 'NEW_YEAR'),
('2026-02-17', 'TET_2026'),
('2026-02-18', 'TET_2026'),
('2026-02-19', 'TET_2026'),
('2026-02-20', 'TET_2026'),
('2026-02-21', 'TET_2026'),
('2026-02-22', 'TET_2026'),
('2026-02-23', 'TET_2026');
```

### Admin APIs for Holiday Management

#### Get All Holiday Definitions
```http
GET /api/holidays/definitions
```

**Response**:
```json
[
  {
    "definitionId": "TET_2025",
    "holidayName": "Tết Nguyên Đán 2025",
    "description": "Lunar New Year 2025",
    "dates": [
      "2025-01-29",
      "2025-01-30",
      "2025-01-31",
      "2025-02-01",
      "2025-02-02",
      "2025-02-03",
      "2025-02-04"
    ]
  }
]
```

#### Create Holiday Date
```http
POST /api/holidays/dates
```

**Request**:
```json
{
  "definitionId": "TET_2027",
  "holidayDate": "2027-02-06"
}
```

#### Delete Holiday Date
```http
DELETE /api/holidays/dates/{definitionId}/{date}
```

---

## Integration Examples

### Example 1: Display Service Constraints in Service Selection

```typescript
interface ServiceConstraints {
  minimumPreparationDays?: number;
  recoveryDays?: number;
  spacingDays?: number;
  maxAppointmentsPerDay?: number;
}

interface Service {
  serviceId: number;
  serviceCode: string;
  serviceName: string;
  price: number;
  constraints: ServiceConstraints;
}

// Fetch services
const services: Service[] = await api.get('/api/services');

// Display in UI
services.forEach(service => {
  console.log(`${service.serviceName}:`);
  
  if (service.constraints.minimumPreparationDays) {
    console.log(`  ⏱️ Prep time: ${service.constraints.minimumPreparationDays} days`);
  }
  
  if (service.constraints.recoveryDays) {
    console.log(`  💊 Recovery: ${service.constraints.recoveryDays} days`);
  }
  
  if (service.constraints.spacingDays) {
    console.log(`  📅 Spacing: ${service.constraints.spacingDays} days between appointments`);
  }
  
  if (service.constraints.maxAppointmentsPerDay) {
    console.log(`  ⚠️ Limit: ${service.constraints.maxAppointmentsPerDay} per day`);
  }
});

// Output:
// Implant Surgery (Korea):
//   ⏱️ Prep time: 7 days
//   💊 Recovery: 90 days
//   ⚠️ Limit: 1 per day
//
// Orthodontic Adjustment:
//   📅 Spacing: 30 days between appointments
```

---

### Example 2: Check if Date is Holiday Before Booking

```typescript
async function validateAppointmentDate(date: string): Promise<boolean> {
  const response = await api.get(`/api/holidays/check?date=${date}`);
  
  if (response.isHoliday) {
    alert(`Cannot book on ${date} - ${response.holidayName}`);
    return false;
  }
  
  return true;
}

// Usage in date picker
const selectedDate = '2025-01-29';
const isValid = await validateAppointmentDate(selectedDate);

if (isValid) {
  // Proceed with booking
}
```

---

### Example 3: Display Holiday Calendar

```typescript
async function loadHolidayCalendar(year: number) {
  const startDate = `${year}-01-01`;
  const endDate = `${year}-12-31`;
  
  const response = await api.get(
    `/api/holidays/range?startDate=${startDate}&endDate=${endDate}`
  );
  
  // Mark holidays in calendar UI
  response.holidays.forEach(holiday => {
    markDateAsHoliday(holiday.date, holiday.holidayName);
  });
}

// Calendar UI shows:
// Jan 29-Feb 4: Tết Nguyên Đán (7 days) - RED
// Apr 30: Giải phóng miền Nam - RED
// May 1: Quốc tế Lao động - RED
// Sep 2: Quốc khánh - RED
```

---

### Example 4: Validate Before Creating Appointment

```typescript
async function createAppointment(
  dateTime: string,
  serviceId: number,
  patientId: number
) {
  // Step 1: Validate constraints
  const validation = await api.post('/api/appointments/validate-constraints', {
    appointmentDateTime: dateTime,
    serviceId: serviceId,
    patientId: patientId
  });
  
  if (!validation.valid) {
    // Show error to user
    alert(validation.message);
    
    // Display specific violations
    if (validation.details.isHoliday) {
      console.log('❌ Cannot book on holiday');
    }
    
    if (validation.details.maxAppointmentsReached) {
      console.log('❌ Daily limit reached for this service');
    }
    
    validation.details.constraintViolations.forEach(violation => {
      console.log(`❌ ${violation}`);
    });
    
    return;
  }
  
  // Step 2: Create appointment
  const appointment = await api.post('/api/appointments', {
    appointmentDateTime: dateTime,
    serviceId: serviceId,
    patientId: patientId,
    // ... other fields
  });
  
  alert('✅ Appointment created successfully!');
}
```

---

### Example 5: Auto-Calculate Treatment Plan Schedule

```typescript
interface TreatmentPlanScheduleRequest {
  startDate: string;
  estimatedDurationDays: number;
  services: Array<{
    serviceId: number;
    serviceCode: string;
    serviceName: string;
  }>;
}

async function calculateTreatmentSchedule(
  startDate: string,
  duration: number,
  services: any[]
) {
  const request: TreatmentPlanScheduleRequest = {
    startDate: startDate,
    estimatedDurationDays: duration,
    services: services
  };
  
  const schedule = await api.post(
    '/api/treatment-plans/calculate-schedule',
    request
  );
  
  // Display timeline
  console.log(`Treatment Plan: ${startDate} → ${schedule.endDate}`);
  console.log(`Working days: ${schedule.actualWorkingDays}`);
  console.log(`Holidays skipped: ${schedule.holidaysSkipped}`);
  console.log('');
  
  schedule.appointmentSchedule.forEach((apt, index) => {
    console.log(`${index + 1}. ${apt.scheduledDate}: ${apt.serviceName}`);
    if (apt.notes) {
      console.log(`   ℹ️ ${apt.notes}`);
    }
  });
  
  return schedule;
}

// Example usage
const services = [
  { serviceId: 101, serviceCode: 'EXAM_GENERAL', serviceName: 'Examination' },
  { serviceId: 123, serviceCode: 'IMPL_SURGERY_KR', serviceName: 'Implant Surgery' },
  { serviceId: 145, serviceCode: 'IMPL_CROWN_INSTALL', serviceName: 'Crown' }
];

const schedule = await calculateTreatmentSchedule('2025-12-15', 180, services);

// Output:
// Treatment Plan: 2025-12-15 → 2026-06-13
// Working days: 165
// Holidays skipped: 15
//
// 1. 2025-12-15: Examination
// 2. 2026-01-14: Implant Surgery
//    ℹ️ 7 days preparation time applied, skipped Tết holidays
// 3. 2026-06-13: Crown
//    ℹ️ 90 days recovery time applied after implant surgery
```

---

### Example 6: Show Next Available Date

```typescript
async function findNextAvailableDate(
  requestedDate: string,
  serviceId: number,
  patientId: number
): Promise<string> {
  let currentDate = requestedDate;
  let attempts = 0;
  const maxAttempts = 30;
  
  while (attempts < maxAttempts) {
    const validation = await api.post('/api/appointments/validate-constraints', {
      appointmentDateTime: `${currentDate}T10:00:00`,
      serviceId: serviceId,
      patientId: patientId
    });
    
    if (validation.valid) {
      return currentDate;
    }
    
    // Try next day
    currentDate = addDays(currentDate, 1);
    attempts++;
  }
  
  throw new Error('Could not find available date within 30 days');
}

// Usage
const requestedDate = '2025-01-29'; // Holiday
const availableDate = await findNextAvailableDate(requestedDate, 123, 456);

console.log(`Requested: ${requestedDate}`);
console.log(`Available: ${availableDate}`); // 2025-02-05 (after Tết)
```

---

## Validation Rules

### Service Constraint Validation Matrix

| Constraint Type | Checks Against | Scope | Example |
|----------------|----------------|-------|---------|
| **Minimum Preparation Days** | Last appointment (any service) | Patient-level | "Need 7 days to prepare implant" |
| **Recovery Days** | Last completed appointment | Patient-level | "Wait 90 days after implant surgery" |
| **Spacing Days** | Last appointment **with same service** | Service-specific | "Orthodontic adjustments every 30 days" |
| **Max Appointments Per Day** | All appointments on that date | Service + Date | "Only 1 implant surgery per day" |

### Validation Flow

```
User selects appointment date & service
    ↓
1. Check if date is holiday
    ├─ YES → ❌ Reject (show holiday name)
    └─ NO → Continue
         ↓
2. Check max appointments per day for service
    ├─ Limit reached → ❌ Reject (show current/max count)
    └─ OK → Continue
         ↓
3. Get patient's last appointment
    ├─ No previous appointments → ✅ Accept
    └─ Has previous → Continue
         ↓
4. Check minimum preparation days
    ├─ Not enough time → ❌ Reject (show days needed)
    └─ OK → Continue
         ↓
5. Check recovery days
    ├─ Still in recovery → ❌ Reject (show recovery period)
    └─ OK → Continue
         ↓
6. Check spacing days (for same service)
    ├─ Too soon → ❌ Reject (show required spacing)
    └─ OK → ✅ Accept
```

---

## Error Handling

### Error Response Format

```json
{
  "error": {
    "code": "APPOINTMENT_CONSTRAINT_VIOLATION",
    "message": "Cannot create appointment on 2025-01-29 - it is a holiday",
    "details": {
      "constraintType": "HOLIDAY",
      "date": "2025-01-29",
      "holidayName": "Tết Nguyên Đán 2025"
    },
    "timestamp": "2025-12-11T10:30:00Z"
  }
}
```

### Common Error Codes

| Error Code | HTTP Status | Description | Resolution |
|------------|-------------|-------------|------------|
| `APPOINTMENT_ON_HOLIDAY` | 400 | Date is a holiday | Choose another date |
| `MAX_APPOINTMENTS_REACHED` | 400 | Daily limit exceeded | Choose another date |
| `INSUFFICIENT_PREPARATION_TIME` | 400 | Not enough prep days | Wait longer |
| `RECOVERY_PERIOD_REQUIRED` | 400 | Still in recovery | Wait for recovery to complete |
| `INSUFFICIENT_SPACING` | 400 | Same service too soon | Wait for spacing period |
| `SERVICE_NOT_FOUND` | 404 | Invalid service ID | Check service ID |
| `PATIENT_NOT_FOUND` | 404 | Invalid patient ID | Check patient ID |
| `INVALID_DATE_FORMAT` | 400 | Wrong date format | Use `YYYY-MM-DD` |

### Error Handling Example

```typescript
try {
  const appointment = await createAppointment(date, serviceId, patientId);
} catch (error) {
  switch (error.code) {
    case 'APPOINTMENT_ON_HOLIDAY':
      showError(`Cannot book on ${error.details.holidayName}`);
      suggestAlternativeDate(error.details.nextWorkingDay);
      break;
      
    case 'MAX_APPOINTMENTS_REACHED':
      showError(`Daily limit (${error.details.maxAllowed}) reached`);
      showAlternativeDates(error.details.suggestedDates);
      break;
      
    case 'RECOVERY_PERIOD_REQUIRED':
      showError(`Recovery needed until ${error.details.recoveryCompleteDate}`);
      setMinimumDate(error.details.recoveryCompleteDate);
      break;
      
    case 'INSUFFICIENT_SPACING':
      showError(`Wait until ${error.details.nextAvailableDate}`);
      highlightDate(error.details.nextAvailableDate);
      break;
      
    default:
      showError('An unexpected error occurred');
  }
}
```

---

## Testing Scenarios

### Test Case 1: Holiday Detection

**Setup**:
- Date: 2025-01-29 (Tết)
- Service: Any
- Patient: Any

**Expected Result**: ❌ Rejected with message "Cannot create appointment on 2025-01-29 - it is a holiday"

---

### Test Case 2: Max Appointments Per Day

**Setup**:
- Date: 2025-12-15
- Service: IMPL_SURGERY_KR (max = 1)
- Existing appointments on 2025-12-15 for IMPL_SURGERY_KR: 1

**Expected Result**: ❌ Rejected with message "Maximum appointments per day reached for service 'Implant Surgery (Korea)' on 2025-12-15 (1/1)"

---

### Test Case 3: Recovery Days

**Setup**:
- Date: 2025-12-15
- Service: IMPL_SURGERY_KR (recovery = 90 days)
- Patient: Has completed implant surgery on 2025-10-01
- Days since last appointment: 75 days

**Expected Result**: ❌ Rejected with message "Service 'Implant Surgery (Korea)' requires 90 days recovery period. Last appointment was on 2025-10-01 (75 days ago)"

---

### Test Case 4: Spacing Days

**Setup**:
- Date: 2025-12-15
- Service: ORTHO_ADJUST (spacing = 30 days)
- Patient: Had ORTHO_ADJUST on 2025-12-01
- Days since last same-service appointment: 14 days

**Expected Result**: ❌ Rejected with message "Service 'Orthodontic Adjustment' requires 30 days spacing between appointments. Last appointment with this service was on 2025-12-01 (14 days ago)"

---

### Test Case 5: Successful Booking

**Setup**:
- Date: 2025-12-20 (not a holiday)
- Service: EXAM_GENERAL (no constraints)
- Patient: First appointment

**Expected Result**: ✅ Accepted

---

### Test Case 6: Treatment Plan with Holidays

**Setup**:
- Start Date: 2025-01-28
- Duration: 14 days
- Services: 2 (Consultation, Treatment)

**Expected Schedule**:
1. 2025-01-28: Consultation (start date)
2. 2025-02-05: Treatment (skipped Tết Jan 29 - Feb 4)

**Validation**:
- Actual working days: 8 (skipped 7 holiday days)
- All appointments on working days
- No appointments during Tết

---

## UI/UX Recommendations

### 1. Service Selection Screen

Display constraint badges:
```
┌────────────────────────────────────────┐
│ 🦷 Implant Surgery (Korea)            │
│ 💰 15,000,000 VND                      │
│                                         │
│ ⏱️ Prep: 7 days                        │
│ 💊 Recovery: 90 days                   │
│ ⚠️ Limit: 1 per day                   │
│                                         │
│ [Select Service]                        │
└────────────────────────────────────────┘
```

### 2. Date Picker

Mark holidays in red:
```
December 2025
Su Mo Tu We Th Fr Sa
       1  2  3  4  5
 6  7  8  9 10 11 12
13 14 15 16 17 18 19
20 21 22 23 24 25 26
27 28 29 30 31

January 2026
Su Mo Tu We Th Fr Sa
             1  2  3
 4  5  6  7  8  9 10
11 12 13 14 15 16 17
18 19 20 21 22 23 24
25 26 27 28 [29][30][31]
      Tết Holiday →
```

### 3. Validation Error Display

Show helpful suggestions:
```
┌────────────────────────────────────────┐
│ ❌ Cannot create appointment           │
│                                         │
│ Service 'Implant Surgery' requires     │
│ 90 days recovery period.               │
│                                         │
│ Last appointment: Oct 1, 2025          │
│ Days elapsed: 75 days                  │
│ Required: 90 days                      │
│                                         │
│ ✅ Available from: Jan 3, 2026         │
│                                         │
│ [View Calendar] [Select Jan 3]         │
└────────────────────────────────────────┘
```

### 4. Treatment Plan Timeline

Visual timeline with constraints:
```
Treatment Plan Timeline
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Dec 15, 2025
│ 📋 Examination
│
├── 7 days prep ──→
│
Jan 14, 2026 (Skipped Tết: Jan 29 - Feb 4)
│ 🦷 Implant Surgery
│
├── 90 days recovery ──────────────→
│
Jun 13, 2026
│ 👑 Crown Installation
│
└── End
```

---

## Summary Checklist for FE Integration

- [ ] Update service DTOs to include 4 new constraint fields
- [ ] Add holiday checking before date selection
- [ ] Display service constraints in service selection UI
- [ ] Implement appointment validation API call before submission
- [ ] Mark holidays in calendar/date picker (red color)
- [ ] Show constraint violations with helpful error messages
- [ ] Suggest alternative dates when validation fails
- [ ] Calculate and display treatment plan schedules
- [ ] Show timeline with constraint annotations
- [ ] Handle edge cases (no constraints, first appointment, etc.)
- [ ] Add loading states during validation API calls
- [ ] Implement retry logic for failed validation checks
- [ ] Cache holiday data to reduce API calls
- [ ] Add tooltips explaining each constraint type
- [ ] Test with various service combinations and dates

---

## Contact & Support

For questions or issues with this implementation, please contact:
- **Backend Team**: backend@dentalclinic.com
- **Documentation**: docs@dentalclinic.com
- **API Issues**: Create ticket in JIRA with tag `BE_4`

---

**Document Version**: 1.0  
**Last Updated**: December 11, 2025  
**Status**: ✅ Production Ready
