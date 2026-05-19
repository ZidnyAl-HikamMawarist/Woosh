package com.example.woosh.ui.screens

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.woosh.model.PointHistory
import com.google.firebase.firestore.Query

@HiltViewModel
class LoyaltyViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _loyaltyPoints = MutableStateFlow(0L)
    val loyaltyPoints: StateFlow<Long> = _loyaltyPoints.asStateFlow()

    private val _pointHistory = MutableStateFlow<List<PointHistory>>(emptyList())
    val pointHistory: StateFlow<List<PointHistory>> = _pointHistory.asStateFlow()

    private var pointsListener: ListenerRegistration? = null
    private var historyListener: ListenerRegistration? = null

    init {
        fetchLoyaltyData()
    }

    private fun fetchLoyaltyData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Monitor Points
            pointsListener = firestore.collection("users")
                .document(currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Handle error (e.g. log it)
                        return@addSnapshotListener
                    }
                    _loyaltyPoints.value = snapshot?.getLong("loyaltyPoints") ?: 0L
                }

            // Monitor History
            historyListener = firestore.collection("users")
                .document(currentUser.uid)
                .collection("point_history")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    val history = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(com.example.woosh.model.PointHistory::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _pointHistory.value = history ?: emptyList()
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pointsListener?.remove()
        historyListener?.remove()
    }
}
