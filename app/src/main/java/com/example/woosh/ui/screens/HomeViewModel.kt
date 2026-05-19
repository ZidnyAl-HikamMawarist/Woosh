package com.example.woosh.ui.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Daftar stasiun resmi Kereta Cepat Whoosh
 */
object WooshStations {
    data class Station(val name: String, val code: String)

    val all = listOf(
        Station("Halim", "HLM"),
        Station("Karawang", "KRW"),
        Station("Padalarang", "PDL"),
        Station("Tegalluar Summarecon", "TLS")
    )

    fun getByName(name: String): Station? = all.find { it.name == name }
}

data class HomeUiState(
    val origin: String = "Halim",
    val destination: String = "Tegalluar Summarecon",
    val passengerCount: Int = 1,
    val selectedDate: Long = System.currentTimeMillis(),
    val selectedTimeSlot: String = "Pagi (06:00 - 11:00)",
    val recentSearches: List<String> = emptyList(),
    val loyaltyPoints: Long = 0L
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val sharedPrefs = application.getSharedPreferences("woosh_prefs", Context.MODE_PRIVATE)
    private var firestoreListener: ListenerRegistration? = null

    init {
        loadRecentSearches()
        setupFirebaseListener()
    }

    private fun loadRecentSearches() {
        val recent = sharedPrefs.getStringSet("recent_searches", emptySet())?.toList()?.take(3) ?: emptyList()
        _uiState.update { it.copy(recentSearches = recent) }
    }

    private fun setupFirebaseListener() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestoreListener = firestore.collection("users")
                .document(currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val points = snapshot?.getLong("loyaltyPoints") ?: 0L
                    _uiState.update { it.copy(loyaltyPoints = points) }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        firestoreListener?.remove()
    }

    fun updateOrigin(origin: String) {
        _uiState.update { it.copy(origin = origin) }
    }

    fun updateDestination(destination: String) {
        _uiState.update { it.copy(destination = destination) }
    }

    /**
     * Swap origin ↔ destination (tombol bolak-balik di form)
     */
    fun swapRoute() {
        _uiState.update {
            it.copy(origin = it.destination, destination = it.origin)
        }
    }

    fun updatePassengerCount(count: Int) {
        _uiState.update { it.copy(passengerCount = count) }
    }

    fun updateSelectedDate(date: Long) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun updateSelectedTimeSlot(timeSlot: String) {
        _uiState.update { it.copy(selectedTimeSlot = timeSlot) }
    }

    fun addRecentSearch(origin: String, destination: String) {
        val searchStr = "$origin → $destination"
        val currentSet = sharedPrefs.getStringSet("recent_searches", emptySet()) ?: emptySet()
        val newSet = (setOf(searchStr) + currentSet).take(3).toSet()
        
        sharedPrefs.edit().putStringSet("recent_searches", newSet).apply()
        _uiState.update { it.copy(recentSearches = newSet.toList()) }
    }

    fun clearRecentSearches() {
        sharedPrefs.edit().remove("recent_searches").apply()
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }
}
