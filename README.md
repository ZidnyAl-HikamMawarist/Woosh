# Woosh - Jakarta Bandung High Speed Railway Booking App

## 1. Pengenalan Project
**Woosh** adalah aplikasi mobile berbasis Android yang dirancang untuk mempermudah pengguna dalam memesan tiket Kereta Cepat Jakarta-Bandung (KCJB). Aplikasi ini menawarkan pengalaman pengguna yang modern dengan antarmuka yang elegan, mendukung sistem loyalitas, dan integrasi pembayaran yang beragam. Dibangun menggunakan teknologi terbaru Android seperti Jetpack Compose dan Firebase sebagai backend.

---

## 2. Library & Dependencies
Berikut adalah daftar library utama yang digunakan dalam project ini beserta versinya (berdasarkan `libs.versions.toml`):

### Core & UI
| Library | Versi | Deskripsi |
| --- | --- | --- |
| **Kotlin** | 2.0.21 | Bahasa pemrograman utama. |
| **Jetpack Compose BOM** | 2024.09.00 | Toolkit UI modern untuk Android. |
| **Material 3** | Latest (BOM) | Desain sistem Material Design terbaru. |
| **Navigation Compose** | 2.7.7 | Library navigasi antar screen. |
| **Material Icons Extended** | 1.6.0 | Koleksi icon material lengkap. |

### Backend & Service
| Library | Versi | Deskripsi |
| --- | --- | --- |
| **Firebase BOM** | 33.4.0 | Platform backend (Auth & Firestore). |
| **Firebase Auth** | - | Manajemen autentikasi pengguna. |
| **Firebase Firestore** | - | Database NoSQL real-time. |
| **Google Play Services Auth** | 21.2.0 | Integrasi Google Sign-In (SSO). |
| **Ktor Client** | 2.3.9 | HTTP Client untuk integrasi EmailJS. |

### Dependency Injection & Tooling
| Library | Versi | Deskripsi |
| --- | --- | --- |
| **Dagger Hilt** | 2.59.2 | Library Dependency Injection. |
| **KSP (Kotlin Symbol Processing)** | 2.0.21-1.0.28 | Tooling untuk pengolahan simbol Kotlin. |
| **AGP (Android Gradle Plugin)** | 9.0.0 | Tool build untuk project Android. |

---

## 3. Daftar Fitur (Sudah Berjalan)
Aplikasi Woosh telah memiliki fitur-fitur fungsional berikut:

1.  **Autentikasi Pengguna**:
    *   Registrasi akun baru (Email & Password).
    *   Login (Email & Password).
    *   **Google SSO**: Login cepat menggunakan akun Google.
2.  **Dashboard & Pencarian**:
    *   Informasi saldo point loyalitas.
    *   Banner promosi/informasi.
    *   Form pencarian tiket berdasarkan tujuan dan jumlah penumpang.
3.  **Pemesanan Tiket (Booking Flow)**:
    *   **Train List**: Daftar jadwal kereta dengan kelas berbeda (Business, First Class, Premium Economy).
    *   **Seat Selection**: Pemilihan kursi secara interaktif (Coach & Seat Number).
    *   **Checkout**: Ringkasan pesanan dan penerapan voucher/poin.
4.  **Sistem Pembayaran**:
    *   Integrasi berbagai metode: GoPay, QRIS, ShopeePay, dan Virtual Account.
5.  **Manajemen Tiket**:
    *   Daftar tiket aktif yang telah dibeli.
    *   E-Ticket dengan QR Code untuk check-in.
6.  **Loyalty System**:
    *   Akumulasi poin dari setiap pembelian tiket.
    *   Tampilan level membership dan total poin.
7.  **Profil & Notifikasi**:
    *   Manajemen data diri pengguna.
    *   **Notifikasi Email**: Pengiriman konfirmasi tiket via EmailJS.
    *   **Real-Time In-App Notifications**: Notifikasi status pesanan, pembayaran, dan perubahan jadwal secara langsung.
8.  **Subscription Pass**:
    *   **Frequent Whoosher Pass**: Pilihan bundel tiket (10, 25, 50 perjalanan) dengan harga diskon flat.
9.  **Dynamic Pricing**:
    *   Penyesuaian harga otomatis sebesar 20% selama jam sibuk (07:00–10:00 dan 16:00–19:00).
10. **Layanan Penumpang Lanjutan**:
    *   **Group Booking**: Alur pemesanan khusus rombongan besar (min. 20 orang).
    *   **Passenger Management**: Penyimpanan data NIK/Passport untuk booking cepat.
    *   **Reschedule & Refund**: Fasilitas perubahan jadwal dan pembatalan tiket langsung dari aplikasi.
11. **Pusat Informasi Terintegrasi**:
    *   Panduan KA Feeder, integrasi antarmoda (LRT/Damri), dan aksesibilitas stasiun.

---

## 4. Alur Aplikasi (App Flow)
Urutan penggunaan aplikasi dari awal hingga selesai:

1.  **Splash Screen**: Tampilan awal (logo Woosh) saat aplikasi dibuka.
2.  **Authentication**: Pengguna diarahkan ke Login atau Register (jika belum login).
3.  **Home Screen**: Setelah login, pengguna masuk ke dashboard utama.
4.  **Search & Selection**:
    *   Pilih tujuan dan jumlah penumpang di Home.
    *   Pilih jadwal kereta di **Train List**.
    *   Pilih posisi tempat duduk di **Seat Selection**.
5.  **Checkout & Payment**:
    *   Review pesanan di **Checkout Screen**.
    *   Pilih metode dan lakukan pembayaran di **Payment Screen**.
6.  **Confirmation**:
    *   Pengguna mendapatkan **Ticket** (QR Code).
    *   Sistem mengirimkan konfirmasi via **Email**.
7.  **Post-Booking**: Pengguna dapat melihat tiket mereka di menu **Tiket** atau mengecek poin di menu **Loyalty**.

---

## 5. Fitur Mendatang (Roadmap)
Berikut adalah daftar fitur resmi Woosh yang direncanakan untuk diimplementasikan ke dalam project guna menyamai fungsionalitas aplikasi resmi KCIC:

### Fitur Utama (Krusial)
- [x] **Reschedule Tiket**: Fitur perubahan jadwal keberangkatan secara online sebelum waktu keberangkatan.
- [x] **Refund Tiket**: Mekanisme pembatalan tiket dan pengembalian dana (Refund Account Management).
- [x] **Passenger Management**: Fitur menyimpan daftar data penumpang (NIK/Passport) untuk mempercepat proses booking.

### Fungsionalitas & Layanan
- [x] **Multi-Language Support**: Dukungan Bahasa Indonesia, English, dan 中文 (Chinese).
- [x] **Informasi Kereta Feeder**: Integrasi jadwal dan panduan KA Feeder gratis (Padalarang - Bandung).
- [x] **Frequent Whoosher Card**: Sistem kartu langganan dengan tarif flat untuk perjalanan rutin.
- [x] **Real-Time Notification**: Notifikasi delay, perubahan peron, dan update jadwal secara real-time.
- [x] **Dynamic Pricing**: Tampilan harga dinamis berdasarkan jam sibuk dan hari keberangkatan.

### Informasi & Aksesibilitas
- [x] **Pusat Informasi & Peraturan**: Halaman regulasi kereta api, syarat boarding, dan panduan layanan.
- [x] **Informasi Aksesibilitas**: Detail fasilitas untuk penumpang prioritas (lansia/difabel).
- [x] **Integrasi Antarmoda**: Informasi akses transportasi di setiap stasiun (LRT, Damri, Shuttle).
- [x] **Contact Center**: Integrasi informasi CS (Telepon, WA, Email, Instagram).

### Keamanan & Teknis
- [x] **Group Booking Support**: Informasi dan jalur khusus untuk pemesanan rombongan (min. 20 orang).
- [x] **Integrasi TVM**: Dukungan data untuk cetak tiket fisik di stasiun (Ticket Vending Machine).
- [x] **Account Security**: Fitur ganti password dan opsi penghapusan data akun secara permanen.
