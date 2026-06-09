<?php

namespace Tests\Feature\Api;

use App\Models\Trip;
use App\Models\Station;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class GetTripsApiTest extends TestCase
{
    use RefreshDatabase;

    protected function setUp(): void
    {
        parent::setUp();
        
        // Create test stations
        $this->departureStation = Station::create([
            'name' => 'Jakarta Kota',
            'code' => 'JKT',
            'city' => 'Jakarta',
            'latitude' => -6.1275,
            'longitude' => 106.8650,
        ]);

        $this->arrivalStation = Station::create([
            'name' => 'Bandung',
            'code' => 'BDG',
            'city' => 'Bandung',
            'latitude' => -6.9175,
            'longitude' => 107.6062,
        ]);

        $this->departureStation2 = Station::create([
            'name' => 'Surabaya',
            'code' => 'SBY',
            'city' => 'Surabaya',
            'latitude' => -7.2575,
            'longitude' => 112.7521,
        ]);
    }

    /**
     * Test get trips endpoint returns all trips
     */
    public function test_get_trips_returns_all_trips(): void
    {
        // Create test trips
        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => now()->addDay(),
            'arrival_time' => now()->addDay()->addHours(3),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        Trip::create([
            'trip_id' => 'TRIP002',
            'train_name' => 'Gajayana',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => now()->addDay(),
            'arrival_time' => now()->addDay()->addHours(4),
            'train_class' => 'Business',
            'base_price' => 150000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        $response = $this->getJson('/api/v1/trips');

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
            ])
            ->assertJsonStructure([
                'status',
                'data' => [
                    '*' => [
                        'trip_id',
                        'train_name',
                        'departure_time',
                        'arrival_time',
                        'train_class',
                        'base_price',
                        'seat_availability',
                    ]
                ],
                'count',
            ]);

        $this->assertEquals(2, $response->json('count'));
    }

    /**
     * Test get trips endpoint returns empty list when no trips exist
     */
    public function test_get_trips_returns_empty_list(): void
    {
        $response = $this->getJson('/api/v1/trips');

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'data' => [],
                'count' => 0,
            ]);
    }

    /**
     * Test get trips endpoint filters by date
     */
    public function test_get_trips_filters_by_date(): void
    {
        $tomorrow = now()->addDay();
        $dayAfter = now()->addDays(2);

        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => $tomorrow,
            'arrival_time' => $tomorrow->addHours(3),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        Trip::create([
            'trip_id' => 'TRIP002',
            'train_name' => 'Gajayana',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => $dayAfter,
            'arrival_time' => $dayAfter->addHours(4),
            'train_class' => 'Business',
            'base_price' => 150000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        $response = $this->getJson('/api/v1/trips?date=' . $tomorrow->format('Y-m-d'));

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'count' => 1,
            ]);

        $this->assertEquals('TRIP001', $response->json('data.0.trip_id'));
    }

    /**
     * Test get trips endpoint filters by departure station
     */
    public function test_get_trips_filters_by_departure_station(): void
    {
        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => now()->addDay(),
            'arrival_time' => now()->addDay()->addHours(3),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        Trip::create([
            'trip_id' => 'TRIP002',
            'train_name' => 'Gajayana',
            'departure_station_id' => $this->departureStation2->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => now()->addDay(),
            'arrival_time' => now()->addDay()->addHours(4),
            'train_class' => 'Business',
            'base_price' => 150000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        // Filter by station code
        $response = $this->getJson('/api/v1/trips?departure=JKT');

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'count' => 1,
            ]);

        $this->assertEquals('TRIP001', $response->json('data.0.trip_id'));
    }

    /**
     * Test get trips endpoint filters by departure station name
     */
    public function test_get_trips_filters_by_departure_station_name(): void
    {
        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => now()->addDay(),
            'arrival_time' => now()->addDay()->addHours(3),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        // Filter by station name
        $response = $this->getJson('/api/v1/trips?departure=Jakarta');

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'count' => 1,
            ]);

        $this->assertEquals('TRIP001', $response->json('data.0.trip_id'));
    }

    /**
     * Test get trips endpoint filters by arrival station
     */
    public function test_get_trips_filters_by_arrival_station(): void
    {
        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => now()->addDay(),
            'arrival_time' => now()->addDay()->addHours(3),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        Trip::create([
            'trip_id' => 'TRIP002',
            'train_name' => 'Gajayana',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->departureStation2->id,
            'departure_time' => now()->addDay(),
            'arrival_time' => now()->addDay()->addHours(4),
            'train_class' => 'Business',
            'base_price' => 150000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        // Filter by arrival station code
        $response = $this->getJson('/api/v1/trips?arrival=BDG');

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'count' => 1,
            ]);

        $this->assertEquals('TRIP001', $response->json('data.0.trip_id'));
    }

    /**
     * Test get trips endpoint filters by multiple criteria
     */
    public function test_get_trips_filters_by_multiple_criteria(): void
    {
        $tomorrow = now()->addDay();

        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => $tomorrow,
            'arrival_time' => $tomorrow->addHours(3),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        Trip::create([
            'trip_id' => 'TRIP002',
            'train_name' => 'Gajayana',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->departureStation2->id,
            'departure_time' => $tomorrow,
            'arrival_time' => $tomorrow->addHours(4),
            'train_class' => 'Business',
            'base_price' => 150000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        // Filter by date, departure, and arrival
        $response = $this->getJson('/api/v1/trips?date=' . $tomorrow->format('Y-m-d') . '&departure=JKT&arrival=BDG');

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'count' => 1,
            ]);

        $this->assertEquals('TRIP001', $response->json('data.0.trip_id'));
    }

    /**
     * Test get trips endpoint returns trips sorted by departure time
     */
    public function test_get_trips_sorted_by_departure_time(): void
    {
        $tomorrow = now()->addDay();

        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => $tomorrow->setHour(14),
            'arrival_time' => $tomorrow->setHour(17),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        Trip::create([
            'trip_id' => 'TRIP002',
            'train_name' => 'Gajayana',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => $tomorrow->setHour(8),
            'arrival_time' => $tomorrow->setHour(12),
            'train_class' => 'Business',
            'base_price' => 150000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        $response = $this->getJson('/api/v1/trips');

        $response->assertStatus(200);

        // First trip should be TRIP002 (08:00)
        $this->assertEquals('TRIP002', $response->json('data.0.trip_id'));
        // Second trip should be TRIP001 (14:00)
        $this->assertEquals('TRIP001', $response->json('data.1.trip_id'));
    }

    /**
     * Test get trips endpoint includes seat availability
     */
    public function test_get_trips_includes_seat_availability(): void
    {
        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => now()->addDay(),
            'arrival_time' => now()->addDay()->addHours(3),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => ['A1', 'A2', 'B1'],
        ]);

        $response = $this->getJson('/api/v1/trips');

        $response->assertStatus(200)
            ->assertJsonStructure([
                'data' => [
                    '*' => [
                        'seat_availability' => [
                            'booked_seats',
                            'total_seats',
                            'available_seats',
                        ]
                    ]
                ]
            ]);

        $seatAvailability = $response->json('data.0.seat_availability');
        $this->assertIsArray($seatAvailability['booked_seats']);
        $this->assertIsInt($seatAvailability['total_seats']);
        $this->assertIsInt($seatAvailability['available_seats']);
    }

    /**
     * Test get trips endpoint includes station information
     */
    public function test_get_trips_includes_station_information(): void
    {
        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => now()->addDay(),
            'arrival_time' => now()->addDay()->addHours(3),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        $response = $this->getJson('/api/v1/trips');

        $response->assertStatus(200)
            ->assertJsonStructure([
                'data' => [
                    '*' => [
                        'departure_station' => [
                            'id',
                            'name',
                            'code',
                            'city',
                        ],
                        'arrival_station' => [
                            'id',
                            'name',
                            'code',
                            'city',
                        ]
                    ]
                ]
            ]);

        $this->assertEquals('Jakarta Kota', $response->json('data.0.departure_station.name'));
        $this->assertEquals('Bandung', $response->json('data.0.arrival_station.name'));
    }

    /**
     * Test get trips endpoint validates date format
     */
    public function test_get_trips_validates_date_format(): void
    {
        $response = $this->getJson('/api/v1/trips?date=invalid-date');

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['date']);
    }

    /**
     * Test get trips endpoint with invalid date format returns validation error
     */
    public function test_get_trips_with_invalid_date_format(): void
    {
        $response = $this->getJson('/api/v1/trips?date=2024/01/01');

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['date']);
    }

    /**
     * Test get trips endpoint with no matching filters returns empty
     */
    public function test_get_trips_with_no_matching_filters(): void
    {
        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => now()->addDay(),
            'arrival_time' => now()->addDay()->addHours(3),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        // Filter by non-existent station
        $response = $this->getJson('/api/v1/trips?departure=NONEXISTENT');

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'data' => [],
                'count' => 0,
            ]);
    }

    /**
     * Test get trips endpoint returns correct trip count
     */
    public function test_get_trips_returns_correct_count(): void
    {
        for ($i = 1; $i <= 5; $i++) {
            $tripId = sprintf('TRIP%03d', $i);
            Trip::create([
                'trip_id' => $tripId,
                'train_name' => "Train {$i}",
                'departure_station_id' => $this->departureStation->id,
                'arrival_station_id' => $this->arrivalStation->id,
                'departure_time' => now()->addDay()->addHours($i),
                'arrival_time' => now()->addDay()->addHours($i + 3),
                'train_class' => 'Executive',
                'base_price' => 250000,
                'carriages_count' => 4,
                'booked_seats' => [],
            ]);
        }

        $response = $this->getJson('/api/v1/trips');

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'count' => 5,
            ]);

        $this->assertCount(5, $response->json('data'));
    }

    /**
     * Test get trips endpoint with partial station name match
     */
    public function test_get_trips_with_partial_station_name_match(): void
    {
        Trip::create([
            'trip_id' => 'TRIP001',
            'train_name' => 'Argo Bromo',
            'departure_station_id' => $this->departureStation->id,
            'arrival_station_id' => $this->arrivalStation->id,
            'departure_time' => now()->addDay(),
            'arrival_time' => now()->addDay()->addHours(3),
            'train_class' => 'Executive',
            'base_price' => 250000,
            'carriages_count' => 4,
            'booked_seats' => [],
        ]);

        // Filter by partial station name
        $response = $this->getJson('/api/v1/trips?departure=Kota');

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'count' => 1,
            ]);

        $this->assertEquals('TRIP001', $response->json('data.0.trip_id'));
    }
}
