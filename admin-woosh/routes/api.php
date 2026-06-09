<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

use App\Http\Controllers\Api\ApiController;

Route::get('/user', function (Request $request) {
    return $request->user();
})->middleware('auth:sanctum');

Route::prefix('v1')->group(function () {
    // Endpoints for Mobile App
    Route::get('/trips', [ApiController::class, 'getTrips']);
    Route::post('/sync-user', [ApiController::class, 'syncUserToMySQL']);
    Route::post('/book-ticket', [ApiController::class, 'bookTicket']);
    Route::post('/user-tickets', [ApiController::class, 'getUserTickets']);
    Route::post('/refund-ticket', [ApiController::class, 'refundTicket']);
    Route::post('/update-profile', [ApiController::class, 'updateProfile']);
    Route::post('/validate-ticket', [ApiController::class, 'validateTicket']);
});
