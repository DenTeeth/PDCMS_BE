# Redis Setup Quick Start

## 🚀 Start Redis Container

```bash
# Start all services (PostgreSQL + Redis)
docker-compose up -d

# Verify containers are running
docker ps

# Expected output:
# redis-dental    - running on port 6379
# postgres-dental - running on port 5432
```

## ✅ Check Redis Status

```bash
# Connect to Redis CLI
docker exec -it redis-dental redis-cli

# Test connection
ping
# Should return: PONG

# List all keys
KEYS *

# Exit
exit
```

## 🔥 Run Application

```powershell
# Set JAVA_HOME
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"

# Run Spring Boot
.\mvnw.cmd spring-boot:run
```

## 📊 Monitor Cache

```bash
# Open Redis CLI
docker exec -it redis-dental redis-cli

# Monitor real-time commands
MONITOR

# In another terminal, make API requests
# Watch Redis commands appear in MONITOR
```

## 🧹 Clear Cache

```bash
# Connect to Redis
docker exec -it redis-dental redis-cli

# Clear specific cache
DEL appointments::123
DEL patients::456

# Clear all appointment cache
KEYS appointments::* | xargs redis-cli DEL

# Clear ALL cache (careful!)
FLUSHALL
```

## 📝 Cached Entities

| Entity        | Cache Key                | TTL      | Auto-Evict |
| ------------- | ------------------------ | -------- | ---------- |
| Appointment   | `appointments::{id}`     | 3 min    | ✅ Yes     |
| Patient       | `patients::{id}`         | 5 min    | ✅ Yes     |
| Service       | `services::{id}`         | 30 min   | ✅ Yes     |
| JWT Blacklist | `jwt-blacklist::{token}` | 24 hours | ✅ Yes     |

## 🔧 Troubleshooting

### Redis not starting?

```bash
# Check logs
docker logs redis-dental

# Restart container
docker restart redis-dental
```

### Application can't connect?

```yaml
# Check application.yaml
spring:
  data:
    redis:
      host: localhost # Should match docker host
      port: 6379
```

### Clear everything and restart

```bash
# Stop all
docker-compose down

# Remove volumes (⚠️ deletes all data)
docker-compose down -v

# Start fresh
docker-compose up -d
```

## 📚 Documentation

- Full Guide: [REDIS_INTEGRATION_GUIDE.md](./REDIS_INTEGRATION_GUIDE.md)
- Implementation: [REDIS_IMPLEMENTATION_SUMMARY.md](./REDIS_IMPLEMENTATION_SUMMARY.md)

---

**Last Updated:** 2025-12-11
**Redis Version:** 7 Alpine
**Persistence:** AOF (Append Only File) enabled
