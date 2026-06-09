<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test Koneksi Firebase</title>
    <style>
        body { 
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
            display: flex; 
            justify-content: center; 
            align-items: center; 
            height: 100vh; 
            background-color: #f3f4f6; 
            margin: 0; 
        }
        .card { 
            background: white; 
            padding: 2rem; 
            border-radius: 12px; 
            box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -2px rgba(0,0,0,0.05); 
            text-align: center; 
            max-width: 600px; 
            width: 90%; 
        }
        .icon {
            font-size: 3rem;
            margin-bottom: 1rem;
        }
        .success { 
            color: #166534; 
            background: #dcfce7; 
            padding: 1.5rem; 
            border-radius: 8px; 
            margin-bottom: 1.5rem; 
            font-weight: 600; 
            border: 1px solid #bbf7d0;
        }
        .error { 
            color: #991b1b; 
            background: #fee2e2; 
            padding: 1.5rem; 
            border-radius: 8px; 
            margin-bottom: 1.5rem; 
            font-weight: 600; 
            word-break: break-all; 
            border: 1px solid #fecaca;
            text-align: left;
        }
        .btn { 
            display: inline-block; 
            padding: 10px 24px; 
            background-color: #3b82f6; 
            color: white; 
            text-decoration: none; 
            border-radius: 8px; 
            font-weight: 500;
            transition: background-color 0.2s;
        }
        .btn:hover { 
            background-color: #2563eb; 
        }
        h2 {
            color: #1f2937;
            margin-top: 0;
            margin-bottom: 1.5rem;
        }
        .error-detail {
            margin-top: 10px;
            font-size: 0.875rem;
            color: #7f1d1d;
            font-weight: normal;
            font-family: monospace;
            background: rgba(255,255,255,0.5);
            padding: 10px;
            border-radius: 4px;
        }
    </style>
</head>
<body>
    <div class="card">
        <h2>Status Koneksi Firebase</h2>
        
        @if(isset($error) && $error)
            <div class="icon">❌</div>
            <div class="error">
                <div>{{ $status }}</div>
                <div class="error-detail"><strong>Detail Error:</strong><br>{{ $error }}</div>
            </div>
        @else
            <div class="icon">✅</div>
            <div class="success">
                {{ $status }}
            </div>
        @endif

        <a href="/" class="btn">Kembali ke Beranda</a>
        <button onclick="window.location.reload()" class="btn" style="background-color: #6b7280; margin-left: 10px;">Coba Lagi</button>
    </div>
</body>
</html>
