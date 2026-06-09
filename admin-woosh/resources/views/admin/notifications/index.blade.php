@extends('layouts.admin')

@section('title', 'Broadcast Pengumuman')

@section('content')
    <div class="card" style="max-width: 800px; margin: 0 auto;">
        <div class="card-header">
            <h3 class="card-title">Kirim Push Notification</h3>
        </div>
        
        @if(session('success'))
            <div style="background-color: #dcfce7; color: #166534; padding: 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600; border: 1px solid #bbf7d0;">
                ✅ {{ session('success') }}
            </div>
        @endif

        @if(auth()->user()->role == 'admin')
        <form action="{{ route('admin.notifications.send') }}" method="POST">
            @csrf
            <div class="form-group">
                <label for="title">Judul Notifikasi</label>
                <input type="text" name="title" id="title" class="form-control" placeholder="Contoh: Promo Tiket Akhir Pekan!" required>
            </div>

            <div class="form-group">
                <label for="message">Pesan / Pengumuman</label>
                <textarea name="message" id="message" class="form-control" rows="5" placeholder="Tuliskan pesan yang akan dikirim ke semua pengguna aplikasi mobile..." required></textarea>
            </div>

            <div style="margin-top: 24px; display: flex; gap: 12px;">
                <button type="submit" class="btn btn-primary" style="flex: 1;">🚀 Kirim Sekarang</button>
                <button type="reset" class="btn btn-secondary">Reset</button>
            </div>
        </form>
        @else
            <div style="text-align: center; padding: 40px; color: var(--text-secondary);">
                <span style="font-size: 40px;">🔒</span>
                <p>Hanya Admin yang dapat mengirim broadcast pengumuman.</p>
            </div>
        @endif

        <div style="margin-top: 32px; padding: 16px; background-color: var(--bg-hover); border-radius: 8px; border-left: 4px solid var(--brand-color);">
            <p style="font-size: 14px; margin-bottom: 0;">
                <strong>Info:</strong> Pesan ini akan dikirimkan ke seluruh pengguna yang telah menginstal aplikasi mobile Woosh melalui <strong>Firebase Cloud Messaging (FCM)</strong>.
            </p>
        </div>
    </div>
@endsection
