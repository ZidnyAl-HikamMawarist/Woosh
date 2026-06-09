<?php

use App\Http\Controllers\ProfileController;
use App\Http\Controllers\Admin\AdminController;
use App\Http\Controllers\Admin\FirebaseController;
use App\Http\Controllers\Admin\ReportController;
use App\Http\Controllers\Admin\NotificationController;
use Illuminate\Support\Facades\Route;

// Redirect root to admin dashboard
Route::get('/', function () {
    return redirect()->route('admin.dashboard');
});

// Profile Routes (Breeze)
Route::middleware('auth')->group(function () {
    Route::get('/profile', [ProfileController::class, 'edit'])->name('profile.edit');
    Route::patch('/profile', [ProfileController::class, 'update'])->name('profile.update');
    Route::delete('/profile', [ProfileController::class, 'destroy'])->name('profile.destroy');
});

// Admin & Manager Routes (View Only + Reports)
Route::middleware(['auth', 'role:admin,manager'])->prefix('admin')->name('admin.')->group(function () {
    Route::get('/', [AdminController::class, 'dashboard'])->name('dashboard');
    Route::get('/trips', [AdminController::class, 'trips'])->name('trips');
    Route::get('/tickets', [AdminController::class, 'tickets'])->name('tickets');
    Route::get('/users', [AdminController::class, 'users'])->name('users');
    Route::get('/notifications', [NotificationController::class, 'index'])->name('notifications');
    Route::get('/reports/tickets/pdf', [ReportController::class, 'exportTicketsPdf'])->name('reports.tickets.pdf');
    Route::get('/stations', [AdminController::class, 'stations'])->name('stations');

    // Super Admin Only Routes (Modifications)
    Route::middleware('role:admin')->group(function () {
        // Stations CRUD
        Route::get('/stations/create', [AdminController::class, 'createStation'])->name('stations.create');
        Route::post('/stations', [AdminController::class, 'storeStation'])->name('stations.store');
        Route::get('/stations/{station}/edit', [AdminController::class, 'editStation'])->name('stations.edit');
        Route::put('/stations/{station}', [AdminController::class, 'updateStation'])->name('stations.update');
        Route::delete('/stations/{station}', [AdminController::class, 'destroyStation'])->name('stations.destroy');

        // Trips CRUD
        Route::get('/trips/create', [AdminController::class, 'createTrip'])->name('trips.create');
        Route::post('/trips', [AdminController::class, 'storeTrip'])->name('trips.store');
        Route::get('/trips/{trip}/edit', [AdminController::class, 'editTrip'])->name('trips.edit');
        Route::put('/trips/{trip}', [AdminController::class, 'updateTrip'])->name('trips.update');
        Route::delete('/trips/{trip}', [AdminController::class, 'destroyTrip'])->name('trips.destroy');

        // Tickets CRUD
        Route::get('/tickets/create', [AdminController::class, 'createTicket'])->name('tickets.create');
        Route::post('/tickets', [AdminController::class, 'storeTicket'])->name('tickets.store');
        Route::get('/tickets/{ticket}/edit', [AdminController::class, 'editTicket'])->name('tickets.edit');
        Route::put('/tickets/{ticket}', [AdminController::class, 'updateTicket'])->name('tickets.update');
        Route::delete('/tickets/{ticket}', [AdminController::class, 'destroyTicket'])->name('tickets.destroy');
        Route::post('/tickets/{ticket}/refund', [AdminController::class, 'refundTicket'])->name('tickets.refund');
        Route::post('/tickets/{ticket}/validate', [AdminController::class, 'validateTicket'])->name('tickets.validate');

        // Users CRUD
        Route::get('/users/create', [AdminController::class, 'createUser'])->name('users.create');
        Route::post('/users', [AdminController::class, 'storeUser'])->name('users.store');
        Route::get('/users/{user}/edit', [AdminController::class, 'editUser'])->name('users.edit');
        Route::put('/users/{user}', [AdminController::class, 'updateUser'])->name('users.update');
        Route::delete('/users/{user}', [AdminController::class, 'destroyUser'])->name('users.destroy');

        // Notifications Send
        Route::post('/notifications/send', [NotificationController::class, 'sendBroadcast'])->name('notifications.send');
        
        // Audit Logs
        Route::get('/logs', [AdminController::class, 'logs'])->name('logs');

        // Refund Requests
        Route::get('/refunds', [AdminController::class, 'refunds'])->name('refunds');
        Route::post('/refunds/{refund}/process', [AdminController::class, 'processRefund'])->name('refunds.process');
    });

    // Firebase Utility — accessible by admin and manager for sync
    Route::get('/sync-firebase', [FirebaseController::class, 'syncToMysql'])->name('sync.firebase');
    Route::get('/test-firebase', [FirebaseController::class, 'testConnection'])->name('test.firebase');
});

// Override Breeze Dashboard to redirect to Admin Dashboard
Route::get('/dashboard', function () {
    return redirect()->route('admin.dashboard');
})->middleware(['auth', 'verified'])->name('dashboard');

// Payment Routes
Route::post('/payments/notification', [App\Http\Controllers\PaymentController::class, 'handleNotification'])->name('payments.notification');
Route::post('/payments/simulate/{ticket}', [App\Http\Controllers\PaymentController::class, 'simulatePayment'])->name('payments.simulate');

// Authentication Routes
require __DIR__.'/auth.php';
