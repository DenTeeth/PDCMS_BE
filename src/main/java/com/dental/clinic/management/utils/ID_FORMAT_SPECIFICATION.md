# ID Format Specification - Working Schedule Module

## Overview

All working schedule related entities use a standardized ID format: **PREFIXYYMMDDSSS**

## Format Structure

- **PREFIX**: 3-character identifier for the entity type
- **YYMMDD**: Date component (6 digits)
  - YY: Year (last 2 digits) - e.g., 25 = 2025
  - MM: Month (2 digits) - e.g., 10 = October
  - DD: Day (2 digits) - e.g., 22 = Day 22
- **SSS**: Daily sequence number (001-999)

## Total Length

12 characters (3 + 6 + 3)

## Entity ID Prefixes

| Entity                        | Prefix | Description                 | Example                                    |
| ----------------------------- | ------ | --------------------------- | ------------------------------------------ |
| **OvertimeRequest**           | `OTR`  | Overtime Request            | `OTR251022001` = Oct 22, 2025, sequence #1 |
| **TimeOffRequest**            | `TOR`  | Time-Off Request            | `TOR251022001` = Oct 22, 2025, sequence #1 |
| **EmployeeShiftRegistration** | `ESR`  | Employee Shift Registration | `ESR251022001` = Oct 22, 2025, sequence #1 |
| **ShiftRenewalRequest**       | `SRR`  | Shift Renewal Request       | `SRR251022001` = Oct 22, 2025, sequence #1 |

## Examples

### Example 1: Overtime Request

```
OTR251022001
│││││││││└┴┴─ Sequence: 001 (first request of the day)
││││││└┴┴──── Day: 22
││││└┴─────── Month: 10 (October)
││└┴────────── Year: 25 (2025)
└┴─────────── Prefix: OTR (Overtime Request)
```

### Example 2: Time-Off Request on New Year

```
TOR260101005
│││││││││└┴┴─ Sequence: 005 (fifth request of the day)
││││││└┴┴──── Day: 01
││││└┴─────── Month: 01 (January)
││└┴────────── Year: 26 (2026)
└┴─────────── Prefix: TOR (Time-Off Request)
```

## Implementation

### Using IdGenerator

All entities use the centralized `IdGenerator` utility class located in `com.dental.clinic.management.utils`.

```java
@Entity
public class MyEntity {
    private static final String ID_PREFIX = "XXX";
    private static IdGenerator idGenerator;

    public static void setIdGenerator(IdGenerator generator) {
        idGenerator = generator;
    }

    @Id
    @Column(name = "entity_id", length = 12)
    private String entityId;

    @PrePersist
    protected void onCreate() {
        if (entityId == null && idGenerator != null) {
            entityId = idGenerator.generateId(ID_PREFIX);
        }
    }
}
```

### Configuration

The `EntityIdGeneratorConfig` class injects the `IdGenerator` into all entities during application startup:

```java
@Configuration
public class EntityIdGeneratorConfig {
    @PostConstruct
    public void configureEntities() {
        MyEntity.setIdGenerator(idGenerator);
    }
}
```

## Sequence Management

### Thread Safety

The `IdGenerator` uses `ConcurrentHashMap` and `AtomicInteger` to ensure thread-safe ID generation.

### Daily Reset

- Sequence counters are automatically reset for each new date
- Maximum 999 requests per day per entity type
- Old counters are cleaned up periodically to prevent memory growth

### Capacity

- Maximum requests per entity type per day: **999**
- If limit exceeded: `IllegalStateException` is thrown

## Date Component

### Date Format

- Always uses the **current date** when the entity is created
- Format: `YYMMDD` (6 digits)
- Range: `000101` (Jan 1, 2000) to `991231` (Dec 31, 2099)

### Examples by Date

| Date         | Format   | Example ID     |
| ------------ | -------- | -------------- |
| Jan 1, 2025  | `250101` | `OTR250101001` |
| Dec 31, 2025 | `251231` | `TOR251231999` |
| Oct 22, 2025 | `251022` | `ESR251022001` |

## Validation

### Format Validation

```java
public static boolean isValidFormat(String id) {
    // Length must be 12
    if (id == null || id.length() != 12) {
        return false;
    }

    // First 3 chars must be known prefix
    String prefix = id.substring(0, 3);
    if (!VALID_PREFIXES.contains(prefix)) {
        return false;
    }

    // Characters 4-9 must be numeric (date)
    String datePart = id.substring(3, 9);

    // Characters 10-12 must be numeric (sequence 001-999)
    String seqPart = id.substring(9, 12);

    try {
        LocalDate.parse(datePart, DateTimeFormatter.ofPattern("yyMMdd"));
        int seq = Integer.parseInt(seqPart);
        return seq >= 1 && seq <= 999;
    } catch (Exception e) {
        return false;
    }
}
```

## Benefits

### 1. **Readability**

- Easy to identify entity type from prefix
- Date is visible in the ID itself
- Useful for debugging and logging

### 2. **Sortability**

- IDs naturally sort by date
- Within same date, sorted by creation order

### 3. **No Database Dependency**

- Generated before entity is persisted
- No need for database sequences or auto-increment

### 4. **Distributed System Ready**

- Thread-safe generation
- No conflicts across multiple instances (within same day)

### 5. **Business Intelligence**

- Can extract date from ID for reporting
- Sequence number indicates daily volume

## Migration Notes

### Legacy ID Formats

If migrating from different ID formats:

1. Keep old IDs in a separate column for reference
2. Generate new IDs using this format
3. Update all foreign key references

### Database Constraints

```sql
-- Example constraint for OvertimeRequest
ALTER TABLE overtime_requests
ADD CONSTRAINT chk_request_id_format
CHECK (
    request_id ~ '^OTR[0-9]{6}[0-9]{3}$'
    AND LENGTH(request_id) = 12
);
```

## Related Classes

- `IdGenerator.java` - Main ID generation utility
- `EntityIdGeneratorConfig.java` - Configuration for dependency injection
- `SequentialCodeGenerator.java` - For different ID format (ACC001, EMP001, PAT001)

## Support

For questions or issues related to ID generation, contact the development team or refer to:

- `IdGenerator.java` implementation
- Unit tests in `IdGeneratorTest.java`
