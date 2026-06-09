# WOOSH Integration - Quick Testing Guide

**Estimated Testing Time**: 30-45 seconds per flow

---

## Pre-Testing Setup

```bash
# 1. Clear Laravel cache
php artisan cache:clear
php artisan route:clear

# 2. Run migrations (if not done)
php artisan migrate

# 3. Start Laravel server
php artisan serve
```

---

## Quick Test Flows (30-45 seconds each)

### Test 1: User Registration Flow (45 sec)
```bash
# Mobile: Register new user
# Expected: User appears in admin dashboard within 5 seconds

# Verify in MySQL:
mysql> SELECT * FROM users WHERE email='test@example.com';

# Verify in admin:
http://localhost:8000/admin/users
```

### Test 2: Ticket Booking Flow (45 sec)
```bash
# Mobile: Book ticket
# Expected: Ticket appears in admin dashboard

# Verify in MySQL:
mysql> SELECT * FROM tickets WHERE ticket_code='WSH-TK-xxxxx';

# Verify in admin:
http://localhost:8000/admin/tickets
```

### Test 3: Refund Flow (30 sec)
```bash
# Mobile: Refund ticket
# Expected: Status changes to "Batal"

# Verify in MySQL:
mysql> SELECT status FROM tickets WHERE ticket_code='WSH-TK-xxxxx';
# Should show: Batal

# Verify in admin:
http://localhost:8000/admin/tickets
# Status should show: Batal
```

### Test 4: Broadcast Notification (30 sec)
```bash
# Admin: Send broadcast notification
# Expected: Notification appears on mobile

# Verify in Firestore:
Firebase Console → Firestore → users/{uid}/notifications
```

---

## API Testing with Curl (Quick Verification)

### Test Sync User API
```bash
curl -X POST http://localhost:8000/api/v1/sync-user \
  -H "Content-Type: application/json" \
  -d '{
    "firebase_uid": "test-uid-123",
    "name": "Test User",
    "email": "test@example.com",
    "phone": "08123456789"
  }'
```

### Test Get Trips API
```bash
curl http://localhost:8000/api/v1/trips
```

### Test Refund Ticket API
```bash
curl -X POST http://localhost:8000/api/v1/refund-ticket \
  -H "Content-Type: application/json" \
  -d '{
    "ticket_code": "WSH-TK-123456",
    "email": "user@example.com",
    "reason": "Change of plans"
  }'
```

### Test Validate Ticket API
```bash
curl -X POST http://localhost:8000/api/v1/validate-ticket \
  -H "Content-Type: application/json" \
  -d '{
    "ticket_code": "WSH-TK-123456"
  }'
```

---

## Database Quick Checks

### Check Users
```sql
SELECT id, name, email, firebase_uid, created_at FROM users ORDER BY created_at DESC LIMIT 5;
```

### Check Tickets
```sql
SELECT id, ticket_code, user_id, status, total_price, booked_at FROM tickets ORDER BY booked_at DESC LIMIT 5;
```

### Check Refund Requests
```sql
SELECT id, ticket_id, user_id, reason, status FROM refund_requests ORDER BY created_at DESC LIMIT 5;
```

### Check Activity Logs
```sql
SELECT id, user_id, action, model, model_id, created_at FROM activity_logs ORDER BY created_at DESC LIMIT 10;
```

---

## Firestore Quick Checks

### Check User Data
```
Firebase Console → Firestore → users/{uid}
```

### Check Tickets
```
Firebase Console → Firestore → users/{uid}/tickets
```

### Check Notifications
```
Firebase Console → Firestore → users/{uid}/notifications
```

### Check Trips
```
Firebase Console → Firestore → trips
```

---

## Email Testing

### Check Sent Emails
```bash
# Laravel logs
tail -f storage/logs/laravel.log | grep -i email

# Or check mail driver config in .env
MAIL_DRIVER=log  # For testing, emails go to logs
```

### Test Email Sending
```bash
php artisan tinker
>>> Mail::raw('Test email', function($m) { $m->to('test@example.com')->subject('Test'); });
```

---

## Common Issues & Quick Fixes

| Issue | Quick Fix |
|-------|-----------|
| 404 on API | `php artisan route:clear` |
| Firestore not syncing | Check Firebase credentials in `.env` |
| Email not sending | Check `MAIL_DRIVER` in `.env` (use `log` for testing) |
| User not appearing | Check MySQL sync in RegisterViewModel |
| Ticket not appearing | Check bookTicket API call in PaymentViewModel |

---

## Testing Checklist

- [ ] User registration syncs to MySQL
- [ ] User appears in admin dashboard
- [ ] Ticket booking syncs to MySQL
- [ ] Ticket appears in admin dashboard
- [ ] Refund updates status to "Batal"
- [ ] Admin dashboard shows "Batal" status
- [ ] Broadcast notification appears on mobile
- [ ] Email notifications are sent
- [ ] Firestore data is consistent with MySQL
- [ ] Activity logs are recorded

---

## Performance Notes

- **User Sync**: < 2 seconds
- **Ticket Booking**: < 3 seconds
- **Refund Processing**: < 2 seconds
- **Broadcast Notification**: < 5 seconds
- **Email Sending**: < 1 second (async)

---

**Total Testing Time**: ~3-5 minutes for all flows
**Status**: READY FOR TESTING
