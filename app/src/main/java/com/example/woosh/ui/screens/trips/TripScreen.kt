package com.example.woosh.ui.screens.trips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.woosh.data.remote.TripItem
import com.example.woosh.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(
    viewModel: TripViewModel = viewModel()
) {
    val trips by viewModel.trips.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jadwal Kereta Woosh") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = errorMessage ?: "Terjadi kesalahan", color = Color.Red)
                    Button(onClick = { viewModel.fetchTrips() }) {
                        Text("Coba Lagi")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trips) { trip ->
                        TripCard(trip)
                    }
                }
            }
        }
    }
}

@Composable
fun TripCard(trip: TripItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = trip.trainName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rp ${trip.basePrice}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(text = trip.trainClass, fontSize = 14.sp, color = TextSecondary)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                val depDisplay = trip.departureTime.trim().let { raw ->
                    if (raw.contains(" ")) raw.split(" ").getOrNull(1) ?: "00:00" else raw
                }
                val arrDisplay = trip.arrivalTime.trim().let { raw ->
                    if (raw.contains(" ")) raw.split(" ").getOrNull(1) ?: "00:00" else raw
                }
                Column {
                    Text(text = "Berangkat", fontSize = 12.sp, color = TextSecondary)
                    Text(text = depDisplay, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.width(24.dp))
                Text(text = "➔", color = TextSecondary)
                Spacer(modifier = Modifier.width(24.dp))
                Column {
                    Text(text = "Tiba", fontSize = 12.sp, color = TextSecondary)
                    Text(text = arrDisplay, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
