<?php

namespace App\Services;

use App\Models\User;
use App\Models\Ticket;
use App\Models\Trip;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Log;

class EmailService
{
    /**
     * Send booking confirmation email
     */
    public function sendBookingConfirmation(User $user, Ticket $ticket, Trip $trip): bool
    {
        try {
            Mail::send('emails.booking-confirmation', [
                'user' => $user,
                'ticket' => $ticket,
                'trip' => $trip,
            ], function ($message) use ($user) {
                $message->to($user->email)
                    ->subject('Booking Confirmation - WOOSH');
            });

            Log::info("Booking confirmation email sent to {$user->email}");
            return true;
        } catch (\Exception $e) {
            Log::error("Failed to send booking confirmation: " . $e->getMessage());
            return false;
        }
    }

    /**
     * Send refund notification email
     */
    public function sendRefundNotification(User $user, Ticket $ticket): bool
    {
        try {
            Mail::send('emails.refund-notification', [
                'user' => $user,
                'ticket' => $ticket,
            ], function ($message) use ($user) {
                $message->to($user->email)
                    ->subject('Refund Confirmation - WOOSH');
            });

            Log::info("Refund notification email sent to {$user->email}");
            return true;
        } catch (\Exception $e) {
            Log::error("Failed to send refund notification: " . $e->getMessage());
            return false;
        }
    }

    /**
     * Send broadcast notification email
     */
    public function sendBroadcastNotification(User $user, string $title, string $body): bool
    {
        try {
            Mail::send('emails.broadcast-notification', [
                'user' => $user,
                'title' => $title,
                'body' => $body,
            ], function ($message) use ($user) {
                $message->to($user->email)
                    ->subject($title . ' - WOOSH');
            });

            Log::info("Broadcast notification email sent to {$user->email}");
            return true;
        } catch (\Exception $e) {
            Log::error("Failed to send broadcast notification: " . $e->getMessage());
            return false;
        }
    }

    /**
     * Send ticket validation email
     */
    public function sendValidationConfirmation(User $user, Ticket $ticket): bool
    {
        try {
            Mail::send('emails.validation-confirmation', [
                'user' => $user,
                'ticket' => $ticket,
            ], function ($message) use ($user) {
                $message->to($user->email)
                    ->subject('Ticket Validated - WOOSH');
            });

            Log::info("Validation confirmation email sent to {$user->email}");
            return true;
        } catch (\Exception $e) {
            Log::error("Failed to send validation confirmation: " . $e->getMessage());
            return false;
        }
    }
}
