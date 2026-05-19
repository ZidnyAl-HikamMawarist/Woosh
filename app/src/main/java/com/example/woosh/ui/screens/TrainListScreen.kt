package com.example.woosh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.woosh.model.TrainData
import com.example.woosh.ui.components.TrainItemShimmer
import com.example.woosh.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainListScreen(
    navController: NavHostController,
    destination: String = "Halim",
    passengers: Int = 1,
    date: Long = System.currentTimeMillis(),
    rescheduleId: String? = null,
    viewModel: TrainListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(date) {
        viewModel.fetchTrains(date)
    }
    val classes = listOf("Semua", "First Class", "Business Class", "Premium Economy")
    val isFeederRoute = destination.contains("BDG", ignoreCase = true) || destination.contains("Bandung", ignoreCase = true) || destination.contains("Padalarang", ignoreCase = true)

    Scaffold(
        topBar = { 
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (rescheduleId != null) "Reschedule" else "Pilih Kereta", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                        Text(formatter.format(Date(date)), color = Color.White.copy(0.8f), fontSize = 12.sp)
                    }
                }, 
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WooshRed)
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(OffWhite)) {
            // Class Filters
            Surface(shadowElevation = 2.dp, color = SurfaceWhite) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(classes) { className ->
                        FilterChip(
                            selected = uiState.selectedClass == className,
                            onClick = { viewModel.selectClass(className) },
                            label = { Text(className, fontSize = 12.sp, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WooshRed,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceWhite,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.selectedClass == className,
                                borderColor = DividerColor,
                                selectedBorderColor = WooshRed
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
            
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                if (uiState.isLoading) {
                    items(3) { TrainItemShimmer() }
                } else if (uiState.errorMessage != null) {
                    item {
                        val errorMsg = uiState.errorMessage ?: "Terjadi kesalahan"
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Gagal memuat jadwal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text(errorMsg, fontSize = 13.sp, color = TextSecondary)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.fetchTrains(date) }, colors = ButtonDefaults.buttonColors(containerColor = WooshRed)) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                } else if (uiState.filteredTrains.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Train, null, modifier = Modifier.size(64.dp), tint = TextSecondary.copy(0.3f))
                            Spacer(Modifier.height(16.dp))
                            Text("Tidak ada jadwal tersedia", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Silakan pilih tanggal atau kelas lain", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    item { Text("Tersedia ${uiState.filteredTrains.size} Kereta", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 16.dp)) }
                    items(uiState.filteredTrains) { train ->
                        TrainItem(train, isFeederRoute) { 
                            // Gunakan tripId asli dari MySQL, fallback ke nama jika kosong
                            val trainId = train.tripId.ifEmpty { train.name.replace(" ", "") }
                            val route = "seat_selection/$passengers/${android.net.Uri.encode(train.price)}/${android.net.Uri.encode(trainId)}/${android.net.Uri.encode(train.name)}/$date" + if (rescheduleId != null) "?rescheduleId=$rescheduleId" else ""
                            navController.navigate(route) 
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun TrainItem(train: TrainData, isFeeder: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick, 
        modifier = Modifier.fillMaxWidth(), 
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite), 
        shape = RoundedCornerShape(12.dp), 
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = WooshRed.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(train.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = WooshRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(train.price, fontWeight = FontWeight.Bold, color = WooshRed, fontSize = 16.sp)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) { 
                    Text(train.dep, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Halim", fontSize = 12.sp, color = TextSecondary) 
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(Icons.Default.Train, null, modifier = Modifier.size(16.dp), tint = DividerColor)
                    Box(modifier = Modifier.width(40.dp).height(1.dp).background(DividerColor))
                    Text("45m", fontSize = 10.sp, color = TextSecondary)
                }
                
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) { 
                    Text(train.arr, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Tegalluar", fontSize = 12.sp, color = TextSecondary) 
                }
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = DividerColor.copy(0.5f))
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(train.trainClass, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                if (isFeeder) {
                    Text("Feeder Included", color = Color(0xFF16a34a), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

