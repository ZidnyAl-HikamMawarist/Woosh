<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Ticket;
use App\Models\Trip;
use App\Models\Station;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class ApiEndpointsTest extends TestCase
{
    use RefreshDatabase;

    protected function setUp(): void
    {
        parent::setUp();
        // Create stations for foreign key constraints
        Station::create(['id' => 1, 'name' => 'Jakarta', 'city' => 'Jakarta']);
        Station::create(['id' => 2, 'name' => 'Surabaya', 'city' => 'Surabaya']);
    }

    /**
     * Test 1: Sync User API
     */
    public function test_sync_user_api()
    {
        $response = $this->postJson('/api/v1/sync-user', [
            'firebase_uid' => 'test-uid-' . time(),
            'name' => 'Test User',
            'email' => 'test-' . time() . '@example.com',
            'phone' => '08123456789',
        ]);

        $response->assertStatus(200 || 201);
        $response->assertJsonStructure(['status', 'message']);
    }

    /**
     * Test 2: Get Trips API
     */
    public function test_get_trips_api()
    {
        // Create a trip
        Trip::create([
            'trip_id' => 'TRIP-' . time(),
            'train_name' => 'Test Train',
            'departure_station_id' => 1,
            'arrival_station_id' => 2,
            'departure_time' => now()->addHours(2),
            'arrival_time' => now()->addHours(8),
            'train_class' => 'Economy',
            'base_price' => 250000,
            'carriages_count' => 10,
        ]);

        $response = $this->getJson('/api/v1/trips');
        $response->assertStatus(200);
        $response->assertJsonStructure(['status', 'data']);
    }

    /**
     * Test 3: Update Profile API
     */
    public function test_update_profile_api()
    {
        // Create user first
        $user = User::create([
            'name' => 'Original Name',
            'email' => 'profile-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        $response = $this->postJson('/api/v1/update-profile', [
            'email' => $user->email,
            'name' => 'Updated Name',
            'phone' => '08987654321',
        ]);

        $response->assertStatus(200);
        $response->assertJsonStructure(['status', 'message', 'data']);
    }

    /**
     * Test 4: Get User Tickets API
     */
    public function test_get_user_tickets_api()
    {
        // Create user
        $user = User::create([
            'name' => 'Ticket User',
            'email' => 'tickets-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        // Create trip
        $trip = Trip::create([
            'trip_id' => 'TRIP-' . time(),
            'train_name' => 'Test Train',
            'departure_station_id' => 1,
            'arrival_station_id' => 2,
            'departure_time' => now()->addHours(2),
            'arrival_time' => now()->addHours(8),
            'train_class' => 'Economy',
            'base_price' => 250000,
            'carriages_count' => 10,
        ]);

        // Create ticket
        Ticket::create([
            'user_id' => $user->id,
            'trip_id' => $trip->trip_id,
            'ticket_code' => 'WSH-TK-' . time(),
            'seats' => 'A1',
            'total_price' => 250000,
            'status' => 'Active',
            'payment_status' => 'paid',
        ]);

        $response = $this->postJson('/api/v1/user-tickets', [
            'email' => $user->email,
        ]);

        $response->assertStatus(200);
        $response->assertJsonStructure(['status', 'data']);
    }

    /**
     * Test 5: Validate Ticket API
     */
    public function test_validate_ticket_api()
    {
        // Create user
        $user = User::create([
            'name' => 'Validate User',
            'email' => 'validate-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        // Create trip
        $trip = Trip::create([
            'trip_id' => 'TRIP-' . time(),
            'train_name' => 'Test Train',
            'departure_station_id' => 1,
            'arrival_station_id' => 2,
            'departure_time' => now()->addHours(2),
            'arrival_time' => now()->addHours(8),
            'train_class' => 'Economy',
            'base_price' => 250000,
            'carriages_count' => 10,
        ]);

        // Create ticket
        $ticket = Ticket::create([
            'user_id' => $user->id,
            'trip_id' => $trip->trip_id,
            'ticket_code' => 'WSH-TK-' . time(),
            'seats' => 'A1',
            'total_price' => 250000,
            'status' => 'Active',
            'payment_status' => 'paid',
        ]);

        $response = $this->postJson('/api/v1/validate-ticket', [
            'ticket_code' => $ticket->ticket_code,
        ]);

        $response->assertStatus(200);
        $response->assertJsonStructure(['status', 'message', 'ticket']);
    }

    /**
     * Test 6: Refund Ticket API
     */
    public function test_refund_ticket_api()
    {
        // Create user
        $user = User::create([
            'name' => 'Refund User',
            'email' => 'refund-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        // Create trip
        $trip = Trip::create([
            'trip_id' => 'TRIP-' . time(),
            'train_name' => 'Test Train',
            'departure_station_id' => 1,
            'arrival_station_id' => 2,
            'departure_time' => now()->addHours(2),
            'arrival_time' => now()->addHours(8),
            'train_class' => 'Economy',
            'base_price' => 250000,
            'carriages_count' => 10,
        ]);

        // Create ticket
        $ticket = Ticket::create([
            'user_id' => $user->id,
            'trip_id' => $trip->trip_id,
            'ticket_code' => 'WSH-TK-' . time(),
            'seats' => 'A1',
            'total_price' => 250000,
            'status' => 'Active',
            'payment_status' => 'paid',
        ]);

        $response = $this->postJson('/api/v1/refund-ticket', [
            'ticket_code' => $ticket->ticket_code,
            'email' => $user->email,
            'reason' => 'Change of plans',
        ]);

        $response->assertStatus(200);
        $response->assertJsonStructure(['status', 'message', 'ticket']);
    }

    /**
     * Test 7: Database Integrity
     */
    public function test_database_integrity()
    {
        // Verify users table
        $user = User::create([
            'name' => 'DB Test User',
            'email' => 'db-test-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        $this->assertDatabaseHas('users', [
            'id' => $user->id,
            'email' => $user->email,
        ]);
    }

    /**
     * Test 8: Models Functionality
     */
    public function test_models_functionality()
    {
        // Test User model
        $user = User::create([
            'name' => 'Model Test',
            'email' => 'model-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        $this->assertNotNull($user->id);
        $this->assertEquals('Model Test', $user->name);

        // Test Trip model
        $trip = Trip::create([
            'trip_id' => 'TRIP-' . time(),
            'train_name' => 'Model Test Train',
            'departure_station_id' => 1,
            'arrival_station_id' => 2,
            'departure_time' => now()->addHours(2),
            'arrival_time' => now()->addHours(8),
            'train_class' => 'Economy',
            'base_price' => 250000,
            'carriages_count' => 10,
        ]);

        $this->assertNotNull($trip->trip_id);
        $this->assertEquals('Model Test Train', $trip->train_name);
    }
}
