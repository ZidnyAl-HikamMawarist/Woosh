<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>{{ $title }}</title>
</head>
<body>
    <h2>{{ $title }}</h2>
    <p>Dear {{ $user->name }},</p>
    
    <p>{{ $body }}</p>
    
    <p>Thank you for using WOOSH!</p>
    
    <hr>
    <p><small>This is an automated email. Please do not reply to this message.</small></p>
</body>
</html>
