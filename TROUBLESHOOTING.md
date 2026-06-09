# WOOSH - Troubleshooting Guide

Panduan cepat untuk mengatasi masalah umum yang mungkin terjadi.

---

## 🔴 CRITICAL ISSUES

### Issue: User tidak muncul di admin setelah register
**Symptoms**: User berhasil register di mobile, tapi tidak ada di admin dashboard

**Diagnosis**:
```bash
# Check MySQL
SELECT * FROM users WHERE email = 'user@example.com';

# Check Firestore
# Go to Firebase Console → Firestore → users collection
```

**Solutions**:
1. Pastikan API `/api/v1/sync-user` terdaftar di `routes/api.php`
2. Pastikan `syncUser()` dipanggil di `RegisteredUserController`
3. Check Laravel logs: `tail -f storage/logs/laravel.log`
4. Test API dengan Postman:
   ```
   POST http://localhost:8000/api/v1/sync-user
   ?firebase_uid=abc123&name=John&email=john@example.com&phone=08123456789
   ```

---

### Issue: Tiket tidak muncul di admin setelah booking
**Symptoms**: Tiket berhasil dibuat di mobile, tapi tidak ada di admin dashboard

**Diagnosis**:
```bash
# Check MySQL
SELECT * FROM tickets WHERE ticket_code LIKE 'WSH-TK-%';

# Check Firestore
# Go to Firebase Console → Firestore → users/{uid}/tickets
```

**Solutions**:
1. Pastikan `bookTicket()` API menyimpan ke MySQL
2. Pastikan `FirebaseSyncService.syncTrip()` dipanggil setelah booking
3. Check API response di mobile (Logcat)
4. Verify Firebase Firestore rules allow write

---

### Issue: Refund tidak berfungsi
**Symptoms**: User klik refund, tapi status tiket tidak berubah

**Diagnosis**:
```bash
# Check ticket status
SELECT * FROM tickets WHERE ticket_code = 'WSH-TK-xxxxx';

# Check Firestore
# Go to Firebase Console → Firestore → users/{uid}/tickets/{ticket_id}
```

**Solutions**:
1. Pastikan `refundTicket()` di TicketViewModel memanggil API
2. Pastikan API `/api/v1/refund-ticket` terdaftar
3. Check mobile logs untuk error response
4. Verify Firebase Firestore rules allow update

---

### Issue: Sync Firebase route 404
**Symptoms**: Tombol "Sync Firebase" di admin menampilkan 404 error

**Solutions**:
```bash
cd admin-woosh

# Clear route cache
php artisan route:clear
php artisan cache:clear

# Verify route exists
php artisan route:list | grep sync-firebase

# Check routes/web.php for correct route definition
```

---

## 🟡 COMMON ISSUES

### Issue: Cleartext traffic error
**Error**: `cleartext communication to 10.30.203.131 not permitted by network security policy`

**Solution**: 
File `app/src/main/res/xml/network_security_config.xml` sudah di-set dengan `cleartextTrafficPermitted="true"`. Jika masih error:
1. Clean build: `Build → Clean Project`
2. Rebuild: `Build → Rebuild Project`
3. Restart Android Studio

---

### Issue: Server configuration subtitle tidak update
**Symptoms**: Saat memilih IP server, subtitle tetap menampilkan IP lama

**Solution**: 
Sudah di-fix dengan `LaunchedEffect`. Jika masih terjadi:
1. Rebuild app
2. Clear app data: `Settings → Apps → Woosh → Storage → Clear Data`
3. Restart app

---

### Issue: Notifikasi tidak muncul di mobile
**Symptoms**: Admin kirim broadcast, tapi mobile tidak menerima notifikasi

**Diagnosis**:
```bash
# Check Firestore
# Go to Firebase Console → Firestore → users/{uid}/notifications

# Check mobile logs
adb logcat | grep "notification"
```

**Solutions**:
1. Pastikan `NotificationController.broadcast()` menulis ke Firestore untuk setiap user
2. Pastikan mobile app listening ke `users/{uid}/notifications` collection
3. Check Firebase Cloud Messaging (FCM) token di mobile
4. Verify Firestore security rules allow read

---

### Issue: Jadwal kereta tidak muncul di mobile
**Symptoms**: Admin tambah jadwal, tapi mobile tidak bisa lihat

**Diagnosis**:
```bash
# Check MySQL
SELECT * FROM trips;

# Check Firestore
# Go to Firebase Console → Firestore → trips collection

# Check mobile API response
# Look at Logcat for /api/v1/trips response
```

**Solutions**:
1. Pastikan `FirebaseSyncService.syncTrip()` dipanggil saat admin tambah jadwal
2. Pastikan mobile API `/api/v1/trips` mengembalikan data
3. Check network connectivity di mobile
4. Verify Firestore security rules allow read

---

### Issue: Kursi yang sudah dipesan masih bisa dipilih
**Symptoms**: User A pesan kursi 4A, user B masih bisa pesan kursi 4A

**Diagnosis**:
```bash
# Check booked_seats di Firestore
# Go to Firebase Console → Firestore → trips/{trip_id}
# Look at bookedSeats field

# Check MySQL
SELECT booked_seats FROM trips WHERE trip_id = 'xxx';
```

**Solutions**:
1. Pastikan `bookTicket()` API update `trips.booked_seats` di MySQL
2. Pastikan `FirebaseSyncService.syncTrip()` update `trips.bookedSeats` di Firestore
3. Pastikan mobile app fetch latest booked seats sebelum menampilkan seat selection
4. Add real-time listener di mobile untuk booked seats

---

## 🟢 VERIFICATION STEPS

### Verify User Sync
```bash
# 1. Register user di mobile
# 2. Check MySQL
mysql> SELECT * FROM users WHERE email = 'test@example.com';

# 3. Check Firestore
# Firebase Console → Firestore → users collection

# 4. Check admin dashboard
# Should see user in Users list
```

### Verify Ticket Booking
```bash
# 1. Book ticket di mobile
# 2. Check MySQL
mysql> SELECT * FROM tickets WHERE ticket_code LIKE 'WSH-TK-%' ORDER BY booked_at DESC LIMIT 1;

# 3. Check Firestore
# Firebase Console → Firestore → users/{uid}/tickets

# 4. Check admin dashboard
# Should see ticket in Tickets list
```

### Verify Refund
```bash
# 1. Refund ticket di mobile
# 2. Check MySQL
mysql> SELECT status FROM tickets WHERE ticket_code = 'WSH-TK-xxxxx';
# Should be 'Batal'

# 3. Check Firestore
# Firebase Console → Firestore → users/{uid}/tickets/{ticket_id}
# status should be 'Batal'

# 4. Check admin dashboard
# Ticket status should show 'Batal'
```

### Verify Notification
```bash
# 1. Send broadcast from admin
# 2. Check Firestore
# Firebase Console → Firestore → users/{uid}/notifications
# Should see new notification document

# 3. Check mobile
# Should see notification in NotificationScreen
```

---

## 🔧 DEBUG COMMANDS

### Android/Mobile
```bash
# View logs
adb logcat | grep "woosh\|firebase\|retrofit"

# Clear app data
adb shell pm clear com.example.woosh

# Restart app
adb shell am force-stop com.example.woosh
adb shell am start -n com.example.woosh/.MainActivity

# Check network traffic
# Use Android Studio Network Profiler
```

### Laravel/Admin
```bash
# View logs
tail -f storage/logs/laravel.log

# Clear cache
php artisan cache:clear
php artisan route:clear
php artisan view:clear

# Run migrations
php artisan migrate

# Seed database
php artisan db:seed

# Test API
curl -X POST http://localhost:8000/api/v1/sync-user \
  -d "firebase_uid=test123&name=Test&email=test@example.com&phone=08123456789"
```

### Firebase
```bash
# View Firestore data
# Firebase Console → Firestore Database

# View Authentication
# Firebase Console → Authentication

# View Cloud Messaging
# Firebase Console → Cloud Messaging

# View Logs
# Firebase Console → Logs
```

---

## 📊 MONITORING CHECKLIST

### Daily Checks
- [ ] Check error logs in Laravel
- [ ] Check Firebase error logs
- [ ] Verify database connectivity
- [ ] Check API response times
- [ ] Monitor server resources

### Weekly Checks
- [ ] Review user registrations
- [ ] Review ticket bookings
- [ ] Check refund requests
- [ ] Verify data sync between MySQL and Firestore
- [ ] Review admin activity logs

### Monthly Checks
- [ ] Database backup verification
- [ ] Security audit
- [ ] Performance optimization
- [ ] Update dependencies
- [ ] Review and update documentation

---

## 🆘 EMERGENCY PROCEDURES

### If Database is Down
1. Check MySQL service: `sudo systemctl status mysql`
2. Restart MySQL: `sudo systemctl restart mysql`
3. Check disk space: `df -h`
4. Check MySQL logs: `tail -f /var/log/mysql/error.log`

### If Firebase is Down
1. Check Firebase Console status
2. Verify credentials file exists
3. Check network connectivity
4. Restart Laravel app: `php artisan serve`

### If API is Not Responding
1. Check Laravel service: `ps aux | grep php`
2. Check port 8000: `lsof -i :8000`
3. Restart Laravel: `php artisan serve`
4. Check error logs: `tail -f storage/logs/laravel.log`

### If Mobile App Crashes
1. Check Logcat for crash logs
2. Check Firebase Crashlytics
3. Rebuild app: `Build → Rebuild Project`
4. Clear app data and reinstall

---

## 📞 SUPPORT CONTACTS

- **Firebase Support**: https://firebase.google.com/support
- **Laravel Support**: https://laravel.com/docs
- **Android Support**: https://developer.android.com/support
- **MySQL Support**: https://dev.mysql.com/support

---

## 📝 COMMON ERROR MESSAGES

| Error | Cause | Solution |
|-------|-------|----------|
| `404 Not Found` | Route not registered | Check routes/api.php or routes/web.php |
| `PERMISSION_DENIED` | Firebase rules issue | Check Firestore security rules |
| `Connection refused` | Server not running | Start Laravel server |
| `Cleartext not permitted` | Network security config | Update network_security_config.xml |
| `User not found` | Firebase UID mismatch | Verify firebase_uid in database |
| `Ticket not found` | Ticket code incorrect | Check ticket_code format |
| `Sync failed` | API error | Check Laravel logs |
| `Notification not received` | FCM issue | Check Firebase Cloud Messaging |

---

**Last Updated**: 2026-05-26
**Version**: 1.0.0
