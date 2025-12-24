# Patient Blocking Fields - Frontend Integration Guide

## Overview

Backend has added comprehensive blocking fields to Patient APIs to support the patient booking restriction feature (Business Rules BR-043, BR-044, BR-005). This guide provides complete information for Frontend integration.

**Last Updated**: 2025-12-10
**Backend Version**: Fixed in commit feat/BE-901
**Status**: Ready for Production

---

## What Changed

### APIs Affected

1. **Patient List API** - `GET /api/v1/patients`
2. **Patient Detail API** - `GET /api/v1/patients/{patientCode}`
3. **Create Patient API** - `POST /api/v1/patients` (returns PatientInfoResponse)
4. **Update Patient API** - `PATCH /api/v1/patients/{patientCode}` (returns PatientInfoResponse)

All these APIs now return additional blocking fields in the response.

### New Fields Added to PatientInfoResponse

| Field Name | Type | Description | Example Values |
|------------|------|-------------|----------------|
| `isBookingBlocked` | Boolean | Whether patient is blocked from booking appointments | `true`, `false` |
| `bookingBlockReason` | String (Enum) | Reason code for blocking | `"EXCESSIVE_NO_SHOWS"`, `"REPEATED_LATE_CANCELLATIONS"`, `"PAYMENT_ISSUES"`, `"DISRUPTIVE_BEHAVIOR"`, `"MEDICAL_SAFETY"`, `null` |
| `bookingBlockNotes` | String | Additional notes about the block | `"Bo hen 3 lan lien tiep"`, `null` |
| `blockedBy` | String | Name of employee who blocked the patient | `"Nguyen Thi B"`, `null` |
| `blockedAt` | DateTime (ISO 8601) | Timestamp when patient was blocked | `"2025-12-09T10:30:00"`, `null` |
| `consecutiveNoShows` | Integer | Number of consecutive no-shows | `0`, `1`, `2`, `3` |

---

## BookingBlockReason Enum

### Enum Values and Meanings

```typescript
enum BookingBlockReason {
  EXCESSIVE_NO_SHOWS = "EXCESSIVE_NO_SHOWS",           // Bo hen qua nhieu (3+ lan)
  REPEATED_LATE_CANCELLATIONS = "REPEATED_LATE_CANCELLATIONS", // Huy hen muon lien tuc
  PAYMENT_ISSUES = "PAYMENT_ISSUES",                   // Van de thanh toan
  DISRUPTIVE_BEHAVIOR = "DISRUPTIVE_BEHAVIOR",         // Hanh vi gay roi
  MEDICAL_SAFETY = "MEDICAL_SAFETY"                    // Ly do y te/an toan
}
```

### Reason Categories

**Temporary Blocks** (Can be auto-removed after condition is met):
- `EXCESSIVE_NO_SHOWS` - Auto-unblocked when patient completes appointment
- `REPEATED_LATE_CANCELLATIONS` - May be reviewed periodically

**Permanent Blocks** (Require manual review):
- `PAYMENT_ISSUES` - Requires payment settlement
- `DISRUPTIVE_BEHAVIOR` - Requires management approval
- `MEDICAL_SAFETY` - Requires doctor review

---

## API Response Examples

### Patient List API Response

**Request**:
```http
GET /api/v1/patients?page=0&size=10
Authorization: Bearer {JWT_TOKEN}
```

**Response**:
```json
{
  "content": [
    {
      "patientId": 1,
      "patientCode": "BN-1001",
      "firstName": "Doan Thanh",
      "lastName": "Phong",
      "fullName": "Doan Thanh Phong",
      "email": "phong.dt@email.com",
      "phone": "0971111111",
      "dateOfBirth": "1995-03-15",
      "address": "123 Le Van Viet, Q9, TPHCM",
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
      "createdAt": "2025-12-10T19:01:17.127790",
      "updatedAt": "2025-12-10T19:01:17.127790",
      "hasAccount": true,
      "accountId": 12,
      "accountStatus": "ACTIVE",
      "isEmailVerified": true,
      
      "isBookingBlocked": false,
      "bookingBlockReason": null,
      "bookingBlockNotes": null,
      "blockedBy": null,
      "blockedAt": null,
      "consecutiveNoShows": 0
    },
    {
      "patientId": 4,
      "patientCode": "BN-1004",
      "fullName": "Nguyen Van A",
      "email": "nguyenvana@example.com",
      "phone": "0901234567",
      "isActive": true,
      
      "isBookingBlocked": true,
      "bookingBlockReason": "EXCESSIVE_NO_SHOWS",
      "bookingBlockNotes": "Bo hen 3 lan lien tiep trong thang 12/2025",
      "blockedBy": "Nguyen Thi B",
      "blockedAt": "2025-12-09T10:30:00",
      "consecutiveNoShows": 3
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### Patient Detail API Response

**Request**:
```http
GET /api/v1/patients/BN-1001
Authorization: Bearer {JWT_TOKEN}
```

**Response**: Same structure as individual patient in list response (shown above)

---

## Frontend Integration Steps

### Step 1: Update TypeScript Types

**File**: `src/types/patient.ts`

```typescript
export enum BookingBlockReason {
  EXCESSIVE_NO_SHOWS = "EXCESSIVE_NO_SHOWS",
  REPEATED_LATE_CANCELLATIONS = "REPEATED_LATE_CANCELLATIONS",
  PAYMENT_ISSUES = "PAYMENT_ISSUES",
  DISRUPTIVE_BEHAVIOR = "DISRUPTIVE_BEHAVIOR",
  MEDICAL_SAFETY = "MEDICAL_SAFETY"
}

export interface Patient {
  patientId: number;
  patientCode: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone: string;
  dateOfBirth: string;
  address: string;
  gender: string;
  medicalHistory?: string;
  allergies?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  guardianName?: string;
  guardianPhone?: string;
  guardianRelationship?: string;
  guardianCitizenId?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  hasAccount: boolean;
  accountId?: number;
  accountStatus?: string;
  isEmailVerified?: boolean;
  
  // NEW: Blocking fields
  isBookingBlocked: boolean;
  bookingBlockReason?: BookingBlockReason;
  bookingBlockNotes?: string;
  blockedBy?: string;
  blockedAt?: string;
  consecutiveNoShows: number;
}
```

### Step 2: Create Utility Functions

**File**: `src/utils/patientBlockUtils.ts`

```typescript
import { BookingBlockReason } from '@/types/patient';

export const isTemporaryBlock = (reason?: BookingBlockReason): boolean => {
  if (!reason) return false;
  return [
    BookingBlockReason.EXCESSIVE_NO_SHOWS,
    BookingBlockReason.REPEATED_LATE_CANCELLATIONS
  ].includes(reason);
};

export const isPermanentBlock = (reason?: BookingBlockReason): boolean => {
  if (!reason) return false;
  return [
    BookingBlockReason.PAYMENT_ISSUES,
    BookingBlockReason.DISRUPTIVE_BEHAVIOR,
    BookingBlockReason.MEDICAL_SAFETY
  ].includes(reason);
};

export const getBlockReasonLabel = (reason?: BookingBlockReason): string => {
  if (!reason) return '';
  
  const labels: Record<BookingBlockReason, string> = {
    [BookingBlockReason.EXCESSIVE_NO_SHOWS]: 'Bo hen qua nhieu',
    [BookingBlockReason.REPEATED_LATE_CANCELLATIONS]: 'Huy hen muon lien tuc',
    [BookingBlockReason.PAYMENT_ISSUES]: 'Van de thanh toan',
    [BookingBlockReason.DISRUPTIVE_BEHAVIOR]: 'Hanh vi khong phu hop',
    [BookingBlockReason.MEDICAL_SAFETY]: 'Ly do y te'
  };
  
  return labels[reason] || reason;
};

export const getBlockReasonDescription = (reason?: BookingBlockReason): string => {
  if (!reason) return '';
  
  const descriptions: Record<BookingBlockReason, string> = {
    [BookingBlockReason.EXCESSIVE_NO_SHOWS]: 
      'Benh nhan da bo hen 3 lan lien tiep. Se tu dong mo khoa sau khi hoan thanh 1 cuoc hen.',
    [BookingBlockReason.REPEATED_LATE_CANCELLATIONS]: 
      'Benh nhan huy hen muon qua nhieu lan. Vui long lien he quan ly de duoc mo khoa.',
    [BookingBlockReason.PAYMENT_ISSUES]: 
      'Benh nhan co cong no chua thanh toan. Vui long hoan tat thanh toan de duoc mo khoa.',
    [BookingBlockReason.DISRUPTIVE_BEHAVIOR]: 
      'Tai khoan tam khoa vi hanh vi khong phu hop. Lien he quan ly de duoc xem xet.',
    [BookingBlockReason.MEDICAL_SAFETY]: 
      'Tai khoan tam khoa vi ly do y te/an toan. Vui long lien he bac si de duoc tu van.'
  };
  
  return descriptions[reason] || '';
};

export const getBlockBadgeVariant = (reason?: BookingBlockReason): 'warning' | 'destructive' => {
  return isTemporaryBlock(reason) ? 'warning' : 'destructive';
};

export const getBlockBadgeLabel = (reason?: BookingBlockReason): string => {
  return isTemporaryBlock(reason) ? 'Tam chan' : 'Chan';
};
```

### Step 3: Update Patient List Table

**File**: `src/app/admin/accounts/users/page.tsx`

```typescript
import { isTemporaryBlock, getBlockBadgeVariant, getBlockBadgeLabel } from '@/utils/patientBlockUtils';
import { Badge } from '@/components/ui/badge';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";

// Inside table columns definition
{
  accessorKey: "isBookingBlocked",
  header: "Chan dat lich",
  cell: ({ row }) => {
    const patient = row.original;
    const isBlocked = patient.isBookingBlocked || false;
    const reason = patient.bookingBlockReason;
    const notes = patient.bookingBlockNotes;
    
    return (
      <div className="flex items-center gap-2">
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger asChild>
              <div>
                <Checkbox
                  checked={isBlocked}
                  disabled
                  className={cn(
                    "h-5 w-5 border-2 cursor-default",
                    isBlocked && isTemporaryBlock(reason)
                      ? "border-orange-500 data-[state=checked]:bg-orange-500"
                      : isBlocked
                      ? "border-red-500 data-[state=checked]:bg-red-500"
                      : ""
                  )}
                />
              </div>
            </TooltipTrigger>
            {isBlocked && (
              <TooltipContent>
                <div className="space-y-1">
                  <p className="font-semibold">
                    {getBlockReasonLabel(reason)}
                  </p>
                  {notes && <p className="text-sm">{notes}</p>}
                  {patient.blockedBy && (
                    <p className="text-xs text-muted-foreground">
                      Bi chan boi: {patient.blockedBy}
                    </p>
                  )}
                  {patient.blockedAt && (
                    <p className="text-xs text-muted-foreground">
                      Thoi gian: {formatDateTime(patient.blockedAt)}
                    </p>
                  )}
                  {patient.consecutiveNoShows > 0 && (
                    <p className="text-xs text-muted-foreground">
                      So lan bo hen: {patient.consecutiveNoShows}
                    </p>
                  )}
                </div>
              </TooltipContent>
            )}
          </Tooltip>
        </TooltipProvider>
        
        {isBlocked && (
          <Badge variant={getBlockBadgeVariant(reason)}>
            {getBlockBadgeLabel(reason)}
          </Badge>
        )}
      </div>
    );
  },
},
```

### Step 4: Update Patient Detail Page

**File**: `src/app/admin/accounts/users/[patientCode]/page.tsx`

```typescript
export default function PatientDetailPage({ params }: { params: { patientCode: string } }) {
  const [patient, setPatient] = useState<Patient | null>(null);
  
  // ... fetch patient data ...
  
  return (
    <div className="space-y-6">
      {/* Existing patient info sections */}
      
      {/* Booking Status Section */}
      <Card>
        <CardHeader>
          <CardTitle>Trang thai dat lich</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Checkbox
                checked={patient.isBookingBlocked}
                disabled
                className={cn(
                  "h-6 w-6 border-2",
                  patient.isBookingBlocked && isTemporaryBlock(patient.bookingBlockReason)
                    ? "border-orange-500 data-[state=checked]:bg-orange-500"
                    : patient.isBookingBlocked
                    ? "border-red-500 data-[state=checked]:bg-red-500"
                    : ""
                )}
              />
              <div>
                <p className="font-medium">
                  {patient.isBookingBlocked ? 'Bi chan dat lich' : 'Co the dat lich'}
                </p>
                <p className="text-sm text-muted-foreground">
                  So lan bo hen lien tiep: {patient.consecutiveNoShows}
                </p>
              </div>
            </div>
            
            {patient.isBookingBlocked && (
              <Badge variant={getBlockBadgeVariant(patient.bookingBlockReason)}>
                {getBlockBadgeLabel(patient.bookingBlockReason)}
              </Badge>
            )}
          </div>
          
          {patient.isBookingBlocked && (
            <Alert variant={isTemporaryBlock(patient.bookingBlockReason) ? "default" : "destructive"}>
              <AlertCircle className="h-4 w-4" />
              <AlertTitle>{getBlockReasonLabel(patient.bookingBlockReason)}</AlertTitle>
              <AlertDescription>
                <div className="space-y-2 mt-2">
                  <p>{getBlockReasonDescription(patient.bookingBlockReason)}</p>
                  
                  {patient.bookingBlockNotes && (
                    <p className="text-sm">
                      <strong>Ghi chu:</strong> {patient.bookingBlockNotes}
                    </p>
                  )}
                  
                  {patient.blockedBy && (
                    <p className="text-sm">
                      <strong>Bi chan boi:</strong> {patient.blockedBy}
                    </p>
                  )}
                  
                  {patient.blockedAt && (
                    <p className="text-sm">
                      <strong>Thoi gian chan:</strong> {formatDateTime(patient.blockedAt)}
                    </p>
                  )}
                </div>
              </AlertDescription>
            </Alert>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
```

### Step 5: Update Create Appointment Validation

**File**: `src/app/admin/appointments/create/page.tsx`

```typescript
const handlePatientSelect = (patient: Patient) => {
  setSelectedPatient(patient);
  
  // Check if patient is blocked
  if (patient.isBookingBlocked) {
    toast({
      title: "Benh nhan bi chan dat lich",
      description: patient.bookingBlockNotes || getBlockReasonDescription(patient.bookingBlockReason),
      variant: "destructive"
    });
    
    // Optionally prevent form submission
    setCanSubmit(false);
  } else {
    setCanSubmit(true);
  }
};

const handleSubmit = async () => {
  // Final check before submission
  if (selectedPatient?.isBookingBlocked) {
    toast({
      title: "Khong the dat lich",
      description: "Benh nhan nay dang bi chan dat lich. Vui long lien he quan ly.",
      variant: "destructive"
    });
    return;
  }
  
  // ... proceed with appointment creation ...
};
```

---

## UI/UX Guidelines

### Visual Indicators

**Checkbox Colors**:
- Temporary Block: Orange (`border-orange-500`, `bg-orange-500`)
- Permanent Block: Red (`border-red-500`, `bg-red-500`)
- Not Blocked: Default gray

**Badge Labels**:
- Temporary Block: "Tam chan" (Warning variant, orange)
- Permanent Block: "Chan" (Destructive variant, red)

**Alert Styling**:
- Temporary Block: Default alert (blue/gray)
- Permanent Block: Destructive alert (red)

### User Warnings

1. **Patient List Page**:
   - Show checkbox + badge for blocked patients
   - Tooltip on hover showing block details
   - Sort blocked patients to top (optional)

2. **Patient Detail Page**:
   - Dedicated "Booking Status" section
   - Clear alert with reason and description
   - Show block metadata (who, when, notes)

3. **Appointment Creation**:
   - Warning toast when selecting blocked patient
   - Prevent form submission with clear message
   - Suggest contacting manager for unblock

### Accessibility

- Use proper ARIA labels for checkboxes
- Ensure color contrast meets WCAG AA standards
- Provide text alternatives to color indicators
- Support keyboard navigation for tooltips

---

## Testing Checklist

### Frontend Tests

- [ ] Patient List displays blocking fields correctly
- [ ] Patient Detail shows block status section
- [ ] Temporary blocks show orange indicators
- [ ] Permanent blocks show red indicators
- [ ] Tooltips display block details on hover
- [ ] Badges show correct labels
- [ ] Create Appointment warns when selecting blocked patient
- [ ] Create Appointment prevents submission for blocked patients
- [ ] No-show counter displays correctly
- [ ] Block reason labels are in Vietnamese
- [ ] All fields handle null values gracefully

### API Integration Tests

```typescript
// Test 1: Fetch patient list
const response = await fetch('/api/v1/patients?page=0&size=10', {
  headers: { Authorization: `Bearer ${token}` }
});
const data = await response.json();
expect(data.content[0]).toHaveProperty('isBookingBlocked');
expect(data.content[0]).toHaveProperty('consecutiveNoShows');

// Test 2: Fetch patient detail
const patient = await fetch('/api/v1/patients/BN-1001', {
  headers: { Authorization: `Bearer ${token}` }
});
const patientData = await patient.json();
expect(patientData.isBookingBlocked).toBeDefined();

// Test 3: Check blocked patient
const blockedPatient = data.content.find(p => p.isBookingBlocked === true);
if (blockedPatient) {
  expect(blockedPatient.bookingBlockReason).toBeTruthy();
  expect(['EXCESSIVE_NO_SHOWS', 'PAYMENT_ISSUES', ...]).toContain(
    blockedPatient.bookingBlockReason
  );
}
```

---

## Common Issues and Solutions

### Issue 1: Fields showing undefined

**Problem**: `patient.isBookingBlocked` is `undefined`

**Solution**: 
- Check API response in browser DevTools Network tab
- Verify backend is updated (check response has blocking fields)
- Clear browser cache and cookies
- Check TypeScript type definitions match API response

### Issue 2: Enum values not matching

**Problem**: `bookingBlockReason` value not recognized

**Solution**:
```typescript
// Use type guard
const isValidBlockReason = (value: any): value is BookingBlockReason => {
  return Object.values(BookingBlockReason).includes(value);
};

if (patient.bookingBlockReason && isValidBlockReason(patient.bookingBlockReason)) {
  // Safe to use
}
```

### Issue 3: Date formatting errors

**Problem**: `blockedAt` showing raw ISO string

**Solution**:
```typescript
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';

const formatDateTime = (isoString: string) => {
  return format(parseISO(isoString), 'dd/MM/yyyy HH:mm', { locale: vi });
};
```

---

## Migration Notes

### Backward Compatibility

All new fields are nullable/optional, so existing code will continue to work. However, you should update UI to display blocking status ASAP.

### Recommended Migration Path

1. **Phase 1** (Immediate): Update TypeScript types
2. **Phase 2** (Same PR): Add basic blocking display in Patient List
3. **Phase 3** (Same PR): Add detailed blocking section in Patient Detail
4. **Phase 4** (Same PR): Add validation in Appointment Creation
5. **Phase 5** (Testing): Verify all scenarios

### Testing with Seed Data

Current seed data has all patients with `isBookingBlocked: false`. To test blocking UI:

**Option A**: Use Backend API to block a patient
```bash
PATCH /api/v1/patients/BN-1001
{
  "isBookingBlocked": true,
  "bookingBlockReason": "EXCESSIVE_NO_SHOWS",
  "bookingBlockNotes": "Test blocking",
  "consecutiveNoShows": 3
}
```

**Option B**: Mock data in frontend during development
```typescript
const mockBlockedPatient = {
  ...patient,
  isBookingBlocked: true,
  bookingBlockReason: BookingBlockReason.EXCESSIVE_NO_SHOWS,
  consecutiveNoShows: 3
};
```

---

## API Documentation References

- Patient List API: `docs/api-guides/patient/API_4.1_Get_Patient_List.md`
- Patient Detail API: `docs/api-guides/patient/API_4.2_Get_Patient_Detail.md`
- Create Patient API: `docs/api-guides/patient/API_4.3_Create_Patient.md`
- Update Patient API: `docs/api-guides/patient/API_4.4_Update_Patient.md`

---

## Support and Questions

For questions or issues:
1. Check this guide first
2. Verify API responses in browser DevTools
3. Check backend logs for errors
4. Contact backend team if API returns unexpected data

**Backend Contact**: Backend team
**Last Verified**: 2025-12-10
**Backend Deploy Status**: Ready for production

---

## Changelog

### 2025-12-10 - Initial Release
- Added 6 blocking fields to PatientInfoResponse
- Updated PatientMapper to include blocking fields
- All Patient APIs now return blocking information
- Fully tested and verified
- Ready for FE integration
