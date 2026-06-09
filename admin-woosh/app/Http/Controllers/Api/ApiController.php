<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Trip;
use App\Models\User;
use App\Models\Ticket;
use App\Models\ActivityLog;
use App\Models\RefundRequest;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;

class ApiController extends Controller
{
    // Ambil daftar jadwal kereta dengan filter dan seat availability
    public function getTrips(Request $request)
    {
        // Validate optional filters
        $request->validate([
            'date'      => 'nullable|date_format:Y-m-d',
            'departure' => 'nullable|string',
            'arrival'   => 'nullable|string',
        ]);

        $query = Trip::query();

        // Filter by date if provided
        if ($request->has('date') && $request->date) {
            $date = $request->date; // Format YYYY-MM-DD
            $query->whereDate('departure_time', $date);
        }

        // Filter by departure station if provided
        if ($request->has('departure') && $request->departure) {
            $departure = $request->departure;
            $query->whereHas('departureStation', function ($q) use ($departure) {
                $q->where('code', 'like', "%{$departure}%")
                  ->orWhere('name', 'like', "%{$departure}%")
                  ->orWhere('city', 'like', "%{$departure}%");
            });
        }

        // Filter by arrival station if provided
        if ($request->has('arrival') && $request->arrival) {
            $arrival = $request->arrival;
            $query->whereHas('arrivalStation', function ($q) use ($arrival) {
                $q->where('code', 'like', "%{$arrival}%")
                  ->orWhere('name', 'like', "%{$arrival}%")
                  ->orWhere('city', 'like', "%{$arrival}%");
            });
        }

        // Get trips with related station data
        $trips = $query->with(['departureStation', 'arrivalStation'])
                       ->orderBy('departure_time', 'asc')
                       ->get();

        // Enrich trips with seat availability from local database
        $enrichedTrips = $trips->map(function (\App\Models\Trip $trip) {
            $tripData = $trip->toArray();
            
            $tripData['seat_availability'] = [
                'booked_seats' => $trip->booked_seats ?? [],
                'total_seats' => $trip->carriages_count * 80, // Assuming 80 seats per carriage
                'available_seats' => ($trip->carriages_count * 80) - count($trip->booked_seats ?? []),
            ];
            
            return $tripData;
        });

        return response()->json([
            'status' => 'success',
            'data' => $enrichedTrips,
            'count' => $enrichedTrips->count(),
        ]);
    }

    /**
     * Get seat availability from Firestore for a specific trip
     */
    private function getSeatAvailabilityFromFirestore(string $tripId): array
    {
        try {
            $url = "https://firestore.googleapis.com/v1/projects/" . env('FIREBASE_PROJECT_ID', 'wooshh-43000') . "/databases/(default)/documents/trips/{$tripId}";
            
            $firebaseSync = new \App\Services\FirebaseSyncService();
            
            // Use reflection to access private method getAccessToken
            $reflection = new \ReflectionClass($firebaseSync);
            $method = $reflection->getMethod('getAccessToken');
            $method->setAccessible(true);
            $accessToken = $method->invoke($firebaseSync);
            
            $response = \Illuminate\Support\Facades\Http::withToken($accessToken)->get($url);
            
            if ($response->successful()) {
                $data = $response->json();
                $fields = $data['fields'] ?? [];
                
                // Extract booked seats from Firestore
                $bookedSeats = [];
                if (isset($fields['bookedSeats']['arrayValue']['values'])) {
                    $bookedSeats = array_map(function ($val) {
                        return $val['stringValue'] ?? $val['integerValue'] ?? null;
                    }, $fields['bookedSeats']['arrayValue']['values']);
                    $bookedSeats = array_filter($bookedSeats);
                }
                
                $totalSeats = (int) ($fields['totalSeats']['integerValue'] ?? 320); // Default 4 carriages * 80 seats
                $availableSeats = $totalSeats - count($bookedSeats);
                
                return [
                    'booked_seats' => $bookedSeats,
                    'total_seats' => $totalSeats,
                    'available_seats' => max(0, $availableSeats),
                ];
            }
            
            // If not found in Firestore, return empty availability
            return [
                'booked_seats' => [],
                'total_seats' => 320,
                'available_seats' => 320,
            ];
        } catch (\Throwable $e) {
            \Illuminate\Support\Facades\Log::error(
                "[GetTrips] Error fetching seat availability from Firestore: " . $e->getMessage()
            );
            throw $e;
        }
    }

    // Sinkronisasi data user dari Firebase ke MySQL
    public function syncUserToMySQL(Request $request)
    {
        // Validate required fields
        $request->validate([
            'firebase_uid' => 'required|string',
            'email' => 'required|email',
            'name' => 'required|string',
            'phone' => 'nullable|string',
        ]);

        // Check if user exists
        $user = User::where('firebase_uid', $request->firebase_uid)->first();
        
        if ($user) {
            // Update existing user
            $user->update([
                'name'  => $request->name,
                'email' => $request->email,
                'phone' => $request->phone ?? $user->phone,
            ]);
            
            // Log activity
            ActivityLog::create([
                'user_id' => $user->id,
                'action' => 'updated',
                'model' => User::class,
                'model_id' => $user->id,
                'details' => json_encode([
                    'firebase_uid' => $request->firebase_uid,
                    'name' => $request->name,
                    'email' => $request->email,
                    'phone' => $request->phone,
                ]),
                'ip_address' => $request->ip(),
            ]);
            
            return response()->json([
                'status' => 'success',
                'message' => 'User updated successfully',
                'data' => $user
            ], 200);
        } else {
            // Create new user
            $user = User::create([
                'firebase_uid' => $request->firebase_uid,
                'name'         => $request->name,
                'email'        => $request->email,
                'phone'        => $request->phone,
                'password'     => Hash::make(Str::random(16)),
                'role'         => 'user',
            ]);
            
            // Log activity
            ActivityLog::create([
                'user_id' => $user->id,
                'action' => 'created',
                'model' => User::class,
                'model_id' => $user->id,
                'details' => json_encode([
                    'firebase_uid' => $request->firebase_uid,
                    'name' => $request->name,
                    'email' => $request->email,
                    'phone' => $request->phone,
                ]),
                'ip_address' => $request->ip(),
            ]);
            
            return response()->json([
                'status' => 'success',
                'message' => 'User created successfully',
                'data' => $user
            ], 201);
        }
    }

    // Sinkronisasi data user dari Firebase ke MySQL (backward compatibility)
    public function syncUser(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'name' => 'required|string',
            'uid' => 'nullable|string' // Firebase UID
        ]);

        $user = User::updateOrCreate(
            ['email' => $request->email],
            [
                'name'         => $request->name,
                'password'     => Hash::make(Str::random(16)),
                'phone'        => $request->phone ?? null,
                'firebase_uid' => $request->uid ?? null, // simpan Firebase UID
            ]
        );

        return response()->json([
            'status' => 'success',
            'message' => 'User synced successfully',
            'data' => $user
        ]);
    }

    // Simpan data transaksi tiket dari Mobile
    public function bookTicket(Request $request)
    {
        $request->validate([
            'user_id'             => 'nullable|integer|exists:users,id',
            'trip_id'             => 'required|string',
            'email'               => 'required|email',
            'seats'               => 'required|array',
            'seats.*'             => 'string',
            'total_price'         => 'required|numeric|min:0',
            'ticket_code'         => 'nullable|string',
            'firestore_ticket_id' => 'nullable|string',
            'payment_method'      => 'nullable|string',
        ]);

        try {
            // Cari atau buat user jika belum ada di MySQL
            $user = User::firstOrCreate(
                ['email' => $request->email],
                [
                    'name'     => $request->name ?? explode('@', $request->email)[0],
                    'password' => Hash::make(Str::random(16)),
                    'role'     => 'user',
                ]
            );

            // Cari trip — jika tidak ada, return error
            $trip = Trip::find($request->trip_id);
            if (!$trip) {
                return response()->json([
                    'status'  => 'error',
                    'message' => "Trip ID {$request->trip_id} not found in MySQL.",
                ], 422);
            }

            // Generate ticket code jika tidak disediakan
            $ticketCode = $request->ticket_code ?? 'TKT' . strtoupper(Str::random(10));

            // Cek duplikat ticket_code
            if (Ticket::where('ticket_code', $ticketCode)->exists()) {
                return response()->json([
                    'status'  => 'error',
                    'message' => 'Ticket code already exists',
                ], 409);
            }

            // Convert seats array to comma-separated string
            $seatsList = implode(',', $request->seats);

            // Create ticket in MySQL with status "Active"
            $ticket = Ticket::create([
                'ticket_code'         => $ticketCode,
                'firestore_ticket_id' => $request->firestore_ticket_id ?? $ticketCode,
                'trip_id'             => $request->trip_id,
                'user_id'             => $user->id,
                'seats_list'          => $seatsList,
                'total_amount'        => $request->total_price,
                'payment_method'      => $request->payment_method ?? 'Mobile App',
                'payment_status'      => 'paid',
                'status'              => 'Aktif',
                'booked_at'           => now(),
            ]);

            // Update seat availability in MySQL
            $currentBooked = $trip->booked_seats ?? [];
            $trip->booked_seats = array_values(array_unique(array_merge($currentBooked, $request->seats)));
            $trip->save();

            // Update seat availability in Firestore
            try {
                $this->updateSeatAvailabilityInFirestore($request->trip_id, $request->seats);
            } catch (\Throwable $e) {
                \Illuminate\Support\Facades\Log::warning(
                    "[BookTicket] Failed to update seat availability in Firestore: " . $e->getMessage()
                );
                // Don't fail the request if Firestore update fails
            }

            // Create ticket in Firestore under users/{uid}/tickets
            if ($user->firebase_uid) {
                try {
                    $this->createTicketInFirestore($user->firebase_uid, $ticket);
                } catch (\Throwable $e) {
                    \Illuminate\Support\Facades\Log::warning(
                        "[BookTicket] Failed to create ticket in Firestore: " . $e->getMessage()
                    );
                    // Don't fail the request if Firestore creation fails
                }
            }

            // Send confirmation email
            try {
                $this->sendBookingConfirmationEmail($user, $ticket, $trip);
            } catch (\Throwable $e) {
                \Illuminate\Support\Facades\Log::warning(
                    "[BookTicket] Failed to send confirmation email: " . $e->getMessage()
                );
                // Don't fail the request if email fails
            }

            // Log activity
            ActivityLog::create([
                'user_id' => $user->id,
                'action' => 'created',
                'model' => Ticket::class,
                'model_id' => $ticket->ticket_code,
                'details' => json_encode([
                    'trip_id' => $request->trip_id,
                    'seats' => $request->seats,
                    'total_price' => $request->total_price,
                ]),
                'ip_address' => $request->ip(),
            ]);

            return response()->json([
                'status' => 'success',
                'message' => 'Ticket booked successfully',
                'data' => [
                    'ticket_code' => $ticket->ticket_code,
                    'firestore_ticket_id' => $ticket->firestore_ticket_id,
                    'trip_id' => $ticket->trip_id,
                    'seats' => $request->seats,
                    'total_price' => $ticket->total_amount,
                    'status' => $ticket->status,
                    'booked_at' => $ticket->booked_at,
                ]
            ], 201);
        } catch (\Throwable $e) {
            \Illuminate\Support\Facades\Log::error(
                "[BookTicket] Error booking ticket: " . $e->getMessage()
            );
            return response()->json([
                'status' => 'error',
                'message' => 'Failed to book ticket: ' . $e->getMessage(),
            ], 500);
        }
    }

    /**
     * Update seat availability in Firestore
     */
    private function updateSeatAvailabilityInFirestore(string $tripId, array $seats): void
    {
        try {
            $firebaseSync = new \App\Services\FirebaseSyncService();
            
            // Get current booked seats from Firestore
            $url = "https://firestore.googleapis.com/v1/projects/" . env('FIREBASE_PROJECT_ID', 'wooshh-43000') . "/databases/(default)/documents/trips/{$tripId}";
            
            $reflection = new \ReflectionClass($firebaseSync);
            $method = $reflection->getMethod('getAccessToken');
            $method->setAccessible(true);
            $accessToken = $method->invoke($firebaseSync);
            
            $response = \Illuminate\Support\Facades\Http::withToken($accessToken)->get($url);
            
            $bookedSeats = [];
            if ($response->successful()) {
                $data = $response->json();
                $fields = $data['fields'] ?? [];
                
                if (isset($fields['bookedSeats']['arrayValue']['values'])) {
                    $bookedSeats = array_map(function ($val) {
                        return $val['stringValue'] ?? $val['integerValue'] ?? null;
                    }, $fields['bookedSeats']['arrayValue']['values']);
                    $bookedSeats = array_filter($bookedSeats);
                }
            }
            
            // Add new seats to booked seats
            $updatedBookedSeats = array_unique(array_merge($bookedSeats, $seats));
            
            // Update Firestore with new booked seats
            $patchMethod = $reflection->getMethod('patchDocument');
            $patchMethod->setAccessible(true);
            $patchMethod->invoke($firebaseSync, 'trips', $tripId, [
                'bookedSeats' => array_values($updatedBookedSeats),
            ]);
            
            \Illuminate\Support\Facades\Log::info(
                "[BookTicket] Seat availability updated in Firestore for trip {$tripId}"
            );
        } catch (\Throwable $e) {
            \Illuminate\Support\Facades\Log::error(
                "[BookTicket] Failed to update seat availability: " . $e->getMessage()
            );
            throw $e;
        }
    }

    /**
     * Create ticket in Firestore under users/{uid}/tickets
     */
    private function createTicketInFirestore(string $firebaseUid, Ticket $ticket): void
    {
        try {
            $firebaseSync = new \App\Services\FirebaseSyncService();
            
            $reflection = new \ReflectionClass($firebaseSync);
            $setMethod = $reflection->getMethod('setDocument');
            $setMethod->setAccessible(true);
            
            $ticketData = [
                'ticketCode' => $ticket->ticket_code,
                'tripId' => $ticket->trip_id,
                'seats' => explode(',', $ticket->seats_list),
                'totalPrice' => (int) $ticket->total_amount,
                'status' => 'Aktif',
                'paymentMethod' => $ticket->payment_method,
                'bookedAt' => now()->toIso8601String(),
            ];
            
            $setMethod->invoke($firebaseSync, "users/{$firebaseUid}/tickets", $ticket->ticket_code, $ticketData);
            
            \Illuminate\Support\Facades\Log::info(
                "[BookTicket] Ticket created in Firestore for user {$firebaseUid}"
            );
        } catch (\Throwable $e) {
            \Illuminate\Support\Facades\Log::error(
                "[BookTicket] Failed to create ticket in Firestore: " . $e->getMessage()
            );
            throw $e;
        }
    }

    /**
     * Send booking confirmation email
     */
    private function sendBookingConfirmationEmail(User $user, Ticket $ticket, Trip $trip): void
    {
        try {
            $seats = explode(',', $ticket->seats_list);
            
            $emailContent = "
                <h2>Booking Confirmation</h2>
                <p>Dear {$user->name},</p>
                <p>Your ticket has been successfully booked!</p>
                <h3>Ticket Details:</h3>
                <ul>
                    <li><strong>Ticket Code:</strong> {$ticket->ticket_code}</li>
                    <li><strong>Train:</strong> {$trip->train_name}</li>
                    <li><strong>Departure:</strong> {$trip->departure_time}</li>
                    <li><strong>Arrival:</strong> {$trip->arrival_time}</li>
                    <li><strong>Seats:</strong> " . implode(', ', $seats) . "</li>
                    <li><strong>Total Price:</strong> Rp " . number_format((float) $ticket->total_amount, 0, ',', '.') . "</li>
                    <li><strong>Status:</strong> {$ticket->status}</li>
                </ul>
                <p>Thank you for booking with us!</p>
            ";
            
            \Illuminate\Support\Facades\Mail::raw($emailContent, function ($message) use ($user, $emailContent) {
                $message->to($user->email)
                        ->subject('Booking Confirmation - Woosh')
                        ->html($emailContent);
            });
            
            \Illuminate\Support\Facades\Log::info(
                "[BookTicket] Confirmation email sent to {$user->email}"
            );
        } catch (\Throwable $e) {
            \Illuminate\Support\Facades\Log::error(
                "[BookTicket] Failed to send email: " . $e->getMessage()
            );
            throw $e;
        }
    }

    // Ambil riwayat tiket user tertentu
    public function getUserTickets(Request $request)
    {
        $request->validate([
            'email' => 'required|email|exists:users,email'
        ]);

        $user = User::where('email', $request->email)->first();
        $tickets = Ticket::with('trip')->where('user_id', $user->id)->orderBy('booked_at', 'desc')->get();

        return response()->json([
            'status' => 'success',
            'data' => $tickets
        ]);
    }

    // Refund tiket dari Mobile — update status di MySQL
    public function refundTicket(Request $request)
    {
        $request->validate([
            'ticket_code' => 'required|string',
            'email'       => 'required|email',
            'reason'      => 'nullable|string',
        ]);

        $user = User::where('email', $request->email)->first();
        if (!$user) {
            return response()->json(['status' => 'error', 'message' => 'User not found'], 404);
        }

        $ticket = Ticket::where('ticket_code', $request->ticket_code)
            ->where('user_id', $user->id)
            ->first();

        if (!$ticket) {
            $ticket = Ticket::where('firestore_ticket_id', $request->ticket_code)
                ->where('user_id', $user->id)
                ->first();
        }

        if (!$ticket) {
            return response()->json(['status' => 'error', 'message' => 'Ticket not found'], 404);
        }

        // Update ticket status
        $ticket->update(['status' => 'Batal']);

        // Release seats in MySQL and Firestore
        if ($ticket->trip_id) {
            $seatsToRelease = explode(',', $ticket->seats_list);
            
            $trip = Trip::find($ticket->trip_id);
            if ($trip) {
                $currentBooked = $trip->booked_seats ?? [];
                $trip->booked_seats = array_values(array_diff($currentBooked, $seatsToRelease));
                $trip->save();
            }
            
            $this->releaseSeatsInFirestore($ticket->trip_id, $seatsToRelease);
        }

        // Create refund request record
        RefundRequest::create([
            'ticket_code' => $ticket->ticket_code,
            'user_id'     => $user->id,
            'reason'      => $request->reason ?? 'User requested refund',
            'amount'      => $ticket->total_amount,
            'status'      => 'Approved',
        ]);

        // Send refund notification email
        $this->sendRefundNotificationEmail($user, $ticket);

        // Update Firestore
        try {
            $firestore = app('firebase.firestore')->database();
            $firestore->collection('users')->document($user->firebase_uid)
                ->collection('tickets')->document($ticket->firestore_ticket_id ?? $ticket->ticket_code)
                ->update([
                    ['path' => 'status', 'value' => 'Batal'],
                    ['path' => 'refundedAt', 'value' => now()->toDateTimeString()]
                ]);
        } catch (\Exception $e) {
            \Log::warning('Firestore update failed for refund: ' . $e->getMessage());
        }

        return response()->json([
            'status'  => 'success',
            'message' => 'Ticket refunded successfully',
            'ticket'  => $ticket,
        ]);
    }

    private function releaseSeatsInFirestore(string $tripId, array $seats): void
    {
        try {
            $firestore = app('firebase.firestore')->database();
            $tripDoc = $firestore->collection('trips')->document($tripId)->snapshot();
            
            if ($tripDoc->exists()) {
                $bookedSeats = $tripDoc->get('bookedSeats') ?? [];
                $updatedSeats = array_diff($bookedSeats, $seats);
                
                $firestore->collection('trips')->document($tripId)
                    ->update([
                        ['path' => 'bookedSeats', 'value' => array_values($updatedSeats)]
                    ]);
            }
        } catch (\Exception $e) {
            \Log::warning('Failed to release seats in Firestore: ' . $e->getMessage());
        }
    }

    private function sendRefundNotificationEmail(User $user, Ticket $ticket): void
    {
        try {
            \Mail::send('emails.refund-notification', [
                'user'   => $user,
                'ticket' => $ticket,
            ], function ($message) use ($user) {
                $message->to($user->email)
                    ->subject('Refund Confirmation - WOOSH');
            });
        } catch (\Exception $e) {
            \Log::warning('Failed to send refund email: ' . $e->getMessage());
        }
    }

    // Update profil user dari Mobile — sync ke MySQL dan Firestore
    public function updateProfile(Request $request)
    {
        $request->validate([
            'user_id' => 'nullable|integer|exists:users,id',
            'email'   => 'nullable|email',
            'name'    => 'nullable|string|max:255',
            'phone'   => 'nullable|string|max:50',
            'address' => 'nullable|string|max:500',
        ]);

        // Find user by user_id or email
        $user = null;
        if ($request->has('user_id') && $request->user_id) {
            $user = User::find($request->user_id);
        } elseif ($request->has('email') && $request->email) {
            $user = User::where('email', $request->email)->first();
        }

        if (!$user) {
            return response()->json([
                'status'  => 'error',
                'message' => 'User not found',
            ], 404);
        }

        // Prepare update data
        $updateData = array_filter([
            'name'    => $request->name,
            'phone'   => $request->phone,
            'address' => $request->address,
        ], fn($value) => !is_null($value));

        // Update user in MySQL
        $user->update($updateData);

        // Sync to Firestore if user has firebase_uid
        if ($user->firebase_uid) {
            try {
                $firebaseSync = new \App\Services\FirebaseSyncService();
                $firebaseSync->syncUserPoints($user->firebase_uid, $user->toArray());
            } catch (\Throwable $e) {
                \Illuminate\Support\Facades\Log::warning(
                    "[UpdateProfile] Failed to sync to Firestore: " . $e->getMessage()
                );
                // Don't fail the request if Firestore sync fails
            }
        }

        // Log activity
        ActivityLog::create([
            'user_id' => $user->id,
            'action' => 'updated',
            'model' => User::class,
            'model_id' => $user->id,
            'details' => json_encode($updateData),
            'ip_address' => $request->ip(),
        ]);

        return response()->json([
            'status'  => 'success',
            'message' => 'Profile updated successfully',
            'data'    => $user,
        ], 200);
    }

    // Task 6: Validate ticket at station
    public function validateTicket(Request $request)
    {
        $request->validate([
            'ticket_code' => 'required|string',
        ]);

        $ticket = Ticket::where('ticket_code', $request->ticket_code)->first();
        if (!$ticket) {
            return response()->json(['status' => 'error', 'message' => 'Ticket not found'], 404);
        }

        // Update ticket status to "Used"
        $ticket->update(['status' => 'Used']);

        // Update Firestore
        try {
            $user = $ticket->user;
            if ($user && $user->firebase_uid) {
                $firestore = app('firebase.firestore')->database();
                $firestore->collection('users')->document($user->firebase_uid)
                    ->collection('tickets')->document($ticket->firestore_ticket_id ?? $ticket->ticket_code)
                    ->update([
                        ['path' => 'status', 'value' => 'Used'],
                        ['path' => 'validatedAt', 'value' => now()->toDateTimeString()]
                    ]);
            }
        } catch (\Exception $e) {
            \Log::warning('Firestore update failed for validation: ' . $e->getMessage());
        }

        // Log activity
        ActivityLog::create([
            'user_id' => auth()->id() ?? null,
            'action' => 'validated',
            'model' => Ticket::class,
            'model_id' => $ticket->ticket_code,
            'details' => json_encode(['ticket_code' => $ticket->ticket_code]),
            'ip_address' => $request->ip(),
        ]);

        return response()->json([
            'status'  => 'success',
            'message' => 'Ticket validated successfully',
            'ticket'  => $ticket,
        ]);
    }
}
