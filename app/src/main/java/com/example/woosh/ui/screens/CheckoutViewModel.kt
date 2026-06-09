package com.example.woosh.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woosh.model.Passenger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CheckoutUiState(
    val passengers: List<Passenger> = emptyList(),
    val savedPassengers: List<Passenger> = emptyList(),
    val savePassengersToAccount: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun initPassengers(count: Int) {
        if (_uiState.value.passengers.size != count) {
            val initialPassengers = List(count) { Passenger() }
            _uiState.update { it.copy(passengers = initialPassengers) }
        }
    }

    fun fetchSavedPassengers() {
        val uid = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val snapshot = firestore.collection("users")
                    .document(uid).collection("saved_passengers")
                    .get()
                    .await()
                
                val savedList = snapshot.documents.mapNotNull { it.toObject(Passenger::class.java) }
                _uiState.update { it.copy(savedPassengers = savedList, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updatePassenger(index: Int, passenger: Passenger) {
        _uiState.update { state ->
            val updatedList = state.passengers.toMutableList()
            if (index in updatedList.indices) {
                updatedList[index] = passenger
            }
            state.copy(passengers = updatedList)
        }
    }

    fun toggleSavePassengers(save: Boolean) {
        _uiState.update { it.copy(savePassengersToAccount = save) }
    }

    fun savePassengersIfRequested(onComplete: () -> Unit) {
        val state = _uiState.value
        if (!state.savePassengersToAccount) {
            onComplete()
            return
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            onComplete()
            return
        }

        viewModelScope.launch {
            try {
                val collection = firestore.collection("users").document(uid).collection("saved_passengers")
                
                state.passengers.forEach { p ->
                    if (p.name.isNotEmpty() && p.idNumber.isNotEmpty()) {
                        // Cek apakah sudah ada penumpang dengan idNumber yang sama
                        val existing = collection.whereEqualTo("idNumber", p.idNumber).get().await()
                        if (existing.isEmpty) {
                            collection.add(p).await()
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore error and continue checkout
            } finally {
                onComplete()
            }
        }
    }
    fun rescheduleTicket(
        oldTicketId: String, 
        newTrainId: String,
        newTrainName: String, 
        newSeats: String, 
        newDate: Long,
        onComplete: (Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User not logged in") }
            onComplete(false)
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                firestore.runTransaction { transaction ->
                    // 1. Ambil data tiket lama
                    val oldTicketRef = firestore.collection("users")
                        .document(uid)
                        .collection("tickets")
                        .document(oldTicketId)
                    val oldTicketSnap = transaction.get(oldTicketRef)
                    val oldSeats = oldTicketSnap.getString("seats") ?: ""
                    val oldTrainId = oldTicketSnap.getString("trainId") ?: ""
                    
                    // 2. Ambil data trip baru (untuk verifikasi kursi)
                    val newTripRef = firestore.collection("trips").document(newTrainId)
                    val newTripSnap = transaction.get(newTripRef)
                    val bookedSeats = newTripSnap.get("bookedSeats") as? List<String> ?: emptyList()
                    val newSeatsList = newSeats.split(",").map { it.trim() }
                    
                    // Verifikasi ketersediaan kursi baru
                    val alreadyBooked = newSeatsList.filter { bookedSeats.contains(it) }
                    if (alreadyBooked.isNotEmpty()) {
                        throw Exception("Kursi ${alreadyBooked.joinToString(", ")} tidak tersedia.")
                    }

                    // 3. Lepaskan kursi lama dari trip lama
                    if (oldTrainId.isNotEmpty() && oldSeats.isNotEmpty()) {
                        val oldTripRef = firestore.collection("trips").document(oldTrainId)
                        val oldSeatsList = oldSeats.split(",").map { it.trim() }
                        transaction.update(oldTripRef, "bookedSeats", com.google.firebase.firestore.FieldValue.arrayRemove(*oldSeatsList.toTypedArray()))
                    }

                    // 4. Buat tiket baru
                    val newTicketId = "TK" + System.currentTimeMillis().toString().takeLast(6)
                    val newTicketRef = firestore.collection("users")
                        .document(uid)
                        .collection("tickets")
                        .document(newTicketId)
                    
                    val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID"))
                    val dateStr = formatter.format(java.util.Date(newDate))
                    
                    val ticketData = mapOf(
                        "id" to newTicketId,
                        "trainId" to newTrainId,
                        "trainName" to newTrainName,
                        "seats" to newSeats,
                        "date" to dateStr,
                        "totalPrice" to "Rp 0 (Reschedule)",
                        "status" to "Aktif",
                        "timestamp" to System.currentTimeMillis()
                    )

                    // 5. Simpan notifikasi
                    val notifId = "NF" + System.currentTimeMillis().toString().takeLast(6)
                    val notifRef = firestore.collection("users")
                        .document(uid)
                        .collection("notifications")
                        .document(notifId)

                    val notifData = mapOf(
                        "id" to notifId,
                        "title" to "Reschedule Berhasil",
                        "body" to "Tiket $oldTicketId telah diubah ke $newTrainName (Kursi: $newSeats).",
                        "type" to "SUCCESS",
                        "isRead" to false,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )
                    
                    transaction.update(oldTicketRef, "status", "Rescheduled")
                    transaction.set(newTicketRef, ticketData)
                    transaction.set(notifRef, notifData)
                    
                    // 6. Update bookedSeats di trip baru
                    transaction.update(newTripRef, "bookedSeats", com.google.firebase.firestore.FieldValue.arrayUnion(*newSeatsList.toTypedArray()))
                }.await()
                
                _uiState.update { it.copy(isLoading = false) }
                onComplete(true)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                onComplete(false)
            }
        }
    }
}
