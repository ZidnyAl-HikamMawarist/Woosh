package com.example.woosh.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woosh.data.EmailService
import com.example.woosh.data.remote.BookTicketRequest
import com.example.woosh.data.remote.RetrofitClient
import com.example.woosh.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class PaymentStatus {
    object Idle : PaymentStatus()
    object Success : PaymentStatus()
    data class FailedEmail(val message: String, val error: String) : PaymentStatus()
    data class Error(val title: String, val message: String) : PaymentStatus()
}

data class PaymentUiState(
    val isProcessing: Boolean = false,
    val paymentStatus: PaymentStatus = PaymentStatus.Idle,
    val userEmail: String = ""
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun resetStatus() {
        _uiState.update { it.copy(paymentStatus = PaymentStatus.Idle) }
    }

    fun processPayment(
        seats: String,
        trainId: String,
        trainName: String,
        displayAmount: String,
        rawAmount: Long,
        selectedMethod: String
    ) {
        _uiState.update { it.copy(isProcessing = true) }

        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        paymentStatus = PaymentStatus.Error(
                            title = "Auth Error",
                            message = "Sesi Anda telah berakhir. Silakan login kembali."
                        )
                    )
                }
                return@launch
            }

            val uid = user.uid
            val userEmail = user.email ?: "user@example.com"
            val userName = user.displayName ?: "Penumpang Woosh"
            val ticketId = "WSH-TK-${System.currentTimeMillis().toString().takeLast(6)}"
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID"))
            val currentDate = sdf.format(java.util.Date())

            _uiState.update { it.copy(userEmail = userEmail) }

            // 1. Update points and save ticket using TRANSACTION to prevent race condition
            try {
                val points = rawAmount / 10000
                val seatsList = seats.split(",").map { it.trim() }

                val firestoreTicketId = db.runTransaction { transaction ->
                    // a. Update poin pengguna
                    val userRef = db.collection("users").document(uid)
                    val userSnapshot = transaction.get(userRef)
                    if (userSnapshot.exists()) {
                        transaction.update(userRef, "loyaltyPoints", FieldValue.increment(points))
                    } else {
                        transaction.set(userRef, mapOf("loyaltyPoints" to points), SetOptions.merge())
                    }

                    // b. Simpan tiket — gunakan ticketId (WSH-TK-xxx) sebagai document ID
                    //    agar bisa dipakai sebagai ticket_code saat sync ke MySQL
                    val newTicket = Ticket(
                        id = ticketId,
                        trainId = trainId,
                        trainName = trainName,
                        seats = seats,
                        date = currentDate,
                        totalPrice = displayAmount,
                        status = "Aktif"
                    )
                    val ticketRef = db.collection("users").document(uid).collection("tickets").document(ticketId)
                    transaction.set(ticketRef, newTicket)

                    // c. Simpan riwayat poin
                    val historyItem = mapOf(
                        "title" to "Perjalanan $trainName",
                        "date" to currentDate,
                        "amount" to "+$points pts",
                        "timestamp" to System.currentTimeMillis()
                    )
                    val historyRef = db.collection("users").document(uid).collection("point_history").document()
                    transaction.set(historyRef, historyItem)

                    // d. Simpan notifikasi
                    val notifId = "NF" + System.currentTimeMillis().toString().takeLast(6)
                    val notifData = mapOf(
                        "id" to notifId,
                        "title" to "Pembayaran Berhasil",
                        "body" to "Tiket $trainName berhasil dipesan (Kursi: $seats). E-ticket siap digunakan.",
                        "type" to "SUCCESS",
                        "isRead" to false,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )
                    val notifRef = db.collection("users").document(uid).collection("notifications").document(notifId)
                    transaction.set(notifRef, notifData)

                    ticketId // Return ticketId (WSH-TK-xxx) sebagai Firestore doc ID sekaligus ticket_code
                }.await()

                // 2. Sync tiket ke MySQL (admin dashboard) — fire and forget, tidak blokir flow
                try {
                    RetrofitClient.instance.bookTicket(
                        BookTicketRequest(
                            ticketCode        = firestoreTicketId,
                            tripId            = trainId,
                            email             = userEmail,
                            seatsList         = seats,
                            totalAmount       = rawAmount,
                            firestoreTicketId = firestoreTicketId,
                            paymentMethod     = selectedMethod
                        )
                    )
                } catch (e: Exception) {
                    // Jangan gagalkan transaksi hanya karena sync MySQL gagal
                    // Tiket sudah tersimpan di Firestore
                    android.util.Log.w("PaymentViewModel", "MySQL sync failed (non-critical): ${e.message}")
                }

                // 2. Send Email (After transaction success)
                val (isEmailSent, emailError) = EmailService.sendTicketEmail(
                    toEmail = userEmail,
                    name = userName,
                    seats = seats,
                    train = trainName,
                    ticketId = ticketId,
                    date = currentDate,
                    totalPrice = displayAmount,
                    paymentMethod = selectedMethod
                )

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        paymentStatus = if (isEmailSent) {
                            PaymentStatus.Success
                        } else {
                            PaymentStatus.FailedEmail(
                                message = "Tiket Anda sudah tersimpan di sistem, namun pengiriman email gagal:\n\n$emailError\n\nSilakan cek tab 'Tiket' di aplikasi.",
                                error = emailError ?: "Unknown error"
                            )
                        }
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        paymentStatus = PaymentStatus.Error(
                            title = "Terjadi Kesalahan",
                            message = "Gagal memproses transaksi: ${e.message}\n\nSaran: Cek Firebase Rules di Console."
                        )
                    )
                }
            }
        }
    }
}
