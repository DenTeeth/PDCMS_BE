# API 10.1: Get Patient Profile for Mobile App

## Overview

This API allows patients to retrieve their complete profile information for use in mobile applications (Android/iOS). It returns comprehensive patient data including medical history, allergies, emergency contact, and guardian information.

## Endpoint

```
GET /api/v1/patients/me/profile
```

## Authentication

- **Required**: Yes (JWT Bearer Token)
- **Role**: PATIENT
- **Authorization**: `@PreAuthorize("hasRole('PATIENT')")`

## Request

### Headers

```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

### Path Parameters

None (uses authenticated user's username from JWT)

### Query Parameters

None

### Request Body

None

## Response

### Success Response (200 OK)

Returns complete patient profile with all medical and personal information.

```json
{
  "patientId": 1,
  "patientCode": "PT-AN",
  "firstName": "Nguyen",
  "lastName": "Van An",
  "fullName": "Nguyen Van An",
  "email": "an.nv@email.com",
  "phone": "0909123456",
  "dateOfBirth": "1990-05-15",
  "age": 34,
  "address": "123 Le Loi, Quan 1, TP.HCM",
  "gender": "MALE",
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
  "bookingBlockReason": null,
  "blockedAt": null,
  "accountId": 14,
  "username": "patient001",
  "createdAt": "2023-01-10 08:00:00",
  "updatedAt": "2023-01-10 08:00:00"
}
```

### Response for Minor Patient (<16 years old)

When patient is under 16 years old, guardian information will be populated:

```json
{
  "patientId": 10,
  "patientCode": "PT-LAN",
  "firstName": "Nguyen",
  "lastName": "Thi Lan",
  "fullName": "Nguyen Thi Lan",
  "email": "lan.nt@email.com",
  "phone": "0909999990",
  "dateOfBirth": "2011-09-15",
  "age": 13,
  "address": "789 Vo Van Tan, Quan 3, TP.HCM",
  "gender": "FEMALE",
  "medicalHistory": "Tre em khoe manh",
  "allergies": "Khong co",
  "emergencyContactName": "Nguyen Van Minh",
  "emergencyContactPhone": "0900000000",
  "guardianName": "Nguyen Van Minh",
  "guardianPhone": "0900000000",
  "guardianRelationship": "Bo",
  "guardianCitizenId": "079088001234",
  "isActive": true,
  "consecutiveNoShows": 0,
  "isBookingBlocked": false,
  "bookingBlockReason": null,
  "blockedAt": null,
  "accountId": 23,
  "username": "patient010",
  "createdAt": "2023-01-10 08:00:00",
  "updatedAt": "2023-01-10 08:00:00"
}
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
  "message": "Access denied - User does not have ROLE_PATIENT",
  "timestamp": "2025-12-09T10:30:00"
}
```

#### 400 Bad Request

```json
{
  "statusCode": 400,
  "message": "Account not found for username: patient999",
  "timestamp": "2025-12-09T10:30:00"
}
```

## Field Descriptions

### Basic Information

- **patientId**: Internal patient ID (Integer)
- **patientCode**: Unique patient code (String, e.g., "PT-AN")
- **firstName**: Patient's first name
- **lastName**: Patient's last name
- **fullName**: Concatenated full name (firstName + lastName)
- **email**: Patient's email address
- **phone**: Patient's phone number
- **dateOfBirth**: Birth date in yyyy-MM-dd format
- **age**: Calculated age in years (Integer)
- **address**: Full residential address
- **gender**: Gender enum (MALE, FEMALE, OTHER)

### Medical Information

- **medicalHistory**: Patient's medical history and past conditions (Text, nullable)
- **allergies**: Known allergies to medicines, food, etc. (Text, nullable)

### Emergency Contact

- **emergencyContactName**: Emergency contact person's name (nullable)
- **emergencyContactPhone**: Emergency contact phone number (nullable)

### Guardian Information (for minors <16 years old)

- **guardianName**: Legal guardian's name (nullable, required for minors)
- **guardianPhone**: Guardian's phone number (nullable)
- **guardianRelationship**: Relationship to patient (e.g., "Bo", "Me", "Ong", "Ba")
- **guardianCitizenId**: Guardian's citizen ID number (nullable)

### Booking Status

- **isActive**: Whether patient account is active (Boolean)
- **consecutiveNoShows**: Number of consecutive no-shows (Integer)
- **isBookingBlocked**: Whether patient is blocked from booking (Boolean)
- **bookingBlockReason**: Reason for booking block (nullable)
- **blockedAt**: Timestamp when patient was blocked (nullable)

### Account Information

- **accountId**: Related account ID
- **username**: Patient's login username
- **createdAt**: Account creation timestamp (yyyy-MM-dd HH:mm:ss)
- **updatedAt**: Last update timestamp (yyyy-MM-dd HH:mm:ss)

## Business Rules

1. **Authentication Required**: Must provide valid JWT token with ROLE_PATIENT
2. **Self-Service Only**: Patients can only view their own profile (enforced by authentication)
3. **Age Calculation**: Age is automatically calculated from dateOfBirth
4. **Minor Detection**: Patients under 16 years old should have guardian information
5. **Booking Status**: isBookingBlocked=true prevents creating new appointments (Rule #5)
6. **Emergency Contact**: Important for medical staff during emergencies
7. **Allergies Display**: Critical information displayed prominently in clinical records

## Use Cases

### Mobile App Profile Screen

```
Patient opens mobile app -> Logs in -> Views Profile tab
-> App calls GET /api/v1/patients/me/profile
-> Display comprehensive patient information including:
   - Personal details (name, DOB, age, gender)
   - Contact information (phone, email, address)
   - Medical information (history, allergies)
   - Emergency contact details
   - Guardian information (if minor)
   - Booking status
```

### Pre-Appointment Review

```
Patient selects existing appointment -> Views appointment details
-> App displays patient's medical history and allergies
-> Doctor can review before appointment
```

## Testing

### Test Case 1: Adult Patient Profile

**Setup**: Use patient001 account (Nguyen Van An, age 34)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/patients/me/profile" \
  -H "Authorization: Bearer <JWT_TOKEN_FOR_PATIENT001>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 200 OK
- Response includes all patient fields
- guardianName, guardianPhone, guardianRelationship, guardianCitizenId are null
- medicalHistory shows "Tien su viem loi, da dieu tri nam 2020"
- allergies shows "Di ung Penicillin"

### Test Case 2: Minor Patient Profile

**Setup**: Use patient010 account (Nguyen Thi Lan, age 13)

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/patients/me/profile" \
  -H "Authorization: Bearer <JWT_TOKEN_FOR_PATIENT010>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 200 OK
- age field shows 13
- guardianName shows "Nguyen Van Minh"
- guardianPhone shows "0900000000"
- guardianRelationship shows "Bo"
- guardianCitizenId shows "079088001234"

### Test Case 3: Unauthorized Access

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/patients/me/profile" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 401 Unauthorized
- Error message about missing JWT token

### Test Case 4: Non-Patient Role

**Setup**: Use doctor or receptionist JWT token

**Request**:

```bash
curl -X GET "http://localhost:8080/api/v1/patients/me/profile" \
  -H "Authorization: Bearer <JWT_TOKEN_FOR_DOCTOR>" \
  -H "Content-Type: application/json"
```

**Expected Result**:

- Status: 403 Forbidden
- Error message about lacking ROLE_PATIENT

## Implementation Notes

### Service Layer

- Method: `PatientService.getCurrentPatientProfile(String username)`
- Transaction: Read-only
- Logic:
  1. Find Account by username
  2. Find Patient by account_id
  3. Calculate age from dateOfBirth
  4. Build fullName from firstName + lastName
  5. Map all fields to PatientDetailResponse
  6. Return response

### Security

- Controller method annotated with `@PreAuthorize("hasRole('PATIENT')")`
- Username extracted from Authentication object (JWT)
- No risk of data leakage - users can only access their own data

### Performance

- Single database query to fetch patient data
- Age calculated in-memory
- No N+1 query issues

## Related APIs

- **API 6.1**: Get Appointment List - Now returns full patient details in each appointment
- **API 6.2**: Get Appointment by Code - Now includes comprehensive patient information
- **API 7.1**: Get Treatment Plan List - Now includes patient medical history and allergies
- **API 8.1**: Get Clinical Record - Now includes patient allergies and vital signs

## Mobile App Integration

### Android Example (Kotlin)

```kotlin
data class PatientProfile(
    val patientId: Int,
    val patientCode: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String,
    val age: Int,
    val gender: String,
    val address: String,
    val medicalHistory: String?,
    val allergies: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val guardianName: String?,
    val guardianPhone: String?,
    val guardianRelationship: String?,
    val guardianCitizenId: String?,
    val isActive: Boolean,
    val consecutiveNoShows: Int,
    val isBookingBlocked: Boolean,
    val bookingBlockReason: String?
)

// API Service
interface PatientApiService {
    @GET("/api/v1/patients/me/profile")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): PatientProfile
}

// ViewModel usage
class ProfileViewModel(private val apiService: PatientApiService) : ViewModel() {
    private val _profile = MutableLiveData<PatientProfile>()
    val profile: LiveData<PatientProfile> = _profile

    fun loadProfile(token: String) {
        viewModelScope.launch {
            try {
                val profile = apiService.getMyProfile("Bearer $token")
                _profile.value = profile
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
```

### iOS Example (Swift)

```swift
struct PatientProfile: Codable {
    let patientId: Int
    let patientCode: String
    let fullName: String
    let email: String
    let phone: String
    let dateOfBirth: String
    let age: Int
    let gender: String
    let address: String
    let medicalHistory: String?
    let allergies: String?
    let emergencyContactName: String?
    let emergencyContactPhone: String?
    let guardianName: String?
    let guardianPhone: String?
    let guardianRelationship: String?
    let guardianCitizenId: String?
    let isActive: Bool
    let consecutiveNoShows: Int
    let isBookingBlocked: Bool
    let bookingBlockReason: String?
}

// API Service
class PatientAPIService {
    func getMyProfile(token: String, completion: @escaping (Result<PatientProfile, Error>) -> Void) {
        let url = URL(string: "http://localhost:8080/api/v1/patients/me/profile")!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }

            guard let data = data else {
                completion(.failure(NSError(domain: "", code: -1, userInfo: nil)))
                return
            }

            do {
                let profile = try JSONDecoder().decode(PatientProfile.self, from: data)
                completion(.success(profile))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }
}
```

## Changelog

### Version 1.0 (2025-12-09)

- Initial API implementation
- Added comprehensive patient profile endpoint for mobile app
- Includes medical history, allergies, emergency contact, and guardian information
- Support for minor patients (<16 years old) with guardian details
- Automatic age calculation from date of birth
- Integration with JWT authentication and ROLE_PATIENT authorization

## Notes

1. **No Emoji Usage**: Code and documentation follow project standards (no emoji characters)
2. **Password for Testing**: All test accounts use password "123456"
3. **Startup Requirement**: Application must be running for at least 40 seconds before API calls
4. **Data Privacy**: Patients can only access their own data, enforced by JWT authentication
5. **Mobile Optimization**: Response includes all necessary fields for mobile app profile screen
6. **Medical Safety**: Allergies and medical history are critical fields displayed prominently
