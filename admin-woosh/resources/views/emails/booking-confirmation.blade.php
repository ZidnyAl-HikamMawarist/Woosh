<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Booking Confirmation</title>
</head>
<body>
    <h2>Booking Confirmation</h2>
    <p>Dear {{ $user->name }},</p>
    <p>Your ticket booking has been confirmed successfully!</p>
    
    <h3>Ticket Details:</h3>
    <ul>
        <li><strong>Ticket Code:</strong> {{ $ticket->ticket_code }}</li>
        <li><strong>Train:</strong> {{ $trip->train_name ?? 'N/A' }}</li>
        <li><strong>Departure:</strong> {{ $trip->departure_time->format('d M Y H:i') }}</li>
        <li><strong>Arrival:</strong> {{ $trip->arrival_time->format('d M Y H:i') }}</li>
        <li><strong>Seats:</strong> {{ implode(', ', json_decode($ticket->seats, true) ?? []) }}</li>
        <li><strong>Total Price:</strong> Rp {{ number_format($ticket->total_price, 0, ',', '.') }}</li>
        <li><strong>Status:</strong> {{ $ticket->status }}</li>
    </ul>
    
    <p>Please keep your ticket code for check-in at the station.</p>
    
    <p>Thank you for using WOOSH!</p>
    
    <hr>
    <p><small>This is an automated email. Please do not reply to this message.</small></p>
</body>
</html>
