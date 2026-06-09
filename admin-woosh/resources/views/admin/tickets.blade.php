@extends('layouts.admin')

@section('title', 'Riwayat Transaksi Tiket')

@section('content')
    @if(session('success'))
        <div style="background-color: #dcfce7; color: #166534; padding: 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600; border: 1px solid #bbf7d0;">
            ✅ {{ session('success') }}
        </div>
    @endif

    <div class="card">
        <div class="card-header">
            <h3 class="card-title">Semua Transaksi</h3>
            <div style="display: flex; gap: 10px;">
                <a href="{{ route('admin.reports.tickets.pdf') }}" class="btn btn-secondary" style="background-color: #16a34a; color: white; border: none;">📄 Cetak PDF</a>
                @if(auth()->user()->role == 'admin')
                <a href="{{ route('admin.tickets.create') }}" class="btn btn-primary">+ Tambah Tiket</a>
                @endif
            </div>
        </div>
        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Tanggal Booking</th>
                        <th>Kode Tiket</th>
                        <th>Pengguna</th>
                        <th>Kereta</th>
                        <th>Kursi</th>
                        <th>Total Bayar</th>
                        <th>Metode Bayar</th>
                        <th>Status Bayar</th>
                        <th>Status Tiket</th>
                        <th>Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($tickets as $ticket)
                        <tr>
                            <td>{{ \Carbon\Carbon::parse($ticket->booked_at)->format('d M Y, H:i') }}</td>
                            <td style="font-weight: 600;">{{ $ticket->ticket_code }}</td>
                            <td>
                                <div>{{ $ticket->user->name ?? 'User Tidak Diketahui' }}</div>
                                <div class="text-sm">{{ $ticket->user->email ?? '-' }}</div>
                            </td>
                            <td>{{ $ticket->trip->train_name ?? '-' }}</td>
                            <td>{{ $ticket->seats_list }}</td>
                            <td>Rp {{ number_format($ticket->total_amount, 0, ',', '.') }}</td>
                            <td>{{ $ticket->payment_method ?? '-' }}</td>
                            <td>
                                @php
                                    $payBadgeClass = $ticket->payment_status == 'paid' ? 'active' : ($ticket->payment_status == 'pending' ? 'pending' : 'batal');
                                @endphp
                                <span class="badge {{ $payBadgeClass }}">{{ strtoupper($ticket->payment_status) }}</span>
                            </td>
                            <td>
                                @php
                                    $badgeClass = strtolower($ticket->status) == 'aktif' ? 'active' : (strtolower($ticket->status) == 'batal' ? 'batal' : 'pending');
                                @endphp
                                <span class="badge {{ $badgeClass }}">{{ $ticket->status }}</span>
                            </td>
                            <td>
                                <div class="dropdown">
                                    <button class="dropdown-toggle">⋮</button>
                                    <div class="dropdown-menu">
                                        @if($ticket->payment_status == 'pending')
                                            <form action="{{ route('payments.simulate', $ticket->ticket_code) }}" method="POST">
                                                @csrf
                                                <button type="submit" class="dropdown-item" style="color: #16a34a;">💰 Bayar (Mock)</button>
                                            </form>
                                        @endif

                                        @if(strtolower($ticket->status) == 'active')
                                            <form action="{{ route('admin.tickets.validate', $ticket->ticket_code) }}" method="POST">
                                                @csrf
                                                <button type="submit" class="dropdown-item">✅ Validasi</button>
                                            </form>
                                            <button class="dropdown-item" onclick="showQR('{{ $ticket->ticket_code }}')">🔍 Lihat QR</button>
                                        @endif

                                        @if(auth()->user()->role == 'admin')
                                            <a href="{{ route('admin.tickets.edit', $ticket->ticket_code) }}" class="dropdown-item">✏️ Edit Tiket</a>
                                            
                                            @if(strtolower($ticket->status) != 'batal' && strtolower($ticket->status) != 'used')
                                                <form action="{{ route('admin.tickets.refund', $ticket->ticket_code) }}" method="POST" onsubmit="return confirm('Proses refund tiket ini?');">
                                                    @csrf
                                                    <button type="submit" class="dropdown-item" style="color: #f97316;">💸 Refund</button>
                                                </form>
                                            @endif

                                            <form action="{{ route('admin.tickets.destroy', $ticket->ticket_code) }}" method="POST" onsubmit="return confirm('Hapus tiket ini?');">
                                                @csrf
                                                @method('DELETE')
                                                <button type="submit" class="dropdown-item text-danger">🗑️ Hapus</button>
                                            </form>
                                        @endif
                                    </div>
                                </div>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="8" style="text-align: center; color: var(--text-secondary);">Belum ada riwayat transaksi tiket.</td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>

        @if($tickets->hasPages())
            <div class="pagination-container">
                {{ $tickets->links() }}
            </div>
        @endif
    </div>
    </div>

    <!-- QR Modal -->
    <div id="qrModal" style="display:none; position:fixed; z-index:1000; left:0; top:0; width:100%; height:100%; background:rgba(0,0,0,0.5); justify-content:center; align-items:center;">
        <div class="card" style="width:300px; text-align:center;">
            <h3>Kode Tiket QR</h3>
            <div id="qrCodeContainer" style="margin:20px 0;"></div>
            <p id="qrTicketCode" style="font-weight:600;"></p>
            <button onclick="closeQR()" class="btn btn-secondary" style="width:100%;">Tutup</button>
        </div>
    </div>

    <script>
        function showQR(code) {
            const container = document.getElementById('qrCodeContainer');
            const ticketCodeText = document.getElementById('qrTicketCode');
            const modal = document.getElementById('qrModal');
            
            // Generate QR using Google Charts API
            const qrUrl = `https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=${code}&choe=UTF-8`;
            container.innerHTML = `<img src="${qrUrl}" alt="QR Code" style="border: 1px solid #ddd; padding: 10px; border-radius: 8px;">`;
            ticketCodeText.innerText = code;
            modal.style.display = 'flex';
        }

        function closeQR() {
            document.getElementById('qrModal').style.display = 'none';
        }
    </script>
@endsection
