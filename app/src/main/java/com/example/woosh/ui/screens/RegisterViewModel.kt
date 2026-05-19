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

sealed class RegisterResult {
    object Idle : RegisterResult()
    object Success : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

data class RegisterUiState(
    val isLoading: Boolean = false,
    val registerResult: RegisterResult = RegisterResult.Idle
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun resetResult() {
        _uiState.update { it.copy(registerResult = RegisterResult.Idle) }
    }

    fun register(name: String, email: String, phone: String, password: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                val userId = user?.uid
                
                if (userId != null) {
                    val userProfile = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "address" to "Belum diatur",
                        "loyaltyPoints" to 0L,
                        "createdAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users").document(userId).set(userProfile).await()

                    // Sync user ke MySQL agar muncul di admin dashboard
                    try {
                        com.example.woosh.data.remote.RetrofitClient.instance.syncUser(
                            com.example.woosh.data.remote.UserSyncRequest(
                                email = email,
                                name  = name,
                                uid   = userId
                            )
                        )
                    } catch (e: Exception) {
                        android.util.Log.w("RegisterViewModel", "MySQL sync failed: ${e.message}")
                    }
                }
                
                _uiState.update { it.copy(isLoading = false, registerResult = RegisterResult.Success) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, registerResult = RegisterResult.Error(e.message ?: "Registrasi gagal")) 
                }
            }
        }
    }
}
