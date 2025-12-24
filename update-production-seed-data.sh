#!/bin/bash
# Production Seed Data Update Script
# This script can be run anytime to update/sync seed data to production DB
# Safe to run multiple times (idempotent)

set -e

echo "=========================================="
echo "🔄 Updating Production Seed Data"
echo "=========================================="

# Configuration
CONTAINER_NAME="dentalclinic-postgres"
DB_USER="${DB_USERNAME:-root}"
DB_NAME="${DB_DATABASE:-dental_clinic_db}"
SEED_FILE="./src/main/resources/db/dental-clinic-seed-data.sql"

# Check if container is running
if ! docker ps | grep -q "$CONTAINER_NAME"; then
    echo "❌ Error: Container $CONTAINER_NAME is not running!"
    echo "   Start it with: docker-compose up -d postgres"
    exit 1
fi

# Check if seed file exists
if [ ! -f "$SEED_FILE" ]; then
    echo "❌ Error: Seed file not found: $SEED_FILE"
    exit 1
fi

echo "📋 Container: $CONTAINER_NAME"
echo "📋 Database: $DB_NAME"
echo "📋 Seed file: $SEED_FILE"
echo ""

# Backup current data (optional but recommended)
echo "💾 Creating backup..."
BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
docker exec "$CONTAINER_NAME" pg_dump -U "$DB_USER" "$DB_NAME" > "$BACKUP_FILE" 2>/dev/null || true
if [ -f "$BACKUP_FILE" ]; then
    echo "   ✓ Backup saved to: $BACKUP_FILE"
else
    echo "   ⚠ Backup skipped (not critical)"
fi
echo ""

# Show current state
echo "📊 Current database state:"
docker exec "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" -t -c "
SELECT 
    'Permissions: ' || COUNT(*) FROM permissions
UNION ALL SELECT 
    'Roles: ' || COUNT(*) FROM roles
UNION ALL SELECT 
    'Role-Permissions: ' || COUNT(*) FROM role_permissions
UNION ALL SELECT 
    'Employees: ' || COUNT(*) FROM employees
UNION ALL SELECT 
    'Patients: ' || COUNT(*) FROM patients;
" | sed 's/^[[:space:]]*/   /'
echo ""

# Execute seed data
echo "🚀 Running seed data update..."
docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" < "$SEED_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✅ Seed data updated successfully!"
    echo "=========================================="
    echo ""
    
    # Show updated state
    echo "📊 Updated database state:"
    docker exec "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" -t -c "
    SELECT 
        'Permissions: ' || COUNT(*) FROM permissions
    UNION ALL SELECT 
        'Roles: ' || COUNT(*) FROM roles
    UNION ALL SELECT 
        'Role-Permissions: ' || COUNT(*) FROM role_permissions
    UNION ALL SELECT 
        'Employees: ' || COUNT(*) FROM employees
    UNION ALL SELECT 
        'Patients: ' || COUNT(*) FROM patients;
    " | sed 's/^[[:space:]]*/   /'
    echo ""
    
    # Verify critical permissions
    echo "🔍 Verifying critical permissions:"
    docker exec "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" -t -c "
    SELECT 
        '   ' || permission_code || ': ' || COUNT(rp.role_code) || ' roles'
    FROM permissions p
    LEFT JOIN role_permissions rp ON p.permission_code = rp.permission_code
    WHERE p.permission_code LIKE '%NOTIFICATION%'
    GROUP BY p.permission_code
    ORDER BY p.permission_code;
    "
    echo ""
    echo "✅ Production seed data is now in sync!"
else
    echo ""
    echo "=========================================="
    echo "❌ Error updating seed data!"
    echo "=========================================="
    echo ""
    echo "To rollback, run:"
    echo "   docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < $BACKUP_FILE"
    exit 1
fi
