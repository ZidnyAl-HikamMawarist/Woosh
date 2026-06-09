<?php

namespace Tests\Feature\Api;

use App\Models\User;
use App\Models\ActivityLog;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class UpdateProfileApiTest extends TestCase
{
    use RefreshDatabase;

    /**
     * Test updating user profile with email identifier
     */
    public function test_update_profile_with_email(): void
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
            'email' => 'john@example.com',
            'name' => 'Jane Doe',
            'phone' => '08987654321',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'Profile updated successfully',
            ])
            ->assertJsonStructure([
                'status',
                'message',
                'data' => [
                    'id',
                    'name',
                    'email',
                    'phone',
                ]
            ]);

        $this->assertDatabaseHas('users', [
            'id' => $user->id,
            'name' => 'Jane Doe',
            'email' => 'john@example.com',
            'phone' => '08987654321',
        ]);
    }

    /**
     * Test updating user profile with user_id identifier
     */
    public function test_update_profile_with_user_id(): void
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
            'user_id' => $user->id,
            'name' => 'Jane Doe',
            'phone' => '08987654321',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'Profile updated successfully',
            ]);

        $this->assertDatabaseHas('users', [
            'id' => $user->id,
            'name' => 'Jane Doe',
            'phone' => '08987654321',
        ]);
    }

    /**
     * Test updating user profile with address field
     */
    public function test_update_profile_with_address(): void
    {
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'email' => 'john@example.com',
            'name' => 'Jane Doe',
            'address' => 'Jl. Merdeka No. 123, Jakarta',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'Profile updated successfully',
            ]);

        $this->assertDatabaseHas('users', [
            'id' => $user->id,
            'name' => 'Jane Doe',
            'address' => 'Jl. Merdeka No. 123, Jakarta',
        ]);
    }

    /**
     * Test updating only name field
     */
    public function test_update_profile_only_name(): void
    {
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'email' => 'john@example.com',
            'name' => 'Jane Doe',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(200);

        $this->assertDatabaseHas('users', [
            'id' => $user->id,
            'name' => 'Jane Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789', // Should remain unchanged
        ]);
    }

    /**
     * Test updating only phone field
     */
    public function test_update_profile_only_phone(): void
    {
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'email' => 'john@example.com',
            'phone' => '08987654321',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(200);

        $this->assertDatabaseHas('users', [
            'id' => $user->id,
            'name' => 'John Doe', // Should remain unchanged
            'phone' => '08987654321',
        ]);
    }

    /**
     * Test updating profile with all fields
     */
    public function test_update_profile_all_fields(): void
    {
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'user_id' => $user->id,
            'name' => 'Jane Doe',
            'phone' => '08987654321',
            'address' => 'Jl. Sudirman No. 456, Bandung',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'Profile updated successfully',
                'data' => [
                    'name' => 'Jane Doe',
                    'phone' => '08987654321',
                    'address' => 'Jl. Sudirman No. 456, Bandung',
                ]
            ]);
    }

    /**
     * Test updating profile with non-existent user returns 404
     */
    public function test_update_profile_user_not_found(): void
    {
        $payload = [
            'email' => 'nonexistent@example.com',
            'name' => 'Jane Doe',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(404)
            ->assertJson([
                'status' => 'error',
                'message' => 'User not found',
            ]);
    }

    /**
     * Test updating profile with invalid email format
     */
    public function test_update_profile_validates_email_format(): void
    {
        $payload = [
            'email' => 'invalid-email',
            'name' => 'Jane Doe',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['email']);
    }

    /**
     * Test updating profile with name exceeding max length
     */
    public function test_update_profile_validates_name_max_length(): void
    {
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'email' => 'john@example.com',
            'name' => str_repeat('a', 256), // Exceeds 255 character limit
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['name']);
    }

    /**
     * Test updating profile with address exceeding max length
     */
    public function test_update_profile_validates_address_max_length(): void
    {
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'email' => 'john@example.com',
            'address' => str_repeat('a', 501), // Exceeds 500 character limit
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['address']);
    }

    /**
     * Test updating profile logs activity
     */
    public function test_update_profile_logs_activity(): void
    {
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'email' => 'john@example.com',
            'name' => 'Jane Doe',
            'phone' => '08987654321',
        ];

        $this->postJson('/api/v1/update-profile', $payload);

        $this->assertDatabaseHas('activity_logs', [
            'user_id' => $user->id,
            'action' => 'updated',
            'model' => User::class,
            'model_id' => $user->id,
        ]);
    }

    /**
     * Test updating profile returns updated user data
     */
    public function test_update_profile_returns_updated_data(): void
    {
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'email' => 'john@example.com',
            'name' => 'Jane Doe',
            'phone' => '08987654321',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertJson([
            'data' => [
                'id' => $user->id,
                'name' => 'Jane Doe',
                'email' => 'john@example.com',
                'phone' => '08987654321',
            ]
        ]);
    }

    /**
     * Test updating profile with empty payload (no fields to update)
     */
    public function test_update_profile_with_empty_fields(): void
    {
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'email' => 'john@example.com',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'Profile updated successfully',
            ]);

        // User data should remain unchanged
        $this->assertDatabaseHas('users', [
            'id' => $user->id,
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
        ]);
    }

    /**
     * Test updating profile with user_id takes precedence over email
     */
    public function test_update_profile_user_id_takes_precedence(): void
    {
        $user1 = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $user2 = User::create([
            'firebase_uid' => 'firebase_uid_456',
            'name' => 'Jane Doe',
            'email' => 'jane@example.com',
            'phone' => '08987654321',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'user_id' => $user1->id,
            'email' => 'jane@example.com', // Different user's email
            'name' => 'Updated Name',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(200);

        // user1 should be updated, not user2
        $this->assertDatabaseHas('users', [
            'id' => $user1->id,
            'name' => 'Updated Name',
        ]);

        $this->assertDatabaseHas('users', [
            'id' => $user2->id,
            'name' => 'Jane Doe', // Should remain unchanged
        ]);
    }

    /**
     * Test updating profile with invalid user_id
     */
    public function test_update_profile_invalid_user_id(): void
    {
        $payload = [
            'user_id' => 99999,
            'name' => 'Jane Doe',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['user_id']);
    }

    /**
     * Test updating profile with phone as optional field
     */
    public function test_update_profile_phone_is_optional(): void
    {
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'email' => 'john@example.com',
            'name' => 'Jane Doe',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(200);

        $this->assertDatabaseHas('users', [
            'id' => $user->id,
            'name' => 'Jane Doe',
        ]);
    }

    /**
     * Test updating profile with address as optional field
     */
    public function test_update_profile_address_is_optional(): void
    {
        $user = User::create([
            'firebase_uid' => 'firebase_uid_123',
            'name' => 'John Doe',
            'email' => 'john@example.com',
            'phone' => '08123456789',
            'password' => bcrypt('password'),
        ]);

        $payload = [
            'email' => 'john@example.com',
            'name' => 'Jane Doe',
        ];

        $response = $this->postJson('/api/v1/update-profile', $payload);

        $response->assertStatus(200);

        $this->assertDatabaseHas('users', [
            'id' => $user->id,
            'name' => 'Jane Doe',
        ]);
    }
}
