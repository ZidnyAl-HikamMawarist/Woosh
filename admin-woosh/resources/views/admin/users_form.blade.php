@extends('layouts.admin')

@section('title', isset($user) ? 'Edit Pengguna' : 'Tambah Pengguna')

@section('content')
    <div class="card" style="max-width: 600px; margin: 0 auto;">
        <div class="card-header">
            <h3 class="card-title">{{ isset($user) ? 'Edit Pengguna' : 'Tambah Pengguna Baru' }}</h3>
            <a href="{{ route('admin.users') }}" class="btn btn-secondary">Kembali</a>
        </div>

        @if ($errors->any())
            <div style="background-color: #fee2e2; color: #991b1b; padding: 16px; border-radius: 8px; margin-bottom: 20px;">
                <ul style="margin-left: 20px;">
                    @foreach ($errors->all() as $error)
                        <li>{{ $error }}</li>
                    @endforeach
                </ul>
            </div>
        @endif

        <form action="{{ isset($user) ? route('admin.users.update', $user->id) : route('admin.users.store') }}" method="POST">
            @csrf
            @if(isset($user))
                @method('PUT')
            @endif

            <div class="form-group">
                <label>Nama Lengkap</label>
                <input type="text" name="name" class="form-control" value="{{ old('name', $user->name ?? '') }}" required>
            </div>

            <div class="form-group">
                <label>Email</label>
                <input type="email" name="email" class="form-control" value="{{ old('email', $user->email ?? '') }}" required>
            </div>

            <div class="form-group">
                <label>Password {{ isset($user) ? '(Kosongkan jika tidak ingin diubah)' : '' }}</label>
                <input type="password" name="password" class="form-control" {{ isset($user) ? '' : 'required' }}>
            </div>

            <div class="form-group">
                <label>Nomor Telepon</label>
                <input type="text" name="phone" class="form-control" value="{{ old('phone', $user->phone ?? '') }}">
            </div>

            <div class="form-group">
                <label>Poin Loyalitas</label>
                <input type="number" name="loyalty_points" class="form-control" value="{{ old('loyalty_points', $user->loyalty_points ?? 0) }}">
            </div>

            <div class="form-group">
                <label>Status Pass (Contoh: Premium, VIP)</label>
                <input type="text" name="active_pass" class="form-control" value="{{ old('active_pass', $user->active_pass ?? '') }}">
            </div>

            <div class="form-group">
                <label>Sisa Trip</label>
                <input type="number" name="remaining_trips" class="form-control" value="{{ old('remaining_trips', $user->remaining_trips ?? 0) }}">
            </div>

            <div class="form-group">
                <label>Tanggal Kedaluwarsa Pass</label>
                <input type="datetime-local" name="pass_expiry_at" class="form-control" value="{{ old('pass_expiry_at', isset($user) && $user->pass_expiry_at ? date('Y-m-d\TH:i', strtotime($user->pass_expiry_at)) : '') }}">
            </div>
            
            <div class="form-group">
                <label>Role / Peran</label>
                <select name="role" class="form-control" required>
                    <option value="user" {{ old('role', $user->role ?? '') == 'user' ? 'selected' : '' }}>User (Pelanggan)</option>
                    <option value="manager" {{ old('role', $user->role ?? '') == 'manager' ? 'selected' : '' }}>Manager (Petugas)</option>
                    <option value="admin" {{ old('role', $user->role ?? '') == 'admin' ? 'selected' : '' }}>Admin (Super User)</option>
                </select>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;">{{ isset($user) ? 'Simpan Perubahan' : 'Tambah Pengguna' }}</button>
        </form>
    </div>
@endsection
