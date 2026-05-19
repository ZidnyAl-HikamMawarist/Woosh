# Spesifikasi Admin Site Woosh (Laravel & MySQL)

Dokumen ini berisi daftar fitur, pemetaan variabel, dan panduan teknis untuk membangun Admin Site Woosh menggunakan framework Laravel dan database MySQL, yang tersinkronisasi dengan Firebase Firestore.

## 1. Pemetaan Variabel (Firestore vs MySQL)
Penting untuk menjaga konsistensi penamaan agar tidak terjadi kesalahan saat proses sinkronisasi data (ETL/Webhooks).

### A. Tabel `users`
| Firestore Field | MySQL Column | Type | Keterangan |
| :--- | :--- | :--- | :--- |
| `name` | `name` | VARCHAR | Nama lengkap user |
| `email` | `email` | VARCHAR | Email (Unique) |
| `phone` | `phone` | VARCHAR | Nomor HP |
| `loyaltyPoints` | `loyalty_points` | BIGINT | Poin loyalitas |
| `activePass` | `active_pass` | VARCHAR | Jenis Pass (Basic/Silver/Gold) |
| `remainingTrips` | `remaining_trips` | INT | Sisa kuota perjalanan pass |
| `expiryDate` | `pass_expiry_at` | DATETIME | Masa berlaku pass |
| `createdAt` | `created_at` | TIMESTAMP | Tanggal daftar |

### B. Tabel `trips` (Jadwal Kereta)
| Firestore Field | MySQL Column | Type | Keterangan |
| :--- | :--- | :--- | :--- |
| (Document ID) | `trip_id` | VARCHAR | UID dari Firestore |
| `name` | `train_name` | VARCHAR | Misal: WOOSH 501 |
| `dep` | `departure_time` | TIME | Jam keberangkatan (HH:mm) |
| `arr` | `arrival_time` | TIME | Jam kedatangan (HH:mm) |
| `trainClass` | `train_class` | VARCHAR | Premium Economy/First Class/Business |
| `price` | `base_price` | DECIMAL | Harga dasar (sebelum dynamic pricing) |
| `bookedSeats` | (Tabel Terpisah) | JSON/TEXT | Daftar kursi yang sudah dipesan |

### C. Tabel `tickets` (Transaksi)
| Firestore Field | MySQL Column | Type | Keterangan |
| :--- | :--- | :--- | :--- |
| `id` | `ticket_code` | VARCHAR | Kode tiket (Misal: WSH-TK-12345) |
| `trainId` | `trip_id` | VARCHAR | Foreign key ke tabel trips |
| `seats` | `seats_list` | VARCHAR | Daftar kursi (Misal: 4A, 4B) |
| `totalPrice` | `total_amount` | DECIMAL | Total bayar (termasuk pajak/diskon) |
| `status` | `status` | ENUM | Aktif, Selesai, Rescheduled, Batal |
| `timestamp` | `booked_at` | TIMESTAMP | Waktu transaksi |

---

## 2. Daftar Fitur Utama Admin Site

### 1. Dashboard Analytics
*   **Total Revenue**: Pantauan pendapatan harian/bulanan dari tiket.
*   **Active Users**: Jumlah pengguna terdaftar.
*   **Top Routes**: Jadwal yang paling sering dipesan.
*   **Seat Occupancy**: Grafik keterisian kursi per kereta.

### 2. Manajemen Jadwal (Trips)
*   **Add/Edit Schedule**: Menambah atau mengubah jam keberangkatan.
*   **Dynamic Pricing Config**: Pengaturan persentase kenaikan harga pada jam sibuk (Peak Hours).
*   **Manual Seat Reset**: Fitur untuk "melepaskan" kursi jika terjadi error sistem.

### 3. Monitoring Tiket & Transaksi
*   **Ticket Logs**: Melihat detail setiap tiket yang dibeli (siapa pembelinya, rutenya, metodenya).
*   **Refund/Reschedule Override**: Admin bisa mengubah status tiket pengguna secara manual jika ada komplain.
*   **Export Data**: Download laporan transaksi dalam format Excel/CSV.

### 4. Manajemen Group Booking (Rombongan)
*   **Inquiry Manager**: Daftar permintaan rombongan (min. 20 orang) yang masuk dari aplikasi.
*   **Status Approval**: Mengubah status dari "Pending" menjadi "Approved" atau "Paid".
*   **Payment Verification**: Upload/verifikasi bukti bayar manual untuk rombongan.

### 5. Loyalitas & Subscription
*   **Whoosher Pass Control**: Melihat daftar pengguna yang memiliki pass aktif.
*   **Points Adjustment**: Admin bisa menambah/mengurangi poin user secara manual (misal: untuk reward khusus).

### 6. Pusat Informasi & Notifikasi
*   **Global Broadcast**: Mengirim notifikasi ke semua pengguna sekaligus (Misal: pengumuman keterlambatan).
*   **FAQ/Info Manager**: Update konten informasi yang muncul di `InformationScreen` aplikasi mobile.

---

## 3. Strategi Sinkronisasi (Firestore ↔ MySQL)

Karena Firestore bersifat NoSQL dan MySQL bersifat Relational, disarankan menggunakan salah satu metode ini:

1.  **Direct Mirroring (Laravel as Proxy)**: 
    *   Setiap kali aplikasi mobile melakukan penulisan ke Firestore, kirimkan juga request ke API Laravel untuk menyimpan salinannya di MySQL.
2.  **Firebase Cloud Functions (Webhooks)**:
    *   Gunakan Firestore Triggers (`onCreate`, `onUpdate`). Setiap ada perubahan data di Firestore, Cloud Function akan mengirimkan webhook ke Laravel Admin Site untuk mengupdate MySQL.
3.  **Scheduled Sync (Cron Job)**:
    *   Laravel menjalankan script setiap jam untuk mengambil data terbaru dari Firestore dan menyinkronkannya ke MySQL.

## 4. Hal Penting Lainnya
*   **Security**: Gunakan **Laravel Passport** atau **Sanctum** untuk mengamankan API yang akan berkomunikasi dengan data sensitif.
*   **Data Integrity**: Pastikan `trip_id` di Firestore selalu sama dengan yang ada di MySQL agar relasi data tidak rusak.
*   **Peak Hour Definition**: Definisikan Peak Hour di database Admin agar logika harga di aplikasi mobile dan admin selalu sinkron.

---
*Dibuat oleh Antigravity AI - Woosh Project Architect*
