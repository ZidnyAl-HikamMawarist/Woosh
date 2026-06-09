# WOOSH - Fixes and Solutions Guide

Panduan lengkap untuk memperbaiki semua masalah integrasi antara mobile dan admin dashboard.

---

## 1. FIX: Server Configuration Subtitle Tidak Update

### Problem
Saat user memilih IP server di Settings → Development → Konfigurasi Server, subtitle tidak ter-update sampai app di-restart.

### Root Cause
State `serverUrl` di SettingsScreen tidak ter-update ketika dialog ditutup. Dialog memiliki state lokal sendiri yang tidak ter-sync dengan parent state.

### Solution
Gunakan `LaunchedEffect` untuk update parent state ketika dialog ditutup.

**File**: `app/src/main/java/com/example/woosh/ui/screens/SettingsScreen.kt`

```kotlin
// Section: Development (Only shown in Debug builds)
if (BuildConfig.DEBUG) {
    var serverUrl by remember { mutableStateOf(RetrofitClient.getCurrentBaseUrl()) }
    var showServerConfigDialog by remember { mutableStateOf(false) }

    // Update serverUrl ketika dialog ditutup
    LaunchedEffect(showServerConfigDialog) {
        if (!showServerConfigDialog) {
            serverUrl = RetrofitClient.getCurrentBaseUrl()
        }
    }

    if (showServerConfigDialog) {
        // Dialog content...
    }

    SettingsSection(title = "Development") {
        SettingsClickItem(
            icon = Icons.Default.Dns,
            title = "Konfigurasi Server",
            subtitle = serverUrl.removePrefix("http://").removePrefix("https://").removeSuffix("/"),
            onClick = {
                showServerConfigDialog = true
            }
        )
    }
}
```

**Status**: ✅ FIXED

---

## 2. FIX: User Tidak Muncul di Admin Dashboard

### Problem
User yang register di mobile tidak muncul di daftar user admin.

### Root Cause
API `/api/v1/sync-user` tidak dipanggil saat register atau login, atau endpoint tidak terdaftar.

### Solution

#### Step 1: Pastikan API Endpoint Terdaftar
**File**: `admin-woosh/routes/api.php`

```php
Route::middleware('auth:sanctum')->group(function () {
    Route::post('/v1/sync-user', [ApiController::class, 'syncUser']);
    Route::post('/v1/update-profile', [ApiController::class, 'updateProfile']);
    Route::post('/v1/book-ticket', [ApiController::class, 'bookTicket']);
    Route::post('/v1/refund-ticket', [ApiController::class, 'refundTicket']);
    Route::get('/v1/trips', [ApiController::class, 'getTrips']);
    Route::get('/v1/user-tickets', [ApiController::class, 'getUserTickets']);
});
```

#### Step 2: Implementasi ApiController
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

    /**
     * Sync user dari mobile ke MySQL
     */
    public function syncUser(Request $request)
    {
        try {
            $firebaseUid = $request->input('firebase_uid');
            $name = $request->input('name');
            $email = $request->input('email');
            $phone = $request->input('phone');

            if (!$firebaseUid || !$email) {
                return response()->json(['error' => 'Missing required fields'], 400);
            }

            // Cari atau buat user
            $user = User::firstOrCreate(
                ['email' => $email],
                [
                    'firebase_uid' => $firebaseUid,
                    'name' => $name,
                    'phone' => $phone,
                    'loyalty_points' => 0,
                ]
            );

            // Update jika sudah ada
            if ($user->wasRecentlyCreated === false) {
                $user->update([
                    'firebase_uid' => $firebaseUid,
                    'name' => $name,
                    'phone' => $phone,
                ]);
            }

            // Sync ke Firestore
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

    /**
     * Update profil user
     */
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

            // Sync ke Firestore
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

    /**
     * Get all trips
     */
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

    /**
     * Book ticket
     */
    public function bookTicket(Request $request)
    {
        try {
            $firebaseUid = $request->input('firebase_uid');
            $tripId = $request->input('trip_id');
            $seats = $request->input('seats'); // Array of seat codes
            $totalPrice = $request->input('total_price');
            $passengers = $request->input('passengers'); // Array of passenger data

            if (!$firebaseUid || !$tripId || !$seats || !$totalPrice) {
                return response()->json(['error' => 'Missing required fields'], 400);
            }

            // Get user
            $user = User::where('firebase_uid', $firebaseUid)->first();
            if (!$user) {
                return response()->json(['error' => 'User not found'], 404);
            }

            // Create ticket
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

            // Update trip booked seats
            $trip = Trip::find($tripId);
            if ($trip) {
                $bookedSeats = json_decode($trip->booked_seats ?? '[]', true);
                $bookedSeats = array_merge($bookedSeats, $seats);
                $trip->update(['booked_seats' => json_encode($bookedSeats)]);

                // Sync trip to Firestore
                $this->firebaseSync->syncTrip($trip->toArray());
            }

            // Add loyalty points
            $points = (int) ($totalPrice * 0.1); // 10% of price
            $user->increment('loyalty_points', $points);

            // Sync user to Firestore
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

    /**
     * Refund ticket
     */
    public function refundTicket(Request $request)
    {
        try {
            $firebaseUid = $request->input('firebase_uid');
            $ticketCode = $request->input('ticket_code');

            $ticket = Ticket::where('ticket_code', $ticketCode)->first();
            if (!$ticket) {
                return response()->json(['error' => 'Ticket not found'], 404);
            }

            // Update ticket status
            $ticket->update(['status' => 'Batal']);

            // Remove seats from trip
            $trip = Trip::find($ticket->trip_id);
            if ($trip) {
                $bookedSeats = json_decode($trip->booked_seats ?? '[]', true);
                $seats = json_decode($ticket->seats_list ?? '[]', true);
                $bookedSeats = array_diff($bookedSeats, $seats);
                $trip->update(['booked_seats' => json_encode($bookedSeats)]);

                // Sync trip to Firestore
                $this->firebaseSync->syncTrip($trip->toArray());
            }

            // Send notification
            $this->firebaseSync->sendNotification(
                $firebaseUid,
                'Refund Berhasil',
                'Tiket ' . $ticketCode . ' telah dibatalkan. Dana akan dikembalikan dalam 3-5 hari kerja.',
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

    /**
     * Get user tickets
     */
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
}
```

#### Step 3: Panggil API dari Mobile saat Register
**File**: `app/src/main/java/com/example/woosh/ui/viewmodels/AuthViewModel.kt`

```kotlin
fun register(name: String, email: String, password: String) {
    viewModelScope.launch {
        try {
            // Register di Firebase
            val result = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUid = result.user?.uid ?: return@launch

            // Sync ke MySQL
            val response = apiService.syncUser(
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
```

**Status**: 🔧 IN PROGRESS

---

## 3. FIX: Tiket Tidak Muncul di Admin Dashboard

### Problem
Tiket yang dipesan di mobile tidak muncul di transaksi admin.

### Root Cause
API `bookTicket` hanya menyimpan ke Firestore, tidak menyimpan ke MySQL.

### Solution
Pastikan `bookTicket` API menyimpan ke MySQL setelah pembayaran berhasil (lihat Step 2 di atas untuk implementasi lengkap).

**Status**: 🔧 IN PROGRESS

---

## 4. FIX: Refund Tidak Berfungsi

### Problem
Saat user refund tiket, status di mobile tidak berubah menjadi "Batal".

### Root Cause
TicketViewModel tidak memanggil refund API, hanya update Firestore lokal.

### Solution
Update TicketViewModel untuk memanggil API refund.

**File**: `app/src/main/java/com/example/woosh/ui/viewmodels/TicketViewModel.kt`

```kotlin
fun refundTicket(ticketCode: String) {
    viewModelScope.launch {
        try {
            val firebaseUid = Firebase.auth.currentUser?.uid ?: return@launch

            // Call API refund
            val response = apiService.refundTicket(
                firebaseUid = firebaseUid,
                ticketCode = ticketCode
            )

            if (response.isSuccessful) {
                // Update local state
                _tickets.value = _tickets.value.map { ticket ->
                    if (ticket.ticketCode == ticketCode) {
                        ticket.copy(status = "Batal")
                    } else {
                        ticket
                    }
                }
                _refundState.value = RefundState.Success("Tiket berhasil dibatalkan")
            } else {
                _refundState.value = RefundState.Error("Refund gagal: ${response.message()}")
            }
        } catch (e: Exception) {
            _refundState.value = RefundState.Error(e.message ?: "Unknown error")
        }
    }
}
```

**Status**: 🔧 IN PROGRESS

---

## 5. FIX: Sync Firebase Route 404

### Problem
Tombol "Sync Firebase" di admin menampilkan error 404.

### Root Cause
Route `/admin/sync-firebase` tidak terdaftar atau cache route belum di-clear.

### Solution

#### Step 1: Clear Route Cache
```bash
cd admin-woosh
php artisan route:clear
php artisan cache:clear
```

#### Step 2: Pastikan Route Terdaftar
**File**: `admin-woosh/routes/web.php`

```php
Route::middleware(['auth', 'verified'])->group(function () {
    Route::get('/admin/dashboard', [AdminController::class, 'dashboard'])->name('admin.dashboard');
    Route::get('/admin/users', [AdminController::class, 'users'])->name('admin.users');
    Route::get('/admin/trips', [AdminController::class, 'trips'])->name('admin.trips');
    Route::post('/admin/trips', [AdminController::class, 'storeTrip'])->name('admin.trips.store');
    Route::put('/admin/trips/{id}', [AdminController::class, 'updateTrip'])->name('admin.trips.update');
    Route::delete('/admin/trips/{id}', [AdminController::class, 'deleteTrip'])->name('admin.trips.delete');
    
    Route::get('/admin/tickets', [AdminController::class, 'tickets'])->name('admin.tickets');
    Route::post('/admin/tickets/{id}/validate', [AdminController::class, 'validateTicket'])->name('admin.tickets.validate');
    Route::post('/admin/tickets/{id}/refund', [AdminController::class, 'refundTicket'])->name('admin.tickets.refund');
    
    Route::get('/admin/notifications', [NotificationController::class, 'index'])->name('admin.notifications');
    Route::post('/admin/notifications/broadcast', [NotificationController::class, 'broadcast'])->name('admin.notifications.broadcast');
    
    Route::get('/admin/sync-firebase', [FirebaseController::class, 'index'])->name('admin.sync-firebase');
    Route::post('/admin/sync-firebase', [FirebaseController::class, 'sync'])->name('admin.sync-firebase.store');
    
    Route::get('/admin/reports', [ReportController::class, 'index'])->name('admin.reports');
});
```

#### Step 3: Implementasi FirebaseController
**File**: `admin-woosh/app/Http/Controllers/Admin/FirebaseController.php`

```php
<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Services\FirebaseSyncService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;

class FirebaseController extends Controller
{
    protected FirebaseSyncService $firebaseSync;

    public function __construct(FirebaseSyncService $firebaseSync)
    {
        $this->firebaseSync = $firebaseSync;
    }

    public function index()
    {
        return view('admin.sync-firebase');
    }

    public function sync(Request $request)
    {
        try {
            $syncType = $request->input('sync_type', 'all');

            $result = match ($syncType) {
                'trips' => $this->firebaseSync->syncAllTrips(),
                'users' => $this->syncAllUsers(),
                default => $this->syncAll(),
            };

            Log::info("[Admin] Firebase sync completed: {$syncType}");

            return response()->json([
                'success' => true,
                'message' => "Sync completed: {$result} items",
            ]);
        } catch (\Throwable $e) {
            Log::error("[Admin] Firebase sync failed: " . $e->getMessage());
            return response()->json(['error' => $e->getMessage()], 500);
        }
    }

    private function syncAll(): int
    {
        $count = 0;
        $count += $this->firebaseSync->syncAllTrips();
        $count += $this->syncAllUsers();
        return $count;
    }

    private function syncAllUsers(): int
    {
        $users = \App\Models\User::all();
        $count = 0;
        foreach ($users as $user) {
            if ($user->firebase_uid) {
                $this->firebaseSync->syncUserPoints($user->firebase_uid, $user->toArray());
                $count++;
            }
        }
        return $count;
    }
}
```

**Status**: 🔧 IN PROGRESS

---

## 6. FIX: Broadcast Notification Tidak Muncul di Mobile

### Problem
Notifikasi broadcast dari admin tidak muncul di mobile.

### Root Cause
NotificationController tidak menulis ke Firestore setiap user.

### Solution

**File**: `admin-woosh/app/Http/Controllers/Admin/NotificationController.php`

```php
<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Services\FirebaseSyncService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;

class NotificationController extends Controller
{
    protected FirebaseSyncService $firebaseSync;

    public function __construct(FirebaseSyncService $firebaseSync)
    {
        $this->firebaseSync = $firebaseSync;
    }

    public function index()
    {
        return view('admin.notifications');
    }

    public function broadcast(Request $request)
    {
        try {
            $title = $request->input('title');
            $body = $request->input('body');
            $type = $request->input('type', 'INFO');

            if (!$title || !$body) {
                return response()->json(['error' => 'Missing required fields'], 400);
            }

            // Send to all users
            $users = User::whereNotNull('firebase_uid')->get();
            $count = 0;

            foreach ($users as $user) {
                $this->firebaseSync->sendNotification(
                    $user->firebase_uid,
                    $title,
                    $body,
                    $type
                );
                $count++;
            }

            Log::info("[Admin] Broadcast sent to {$count} users");

            return response()->json([
                'success' => true,
                'message' => "Notification sent to {$count} users",
            ]);
        } catch (\Throwable $e) {
            Log::error("[Admin] Broadcast failed: " . $e->getMessage());
            return response()->json(['error' => $e->getMessage()], 500);
        }
    }
}
```

**Status**: 🔧 IN PROGRESS

---

## 7. FIX: Cleartext Traffic Error

### Problem
"cleartext communication to 10.30.203.131 not permitted by network security policy"

### Root Cause
Network security config tidak mengizinkan cleartext untuk IP tertentu.

### Solution
Sudah di-fix dengan `cleartextTrafficPermitted="true"` di `network_security_config.xml`.

**File**: `app/src/main/res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Mengizinkan cleartext HTTP secara global untuk mempermudah development dengan IP dinamis -->
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

**Status**: ✅ FIXED

---

## 8. FIX: Gerbong Classification (First Class, Business, Economy)

### Problem
Gerbong tidak diklasifikasikan dengan benar (First Class, Business, Premium Economy, Economy).

### Solution

**File**: `app/src/main/java/com/example/woosh/data/models/Seat.kt`

```kotlin
data class Seat(
    val seatCode: String,
    val row: Int,
    val column: Int,
    val isBooked: Boolean = false,
    val seatClass: SeatClass = SeatClass.ECONOMY
)

enum class SeatClass {
    FIRST_CLASS,      // Gerbong 1
    BUSINESS_CLASS,   // Gerbong 2-3
    PREMIUM_ECONOMY,  // Gerbong 4-5
    ECONOMY           // Gerbong 6+
}
```

**File**: `app/src/main/java/com/example/woosh/ui/viewmodels/SeatSelectionViewModel.kt`

```kotlin
fun generateSeats(tripId: String, trainClass: String): List<List<Seat>> {
    val seats = mutableListOf<List<Seat>>()
    
    val (coaches, seatsPerCoach, layout) = when (trainClass) {
        "First Class" -> Triple(1, 8, Pair(2, 2))      // Gerbong 1: 2+2, 8 baris
        "Business" -> Triple(2, 10, Pair(2, 2))        // Gerbong 2-3: 2+2, 10 baris
        "Premium Economy" -> Triple(2, 12, Pair(3, 2)) // Gerbong 4-5: 3+2, 12 baris
        else -> Triple(3, 12, Pair(3, 2))              // Gerbong 6+: 3+2, 12 baris
    }
    
    for (coach in 1..coaches) {
        val coachSeats = mutableListOf<Seat>()
        for (row in 1..seatsPerCoach) {
            for (col in 1..layout.first + layout.second) {
                val seatCode = "${coach}${('A' + col - 1)}"
                val seatClass = when (coach) {
                    1 -> SeatClass.FIRST_CLASS
                    2, 3 -> SeatClass.BUSINESS_CLASS
                    4, 5 -> SeatClass.PREMIUM_ECONOMY
                    else -> SeatClass.ECONOMY
                }
                coachSeats.add(Seat(seatCode, row, col, false, seatClass))
            }
        }
        seats.add(coachSeats)
    }
    
    return seats
}
```

**Status**: 🔧 IN PROGRESS

---

## 📋 SUMMARY OF FIXES

| # | Issue | Status | Priority |
|---|-------|--------|----------|
| 1 | Server Config Subtitle | ✅ FIXED | High |
| 2 | User Not in Admin | 🔧 IN PROGRESS | Critical |
| 3 | Ticket Not in Admin | 🔧 IN PROGRESS | Critical |
| 4 | Refund Not Working | 🔧 IN PROGRESS | Critical |
| 5 | Sync Firebase 404 | 🔧 IN PROGRESS | High |
| 6 | Broadcast Notification | 🔧 IN PROGRESS | High |
| 7 | Cleartext Traffic | ✅ FIXED | Medium |
| 8 | Gerbong Classification | 🔧 IN PROGRESS | Medium |

---

**Last Updated**: 2026-05-26
**Next Steps**: Implement all fixes and test end-to-end flows
