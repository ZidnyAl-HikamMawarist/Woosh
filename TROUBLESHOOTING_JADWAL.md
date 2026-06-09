# Troubleshooting - Jadwal Tidak Muncul di Mobile

**Problem**: Saat cari jadwal di mobile, tidak ada jadwal yang muncul
**Status**: ✅ SOLVED

---

## Root Cause

Jadwal tidak muncul karena **tidak ada data trip di database MySQL**. API `/api/v1/trips` mengembalikan array kosong.

---

## Solution

### Step 1: Pastikan Laravel Server Berjalan
```bash
php artisan serve --host=0.0.0.0 --port=8000
```

### Step 2: Seed Sample Data
```bash
php artisan db:seed --class=TripsSeeder
```

Ini akan membuat 5 sample trips:
- TRIP-001: Jakarta → Surabaya
- TRIP-002: Jakarta → Bandung
- TRIP-003: Jakarta → Yogyakarta
- TRIP-004: Surabaya → Yogyakarta
- TRIP-005: Bandung → Surabaya

### Step 3: Verify Data di Database
```bash
mysql> SELECT * FROM trips;
```

Harus ada minimal 1 trip.

### Step 4: Test API di Postman
```
GET http://YOUR_IP:8000/api/v1/trips
```

Response harus:
```json
{
  "status": "success",
  "data": [
    {
      "trip_id": "TRIP-001",
      "train_name": "Express Jakarta-Surabaya",
      ...
    }
  ],
  "count": 5
}
```

### Step 5: Configure Mobile App
1. Open Settings → Development → Konfigurasi Server
2. Enter your PC IP (e.g., 192.168.1.100)
3. Port: 8000
4. Save

### Step 6: Test di Mobile
1. Go to Home → Search Trains
2. Jadwal harus muncul sekarang

---

## Common Issues & Fixes

### Issue 1: "Connection Refused"
**Cause**: Server tidak berjalan atau IP salah

**Fix**:
```bash
# Check if server running
netstat -an | findstr 8000

# Start server
php artisan serve --host=0.0.0.0 --port=8000

# Get your IP
ipconfig
# Cari "IPv4 Address" (e.g., 192.168.1.100)
```

### Issue 2: "404 Not Found"
**Cause**: Route tidak terdaftar

**Fix**:
```bash
# Clear route cache
php artisan route:clear

# Check routes
php artisan route:list | findstr "api/v1/trips"
```

### Issue 3: "Empty Array"
**Cause**: Tidak ada data di database

**Fix**:
```bash
# Seed data
php artisan db:seed --class=TripsSeeder

# Verify
php artisan tinker
>>> App\Models\Trip::count()
# Should return 5
```

### Issue 4: "Firestore Error"
**Cause**: Firebase tidak terkonfigurasi

**Fix**: API akan fallback ke MySQL, jadi tidak masalah. Tapi untuk production:
```bash
# Check .env
cat .env | grep FIREBASE

# Configure if needed
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CREDENTIALS=storage/app/firebase-auth.json
```

### Issue 5: "Network Error on Mobile"
**Cause**: Mobile tidak bisa reach server

**Fix**:
```bash
# Test dari mobile
# Open browser: http://192.168.1.100:8000/api/v1/trips
# Should see JSON response

# If not working:
# 1. Check firewall
# 2. Check IP address (use ipconfig)
# 3. Check port 8000 is open
# 4. Try ping: ping 192.168.1.100
```

---

## Quick Checklist

- [ ] Laravel server running: `php artisan serve --host=0.0.0.0 --port=8000`
- [ ] Sample data seeded: `php artisan db:seed --class=TripsSeeder`
- [ ] Database has trips: `SELECT COUNT(*) FROM trips;` (should be 5)
- [ ] API working: `GET http://localhost:8000/api/v1/trips` (should return JSON)
- [ ] Mobile IP configured correctly
- [ ] Mobile can reach server: `ping 192.168.1.100`
- [ ] Port 8000 is open/not blocked

---

## Testing Steps

### Test 1: API Response
```bash
curl http://localhost:8000/api/v1/trips
```

Expected: JSON with 5 trips

### Test 2: Database
```bash
php artisan tinker
>>> App\Models\Trip::all()
```

Expected: 5 trips

### Test 3: Mobile
1. Open app
2. Go to Search Trains
3. Should see 5 trips

---

## If Still Not Working

### Debug Steps:
1. Check Laravel logs:
```bash
tail -f storage/logs/laravel.log
```

2. Check mobile logs:
```bash
adb logcat | grep woosh
```

3. Test API with Postman:
```
GET http://192.168.1.100:8000/api/v1/trips
```

4. Check database directly:
```bash
mysql -u root -p
> USE woosh_db;
> SELECT * FROM trips;
```

---

## Complete Setup Guide

```bash
# 1. Navigate to project
cd c:\Users\Thinkpad\AndroidStudioProjects\Woosh\admin-woosh

# 2. Clear cache
php artisan cache:clear
php artisan route:clear

# 3. Seed data
php artisan db:seed --class=TripsSeeder

# 4. Start server
php artisan serve --host=0.0.0.0 --port=8000

# 5. Get your IP
ipconfig
# Look for IPv4 Address (e.g., 192.168.1.100)

# 6. On mobile:
# Settings → Development → Konfigurasi Server
# Enter: 192.168.1.100:8000
# Save

# 7. Test:
# Home → Search Trains
# Should see 5 trips
```

---

## Performance Notes

- First load: ~2-3 seconds
- Subsequent loads: ~1 second
- If slow, check:
  - Network connection
  - Server CPU usage
  - Database query performance

---

## Next Steps

After jadwal muncul:
1. Test booking flow
2. Test refund flow
3. Test notifications
4. Test admin dashboard

---

**Status**: ✅ SOLVED
**Last Updated**: 2026-05-27
