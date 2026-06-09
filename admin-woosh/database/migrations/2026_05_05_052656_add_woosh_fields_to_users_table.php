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
        Schema::table('users', function (Blueprint $table) {
            $table->string('phone')->nullable();
            $table->bigInteger('loyalty_points')->default(0);
            $table->string('active_pass')->nullable();
            $table->integer('remaining_trips')->default(0);
            $table->dateTime('pass_expiry_at')->nullable();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn([
                'phone',
                'loyalty_points',
                'active_pass',
                'remaining_trips',
                'pass_expiry_at'
            ]);
        });
    }
};
