<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('tickets', function (Blueprint $table) {
            $table->string('ticket_code')->primary();
            $table->string('trip_id');
            $table->foreignId('user_id')->nullable()->constrained('users')->nullOnDelete();
            $table->string('seats_list');
            $table->decimal('total_amount', 10, 2);
            $table->enum('status', ['Aktif', 'Selesai', 'Rescheduled', 'Batal'])->default('Aktif');
            $table->timestamp('booked_at')->useCurrent();
            $table->timestamps();

            // Setup foreign key constraint
            $table->foreign('trip_id')->references('trip_id')->on('trips')->onDelete('cascade');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('tickets');
    }
};
