# Troubleshooting & Fixes

**Last Updated:** 2025-11-25

This folder contains documentation for backend issues, fixes, and configuration guides.

---

## 📁 Available Documentation

### 🔧 Latest Fixes (2025-11-25)

**[BACKEND_FIXES_2025_11_25.md](./BACKEND_FIXES_2025_11_25.md)**
- ✅ **Issue #3 Fixed:** Treatment Plan Duration NULL (column mapping corrected)
- ✅ **Issue #2 Verified:** Patient Creation 500 Error (already handled with try-catch)
- ✅ **Issue #4 Verified:** Item Category seed data complete
- ✅ Email system with 24-hour token expiry
- Build & test results included

**Key Fixes:**
- `service/domain/DentalService.java` - Column mapping: `duration_minutes` → `default_duration_minutes`
- `CustomTreatmentPlanService.java` - Updated getter calls
- `DentalServiceService.java` - Fixed 2 occurrences of getDurationMinutes()

---

### 📧 Configuration Guides

**[EMAIL_CONFIGURATION_GUIDE.md](./EMAIL_CONFIGURATION_GUIDE.md)**
- Gmail SMTP setup with App Password
- Environment variables configuration (.env)
- Email templates (Welcome, Password Reset, Verification)
- Token expiry: 24 hours
- Troubleshooting common SMTP issues

**Features:**
- Welcome email for new patients with password setup link
- Password reset flow
- Email verification for accounts
- Professional branding: "Phòng khám nha khoa DenTeeth"

---

## 🎯 Quick Start

### For Frontend Team

1. **Check Latest Fixes:** Read `BACKEND_FIXES_2025_11_25.md` to see what BE issues have been resolved
2. **Email Integration:** Refer to `EMAIL_CONFIGURATION_GUIDE.md` for email flow details
3. **Testing:** All fixes have been tested and verified - server running on port 8080

### For Backend Team

1. **Apply Fixes:** Review `BACKEND_FIXES_2025_11_25.md` for implementation details
2. **Email Setup:** Follow `EMAIL_CONFIGURATION_GUIDE.md` for local environment setup
3. **Testing:** Run `./mvnw clean compile -DskipTests` to verify compilation

---

## 🔍 What's New (2025-11-25)

✅ **Treatment Plan Duration Issue Resolved**
- NEW treatment plans will have correct `estimated_time_minutes` values
- Calendar appointments show accurate duration
- No more NULL duration in database

✅ **Patient Creation Stable**
- Email failures don't block patient creation
- Graceful error handling with detailed logging
- System remains functional even if SMTP is down

✅ **Email System Enhanced**
- Token expiry extended to 24 hours (was 1 hour)
- Professional email templates without emojis
- Consistent "DenTeeth" branding throughout

---

## 📞 Support

**Issues Found?**
- Create ticket in project management system
- Reference this documentation when reporting
- Include server logs from `server_fixed.log`

**Questions?**
- Check `docs/API_DOCUMENTATION.md` for API details
- Review `docs/api-guides/` for specific module documentation
- Contact backend team with specific file/line references

---

**Developer:** GitHub Copilot  
**Date:** November 25, 2025  
**Status:** ✅ Production Ready  
**Server:** Spring Boot 3.2.10, Java 17
