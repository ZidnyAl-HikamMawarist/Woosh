<?php

namespace Tests\Feature\Api;

use App\Models\User;
use App\Models\ActivityLog;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class SyncUserApiTest extends TestCase
{
    use RefreshDatabase;

    /**
     * Test creating a new user via sync-user endpoint
     */
    public function test_sync_user_creates_new_user(): void
    {
        $payload = [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
        ];

        $response = $this->postJson('/api/v1/sync-user', $payload);

        $response->assertStatus(201)
            ->assertJson([
                'status' => 'success',
                'message' => 'User created successfully',
            ])
            ->assertJsonStructure([
                'status',
                'message',
                'data' => [
                    'id',
                    'firebase_uid',
                    'name',
                    'email',
                    'phone',
                ]
            ]);

        $this->assertDatabaseHas('users', [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
        ]);
    }

    /**
     * Test updating an existing user via sync-user endpoint
     */
    public function test_sync_user_updates_existing_user(): void
    {
        // Create initial user
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'Jane Doe',
            'email' => 'jane@example.com',
            'phone' => '08987654321',
        ];

        $response = $this->postJson('/api/v1/sync-user', $payload);

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'User updated successfully',
            ]);

        $this->assertDatabaseHas('users', [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'Jane Doe',
            'email' => 'jane@example.com',
            'phone' => '08987654321',
        ]);
    }

    /**
     * Test sync-user endpoint validates required fields
     */
    public function test_sync_user_validates_required_fields(): void
    {
        // Missing firebase_uid
        $response = $this->postJson('/api/v1/sync-user', [
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
        ]);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['firebase_uid']);

        // Missing email
        $response = $this->postJson('/api/v1/sync-user', [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'phone' => '08123456789',
        ]);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['email']);

        // Missing name
        $response = $this->postJson('/api/v1/sync-user', [
            'firebase_uid' => 'firebase_uid_123',
            'email' => 'john@example.com',
            'phone' => '08123456789',
        ]);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['name']);
    }

    /**
     * Test sync-user endpoint validates email format
     */
    public function test_sync_user_validates_email_format(): void
    {
        $payload = [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'invalid-email',
            'phone' => '08123456789',
        ];

        $response = $this->postJson('/api/v1/sync-user', $payload);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['email']);
    }

    /**
     * Test sync-user endpoint logs activity when creating user
     */
    public function test_sync_user_logs_activity_on_create(): void
    {
        $payload = [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
        ];

        $this->postJson('/api/v1/sync-user', $payload);

        $user = User::where('firebase_uid', 'firebase_uid_123')->first();

        $this->assertDatabaseHas('activity_logs', [
            'user_id' => $user->id,
            'action' => 'created',
            'model' => User::class,
            'model_id' => $user->id,
        ]);
    }

    /**
     * Test sync-user endpoint logs activity when updating user
     */
    public function test_sync_user_logs_activity_on_update(): void
    {
        // Create initial user
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'Jane Doe',
            'email' => 'jane@example.com',
            'phone' => '08987654321',
        ];

        $this->postJson('/api/v1/sync-user', $payload);

        $this->assertDatabaseHas('activity_logs', [
            'user_id' => $user->id,
            'action' => 'updated',
            'model' => User::class,
            'model_id' => $user->id,
        ]);
    }

    /**
     * Test sync-user endpoint returns user data in response
     */
    public function test_sync_user_returns_user_data(): void
    {
        $payload = [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
        ];

        $response = $this->postJson('/api/v1/sync-user', $payload);

        $response->assertJson([
            'data' => [
                'firebase_uid' => 'firebase_uid_123',
                'name' => 'John Doe',
                'email' => 'john@example.com',
                'phone' => '08123456789',
            ]
        ]);
    }

    /**
     * Test sync-user endpoint with phone as optional field
     */
    public function test_sync_user_phone_is_optional(): void
    {
        $payload = [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
        ];

        $response = $this->postJson('/api/v1/sync-user', $payload);

        $response->assertStatus(201)
            ->assertJson([
                'status' => 'success',
                'message' => 'User created successfully',
            ]);

        $this->assertDatabaseHas('users', [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
        ]);
    }

    /**
     * Test sync-user endpoint preserves phone when updating without phone
     */
    public function test_sync_user_preserves_phone_on_update(): void
    {
        // Create initial user with phone
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        // Update without phone
        $payload = [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'Jane Doe',
            'email' => 'jane@example.com',
        ];

        $this->postJson('/api/v1/sync-user', $payload);

        $this->assertDatabaseHas('users', [
            'firebase_uid' => 'firebase_uid_123',
            'phone' => '08123456789', // Phone should be preserved
        ]);
    }

    /**
     * Test sync-user endpoint creates user with role 'user'
     */
    public function test_sync_user_creates_user_with_role(): void
    {
        $payload = [
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
        ];

        $this->postJson('/api/v1/sync-user', $payload);

        $this->assertDatabaseHas('users', [
            'firebase_uid' => 'firebase_uid_123',
            'role' => 'user',
        ]);
    }
}
