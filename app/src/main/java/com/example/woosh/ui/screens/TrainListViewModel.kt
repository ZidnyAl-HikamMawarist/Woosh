package com.example.woosh.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woosh.data.remote.RetrofitClient
import com.example.woosh.model.TrainData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.*

data class TrainListUiState(
    val isLoading: Boolean = true,
    val selectedClass: String = "Semua",
    val trains: List<TrainData> = emptyList(),
    val filteredTrains: List<TrainData> = emptyList(),
    val errorMessage: String? = null,
    val selectedDate: Long = System.currentTimeMillis()
)

@HiltViewModel
class TrainListViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TrainListUiState())
    val uiState: StateFlow<TrainListUiState> = _uiState.asStateFlow()

    fun fetchTrains(date: Long) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, selectedDate = date) }
        viewModelScope.launch {
            try {
                val targetDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
                val response = RetrofitClient.instance.getTrips(targetDate)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    val trainsList = body?.data?.map { trip ->
                        // Dynamic Pricing Logic: Increase price by 20% during peak hours (07-10 and 16-19)
                        val rawDep = trip.departureTime.trim()
                        val timePartDep = if (rawDep.contains(" ")) {
                            rawDep.split(" ").getOrNull(1) ?: "00:00:00"
                        } else {
                            rawDep
                        }
                        val depTime = if (timePartDep.length >= 5) timePartDep.substring(0, 5) else timePartDep

                        val hour = timePartDep.split(":").firstOrNull()?.toIntOrNull() ?: 0
                        val isPeakHour = hour in 7..10 || hour in 16..19
                        val finalPrice = if (isPeakHour) (trip.basePrice * 1.2).toLong() else trip.basePrice.toLong()
                        val formattedPrice = "Rp ${String.format("%,d", finalPrice).replace(',', '.')}"

                        val rawArr = trip.arrivalTime.trim()
                        val timePartArr = if (rawArr.contains(" ")) {
                            rawArr.split(" ").getOrNull(1) ?: "00:00:00"
                        } else {
                            rawArr
                        }
                        val arrTime = if (timePartArr.length >= 5) timePartArr.substring(0, 5) else timePartArr

                        TrainData(
                            name = trip.trainName,
                            dep = depTime,
                            arr = arrTime,
                            price = formattedPrice,
                            trainClass = trip.trainClass,
                            tripId = trip.tripId
                        )
                    } ?: emptyList()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            trains = trainsList,
                            filteredTrains = filterTrains(trainsList, it.selectedClass)
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Server error: ${response.code()} ${response.message()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Gagal terhubung ke server: ${e.message}")
                }
            }
        }
    }

    private fun filterTrains(trains: List<TrainData>, className: String): List<TrainData> {
        return if (className == "Semua") {
            trains
        } else {
            trains.filter { it.trainClass == className }
        }
    }

    fun selectClass(className: String) {
        _uiState.update { state ->
            state.copy(
                selectedClass = className,
                filteredTrains = filterTrains(state.trains, className)
            )
        }
    }
}
