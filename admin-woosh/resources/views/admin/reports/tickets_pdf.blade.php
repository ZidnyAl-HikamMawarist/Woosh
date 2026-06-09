<!DOCTYPE html>
<html>
<head>
    <title>Laporan Tiket Woosh</title>
    <style>
        body { font-family: sans-serif; font-size: 12px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .header { text-align: center; margin-bottom: 30px; }
        .summary { margin-top: 20px; font-weight: bold; }
    </style>
</head>
<body>
    <div class="header">
        <h2>Laporan Transaksi Tiket Woosh</h2>
        <p>Tanggal Cetak: {{ date('d F Y H:i') }}</p>
    </div>

    <table>
        <thead>
            <tr>
                <th>No</th>
                <th>Kode Tiket</th>
                <th>Penumpang</th>
                <th>Kereta</th>
                <th>Total Bayar</th>
                <th>Tanggal</th>
                <th>Status</th>
            </tr>
        </thead>
        <tbody>
            @foreach($tickets as $index => $ticket)
            <tr>
                <td>{{ $index + 1 }}</td>
                <td>{{ $ticket->ticket_code }}</td>
                <td>{{ $ticket->user->name ?? '-' }}</td>
                <td>{{ $ticket->trip->train_name ?? '-' }}</td>
                <td>Rp {{ number_format($ticket->total_amount, 0, ',', '.') }}</td>
                <td>{{ \Carbon\Carbon::parse($ticket->booked_at)->format('d/m/Y') }}</td>
                <td>{{ $ticket->status }}</td>
            </tr>
            @endforeach
        </tbody>
    </table>

    <div class="summary">
        Total Pendapatan Terverifikasi: Rp {{ number_format($totalRevenue, 0, ',', '.') }}
    </div>
</body>
</html>
