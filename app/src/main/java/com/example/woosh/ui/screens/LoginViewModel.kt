package com.example.woosh.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
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

sealed class LoginResult {
    object Idle : LoginResult()
    object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val loginResult: LoginResult = LoginResult.Idle
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun resetResult() {
        _uiState.update { it.copy(loginResult = LoginResult.Idle) }
    }

    fun loginWithEmail(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                // Sync user ke MySQL saat login agar admin dashboard selalu up-to-date
                if (user != null) {
                    try {
                        com.example.woosh.data.remote.RetrofitClient.instance.syncUser(
                            com.example.woosh.data.remote.UserSyncRequest(
                                email = user.email ?: email,
                                name  = user.displayName ?: "User Woosh",
                                uid   = user.uid
                            )
                        )
                    } catch (e: Exception) {
                        android.util.Log.w("LoginViewModel", "MySQL sync failed: ${e.message}")
                    }
                }
                _uiState.update { it.copy(isLoading = false, loginResult = LoginResult.Success) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, loginResult = LoginResult.Error(e.message ?: "Login gagal")) 
                }
            }
        }
    }

    fun loginWithGoogle(credential: AuthCredential) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user
                
                if (user != null) {
                    val doc = firestore.collection("users").document(user.uid).get().await()
                    if (!doc.exists()) {
                        // Cek apakah email sudah terdaftar di dokumen lain (misal login manual sebelumnya)
                        val emailQuery = firestore.collection("users")
                            .whereEqualTo("email", user.email)
                            .get()
                            .await()

                        if (!emailQuery.isEmpty) {
                            // Ambil data dari dokumen lama (berdasarkan email)
                            val existingData = emailQuery.documents[0].data ?: emptyMap<String, Any>()
                            val newProfile = existingData.toMutableMap()
                            
                            // Pastikan UID baru memiliki data yang sama
                            firestore.collection("users").document(user.uid).set(newProfile).await()
                        } else {
                            // Buat profil baru jika benar-benar belum ada
                            val profile = hashMapOf(
                                "name" to (user.displayName ?: ""),
                                "email" to (user.email ?: ""),
                                "loyaltyPoints" to 0L,
                                "createdAt" to System.currentTimeMillis()
                            )
                            firestore.collection("users").document(user.uid).set(profile).await()
                        }
                    }
                }
                
                _uiState.update { it.copy(isLoading = false, loginResult = LoginResult.Success) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, loginResult = LoginResult.Error(e.message ?: "Google Sign-In gagal")) 
                }
            }
        }
    }

    fun sendPasswordReset(email: String, onComplete: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }
}
