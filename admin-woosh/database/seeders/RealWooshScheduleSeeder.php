<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class RealWooshScheduleSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // 1. Ensure Stations exist
        $halimId = \App\Models\Station::where('code', 'HLM')->first()->id;
        $tegalluarId = \App\Models\Station::where('code', 'TGL')->first()->id;
        $karawangId = \App\Models\Station::where('code', 'KWG')->first()->id;
        $padalarangId = \App\Models\Station::where('code', 'PDL')->first()->id;

        // 2. Clear existing trips to avoid messy data (optional, but requested "lengkap")
        DB::statement('SET FOREIGN_KEY_CHECKS=0;');
        DB::table('trips')->truncate();
        DB::statement('SET FOREIGN_KEY_CHECKS=1;');

        // 3. Define the base schedules from the images
        $eastboundBase = [
            ['id' => 'G1003', 'dep' => '06:25', 'arr' => '07:19', 'mon_sat' => true],
            ['id' => 'G1005', 'dep' => '07:00', 'arr' => '07:47', 'mon_sat' => true],
            ['id' => 'G1007', 'dep' => '07:25', 'arr' => '08:19', 'mon_sat' => true],
            ['id' => 'G1009', 'dep' => '08:00', 'arr' => '08:47', 'mon_sat' => false],
            ['id' => 'G1011', 'dep' => '08:25', 'arr' => '09:19', 'mon_sat' => false],
            ['id' => 'G1013', 'dep' => '09:00', 'arr' => '09:47', 'mon_sat' => false],
            ['id' => 'G1015', 'dep' => '09:25', 'arr' => '10:19', 'mon_sat' => false],
            ['id' => 'G1017', 'dep' => '10:00', 'arr' => '10:47', 'mon_sat' => false],
            ['id' => 'G1019', 'dep' => '10:25', 'arr' => '11:19', 'mon_sat' => false],
            ['id' => 'G1021', 'dep' => '11:00', 'arr' => '11:47', 'mon_sat' => false],
            ['id' => 'G1023', 'dep' => '11:25', 'arr' => '12:19', 'mon_sat' => false],
            ['id' => 'G1025', 'dep' => '12:00', 'arr' => '12:47', 'mon_sat' => false],
            ['id' => 'G1027', 'dep' => '12:25', 'arr' => '13:19', 'mon_sat' => false],
            ['id' => 'G1029', 'dep' => '13:00', 'arr' => '13:47', 'mon_sat' => false],
            ['id' => 'G1031', 'dep' => '13:25', 'arr' => '14:19', 'mon_sat' => false],
            ['id' => 'G1033', 'dep' => '14:00', 'arr' => '14:47', 'mon_sat' => false],
            ['id' => 'G1035', 'dep' => '14:25', 'arr' => '15:19', 'mon_sat' => false],
            ['id' => 'G1037', 'dep' => '15:00', 'arr' => '15:47', 'mon_sat' => false],
            ['id' => 'G1039', 'dep' => '15:25', 'arr' => '16:19', 'mon_sat' => false],
            ['id' => 'G1041', 'dep' => '16:00', 'arr' => '16:47', 'mon_sat' => false],
            ['id' => 'G1043', 'dep' => '16:25', 'arr' => '17:19', 'mon_sat' => false],
            ['id' => 'G1045', 'dep' => '17:00', 'arr' => '17:47', 'mon_sat' => false],
            ['id' => 'G1047', 'dep' => '17:25', 'arr' => '18:19', 'mon_sat' => false],
            ['id' => 'G1049', 'dep' => '18:00', 'arr' => '18:47', 'mon_sat' => false], // Wait, 18:47? Yes image says 18:47.
            ['id' => 'G1051', 'dep' => '18:25', 'arr' => '19:19', 'mon_sat' => false],
            ['id' => 'G1053', 'dep' => '19:00', 'arr' => '19:47', 'mon_sat' => false],
            ['id' => 'G1055', 'dep' => '19:25', 'arr' => '20:19', 'mon_sat' => false],
            ['id' => 'G1057', 'dep' => '20:00', 'arr' => '20:47', 'mon_sat' => false],
            ['id' => 'G1059', 'dep' => '20:25', 'arr' => '21:19', 'mon_sat' => false],
            ['id' => 'G1061', 'dep' => '21:00', 'arr' => '21:47', 'mon_sat' => false],
            ['id' => 'G1063', 'dep' => '21:25', 'arr' => '22:19', 'mon_sat' => false],
        ];

        $westboundBase = [
            ['id' => 'G1004', 'dep' => '06:05', 'arr' => '06:52', 'mon_sat' => true],
            ['id' => 'G1006', 'dep' => '06:35', 'arr' => '07:29', 'mon_sat' => true],
            ['id' => 'G1008', 'dep' => '07:05', 'arr' => '07:52', 'mon_sat' => true],
            ['id' => 'G1010', 'dep' => '07:35', 'arr' => '08:29', 'mon_sat' => false],
            ['id' => 'G1012', 'dep' => '08:05', 'arr' => '08:52', 'mon_sat' => false],
            ['id' => 'G1014', 'dep' => '08:35', 'arr' => '09:29', 'mon_sat' => false],
            ['id' => 'G1016', 'dep' => '09:05', 'arr' => '09:52', 'mon_sat' => false],
            ['id' => 'G1018', 'dep' => '09:35', 'arr' => '10:29', 'mon_sat' => false],
            ['id' => 'G1020', 'dep' => '10:05', 'arr' => '10:52', 'mon_sat' => false],
            ['id' => 'G1022', 'dep' => '10:35', 'arr' => '11:29', 'mon_sat' => false],
            ['id' => 'G1024', 'dep' => '11:05', 'arr' => '11:52', 'mon_sat' => false],
            ['id' => 'G1026', 'dep' => '11:35', 'arr' => '12:29', 'mon_sat' => false],
            ['id' => 'G1028', 'dep' => '12:05', 'arr' => '12:52', 'mon_sat' => false],
            ['id' => 'G1030', 'dep' => '12:35', 'arr' => '13:29', 'mon_sat' => false],
            ['id' => 'G1032', 'dep' => '13:05', 'arr' => '13:52', 'mon_sat' => false],
            ['id' => 'G1034', 'dep' => '13:35', 'arr' => '14:29', 'mon_sat' => false],
            ['id' => 'G1036', 'dep' => '14:05', 'arr' => '14:52', 'mon_sat' => false],
            ['id' => 'G1038', 'dep' => '14:35', 'arr' => '15:29', 'mon_sat' => false],
            ['id' => 'G1040', 'dep' => '15:05', 'arr' => '15:52', 'mon_sat' => false],
            ['id' => 'G1042', 'dep' => '15:35', 'arr' => '16:29', 'mon_sat' => false],
            ['id' => 'G1044', 'dep' => '16:05', 'arr' => '16:52', 'mon_sat' => false],
            ['id' => 'G1046', 'dep' => '16:35', 'arr' => '17:29', 'mon_sat' => false],
            ['id' => 'G1048', 'dep' => '17:05', 'arr' => '17:52', 'mon_sat' => false],
            ['id' => 'G1050', 'dep' => '17:35', 'arr' => '18:29', 'mon_sat' => false],
            ['id' => 'G1052', 'dep' => '18:05', 'arr' => '18:52', 'mon_sat' => false],
            ['id' => 'G1054', 'dep' => '18:35', 'arr' => '19:29', 'mon_sat' => false],
            ['id' => 'G1056', 'dep' => '19:05', 'arr' => '19:52', 'mon_sat' => false],
            ['id' => 'G1058', 'dep' => '19:35', 'arr' => '20:29', 'mon_sat' => false],
            ['id' => 'G1060', 'dep' => '20:05', 'arr' => '20:52', 'mon_sat' => false],
            ['id' => 'G1062', 'dep' => '20:35', 'arr' => '21:29', 'mon_sat' => false],
            ['id' => 'G1064', 'dep' => '21:05', 'arr' => '21:52', 'mon_sat' => false],
        ];

        // Halim -> Karawang (intermediate station, shorter travel time ~30 min)
        $halimKarawangBase = [
            ['id' => 'K2001', 'dep' => '06:25', 'arr' => '06:55'],
            ['id' => 'K2003', 'dep' => '08:00', 'arr' => '08:30'],
            ['id' => 'K2005', 'dep' => '10:00', 'arr' => '10:30'],
            ['id' => 'K2007', 'dep' => '12:00', 'arr' => '12:30'],
            ['id' => 'K2009', 'dep' => '14:00', 'arr' => '14:30'],
            ['id' => 'K2011', 'dep' => '16:00', 'arr' => '16:30'],
            ['id' => 'K2013', 'dep' => '18:00', 'arr' => '18:30'],
            ['id' => 'K2015', 'dep' => '20:00', 'arr' => '20:30'],
        ];

        // Karawang -> Halim
        $karawangHalimBase = [
            ['id' => 'K2002', 'dep' => '07:00', 'arr' => '07:30'],
            ['id' => 'K2004', 'dep' => '09:00', 'arr' => '09:30'],
            ['id' => 'K2006', 'dep' => '11:00', 'arr' => '11:30'],
            ['id' => 'K2008', 'dep' => '13:00', 'arr' => '13:30'],
            ['id' => 'K2010', 'dep' => '15:00', 'arr' => '15:30'],
            ['id' => 'K2012', 'dep' => '17:00', 'arr' => '17:30'],
            ['id' => 'K2014', 'dep' => '19:00', 'arr' => '19:30'],
            ['id' => 'K2016', 'dep' => '21:00', 'arr' => '21:30'],
        ];

        // Halim -> Padalarang (intermediate station, ~40 min)
        $halimPadalarangBase = [
            ['id' => 'P3001', 'dep' => '06:30', 'arr' => '07:10'],
            ['id' => 'P3003', 'dep' => '08:30', 'arr' => '09:10'],
            ['id' => 'P3005', 'dep' => '10:30', 'arr' => '11:10'],
            ['id' => 'P3007', 'dep' => '12:30', 'arr' => '13:10'],
            ['id' => 'P3009', 'dep' => '14:30', 'arr' => '15:10'],
            ['id' => 'P3011', 'dep' => '16:30', 'arr' => '17:10'],
            ['id' => 'P3013', 'dep' => '18:30', 'arr' => '19:10'],
            ['id' => 'P3015', 'dep' => '20:30', 'arr' => '21:10'],
        ];

        // Padalarang -> Halim
        $padalarangHalimBase = [
            ['id' => 'P3002', 'dep' => '07:15', 'arr' => '07:55'],
            ['id' => 'P3004', 'dep' => '09:15', 'arr' => '09:55'],
            ['id' => 'P3006', 'dep' => '11:15', 'arr' => '11:55'],
            ['id' => 'P3008', 'dep' => '13:15', 'arr' => '13:55'],
            ['id' => 'P3010', 'dep' => '15:15', 'arr' => '15:55'],
            ['id' => 'P3012', 'dep' => '17:15', 'arr' => '17:55'],
            ['id' => 'P3014', 'dep' => '19:15', 'arr' => '19:55'],
            ['id' => 'P3016', 'dep' => '21:15', 'arr' => '21:55'],
        ];

        // 4. Generate trips for the next 7 days (today + 6 days for testing)
        $startDate = Carbon::today();
        $tripsToInsert = [];
        $now = Carbon::now();

        for ($i = 0; $i < 7; $i++) {
            $currentDate = $startDate->copy()->addDays($i);
            $isSunday = $currentDate->isSunday();
            $dateStr = $currentDate->format('Y-m-d');
            $dateSuffix = $currentDate->format('dmY');

            // === HALIM <-> TEGALLUAR (Main Route) ===
            // Eastbound (Halim -> Tegalluar)
            foreach ($eastboundBase as $base) {
                if ($isSunday && $base['mon_sat']) continue;

                $classes = [
                    ['name' => 'Premium Economy', 'price' => 250000],
                    ['name' => 'Business Class', 'price' => 500000],
                    ['name' => 'First Class', 'price' => 750000],
                ];

                foreach ($classes as $class) {
                    $tripsToInsert[] = [
                        'trip_id' => 'WSH-' . $base['id'] . '-' . substr($class['name'], 0, 1) . '-' . $dateSuffix,
                        'train_name' => 'Whoosh ' . $base['id'],
                        'departure_station_id' => $halimId,
                        'arrival_station_id' => $tegalluarId,
                        'departure_time' => $dateStr . ' ' . $base['dep'] . ':00',
                        'arrival_time' => $dateStr . ' ' . $base['arr'] . ':00',
                        'train_class' => $class['name'],
                        'base_price' => $class['price'],
                        'carriages_count' => 8,
                        'booked_seats' => json_encode([]),
                        'created_at' => $now,
                        'updated_at' => $now,
                    ];
                }
            }

            // Westbound (Tegalluar -> Halim)
            foreach ($westboundBase as $base) {
                if ($isSunday && $base['mon_sat']) continue;

                $classes = [
                    ['name' => 'Premium Economy', 'price' => 250000],
                    ['name' => 'Business Class', 'price' => 500000],
                    ['name' => 'First Class', 'price' => 750000],
                ];

                foreach ($classes as $class) {
                    $tripsToInsert[] = [
                        'trip_id' => 'WSH-' . $base['id'] . '-' . substr($class['name'], 0, 1) . '-' . $dateSuffix,
                        'train_name' => 'Whoosh ' . $base['id'],
                        'departure_station_id' => $tegalluarId,
                        'arrival_station_id' => $halimId,
                        'departure_time' => $dateStr . ' ' . $base['dep'] . ':00',
                        'arrival_time' => $dateStr . ' ' . $base['arr'] . ':00',
                        'train_class' => $class['name'],
                        'base_price' => $class['price'],
                        'carriages_count' => 8,
                        'booked_seats' => json_encode([]),
                        'created_at' => $now,
                        'updated_at' => $now,
                    ];
                }
            }

            // === HALIM <-> KARAWANG (Intermediate Route) ===
            // Halim -> Karawang
            foreach ($halimKarawangBase as $base) {
                $classes = [
                    ['name' => 'Premium Economy', 'price' => 150000],
                    ['name' => 'Business Class', 'price' => 300000],
                ];

                foreach ($classes as $class) {
                    $tripsToInsert[] = [
                        'trip_id' => 'WSH-' . $base['id'] . '-' . substr($class['name'], 0, 1) . '-' . $dateSuffix,
                        'train_name' => 'Whoosh ' . $base['id'],
                        'departure_station_id' => $halimId,
                        'arrival_station_id' => $karawangId,
                        'departure_time' => $dateStr . ' ' . $base['dep'] . ':00',
                        'arrival_time' => $dateStr . ' ' . $base['arr'] . ':00',
                        'train_class' => $class['name'],
                        'base_price' => $class['price'],
                        'carriages_count' => 6,
                        'booked_seats' => json_encode([]),
                        'created_at' => $now,
                        'updated_at' => $now,
                    ];
                }
            }

            // Karawang -> Halim
            foreach ($karawangHalimBase as $base) {
                $classes = [
                    ['name' => 'Premium Economy', 'price' => 150000],
                    ['name' => 'Business Class', 'price' => 300000],
                ];

                foreach ($classes as $class) {
                    $tripsToInsert[] = [
                        'trip_id' => 'WSH-' . $base['id'] . '-' . substr($class['name'], 0, 1) . '-' . $dateSuffix,
                        'train_name' => 'Whoosh ' . $base['id'],
                        'departure_station_id' => $karawangId,
                        'arrival_station_id' => $halimId,
                        'departure_time' => $dateStr . ' ' . $base['dep'] . ':00',
                        'arrival_time' => $dateStr . ' ' . $base['arr'] . ':00',
                        'train_class' => $class['name'],
                        'base_price' => $class['price'],
                        'carriages_count' => 6,
                        'booked_seats' => json_encode([]),
                        'created_at' => $now,
                        'updated_at' => $now,
                    ];
                }
            }

            // === HALIM <-> PADALARANG (Intermediate Route) ===
            // Halim -> Padalarang
            foreach ($halimPadalarangBase as $base) {
                $classes = [
                    ['name' => 'Premium Economy', 'price' => 200000],
                    ['name' => 'Business Class', 'price' => 400000],
                ];

                foreach ($classes as $class) {
                    $tripsToInsert[] = [
                        'trip_id' => 'WSH-' . $base['id'] . '-' . substr($class['name'], 0, 1) . '-' . $dateSuffix,
                        'train_name' => 'Whoosh ' . $base['id'],
                        'departure_station_id' => $halimId,
                        'arrival_station_id' => $padalarangId,
                        'departure_time' => $dateStr . ' ' . $base['dep'] . ':00',
                        'arrival_time' => $dateStr . ' ' . $base['arr'] . ':00',
                        'train_class' => $class['name'],
                        'base_price' => $class['price'],
                        'carriages_count' => 6,
                        'booked_seats' => json_encode([]),
                        'created_at' => $now,
                        'updated_at' => $now,
                    ];
                }
            }

            // Padalarang -> Halim
            foreach ($padalarangHalimBase as $base) {
                $classes = [
                    ['name' => 'Premium Economy', 'price' => 200000],
                    ['name' => 'Business Class', 'price' => 400000],
                ];

                foreach ($classes as $class) {
                    $tripsToInsert[] = [
                        'trip_id' => 'WSH-' . $base['id'] . '-' . substr($class['name'], 0, 1) . '-' . $dateSuffix,
                        'train_name' => 'Whoosh ' . $base['id'],
                        'departure_station_id' => $padalarangId,
                        'arrival_station_id' => $halimId,
                        'departure_time' => $dateStr . ' ' . $base['dep'] . ':00',
                        'arrival_time' => $dateStr . ' ' . $base['arr'] . ':00',
                        'train_class' => $class['name'],
                        'base_price' => $class['price'],
                        'carriages_count' => 6,
                        'booked_seats' => json_encode([]),
                        'created_at' => $now,
                        'updated_at' => $now,
                    ];
                }
            }

            // Chunk insert to avoid memory issues
            if (count($tripsToInsert) > 500) {
                DB::table('trips')->insert($tripsToInsert);
                $tripsToInsert = [];
            }
        }

        if (!empty($tripsToInsert)) {
            DB::table('trips')->insert($tripsToInsert);
        }
    }
}
