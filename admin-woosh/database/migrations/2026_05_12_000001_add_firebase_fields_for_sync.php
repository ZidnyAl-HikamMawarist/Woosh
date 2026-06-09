<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Menambahkan kolom yang dibutuhkan untuk sinkronisasi dua arah
 * antara MySQL (Laravel) dan Firebase Firestore.
 *
 * - users.firebase_uid       → UID dari Firebase Auth, dipakai sebagai document ID di Firestore
 * - tickets.firestore_ticket_id → Document ID tiket di Firestore subcollection users/{uid}/tickets
 */
return new class extends Migration
{
    public function up(): void
    {
        // Tambah firebase_uid ke tabel users
        Schema::table('users', function (Blueprint $table) {
            if (!Schema::hasColumn('users', 'firebase_uid')) {
                $table->string('firebase_uid')->nullable()->unique()->after('email');
            }
        });

        // Tambah firestore_ticket_id ke tabel tickets
        Schema::table('tickets', function (Blueprint $table) {
            if (!Schema::hasColumn('tickets', 'firestore_ticket_id')) {
                $table->string('firestore_ticket_id')->nullable()->after('ticket_code')
                    ->comment('Document ID tiket di Firestore subcollection users/{uid}/tickets');
            }
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn('firebase_uid');
        });

        Schema::table('tickets', function (Blueprint $table) {
            $table->dropColumn('firestore_ticket_id');
        });
    }
};
