<?php

namespace Tests\Feature;

use Tests\TestCase;
use App\Models\User;
use App\Models\Trip;
use App\Models\Ticket;
use App\Models\Station;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Http;

class BookTicketApiTest extends TestCase
{
    use RefreshDatabase;

    protected function setUp(): void
    {
        parent::setUp();
        
        // Mock Mail to prevent actual email sending
        Mail::fake();
        
        // Mock HTTP requests to Firestore
        Http::fake([
            'firestore.googleapis.com/*' => Http::response(['fields' => []], 200),
        ]);
        
        // Create test stations
        $station1 = Station::create([
            'station_id' => 'STN001',
            'code' => 'CGK',
            'name' => 'Jakarta',
            'city' => 'Jakarta',
            'latitude' => -6.1275,
            'longitude' => 106.6537,
        ]);

        $station2 = Station::create([
            'station_id' => 'STN002',
            'code' => 'BDO',
            'name' => 'Bandung',
            'city' => 'Bandung',
            'latitude' => -6.9175,
            'longitude' => 107.6062,
        ]);

        // Create test trip
        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $station1->id,
            'arrival_station_id' => $station2->id,
            'departure_time' => now()->addHours(2),
            'arrival_time' => now()->addHours(4),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);
    }

    /**
     * Test successful ticket booking
     */
    public function test_book_ticket_success()
    {
        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'passenger@example.com',
            'seats' => ['A1', 'A2', 'A3'],
            'total_price' => 750000,
            'payment_method' => 'Credit Card',
        ]);

        // Debug: print response if it's an error
        if ($response->status() !== 201) {
            $this->fail('Expected 201 but got ' . $response->status() . ': ' . json_encode($response->json()));
        }

        $response->assertStatus(201)
                 ->assertJsonStructure([
                     'status',
                     'message',
                     'data' => [
                         'ticket_code',
                         'firestore_ticket_id',
                         'trip_id',
                         'seats',
                         'total_price',
                         'status',
                         'booked_at',
                     ]
                 ])
                 ->assertJson([
                     'status' => 'success',
                     'data' => [
                         'trip_id' => 'TRIP001',
                         'seats' => ['A1', 'A2', 'A3'],
                         'total_price' => 750000,
                         'status' => 'Aktif',
                     ]
                 ]);

        // Verify ticket was created in database
        $this->assertDatabaseHas('tickets', [
            'trip_id' => 'TRIP001',
            'status' => 'Aktif',
            'total_amount' => 750000,
        ]);

        // Verify user was created
        $this->assertDatabaseHas('users', [
            'email' => 'passenger@example.com',
        ]);
    }

    /**
     * Test ticket booking with custom ticket code
     */
    public function test_book_ticket_with_custom_code()
    {
        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'passenger@example.com',
            'seats' => ['B1', 'B2'],
            'total_price' => 500000,
            'ticket_code' => 'CUSTOM123',
        ]);

        $response->assertStatus(201)
                 ->assertJson([
                     'status' => 'success',
                     'data' => [
                         'ticket_code' => 'CUSTOM123',
                     ]
                 ]);

        $this->assertDatabaseHas('tickets', [
            'ticket_code' => 'CUSTOM123',
        ]);
    }

    /**
     * Test ticket booking with invalid trip
     */
    public function test_book_ticket_invalid_trip()
    {
        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'INVALID_TRIP',
            'email' => 'passenger@example.com',
            'seats' => ['A1'],
            'total_price' => 250000,
        ]);

        $response->assertStatus(422)
                 ->assertJson([
                     'status' => 'error',
                     'message' => 'Trip ID INVALID_TRIP not found in MySQL.',
                 ]);
    }

    /**
     * Test ticket booking with duplicate ticket code
     */
    public function test_book_ticket_duplicate_code()
    {
        // Create first ticket
        $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'passenger1@example.com',
            'seats' => ['A1'],
            'total_price' => 250000,
            'ticket_code' => 'DUP001',
        ]);

        // Try to create second ticket with same code
        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'passenger2@example.com',
            'seats' => ['A2'],
            'total_price' => 250000,
            'ticket_code' => 'DUP001',
        ]);

        $response->assertStatus(409)
                 ->assertJson([
                     'status' => 'error',
                     'message' => 'Ticket code already exists',
                 ]);
    }

    /**
     * Test ticket booking with missing required fields
     */
    public function test_book_ticket_missing_fields()
    {
        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            // Missing email
            'seats' => ['A1'],
            'total_price' => 250000,
        ]);

        $response->assertStatus(422)
                 ->assertJsonValidationErrors(['email']);
    }

    /**
     * Test ticket booking with invalid email
     */
    public function test_book_ticket_invalid_email()
    {
        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'invalid-email',
            'seats' => ['A1'],
            'total_price' => 250000,
        ]);

        $response->assertStatus(422)
                 ->assertJsonValidationErrors(['email']);
    }

    /**
     * Test ticket booking with negative price
     */
    public function test_book_ticket_negative_price()
    {
        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'passenger@example.com',
            'seats' => ['A1'],
            'total_price' => -100,
        ]);

        $response->assertStatus(422)
                 ->assertJsonValidationErrors(['total_price']);
    }

    /**
     * Test ticket booking creates activity log
     */
    public function test_book_ticket_creates_activity_log()
    {
        $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'passenger@example.com',
            'seats' => ['A1', 'A2'],
            'total_price' => 500000,
        ]);

        $user = User::where('email', 'passenger@example.com')->first();
        
        $this->assertDatabaseHas('activity_logs', [
            'user_id' => $user->id,
            'action' => 'created',
            'model' => Ticket::class,
        ]);
    }

    /**
     * Test ticket booking with existing user
     */
    public function test_book_ticket_existing_user()
    {
        // Create user first
        $user = User::create([
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'password' => bcrypt('password'),
            'role' => 'user',
        ]);

        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'john@example.com',
            'seats' => ['A1'],
            'total_price' => 250000,
        ]);

        $response->assertStatus(201)
                 ->assertJson([
                     'status' => 'success',
                 ]);

        // Verify only one user exists with this email
        $this->assertEquals(1, User::where('email', 'john@example.com')->count());
    }

    /**
     * Test ticket booking response includes all required fields
     */
    public function test_book_ticket_response_structure()
    {
        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'passenger@example.com',
            'seats' => ['A1', 'A2'],
            'total_price' => 500000,
        ]);

        $response->assertStatus(201)
                 ->assertJsonStructure([
                     'status',
                     'message',
                     'data' => [
                         'ticket_code',
                         'firestore_ticket_id',
                         'trip_id',
                         'seats',
                         'total_price',
                         'status',
                         'booked_at',
                     ]
                 ]);

        // Verify data values
        $data = $response->json('data');
        $this->assertNotEmpty($data['ticket_code']);
        $this->assertNotEmpty($data['firestore_ticket_id']);
        $this->assertEquals('TRIP001', $data['trip_id']);
        $this->assertEquals(['A1', 'A2'], $data['seats']);
        $this->assertEquals(500000, $data['total_price']);
        $this->assertEquals('Aktif', $data['status']);
        $this->assertNotEmpty($data['booked_at']);
    }

    /**
     * Test ticket booking with multiple seats
     */
    public function test_book_ticket_multiple_seats()
    {
        $seats = ['A1', 'A2', 'A3', 'A4', 'A5'];
        
        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'passenger@example.com',
            'seats' => $seats,
            'total_price' => 1250000,
        ]);

        $response->assertStatus(201)
                 ->assertJson([
                     'status' => 'success',
                     'data' => [
                         'seats' => $seats,
                     ]
                 ]);

        $ticket = Ticket::latest()->first();
        $this->assertEquals(implode(',', $seats), $ticket->seats_list);
    }

    /**
     * Test ticket booking with payment method
     */
    public function test_book_ticket_with_payment_method()
    {
        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'passenger@example.com',
            'seats' => ['A1'],
            'total_price' => 250000,
            'payment_method' => 'Bank Transfer',
        ]);

        $response->assertStatus(201);

        $ticket = Ticket::latest()->first();
        $this->assertEquals('Bank Transfer', $ticket->payment_method);
    }

    /**
     * Test ticket booking auto-generates ticket code
     */
    public function test_book_ticket_auto_generates_code()
    {
        $response = $this->postJson('/api/v1/book-ticket', [
            'trip_id' => 'TRIP001',
            'email' => 'passenger@example.com',
            'seats' => ['A1'],
            'total_price' => 250000,
        ]);

        $response->assertStatus(201);

        $data = $response->json('data');
        $this->assertStringStartsWith('TKT', $data['ticket_code']);
        $this->assertGreaterThan(3, strlen($data['ticket_code']));
    }
}
