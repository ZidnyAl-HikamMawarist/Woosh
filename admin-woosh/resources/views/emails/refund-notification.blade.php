<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Refund Confirmation</title>
</head>
<body>
    <h2>Refund Confirmation</h2>
    <p>Dear {{ $user->name }},</p>
    <p>Your ticket refund has been processed successfully.</p>
    
    <h3>Ticket Details:</h3>
    <ul>
        <li><strong>Ticket Code:</strong> {{ $ticket->ticket_code }}</li>
        <li><strong>Status:</strong> Batal (Cancelled)</li>
        <li><strong>Refund Amount:</strong> Rp {{ number_format($ticket->total_price, 0, ',', '.') }}</li>
    </ul>
    
    <p>The refund will be processed to your original payment method within 3-5 business days.</p>
    
    <p>Thank you for using WOOSH!</p>
    
    <hr>
    <p><small>This is an automated email. Please do not reply to this message.</small></p>
</body>
</html>
