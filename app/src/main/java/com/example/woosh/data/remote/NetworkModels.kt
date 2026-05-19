package com.example.woosh.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Data Class untuk Response Trip List
 */
data class TripListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<TripItem>
)

data class TripItem(
    @SerializedName("trip_id") val tripId: String,
    @SerializedName("train_name") val trainName: String,
    @SerializedName("train_class") val trainClass: String,
    @SerializedName("base_price") val basePrice: Int,
    @SerializedName("departure_time") val departureTime: String,
    @SerializedName("arrival_time") val arrivalTime: String
)

/**
 * Data Class untuk Request Sync User
 */
data class UserSyncRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("uid") val uid: String
)

/**
 * Data Class untuk Response Sync User
 */
data class UserSyncResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserData?
)

data class UserData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)

/**
 * Data Class untuk Request Book Ticket (simpan ke MySQL)
 */
data class BookTicketRequest(
    @SerializedName("ticket_code") val ticketCode: String,
    @SerializedName("trip_id") val tripId: String,
    @SerializedName("email") val email: String,
    @SerializedName("seats_list") val seatsList: String,
    @SerializedName("total_amount") val totalAmount: Long,
    @SerializedName("firestore_ticket_id") val firestoreTicketId: String,
    @SerializedName("payment_method") val paymentMethod: String
)

/**
 * Data Class untuk Response Book Ticket
 */
data class BookTicketResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

/**
 * Data Class untuk Request Refund Ticket
 */
data class RefundTicketRequest(
    @SerializedName("ticket_code") val ticketCode: String,
    @SerializedName("email") val email: String
)

/**
 * Data Class untuk Request Update Profile
 */
data class UpdateProfileRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String
)
