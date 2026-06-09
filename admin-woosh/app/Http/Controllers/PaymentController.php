<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Ticket;
use Illuminate\Support\Facades\Log;

class PaymentController extends Controller
{
    /**
     * Webhook / Notification Handler (Simulasi Midtrans/Xendit)
     * Ini adalah URL yang dipanggil otomatis oleh Payment Gateway (PG)
     */
    public function handleNotification(Request $request)
    {
        // Dalam realita, kita akan memverifikasi signature key dari PG di sini
        $orderId = $request->input('order_id'); // Kode Tiket
        $statusCode = $request->input('status_code');
        $transactionStatus = $request->input('transaction_status'); // 'settlement', 'pending', 'expire'

        $ticket = Ticket::where('ticket_code', $orderId)->first();

        if (!$ticket) {
            return response()->json(['message' => 'Ticket not found'], 404);
        }

        // Logika Update Status Berdasarkan Respon PG
        if ($transactionStatus == 'settlement' || $transactionStatus == 'capture') {
            $ticket->update([
                'payment_status' => 'paid',
                'status' => 'Aktif' // Tiket jadi aktif setelah dibayar
            ]);
            Log::info("Pembayaran Sukses: " . $orderId);
        } elseif ($transactionStatus == 'pending') {
            $ticket->update(['payment_status' => 'pending']);
        } elseif ($transactionStatus == 'expire' || $transactionStatus == 'cancel') {
            $ticket->update([
                'payment_status' => 'expired',
                'status' => 'Batal'
            ]);
        }

        return response()->json(['message' => 'Notification Handled']);
    }

    /**
     * Simulasi Pembayaran (Untuk Testing Tanpa PG beneran)
     */
    public function simulatePayment($ticket_code)
    {
        $ticket = Ticket::where('ticket_code', $ticket_code)->firstOrFail();
        
        $ticket->update([
            'payment_method' => 'Bank Transfer (MOCK)',
            'payment_status' => 'paid',
            'status' => 'Aktif',
            'payment_reference' => 'REF-' . rand(1000, 9999)
        ]);

        return redirect()->back()->with('success', 'Simulasi pembayaran berhasil! Tiket ' . $ticket_code . ' sekarang aktif.');
    }
}
