package com.example.woosh.ui.screens

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SeatSelectionUiState(
    val selectedSeats: List<String> = emptyList(),
    val bookedSeats: List<String> = emptyList(),
    val selectedCoach: Int = 1,
    val passengerCount: Int = 1
)

@HiltViewModel
class SeatSelectionViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {
    private val _uiState = MutableStateFlow(SeatSelectionUiState())
    val uiState: StateFlow<SeatSelectionUiState> = _uiState.asStateFlow()
    private var snapshotListener: ListenerRegistration? = null

    fun initialize(passengerCount: Int, trainId: String) {
        if (_uiState.value.passengerCount != passengerCount) {
            _uiState.update { it.copy(passengerCount = passengerCount, selectedSeats = emptyList()) }
        }
        observeBookedSeats(trainId)
    }

    private fun observeBookedSeats(trainId: String) {
        snapshotListener?.remove()
        snapshotListener = db.collection("trips").document(trainId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val seats = snapshot.get("bookedSeats") as? List<String> ?: emptyList()
                    _uiState.update { it.copy(bookedSeats = seats) }
                }
            }
    }

    fun selectCoach(coach: Int) {
        _uiState.update { 
            it.copy(
                selectedCoach = coach,
                selectedSeats = emptyList() // clear seats when changing coach if needed, or keep them
            ) 
        }
    }

    fun toggleSeat(seatId: String) {
        _uiState.update { state ->
            val currentSelected = state.selectedSeats.toMutableList()
            if (currentSelected.contains(seatId)) {
                currentSelected.remove(seatId)
            } else if (currentSelected.size < state.passengerCount) {
                currentSelected.add(seatId)
            }
            state.copy(selectedSeats = currentSelected)
        }
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }
}
