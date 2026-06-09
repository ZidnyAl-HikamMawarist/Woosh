# WOOSH - Step-by-Step Implementation Guide

Panduan lengkap untuk mengimplementasikan semua fitur integrasi mobile ↔ admin dashboard.

---

## 📋 TABLE OF CONTENTS

1. [Setup & Prerequisites](#setup--prerequisites)
2. [Database Schema](#database-schema)
3. [API Implementation](#api-implementation)
4. [Mobile Implementation](#mobile-implementation)
5. [Admin Dashboard Implementation](#admin-dashboard-implementation)
6. [Testing & Verification](#testing--verification)
7. [Deployment](#deployment)

---

## Setup & Prerequisites

### Requirements
- Android Studio (latest)
- PHP 8.2+
- MySQL 8.0+
- Laravel 11
- Firebase Project (with Firestore & Auth enabled)
- Postman (for API testing)

### Firebase Setup
1. Create Firebase project at https://console.firebase.google.com
2. Enable Firestore Database
3. Enable Firebase Authentication (Email/Password)
4. Enable Firebase Cloud Messaging (FCM)
5. Download service account JSON file
6. Place it in `admin-woosh/storage/app/firebase-auth.json`

### Laravel Setup
```bash
cd admin-woosh

# Install dependencies
composer install
npm install

# Setup environment
cp .env.example .env
php artisan key:generate

# Setup database
php artisan migrate
php artisan db:seed

# Clear cache
php artisan cache:clear
php artisan route:clear

# Start development server
php artisan serve
npm run dev
```

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    firebase_uid VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    loyalty_points BIGINT DEFAULT 0,
    active_pass VARCHAR(50),
    remaining_trips INT DEFAULT 0,
    pass_expiry_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Trips Table
```sql
CREATE TABLE trips (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    trip_id VARCHAR(255) UNIQUE,
    train_name VARCHAR(255),
    departure_time TIME,
    arrival_time TIME,
    train_class VARCHAR(50),
    base_price DECIMAL(10, 2),
    booked_seats JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Tickets Table
```sql
CREATE TABLE tickets (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    ticket_code VARCHAR(255) UNIQUE,
    user_id BIGINT UNSIGNED,
    trip_id VARCHAR(255),
    seats_list JSON,
    total_amount DECIMAL(10, 2),
    status ENUM('Active', 'Used', 'Batal', 'Rescheduled') DEFAULT 'Active',
    passengers JSON,
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (trip_id) REFERENCES trips(trip_id)
);
```

### Activity Logs Table
```sql
CREATE TABLE activity_logs (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED,
    action VARCHAR(255),
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Refund Requests Table
```sql
CREATE TABLE refund_requests (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT UNSIGNED,
    user_id BIGINT UNSIGNED,
    reason TEXT,
    status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending',
    refund_amount DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## API Implementation

### Step 1: Create API Routes
**File**: `admin-woosh/routes/api.php`

```php
<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\ApiController;

Route::post('/v1/sync-user', [ApiController::class, 'syncUser']);
Route::post('/v1/update-profile', [ApiController::class, 'updateProfile']);
Route::get('/v1/trips', [ApiController::class, 'getTrips']);
Route::post('/v1/book-ticket', [ApiController::class, 'bookTicket']);
Route::post('/v1/refund-ticket', [ApiController::class, 'refundTicket']);
Route::get('/v1/user-tickets', [ApiController::class, 'getUserTickets']);
Route::post('/v1/validate-ticket', [ApiController::class, 'validateTicket']);
```

### Step 2: Create ApiController
**File**: `admin-woosh/app/Http/Controllers/Api/ApiController.php`

```php
<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\Trip;
use App\Models\Ticket;
use App\Services\FirebaseSyncService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;

class ApiController extends Controller
{
    protected FirebaseSyncService $firebaseSync;

    public function __construct(FirebaseSyncService $firebaseSync)
    {
        $this->firebaseSync = $firebaseSync;
    }

    public function syncUser(Request $request)
    {
        try {
            $firebaseUid = $request->input('firebase_uid');
            $name = $request->input('name');
            $email = $request->input('email');
            $phone = $request->input('phone', '');

            if (!$firebaseUid || !$email) {
                return response()->json(['error' => 'Missing required fields'], 400);
            }

            $user = User::firstOrCreate(
                ['email' => $email],
                [
                    'firebase_uid' => $firebaseUid,
                    'name' => $name,
                    'phone' => $phone,
                    'loyalty_points' => 0,
                ]
            );

            if (!$user->wasRecentlyCreated) {
                $user->update([
                    'firebase_uid' => $firebaseUid,
                    'name' => $name,
                    'phone' => $phone,
                ]);
            }

            $this->firebaseSync->syncUserPoints($firebaseUid, $user->toArray());

            Log::info("[API] User synced: {$firebaseUid}");

            return response()->json([
                'success' => true,
                'message' => 'User synced successfully',
                'user' => $user,
            ]);
        } catch (\Throwable $e) {
            Log::error("[API] Sync user failed: " . $e->getMessage());
            return response()->json(['error' => $e->getMessage()], 500);
        }
    }

    public function updateProfile(Request $request)
    {
        try {
            $firebaseUid = $request->input('firebase_uid');
            $name = $request->input('name');
            $phone = $request->input('phone');
            $email = $request->input('email');

            $user = User::where('firebase_uid', $firebaseUid)->first();
            if (!$user) {
                return response()->json(['error' => 'User not found'], 404);
            }

            $user->update([
                'name' => $name,
                'phone' => $phone,
                'email' => $email,
            ]);

            $this->firebaseSync->syncUserPoints($firebaseUid, $user->toArray());

            Log::info("[API] Profile updated: {$firebaseUid}");

            return response()->json([
                'success' => true,
                'message' => 'Profile updated successfully',
                'user' => $user,
            ]);
        } catch (\Throwable $e) {
            Log::error("[API] Update profile failed: " . $e->getMessage());
            return response()->json(['error' => $e->getMessage()], 500);
        }
    }

    public function getTrips()
    {
        try {
            $trips = Trip::all();
            return response()->json([
                'success' => true,
                'trips' => $trips,
            ]);
        } catch (\Throwable $e) {
            Log::error("[API] Get trips failed: " . $e->getMessage());
            return response()->json(['error' => $e->getMessage()], 500);
        }
    }

    public function bookTicket(Request $request)
    {
        try {
            $firebaseUid = $request->input('firebase_uid');
            $tripId = $request->input('trip_id');
            $seats = $request->input('seats');
            $totalPrice = $request->input('total_price');
            $passengers = $request->input('passengers');

            if (!$firebaseUid || !$tripId || !$seats || !$totalPrice) {
                return response()->json(['error' => 'Missing required fields'], 400);
            }

            $user = User::where('firebase_uid', $firebaseUid)->first();
            if (!$user) {
                return response()->json(['error' => 'User not found'], 404);
            }

            $ticketCode = 'WSH-TK-' . strtoupper(uniqid());
            $ticket = Ticket::create([
                'ticket_code' => $ticketCode,
                'user_id' => $user->id,
                'trip_id' => $tripId,
                'seats_list' => json_encode($seats),
                'total_amount' => $totalPrice,
                'status' => 'Active',
                'booked_at' => now(),
                'passengers' => json_encode($passengers),
            ]);

            $trip = Trip::where('trip_id', $tripId)->first();
            if ($trip) {
                $bookedSeats = json_decode($trip->booked_seats ?? '[]', true);
                $bookedSeats = array_merge($bookedSeats, $seats);
                $trip->update(['booked_seats' => json_encode($bookedSeats)]);
                $this->firebaseSync->syncTrip($trip->toArray());
            }

            $points = (int) ($totalPrice * 0.1);
            $user->increment('loyalty_points', $points);
            $this->firebaseSync->syncUserPoints($firebaseUid, $user->toArray());

            Log::info("[API] Ticket booked: {$ticketCode}");

            return response()->json([
                'success' => true,
                'message' => 'Ticket booked successfully',
                'ticket' => $ticket,
            ]);
        } catch (\Throwable $e) {
            Log::error("[API] Book ticket failed: " . $e->getMessage());
            return response()->json(['error' => $e->getMessage()], 500);
        }
    }

    public function refundTicket(Request $request)
    {
        try {
            $firebaseUid = $request->input('firebase_uid');
            $ticketCode = $request->input('ticket_code');

            $ticket = Ticket::where('ticket_code', $ticketCode)->first();
            if (!$ticket) {
                return response()->json(['error' => 'Ticket not found'], 404);
            }

            $ticket->update(['status' => 'Batal']);

            $trip = Trip::where('trip_id', $ticket->trip_id)->first();
            if ($trip) {
                $bookedSeats = json_decode($trip->booked_seats ?? '[]', true);
                $seats = json_decode($ticket->seats_list ?? '[]', true);
                $bookedSeats = array_diff($bookedSeats, $seats);
                $trip->update(['booked_seats' => json_encode($bookedSeats)]);
                $this->firebaseSync->syncTrip($trip->toArray());
            }

            $this->firebaseSync->sendNotification(
                $firebaseUid,
                'Refund Berhasil',
                'Tiket ' . $ticketCode . ' telah dibatalkan.',
                'REFUND'
            );

            Log::info("[API] Ticket refunded: {$ticketCode}");

            return response()->json([
                'success' => true,
                'message' => 'Ticket refunded successfully',
                'ticket' => $ticket,
            ]);
        } catch (\Throwable $e) {
            Log::error("[API] Refund ticket failed: " . $e->getMessage());
            return response()->json(['error' => $e->getMessage()], 500);
        }
    }

    public function getUserTickets(Request $request)
    {
        try {
            $firebaseUid = $request->input('firebase_uid');

            $user = User::where('firebase_uid', $firebaseUid)->first();
            if (!$user) {
                return response()->json(['error' => 'User not found'], 404);
            }

            $tickets = Ticket::where('user_id', $user->id)->get();

            return response()->json([
                'success' => true,
                'tickets' => $tickets,
            ]);
        } catch (\Throwable $e) {
            Log::error("[API] Get user tickets failed: " . $e->getMessage());
            return response()->json(['error' => $e->getMessage()], 500);
        }
    }

    public function validateTicket(Request $request)
    {
        try {
            $ticketCode = $request->input('ticket_code');

            $ticket = Ticket::where('ticket_code', $ticketCode)->first();
            if (!$ticket) {
                return response()->json(['error' => 'Ticket not found'], 404);
            }

            $ticket->update(['status' => 'Used']);

            $user = User::find($ticket->user_id);
            if ($user) {
                $this->firebaseSync->syncTicketStatus(
                    $user->firebase_uid,
                    $ticket->id,
                    'Used'
                );
            }

            Log::info("[API] Ticket validated: {$ticketCode}");

            return response()->json([
                'success' => true,
                'message' => 'Ticket validated successfully',
                'ticket' => $ticket,
            ]);
        } catch (\Throwable $e) {
            Log::error("[API] Validate ticket failed: " . $e->getMessage());
            return response()->json(['error' => $e->getMessage()], 500);
        }
    }
}
```

---

## Mobile Implementation

### Step 1: Create API Service Interface
**File**: `app/src/main/java/com/example/woosh/data/remote/ApiService.kt`

```kotlin
package com.example.woosh.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("/api/v1/sync-user")
    suspend fun syncUser(
        @Query("firebase_uid") firebaseUid: String,
        @Query("name") name: String,
        @Query("email") email: String,
        @Query("phone") phone: String
    ): Response<SyncUserResponse>

    @POST("/api/v1/update-profile")
    suspend fun updateProfile(
        @Query("firebase_uid") firebaseUid: String,
        @Query("name") name: String,
        @Query("phone") phone: String,
        @Query("email") email: String
    ): Response<UpdateProfileResponse>

    @GET("/api/v1/trips")
    suspend fun getTrips(): Response<GetTripsResponse>

    @POST("/api/v1/book-ticket")
    suspend fun bookTicket(
        @Query("firebase_uid") firebaseUid: String,
        @Query("trip_id") tripId: String,
        @Query("seats") seats: String,
        @Query("total_price") totalPrice: Double,
        @Query("passengers") passengers: String
    ): Response<BookTicketResponse>

    @POST("/api/v1/refund-ticket")
    suspend fun refundTicket(
        @Query("firebase_uid") firebaseUid: String,
        @Query("ticket_code") ticketCode: String
    ): Response<RefundTicketResponse>

    @GET("/api/v1/user-tickets")
    suspend fun getUserTickets(
        @Query("firebase_uid") firebaseUid: String
    ): Response<GetUserTicketsResponse>
}

// Response models
data class SyncUserResponse(val success: Boolean, val message: String, val user: UserData)
data class UpdateProfileResponse(val success: Boolean, val message: String, val user: UserData)
data class GetTripsResponse(val success: Boolean, val trips: List<TripData>)
data class BookTicketResponse(val success: Boolean, val message: String, val ticket: TicketData)
data class RefundTicketResponse(val success: Boolean, val message: String, val ticket: TicketData)
data class GetUserTicketsResponse(val success: Boolean, val tickets: List<TicketData>)

data class UserData(
    val id: Int,
    val firebase_uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val loyalty_points: Long
)

data class TripData(
    val id: Int,
    val trip_id: String,
    val train_name: String,
    val departure_time: String,
    val arrival_time: String,
    val train_class: String,
    val base_price: Double,
    val booked_seats: List<String>
)

data class TicketData(
    val id: Int,
    val ticket_code: String,
    val trip_id: String,
    val seats_list: List<String>,
    val total_amount: Double,
    val status: String,
    val booked_at: String
)
```

### Step 2: Update AuthViewModel
**File**: `app/src/main/java/com/example/woosh/ui/viewmodels/AuthViewModel.kt`

```kotlin
fun register(name: String, email: String, password: String) {
    viewModelScope.launch {
        try {
            val result = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUid = result.user?.uid ?: return@launch

            // Sync to MySQL
            val response = RetrofitClient.instance.syncUser(
                firebaseUid = firebaseUid,
                name = name,
                email = email,
                phone = ""
            )

            if (response.isSuccessful) {
                _authState.value = AuthState.Success(firebaseUid)
            } else {
                _authState.value = AuthState.Error("Sync failed: ${response.message()}")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }
}

fun login(email: String, password: String) {
    viewModelScope.launch {
        try {
            val result = Firebase.auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUid = result.user?.uid ?: return@launch

            // Sync to MySQL
            val response = RetrofitClient.instance.syncUser(
                firebaseUid = firebaseUid,
                name = result.user?.displayName ?: "",
                email = email,
                phone = ""
            )

            if (response.isSuccessful) {
                _authState.value = AuthState.Success(firebaseUid)
            } else {
                _authState.value = AuthState.Error("Sync failed: ${response.message()}")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }
}
```

---

## Admin Dashboard Implementation

### Step 1: Create Admin Controllers
**File**: `admin-woosh/app/Http/Controllers/Admin/AdminController.php`

```php
<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\Trip;
use App\Models\Ticket;
use App\Services\FirebaseSyncService;
use Illuminate\Http\Request;

class AdminController extends Controller
{
    protected FirebaseSyncService $firebaseSync;

    public function __construct(FirebaseSyncService $firebaseSync)
    {
        $this->firebaseSync = $firebaseSync;
    }

    public function dashboard()
    {
        $totalUsers = User::count();
        $totalTrips = Trip::count();
        $totalTickets = Ticket::count();
        $totalRevenue = Ticket::where('status', 'Active')->sum('total_amount');

        return view('admin.dashboard', compact('totalUsers', 'totalTrips', 'totalTickets', 'totalRevenue'));
    }

    public function users()
    {
        $users = User::paginate(15);
        return view('admin.users', compact('users'));
    }

    public function trips()
    {
        $trips = Trip::paginate(15);
        return view('admin.trips', compact('trips'));
    }

    public function storeTrip(Request $request)
    {
        $validated = $request->validate([
            'train_name' => 'required|string',
            'departure_time' => 'required|date_format:H:i',
            'arrival_time' => 'required|date_format:H:i',
            'train_class' => 'required|string',
            'base_price' => 'required|numeric',
        ]);

        $trip = Trip::create([
            'trip_id' => 'TRIP-' . strtoupper(uniqid()),
            ...$validated,
            'booked_seats' => json_encode([]),
        ]);

        $this->firebaseSync->syncTrip($trip->toArray());

        return redirect()->route('admin.trips')->with('success', 'Trip created successfully');
    }

    public function updateTrip(Request $request, $id)
    {
        $trip = Trip::find($id);
        if (!$trip) {
            return redirect()->back()->with('error', 'Trip not found');
        }

        $validated = $request->validate([
            'train_name' => 'required|string',
            'departure_time' => 'required|date_format:H:i',
            'arrival_time' => 'required|date_format:H:i',
            'train_class' => 'required|string',
            'base_price' => 'required|numeric',
        ]);

        $trip->update($validated);
        $this->firebaseSync->syncTrip($trip->toArray());

        return redirect()->route('admin.trips')->with('success', 'Trip updated successfully');
    }

    public function deleteTrip($id)
    {
        $trip = Trip::find($id);
        if (!$trip) {
            return redirect()->back()->with('error', 'Trip not found');
        }

        $this->firebaseSync->deleteTrip($trip->trip_id);
        $trip->delete();

        return redirect()->route('admin.trips')->with('success', 'Trip deleted successfully');
    }

    public function tickets()
    {
        $tickets = Ticket::with('user')->paginate(15);
        return view('admin.tickets', compact('tickets'));
    }

    public function validateTicket($id)
    {
        $ticket = Ticket::find($id);
        if (!$ticket) {
            return redirect()->back()->with('error', 'Ticket not found');
        }

        $ticket->update(['status' => 'Used']);
        $user = User::find($ticket->user_id);
        if ($user) {
            $this->firebaseSync->syncTicketStatus($user->firebase_uid, $ticket->id, 'Used');
        }

        return redirect()->back()->with('success', 'Ticket validated successfully');
    }

    public function refundTicket($id)
    {
        $ticket = Ticket::find($id);
        if (!$ticket) {
            return redirect()->back()->with('error', 'Ticket not found');
        }

        $ticket->update(['status' => 'Batal']);
        $user = User::find($ticket->user_id);
        if ($user) {
            $this->firebaseSync->syncTicketStatus($user->firebase_uid, $ticket->id, 'Batal');
            $this->firebaseSync->sendNotification(
                $user->firebase_uid,
                'Refund Berhasil',
                'Tiket ' . $ticket->ticket_code . ' telah dibatalkan.',
                'REFUND'
            );
        }

        return redirect()->back()->with('success', 'Ticket refunded successfully');
    }
}
```

---

## Testing & Verification

### Test Checklist
- [ ] User register → appears in admin
- [ ] User login → data synced to MySQL
- [ ] User edit profile → changes appear in admin
- [ ] Admin add trip → appears in mobile
- [ ] User book ticket → appears in admin
- [ ] User refund ticket → status changes in admin
- [ ] Admin validate ticket → status changes in mobile
- [ ] Admin broadcast notification → appears in mobile

### Using Postman
1. Import API collection
2. Set base URL to `http://localhost:8000`
3. Test each endpoint with sample data
4. Verify responses and database changes

---

## Deployment

### Pre-Deployment Checklist
- [ ] All tests passing
- [ ] Firebase rules configured
- [ ] Database backed up
- [ ] Environment variables set
- [ ] SSL certificate installed
- [ ] Admin credentials secured

### Deployment Steps
1. Build mobile app for release
2. Deploy Laravel app to production server
3. Configure Firebase for production
4. Test all flows in production
5. Monitor logs and errors

---

**Last Updated**: 2026-05-26
**Status**: Ready for Implementation
