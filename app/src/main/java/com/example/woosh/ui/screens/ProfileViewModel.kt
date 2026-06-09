package com.example.woosh.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woosh.data.remote.RetrofitClient
import com.example.woosh.data.remote.UpdateProfileRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {
    private val _userName = MutableStateFlow("Memuat...")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userPhone = MutableStateFlow("+62 812-xxxx-xxxx")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userAddress = MutableStateFlow("Jl. Mawar No. 123, Jakarta")
    val userAddress: StateFlow<String> = _userAddress.asStateFlow()

    private val _loyaltyPoints = MutableStateFlow(0L)
    val loyaltyPoints: StateFlow<Long> = _loyaltyPoints.asStateFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    _userName.value = document.getString("name") ?: "User Woosh"
                    document.getString("phone")?.let { _userPhone.value = it }
                    document.getString("address")?.let { _userAddress.value = it }
                    _loyaltyPoints.value = document.getLong("loyaltyPoints") ?: 0L
                }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun sendPasswordReset(onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        val email = user?.email
        
        if (email != null) {
            // Check provider
            val providers = user.providerData.map { it.providerId }
            if (providers.contains("google.com")) {
                onComplete(false, "Akun Google tidak memerlukan reset kata sandi via email Firebase. Silakan ubah melalui pengaturan Akun Google Anda.")
                return
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(true, null)
                    } else {
                        onComplete(false, task.exception?.message)
                    }
                }
        } else {
            onComplete(false, "Email tidak ditemukan")
        }
    }

    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        val uid = user?.uid
        if (user != null && uid != null) {
            val userDocRef = firestore.collection("users").document(uid)
            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val backupData = document.data
                    userDocRef.delete()
                        .addOnSuccessListener {
                            user.delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        onComplete(true)
                                    } else {
                                        // Restore Firestore data if auth deletion fails
                                        if (backupData != null) {
                                            userDocRef.set(backupData)
                                        }
                                        onComplete(false)
                                    }
                                }
                        }
                        .addOnFailureListener {
                            onComplete(false)
                        }
                } else {
                    // No data in Firestore, try to delete Auth user directly
                    user.delete()
                        .addOnCompleteListener { task ->
                            onComplete(task.isSuccessful)
                        }
                }
            }.addOnFailureListener {
                onComplete(false)
            }
        } else {
            onComplete(false)
        }
    }

    fun updateProfile(name: String, address: String, phone: String) {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: return

        firestore.collection("users").document(uid)
            .update(mapOf(
                "name" to name,
                "address" to address,
                "phone" to phone
            ))
            .addOnSuccessListener {
                _userName.value = name
                _userAddress.value = address
                _userPhone.value = phone

                // Sync ke MySQL (fire and forget)
                viewModelScope.launch {
                    try {
                        RetrofitClient.instance.updateProfile(
                            UpdateProfileRequest(email = email, name = name, phone = phone)
                        )
                    } catch (e: Exception) {
                        android.util.Log.w("ProfileViewModel", "MySQL profile sync failed: ${e.message}")
                    }
                }
            }
    }
}
