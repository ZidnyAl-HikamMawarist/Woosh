package com.example.woosh.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "INFO", // INFO, SUCCESS, ALERT
    val isRead: Boolean = false,
    val timestamp: Timestamp = Timestamp.now()
)
