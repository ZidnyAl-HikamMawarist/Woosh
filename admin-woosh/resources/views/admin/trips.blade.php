@extends('layouts.admin')

@section('title', 'Manajemen Jadwal Kereta')

@section('content')
    @if(session('success'))
        <div style="background-color: #dcfce7; color: #166534; padding: 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600; border: 1px solid #bbf7d0;">
            ✅ {{ session('success') }}
        </div>
    @endif

    <div class="card">
        <div class="card-header">
            <h3 class="card-title">Daftar Jadwal Perjalanan (Trips)</h3>
            @if(auth()->user()->role == 'admin')
            <a href="{{ route('admin.trips.create') }}" class="btn btn-primary">+ Tambah Jadwal</a>
            @endif
        </div>
        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Trip ID</th>
                        <th>Kereta & Rute</th>
                        <th>Kelas</th>
                        <th>Waktu</th>
                        <th>Gerbong</th>
                        <th>Harga</th>
                        <th>Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($trips as $trip)
                        <tr>
                            <td style="font-family: monospace;">{{ $trip->trip_id }}</td>
                            <td>
                                <div style="font-weight: 600;">{{ $trip->train_name }}</div>
                                <div class="text-sm">
                                    {{ $trip->departureStation->code ?? '???' }} ➔ {{ $trip->arrivalStation->code ?? '???' }}
                                </div>
                            </td>
                            <td><span class="badge active">{{ $trip->train_class }}</span></td>
                            <td>
                                <div class="text-sm">🛫 {{ \Carbon\Carbon::parse($trip->departure_time)->format('H:i') }}</div>
                                <div class="text-sm">🛬 {{ \Carbon\Carbon::parse($trip->arrival_time)->format('H:i') }}</div>
                            </td>
                            <td><span class="badge pending">{{ $trip->carriages_count }}</span></td>
                            <td>Rp {{ number_format($trip->base_price, 0, ',', '.') }}</td>
                            <td>
                                <div class="dropdown">
                                    <button class="dropdown-toggle">⋮</button>
                                    <div class="dropdown-menu">
                                        @if(auth()->user()->role == 'admin')
                                            <a href="{{ route('admin.trips.edit', $trip->trip_id) }}" class="dropdown-item">✏️ Edit</a>
                                            <form action="{{ route('admin.trips.destroy', $trip->trip_id) }}" method="POST" onsubmit="return confirm('Hapus jadwal ini?');">
                                                @csrf
                                                @method('DELETE')
                                                <button type="submit" class="dropdown-item text-danger">🗑️ Hapus</button>
                                            </form>
                                        @else
                                            <span class="dropdown-item" style="color: var(--text-secondary); font-size: 12px;">Read Only</span>
                                        @endif
                                    </div>
                                </div>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="7" style="text-align: center; color: var(--text-secondary);">Belum ada jadwal kereta yang tersinkronisasi.</td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>

        @if($trips->hasPages())
            <div class="pagination-container">
                {{ $trips->links() }}
            </div>
        @endif
    </div>
@endsection
