# ✅ DEPLOYMENT CHECKLIST

Copy checklist này và tick ✅ khi hoàn thành mỗi bước.

---

## 🖥️ PHẦN 1: CHUẨN BỊ DROPLET

- [ ] SSH vào Droplet thành công
- [ ] Docker đã cài đặt (`docker --version`)
- [ ] Docker Compose đã cài đặt (`docker-compose --version`)
- [ ] Tạo thư mục `/root/pdcms-be`
- [ ] Clone repository về `/root/pdcms-be`
- [ ] Checkout branch `main`
- [ ] Tạo file `.env` với đầy đủ biến môi trường:
  - [ ] `SPRING_PROFILES_ACTIVE=prod`
  - [ ] `DB_USERNAME` (ví dụ: `pdcms_user`)
  - [ ] `DB_PASSWORD` (password mạnh)
  - [ ] `DB_DATABASE` (ví dụ: `pdcms_db`)
  - [ ] `REDIS_PASSWORD` (password mạnh)
  - [ ] `JWT_SECRET` (ít nhất 256 bits)
  - [ ] `MAIL_USERNAME` (Gmail)
  - [ ] `MAIL_PASSWORD` (App password)
  - [ ] `FRONTEND_URL` (domain frontend)

---

## 🔐 PHẦN 2: SETUP SSH KEY

- [ ] Tạo SSH key pair (`ssh-keygen`)
- [ ] Copy private key (`cat ~/.ssh/id_rsa`)
- [ ] Lưu private key vào notepad
- [ ] Thêm public key vào authorized_keys (`cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys`)
- [ ] Test SSH từ máy local (nếu có):
  ```bash
  ssh -i path/to/private_key root@YOUR_DROPLET_IP
  ```

---

## 🐙 PHẦN 3: SETUP GITHUB REPOSITORY

- [ ] Vào GitHub repository
- [ ] Vào **Settings** → **Secrets and variables** → **Actions**
- [ ] Thêm secret `DO_SSH_KEY`:
  - [ ] Copy toàn bộ private key (bao gồm `-----BEGIN` và `-----END`)
  - [ ] Paste vào value
- [ ] Thêm secret `DO_HOST`:
  - [ ] IP address của Droplet (ví dụ: `134.209.100.50`)
- [ ] Thêm secret `DO_USERNAME`:
  - [ ] Value: `root`
- [ ] (Tùy chọn) Thêm secret `DISCORD_WEBHOOK`:
  - [ ] Copy webhook URL từ Discord
  - [ ] Paste vào value

---

## 🧪 PHẦN 4: TEST DEPLOYMENT THỦ CÔNG

- [ ] SSH vào Droplet
- [ ] `cd /root/pdcms-be`
- [ ] `docker-compose down`
- [ ] `docker-compose build --no-cache`
- [ ] `docker-compose up -d`
- [ ] Đợi 30-60 giây
- [ ] Kiểm tra containers: `docker-compose ps`
  - [ ] postgres: Up
  - [ ] redis: Up
  - [ ] app: Up
- [ ] Xem logs: `docker-compose logs -f app`
  - [ ] Không có error nghiêm trọng
  - [ ] Thấy "Started Application in X seconds"
- [ ] Test health check:
  ```bash
  curl http://localhost:8080/actuator/health
  ```
  - [ ] Response: `{"status":"UP"}`
- [ ] Test database connection:
  ```bash
  docker-compose exec postgres psql -U pdcms_user -d pdcms_db -c "SELECT 1;"
  ```
  - [ ] Response: `?column? \n ----------\n 1`
- [ ] Test Redis connection:
  ```bash
  docker-compose exec redis redis-cli -a YOUR_REDIS_PASSWORD ping
  ```
  - [ ] Response: `PONG`

---

## 🚀 PHẦN 5: KÍCH HOẠT GITHUB ACTIONS

### Lần đầu tiên:

- [ ] Commit file workflow:
  ```bash
  git add .github/workflows/deploy.yml
  git commit -m "feat: add GitHub Actions deployment workflow"
  git push origin main
  ```
- [ ] Vào GitHub → **Actions** tab
- [ ] Xem workflow đang chạy
- [ ] Đợi workflow hoàn thành

### Kiểm tra workflow:

- [ ] Step "📥 Checkout code" → ✅
- [ ] Step "🔐 Setup SSH" → ✅
- [ ] Step "🧪 Test SSH connection" → ✅
- [ ] Step "💾 Backup current deployment" → ✅
- [ ] Step "🚀 Deploy to Digital Ocean" → ✅
- [ ] Step "🏥 Health Check" → ✅
- [ ] Step "🎉 Discord notification - Deployment succeeded" → ✅ (nếu có Discord)

### Nếu có lỗi:

- [ ] Đọc error message trong workflow logs
- [ ] Check logs trên Droplet: `docker-compose logs -f app`
- [ ] Fix lỗi
- [ ] Push lại để trigger workflow mới

---

## 🔍 PHẦN 6: KIỂM TRA SAU DEPLOYMENT

### Trên Droplet:

- [ ] Containers đang chạy:
  ```bash
  docker-compose ps
  ```
- [ ] Không có container nào Exit/Restart
- [ ] Resource usage OK:
  ```bash
  docker stats
  ```
- [ ] Memory usage < 80%
- [ ] CPU usage ổn định

### API Endpoints:

- [ ] Health check:
  ```bash
  curl http://localhost:8080/actuator/health
  ```
- [ ] API info (nếu có):
  ```bash
  curl http://localhost:8080/actuator/info
  ```

### Database:

- [ ] Login vào database thành công:
  ```bash
  docker-compose exec postgres psql -U pdcms_user -d pdcms_db
  ```
- [ ] List tables: `\dt`
- [ ] Có dữ liệu seed: `SELECT COUNT(*) FROM service_categories;`
- [ ] Exit: `\q`

### Redis:

- [ ] Redis CLI:
  ```bash
  docker-compose exec redis redis-cli -a YOUR_REDIS_PASSWORD
  ```
- [ ] Info: `INFO`
- [ ] Memory usage OK
- [ ] Exit: `exit`

---

## 📱 PHẦN 7: TEST TỪ FRONTEND (Nếu có)

- [ ] Frontend connect được đến API
- [ ] Login thành công
- [ ] Các chức năng hoạt động bình thường:
  - [ ] Booking
  - [ ] Patient management
  - [ ] Service management
  - [ ] Treatment plan
  - [ ] Warehouse

---

## 🔄 PHẦN 8: TEST AUTO DEPLOYMENT

### Push code mới để test:

- [ ] Tạo commit test:
  ```bash
  git commit --allow-empty -m "test: trigger auto deployment"
  git push origin main
  ```
- [ ] Vào GitHub Actions
- [ ] Workflow tự động chạy
- [ ] Deployment thành công
- [ ] Nhận thông báo Discord (nếu có)

### Test rollback (nếu cần):

- [ ] Push code có bug cố ý
- [ ] Workflow chạy
- [ ] Health check failed
- [ ] Rollback tự động thực hiện
- [ ] Service vẫn hoạt động bình thường

---

## 📊 PHẦN 9: MONITORING (Sau deployment)

### Hàng ngày:

- [ ] Check logs:
  ```bash
  docker-compose logs --tail=100 app | grep ERROR
  ```
- [ ] Check resource usage:
  ```bash
  docker stats
  ```
- [ ] Check disk space:
  ```bash
  df -h
  ```

### Hàng tuần:

- [ ] Backup database:
  ```bash
  docker-compose exec postgres pg_dump -U pdcms_user pdcms_db > backup_$(date +%Y%m%d).sql
  ```
- [ ] Clean Docker:
  ```bash
  docker system prune -f
  ```

---

## 🎓 PHẦN 10: DOCUMENTATION

- [ ] Lưu thông tin quan trọng:
  - [ ] IP Droplet
  - [ ] Database credentials
  - [ ] Redis password
  - [ ] JWT secret
  - [ ] Email credentials
- [ ] Lưu SSH private key an toàn (password manager)
- [ ] Share thông tin cần thiết với team
- [ ] Update README.md với thông tin deployment

---

## 🎉 HOÀN TẤT!

Nếu tất cả đã tick ✅, chúc mừng bạn đã deploy thành công!

### Tổng kết những gì đã có:

✅ Backend Spring Boot running on Docker
✅ PostgreSQL 13 + Redis cache
✅ GitHub Actions CI/CD
✅ Auto deployment on push
✅ Backup before deployment
✅ Auto rollback on failure
✅ Health checks
✅ Discord notifications (optional)
✅ Production ready!

---

## 📞 NẾU CÓ VẤN ĐỀ

1. ✅ Check file `QUICK_DEPLOY_GUIDE.md`
2. ✅ Check file `docs/DEPLOY_TO_DIGITALOCEAN_STEP_BY_STEP.md`
3. ✅ Check GitHub Actions logs
4. ✅ Check Docker logs: `docker-compose logs -f`
5. ✅ Check Discord notifications

**Chúc bạn deploy thành công! 🚀**
