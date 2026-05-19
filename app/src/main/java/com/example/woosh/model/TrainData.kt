package com.example.woosh.model

data class TrainData(
    val name: String,
    val dep: String,
    val arr: String,
    val price: String,
    val trainClass: String = "First Class",
    val tripId: String = ""  // ID asli dari MySQL, dipakai saat book ticket
)

data class Passenger(
    val name: String = "",
    val idNumber: String = "" // NIK or Passport
)

data class Ticket(
    val id: String = "",
    val trainId: String = "",
    val trainName: String = "WOOSH 502",
    val seats: String = "",
    val date: String = "",
    val totalPrice: String = "",
    val status: String = "Aktif" // Aktif, Selesai, or Rescheduled
)
data class PointHistory(
    val title: String = "",
    val date: String = "",
    val amount: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
