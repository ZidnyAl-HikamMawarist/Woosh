# WOOSH Mobile ↔ Admin Dashboard Integration

Implementasi lengkap integrasi antara aplikasi mobile Android dan admin dashboard Laravel dengan Firebase Firestore.

---

## Phase 1: API Endpoints & Mobile Integration

### Task 1: Implement User Sync API Endpoint
**Description**: Create `/api/v1/sync-user` endpoint to sync user data from mobile to MySQL when user registers or logs in.

**Requirements**:
- Accept POST request with firebase_uid, name, email, phone
- Check if user exists in MySQL
- If exists: update user data
- If not exists: create new user
- Return success response with user data
- Log activity to activity_logs table

**Acceptance Criteria**:
- [x] Endpoint created in ApiController
- [x] User data synced to MySQL
- [x] Activity logged
- [x] Mobile can call endpoint successfully
- [x] Admin dashboard shows new users

**Sub-tasks**:
- Create route in routes/api.php
- Implement syncUserToMySQL() in ApiController
- Add validation for required fields
- Test with Postman

---

### Task 2: Implement Update Profile API Endpoint
**Description**: Create `/api/v1/update-profile` endpoint to update user profile data.

**Requirements**:
- Accept POST request with user_id, name, email, phone, address
- Update user in MySQL
- Sync to Firestore via FirebaseSyncService
- Return updated user data

**Acceptance Criteria**:
- [x] Endpoint created
- [x] Profile updated in MySQL
- [x] Profile synced to Firestore
- [x] Mobile receives updated data
- [x] Admin dashboard reflects changes

**Sub-tasks**:
- Create route in routes/api.php
- Implement updateProfile() in ApiController
- Add validation
- Test end-to-end

---

### Task 3: Implement Get Trips API Endpoint
**Description**: Create `/api/v1/trips` endpoint to fetch train schedules for mobile search.

**Requirements**:
- Accept GET request with optional filters (date, departure, arrival)
- Return list of trips from MySQL
- Include seat availability from Firestore
- Return JSON response

**Acceptance Criteria**:
- [ ] Endpoint created
- [x] Returns trips from MySQL
- [x] Includes seat availability
- [x] Mobile search works
- [x] Filters work correctly

**Sub-tasks**:
- Create route in routes/api.php
- Implement getTrips() in ApiController
- Add filtering logic
- Test with various filters

---

### Task 4: Implement Book Ticket API Endpoint
**Description**: Create `/api/v1/book-ticket` endpoint to save ticket booking to MySQL and Firestore.

**Requirements**:
- Accept POST request with user_id, trip_id, seats, total_price
- Create ticket in MySQL with status "Active"
- Create ticket in Firestore under users/{uid}/tickets
- Update seat availability in Firestore
- Send confirmation email
- Return ticket data with ticket_code

**Acceptance Criteria**:
- [ ] Endpoint created
- [x] Ticket saved to MySQL
- [x] Ticket saved to Firestore
- [x] Seats marked as booked
- [x] Email sent
- [x] Mobile receives ticket_code
- [x] Admin dashboard shows ticket

**Sub-tasks**:
- Create route in routes/api.php
- Implement bookTicket() in ApiController
- Add email notification
- Test payment flow

---

### Task 5: Implement Refund Ticket API Endpoint
**Description**: Create `/api/v1/refund-ticket` endpoint to process ticket refunds.

**Requirements**:
- Accept POST request with ticket_id, reason
- Update ticket status to "Batal" in MySQL
- Update ticket status in Firestore
- Release booked seats in Firestore
- Send refund notification email
- Create refund_request record

**Acceptance Criteria**:
- [ ] Endpoint created
- [~] Ticket status updated to "Batal"
- [~] Firestore updated
- [~] Seats released
- [ ] Email sent
- [~] Refund request logged
- [~] Mobile shows updated status

**Sub-tasks**:
- Create route in routes/api.php
- Implement refundTicket() in ApiController
- Add refund notification
- Test refund flow

---

### Task 6: Implement Validate Ticket API Endpoint
**Description**: Create `/api/v1/validate-ticket` endpoint for admin to validate tickets at station.

**Requirements**:
- Accept POST request with ticket_id
- Update ticket status to "Used" in MySQL
- Update ticket status in Firestore
- Log validation activity
- Return success response

**Acceptance Criteria**:
- [ ] Endpoint created
- [~] Ticket status updated to "Used"
- [ ] Firestore updated
- [ ] Activity logged
- [~] Admin can validate tickets
- [ ] Mobile shows updated status

**Sub-tasks**:
- Create route in routes/api.php
- Implement validateTicket() in ApiController
- Add activity logging
- Test validation flow

---

### Task 7: Implement Get User Tickets API Endpoint
**Description**: Create `/api/v1/user-tickets` endpoint to fetch user's tickets.

**Requirements**:
- Accept GET request with user_id
- Return list of user's tickets from MySQL
- Include ticket details (trip, seats, status, price)
- Sort by booking date descending

**Acceptance Criteria**:
- [ ] Endpoint created
- [~] Returns user's tickets
- [~] Includes all details
- [~] Sorted correctly
- [~] Mobile displays tickets

**Sub-tasks**:
- Create route in routes/api.php
- Implement getUserTickets() in ApiController
- Add sorting and filtering
- Test with multiple tickets

---

## Phase 2: Admin Dashboard Routes & Controllers

### Task 8: Create Admin User Management Route & Controller
**Description**: Implement `/admin/users` route and controller for user management.

**Requirements**:
- Display list of all users from MySQL
- Show user details (name, email, phone, loyalty points)
- Allow search and filter
- Show user activity history
- Link to user's tickets

**Acceptance Criteria**:
- [~] Route created
- [~] Controller created
- [~] View displays users
- [~] Search works
- [~] Activity history shown
- [~] Can view user tickets

**Sub-tasks**:
- Create route in routes/web.php
- Create AdminController method
- Create view template
- Add search functionality

---

### Task 9: Create Admin Trip Management Route & Controller
**Description**: Implement `/admin/trips` route and controller for train schedule management.

**Requirements**:
- Display list of all trips
- Allow add/edit/delete trips
- Show seat availability
- Sync to Firestore when changes made
- Show trip details

**Acceptance Criteria**:
- [ ] Route created
- [ ] Controller created
- [~] View displays trips
- [~] Can add trip
- [~] Can edit trip
- [~] Can delete trip
- [~] Firestore synced

**Sub-tasks**:
- Create routes in routes/web.php
- Create controller methods
- Create view templates
- Add form validation

---

### Task 10: Create Admin Ticket Management Route & Controller
**Description**: Implement `/admin/tickets` route and controller for ticket management.

**Requirements**:
- Display list of all tickets
- Show ticket details (passenger, trip, seats, status, price)
- Allow validate/refund tickets
- Show ticket history
- Filter by status

**Acceptance Criteria**:
- [ ] Route created
- [ ] Controller created
- [~] View displays tickets
- [~] Can view details
- [~] Can validate ticket
- [~] Can refund ticket
- [~] Status filters work

**Sub-tasks**:
- Create routes in routes/web.php
- Create controller methods
- Create view templates
- Add status filtering

---

### Task 11: Create Firebase Sync Route & Controller
**Description**: Implement `/admin/sync-firebase` route to manually sync MySQL data to Firestore.

**Requirements**:
- Sync all users to Firestore
- Sync all trips to Firestore
- Sync all tickets to Firestore
- Show sync progress
- Log sync activity

**Acceptance Criteria**:
- [ ] Route created
- [ ] Controller created
- [~] Can sync users
- [~] Can sync trips
- [~] Can sync tickets
- [~] Progress shown
- [ ] Activity logged

**Sub-tasks**:
- Create route in routes/web.php
- Create controller method
- Use FirebaseSyncService
- Add progress tracking

---

### Task 12: Create Notification Broadcast Route & Controller
**Description**: Implement `/admin/notifications/broadcast` route to send broadcast notifications.

**Requirements**:
- Accept POST request with message
- Write notification to Firestore for all users
- Send FCM push notification
- Log broadcast activity
- Show success message

**Acceptance Criteria**:
- [ ] Route created
- [ ] Controller created
- [~] Can send broadcast
- [~] Notification in Firestore
- [~] FCM sent
- [ ] Activity logged
- [~] Mobile receives notification

**Sub-tasks**:
- Create route in routes/web.php
- Create controller method
- Implement Firestore write
- Test notification delivery

---

## Phase 3: Database & Services

### Task 13: Create Database Migrations
**Description**: Create all necessary database tables for the application.

**Requirements**:
- Create users table with loyalty_points
- Create trips table
- Create tickets table with status
- Create activity_logs table
- Create refund_requests table
- Add proper indexes and foreign keys

**Acceptance Criteria**:
- [~] All tables created
- [~] Proper columns
- [~] Foreign keys set
- [~] Indexes added
- [~] Migrations run successfully

**Sub-tasks**:
- Create users migration
- Create trips migration
- Create tickets migration
- Create activity_logs migration
- Create refund_requests migration
- Run migrations

---

### Task 14: Implement Payment Service
**Description**: Create PaymentService to handle payment processing.

**Requirements**:
- Process payment from mobile
- Verify payment status
- Handle payment failures
- Log payment transactions
- Return payment status

**Acceptance Criteria**:
- [~] Service created
- [~] Can process payment
- [~] Verifies status
- [~] Handles failures
- [~] Transactions logged
- [~] Mobile receives status

**Sub-tasks**:
- Create PaymentService class
- Implement payment processing
- Add error handling
- Test payment flow

---

### Task 15: Implement Email Service
**Description**: Create EmailService to send confirmation and notification emails.

**Requirements**:
- Send booking confirmation email
- Send refund notification email
- Send broadcast notification email
- Use Laravel Mail
- Include ticket details in email

**Acceptance Criteria**:
- [ ] Service created
- [~] Can send confirmation
- [~] Can send refund notification
- [ ] Can send broadcast
- [~] Emails formatted properly
- [~] Users receive emails

**Sub-tasks**:
- Create EmailService class
- Create email templates
- Implement sending logic
- Test email delivery

---

## Phase 4: Mobile Integration

### Task 16: Update Mobile Register Flow
**Description**: Update RegisteredUserController to call sync-user API after registration.

**Requirements**:
- After Firebase Auth registration
- Call `/api/v1/sync-user` API
- Pass firebase_uid, name, email, phone
- Handle API response
- Show success/error message

**Acceptance Criteria**:
- [~] API called after registration
- [~] User synced to MySQL
- [~] User appears in admin dashboard
- [~] Error handling works
- [~] Mobile shows confirmation

**Sub-tasks**:
- Update RegisteredUserController
- Add API call
- Add error handling
- Test registration flow

---

### Task 17: Update Mobile Login Flow
**Description**: Update AuthenticatedSessionController to call sync-user API after login.

**Requirements**:
- After Firebase Auth login
- Call `/api/v1/sync-user` API
- Update user data in MySQL
- Handle API response
- Show success/error message

**Acceptance Criteria**:
- [~] API called after login
- [~] User data updated
- [~] Admin dashboard updated
- [ ] Error handling works
- [ ] Mobile shows confirmation

**Sub-tasks**:
- Update AuthenticatedSessionController
- Add API call
- Add error handling
- Test login flow

---

### Task 18: Update Mobile Profile Update Flow
**Description**: Update ProfileController to call update-profile API when user edits profile.

**Requirements**:
- When user saves profile changes
- Call `/api/v1/update-profile` API
- Pass updated data
- Handle API response
- Show success/error message

**Acceptance Criteria**:
- [~] API called on profile save
- [ ] Profile updated in MySQL
- [ ] Profile synced to Firestore
- [ ] Admin dashboard updated
- [ ] Mobile shows confirmation

**Sub-tasks**:
- Update ProfileController
- Add API call
- Add error handling
- Test profile update flow

---

### Task 19: Update Mobile Ticket Booking Flow
**Description**: Update ticket booking to call book-ticket API after payment.

**Requirements**:
- After payment successful
- Call `/api/v1/book-ticket` API
- Pass trip_id, seats, total_price
- Receive ticket_code
- Save ticket to local storage
- Show confirmation

**Acceptance Criteria**:
- [~] API called after payment
- [ ] Ticket saved to MySQL
- [ ] Ticket saved to Firestore
- [ ] Seats marked as booked
- [ ] Email sent
- [~] Mobile shows ticket_code
- [ ] Admin dashboard shows ticket

**Sub-tasks**:
- Update booking controller
- Add API call
- Add error handling
- Test booking flow

---

### Task 20: Update Mobile Refund Flow
**Description**: Update ticket refund to call refund-ticket API.

**Requirements**:
- When user clicks refund
- Call `/api/v1/refund-ticket` API
- Pass ticket_id and reason
- Update local ticket status
- Show confirmation

**Acceptance Criteria**:
- [~] API called on refund
- [ ] Ticket status updated to "Batal"
- [ ] Firestore updated
- [ ] Seats released
- [ ] Email sent
- [ ] Mobile shows updated status
- [ ] Admin dashboard updated

**Sub-tasks**:
- Update refund controller
- Add API call
- Add error handling
- Test refund flow

---

## Phase 5: Testing & Verification

### Task 21: End-to-End Testing - User Registration
**Description**: Test complete user registration flow from mobile to admin dashboard.

**Requirements**:
- User registers on mobile
- Firebase Auth creates account
- API syncs user to MySQL
- User appears in admin dashboard
- User data correct in both systems

**Acceptance Criteria**:
- [~] User registration works
- [~] User in MySQL
- [~] User in admin dashboard
- [~] Data consistent
- [~] No errors

**Sub-tasks**:
- Test registration on mobile
- Verify MySQL data
- Verify admin dashboard
- Check data consistency

---

### Task 22: End-to-End Testing - Train Schedule
**Description**: Test complete train schedule flow from admin to mobile.

**Requirements**:
- Admin adds train schedule
- Data saved to MySQL
- Data synced to Firestore
- Mobile search shows schedule
- Schedule details correct

**Acceptance Criteria**:
- [~] Schedule added in admin
- [~] Schedule in MySQL
- [~] Schedule in Firestore
- [ ] Mobile search works
- [~] Details correct

**Sub-tasks**:
- Add schedule in admin
- Verify MySQL data
- Verify Firestore data
- Test mobile search

---

### Task 23: End-to-End Testing - Ticket Booking
**Description**: Test complete ticket booking flow from mobile to admin.

**Requirements**:
- User books ticket on mobile
- Payment processed
- Ticket saved to MySQL
- Ticket saved to Firestore
- Ticket appears in admin dashboard
- Email sent
- Seats marked as booked

**Acceptance Criteria**:
- [~] Booking works
- [~] Ticket in MySQL
- [~] Ticket in Firestore
- [~] Ticket in admin dashboard
- [ ] Email sent
- [~] Seats booked
- [ ] No errors

**Sub-tasks**:
- Book ticket on mobile
- Verify MySQL data
- Verify Firestore data
- Verify admin dashboard
- Check email

---

### Task 24: End-to-End Testing - Refund
**Description**: Test complete refund flow from mobile to admin.

**Requirements**:
- User refunds ticket on mobile
- Ticket status updated to "Batal"
- MySQL updated
- Firestore updated
- Admin dashboard shows "Batal"
- Email sent
- Seats released

**Acceptance Criteria**:
- [~] Refund works
- [~] Status updated
- [~] MySQL updated
- [ ] Firestore updated
- [~] Admin shows "Batal"
- [ ] Email sent
- [ ] Seats released

**Sub-tasks**:
- Refund ticket on mobile
- Verify status change
- Verify admin dashboard
- Check email

---

### Task 25: End-to-End Testing - Notifications
**Description**: Test notification system from admin to mobile.

**Requirements**:
- Admin sends broadcast notification
- Notification written to Firestore for all users
- FCM push sent
- Mobile receives notification
- Notification appears in app

**Acceptance Criteria**:
- [~] Broadcast works
- [ ] Notification in Firestore
- [ ] FCM sent
- [~] Mobile receives
- [~] Notification displayed

**Sub-tasks**:
- Send broadcast in admin
- Verify Firestore data
- Check mobile notification
- Verify display

---

## Summary

Total Tasks: 25
- Phase 1 (API Endpoints): 7 tasks
- Phase 2 (Admin Routes): 5 tasks
- Phase 3 (Database & Services): 3 tasks
- Phase 4 (Mobile Integration): 5 tasks
- Phase 5 (Testing): 5 tasks

All tasks must be completed in order for full integration.
