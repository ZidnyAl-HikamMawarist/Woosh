<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Trip extends Model
{
    use \App\Traits\LogsActivity;

    protected $primaryKey = 'trip_id';
    public $incrementing = false;
    protected $keyType = 'string';

    protected $fillable = [
        'trip_id',
        'train_name',
        'departure_station_id',
        'arrival_station_id',
        'departure_time',
        'arrival_time',
        'train_class',
        'base_price',
        'carriages_count',
        'booked_seats',
        'current_latitude',
        'current_longitude',
    ];

    protected $casts = [
        'booked_seats' => 'array',
        'base_price' => 'decimal:2',
    ];

    public function departureStation()
    {
        return $this->belongsTo(Station::class, 'departure_station_id');
    }

    public function arrivalStation()
    {
        return $this->belongsTo(Station::class, 'arrival_station_id');
    }

    public function tickets()
    {
        return $this->hasMany(Ticket::class, 'trip_id', 'trip_id');
    }
}
