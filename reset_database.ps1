# PowerShell script to reset PostgreSQL database
# Run this before starting Spring Boot application

$env:PGPASSWORD = "123456"

Write-Host "🔄 Resetting database dental_clinic_db..." -ForegroundColor Yellow

# Terminate all connections to the database
& "C:\Program Files\PostgreSQL\13\bin\psql.exe" -U root -d postgres -c "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = 'dental_clinic_db' AND pid <> pg_backend_pid();"

# Drop and recreate database
& "C:\Program Files\PostgreSQL\13\bin\psql.exe" -U root -d postgres -c "DROP DATABASE IF EXISTS dental_clinic_db;"
& "C:\Program Files\PostgreSQL\13\bin\psql.exe" -U root -d postgres -c "CREATE DATABASE dental_clinic_db WITH OWNER = root ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8' TEMPLATE = template0;"

Write-Host "✅ Database reset successfully!" -ForegroundColor Green
Write-Host "Now you can start Spring Boot application." -ForegroundColor Cyan

$env:PGPASSWORD = ""
