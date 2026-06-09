@extends('layouts.admin')

@section('title', 'Manajemen Pengguna')

@section('content')
    @if(session('success'))
        <div style="background-color: #dcfce7; color: #166534; padding: 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600; border: 1px solid #bbf7d0;">
            ✅ {{ session('success') }}
        </div>
    @endif

    <div class="card">
        <div class="card-header">
            <h3 class="card-title">Daftar Pengguna Terdaftar</h3>
            @if(auth()->user()->role == 'admin')
            <div>
                <a href="{{ route('admin.users.create') }}" class="btn btn-primary">+ Tambah Pengguna</a>
                <a href="{{ route('admin.sync.firebase') }}" style="background-color: #f59e0b; color: white; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; text-decoration: none; display: inline-block; margin-left: 8px;">🔄 Sync Firebase</a>
            </div>
            @endif
        </div>
        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Nama</th>
                        <th>Kontak</th>
                        <th>Poin Loyalitas</th>
                        <th>Status Pass</th>
                        <th>Sisa Trip</th>
                        <th>Role</th>
                        <th>Tanggal Daftar</th>
                        <th>Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($users as $user)
                        <tr>
                            <td style="font-weight: 600;">{{ $user->name }}</td>
                            <td>
                                <div>{{ $user->email }}</div>
                                <div class="text-sm">{{ $user->phone ?? '-' }}</div>
                            </td>
                            <td><span class="badge pending">{{ number_format($user->loyalty_points) }} Pts</span></td>
                            <td>
                                @if($user->active_pass)
                                    <span class="badge active">{{ $user->active_pass }}</span>
                                @else
                                    <span class="badge" style="background: var(--bg-hover); color: var(--text-secondary);">None</span>
                                @endif
                            </td>
                            <td>{{ $user->remaining_trips }}x</td>
                            <td>
                                @if($user->role == 'admin')
                                    <span class="badge active" style="background: #ef4444; color: white;">Admin</span>
                                @elseif($user->role == 'manager')
                                    <span class="badge active" style="background: #3b82f6; color: white;">Manager</span>
                                @else
                                    <span class="badge" style="background: #6b7280; color: white;">User</span>
                                @endif
                            </td>
                            <td>{{ \Carbon\Carbon::parse($user->created_at)->format('d M Y') }}</td>
                            <td>
                                <div class="dropdown">
                                    <button class="dropdown-toggle">⋮</button>
                                    <div class="dropdown-menu">
                                        @if(auth()->user()->role == 'admin')
                                            <a href="{{ route('admin.users.edit', $user->id) }}" class="dropdown-item">✏️ Edit</a>
                                            @if(auth()->user()->id !== $user->id)
                                                <form action="{{ route('admin.users.destroy', $user->id) }}" method="POST" onsubmit="return confirm('Hapus user ini?');">
                                                    @csrf
                                                    @method('DELETE')
                                                    <button type="submit" class="dropdown-item text-danger">🗑️ Hapus</button>
                                                </form>
                                            @endif
                                        @else
                                            <span class="dropdown-item" style="color: var(--text-secondary); font-size: 12px;">Read Only</span>
                                        @endif
                                    </div>
                                </div>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="7" style="text-align: center; color: var(--text-secondary);">Belum ada pengguna terdaftar.</td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>

        @if($users->hasPages())
            <div class="pagination-container">
                {{ $users->links() }}
            </div>
        @endif
    </div>
@endsection
