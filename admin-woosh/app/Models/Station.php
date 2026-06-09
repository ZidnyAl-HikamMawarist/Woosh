<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Station extends Model
{
    use \App\Traits\LogsActivity;

    protected $fillable = [
        'name',
        'code',
        'city',
        'facilities',
        'latitude',
        'longitude',
    ];

    public function departingTrips()
    {
        return $this->hasMany(Trip::class, 'departure_station_id');
    }

    public function arrivingTrips()
    {
        return $this->hasMany(Trip::class, 'arrival_station_id');
    }
}
