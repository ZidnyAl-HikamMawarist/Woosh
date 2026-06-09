<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Station;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class AdminDashboardTest extends TestCase
{
    use RefreshDatabase;

    public function test_guest_is_redirected_to_login(): void
    {
        $response = $this->get('/admin');
        $response->assertRedirect('/login');
    }

    public function test_admin_can_access_dashboard(): void
    {
        $user = User::factory()->create(['role' => 'admin']);

        $response = $this->actingAs($user)->get('/admin');

        $response->assertStatus(200);
        $response->assertViewIs('admin.dashboard');
    }

    public function test_manager_can_access_dashboard(): void
    {
        $user = User::factory()->create(['role' => 'manager']);

        $response = $this->actingAs($user)->get('/admin');

        $response->assertStatus(200);
        $response->assertViewIs('admin.dashboard');
    }

    public function test_admin_can_view_stations_list(): void
    {
        $user = User::factory()->create(['role' => 'admin']);
        Station::create([
            'name' => 'Gambir',
            'code' => 'GMR',
            'city' => 'Jakarta'
        ]);

        $response = $this->actingAs($user)->get('/admin/stations');

        $response->assertStatus(200);
        $response->assertSee('Gambir');
    }

    public function test_manager_can_view_stations_list(): void
    {
        $user = User::factory()->create(['role' => 'manager']);
        
        $response = $this->actingAs($user)->get('/admin/stations');

        $response->assertStatus(200);
    }

    public function test_admin_can_create_station(): void
    {
        $user = User::factory()->create(['role' => 'admin']);

        $response = $this->actingAs($user)->post('/admin/stations', [
            'name' => 'Bandung',
            'code' => 'BD',
            'city' => 'Bandung',
            'facilities' => 'WiFi, Toilet',
            'latitude' => -6.9147,
            'longitude' => 107.6098,
        ]);

        $response->assertRedirect(route('admin.stations'));
        $this->assertDatabaseHas('stations', ['code' => 'BD']);
    }

    public function test_manager_cannot_create_station(): void
    {
        $user = User::factory()->create(['role' => 'manager']);

        $response = $this->actingAs($user)->post('/admin/stations', [
            'name' => 'Bandung',
            'code' => 'BD',
            'city' => 'Bandung',
        ]);

        // RoleMiddleware redirects back to admin.dashboard with error
        $response->assertRedirect(route('admin.dashboard'));
        $this->assertDatabaseMissing('stations', ['code' => 'BD']);
    }
}
