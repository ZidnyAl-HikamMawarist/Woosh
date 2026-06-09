<?php

namespace App\Services;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

/**
 * FirebaseSyncService
 *
 * Sinkronisasi data MySQL → Firebase Firestore menggunakan
 * Firebase REST API langsung (tanpa ext-grpc).
 *
 * Dokumentasi: https://firebase.google.com/docs/firestore/reference/rest
 */
class FirebaseSyncService
{
    private string $projectId;
    private string $credentialsPath;
    private ?string $accessToken = null;
    private int $tokenExpiry = 0;

    public function __construct()
    {
        $this->projectId = env('FIREBASE_PROJECT_ID', 'wooshh-43000');

        $credPath = env('FIREBASE_CREDENTIALS', 'storage/app/firebase-auth.json');
        $this->credentialsPath = (str_starts_with($credPath, '/') || str_contains($credPath, ':\\'))
            ? $credPath
            : base_path($credPath);
    }

    // =========================================================
    // TRIPS (Jadwal Kereta)
    // =========================================================

    public function syncTrip(array $trip): void
    {
        try {
            $dep = is_string($trip['departure_time'])
                ? (strlen($trip['departure_time']) > 5 ? substr($trip['departure_time'], 11, 5) : $trip['departure_time'])
                : (string) $trip['departure_time'];

            $arr = is_string($trip['arrival_time'])
                ? (strlen($trip['arrival_time']) > 5 ? substr($trip['arrival_time'], 11, 5) : $trip['arrival_time'])
                : (string) $trip['arrival_time'];

            $this->setDocument('trips', $trip['trip_id'], [
                'name'        => $trip['train_name'],
                'dep'         => $dep,
                'arr'         => $arr,
                'trainClass'  => $trip['train_class'],
                'price'       => (int) $trip['base_price'],
                'bookedSeats' => $trip['booked_seats'] ?? [],
            ]);

            Log::info("[FirebaseSync] Trip synced: {$trip['trip_id']}");
        } catch (\Throwable $e) {
            Log::error("[FirebaseSync] Failed to sync trip {$trip['trip_id']}: " . $e->getMessage());
        }
    }

    public function deleteTrip(string $tripId): void
    {
        try {
            $this->deleteDocument('trips', $tripId);
            Log::info("[FirebaseSync] Trip deleted: {$tripId}");
        } catch (\Throwable $e) {
            Log::error("[FirebaseSync] Failed to delete trip {$tripId}: " . $e->getMessage());
        }
    }

    // =========================================================
    // USERS (Poin & Pass)
    // =========================================================

    public function syncUserPoints(string $firebaseUid, array $userData): void
    {
        if (empty($firebaseUid)) return;

        try {
            $this->patchDocument('users', $firebaseUid, [
                'loyaltyPoints'  => (int) ($userData['loyalty_points'] ?? 0),
                'activePass'     => $userData['active_pass'] ?? null,
                'remainingTrips' => (int) ($userData['remaining_trips'] ?? 0),
                'expiryDate'     => $userData['pass_expiry_at'] ?? null,
                'name'           => $userData['name'] ?? null,
                'email'          => $userData['email'] ?? null,
                'phone'          => $userData['phone'] ?? null,
            ]);

            Log::info("[FirebaseSync] User synced: {$firebaseUid}");
        } catch (\Throwable $e) {
            Log::error("[FirebaseSync] Failed to sync user {$firebaseUid}: " . $e->getMessage());
        }
    }

    // =========================================================
    // TICKETS (Status)
    // =========================================================

    public function syncTicketStatus(string $firebaseUid, string $firestoreTicketId, string $status): void
    {
        if (empty($firebaseUid) || empty($firestoreTicketId)) return;

        $statusMap = ['Active' => 'Aktif', 'Used' => 'Selesai', 'Batal' => 'Batal'];
        $firestoreStatus = $statusMap[$status] ?? $status;

        try {
            $path = "users/{$firebaseUid}/tickets";
            $this->patchDocument($path, $firestoreTicketId, ['status' => $firestoreStatus]);
            Log::info("[FirebaseSync] Ticket status synced: {$firestoreTicketId} → {$firestoreStatus}");
        } catch (\Throwable $e) {
            Log::error("[FirebaseSync] Failed to sync ticket {$firestoreTicketId}: " . $e->getMessage());
        }
    }

    // =========================================================
    // NOTIFICATIONS
    // =========================================================

    public function sendNotification(string $firebaseUid, string $title, string $body, string $type = 'INFO'): void
    {
        if (empty($firebaseUid)) return;

        try {
            $notifId = 'NF' . now()->timestamp;
            $path = "users/{$firebaseUid}/notifications";
            $this->setDocument($path, $notifId, [
                'id'        => $notifId,
                'title'     => $title,
                'body'      => $body,
                'type'      => $type,
                'isRead'    => false,
                'timestamp' => now()->toIso8601String(),
            ]);
            Log::info("[FirebaseSync] Notification sent to {$firebaseUid}: {$title}");
        } catch (\Throwable $e) {
            Log::error("[FirebaseSync] Failed to send notification to {$firebaseUid}: " . $e->getMessage());
        }
    }

    // =========================================================
    // BULK SYNC
    // =========================================================

    public function syncAllTrips(): int
    {
        $trips = \App\Models\Trip::all();
        $count = 0;
        foreach ($trips as $trip) {
            $this->syncTrip($trip->toArray());
            $count++;
        }
        Log::info("[FirebaseSync] Bulk sync: {$count} trips.");
        return $count;
    }

    // =========================================================
    // PRIVATE: REST API Helpers
    // =========================================================

    private function setDocument(string $collection, string $docId, array $data): void
    {
        $url = $this->buildUrl($collection, $docId);
        $body = ['fields' => $this->encodeFields($data)];

        $response = Http::withToken($this->getAccessToken())->patch($url, $body);

        if (!$response->successful()) {
            throw new \RuntimeException("Firestore SET failed [{$response->status()}]: " . $response->body());
        }
    }

    private function patchDocument(string $collection, string $docId, array $data): void
    {
        $url = $this->buildUrl($collection, $docId);
        $fieldPaths = array_keys($data);
        $maskQuery = implode('&', array_map(fn($f) => 'updateMask.fieldPaths=' . urlencode($f), $fieldPaths));

        $body = ['fields' => $this->encodeFields($data)];

        $response = Http::withToken($this->getAccessToken())->patch($url . '?' . $maskQuery, $body);

        if (!$response->successful()) {
            throw new \RuntimeException("Firestore PATCH failed [{$response->status()}]: " . $response->body());
        }
    }

    private function deleteDocument(string $collection, string $docId): void
    {
        $url = $this->buildUrl($collection, $docId);
        $response = Http::withToken($this->getAccessToken())->delete($url);

        if (!$response->successful() && $response->status() !== 404) {
            throw new \RuntimeException("Firestore DELETE failed [{$response->status()}]: " . $response->body());
        }
    }

    private function buildUrl(string $collection, string $docId): string
    {
        $collection = trim($collection, '/');
        return "https://firestore.googleapis.com/v1/projects/{$this->projectId}/databases/(default)/documents/{$collection}/{$docId}";
    }

    private function encodeFields(array $data): array
    {
        $fields = [];
        foreach ($data as $key => $value) {
            $fields[$key] = $this->encodeValue($value);
        }
        return $fields;
    }

    private function encodeValue(mixed $value): array
    {
        if (is_null($value))  return ['nullValue' => null];
        if (is_bool($value))  return ['booleanValue' => $value];
        if (is_int($value))   return ['integerValue' => (string) $value];
        if (is_float($value)) return ['doubleValue' => $value];
        if (is_array($value)) {
            if (array_is_list($value)) {
                return ['arrayValue' => ['values' => array_map([$this, 'encodeValue'], $value)]];
            }
            return ['mapValue' => ['fields' => $this->encodeFields($value)]];
        }
        return ['stringValue' => (string) $value];
    }

    private function getAccessToken(): string
    {
        if ($this->accessToken && time() < $this->tokenExpiry - 60) {
            return $this->accessToken;
        }

        $credentials = json_decode(file_get_contents($this->credentialsPath), true);

        $now = time();
        $payload = [
            'iss'   => $credentials['client_email'],
            'scope' => 'https://www.googleapis.com/auth/datastore',
            'aud'   => 'https://oauth2.googleapis.com/token',
            'iat'   => $now,
            'exp'   => $now + 3600,
        ];

        $jwt = $this->createJwt($payload, $credentials['private_key']);

        $response = Http::asForm()->post('https://oauth2.googleapis.com/token', [
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion'  => $jwt,
        ]);

        if (!$response->successful()) {
            throw new \RuntimeException('Failed to get Firebase access token: ' . $response->body());
        }

        $this->accessToken = $response->json('access_token');
        $this->tokenExpiry = $now + $response->json('expires_in', 3600);

        return $this->accessToken;
    }

    private function createJwt(array $payload, string $privateKey): string
    {
        $header = rtrim(strtr(base64_encode(json_encode(['alg' => 'RS256', 'typ' => 'JWT'])), '+/', '-_'), '=');
        $body   = rtrim(strtr(base64_encode(json_encode($payload)), '+/', '-_'), '=');

        $signingInput = "{$header}.{$body}";
        openssl_sign($signingInput, $signature, $privateKey, OPENSSL_ALGO_SHA256);
        $sig = rtrim(strtr(base64_encode($signature), '+/', '-_'), '=');

        return "{$signingInput}.{$sig}";
    }
}
