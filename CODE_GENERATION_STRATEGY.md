# Code Generation Strategy Documentation

## Overview
This document explains the two different code/ID generation strategies used in the Dental Clinic Management System.

## Strategy 1: Date-Based IDs (for Contact Management)

### Entities
- **CustomerContact** - Contact ID
- **ContactHistory** - History ID

### Format
```
PREFIX-YYMMDD-SEQ
```

### Examples
- `CTC-251016-001` - First customer contact on Oct 16, 2025
- `CTC-251016-002` - Second customer contact on Oct 16, 2025
- `CTH-251016-001` - First contact history on Oct 16, 2025

### Implementation
- **Generator**: `IdGenerator.java`
- **Column**: VARCHAR(20)
- **Generated**: In `@PrePersist` method of entity
- **Counter**: Resets daily, tracked by date

### Usage Example
```java
@Entity
public class CustomerContact {
    @Id
    @Column(name = "contact_id", length = 20)
    private String contactId;
    
    @PrePersist
    protected void onCreate() {
        if (contactId == null && idGenerator != null) {
            contactId = idGenerator.generateId("CTC");
        }
        // ... rest of code
    }
}
```

## Strategy 2: Sequential Codes (for Core Entities)

### Entities
- **Account** - Account Code
- **Employee** - Employee Code
- **Patient** - Patient Code

### Format
```
PREFIX###
```
Where `###` is the auto-increment ID (padded to 3 digits)

### Examples
- `ACC001` - Account with ID = 1
- `ACC151` - Account with ID = 151
- `EMP002` - Employee with ID = 2
- `PAT012` - Patient with ID = 12

### Implementation
- **Generator**: `SequentialCodeGenerator.java`
- **Primary Key**: INTEGER (auto-increment)
- **Code Column**: VARCHAR(20) (separate field)
- **Generated**: In service layer after entity is saved

### Database Schema
```sql
-- Example for Account
CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    account_code VARCHAR(20) UNIQUE NOT NULL,
    username VARCHAR(50) NOT NULL,
    -- ... other fields
);
```

### Usage Example in Service Layer
```java
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final SequentialCodeGenerator codeGenerator;
    
    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setUsername(request.getUsername());
        // ... set other fields ...
        
        // Save to get auto-generated ID
        Account saved = accountRepository.save(account);
        
        // Generate code based on ID
        saved.setAccountCode(codeGenerator.generateAccountCode(saved.getAccountId()));
        
        // Save again with code
        return accountRepository.save(saved);
    }
}
```

### Why Generate Code After Save?
1. **Consistency**: Code is based on actual database ID
2. **Reliability**: No separate counter to manage
3. **Simplicity**: No risk of counter/ID mismatch
4. **Persistence**: Code survives application restart

## Strategy Comparison

| Aspect | Date-Based IDs | Sequential Codes |
|--------|---------------|------------------|
| **Format** | CTC-251016-001 | ACC001 |
| **Primary Key Type** | VARCHAR(20) | INTEGER |
| **Separate Code Field** | No (ID is the code) | Yes (code + ID) |
| **Counter Reset** | Daily | Never (based on ID) |
| **Generated When** | @PrePersist | Service layer post-save |
| **Use Case** | Audit/tracking records | Core business entities |

## Migration Notes

### For Date-Based IDs
No migration needed for new records. Old UUID records need data migration.

### For Sequential Codes
1. Change primary key from VARCHAR(36) to INT
2. Add code field VARCHAR(20)
3. Migrate existing UUIDs to integer IDs
4. Generate codes for existing records:
   ```sql
   UPDATE accounts SET account_code = CONCAT('ACC', LPAD(account_id, 3, '0'));
   UPDATE employees SET employee_code = CONCAT('EMP', LPAD(employee_id, 3, '0'));
   UPDATE patients SET patient_code = CONCAT('PAT', LPAD(patient_id, 3, '0'));
   ```

## Current Implementation Status

### ✅ Completed
- [x] CustomerContact (CTC-YYMMDD-SEQ) - Date-based ID
- [x] ContactHistory (CTH-YYMMDD-SEQ) - Date-based ID
- [x] IdGenerator utility class
- [x] SequentialCodeGenerator utility class
- [x] Account entity updated (structure only)

### ⚠️ In Progress / Not Started
- [ ] Account service integration with SequentialCodeGenerator
- [ ] Employee entity update (ID: String → Integer, code generation)
- [ ] Patient entity update (ID: String → Integer, code generation)
- [ ] Employee service integration with SequentialCodeGenerator
- [ ] Patient service integration with SequentialCodeGenerator
- [ ] Update all foreign key references
- [ ] Update all DTOs, requests, responses
- [ ] Update all repositories
- [ ] Database migration scripts

### 📋 Entities Not Changing IDs
- **Role** - Uses role name as ID (e.g., ROLE_ADMIN) - VARCHAR(50)
- **Permission** - Uses permission name as ID (e.g., CREATE_CONTACT) - VARCHAR(50)
- **Specialization** - Uses simple integers (1, 2, 3...) - VARCHAR(20)
- **RefreshToken** - Not touching

## Questions for Discussion

1. **Should we proceed with Account/Employee/Patient ID refactoring?**
   - This is a breaking change affecting 80-100+ files
   - Requires complex database migration
   - Alternative: Keep UUID IDs, only add code fields

2. **Code field: Required or Optional?**
   - Currently set as nullable for Account
   - Should it be required (NOT NULL)?

3. **Code generation timing:**
   - Current approach: Generate after first save (requires 2 saves)
   - Alternative: Use database triggers
   - Alternative: Use JPA @PostPersist listener

4. **What about existing records?**
   - How to handle UUID → Integer migration?
   - Migration script strategy?

Please review and provide feedback!
