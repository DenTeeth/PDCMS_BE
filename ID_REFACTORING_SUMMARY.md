# ID Refactoring Summary

This document summarizes the changes made to refactor entity IDs from UUID format to custom formats.

## Overview

Changed ID generation strategy for the following entities:

### 1. **CustomerContact** ✅
- **Old Format**: UUID (36 characters) - e.g., `550e8400-e29b-41d4-a716-446655440000`
- **New Format**: `CTC-YYMMDD-SEQ` (20 characters) - e.g., `CTC-251016-001`
- **Changes Made**:
  - Updated `contact_id` column from `VARCHAR(36)` to `VARCHAR(20)`
  - Added `IdGenerator` injection
  - Modified `@PrePersist` to generate ID using format `CTC-YYMMDD-SEQ`

### 2. **ContactHistory** ✅
- **Old Format**: UUID (36 characters)
- **New Format**: `CTH-YYMMDD-SEQ` (20 characters) - e.g., `CTH-251016-001`
- **Changes Made**:
  - Updated `history_id` column from `VARCHAR(36)` to `VARCHAR(20)`
  - Updated `contact_id` foreign key from `VARCHAR(36)` to `VARCHAR(20)`
  - Added `IdGenerator` injection
  - Modified `@PrePersist` to generate ID using format `CTH-YYMMDD-SEQ`

### 3. **Permission** ✅
- **Old Format**: UUID (36 characters)
- **New Format**: Permission name (e.g., `CREATE_CONTACT`, `UPDATE_CONTACT`)
- **Changes Made**:
  - Updated `permission_id` column from `VARCHAR(30)` to `VARCHAR(50)`
  - Modified `PermissionService.createPermission()` to use permission name as ID
  - Removed UUID import and generation

### 4. **Role** ✅
- **Format**: Role name (e.g., `ROLE_ADMIN`, `ROLE_RECEPTIONIST`)
- **Status**: Already using `VARCHAR(50)` - no changes needed
- **Note**: RoleId already matches roleName in the current implementation

### 5. **Specialization** ✅
- **Old Format**: UUID (36 characters)
- **New Format**: Simple integer as string (e.g., `1`, `2`, `3`, `4`, `5`)
- **Changes Made**:
  - Updated `specialization_id` column from `VARCHAR(36)` to `VARCHAR(20)`
  - Updated seed data to use integers `1-7` instead of UUIDs
  - Updated `employee_specializations` join table references

## Files Modified

### Domain/Entity Files
1. `customer_contact/domain/CustomerContact.java`
   - Changed ID column length to 20
   - Added static IdGenerator field
   - Added setIdGenerator() method
   - Modified @PrePersist to generate CTC format

2. `contact_history/domain/ContactHistory.java`
   - Changed ID column length to 20
   - Changed contact_id column length to 20
   - Added static IdGenerator field
   - Added setIdGenerator() method
   - Modified @PrePersist to generate CTH format

3. `permission/domain/Permission.java`
   - Changed ID column length from 30 to 50

4. `specialization/domain/Specialization.java`
   - Changed ID column length from 36 to 20

### Service Files
1. `permission/service/PermissionService.java`
   - Removed UUID import
   - Changed createPermission() to use permissionName as ID

### Utility Files (New)
1. **`utils/IdGenerator.java`** (NEW)
   - Central ID generation service
   - Generates IDs in format: `PREFIX-YYMMDD-SEQ`
   - Thread-safe with daily counter reset
   - Methods:
     - `generateId(String prefix)`: Generate new ID
     - `cleanupOldCounters()`: Clean up old date counters

### Configuration Files (New)
1. **`config/EntityIdGeneratorConfig.java`** (NEW)
   - Injects IdGenerator into entity static fields
   - Called at application startup via @PostConstruct

### Database Files
1. `resources/db/dental-clinic-seed-data.sql`
   - Updated specialization IDs from UUIDs to integers (1-7)
   - Updated employee_specializations references to match

## Entities NOT Changed (As Requested)
- ❌ **Account** - accountId (still UUID)
- ❌ **Employee** - employeeId (still UUID)
- ❌ **Patient** - patientId (still UUID)
- ❌ **RefreshToken** - tokenId (not touched)

## Database Migration Required

After these code changes, you'll need to run database migrations to:

1. **Alter column definitions**:
   ```sql
   ALTER TABLE customer_contacts MODIFY COLUMN contact_id VARCHAR(20);
   ALTER TABLE contact_history MODIFY COLUMN history_id VARCHAR(20);
   ALTER TABLE contact_history MODIFY COLUMN contact_id VARCHAR(20);
   ALTER TABLE permissions MODIFY COLUMN permission_id VARCHAR(50);
   ALTER TABLE specializations MODIFY COLUMN specialization_id VARCHAR(20);
   ALTER TABLE employee_specializations MODIFY COLUMN specialization_id VARCHAR(20);
   ```

2. **Update existing data** (if any):
   - Backup your database first!
   - Migrate existing CustomerContact records to new format
   - Migrate existing ContactHistory records to new format
   - Update Permission IDs to match permission names
   - Update Specialization IDs to integers

3. **Re-run seed data** (recommended for fresh installations):
   ```bash
   # Execute the updated dental-clinic-seed-data.sql
   ```

## Testing Recommendations

1. **Unit Tests**: Test ID generation
   - Test IdGenerator generates correct format
   - Test daily counter increments
   - Test counter resets on new day

2. **Integration Tests**: Test entity persistence
   - Test CustomerContact creation with new ID format
   - Test ContactHistory creation with new ID format
   - Test Permission creation with name as ID
   - Test Specialization with integer IDs

3. **Foreign Key Tests**: Ensure relationships still work
   - Test ContactHistory -> CustomerContact FK
   - Test Employee -> Specialization FK (via join table)

## Next Steps

1. ✅ Code changes completed
2. ⚠️ **TODO**: Create database migration scripts
3. ⚠️ **TODO**: Update DTOs, Requests, Responses if needed
4. ⚠️ **TODO**: Update API documentation (Swagger/OpenAPI)
5. ⚠️ **TODO**: Test all endpoints
6. ⚠️ **TODO**: Update unit/integration tests

## ID Format Examples

| Entity | Old ID | New ID |
|--------|--------|--------|
| CustomerContact | `550e8400-e29b-41d4-a716-446655440000` | `CTC-251016-001` |
| ContactHistory | `660e8400-e29b-41d4-a716-446655440000` | `CTH-251016-001` |
| Permission | `770e8400-e29b-41d4-a716-446655440000` | `CREATE_CONTACT` |
| Specialization | `770e8400-e29b-41d4-a716-446655440001` | `1` |
| Role | Already using role names | `ROLE_ADMIN` |

## Notes

- The IdGenerator is thread-safe and handles concurrent requests
- The daily sequence counter resets automatically at midnight
- The format allows for 999 records per entity per day (001-999)
- If you need more than 999 records per day, consider increasing the sequence padding
