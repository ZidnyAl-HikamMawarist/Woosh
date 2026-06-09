<?php

require __DIR__ . '/vendor/autoload.php';

$app = require_once __DIR__ . '/bootstrap/app.php';
$app->make('Illuminate\Contracts\Console\Kernel')->bootstrap();

use App\Models\Trip;
use App\Models\Station;
use Carbon\Carbon;

echo "=== WOOSH TRIP SEEDER TEST ===" . PHP_EOL . PHP_EOL;

// 1. Total Trips
echo "1. TOTAL TRIPS" . PHP_EOL;
echo "   Total trips in database: " . Trip::count() . PHP_EOL . PHP_EOL;

// 2. Breakdown by Route
echo "2. BREAKDOWN BY ROUTE" . PHP_EOL;
$halim = Station::where('code', 'HLM')->first();
$tegalluar = Station::where('code', 'TGL')->first();
$karawang = Station::where('code', 'KWG')->first();
$padalarang = Station::where('code', 'PDL')->first();

echo "   Halim -> Tegalluar: " . Trip::where('departure_station_id', $halim->id)->where('arrival_station_id', $tegalluar->id)->count() . PHP_EOL;
echo "   Tegalluar -> Halim: " . Trip::where('departure_station_id', $tegalluar->id)->where('arrival_station_id', $halim->id)->count() . PHP_EOL;
echo "   Halim -> Karawang: " . Trip::where('departure_station_id', $halim->id)->where('arrival_station_id', $karawang->id)->count() . PHP_EOL;
echo "   Karawang -> Halim: " . Trip::where('departure_station_id', $karawang->id)->where('arrival_station_id', $halim->id)->count() . PHP_EOL;
echo "   Halim -> Padalarang: " . Trip::where('departure_station_id', $halim->id)->where('arrival_station_id', $padalarang->id)->count() . PHP_EOL;
echo "   Padalarang -> Halim: " . Trip::where('departure_station_id', $padalarang->id)->where('arrival_station_id', $halim->id)->count() . PHP_EOL . PHP_EOL;

// 3. Trips by Date
echo "3. TRIPS BY DATE (7 days)" . PHP_EOL;
for ($i = 0; $i < 7; $i++) {
    $date = Carbon::today()->addDays($i);
    $count = Trip::whereDate('departure_time', $date)->count();
    echo "   " . $date->format('Y-m-d (l)') . ": " . $count . " trips" . PHP_EOL;
}
echo PHP_EOL;

// 4. Trips by Class
echo "4. TRIPS BY CLASS" . PHP_EOL;
$classes = Trip::select('train_class')->distinct()->get();
foreach ($classes as $class) {
    $count = Trip::where('train_class', $class->train_class)->count();
    echo "   " . $class->train_class . ": " . $count . " trips" . PHP_EOL;
}
echo PHP_EOL;

// 5. Sample Trips for Today
echo "5. SAMPLE TRIPS FOR TODAY (" . Carbon::today()->format('Y-m-d') . ")" . PHP_EOL;
$sampleTrips = Trip::with(['departureStation', 'arrivalStation'])
    ->whereDate('departure_time', Carbon::today())
    ->orderBy('departure_time')
    ->take(10)
    ->get();

foreach ($sampleTrips as $trip) {
    $depTime = Carbon::parse($trip->departure_time)->format('H:i');
    $arrTime = Carbon::parse($trip->arrival_time)->format('H:i');
    echo "   " . $trip->train_name . " | " . 
         $trip->departureStation->name . " -> " . $trip->arrivalStation->name . " | " .
         $depTime . " - " . $arrTime . " | " .
         $trip->train_class . " | Rp " . number_format($trip->base_price, 0, ',', '.') . PHP_EOL;
}
echo PHP_EOL;

// 6. Price Range
echo "6. PRICE RANGE" . PHP_EOL;
$minPrice = Trip::min('base_price');
$maxPrice = Trip::max('base_price');
echo "   Min price: Rp " . number_format($minPrice, 0, ',', '.') . PHP_EOL;
echo "   Max price: Rp " . number_format($maxPrice, 0, ',', '.') . PHP_EOL . PHP_EOL;

// 7. Verify All Stations Have Trips
echo "7. VERIFY ALL STATIONS HAVE TRIPS" . PHP_EOL;
$stations = Station::all();
foreach ($stations as $station) {
    $departureCount = Trip::where('departure_station_id', $station->id)->count();
    $arrivalCount = Trip::where('arrival_station_id', $station->id)->count();
    echo "   " . $station->name . " (" . $station->code . "): " . 
         $departureCount . " departures, " . $arrivalCount . " arrivals" . PHP_EOL;
}
echo PHP_EOL;

// 8. Check for Duplicate Trip IDs
echo "8. CHECK FOR DUPLICATES" . PHP_EOL;
$totalTrips = Trip::count();
$uniqueTripIds = Trip::select('trip_id')->distinct()->count();
if ($totalTrips === $uniqueTripIds) {
    echo "   ✅ No duplicate trip IDs found" . PHP_EOL;
} else {
    echo "   ❌ Found " . ($totalTrips - $uniqueTripIds) . " duplicate trip IDs" . PHP_EOL;
}
echo PHP_EOL;

// 9. Verify Booked Seats are Empty
echo "9. VERIFY BOOKED SEATS" . PHP_EOL;
$emptySeats = Trip::whereRaw("JSON_LENGTH(booked_seats) = 0")->count();
echo "   Trips with empty booked_seats: " . $emptySeats . " / " . $totalTrips . PHP_EOL;
if ($emptySeats === $totalTrips) {
    echo "   ✅ All trips have empty booked_seats" . PHP_EOL;
} else {
    echo "   ⚠️  Some trips have booked seats" . PHP_EOL;
}
echo PHP_EOL;

echo "=== TEST COMPLETED ===" . PHP_EOL;
