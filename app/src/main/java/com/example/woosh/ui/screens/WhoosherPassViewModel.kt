package com.example.woosh.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

sealed class PurchaseResult {
    object Idle : PurchaseResult()
    object Success : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

data class WhoosherPassUiState(
    val isLoading: Boolean = false,
    val purchaseResult: PurchaseResult = PurchaseResult.Idle
)

@HiltViewModel
class WhoosherPassViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {
    private val _uiState = MutableStateFlow(WhoosherPassUiState())
    val uiState: StateFlow<WhoosherPassUiState> = _uiState.asStateFlow()

    fun resetResult() {
        _uiState.update { it.copy(purchaseResult = PurchaseResult.Idle) }
    }

    fun buyPass(passName: String, tripCount: Int, price: Long) {
        val uid = auth.currentUser?.uid ?: return
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                db.runTransaction { transaction ->
                    val userRef = db.collection("users").document(uid)
                    
                    // 1. Simpan data pass ke profil user
                    val passData = mapOf(
                        "activePass" to passName,
                        "remainingTrips" to tripCount,
                        "expiryDate" to System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 hari
                    )
                    transaction.update(userRef, passData)
                    
                    // 2. Simpan notifikasi
                    val notifId = "NF" + System.currentTimeMillis().toString().takeLast(6)
                    val notifRef = db.collection("users").document(uid).collection("notifications").document(notifId)
                    val notifData = mapOf(
                        "id" to notifId,
                        "title" to "Pembelian Pass Berhasil",
                        "body" to "Selamat! $passName Anda sudah aktif dengan $tripCount perjalanan.",
                        "type" to "SUCCESS",
                        "isRead" to false,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )
                    transaction.set(notifRef, notifData)
                }.await()
                
                _uiState.update { it.copy(isLoading = false, purchaseResult = PurchaseResult.Success) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, purchaseResult = PurchaseResult.Error(e.message ?: "Gagal membeli pass")) }
            }
        }
    }
}
