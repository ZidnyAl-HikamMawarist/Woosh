<?php

namespace App\Services;

use App\Models\Ticket;
use Illuminate\Support\Facades\Log;

class PaymentService
{
    /**
     * Process payment notification from payment gateway
     */
    public function processPaymentNotification(array $data): array
    {
        $orderId = $data['order_id'] ?? null;
        $transactionStatus = $data['transaction_status'] ?? null;

        if (!$orderId || !$transactionStatus) {
            return ['status' => 'error', 'message' => 'Invalid payment data'];
        }

        $ticket = Ticket::where('ticket_code', $orderId)->first();
        if (!$ticket) {
            return ['status' => 'error', 'message' => 'Ticket not found'];
        }

        // Update ticket based on transaction status
        $statusMap = [
            'settlement' => ['payment_status' => 'paid', 'status' => 'Active'],
            'capture' => ['payment_status' => 'paid', 'status' => 'Active'],
            'pending' => ['payment_status' => 'pending'],
            'expire' => ['payment_status' => 'expired', 'status' => 'Batal'],
            'cancel' => ['payment_status' => 'cancelled', 'status' => 'Batal'],
        ];

        $updateData = $statusMap[$transactionStatus] ?? [];
        if (!empty($updateData)) {
            $ticket->update($updateData);
            Log::info("Payment processed for ticket {$orderId}: {$transactionStatus}");
        }

        return [
            'status' => 'success',
            'message' => 'Payment processed',
            'ticket' => $ticket,
        ];
    }

    /**
     * Verify payment status
     */
    public function verifyPaymentStatus(string $ticketCode): array
    {
        $ticket = Ticket::where('ticket_code', $ticketCode)->first();
        if (!$ticket) {
            return ['status' => 'error', 'message' => 'Ticket not found'];
        }

        return [
            'status' => 'success',
            'payment_status' => $ticket->payment_status,
            'ticket_status' => $ticket->status,
        ];
    }

    /**
     * Simulate payment for testing
     */
    public function simulatePayment(string $ticketCode): array
    {
        $ticket = Ticket::where('ticket_code', $ticketCode)->first();
        if (!$ticket) {
            return ['status' => 'error', 'message' => 'Ticket not found'];
        }

        $ticket->update([
            'payment_method' => 'Bank Transfer (MOCK)',
            'payment_status' => 'paid',
            'status' => 'Active',
            'payment_reference' => 'REF-' . rand(100000, 999999),
        ]);

        Log::info("Payment simulated for ticket {$ticketCode}");

        return [
            'status' => 'success',
            'message' => 'Payment simulated successfully',
            'ticket' => $ticket,
        ];
    }

    /**
     * Handle payment failure
     */
    public function handlePaymentFailure(string $ticketCode, string $reason = 'Payment failed'): array
    {
        $ticket = Ticket::where('ticket_code', $ticketCode)->first();
        if (!$ticket) {
            return ['status' => 'error', 'message' => 'Ticket not found'];
        }

        $ticket->update([
            'payment_status' => 'failed',
            'status' => 'Batal',
        ]);

        Log::warning("Payment failed for ticket {$ticketCode}: {$reason}");

        return [
            'status' => 'success',
            'message' => 'Payment failure recorded',
            'ticket' => $ticket,
        ];
    }
}
