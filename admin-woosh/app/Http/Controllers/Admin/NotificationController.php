<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Services\FirebaseSyncService;
use Illuminate\Http\Request;
use Kreait\Firebase\Contract\Messaging;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\Notification;

class NotificationController extends Controller
{
    public function __construct(
        private Messaging $messaging,
        private FirebaseSyncService $syncService
    ) {}

    public function index()
    {
        return view('admin.notifications');
    }

    /**
     * Kirim broadcast notification ke semua user.
     * 1. FCM topic push (untuk push notification di device)
     * 2. Tulis ke Firestore subcollection notifications setiap user
     *    (agar muncul di NotificationScreen yang baca Firestore)
     */
    public function sendBroadcast(Request $request)
    {
        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'body'  => 'required|string',
            'type'  => 'nullable|in:INFO,SUCCESS,ALERT,WARNING',
        ]);

        $type = $validated['type'] ?? 'INFO';
        $errors = [];

        // 1. FCM push via topic
        try {
            $message = CloudMessage::fromArray([
                'topic' => 'all_users',
                'notification' => [
                    'title' => $validated['title'],
                    'body'  => $validated['body'],
                ],
            ]);
            $this->messaging->send($message);
        } catch (\Exception $e) {
            $errors[] = 'FCM: ' . $e->getMessage();
        }

        // 2. Tulis ke Firestore subcollection setiap user yang punya firebase_uid
        $users = User::whereNotNull('firebase_uid')->get();
        $notifCount = 0;

        foreach ($users as $user) {
            $this->syncService->sendNotification(
                $user->firebase_uid,
                $validated['title'],
                $validated['body'],
                $type
            );
            $notifCount++;
        }

        $message = "Broadcast dikirim ke {$notifCount} user via Firestore.";
        if (!empty($errors)) {
            $message .= ' FCM error: ' . implode(', ', $errors);
        }

        return redirect()->back()->with('success', $message);
    }
}
