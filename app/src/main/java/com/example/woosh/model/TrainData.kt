package com.example.woosh.model

data class TrainData(
    val name: String, 
    val dep: String, 
    val arr: String, 
    val price: String,
    val trainClass: String = "First Class"
)

data class Passenger(
    val name: String = "",
    val idNumber: String = "" // NIK or Passport
)

data class Ticket(
    val id: String = "",
    val trainName: String = "WOOSH 502",
    val seats: String = "",
    val date: String = "",
    val totalPrice: String = "",
    val status: String = "Aktif" // Aktif or Selesai
)
