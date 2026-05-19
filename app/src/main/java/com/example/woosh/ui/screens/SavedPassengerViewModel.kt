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

data class SavedPassengerUiState(
    val passengers: List<Passenger> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false
)

@HiltViewModel
class SavedPassengerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow(SavedPassengerUiState())
    val uiState: StateFlow<SavedPassengerUiState> = _uiState.asStateFlow()

    init {
        fetchSavedPassengers()
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
                
                val savedList = snapshot.documents.mapNotNull { doc ->
                    val passenger = doc.toObject(Passenger::class.java)
                    // We might need the document ID for deletion if we don't have a unique ID in the model
                    // For now, let's assume we can match by name and idNumber if needed, 
                    // or better, let's add a unique ID to the model or use doc ID.
                    passenger
                }
                _uiState.update { it.copy(passengers = savedList, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun addPassenger(name: String, idNumber: String) {
        val uid = auth.currentUser?.uid ?: return
        val newPassenger = Passenger(name = name, idNumber = idNumber)
        
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(uid).collection("saved_passengers")
                    .add(newPassenger)
                    .await()
                fetchSavedPassengers()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menambah penumpang: ${e.message}") }
            }
        }
    }

    fun deletePassenger(passenger: Passenger) {
        val uid = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                // Find document with matching fields
                val snapshot = firestore.collection("users")
                    .document(uid).collection("saved_passengers")
                    .whereEqualTo("name", passenger.name)
                    .whereEqualTo("idNumber", passenger.idNumber)
                    .get()
                    .await()
                
                snapshot.documents.forEach { doc ->
                    doc.reference.delete().await()
                }
                fetchSavedPassengers()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menghapus: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isDeleting = false) }
            }
        }
    }

    fun updatePassenger(oldPassenger: Passenger, newName: String, newIdNumber: String) {
        val uid = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val snapshot = firestore.collection("users")
                    .document(uid).collection("saved_passengers")
                    .whereEqualTo("name", oldPassenger.name)
                    .whereEqualTo("idNumber", oldPassenger.idNumber)
                    .get()
                    .await()
                
                snapshot.documents.forEach { doc ->
                    doc.reference.update(mapOf(
                        "name" to newName,
                        "idNumber" to newIdNumber
                    )).await()
                }
                fetchSavedPassengers()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal memperbarui: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
