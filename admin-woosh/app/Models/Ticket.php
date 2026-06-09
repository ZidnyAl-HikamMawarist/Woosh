<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Ticket extends Model
{
    use \App\Traits\LogsActivity;

    protected $primaryKey = 'ticket_code';
    public $incrementing = false;
    protected $keyType = 'string';

    protected $fillable = [
        'ticket_code',
        'firestore_ticket_id',
        'trip_id',
        'user_id',
        'seats_list',
        'total_amount',
        'payment_method',
        'payment_status',
        'payment_reference',
        'status',
        'booked_at',
    ];

    protected $casts = [
        'total_amount' => 'decimal:2',
        'booked_at' => 'datetime',
    ];

    public function trip()
    {
        return $this->belongsTo(Trip::class, 'trip_id', 'trip_id');
    }

    public function user()
    {
        return $this->belongsTo(User::class);
    }
}
