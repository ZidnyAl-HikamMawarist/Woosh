package com.example.woosh.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woosh.data.remote.RefundTicketRequest
import com.example.woosh.data.remote.RetrofitClient
import com.example.woosh.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _tickets = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets: StateFlow<List<Ticket>> = _tickets.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private var listenerRegistration: ListenerRegistration? = null

    init {
        fetchTickets()
    }

    private fun fetchTickets() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            listenerRegistration = firestore.collection("users")
                .document(currentUser.uid).collection("tickets")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _isLoading.value = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val ticketList = mutableListOf<Ticket>()
                        snapshot.documents.forEach { doc ->
                            doc.toObject(Ticket::class.java)?.let { ticketList.add(it) }
                        }
                        _tickets.value = ticketList
                    }
                    _isLoading.value = false
                }
        } else {
            _isLoading.value = false
        }
    }

    fun refundTicket(ticketId: String, onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: run { onComplete(false); return }
        val userEmail = currentUser.email ?: run { onComplete(false); return }

        // 1. Update Firestore
        firestore.collection("users")
            .document(currentUser.uid).collection("tickets")
            .document(ticketId)
            .update("status", "Refunded")
            .addOnSuccessListener {
                // 2. Sync ke MySQL (fire and forget)
                viewModelScope.launch {
                    try {
                        RetrofitClient.instance.refundTicket(
                            RefundTicketRequest(ticketCode = ticketId, email = userEmail)
                        )
                    } catch (e: Exception) {
                        android.util.Log.w("TicketViewModel", "MySQL refund sync failed: ${e.message}")
                    }
                }
                onComplete(true)
            }
            .addOnFailureListener { onComplete(false) }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
