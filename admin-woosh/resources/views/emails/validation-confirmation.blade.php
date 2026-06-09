<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Ticket Validated</title>
</head>
<body>
    <h2>Ticket Validated</h2>
    <p>Dear {{ $user->name }},</p>
    <p>Your ticket has been validated at the station.</p>
    
    <h3>Ticket Details:</h3>
    <ul>
        <li><strong>Ticket Code:</strong> {{ $ticket->ticket_code }}</li>
        <li><strong>Status:</strong> {{ $ticket->status }}</li>
        <li><strong>Validated At:</strong> {{ now()->format('d M Y H:i') }}</li>
    </ul>
    
    <p>Thank you for using WOOSH!</p>
    
    <hr>
    <p><small>This is an automated email. Please do not reply to this message.</small></p>
</body>
</html>
