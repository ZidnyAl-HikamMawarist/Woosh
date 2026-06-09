<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Kreait\Firebase\Contract\Auth;
use Kreait\Firebase\Auth\UserRecord;
use Mockery;
use Tests\TestCase;

class FirebaseSyncTest extends TestCase
{
    use RefreshDatabase;

    public function test_sync_firebase_users_to_mysql(): void
    {
        // Mock Firebase Auth
        $firebaseAuth = Mockery::mock(Auth::class);
        
        // Use standard objects instead of final class UserRecord
        $user1 = (object)[
            'uid' => 'uid-123',
            'email' => 'test@example.com',
            'displayName' => 'Test User',
            'phoneNumber' => '+628123456789'
        ];

        $firebaseAuth->shouldReceive('listUsers')
            ->once()
            ->andReturn(new \ArrayIterator([$user1]));

        // Bind the mock to the container
        $this->app->instance(Auth::class, $firebaseAuth);

        // Run as admin
        $admin = User::factory()->create(['role' => 'admin']);
        
        $response = $this->actingAs($admin)->get('/admin/sync-firebase');

        $response->assertRedirect(route('admin.users'));
        $this->assertDatabaseHas('users', [
            'email' => 'test@example.com',
            'name' => 'Test User',
            'phone' => '+628123456789'
        ]);
    }

    public function test_sync_skips_users_without_email(): void
    {
        $firebaseAuth = Mockery::mock(Auth::class);
        
        $user1 = (object)[
            'uid' => 'uid-no-email',
            'email' => null,
            'displayName' => 'No Email User'
        ];

        $firebaseAuth->shouldReceive('listUsers')
            ->once()
            ->andReturn(new \ArrayIterator([$user1]));

        $this->app->instance(Auth::class, $firebaseAuth);

        $admin = User::factory()->create(['role' => 'admin']);
        
        $this->actingAs($admin)->get('/admin/sync-firebase');

        $this->assertDatabaseMissing('users', [
            'name' => 'No Email User'
        ]);
    }
}
