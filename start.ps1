# ================================================
# Dental Clinic Management System - Quick Start (Windows)
# ================================================

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Dental Clinic Management System - Docker Setup" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is installed
try {
    docker --version | Out-Null
    Write-Host "✓ Docker is installed" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Docker is not installed!" -ForegroundColor Red
    Write-Host "Please install Docker Desktop: https://docs.docker.com/desktop/install/windows-install/" -ForegroundColor Yellow
    exit 1
}

# Check if Docker Compose is installed
try {
    docker-compose --version | Out-Null
    Write-Host "✓ Docker Compose is installed" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Docker Compose is not installed!" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Check if .env exists
if (-not (Test-Path .env)) {
    Write-Host "⚠ .env file not found!" -ForegroundColor Yellow
    Write-Host "Creating .env from .env.example..." -ForegroundColor Yellow

    if (Test-Path .env.example) {
        Copy-Item .env.example .env
        Write-Host "✓ .env file created from .env.example" -ForegroundColor Green
        Write-Host ""
        Write-Host "================================================" -ForegroundColor Cyan
        Write-Host "IMPORTANT: Please edit .env file with your values" -ForegroundColor Yellow
        Write-Host "================================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Required changes:" -ForegroundColor Yellow
        Write-Host "1. JWT_SECRET - Generate with: openssl rand -base64 64" -ForegroundColor White
        Write-Host "2. DB_PASSWORD - Set a strong password" -ForegroundColor White
        Write-Host "3. REDIS_PASSWORD - Set a strong password" -ForegroundColor White
        Write-Host "4. MAIL_USERNAME and MAIL_PASSWORD - Gmail credentials" -ForegroundColor White
        Write-Host ""
        Read-Host "Press Enter after editing .env file to continue"
    } else {
        Write-Host "ERROR: .env.example not found!" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✓ .env file exists" -ForegroundColor Green
Write-Host ""

# Ask user which mode to run
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Select deployment mode:" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "1) Development (without nginx)" -ForegroundColor White
Write-Host "2) Production (with nginx reverse proxy)" -ForegroundColor White
Write-Host ""
$mode = Read-Host "Enter choice [1 or 2]"

switch ($mode) {
    "1" {
        Write-Host ""
        Write-Host "Starting in DEVELOPMENT mode..." -ForegroundColor Green
        Write-Host ""
        docker-compose up -d postgres redis app
    }
    "2" {
        Write-Host ""
        Write-Host "Starting in PRODUCTION mode..." -ForegroundColor Green
        Write-Host ""
        docker-compose --profile with-nginx up -d
    }
    default {
        Write-Host "Invalid choice. Starting in DEVELOPMENT mode..." -ForegroundColor Yellow
        docker-compose up -d postgres redis app
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Waiting for services to start..." -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 10

# Check service status
Write-Host ""
Write-Host "Service Status:" -ForegroundColor Cyan
Write-Host "===============" -ForegroundColor Cyan
docker-compose ps

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Checking application health..." -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 5

# Wait for app to be healthy
$maxRetries = 30
$retryCount = 0

while ($retryCount -lt $maxRetries) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host ""
            Write-Host "✓ Application is healthy!" -ForegroundColor Green
            break
        }
    } catch {
        Write-Host -NoNewline "." -ForegroundColor Yellow
        Start-Sleep -Seconds 2
        $retryCount++
    }
}

if ($retryCount -eq $maxRetries) {
    Write-Host ""
    Write-Host "⚠ Application health check timed out" -ForegroundColor Yellow
    Write-Host "Check logs with: docker-compose logs app" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Deployment Complete!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Application is running at:" -ForegroundColor Green
Write-Host "  - API: http://localhost:8080" -ForegroundColor White
Write-Host "  - Health: http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host "  - Swagger: http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "Database:" -ForegroundColor Green
Write-Host "  - PostgreSQL: localhost:5432" -ForegroundColor White
Write-Host "  - Redis: localhost:6379" -ForegroundColor White
Write-Host ""

if ($mode -eq "2") {
    Write-Host "Nginx:" -ForegroundColor Green
    Write-Host "  - HTTP: http://localhost:80" -ForegroundColor White
    Write-Host "  - HTTPS: https://localhost:443 (configure SSL)" -ForegroundColor White
    Write-Host ""
}

Write-Host "Useful commands:" -ForegroundColor Green
Write-Host "  - View logs: docker-compose logs -f app" -ForegroundColor White
Write-Host "  - Stop services: docker-compose down" -ForegroundColor White
Write-Host "  - Restart app: docker-compose restart app" -ForegroundColor White
Write-Host "  - Rebuild: docker-compose up -d --build app" -ForegroundColor White
Write-Host ""
Write-Host "Test login:" -ForegroundColor Green
Write-Host "  username: admin" -ForegroundColor White
Write-Host "  password: 123456" -ForegroundColor White
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
