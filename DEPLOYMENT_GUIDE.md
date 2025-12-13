# DigitalOcean Deployment Guide

Complete guide for deploying Dental Clinic Management System on DigitalOcean App Platform.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Option 1: DigitalOcean App Platform (Recommended)](#option-1-digitalocean-app-platform)
3. [Option 2: Docker Droplet (Manual)](#option-2-docker-droplet)
4. [Post-Deployment](#post-deployment)
5. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Accounts

- DigitalOcean account with payment method
- GitHub account (for automated deployments)
- Gmail account with App Password enabled

### Local Setup

```bash
# Ensure Docker is installed
docker --version

# Ensure Git is configured
git config --global user.name "Your Name"
git config --global user.email "your@email.com"
```

---

## Option 1: DigitalOcean App Platform (Recommended)

### Step 1: Prepare Repository

1. **Create `.env` file locally** (DO NOT commit)

```bash
cp .env.example .env
# Edit .env with your production values
```

2. **Update `.gitignore`**

```gitignore
# Add these lines if not present
.env
.env.local
.env.production
pgdata_dental/
*.log
target/
```

3. **Commit and push to GitHub**

```bash
git add .
git commit -m "feat: add Docker deployment configuration"
git push origin main
```

### Step 2: Create DigitalOcean App

1. **Go to DigitalOcean Console**

   - Navigate to: https://cloud.digitalocean.com/apps
   - Click "Create App"

2. **Connect GitHub Repository**

   - Choose "GitHub"
   - Authorize DigitalOcean
   - Select repository: `DenTeeth/PDCMS_BE`
   - Select branch: `feat/BE-901-business-rules-and-cloud-update` (or `main`)

3. **Configure App Components**

#### PostgreSQL Database

- Click "Add Resource" → "Database"
- Engine: PostgreSQL 15
- Plan: Basic ($12/month for dev, $15/month for prod)
- Database Name: `dental_clinic_db`
- User: `dentalclinic_user`

#### Redis Database

- Click "Add Resource" → "Database"
- Engine: Redis 7
- Plan: Basic ($7/month)

#### Spring Boot Service

- Type: **Web Service**
- Name: `dentalclinic-api`
- Environment: **Docker**
- Dockerfile Path: `Dockerfile`
- HTTP Port: `8080`
- Instance Size: Basic ($12/month - 1GB RAM)
- Instance Count: 1

### Step 3: Configure Environment Variables

In App Platform → Settings → Environment Variables, add:

```bash
# Spring Configuration
SPRING_PROFILES_ACTIVE=prod

# Database (auto-configured by DigitalOcean)
# DATABASE_URL will be injected automatically

# Redis (auto-configured by DigitalOcean)
# REDIS_URL will be injected automatically

# JWT Configuration
JWT_SECRET=<generate-with-openssl-rand-base64-64>
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=2592000000

# Email Configuration
MAIL_USERNAME=dentalclinicPDCMS@gmail.com
MAIL_PASSWORD=<your-gmail-app-password>

# Frontend URL
FRONTEND_URL=https://your-frontend-domain.com

# Application Port
PORT=8080
```

### Step 4: Configure Database Connection

DigitalOcean automatically injects `DATABASE_URL`, but Spring Boot needs it in `SPRING_DATASOURCE_URL` format.

**Add to Environment Variables:**

```bash
SPRING_DATASOURCE_URL=${DATABASE_URL}
```

Or update `application.yaml` to support both:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:${DATABASE_URL:jdbc:postgresql://localhost:5432/dental_clinic_db}}
```

### Step 5: Initialize Database Schema

**Option A: Manual SQL Execution**

```bash
# Connect to managed PostgreSQL
psql "postgresql://user:password@host:25060/dental_clinic_db?sslmode=require"

# Run schema
\i src/main/resources/db/schema.sql

# Run seed data
\i src/main/resources/db/dental-clinic-seed-data.sql
```

**Option B: Add Init Script to Dockerfile**

```dockerfile
# Add before ENTRYPOINT
COPY src/main/resources/db/schema.sql /app/schema.sql
COPY src/main/resources/db/dental-clinic-seed-data.sql /app/seed-data.sql
```

### Step 6: Deploy

1. **Review Configuration**

   - Check all environment variables
   - Verify database connections
   - Confirm health check endpoint: `/actuator/health`

2. **Deploy**

   - Click "Create Resources"
   - Wait 5-10 minutes for deployment

3. **Verify Deployment**
   - Check build logs
   - Visit: `https://dentalclinic-api-xxxxx.ondigitalocean.app/actuator/health`
   - Expected response: `{"status":"UP"}`

---

## Option 2: Docker Droplet (Manual)

### Step 1: Create Droplet

1. **Launch Droplet**

   - Go to: https://cloud.digitalocean.com/droplets
   - Create Droplet
   - Image: Ubuntu 22.04 LTS
   - Plan: Basic ($12/month - 2GB RAM recommended)
   - Add SSH key

2. **SSH into Droplet**

```bash
ssh root@your-droplet-ip
```

### Step 2: Install Docker

```bash
# Update system
apt update && apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Install Docker Compose
apt install docker-compose -y

# Verify installation
docker --version
docker-compose --version
```

### Step 3: Setup Application

```bash
# Clone repository
git clone https://github.com/DenTeeth/PDCMS_BE.git
cd PDCMS_BE

# Create .env file
nano .env
# Paste your environment variables (see .env.example)

# Start services
docker-compose up -d

# Check logs
docker-compose logs -f app
```

### Step 4: Configure Firewall

```bash
# Allow SSH, HTTP, HTTPS
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable

# Verify
ufw status
```

### Step 5: Setup Domain & SSL (Optional)

```bash
# Install Certbot
apt install certbot python3-certbot-nginx -y

# Get SSL certificate
certbot --nginx -d api.your-domain.com

# Auto-renewal is configured automatically
```

### Step 6: Setup Nginx Reverse Proxy

```bash
# Start nginx service
docker-compose --profile with-nginx up -d nginx

# Edit nginx.conf to add your domain
nano nginx.conf

# Reload nginx
docker-compose restart nginx
```

---

## Post-Deployment

### 1. Health Checks

```bash
# Application health
curl https://your-app-url/actuator/health

# Expected response
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

### 2. Test API Endpoints

```bash
# Login test
curl -X POST https://your-app-url/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

### 3. Monitor Logs

**DigitalOcean App Platform:**

- Go to App → Runtime Logs
- Monitor for errors

**Docker Droplet:**

```bash
# View application logs
docker-compose logs -f app

# View nginx logs
docker-compose logs -f nginx

# View database logs
docker-compose logs -f postgres
```

### 4. Setup Scheduled Jobs

Email notification cron job runs automatically at 8:00 AM daily.

Verify in logs:

```bash
# Search for email job logs
docker-compose logs app | grep "Warehouse Expiry Email Job"
```

### 5. Database Backups

**DigitalOcean Managed Database:**

- Automatic daily backups (free)
- Snapshots: Manual or scheduled

**Docker Droplet:**

```bash
# Create backup script
nano /root/backup-db.sh
```

```bash
#!/bin/bash
BACKUP_DIR="/root/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

docker exec postgres-dental pg_dump -U root dental_clinic_db | gzip > \
  $BACKUP_DIR/dental_clinic_db_$TIMESTAMP.sql.gz

# Keep only last 7 days
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
```

```bash
# Make executable
chmod +x /root/backup-db.sh

# Add to crontab (daily at 2 AM)
crontab -e
# Add: 0 2 * * * /root/backup-db.sh
```

---

## Troubleshooting

### App Won't Start

**Check logs:**

```bash
# App Platform
DigitalOcean Console → App → Runtime Logs

# Docker
docker-compose logs app
```

**Common issues:**

1. **Database connection failed**

   - Verify `SPRING_DATASOURCE_URL` is correct
   - Check database is running: `docker-compose ps`
   - Test connection: `psql "your-connection-string"`

2. **Port already in use**

   ```bash
   # Find process using port 8080
   lsof -i :8080
   # Kill process
   kill -9 <PID>
   ```

3. **Out of memory**
   - Increase JVM heap size in Dockerfile
   - Upgrade droplet/instance size

### Email Not Sending

1. **Check Gmail App Password**

   ```bash
   # Test SMTP connection
   curl --url 'smtps://smtp.gmail.com:465' \
     --ssl-reqd \
     --mail-from 'your-email@gmail.com' \
     --user 'your-email@gmail.com:your-app-password'
   ```

2. **Verify environment variables**

   ```bash
   docker exec dentalclinic-app env | grep MAIL
   ```

3. **Check logs for email errors**
   ```bash
   docker-compose logs app | grep -i "email\|mail"
   ```

### Database Schema Not Initialized

```bash
# Connect to database
docker exec -it postgres-dental psql -U root -d dental_clinic_db

# Check tables
\dt

# If empty, run schema manually
\i /docker-entrypoint-initdb.d/01-schema.sql
\i /docker-entrypoint-initdb.d/02-seed-data.sql
```

### Redis Connection Failed

```bash
# Test Redis connection
docker exec -it redis-dental redis-cli -a your-redis-password

# Inside redis-cli
> ping
PONG

# If fails, check password
docker-compose logs redis
```

### SSL Certificate Issues

```bash
# Renew Let's Encrypt certificate
certbot renew

# Test nginx configuration
nginx -t

# Reload nginx
docker-compose restart nginx
```

---

## Monitoring & Maintenance

### 1. Setup Uptime Monitoring

**DigitalOcean Monitoring:**

- Enable in Droplet settings
- Add alerting for CPU, RAM, Disk

**External Services:**

- UptimeRobot: https://uptimerobot.com
- Pingdom: https://www.pingdom.com
- Check endpoint: `/actuator/health`

### 2. Performance Optimization

**Enable JVM monitoring:**

```yaml
# application.yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Connection pooling:**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

### 3. Security Hardening

- Change default passwords immediately
- Enable 2FA on DigitalOcean
- Restrict database access to app only
- Use secrets management (DigitalOcean Vault)
- Regular security updates: `apt update && apt upgrade`

---

## Costs Estimation

### DigitalOcean App Platform

- App (1GB RAM): $12/month
- PostgreSQL (Basic): $15/month
- Redis (Basic): $7/month
- **Total: ~$34/month**

### Docker Droplet

- Droplet (2GB RAM): $12/month
- **Total: ~$12/month** (self-managed)

---

## Support

- **DigitalOcean Docs**: https://docs.digitalocean.com
- **Spring Boot Docs**: https://docs.spring.io/spring-boot
- **Project Issues**: https://github.com/DenTeeth/PDCMS_BE/issues

---

**Last Updated:** 2025-12-13
**Version:** 1.0
**Status:** Production Ready
