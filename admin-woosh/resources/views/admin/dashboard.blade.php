@extends('layouts.admin')

@section('title', 'Dashboard Overview')

@section('content')
    <!-- Live Map Section -->
    <div class="card" style="margin-bottom: 32px; padding: 0; overflow: hidden;">
        <div class="card-header" style="padding: 24px;">
            <h3 class="card-title">📍 Live Station & Trip Map</h3>
            <span class="badge active">Interactive</span>
        </div>
        <div id="map" style="height: 400px; width: 100%;"></div>
    </div>

    <div class="grid-4">
        <div class="card">
            <div class="text-sm">Total Pendapatan</div>
            <div class="text-2xl" style="color: var(--brand-color)">Rp {{ number_format($totalRevenue, 0, ',', '.') }}</div>
        </div>
        <div class="card">
            <div class="text-sm">Pengguna Aktif</div>
            <div class="text-2xl">{{ number_format($activeUsers) }}</div>
        </div>
        <div class="card">
            <div class="text-sm">Total Tiket Terjual</div>
            <div class="text-2xl">{{ number_format($totalTickets) }}</div>
        </div>
        <div class="card">
            <div class="text-sm">Status Firebase</div>
            <div style="display: flex; align-items: center; gap: 10px; margin-top: 10px;">
                <div style="width: 12px; height: 12px; background-color: #16a34a; border-radius: 50%;"></div>
                <div class="text-2xl" style="margin-top: 0; font-size: 20px;">Terhubung</div>
            </div>
            <a href="{{ route('admin.sync.firebase') }}" class="text-sm" style="color: var(--brand-color); text-decoration: none; display: block; margin-top: 8px;">Sync Sekarang &rarr;</a>
        </div>
    </div>

    <!-- Live Tracker Section -->
    <div class="card" style="margin-bottom: 32px;">
        <div class="card-header">
            <h3 class="card-title">🚀 Live Train Tracker (Real-time)</h3>
            <span class="badge active">Simulasi</span>
        </div>
        <div style="padding: 20px;">
            @forelse($liveTrips as $trip)
                <div style="margin-bottom: 24px; background: var(--bg-hover); padding: 16px; border-radius: 12px; border: 1px solid var(--border-color);">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                        <div>
                            <span style="font-weight: 700; color: var(--brand-color);">{{ $trip->train_name }}</span>
                            <span class="text-sm" style="margin-left: 8px;">{{ $trip->departureStation->name }} ➔ {{ $trip->arrivalStation->name }}</span>
                        </div>
                        <div class="text-sm" style="font-weight: 600;">{{ \Carbon\Carbon::parse($trip->arrival_time)->diffForHumans() }} tiba</div>
                    </div>
                    
                    @php
                        $start = \Carbon\Carbon::parse($trip->departure_time);
                        $end = \Carbon\Carbon::parse($trip->arrival_time);
                        $total = $start->diffInMinutes($end);
                        $passed = $start->diffInMinutes(now());
                        $percentage = min(100, max(0, ($passed / $total) * 100));
                    @endphp

                    <div style="height: 10px; background: var(--border-color); border-radius: 5px; overflow: hidden; position: relative;">
                        <div style="width: {{ $percentage }}%; height: 100%; background: linear-gradient(90deg, var(--brand-color), #60a5fa); transition: width 1s;"></div>
                        <!-- Train Icon at progress end -->
                        <div style="position: absolute; left: calc({{ $percentage }}% - 10px); top: -5px; font-size: 16px;">🚄</div>
                    </div>
                    <div style="display: flex; justify-content: space-between; margin-top: 10px; font-size: 12px; color: var(--text-secondary);">
                        <span>Berangkat: {{ $start->format('H:i') }}</span>
                        <span>Estimasi Tiba: {{ $end->format('H:i') }}</span>
                    </div>
                </div>
            @empty
                <div style="text-align: center; padding: 20px; color: var(--text-secondary);">
                    <p>Tidak ada kereta yang sedang dalam perjalanan saat ini.</p>
                </div>
            @endforelse
        </div>
    </div>

    <div class="card" style="margin-bottom: 32px; background-color: var(--bg-hover); border-color: var(--brand-color);">
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <h3 class="card-title" style="margin-bottom: 4px;">🚀 Aksi Cepat</h3>
                <p class="text-sm">Kelola operasional harian Woosh dengan satu klik.</p>
            </div>
            <div style="display: flex; gap: 12px;">
                @if(auth()->user()->role == 'admin')
                <a href="{{ route('admin.trips.create') }}" class="btn btn-primary" style="padding: 8px 16px;">+ Jadwal Baru</a>
                <a href="{{ route('admin.tickets.create') }}" class="btn btn-secondary" style="padding: 8px 16px; background: white;">+ Input Tiket</a>
                @endif
                <a href="{{ route('admin.reports.tickets.pdf') }}" class="btn btn-primary" style="padding: 8px 16px; background-color: #16a34a;">📄 Cetak Laporan</a>
                @if(auth()->user()->role == 'admin')
                <a href="{{ route('admin.sync.firebase') }}" class="btn btn-warning" style="padding: 8px 16px;">🔄 Sync Firebase</a>
                @endif
            </div>
        </div>
    </div>

    <div class="grid-4" style="grid-template-columns: 2fr 1fr; margin-bottom: 32px;">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Tren Pendapatan (7 Hari Terakhir)</h3>
            </div>
            <div style="height: 300px;">
                <canvas id="revenueChart"></canvas>
            </div>
        </div>
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Stasiun Terpopuler</h3>
            </div>
            <div style="height: 300px;">
                <canvas id="stationChart"></canvas>
            </div>
        </div>
    </div>

    <div class="card" style="margin-bottom: 32px;">
        <div class="card-header">
            <h3 class="card-title">Jam Padat Transaksi (Peak Hours)</h3>
        </div>
        <div style="height: 200px;">
            <canvas id="peakChart"></canvas>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h3 class="card-title">Transaksi Tiket Terbaru</h3>
            <a href="{{ route('admin.tickets') }}" style="color: var(--brand-color); text-decoration: none; font-size: 14px; font-weight: 600;">Lihat Semua &rarr;</a>
        </div>
        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Kode Tiket</th>
                        <th>Pengguna</th>
                        <th>Kereta</th>
                        <th>Total Bayar</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($recentTickets as $ticket)
                        <tr>
                            <td style="font-weight: 600;">{{ $ticket->ticket_code }}</td>
                            <td>{{ $ticket->user->name ?? 'User Tidak Diketahui' }}</td>
                            <td>{{ $ticket->trip->train_name ?? '-' }}</td>
                            <td>Rp {{ number_format($ticket->total_amount, 0, ',', '.') }}</td>
                            <td>
                                @php
                                    $badgeClass = strtolower($ticket->status) == 'aktif' ? 'active' : (strtolower($ticket->status) == 'batal' ? 'batal' : 'pending');
                                @endphp
                                <span class="badge {{ $badgeClass }}">{{ $ticket->status }}</span>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="5" style="text-align: center; color: var(--text-secondary);">Belum ada transaksi tiket.</td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const ctx = document.getElementById('revenueChart').getContext('2d');
            
            // Ambil tema saat ini untuk menyesuaikan warna grafik
            const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
            const textColor = isDark ? '#94a3b8' : '#64748b';
            const gridColor = isDark ? '#334155' : '#e2e8f0';

            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: {!! json_encode($revenueLabels) !!},
                    datasets: [{
                        label: 'Pendapatan (Rp)',
                        data: {!! json_encode($revenueData) !!},
                        borderColor: '#2563eb',
                        backgroundColor: 'rgba(37, 99, 235, 0.1)',
                        borderWidth: 3,
                        fill: true,
                        tension: 0.4,
                        pointRadius: 4,
                        pointBackgroundColor: '#2563eb'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            grid: { color: gridColor },
                            ticks: { 
                                color: textColor,
                                callback: function(value) {
                                    if (value >= 1000000) return 'Rp ' + (value/1000000) + 'jt';
                                    if (value >= 1000) return 'Rp ' + (value/1000) + 'rb';
                                    return 'Rp ' + value;
                                }
                            }
                        },
                        x: {
                            grid: { display: false },
                            ticks: { color: textColor }
                        }
                    }
                }
            });

            // Station Popularity Chart
            const stationCtx = document.getElementById('stationChart').getContext('2d');
            new Chart(stationCtx, {
                type: 'doughnut',
                data: {
                    labels: {!! json_encode($stationPopularity->pluck('name')) !!},
                    datasets: [{
                        data: {!! json_encode($stationPopularity->pluck('total')) !!},
                        backgroundColor: ['#2563eb', '#7c3aed', '#db2777', '#ea580c', '#16a34a'],
                        borderWidth: 0
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom', labels: { color: textColor } }
                    }
                }
            });

            // Peak Hours Chart
            const peakCtx = document.getElementById('peakChart').getContext('2d');
            new Chart(peakCtx, {
                type: 'bar',
                data: {
                    labels: {!! json_encode($peakHoursData->pluck('hour')->map(fn($h) => $h.':00')) !!},
                    datasets: [{
                        label: 'Jumlah Transaksi',
                        data: {!! json_encode($peakHoursData->pluck('count')) !!},
                        backgroundColor: '#2563eb',
                        borderRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        y: { beginAtZero: true, grid: { color: gridColor }, ticks: { color: textColor } },
                        x: { grid: { display: false }, ticks: { color: textColor } }
                    }
                }
            });
            });

            // Map Initialization
            const map = L.map('map').setView([-6.9147, 107.6098], 8); // Center on Bandung/Java
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(map);

            const stations = {!! json_encode($allStations) !!};
            stations.forEach(station => {
                if (station.latitude && station.longitude) {
                    L.marker([station.latitude, station.longitude])
                        .addTo(map)
                        .bindPopup(`<b>${station.name}</b><br>${station.city}`);
                }
            });
        });
    </script>
@endsection
