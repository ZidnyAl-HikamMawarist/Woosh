@extends('layouts.admin')

@section('title', 'Audit Log Aktivitas')

@section('content')
    <div class="card">
        <div class="card-header">
            <h3 class="card-title">Riwayat Perubahan Data</h3>
        </div>
        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Waktu</th>
                        <th>Admin/User</th>
                        <th>Aksi</th>
                        <th>Model</th>
                        <th>ID Objek</th>
                        <th>Alamat IP</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($logs as $log)
                        <tr>
                            <td>{{ $log->created_at->format('d M Y, H:i:s') }}</td>
                            <td>
                                <div style="font-weight: 600;">{{ $log->user->name ?? 'System' }}</div>
                                <div class="text-sm">{{ $log->user->email ?? '-' }}</div>
                            </td>
                            <td>
                                @php
                                    $badgeColor = match($log->action) {
                                        'created' => '#16a34a',
                                        'updated' => '#f59e0b',
                                        'deleted' => '#dc2626',
                                        default => '#6b7280'
                                    };
                                @endphp
                                <span class="badge" style="background-color: {{ $badgeColor }}; color: white; text-transform: uppercase;">
                                    {{ $log->action }}
                                </span>
                            </td>
                            <td>{{ class_basename($log->model) }}</td>
                            <td><code>#{{ $log->model_id }}</code></td>
                            <td><span class="text-sm">{{ $log->ip_address }}</span></td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="6" style="text-align: center; color: var(--text-secondary); padding: 40px;">
                                Belum ada log aktivitas tercatat.
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>

        @if($logs->hasPages())
            <div class="pagination-container">
                {{ $logs->links() }}
            </div>
        @endif
    </div>
@endsection
