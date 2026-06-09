<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Services\FirebaseSyncService;
use Illuminate\Http\Request;
use App\Models\User;
use App\Models\Trip;
use App\Models\Ticket;
use Illuminate\Support\Facades\DB;

class AdminController extends Controller
{
    public function __construct(private FirebaseSyncService $firebase) {}
    public function dashboard()
    {
        $totalRevenue = Ticket::where('payment_status', 'paid')->sum('total_amount');
        $activeUsers = User::count();
        $totalTrips = Trip::count();
        $totalTickets = Ticket::count();

        // Data untuk Grafik Pendapatan 7 Hari Terakhir
        $revenueData = [];
        $revenueLabels = [];
        for ($i = 6; $i >= 0; $i--) {
            $date = \Carbon\Carbon::now()->subDays($i);
            $revenueLabels[] = $date->format('d M');
            $revenueData[] = Ticket::where('payment_status', 'paid')
                ->whereDate('booked_at', $date->format('Y-m-d'))
                ->sum('total_amount');
        }

        // Recent tickets
        $recentTickets = Ticket::with(['user', 'trip'])->orderBy('booked_at', 'desc')->limit(5)->get();

        // Live Trips (Currently in progress)
        $now = now();
        $liveTrips = Trip::with(['departureStation', 'arrivalStation'])
            ->where('departure_time', '<=', $now)
            ->where('arrival_time', '>=', $now)
            ->get();

        // Peak Hours Analytics
        $peakHoursData = Ticket::select(DB::raw('HOUR(booked_at) as hour'), DB::raw('count(*) as count'))
            ->groupBy('hour')
            ->orderBy('hour')
            ->get();

        // Station Popularity
        $stationPopularity = Ticket::join('trips', 'tickets.trip_id', '=', 'trips.trip_id')
            ->join('stations', 'trips.arrival_station_id', '=', 'stations.id')
            ->select('stations.name', DB::raw('count(*) as total'))
            ->groupBy('stations.name')
            ->orderBy('total', 'desc')
            ->limit(5)
            ->get();

        // All Stations for Map
        $allStations = \App\Models\Station::all();

        return view('admin.dashboard', compact(
            'totalRevenue', 
            'activeUsers', 
            'totalTrips', 
            'totalTickets', 
            'recentTickets',
            'revenueData',
            'revenueLabels',
            'liveTrips',
            'peakHoursData',
            'stationPopularity',
            'allStations'
        ));
    }

    public function trips()
    {
        $trips = Trip::orderBy('departure_time')->paginate(15);
        return view('admin.trips', compact('trips'));
    }

    public function createTrip()
    {
        return view('admin.trips_form');
    }

    public function storeTrip(Request $request)
    {
        $data = $request->validate([
            'trip_id' => 'required|unique:trips,trip_id',
            'train_name' => 'required|string',
            'train_class' => 'required|string',
            'departure_time' => 'required|date',
            'arrival_time' => 'required|date',
            'base_price' => 'required|numeric',
        ]);
        
        $data['booked_seats'] = [];
        $trip = Trip::create($data);

        // Sync ke Firestore
        $this->firebase->syncTrip($trip->toArray());

        return redirect()->route('admin.trips')->with('success', 'Jadwal berhasil ditambahkan');
    }

    public function editTrip(Trip $trip)
    {
        return view('admin.trips_form', compact('trip'));
    }

    public function updateTrip(Request $request, Trip $trip)
    {
        $data = $request->validate([
            'train_name' => 'required|string',
            'train_class' => 'required|string',
            'departure_time' => 'required|date',
            'arrival_time' => 'required|date',
            'base_price' => 'required|numeric',
        ]);
        
        $trip->update($data);

        // Sync ke Firestore
        $this->firebase->syncTrip($trip->fresh()->toArray());

        return redirect()->route('admin.trips')->with('success', 'Jadwal berhasil diperbarui');
    }

    public function destroyTrip(Trip $trip)
    {
        $tripId = $trip->trip_id;
        $trip->delete();

        // Hapus dari Firestore juga
        $this->firebase->deleteTrip($tripId);

        return redirect()->route('admin.trips')->with('success', 'Jadwal berhasil dihapus');
    }

    // --- Tickets CRUD ---

    public function tickets()
    {
        $tickets = Ticket::with(['user', 'trip'])->orderBy('booked_at', 'desc')->paginate(15);
        return view('admin.tickets', compact('tickets'));
    }

    public function createTicket()
    {
        $trips = Trip::all();
        $users = User::all();
        return view('admin.tickets_form', compact('trips', 'users'));
    }

    public function storeTicket(Request $request)
    {
        $data = $request->validate([
            'ticket_code' => 'required|unique:tickets,ticket_code',
            'trip_id' => 'required|exists:trips,trip_id',
            'user_id' => 'required|exists:users,id',
            'seats_list' => 'required|string',
            'total_amount' => 'required|numeric',
            'payment_method' => 'nullable|string',
            'payment_status' => 'required|in:pending,paid,expired,failed',
            'status' => 'required|in:Active,Used,Batal',
            'booked_at' => 'required|date',
        ]);

        Ticket::create($data);

        return redirect()->route('admin.tickets')->with('success', 'Tiket berhasil ditambahkan');
    }

    public function editTicket(Ticket $ticket)
    {
        $trips = Trip::all();
        $users = User::all();
        return view('admin.tickets_form', compact('ticket', 'trips', 'users'));
    }

    public function updateTicket(Request $request, Ticket $ticket)
    {
        $data = $request->validate([
            'trip_id' => 'required|exists:trips,trip_id',
            'user_id' => 'required|exists:users,id',
            'seats_list' => 'required|string',
            'total_amount' => 'required|numeric',
            'payment_method' => 'nullable|string',
            'payment_status' => 'required|in:pending,paid,expired,failed',
            'status' => 'required|in:Active,Used,Batal',
            'booked_at' => 'required|date',
        ]);

        $ticket->update($data);

        // Sync status tiket ke Firestore
        $ticketUser = $ticket->user;
        if ($ticketUser && !empty($ticketUser->firebase_uid) && !empty($ticket->firestore_ticket_id)) {
            $this->firebase->syncTicketStatus(
                $ticketUser->firebase_uid,
                $ticket->firestore_ticket_id,
                $data['status']
            );
        }

        return redirect()->route('admin.tickets')->with('success', 'Tiket berhasil diperbarui');
    }

    public function destroyTicket(Ticket $ticket)
    {
        $ticket->delete();
        return redirect()->route('admin.tickets')->with('success', 'Tiket berhasil dihapus');
    }

    // --- Users CRUD ---

    public function users()
    {
        $users = User::orderBy('created_at', 'desc')->paginate(15);
        return view('admin.users', compact('users'));
    }

    public function createUser()
    {
        return view('admin.users_form');
    }

    public function storeUser(Request $request)
    {
        $data = $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email',
            'password' => 'required|string|min:8',
            'phone' => 'nullable|string',
            'loyalty_points' => 'nullable|integer',
            'active_pass' => 'nullable|string',
            'remaining_trips' => 'nullable|integer',
            'pass_expiry_at' => 'nullable|date',
            'role' => 'required|in:admin,manager,user',
        ]);

        $data['password'] = bcrypt($data['password']);
        User::create($data);

        return redirect()->route('admin.users')->with('success', 'Pengguna berhasil ditambahkan');
    }

    public function editUser(User $user)
    {
        return view('admin.users_form', compact('user'));
    }

    public function updateUser(Request $request, User $user)
    {
        $data = $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email,' . $user->id,
            'password' => 'nullable|string|min:8',
            'phone' => 'nullable|string',
            'loyalty_points' => 'nullable|integer',
            'active_pass' => 'nullable|string',
            'remaining_trips' => 'nullable|integer',
            'pass_expiry_at' => 'nullable|date',
            'role' => 'required|in:admin,manager,user',
        ]);

        if (!empty($data['password'])) {
            $data['password'] = bcrypt($data['password']);
        } else {
            unset($data['password']);
        }

        $user->update($data);

        // Sync poin & pass ke Firestore jika user punya firebase_uid
        if (!empty($user->firebase_uid)) {
            $this->firebase->syncUserPoints($user->firebase_uid, $user->fresh()->toArray());
        }

        return redirect()->route('admin.users')->with('success', 'Pengguna berhasil diperbarui');
    }

    public function destroyUser(User $user)
    {
        $user->delete();
        return redirect()->route('admin.users')->with('success', 'User berhasil dihapus!');
    }

    public function refundTicket($ticket_code)
    {
        $ticket = Ticket::where('ticket_code', $ticket_code)->firstOrFail();
        $ticket->update(['status' => 'Batal']);

        // Sync ke Firestore & kirim notifikasi ke user
        $ticketUser = $ticket->user;
        if ($ticketUser && !empty($ticketUser->firebase_uid)) {
            if (!empty($ticket->firestore_ticket_id)) {
                $this->firebase->syncTicketStatus($ticketUser->firebase_uid, $ticket->firestore_ticket_id, 'Batal');
            }
            $this->firebase->sendNotification(
                $ticketUser->firebase_uid,
                'Tiket Dibatalkan',
                "Tiket {$ticket_code} telah dibatalkan oleh admin. Hubungi kami jika ada pertanyaan.",
                'WARNING'
            );
        }

        return redirect()->route('admin.tickets')->with('success', 'Tiket ' . $ticket_code . ' berhasil direfund/dibatalkan!');
    }

    public function validateTicket($ticket_code)
    {
        $ticket = Ticket::where('ticket_code', $ticket_code)->first();

        if (!$ticket) {
            return redirect()->back()->with('error', 'Tiket tidak ditemukan!');
        }

        if ($ticket->status != 'Active') {
            return redirect()->back()->with('error', 'Tiket sudah digunakan atau dibatalkan!');
        }

        $ticket->update(['status' => 'Used']);

        // Sync ke Firestore
        $ticketUser = $ticket->user;
        if ($ticketUser && !empty($ticketUser->firebase_uid) && !empty($ticket->firestore_ticket_id)) {
            $this->firebase->syncTicketStatus($ticketUser->firebase_uid, $ticket->firestore_ticket_id, 'Used');
        }

        return redirect()->back()->with('success', 'Tiket ' . $ticket_code . ' berhasil divalidasi dan digunakan!');
    }

    public function logs()
    {
        $logs = \App\Models\ActivityLog::with('user')->orderBy('created_at', 'desc')->paginate(50);
        return view('admin.logs', compact('logs'));
    }

    public function refunds()
    {
        $pendingRefunds = \App\Models\RefundRequest::with(['user', 'ticket'])->where('status', 'Pending')->paginate(15);
        return view('admin.refunds', compact('pendingRefunds'));
    }

    public function processRefund(Request $request, \App\Models\RefundRequest $refund)
    {
        $validated = $request->validate([
            'status' => 'required|in:Approved,Rejected',
            'admin_note' => 'nullable|string'
        ]);

        $refund->update([
            'status' => $validated['status'],
            'admin_note' => $validated['admin_note'] ?? null,
            'processed_at' => now()
        ]);

        if ($validated['status'] == 'Approved') {
            $refund->ticket->update(['status' => 'Batal']);
            // Logic to add points back or notify user
        }

        return redirect()->back()->with('success', 'Permintaan refund berhasil diproses!');
    }
    public function stations()
    {
        $stations = \App\Models\Station::paginate(15);
        return view('admin.stations.index', compact('stations'));
    }

    public function createStation()
    {
        return view('admin.stations.form');
    }

    public function storeStation(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'code' => 'required|string|unique:stations,code',
            'city' => 'required|string|max:255',
            'facilities' => 'nullable|string',
            'latitude' => 'nullable|numeric',
            'longitude' => 'nullable|numeric',
        ]);

        \App\Models\Station::create($validated);
        return redirect()->route('admin.stations')->with('success', 'Stasiun berhasil ditambahkan!');
    }

    public function editStation(\App\Models\Station $station)
    {
        return view('admin.stations.form', compact('station'));
    }

    public function updateStation(Request $request, \App\Models\Station $station)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'code' => 'required|string|unique:stations,code,' . $station->id,
            'city' => 'required|string|max:255',
            'facilities' => 'nullable|string',
            'latitude' => 'nullable|numeric',
            'longitude' => 'nullable|numeric',
        ]);

        $station->update($validated);
        return redirect()->route('admin.stations')->with('success', 'Stasiun berhasil diperbarui!');
    }

    public function destroyStation(\App\Models\Station $station)
    {
        $station->delete();
        return redirect()->route('admin.stations')->with('success', 'Stasiun berhasil dihapus!');
    }
}
