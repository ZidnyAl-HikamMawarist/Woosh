@extends('layouts.admin')

@section('title', 'Manajemen Stasiun')

@section('content')
    <div class="card">
        <div class="card-header">
            <h3 class="card-title">Daftar Stasiun Whoosh</h3>
            @if(auth()->user()->role == 'admin')
            <a href="{{ route('admin.stations.create') }}" class="btn btn-primary">+ Tambah Stasiun</a>
            @endif
        </div>
        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Kode</th>
                        <th>Nama Stasiun</th>
                        <th>Kota</th>
                        <th>Fasilitas Unggulan</th>
                        <th>Koordinat</th>
                        @if(auth()->user()->role == 'admin')
                        <th>Aksi</th>
                        @endif
                    </tr>
                </thead>
                <tbody>
                    @forelse($stations as $station)
                        <tr>
                            <td><code>{{ $station->code }}</code></td>
                            <td style="font-weight: 600;">{{ $station->name }}</td>
                            <td>{{ $station->city }}</td>
                            <td style="font-size: 13px;">{{ $station->facilities }}</td>
                            <td style="font-size: 12px; color: var(--text-secondary);">
                                {{ $station->latitude }}, {{ $station->longitude }}
                            </td>
                            @if(auth()->user()->role == 'admin')
                            <td>
                                <div class="dropdown">
                                    <button class="dropdown-toggle">⋮</button>
                                    <div class="dropdown-menu">
                                        <a href="{{ route('admin.stations.edit', $station->id) }}" class="dropdown-item">✏️ Edit</a>
                                        <form action="{{ route('admin.stations.destroy', $station->id) }}" method="POST" onsubmit="return confirm('Hapus stasiun ini?');">
                                            @csrf
                                            @method('DELETE')
                                            <button type="submit" class="dropdown-item text-danger">🗑️ Hapus</button>
                                        </form>
                                    </div>
                                </div>
                            </td>
                            @endif
                        </tr>
                    @empty
                        <tr>
                            <td colspan="6" style="text-align: center; color: var(--text-secondary);">Belum ada data stasiun.</td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>

        @if($stations->hasPages())
            <div class="pagination-container">
                {{ $stations->links() }}
            </div>
        @endif
    </div>
@endsection
