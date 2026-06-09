<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class StationSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $stations = [
            [
                'name' => 'Stasiun Halim',
                'code' => 'HLM',
                'city' => 'Jakarta Timur',
                'facilities' => 'Ruang Tunggu VIP, Food Court, Akses LRT Jabodebek',
                'latitude' => -6.2464,
                'longitude' => 106.8906,
            ],
            [
                'name' => 'Stasiun Karawang',
                'code' => 'KWG',
                'city' => 'Karawang',
                'facilities' => 'Parkir Luas, Mushola, Area Komersial',
                'latitude' => -6.3491,
                'longitude' => 107.2831,
            ],
            [
                'name' => 'Stasiun Padalarang',
                'code' => 'PDL',
                'city' => 'Bandung Barat',
                'facilities' => 'Koneksi KA Feeder, Ruang Tunggu, Area Retail',
                'latitude' => -6.8406,
                'longitude' => 107.4914,
            ],
            [
                'name' => 'Stasiun Tegalluar Summarecon',
                'code' => 'TGL',
                'city' => 'Kabupaten Bandung',
                'facilities' => 'Shuttle Bus, Drop-off Area, Parkir Luas',
                'latitude' => -6.9691,
                'longitude' => 107.7128,
            ],
        ];

        foreach ($stations as $station) {
            \App\Models\Station::updateOrCreate(['code' => $station['code']], $station);
        }
    }
}
