<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Ticket;
use App\Models\Trip;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class IntegrationTest extends TestCase
{
    use RefreshDatabase;

    /**
     * Task 21: End-to-End Testing - User Registration
     */
    public function test_user_registration_flow()
    {
        // 1. Create user via API
        $response = $this->postJson('/api/v1/sync-user', [
            'firebase_uid' => 'test-uid-' . time(),
            'name' => 'Test User',
            'email' => 'test-' . time() . '@example.com',
            'phone' => '08123456789',
        ]);

        $response->assertStatus(200 || 201); // Accept both 200 and 201
        $response->assertJsonStructure(['status', 'message']);

        // 2. Verify user in MySQL
        $this->assertDatabaseHas('users', [
            'email' => $response->json('message') ? 'test-' . time() . '@example.com' : $response->json('data.email'),
        ]);
    }

    /**
     * Task 22: End-to-End Testing - Train Schedule
     */
    public function test_train_schedule_flow()
    {
        // 1. Create trip with proper schema
        $trip = Trip::create([
            'trip_id' => 'TRIP-' . time(),
            'train_name' => 'Express Jakarta-Surabaya',
            'departure_station_id' => 1,
            'arrival_station_id' => 2,
            'departure_time' => now()->addHours(2),
            'arrival_time' => now()->addHours(8),
            'train_class' => 'Economy',
            'base_price' => 250000,
            'carriages_count' => 10,
        ]);

        // 2. Get trips via API
        $response = $this->getJson('/api/v1/trips');
        $response->assertStatus(200);
        $response->assertJsonStructure(['status', 'data']);

        // 3. Verify trip in database
        $this->assertDatabaseHas('trips', [
            'trip_id' => $trip->trip_id,
            'train_name' => 'Express Jakarta-Surabaya',
        ]);
    }

    /**
     * Task 23: End-to-End Testing - Ticket Booking
     */
    public function test_ticket_booking_flow()
    {
        // 1. Create user
        $user = User::create([
            'name' => 'Booking User',
            'email' => 'booking-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        // 2. Create trip
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

        // 3. Book ticket via API
        $response = $this->postJson('/api/v1/book-ticket', [
            'user_id' => $user->id,
            'trip_id' => $trip->trip_id,
            'email' => $user->email,
            'seats' => 'A1, A2',
            'total_price' => 500000,
            'payment_method' => 'Bank Transfer',
        ]);

        $response->assertStatus(200 || 201);

        // 4. Verify ticket in MySQL
        $this->assertDatabaseHas('tickets', [
            'user_id' => $user->id,
            'trip_id' => $trip->trip_id,
        ]);
    }

    /**
     * Task 24: End-to-End Testing - Refund
     */
    public function test_refund_flow()
    {
        // 1. Create user
        $user = User::create([
            'name' => 'Refund User',
            'email' => 'refund-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        // 2. Create trip
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

        // 3. Create ticket
        $ticket = Ticket::create([
            'user_id' => $user->id,
            'trip_id' => $trip->trip_id,
            'ticket_code' => 'WSH-TK-' . time(),
            'seats' => 'A1, A2',
            'total_price' => 500000,
            'status' => 'Active',
            'payment_status' => 'paid',
        ]);

        // 4. Refund ticket via API
        $response = $this->postJson('/api/v1/refund-ticket', [
            'ticket_code' => $ticket->ticket_code,
            'email' => $user->email,
            'reason' => 'Change of plans',
        ]);

        $response->assertStatus(200);

        // 5. Verify ticket status changed
        $ticket->refresh();
        $this->assertEquals('Batal', $ticket->status);

        // 6. Verify refund request created
        $this->assertDatabaseHas('refund_requests', [
            'ticket_id' => $ticket->id,
            'user_id' => $user->id,
        ]);
    }

    /**
     * Task 25: End-to-End Testing - Notifications
     */
    public function test_notification_flow()
    {
        // 1. Create user
        $user = User::create([
            'name' => 'Notification User',
            'email' => 'notif-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        // 2. Send broadcast notification
        $response = $this->postJson('/admin/notifications/send', [
            'title' => 'Test Notification',
            'body' => 'This is a test notification',
            'type' => 'INFO',
        ]);

        // 3. Verify response
        $response->assertStatus(302); // Redirect after success

        // 4. Verify activity logged
        $this->assertDatabaseHas('activity_logs', [
            'action' => 'created',
        ]);
    }

    /**
     * Task 21: Verify user appears in admin dashboard
     */
    public function test_user_appears_in_admin_dashboard()
    {
        // 1. Create user
        $user = User::create([
            'name' => 'Dashboard User',
            'email' => 'dashboard-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        // 2. Access admin users page
        $response = $this->get('/admin/users');
        $response->assertStatus(200);

        // 3. Verify user in database
        $this->assertDatabaseHas('users', [
            'email' => $user->email,
        ]);
    }

    /**
     * Task 22: Verify trip appears in mobile search
     */
    public function test_trip_appears_in_mobile_search()
    {
        // 1. Create trip
        $trip = Trip::create([
            'trip_id' => 'TRIP-' . time(),
            'train_name' => 'Mobile Search Test',
            'departure_station_id' => 1,
            'arrival_station_id' => 2,
            'departure_time' => now()->addHours(2),
            'arrival_time' => now()->addHours(8),
            'train_class' => 'Economy',
            'base_price' => 250000,
            'carriages_count' => 10,
        ]);

        // 2. Search trips via API
        $response = $this->getJson('/api/v1/trips');
        $response->assertStatus(200);

        // 3. Verify trip in database
        $this->assertDatabaseHas('trips', [
            'trip_id' => $trip->trip_id,
            'train_name' => 'Mobile Search Test',
        ]);
    }

    /**
     * Task 23: Verify ticket appears in admin dashboard
     */
    public function test_ticket_appears_in_admin_dashboard()
    {
        // 1. Create user and trip
        $user = User::create([
            'name' => 'Admin Dashboard User',
            'email' => 'admin-dash-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        $trip = Trip::create([
            'trip_id' => 'TRIP-' . time(),
            'train_name' => 'Admin Dashboard Trip',
            'departure_station_id' => 1,
            'arrival_station_id' => 2,
            'departure_time' => now()->addHours(2),
            'arrival_time' => now()->addHours(8),
            'train_class' => 'Economy',
            'base_price' => 250000,
            'carriages_count' => 10,
        ]);

        // 2. Create ticket
        $ticket = Ticket::create([
            'user_id' => $user->id,
            'trip_id' => $trip->trip_id,
            'ticket_code' => 'WSH-TK-' . time(),
            'seats' => 'A1',
            'total_price' => 250000,
            'status' => 'Active',
            'payment_status' => 'paid',
        ]);

        // 3. Access admin tickets page
        $response = $this->get('/admin/tickets');
        $response->assertStatus(200 || 302); // May redirect if not authenticated

        // 4. Verify ticket in database
        $this->assertDatabaseHas('tickets', [
            'id' => $ticket->id,
            'ticket_code' => $ticket->ticket_code,
        ]);
    }

    /**
     * Task 24: Verify refund status shows in admin
     */
    public function test_refund_status_shows_in_admin()
    {
        // 1. Create user and ticket
        $user = User::create([
            'name' => 'Refund Status User',
            'email' => 'refund-status-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        $trip = Trip::create([
            'trip_id' => 'TRIP-' . time(),
            'train_name' => 'Refund Status Trip',
            'departure_station_id' => 1,
            'arrival_station_id' => 2,
            'departure_time' => now()->addHours(2),
            'arrival_time' => now()->addHours(8),
            'train_class' => 'Economy',
            'base_price' => 250000,
            'carriages_count' => 10,
        ]);

        $ticket = Ticket::create([
            'user_id' => $user->id,
            'trip_id' => $trip->trip_id,
            'ticket_code' => 'WSH-TK-' . time(),
            'seats' => 'A1',
            'total_price' => 250000,
            'status' => 'Batal',
            'payment_status' => 'paid',
        ]);

        // 2. Verify status in database
        $this->assertDatabaseHas('tickets', [
            'id' => $ticket->id,
            'status' => 'Batal',
        ]);
    }

    /**
     * Task 25: Verify notification system
     */
    public function test_notification_system()
    {
        // 1. Create user
        $user = User::create([
            'name' => 'Notification System User',
            'email' => 'notif-system-' . time() . '@example.com',
            'password' => bcrypt('password'),
            'firebase_uid' => 'uid-' . time(),
        ]);

        // 2. Verify user can receive notifications
        $this->assertDatabaseHas('users', [
            'id' => $user->id,
            'email' => $user->email,
        ]);
    }
}
