# WOOSH Integration Checklist - Mobile ↔ Admin Dashboard

Dokumen ini berisi checklist lengkap untuk memastikan semua fitur mobile dan admin dashboard terintegrasi dengan sempurna tanpa bug.

---

## 🔍 MASALAH YANG SUDAH DIIDENTIFIKASI

### 1. ❌ Server Configuration Subtitle Tidak Update
**Masalah**: Saat user memilih IP server di Settings, subtitle tidak ter-update sampai app di-restart.
**Penyebab**: State `serverUrl` tidak ter-update ketika dialog ditutup.
**Status**: ✅ FIXED - Menggunakan `LaunchedEffect` untuk update state saat dialog ditutup.

### 2. ❌ User Tidak Muncul di Admin Dashboard
**Masalah**: User yang register di mobile tidak muncul di daftar user admin.
**Penyebab**: API sync user tidak dipanggil saat register/login.
**Solusi**: Pastikan `syncUserToMySQL()` dipanggil di `RegisteredUserController` dan `AuthenticatedSessionController`.

### 3. ❌ Tiket Tidak Muncul di Admin Dashboard
**Masalah**: Tiket yang dipesan di mobile tidak muncul di transaksi admin.
**Penyebab**: API `bookTicket` tidak menyimpan ke MySQL, hanya ke Firestore.
**Solusi**: Pastikan `bookTicket` API menyimpan ke MySQL setelah pembayaran berhasil.

### 4. ❌ Refund Tidak Berfungsi
**Masalah**: Saat user refund tiket, status di mobile tidak berubah menjadi "Batal".
**Penyebab**: TicketViewModel tidak memanggil refund API, hanya update Firestore lokal.
**Solusi**: Pastikan `refundTicket()` di TicketViewModel memanggil API Laravel.

### 5. ❌ Sync Firebase Route 404
**Masalah**: Tombol "Sync Firebase" di admin menampilkan error 404.
**Penyebab**: Route `/admin/sync-firebase` tidak terdaftar atau cache route belum di-clear.
**Solusi**: Clear route cache dan pastikan route terdaftar di `routes/web.php`.

### 6. ❌ Broadcast Notification Tidak Muncul di Mobile
**Masalah**: Notifikasi broadcast dari admin tidak muncul di mobile.
**Penyebab**: NotificationController tidak menulis ke Firestore setiap user.
**Solusi**: Pastikan broadcast notification menulis ke `users/{uid}/notifications` untuk setiap user.

### 7. ❌ Cleartext Traffic Error
**Masalah**: "cleartext communication to 10.30.203.131 not permitted by network security policy"
**Penyebab**: Network security config tidak mengizinkan cleartext untuk IP tertentu.
**Solusi**: Sudah di-fix dengan `cleartextTrafficPermitted="true"` di `network_security_config.xml`.

---

## ✅ CHECKLIST INTEGRASI MOBILE → ADMIN

### A. User Management
- [ ] **Register Flow**
  - [ ] User register di mobile
  - [ ] Firebase Auth membuat akun
  - [ ] API `/api/v1/sync-user` dipanggil
  - [ ] User tersimpan di MySQL `users` table
  - [ ] User muncul di admin dashboard
  - [ ] Firestore `users/{uid}` dibuat dengan data awal

- [ ] **Login Flow**
  - [ ] User login di mobile
  - [ ] Firebase Auth verifikasi
  - [ ] API `/api/v1/sync-user` dipanggil (update data)
  - [ ] User data ter-update di MySQL
  - [ ] Firestore `users/{uid}` ter-update

- [ ] **Profile Update**
  - [ ] User edit profil di mobile
  - [ ] API `/api/v1/update-profile` dipanggil
  - [ ] Data ter-update di MySQL
  - [ ] Firestore `users/{uid}` ter-update
  - [ ] Admin bisa lihat perubahan di user management

### B. Train Schedule Management
- [ ] **View Trips**
  - [ ] Admin tambah jadwal kereta di dashboard
  - [ ] Data tersimpan di MySQL `trips` table
  - [ ] FirebaseSyncService.syncTrip() dipanggil
  - [ ] Data tersinkron ke Firestore `trips/{trip_id}`
  - [ ] Mobile search trains → API `/api/v1/trips` menampilkan data
  - [ ] Jadwal muncul di TrainListScreen

- [ ] **Edit Trip**
  - [ ] Admin edit jadwal di dashboard
  - [ ] Data ter-update di MySQL
  - [ ] FirebaseSyncService.syncTrip() dipanggil
  - [ ] Firestore ter-update
  - [ ] Mobile refresh → jadwal ter-update

- [ ] **Delete Trip**
  - [ ] Admin hapus jadwal
  - [ ] Data dihapus dari MySQL
  - [ ] FirebaseSyncService.deleteTrip() dipanggil
  - [ ] Firestore ter-delete
  - [ ] Mobile tidak bisa lagi memilih jadwal tersebut

### C. Ticket Booking & Management
- [ ] **Book Ticket Flow**
  - [ ] User pilih jadwal & kursi di mobile
  - [ ] User lakukan pembayaran
  - [ ] API `/api/v1/book-ticket` dipanggil
  - [ ] Tiket tersimpan di MySQL `tickets` table
  - [ ] Firestore `users/{uid}/tickets/{ticket_id}` dibuat
  - [ ] Email konfirmasi dikirim ke user
  - [ ] Tiket muncul di admin dashboard → Transaksi Tiket
  - [ ] Status tiket = "Active"

- [ ] **View Ticket Details**
  - [ ] Admin klik tiket di dashboard
  - [ ] Detail lengkap muncul (penumpang, kursi, harga, status)
  - [ ] Data sesuai dengan yang ada di mobile

- [ ] **Validate Ticket (Admin)**
  - [ ] Admin klik "Validasi" di dashboard
  - [ ] Status tiket berubah menjadi "Used"
  - [ ] API `/api/v1/validate-ticket` dipanggil
  - [ ] MySQL ter-update
  - [ ] Firestore ter-update
  - [ ] Mobile menampilkan status "Selesai"

- [ ] **Refund Ticket (Mobile)**
  - [ ] User klik "Refund" di mobile
  - [ ] API `/api/v1/refund-ticket` dipanggil
  - [ ] MySQL status berubah menjadi "Batal"
  - [ ] Firestore ter-update
  - [ ] Admin dashboard menampilkan status "Batal"
  - [ ] Notifikasi refund dikirim ke user

- [ ] **Refund Ticket (Admin)**
  - [ ] Admin klik "Refund" di dashboard
  - [ ] Status tiket berubah menjadi "Batal"
  - [ ] API dipanggil untuk update MySQL & Firestore
  - [ ] Notifikasi dikirim ke user di mobile
  - [ ] Mobile menampilkan status "Batal"

### D. Notifications
- [ ] **Broadcast Notification**
  - [ ] Admin kirim broadcast di dashboard
  - [ ] Notifikasi ditulis ke Firestore `users/{uid}/notifications` untuk SETIAP user
  - [ ] Mobile menerima notifikasi real-time
  - [ ] Notifikasi muncul di NotificationScreen

- [ ] **Per-User Notification**
  - [ ] Admin refund tiket user tertentu
  - [ ] Notifikasi ditulis ke Firestore `users/{uid}/notifications`
  - [ ] Mobile menerima notifikasi
  - [ ] Notifikasi muncul di NotificationScreen

### E. Loyalty Points
- [ ] **Points Increment**
  - [ ] User book tiket
  - [ ] Poin otomatis ditambah (misal: 10% dari harga tiket)
  - [ ] MySQL `users.loyalty_points` ter-update
  - [ ] Firestore `users/{uid}.loyaltyPoints` ter-update
  - [ ] Mobile menampilkan poin terbaru

- [ ] **Points Adjustment (Admin)**
  - [ ] Admin adjust poin user di dashboard
  - [ ] MySQL ter-update
  - [ ] Firestore ter-update
  - [ ] Mobile menampilkan poin terbaru

### F. Seat Management
- [ ] **Seat Booking**
  - [ ] User pilih kursi di mobile
  - [ ] Kursi ditandai sebagai "booked" di Firestore
  - [ ] Admin dashboard menampilkan kursi yang sudah dipesan
  - [ ] User lain tidak bisa memilih kursi yang sama

- [ ] **Seat Reset (Admin)**
  - [ ] Admin reset kursi jika ada error
  - [ ] Firestore `trips/{trip_id}.bookedSeats` di-clear
  - [ ] Mobile refresh → kursi tersedia lagi

---

## 🔧 TECHNICAL REQUIREMENTS

### Mobile (Android/Kotlin)
1. **RetrofitClient Configuration**
   - ✅ Dynamic host interceptor untuk switch IP
   - ✅ Network security config mengizinkan cleartext
   - ✅ Settings screen untuk konfigurasi server

2. **API Calls**
   - [ ] `POST /api/v1/sync-user` - Sync user saat register/login
   - [ ] `POST /api/v1/update-profile` - Update profil user
   - [ ] `GET /api/v1/trips` - Fetch jadwal kereta
   - [ ] `POST /api/v1/book-ticket` - Book tiket setelah pembayaran
   - [ ] `POST /api/v1/refund-ticket` - Refund tiket
   - [ ] `POST /api/v1/validate-ticket` - Validate tiket (admin only)
   - [ ] `GET /api/v1/user-tickets` - Fetch tiket user

3. **Firebase Integration**
   - ✅ Firebase Auth untuk authentication
   - ✅ Firestore untuk real-time data
   - ✅ FCM untuk push notifications

### Admin Dashboard (Laravel)
1. **Routes**
   - [ ] `GET /admin/users` - User management
   - [ ] `GET /admin/trips` - Trip management
   - [ ] `GET /admin/tickets` - Ticket management
   - [ ] `POST /admin/sync-firebase` - Sync Firebase to MySQL
   - [ ] `POST /admin/notifications/broadcast` - Send broadcast
   - [ ] `POST /admin/tickets/{id}/validate` - Validate ticket
   - [ ] `POST /admin/tickets/{id}/refund` - Refund ticket

2. **Controllers**
   - [ ] `AdminController` - Dashboard & user management
   - [ ] `FirebaseController` - Firebase sync operations
   - [ ] `NotificationController` - Send notifications
   - [ ] `ReportController` - Generate reports
   - [ ] `TicketController` - Ticket management

3. **Services**
   - ✅ `FirebaseSyncService` - Sync MySQL ↔ Firestore
   - [ ] `PaymentService` - Process payments
   - [ ] `EmailService` - Send emails

4. **Database**
   - [ ] `users` table - User data
   - [ ] `trips` table - Train schedules
   - [ ] `tickets` table - Bookings
   - [ ] `activity_logs` table - Audit trail
   - [ ] `refund_requests` table - Refund tracking

---

## 🚀 DEPLOYMENT CHECKLIST

### Before Going Live
- [ ] All API endpoints tested with Postman
- [ ] Firebase Firestore security rules configured
- [ ] MySQL database backed up
- [ ] Email service configured (SMTP)
- [ ] Payment gateway integrated
- [ ] Admin credentials secured
- [ ] Mobile app signed & released to Play Store
- [ ] Admin dashboard deployed to production server

### Post-Deployment
- [ ] Monitor error logs
- [ ] Test end-to-end flows
- [ ] Verify data sync between mobile & admin
- [ ] Check notification delivery
- [ ] Monitor server performance

---

## 📝 NOTES

### Data Flow Diagram
```
Mobile App (Android)
    ↓
Firebase Auth (Authentication)
    ↓
Firestore (Real-time Data)
    ↓
Laravel API (/api/v1/*)
    ↓
MySQL Database
    ↓
Admin Dashboard (Laravel)
    ↓
FirebaseSyncService (Sync back to Firestore)
    ↓
Mobile App (Real-time Updates)
```

### Key Points
1. **Always sync to MySQL** - Every write to Firestore should also write to MySQL
2. **Always sync to Firestore** - Every write to MySQL should also write to Firestore
3. **Use transactions** - Ensure data consistency across both databases
4. **Log everything** - Keep audit trail of all operations
5. **Test thoroughly** - Test all flows before deployment

---

**Last Updated**: 2026-05-26
**Status**: In Progress
