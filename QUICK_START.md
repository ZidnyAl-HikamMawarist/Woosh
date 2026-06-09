# WOOSH - Quick Start Guide

Panduan cepat untuk memulai implementasi integrasi mobile ↔ admin dashboard.

---

## 🚀 5-MINUTE SETUP

### 1. Clone & Setup
```bash
# Clone repository
git clone <repo-url>
cd Woosh

# Setup mobile
cd app
# Open in Android Studio

# Setup admin
cd ../admin-woosh
composer install
npm install
cp .env.example .env
php artisan key:generate
php artisan migrate
php artisan serve
```

### 2. Firebase Setup
1. Go to https://console.firebase.google.com
2. Create new project
3. Enable Firestore Database
4. Enable Firebase Authentication
5. Download service account JSON
6. Place in `admin-woosh/storage/app/firebase-auth.json`

### 3. Environment Configuration
**File**: `admin-woosh/.env`
```
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CREDENTIALS=storage/app/firebase-auth.json
```

---

## 📱 MOBILE QUICK START

### 1. Configure Server IP
1. Open app
2. Go to Settings → Development → Konfigurasi Server
3. Select or add your Laravel server IP
4. Default: `127.0.0.1` (for ADB port forwarding)

### 2. Test Register Flow
1. Go to Register screen
2. Enter: name, email, password
3. Click Register
4. Check admin dashboard → Users (should appear)

### 3. Test Book Ticket Flow
1. Go to Home → Search Trains
2. Select train and seats
3. Complete payment
4. Check admin dashboard → Tickets (should appear)

---

## 🖥️ ADMIN QUICK START

### 1. Access Dashboard
1. Open browser: `http://localhost:8000`
2. Login with admin credentials
3. Go to Dashboard

### 2. Add Train Schedule
1. Go to Trips
2. Click "Add Trip"
3. Fill: Train Name, Departure Time, Arrival Time, Class, Price
4. Click Save
5. Check mobile app (should appear in search)

### 3. View Bookings
1. Go to Tickets
2. See all bookings from mobile
3. Click ticket to see details
4. Can validate or refund

---

## 🔧 COMMON TASKS

### Task 1: Fix User Not Appearing
```bash
# 1. Check database
mysql> SELECT * FROM users;

# 2. Check API
curl http://localhost:8000/api/v1/sync-user?firebase_uid=test&name=Test&email=test@example.com&phone=08123456789

# 3. Check logs
tail -f admin-woosh/storage/logs/laravel.log
```

### Task 2: Fix Ticket Not Appearing
```bash
# 1. Check database
mysql> SELECT * FROM tickets;

# 2. Check Firestore
# Firebase Console → Firestore → users/{uid}/tickets

# 3. Check mobile logs
adb logcat | grep "book"
```

### Task 3: Fix Refund Not Working
```bash
# 1. Check ticket status
mysql> SELECT status FROM tickets WHERE ticket_code = 'WSH-TK-xxxxx';

# 2. Check API response
# Look at mobile Logcat for refund API response

# 3. Check Firestore
# Firebase Console → Firestore → users/{uid}/tickets/{ticket_id}
```

---

## 📊 VERIFICATION CHECKLIST

### ✅ User Registration
- [ ] User register di mobile
- [ ] User muncul di admin dashboard
- [ ] User data tersimpan di MySQL
- [ ] User data tersimpan di Firestore

### ✅ Train Schedule
- [ ] Admin tambah jadwal
- [ ] Jadwal muncul di mobile search
- [ ] Jadwal tersimpan di MySQL
- [ ] Jadwal tersimpan di Firestore

### ✅ Ticket Booking
- [ ] User book tiket di mobile
- [ ] Tiket muncul di admin dashboard
- [ ] Tiket tersimpan di MySQL
- [ ] Tiket tersimpan di Firestore
- [ ] Email konfirmasi dikirim

### ✅ Refund
- [ ] User refund tiket
- [ ] Status berubah menjadi "Batal"
- [ ] Admin dashboard menampilkan status "Batal"
- [ ] Notifikasi dikirim ke user

### ✅ Notifications
- [ ] Admin kirim broadcast
- [ ] Notifikasi muncul di mobile
- [ ] Notifikasi tersimpan di Firestore

---

## 🐛 QUICK TROUBLESHOOTING

| Problem | Quick Fix |
|---------|-----------|
| User tidak muncul | Check API `/api/v1/sync-user` di Postman |
| Tiket tidak muncul | Check MySQL: `SELECT * FROM tickets;` |
| Refund tidak bekerja | Check mobile logs: `adb logcat \| grep refund` |
| Notifikasi tidak muncul | Check Firestore: `users/{uid}/notifications` |
| Server 404 | Run: `php artisan route:clear` |
| Cleartext error | Already fixed in network_security_config.xml |

---

## 📚 DOCUMENTATION REFERENCE

| Document | Purpose |
|----------|---------|
| README.md | Project overview & features |
| ADMIN.md | Admin specifications |
| INTEGRATION_CHECKLIST.md | Integration progress tracker |
| FIXES_AND_SOLUTIONS.md | Detailed fix guide |
| IMPLEMENTATION_GUIDE.md | Step-by-step implementation |
| TROUBLESHOOTING.md | Troubleshooting reference |
| QUICK_START.md | This file |

---

## 🎯 NEXT STEPS

### Immediate (Today)
1. [ ] Setup Firebase project
2. [ ] Configure environment variables
3. [ ] Run database migrations
4. [ ] Test API endpoints with Postman

### Short Term (This Week)
1. [ ] Implement API endpoints
2. [ ] Update mobile ViewModels
3. [ ] Test user registration flow
4. [ ] Test ticket booking flow

### Medium Term (This Month)
1. [ ] Implement admin dashboard
2. [ ] Setup notification system
3. [ ] Complete end-to-end testing
4. [ ] Deploy to staging

### Long Term (Next Month)
1. [ ] Performance optimization
2. [ ] Security audit
3. [ ] Deploy to production
4. [ ] Monitor and maintain

---

## 💬 GETTING HELP

### Documentation
- Read TROUBLESHOOTING.md for common issues
- Read IMPLEMENTATION_GUIDE.md for detailed steps
- Read FIXES_AND_SOLUTIONS.md for specific problems

### Debugging
- Check Laravel logs: `tail -f storage/logs/laravel.log`
- Check mobile logs: `adb logcat | grep woosh`
- Check Firebase Console for errors
- Use Postman to test APIs

### Support
- Firebase Support: https://firebase.google.com/support
- Laravel Support: https://laravel.com/docs
- Android Support: https://developer.android.com/support

---

## 📞 QUICK REFERENCE

### Important URLs
- Laravel Admin: `http://localhost:8000`
- Firebase Console: `https://console.firebase.google.com`
- Postman: `https://www.postman.com`

### Important Files
- Mobile API: `app/src/main/java/com/example/woosh/data/remote/ApiService.kt`
- Admin API: `admin-woosh/app/Http/Controllers/Api/ApiController.php`
- Database: `admin-woosh/database/migrations/`
- Firebase Config: `admin-woosh/config/firebase.php`

### Important Commands
```bash
# Laravel
php artisan serve
php artisan migrate
php artisan cache:clear
php artisan route:clear

# Mobile
./gradlew build
./gradlew assembleDebug
adb logcat

# Firebase
firebase deploy
firebase emulators:start
```

---

## ✨ TIPS & TRICKS

### 1. Use ADB Port Forwarding
```bash
adb reverse tcp:8000 tcp:8000
# Then use 127.0.0.1 in mobile settings
```

### 2. Monitor Real-time Changes
```bash
# Terminal 1: Watch Laravel logs
tail -f admin-woosh/storage/logs/laravel.log

# Terminal 2: Watch mobile logs
adb logcat | grep woosh

# Terminal 3: Watch database
watch -n 1 'mysql -u root -p -e "SELECT * FROM users;"'
```

### 3. Test API Quickly
```bash
# Use curl
curl -X POST http://localhost:8000/api/v1/sync-user \
  -d "firebase_uid=test&name=Test&email=test@example.com&phone=08123456789"

# Or use Postman
# Import collection from documentation
```

### 4. Clear Everything & Start Fresh
```bash
# Mobile
adb shell pm clear com.example.woosh

# Admin
php artisan migrate:refresh --seed
php artisan cache:clear
php artisan route:clear

# Firebase
# Delete all data from Firestore Console
```

---

**Last Updated**: 2026-05-26
**Status**: Ready to Use
**Questions?**: Check TROUBLESHOOTING.md or IMPLEMENTATION_GUIDE.md
