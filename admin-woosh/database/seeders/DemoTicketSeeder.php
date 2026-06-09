<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\Ticket;
use App\Models\Trip;
use App\Models\User;

class DemoTicketSeeder extends Seeder
{
    public function run(): void
    {
        $trip = Trip::first();
        $user = User::first();

        if ($trip && $user) {
            Ticket::updateOrCreate(
                ['ticket_code' => 'TK-DEMO-001'],
                [
                    'trip_id' => $trip->trip_id,
                    'user_id' => $user->id,
                    'seats_list' => 'A1, A2',
                    'total_amount' => 500000,
                    'payment_status' => 'pending',
                    'status' => 'Aktif',
                    'booked_at' => now(),
                ]
            );
            Ticket::updateOrCreate(
                ['ticket_code' => 'TK-DEMO-002'],
                [
                    'trip_id' => $trip->trip_id,
                    'user_id' => $user->id,
                    'seats_list' => 'B5',
                    'total_amount' => 250000,
                    'payment_status' => 'pending',
                    'status' => 'Aktif',
                    'booked_at' => now(),
                ]
            );
        }
    }
}
