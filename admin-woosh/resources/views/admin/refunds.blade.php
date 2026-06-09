@extends('layouts.admin')

@section('title', 'Manajemen Pengembalian Dana (Refund)')

@section('content')
    <div class="card">
        <div class="card-header">
            <h3 class="card-title">Permintaan Refund Pending</h3>
        </div>
        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Tanggal</th>
                        <th>Kode Tiket</th>
                        <th>Pengguna</th>
                        <th>Alasan</th>
                        <th>Jumlah</th>
                        <th>Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($pendingRefunds as $refund)
                        <tr>
                            <td>{{ $refund->created_at->format('d M Y') }}</td>
                            <td style="font-weight: 600;">{{ $refund->ticket_code }}</td>
                            <td>{{ $refund->user->name }}</td>
                            <td>{{ $refund->reason }}</td>
                            <td>Rp {{ number_format($refund->amount, 0, ',', '.') }}</td>
                            <td>
                                <div class="dropdown">
                                    <button class="dropdown-toggle">⋮</button>
                                    <div class="dropdown-menu">
                                        <form action="{{ route('admin.refunds.process', $refund->id) }}" method="POST">
                                            @csrf
                                            <input type="hidden" name="status" value="Approved">
                                            <button type="submit" class="dropdown-item" style="color: #16a34a;">✅ Approve</button>
                                        </form>
                                        <form action="{{ route('admin.refunds.process', $refund->id) }}" method="POST">
                                            @csrf
                                            <input type="hidden" name="status" value="Rejected">
                                            <button type="submit" class="dropdown-item text-danger">❌ Reject</button>
                                        </form>
                                    </div>
                                </div>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="6" style="text-align: center; color: var(--text-secondary);">Tidak ada permintaan refund pending.</td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>

        @if($pendingRefunds->hasPages())
            <div class="pagination-container">
                {{ $pendingRefunds->links() }}
            </div>
        @endif
    </div>
@endsection
