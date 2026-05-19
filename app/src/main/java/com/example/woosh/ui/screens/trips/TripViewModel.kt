package com.example.woosh.ui.screens.trips

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woosh.data.remote.RetrofitClient
import com.example.woosh.data.remote.TripItem
import com.example.woosh.data.remote.UserSyncRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TripViewModel : ViewModel() {

    private val _trips = MutableStateFlow<List<TripItem>>(emptyList())
    val trips: StateFlow<List<TripItem>> = _trips

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchTrips()
    }

    /**
     * Mengambil data jadwal kereta dari backend
     */
    fun fetchTrips() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.instance.getTrips()
                if (response.isSuccessful) {
                    _trips.value = response.body()?.data ?: emptyList()
                } else {
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal terhubung ke server: ${e.message}"
                Log.e("TripViewModel", "fetchTrips error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sinkronisasi data user Firebase ke MySQL via Laravel
     */
    fun syncUserToBackend(email: String, name: String, uid: String) {
        viewModelScope.launch {
            try {
                val request = UserSyncRequest(email, name, uid)
                val response = RetrofitClient.instance.syncUser(request)
                if (response.isSuccessful) {
                    Log.d("TripViewModel", "User synced: ${response.body()?.message}")
                } else {
                    Log.e("TripViewModel", "Sync failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("TripViewModel", "Sync error", e)
            }
        }
    }
}
