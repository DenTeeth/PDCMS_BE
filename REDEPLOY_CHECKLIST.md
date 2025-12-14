# ✅ CHECKLIST DEPLOY LẠI SAU KHI SỬA LỖI ENUM

## 🎯 BẠN CẦN LÀM GÌ TIẾP?

### ✅ ĐÃ XONG TỰ ĐỘNG:

- ✅ Tạo `application-prod.yaml` với `ddl-auto: update`
- ✅ `.env` đã có `SPRING_PROFILES_ACTIVE=prod`
- ✅ `docker-compose.yml` sẽ load profile `prod` tự động
- ✅ Tạo script `redeploy.sh` để deploy nhanh
- ✅ Tạo tài liệu giải thích:
  - `POSTGRESQL_ENUM_FIX.md` (tiếng Anh - kỹ thuật)
  - `ENUM_FIX_EXPLAINED_VI.md` (tiếng Việt - dễ hiểu)

---

## 🚀 CÁC BƯỚC DEPLOY LẠI

### CÁCH 1: DEPLOY TỰ ĐỘNG (KHUYÊN DÙNG) ⭐

**Từ máy local của bạn:**

```bash
# 1. Commit và push code mới lên GitHub
git add .
git commit -m "fix: production config với ddl-auto update để fix ENUM error"
git push origin main

# 2. GitHub Actions sẽ tự động:
#    - Build Docker image
#    - Deploy lên Droplet
#    - Health check
#    - Rollback nếu lỗi
```

**Xem tiến trình:**

- Vào GitHub repository → Actions tab
- Xem workflow "Deploy to DigitalOcean"

---

### CÁCH 2: DEPLOY THỦ CÔNG (NẾU MUỐN)

**SSH vào Droplet và chạy:**

```bash
# Đăng nhập Droplet
ssh root@YOUR_DROPLET_IP

# Di chuyển vào project
cd /root/pdcms-be

# Pull code mới
git pull origin main

# Deploy bằng script tự động
bash redeploy.sh
```

**Script `redeploy.sh` sẽ tự động:**

- ✅ Pull code mới từ GitHub
- ✅ Build Docker images (no cache)
- ✅ Stop containers cũ
- ✅ Start containers mới
- ✅ Wait 30 giây cho app start
- ✅ Health check (10 lần)
- ✅ Verify Spring profile = prod
- ✅ Verify Hibernate ddl-auto = update
- ✅ Check 39 ENUMs exist in PostgreSQL
- ✅ Show logs

---

### CÁCH 3: DEPLOY THỦ CÔNG (TỪNG BƯỚC)

```bash
# 1. SSH vào Droplet
ssh root@YOUR_DROPLET_IP

# 2. Di chuyển vào project
cd /root/pdcms-be

# 3. Pull code mới
git pull origin main

# 4. Stop containers
docker-compose down

# 5. Rebuild images (không dùng cache)
docker-compose build --no-cache

# 6. Start lại
docker-compose up -d

# 7. Xem logs
docker-compose logs -f dental-clinic-app

# 8. Kiểm tra health (tab mới)
curl http://localhost:8080/actuator/health
```

---

## 🔍 KIỂM TRA SAU KHI DEPLOY

### 1. Kiểm tra app đã start chưa:

```bash
curl http://localhost:8080/actuator/health
```

**Kết quả mong đợi:**

```json
{ "status": "UP" }
```

---

### 2. Kiểm tra Spring profile:

```bash
docker-compose logs dental-clinic-app | grep "active profile"
```

**Kết quả mong đợi:**

```
The following profiles are active: prod
```

---

### 3. Kiểm tra Hibernate DDL mode:

```bash
docker-compose logs dental-clinic-app | grep -i "ddl"
```

**Kết quả mong đợi:**

```
ddl-auto: update
```

---

### 4. Kiểm tra ENUMs tồn tại:

```bash
docker-compose exec postgres psql -U root -d dental_clinic_db -c "\dT" | grep enum
```

**Kết quả mong đợi:** List 39 ENUMs

```
 public | account_status               | enum
 public | appointment_action_type      | enum
 public | appointment_status_enum      | enum
 public | attachment_type_enum         | enum
 ... (35 ENUMs nữa)
```

---

### 5. Kiểm tra ENUMs KHÔNG bị xóa sau restart:

```bash
# Restart app
docker-compose restart dental-clinic-app

# Đợi 30 giây
sleep 30

# Kiểm tra lại ENUMs
docker-compose exec postgres psql -U root -d dental_clinic_db -c "SELECT COUNT(*) FROM pg_type WHERE typname LIKE '%enum%' OR typname = 'gender';"

# Kết quả mong đợi: 39 (KHÔNG bị giảm)
```

---

## ❌ XỬ LÝ NẾU VẪN LỖI

### Lỗi: ENUMs vẫn không tồn tại

**Nguyên nhân:** Database container cũ đã có schema với `create-drop`

**Giải pháp:** Reset database hoàn toàn (CHỈ LÀM LẦN ĐẦU)

```bash
# ⚠️ CẢNH BÁO: Lệnh này sẽ XÓA HẾT DỮ LIỆU!

# Stop tất cả containers
docker-compose down

# Xóa volumes (bao gồm database data)
docker volume rm pdcms_be_postgres_data

# Start lại (sẽ tạo database mới)
docker-compose up -d postgres

# Đợi 10 giây cho postgres init
sleep 10

# Start app
docker-compose up -d dental-clinic-app

# Xem logs
docker-compose logs -f dental-clinic-app
```

**Sau bước này:**

- ✅ ENUMs được tạo từ `dental-clinic-seed-data.sql`
- ✅ Hibernate validates schema với `ddl-auto: update`
- ✅ App start thành công
- ✅ Các lần restart sau: ENUMs vẫn tồn tại

---

### Lỗi: App không start (không phải ENUM)

**Kiểm tra logs:**

```bash
docker-compose logs dental-clinic-app | tail -100
```

**Lỗi thường gặp:**

1. **Port 8080 đã được dùng:**

   ```bash
   # Tìm process đang dùng port 8080
   sudo lsof -i :8080

   # Kill process
   sudo kill -9 <PID>
   ```

2. **PostgreSQL chưa sẵn sàng:**

   ```bash
   # Restart postgres trước
   docker-compose restart postgres
   sleep 10
   docker-compose restart dental-clinic-app
   ```

3. **Redis connection failed:**
   ```bash
   # Kiểm tra Redis
   docker-compose exec redis redis-cli -a redis123 PING
   # Kết quả mong đợi: PONG
   ```

---

## 📚 TÀI LIỆU THAM KHẢO

| File                       | Mô tả                        | Ngôn ngữ      |
| -------------------------- | ---------------------------- | ------------- |
| `ENUM_FIX_EXPLAINED_VI.md` | Giải thích chi tiết lỗi ENUM | 🇻🇳 Tiếng Việt |
| `POSTGRESQL_ENUM_FIX.md`   | Technical documentation      | 🇬🇧 English    |
| `READY_TO_DEPLOY.md`       | Hướng dẫn deploy đầy đủ      | 🇻🇳 Tiếng Việt |
| `JUST_3_COMMANDS.md`       | Tóm tắt 3 lệnh nhanh         | 🇻🇳 Tiếng Việt |
| `redeploy.sh`              | Script deploy tự động        | - Bash script |

---

## 🎯 TÓM TẮT

### ✅ Đã sửa gì?

1. **Tạo `application-prod.yaml`:**

   - `ddl-auto: update` thay vì `create-drop`
   - ENUMs không bị xóa khi restart

2. **Cấu hình tự động:**

   - `.env` có `SPRING_PROFILES_ACTIVE=prod`
   - Docker Compose tự động load profile `prod`

3. **Script deploy:**
   - `redeploy.sh` deploy tự động với health checks

### ✅ Bạn cần làm gì?

1. **Commit & Push:**

   ```bash
   git add .
   git commit -m "fix: production config với ddl-auto update"
   git push origin main
   ```

2. **GitHub Actions tự động deploy** HOẶC:

3. **Deploy thủ công:**
   ```bash
   ssh root@YOUR_IP "cd /root/pdcms-be && git pull && bash redeploy.sh"
   ```

---

## 🎉 HOÀN TẤT!

Sau khi deploy xong, app của bạn sẽ:

- ✅ Load profile `prod`
- ✅ Dùng `ddl-auto: update`
- ✅ ENUMs tồn tại mãi mãi
- ✅ KHÔNG bị lỗi `type does not exist` nữa
- ✅ An toàn cho production

**🚀 Ứng dụng sẵn sàng cho production!**
