<?php

require __DIR__ . '/vendor/autoload.php';

$app = require_once __DIR__ . '/bootstrap/app.php';
$kernel = $app->make('Illuminate\Contracts\Http\Kernel');

use Illuminate\Http\Request;
use Carbon\Carbon;

echo "=== API ENDPOINT TEST ===" . PHP_EOL . PHP_EOL;

// Test 1: Get trips without parameters
echo "1. TEST: GET /api/v1/trips (no parameters)" . PHP_EOL;
$request = Request::create('/api/v1/trips', 'GET');
$response = $kernel->handle($request);
$data = json_decode($response->getContent(), true);

if ($response->getStatusCode() === 200) {
    echo "   ✅ Status: " . $response->getStatusCode() . PHP_EOL;
    if (isset($data['data'])) {
        echo "   ✅ Trips returned: " . count($data['data']) . PHP_EOL;
        if (count($data['data']) > 0) {
            $firstTrip = $data['data'][0];
            echo "   Sample trip: " . $firstTrip['train_name'] . " | " . 
                 $firstTrip['departure_station']['name'] . " -> " . 
                 $firstTrip['arrival_station']['name'] . PHP_EOL;
        }
    }
} else {
    echo "   ❌ Status: " . $response->getStatusCode() . PHP_EOL;
    echo "   Error: " . $response->getContent() . PHP_EOL;
}
echo PHP_EOL;

// Test 2: Get trips with date filter (today)
echo "2. TEST: GET /api/v1/trips?date=" . Carbon::today()->format('Y-m-d') . PHP_EOL;
$request = Request::create('/api/v1/trips?date=' . Carbon::today()->format('Y-m-d'), 'GET');
$response = $kernel->handle($request);
$data = json_decode($response->getContent(), true);

if ($response->getStatusCode() === 200) {
    echo "   ✅ Status: " . $response->getStatusCode() . PHP_EOL;
    if (isset($data['data'])) {
        echo "   ✅ Trips for today: " . count($data['data']) . PHP_EOL;
    }
} else {
    echo "   ❌ Status: " . $response->getStatusCode() . PHP_EOL;
}
echo PHP_EOL;

// Test 3: Get trips with station filter (Halim to Tegalluar)
echo "3. TEST: GET /api/v1/trips with station filter (HLM -> TGL)" . PHP_EOL;
$halim = \App\Models\Station::where('code', 'HLM')->first();
$tegalluar = \App\Models\Station::where('code', 'TGL')->first();
$request = Request::create('/api/v1/trips?departure_station_id=' . $halim->id . '&arrival_station_id=' . $tegalluar->id, 'GET');
$response = $kernel->handle($request);
$data = json_decode($response->getContent(), true);

if ($response->getStatusCode() === 200) {
    echo "   ✅ Status: " . $response->getStatusCode() . PHP_EOL;
    if (isset($data['data'])) {
        echo "   ✅ Trips Halim -> Tegalluar: " . count($data['data']) . PHP_EOL;
    }
} else {
    echo "   ❌ Status: " . $response->getStatusCode() . PHP_EOL;
}
echo PHP_EOL;

// Test 4: Get trips with station filter (Halim to Karawang)
echo "4. TEST: GET /api/v1/trips with station filter (HLM -> KWG)" . PHP_EOL;
$karawang = \App\Models\Station::where('code', 'KWG')->first();
$request = Request::create('/api/v1/trips?departure_station_id=' . $halim->id . '&arrival_station_id=' . $karawang->id, 'GET');
$response = $kernel->handle($request);
$data = json_decode($response->getContent(), true);

if ($response->getStatusCode() === 200) {
    echo "   ✅ Status: " . $response->getStatusCode() . PHP_EOL;
    if (isset($data['data'])) {
        echo "   ✅ Trips Halim -> Karawang: " . count($data['data']) . PHP_EOL;
    }
} else {
    echo "   ❌ Status: " . $response->getStatusCode() . PHP_EOL;
}
echo PHP_EOL;

// Test 5: Get trips with station filter (Halim to Padalarang)
echo "5. TEST: GET /api/v1/trips with station filter (HLM -> PDL)" . PHP_EOL;
$padalarang = \App\Models\Station::where('code', 'PDL')->first();
$request = Request::create('/api/v1/trips?departure_station_id=' . $halim->id . '&arrival_station_id=' . $padalarang->id, 'GET');
$response = $kernel->handle($request);
$data = json_decode($response->getContent(), true);

if ($response->getStatusCode() === 200) {
    echo "   ✅ Status: " . $response->getStatusCode() . PHP_EOL;
    if (isset($data['data'])) {
        echo "   ✅ Trips Halim -> Padalarang: " . count($data['data']) . PHP_EOL;
    }
} else {
    echo "   ❌ Status: " . $response->getStatusCode() . PHP_EOL;
}
echo PHP_EOL;

// Test 6: Get trips for tomorrow
echo "6. TEST: GET /api/v1/trips?date=" . Carbon::tomorrow()->format('Y-m-d') . PHP_EOL;
$request = Request::create('/api/v1/trips?date=' . Carbon::tomorrow()->format('Y-m-d'), 'GET');
$response = $kernel->handle($request);
$data = json_decode($response->getContent(), true);

if ($response->getStatusCode() === 200) {
    echo "   ✅ Status: " . $response->getStatusCode() . PHP_EOL;
    if (isset($data['data'])) {
        echo "   ✅ Trips for tomorrow: " . count($data['data']) . PHP_EOL;
    }
} else {
    echo "   ❌ Status: " . $response->getStatusCode() . PHP_EOL;
}
echo PHP_EOL;

echo "=== API TEST COMPLETED ===" . PHP_EOL;
