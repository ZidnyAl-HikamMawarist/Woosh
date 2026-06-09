<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>@yield('title', 'Admin Dashboard') - Woosh Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin=""/>
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
    <style>
        :root {
            /* Light Mode Variables */
            --bg-body: #f8fafc;
            --bg-sidebar: #ffffff;
            --bg-card: #ffffff;
            --bg-hover: #f1f5f9;
            --text-primary: #0f172a;
            --text-secondary: #64748b;
            --border-color: #e2e8f0;
            --brand-color: #2563eb;
            --brand-hover: #1d4ed8;
            --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
            --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
        }

        [data-theme="dark"] {
            /* Dark Mode Variables */
            --bg-body: #0f172a;
            --bg-sidebar: #1e293b;
            --bg-card: #1e293b;
            --bg-hover: #334155;
            --text-primary: #f8fafc;
            --text-secondary: #94a3b8;
            --border-color: #334155;
            --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.5);
            --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.3), 0 2px 4px -2px rgb(0 0 0 / 0.3);
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Plus Jakarta Sans', sans-serif;
            transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
        }

        body {
            background-color: var(--bg-body);
            color: var(--text-primary);
            display: flex;
            min-height: 100vh;
        }

        /* Sidebar */
        .sidebar {
            width: 260px;
            background-color: var(--bg-sidebar);
            border-right: 1px solid var(--border-color);
            display: flex;
            flex-direction: column;
            position: fixed;
            height: 100vh;
            z-index: 100;
        }

        .sidebar-header {
            padding: 24px;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .sidebar-header h2 {
            font-size: 24px;
            font-weight: 700;
            color: var(--brand-color);
            letter-spacing: -0.5px;
        }

        .nav-menu {
            padding: 24px 16px;
            display: flex;
            flex-direction: column;
            gap: 8px;
            flex: 1;
        }

        .nav-link {
            display: flex;
            align-items: center;
            padding: 12px 16px;
            text-decoration: none;
            color: var(--text-secondary);
            font-weight: 500;
            border-radius: 8px;
            transition: all 0.2s ease;
        }

        .nav-link:hover, .nav-link.active {
            background-color: var(--bg-hover);
            color: var(--brand-color);
        }

        /* Main Content */
        .main-content {
            flex: 1;
            margin-left: 260px;
            display: flex;
            flex-direction: column;
        }

        /* Topbar */
        .topbar {
            height: 72px;
            background-color: var(--bg-sidebar);
            border-bottom: 1px solid var(--border-color);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 32px;
            position: sticky;
            top: 0;
            z-index: 90;
        }

        .topbar-title {
            font-size: 20px;
            font-weight: 600;
        }

        .topbar-actions {
            display: flex;
            align-items: center;
            gap: 16px;
        }

        /* Theme Toggle Button */
        .theme-toggle {
            background: none;
            border: 1px solid var(--border-color);
            padding: 8px;
            border-radius: 50%;
            cursor: pointer;
            color: var(--text-secondary);
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .theme-toggle:hover {
            background-color: var(--bg-hover);
            color: var(--brand-color);
        }

        /* Content Area */
        .content-area {
            padding: 32px;
            flex: 1;
        }

        /* Cards */
        .card {
            background-color: var(--bg-card);
            border-radius: 12px;
            padding: 24px;
            box-shadow: var(--shadow-sm);
            border: 1px solid var(--border-color);
        }

        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .card-title {
            font-size: 18px;
            font-weight: 600;
        }

        /* Tables */
        .table-container {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
        }

        th, td {
            padding: 16px;
            border-bottom: 1px solid var(--border-color);
        }

        th {
            background-color: var(--bg-hover);
            color: var(--text-secondary);
            font-weight: 600;
            font-size: 13px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        /* Pagination Styling */
        .pagination-container {
            margin-top: 24px;
            display: flex;
            justify-content: center;
        }

        .pagination {
            display: flex;
            list-style: none;
            gap: 8px;
        }

        .pagination li a, .pagination li span {
            display: flex;
            align-items: center;
            justify-content: center;
            min-width: 36px;
            height: 36px;
            padding: 0 12px;
            border-radius: 8px;
            border: 1px solid var(--border-color);
            text-decoration: none;
            color: var(--text-secondary);
            font-size: 14px;
            font-weight: 500;
            background-color: var(--bg-card);
            transition: all 0.2s;
        }

        .pagination li a:hover {
            background-color: var(--bg-hover);
            color: var(--brand-color);
            border-color: var(--brand-color);
        }

        .pagination li.active span {
            background-color: var(--brand-color);
            color: #ffffff;
            border-color: var(--brand-color);
        }

        .pagination li.disabled span {
            opacity: 0.5;
            cursor: not-allowed;
        }

        tr:hover td {
            background-color: var(--bg-hover);
        }

        /* Badges */
        .badge {
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            display: inline-block;
        }
        .badge.active { background-color: #dcfce7; color: #166534; }
        .badge.pending { background-color: #fef08a; color: #854d0e; }
        .badge.batal { background-color: #fee2e2; color: #991b1b; }
        
        [data-theme="dark"] .badge.active { background-color: rgba(22, 163, 74, 0.2); color: #4ade80; }
        [data-theme="dark"] .badge.pending { background-color: rgba(202, 138, 4, 0.2); color: #facc15; }
        [data-theme="dark"] .badge.batal { background-color: rgba(220, 38, 38, 0.2); color: #f87171; }

        /* Utilities */
        .grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; margin-bottom: 32px; }
        .text-2xl { font-size: 28px; font-weight: 700; margin-top: 8px; }
        .text-sm { font-size: 14px; color: var(--text-secondary); }

        /* Forms */
        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
            color: var(--text-primary);
        }

        .form-control {
            width: 100%;
            padding: 10px 14px;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            background-color: var(--bg-body);
            color: var(--text-primary);
            font-family: inherit;
            font-size: 14px;
            transition: border-color 0.2s;
        }

        .form-control:focus {
            outline: none;
            border-color: var(--brand-color);
            box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
        }

        .btn {
            display: inline-block;
            padding: 10px 20px;
            border-radius: 8px;
            font-weight: 600;
            cursor: pointer;
            border: none;
            text-decoration: none;
            text-align: center;
            transition: all 0.2s;
        }

        .btn-primary {
            background-color: var(--brand-color);
            color: white;
        }

        .btn-primary:hover {
            background-color: var(--brand-hover);
        }

        .btn-danger {
            background-color: #dc2626;
            color: white;
        }

        .btn-danger:hover {
            background-color: #b91c1c;
        }

        .btn-warning {
            background-color: #f59e0b;
            color: white;
        }

        .btn-warning:hover {
            background-color: #d97706;
        }
        
        .btn-secondary {
            background-color: var(--bg-hover);
            color: var(--text-primary);
            border: 1px solid var(--border-color);
        }

        .btn-secondary:hover {
            background-color: var(--border-color);
        }

        .action-buttons {
            display: flex;
            gap: 8px;
        }

        .action-buttons .btn {
            padding: 6px 12px;
            font-size: 12px;
        }

        /* Dropdown Action Menu */
        .dropdown {
            position: relative;
            display: inline-block;
        }

        .dropdown-toggle {
            background: none;
            border: 1px solid var(--border-color);
            padding: 8px 12px;
            border-radius: 6px;
            cursor: pointer;
            color: var(--text-secondary);
            font-size: 18px;
            line-height: 1;
            transition: all 0.2s;
        }

        .dropdown-toggle:hover {
            background-color: var(--bg-hover);
            color: var(--brand-color);
        }

        .dropdown-menu {
            display: none;
            position: absolute;
            right: 0;
            top: 100%;
            background-color: var(--bg-card);
            min-width: 160px;
            box-shadow: var(--shadow-md);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            z-index: 1000;
            margin-top: 4px;
            overflow: hidden;
        }

        .dropdown.active .dropdown-menu {
            display: block;
        }

        .dropdown-item {
            display: block;
            padding: 10px 16px;
            text-decoration: none;
            color: var(--text-primary);
            font-size: 14px;
            transition: background-color 0.2s;
            border: none;
            background: none;
            width: 100%;
            text-align: left;
            cursor: pointer;
        }

        .dropdown-item:hover {
            background-color: var(--bg-hover);
            color: var(--brand-color);
        }

        .dropdown-item.text-danger:hover {
            background-color: #fee2e2;
            color: #dc2626;
        }
    </style>
</head>
<body>
    <!-- Sidebar -->
    <aside class="sidebar">
        <div class="sidebar-header">
            <h2>🚄 Woosh Admin</h2>
        </div>
        <nav class="nav-menu">
            <a href="{{ route('admin.dashboard') }}" class="nav-link {{ request()->routeIs('admin.dashboard') ? 'active' : '' }}">📊 Dashboard</a>
            <a href="{{ route('admin.stations') }}" class="nav-link {{ request()->routeIs('admin.stations*') ? 'active' : '' }}">🚉 Stasiun</a>
            <a href="{{ route('admin.trips') }}" class="nav-link {{ request()->routeIs('admin.trips') ? 'active' : '' }}">🚆 Jadwal Kereta</a>
            <a href="{{ route('admin.tickets') }}" class="nav-link {{ request()->routeIs('admin.tickets') ? 'active' : '' }}">🎟️ Transaksi Tiket</a>
            <a href="{{ route('admin.users') }}" class="nav-link {{ request()->routeIs('admin.users') ? 'active' : '' }}">👥 Pengguna</a>
            <a href="{{ route('admin.notifications') }}" class="nav-link {{ request()->routeIs('admin.notifications') ? 'active' : '' }}">📢 Broadcast</a>
            @if(auth()->user()->role == 'admin')
            <a href="{{ route('admin.refunds') }}" class="nav-link {{ request()->routeIs('admin.refunds') ? 'active' : '' }}">💸 Refund Requests</a>
            <a href="{{ route('admin.logs') }}" class="nav-link {{ request()->routeIs('admin.logs') ? 'active' : '' }}">📜 Audit Log</a>
            @endif
            <a href="/test-firebase" class="nav-link" style="margin-top: auto;">🔥 Cek Firebase</a>
        </nav>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
        <!-- Topbar -->
        <header class="topbar">
            <div class="topbar-title">@yield('title')</div>
            <div class="topbar-actions">
                <div style="text-align: right; margin-right: 12px;">
                    <div style="font-weight: 600; font-size: 14px;">{{ auth()->user()->name }}</div>
                    <div style="font-size: 12px; color: var(--text-secondary); text-transform: capitalize;">{{ auth()->user()->role }}</div>
                </div>
                <button class="theme-toggle" id="themeToggle" title="Toggle Dark/Light Mode">
                    🌙
                </button>
                <form method="POST" action="{{ route('logout') }}">
                    @csrf
                    <button type="submit" class="btn btn-secondary" style="padding: 8px 12px; font-size: 12px;">
                        Logout
                    </button>
                </form>
            </div>
        </header>

        <!-- Dynamic Content -->
        <div class="content-area">
            @yield('content')
        </div>
    </main>

    <script>
        // Dark Mode Logic
        const themeToggleBtn = document.getElementById('themeToggle');
        const rootElement = document.documentElement;
        
        // Check local storage for theme preference
        const currentTheme = localStorage.getItem('theme') || 'light';
        rootElement.setAttribute('data-theme', currentTheme);
        themeToggleBtn.textContent = currentTheme === 'dark' ? '☀️' : '🌙';

        themeToggleBtn.addEventListener('click', () => {
            let theme = rootElement.getAttribute('data-theme');
            let newTheme = theme === 'dark' ? 'light' : 'dark';
            
            rootElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
            themeToggleBtn.textContent = newTheme === 'dark' ? '☀️' : '🌙';
        });

        // Dropdown Toggle Logic
        document.addEventListener('click', (e) => {
            const dropdown = e.target.closest('.dropdown');
            const isToggle = e.target.classList.contains('dropdown-toggle') || e.target.closest('.dropdown-toggle');
            
            // Close all other dropdowns
            document.querySelectorAll('.dropdown').forEach(d => {
                if (d !== dropdown) d.classList.remove('active');
            });

            if (dropdown && isToggle) {
                dropdown.classList.toggle('active');
            }
        });
    </script>
</body>
</html>
