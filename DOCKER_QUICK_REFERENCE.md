# Docker & DigitalOcean Deployment - Quick Reference

## Files Created

### 1. Docker Configuration

- **Dockerfile** - Multi-stage build (Maven + JRE 17)
- **docker-compose.yml** - PostgreSQL + Redis + Spring Boot + Nginx
- **.dockerignore** - Exclude unnecessary files from build
- **nginx.conf** - Reverse proxy configuration

### 2. Environment & Deployment

- **.env.example** - Template for environment variables
- **DEPLOYMENT_GUIDE.md** - Complete deployment guide
- **start.sh** - Quick start script (Linux/Mac)
- **start.ps1** - Quick start script (Windows)

---

## Quick Start (Local Development)

### Windows

```powershell
# 1. Copy environment variables
cp .env.example .env

# 2. Edit .env with your values
notepad .env

# 3. Run quick start script
.\start.ps1
```

### Linux/Mac

```bash
# 1. Copy environment variables
cp .env.example .env

# 2. Edit .env with your values
nano .env

# 3. Make script executable
chmod +x start.sh

# 4. Run quick start script
./start.sh
```

### Manual Start

```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

---

## Environment Variables (.env)

### Required

```bash
# Database
DB_USERNAME=dentalclinic_user
DB_PASSWORD=<strong-password>
DB_DATABASE=dental_clinic_db

# Redis
REDIS_PASSWORD=<strong-password>

# JWT
JWT_SECRET=<generate-with-openssl>

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=<gmail-app-password>

# Frontend
FRONTEND_URL=https://your-frontend-domain.com
```

### Generate JWT Secret

```bash
# Linux/Mac
openssl rand -base64 64

# Windows PowerShell
[Convert]::ToBase64String((1..48|%{Get-Random -Minimum 0 -Maximum 256}))
```

---

## DigitalOcean Deployment

### Option 1: App Platform (Recommended)

1. Push code to GitHub
2. Create App in DigitalOcean
3. Connect GitHub repo
4. Add PostgreSQL database
5. Add Redis database
6. Configure environment variables
7. Deploy

**Cost:** ~$34/month (App + PostgreSQL + Redis)

### Option 2: Docker Droplet

1. Create Ubuntu droplet ($12/month)
2. Install Docker
3. Clone repository
4. Configure .env
5. Run `docker-compose up -d`

**Cost:** ~$12/month (self-managed)

See **DEPLOYMENT_GUIDE.md** for detailed instructions.

---

## Architecture

```
┌─────────────────┐
│   Nginx Proxy   │  Port 80/443
│  (Optional)     │
└────────┬────────┘
         │
┌────────▼────────┐
│  Spring Boot    │  Port 8080
│  Application    │
└────┬────────┬───┘
     │        │
     │        │
┌────▼────┐ ┌▼────────┐
│PostgreSQL│ │ Redis   │
│Port 5432│ │Port 6379│
└─────────┘ └─────────┘
```

---

## Health Checks

### Application Health

```bash
curl http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

### Database Connection

```bash
# PostgreSQL
docker exec -it dentalclinic-postgres psql -U root -d dental_clinic_db

# Redis
docker exec -it dentalclinic-redis redis-cli -a your-password
```

---

## Useful Commands

### Docker Compose

```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d app

# Rebuild and start
docker-compose up -d --build app

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Check status
docker-compose ps
```

### Docker

```bash
# View running containers
docker ps

# Execute command in container
docker exec -it dentalclinic-app bash

# View logs
docker logs -f dentalclinic-app

# Restart container
docker restart dentalclinic-app

# Remove all stopped containers
docker container prune

# Remove unused images
docker image prune -a
```

### Database Management

```bash
# Backup database
docker exec dentalclinic-postgres pg_dump -U root dental_clinic_db > backup.sql

# Restore database
docker exec -i dentalclinic-postgres psql -U root dental_clinic_db < backup.sql

# View database size
docker exec dentalclinic-postgres psql -U root -d dental_clinic_db -c "
  SELECT pg_database.datname,
         pg_size_pretty(pg_database_size(pg_database.datname)) AS size
  FROM pg_database;"
```

---

## Troubleshooting

### Port Already in Use

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Database Connection Failed

```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# View PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Application Won't Start

```bash
# View full logs
docker-compose logs app

# Check environment variables
docker exec dentalclinic-app env

# Rebuild from scratch
docker-compose down -v
docker-compose up -d --build
```

### Redis Connection Failed

```bash
# Check Redis logs
docker-compose logs redis

# Test Redis connection
docker exec -it dentalclinic-redis redis-cli -a your-password ping
# Expected: PONG
```

---

## Security Checklist

- [ ] Change default database password
- [ ] Set strong JWT secret (min 256 bits)
- [ ] Configure Gmail App Password
- [ ] Enable firewall (ufw on Ubuntu)
- [ ] Setup SSL certificate (Let's Encrypt)
- [ ] Use environment variables (never hardcode)
- [ ] Regular backups (database + Redis)
- [ ] Monitor logs for errors
- [ ] Update dependencies regularly
- [ ] Restrict database access to app only

---

## Performance Tuning

### JVM Options (Dockerfile)

```dockerfile
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
```

### Database Connection Pool (application.yaml)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

### Redis Cache TTL

```yaml
spring:
  data:
    redis:
      timeout: 60000
```

---

## Monitoring

### Application Metrics

- **Endpoint:** `/actuator/metrics`
- **Prometheus:** `/actuator/prometheus`
- **Health:** `/actuator/health`

### External Monitoring

- **UptimeRobot:** Free uptime monitoring
- **DigitalOcean Monitoring:** Built-in (enable in droplet settings)
- **Grafana + Prometheus:** Self-hosted monitoring stack

---

## Scheduled Jobs

### Warehouse Expiry Email Job

- **Schedule:** Daily at 8:00 AM
- **Cron:** `0 0 8 * * ?`
- **Recipients:** Users with `VIEW_WAREHOUSE` permission
- **Test:** See `WAREHOUSE_EXPIRY_EMAIL_NOTIFICATION_GUIDE.md`

---

## Support & Resources

- **Deployment Guide:** DEPLOYMENT_GUIDE.md
- **Expiry Email Guide:** WAREHOUSE_EXPIRY_EMAIL_NOTIFICATION_GUIDE.md
- **API Documentation:** docs/api-guides/
- **DigitalOcean Docs:** https://docs.digitalocean.com
- **Spring Boot Docs:** https://docs.spring.io/spring-boot
- **Docker Docs:** https://docs.docker.com

---

**Last Updated:** 2025-12-13
**Version:** 1.0
**Status:** Production Ready
