@extends('layouts.admin')

@section('title', 'Kirim Notifikasi Broadcast')

@section('content')
    <div class="card" style="max-width: 700px; margin: 0 auto;">
        <div class="card-header">
            <h3 class="card-title">Broadcast Notifikasi ke Aplikasi Mobile</h3>
        </div>

        <div style="padding: 20px;">
            @if(session('success'))
                <div style="background-color: #dcfce7; color: #166534; padding: 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600;">
                    ✅ {{ session('success') }}
                </div>
            @endif

            @if(session('error'))
                <div style="background-color: #fee2e2; color: #991b1b; padding: 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600;">
                    ❌ {{ session('error') }}
                </div>
            @endif

            <p style="color: var(--text-secondary); margin-bottom: 24px;">Pesan ini akan dikirimkan ke seluruh pengguna aplikasi mobile Woosh yang terdaftar pada topik <code>all_users</code>.</p>

            <form action="{{ route('admin.notifications.send') }}" method="POST">
                @csrf
                <div class="form-group">
                    <label>Judul Notifikasi</label>
                    <input type="text" name="title" class="form-control" placeholder="Contoh: Info Keterlambatan Kereta" required>
                </div>

                <div class="form-group">
                    <label>Isi Pesan</label>
                    <textarea name="body" class="form-control" rows="4" placeholder="Tuliskan detail informasi di sini..." required></textarea>
                </div>

                <div style="margin-top: 30px;">
                    <button type="submit" class="btn btn-primary" style="width: 100%; padding: 14px;">🚀 Kirim Notifikasi Sekarang</button>
                </div>
            </form>
        </div>
    </div>

    <div class="card" style="max-width: 700px; margin: 40px auto 0;">
        <div class="card-header">
            <h3 class="card-title">Petunjuk Penggunaan</h3>
        </div>
        <div style="padding: 20px; line-height: 1.6;">
            <ul style="margin-left: 20px;">
                <li>Notifikasi akan muncul di tray HP pengguna meskipun aplikasi ditutup.</li>
                <li>Gunakan judul yang singkat dan menarik (maks. 50 karakter).</li>
                <li>Hindari mengirim terlalu banyak notifikasi dalam satu hari agar tidak dianggap spam.</li>
                <li>Pesan akan otomatis tercatat di log aktivitas sistem.</li>
            </ul>
        </div>
    </div>
@endsection
