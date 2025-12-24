#!/bin/bash
# Init script to load seed data into PostgreSQL
# This script runs automatically when container starts for the first time
# or when FORCE_RESEED=true environment variable is set

set -e

echo "=========================================="
echo "Checking if seed data needs to be loaded..."
echo "=========================================="

# Check if database is already seeded
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Check if seed data exists (check if permissions table has data)
    DO \$\$
    DECLARE
        row_count INTEGER;
        force_reseed BOOLEAN := COALESCE(current_setting('app.force_reseed', true)::BOOLEAN, false);
    BEGIN
        SELECT COUNT(*) INTO row_count FROM permissions;
        
        IF row_count = 0 OR force_reseed THEN
            RAISE NOTICE '========================================';
            RAISE NOTICE 'Loading seed data...';
            RAISE NOTICE '========================================';
            
            -- If force reseed, truncate tables first (except system tables)
            IF force_reseed THEN
                RAISE NOTICE 'Force reseed enabled, cleaning existing data...';
                -- Add truncate statements here if needed
            END IF;
        ELSE
            RAISE NOTICE '========================================';
            RAISE NOTICE 'Database already seeded. Skipping...';
            RAISE NOTICE 'Set FORCE_RESEED=true to reload data.';
            RAISE NOTICE '========================================';
        END IF;
    END;
    \$\$;
EOSQL

# Load seed data if needed
SEED_COUNT=$(psql -t -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -c "SELECT COUNT(*) FROM permissions;")
SEED_COUNT=$(echo $SEED_COUNT | xargs) # Trim whitespace

if [ "$SEED_COUNT" = "0" ] || [ "$FORCE_RESEED" = "true" ]; then
    echo "Loading seed data from dental-clinic-seed-data.sql..."
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" < /docker-entrypoint-initdb.d/03-seed-data.sql
    
    echo "=========================================="
    echo "✓ Seed data loaded successfully!"
    echo "=========================================="
else
    echo "Seed data already exists. Skipping."
fi
