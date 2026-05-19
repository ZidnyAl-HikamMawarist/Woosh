package com.example.woosh.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    /**
     * Endpoint untuk mengambil data jadwal kereta (trips)
     */
    @GET("api/v1/trips")
    suspend fun getTrips(
        @retrofit2.http.Query("date") date: String? = null
    ): Response<TripListResponse>

    /**
     * Endpoint untuk sinkronisasi user Firebase ke backend Laravel
     */
    @POST("api/v1/sync-user")
    suspend fun syncUser(
        @Body request: UserSyncRequest
    ): Response<UserSyncResponse>

    /**
     * Endpoint untuk menyimpan tiket ke MySQL setelah pembayaran berhasil
     */
    @POST("api/v1/book-ticket")
    suspend fun bookTicket(
        @Body request: BookTicketRequest
    ): Response<BookTicketResponse>

    /**
     * Endpoint untuk refund tiket — sync status ke MySQL
     */
    @POST("api/v1/refund-ticket")
    suspend fun refundTicket(
        @Body request: RefundTicketRequest
    ): Response<BookTicketResponse>

    /**
     * Endpoint untuk update profil user — sync ke MySQL
     */
    @POST("api/v1/update-profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<BookTicketResponse>
}
