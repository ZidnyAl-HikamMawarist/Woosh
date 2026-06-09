<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Services\FirebaseSyncService;
use Illuminate\Http\Request;
use Kreait\Firebase\Contract\Auth;

class FirebaseController extends Controller
{
    public function __construct(
        private Auth $auth,
        private FirebaseSyncService $syncService
    ) {}

    public function index()
    {
        $users = $this->auth->listUsers();
        $dataUsers = [];

        foreach ($users as $user) {
            $dataUsers[] = [
                'uid' => $user->uid,
                'email' => $user->email,
                'displayName' => $user->displayName,
            ];
        }

        return response()->json([
            'total_data' => count($dataUsers),
            'results' => $dataUsers
        ]);
    }

    /**
     * Sync Firebase Auth users → MySQL
     * Sekaligus sync semua trips MySQL → Firestore
     */
    public function syncToMysql()
    {
        // 1. Sync Firebase Auth users ke MySQL
        $users = $this->auth->listUsers();
        $userCount = 0;

        foreach ($users as $user) {
            if (empty($user->email)) continue;

            \App\Models\User::updateOrCreate(
                ['email' => $user->email],
                [
                    'name'         => $user->displayName ?? 'User Woosh',
                    'phone'        => $user->phoneNumber ?? null,
                    'password'     => bcrypt(\Illuminate\Support\Str::random(16)),
                    'firebase_uid' => $user->uid,
                ]
            );
            $userCount++;
        }

        // 2. Sync semua trips MySQL → Firestore
        $tripCount = $this->syncService->syncAllTrips();

        return redirect()->route('admin.users')->with(
            'success',
            "Sinkronisasi selesai: {$userCount} user dari Firebase Auth, {$tripCount} jadwal ke Firestore."
        );
    }

    public function testConnection()
    {
        try {
            $users = $this->auth->listUsers(1, 1);
            foreach ($users as $user) { break; }
            $status = "Berhasil terhubung ke Firebase!";
            $error = null;
        } catch (\Exception $e) {
            $status = "Gagal terhubung ke Firebase.";
            $error = $e->getMessage();
        }

        return view('firebase-test', compact('status', 'error'));
    }
}
