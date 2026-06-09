<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Ticket;
use Barryvdh\DomPDF\Facade\Pdf;

class ReportController extends Controller
{
    public function exportTicketsPdf()
    {
        $tickets = Ticket::with(['user', 'trip'])->orderBy('booked_at', 'desc')->get();
        $totalRevenue = Ticket::where('status', '!=', 'Batal')->sum('total_amount');

        $pdf = Pdf::loadView('admin.reports.tickets_pdf', compact('tickets', 'totalRevenue'));
        
        return $pdf->download('Laporan_Tiket_Woosh_'.date('d-m-Y').'.pdf');
    }
}
