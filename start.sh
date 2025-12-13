#!/bin/bash

# ================================================
# Dental Clinic Management System - Quick Start
# ================================================

set -e

echo "================================================"
echo "Dental Clinic Management System - Docker Setup"
echo "================================================"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed!"
    echo "Please install Docker first: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "ERROR: Docker Compose is not installed!"
    echo "Please install Docker Compose first: https://docs.docker.com/compose/install/"
    exit 1
fi

echo "✓ Docker is installed"
echo "✓ Docker Compose is installed"
echo ""

# Check if .env exists
if [ ! -f .env ]; then
    echo "⚠ .env file not found!"
    echo "Creating .env from .env.example..."

    if [ -f .env.example ]; then
        cp .env.example .env
        echo "✓ .env file created from .env.example"
        echo ""
        echo "================================================"
        echo "IMPORTANT: Please edit .env file with your values"
        echo "================================================"
        echo ""
        echo "Required changes:"
        echo "1. JWT_SECRET - Generate with: openssl rand -base64 64"
        echo "2. DB_PASSWORD - Set a strong password"
        echo "3. REDIS_PASSWORD - Set a strong password"
        echo "4. MAIL_USERNAME and MAIL_PASSWORD - Gmail credentials"
        echo ""
        read -p "Press Enter after editing .env file to continue..."
    else
        echo "ERROR: .env.example not found!"
        exit 1
    fi
fi

echo "✓ .env file exists"
echo ""

# Ask user which mode to run
echo "================================================"
echo "Select deployment mode:"
echo "================================================"
echo "1) Development (without nginx)"
echo "2) Production (with nginx reverse proxy)"
echo ""
read -p "Enter choice [1 or 2]: " mode

case $mode in
    1)
        echo ""
        echo "Starting in DEVELOPMENT mode..."
        echo ""
        docker-compose up -d postgres redis app
        ;;
    2)
        echo ""
        echo "Starting in PRODUCTION mode..."
        echo ""
        docker-compose --profile with-nginx up -d
        ;;
    *)
        echo "Invalid choice. Starting in DEVELOPMENT mode..."
        docker-compose up -d postgres redis app
        ;;
esac

echo ""
echo "================================================"
echo "Waiting for services to start..."
echo "================================================"
sleep 10

# Check service status
echo ""
echo "Service Status:"
echo "==============="
docker-compose ps

echo ""
echo "================================================"
echo "Checking application health..."
echo "================================================"
sleep 5

# Wait for app to be healthy
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -f http://localhost:8080/actuator/health &> /dev/null; then
        echo ""
        echo "✓ Application is healthy!"
        break
    else
        echo -n "."
        sleep 2
        RETRY_COUNT=$((RETRY_COUNT+1))
    fi
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo ""
    echo "⚠ Application health check timed out"
    echo "Check logs with: docker-compose logs app"
    exit 1
fi

echo ""
echo "================================================"
echo "Deployment Complete!"
echo "================================================"
echo ""
echo "Application is running at:"
echo "  - API: http://localhost:8080"
echo "  - Health: http://localhost:8080/actuator/health"
echo "  - Swagger: http://localhost:8080/swagger-ui.html"
echo ""
echo "Database:"
echo "  - PostgreSQL: localhost:5432"
echo "  - Redis: localhost:6379"
echo ""
if [ "$mode" = "2" ]; then
    echo "Nginx:"
    echo "  - HTTP: http://localhost:80"
    echo "  - HTTPS: https://localhost:443 (configure SSL)"
    echo ""
fi
echo "Useful commands:"
echo "  - View logs: docker-compose logs -f app"
echo "  - Stop services: docker-compose down"
echo "  - Restart app: docker-compose restart app"
echo "  - Rebuild: docker-compose up -d --build app"
echo ""
echo "Test login:"
echo "  username: admin"
echo "  password: 123456"
echo ""
echo "================================================"
