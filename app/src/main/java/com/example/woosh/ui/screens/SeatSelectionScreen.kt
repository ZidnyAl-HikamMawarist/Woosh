package com.example.woosh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.woosh.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    navController: NavHostController, 
    passengerCount: Int = 1, 
    price: String = "Rp 0", 
    trainId: String = "WOOSH502", 
    trainName: String = "WOOSH 502",
    date: Long = System.currentTimeMillis(),
    rescheduleId: String? = null,
    viewModel: SeatSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coaches = (1..8).toList()
    
    LaunchedEffect(trainId, passengerCount) {
        viewModel.initialize(passengerCount, trainId)
    }
    
    // Tentukan kelas berdasarkan gerbong sesuai ketentuan:
    // Gerbong 1 = First Class, 2-3 = Business Class, 4+ = Premium Economy / Economy
    val coachClass = when(uiState.selectedCoach) {
        1    -> "First Class"
        2, 3 -> "Business Class"
        4, 5 -> "Premium Economy"
        else -> "Economy"
    }
    
    // Layout kursi per kelas:
    // First Class: 2+2 (4 kursi/baris, lebih lega)
    // Business Class: 2+2
    // Premium Economy: 3+2
    // Economy: 3+2
    val layout = when(coachClass) {
        "First Class"      -> Pair(listOf("A", "B"), listOf("C", "D"))
        "Business Class"   -> Pair(listOf("A", "B"), listOf("C", "D"))
        "Premium Economy"  -> Pair(listOf("A", "B", "C"), listOf("D", "E"))
        else               -> Pair(listOf("A", "B", "C"), listOf("D", "E"))
    }

    // Jumlah baris per kelas
    val rowCount = when(coachClass) {
        "First Class"    -> 8
        "Business Class" -> 10
        else             -> 12
    }

    Scaffold(
        topBar = { 
            CenterAlignedTopAppBar(
                title = { Text(if (rescheduleId != null) "Reschedule" else "Pilih Kursi", fontWeight = FontWeight.Bold, color = Color.White) }, 
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WooshRed)
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(OffWhite)) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { 
                            Text(trainName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WooshRed)
                            Text("Gerbong ${uiState.selectedCoach.toString().padStart(2, '0')}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary) 
                        }
                        Surface(color = WooshRed.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text(coachClass, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = WooshRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            ScrollableTabRow(
                selectedTabIndex = uiState.selectedCoach - 1,
                containerColor = SurfaceWhite,
                contentColor = WooshRed,
                edgePadding = 24.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedCoach - 1]),
                        color = WooshRed
                    )
                }
            ) {
                coaches.forEach { coach ->
                    Tab(
                        selected = uiState.selectedCoach == coach,
                        onClick = { viewModel.selectCoach(coach) },
                        text = { Text("Gerbong $coach", fontSize = 13.sp, fontWeight = if(uiState.selectedCoach == coach) FontWeight.Bold else FontWeight.Normal) },
                        selectedContentColor = WooshRed,
                        unselectedContentColor = TextSecondary
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp).weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { 
                    LegendItem("Tersedia", SurfaceWhite, true)
                    LegendItem("Dipilih", WooshRed)
                    LegendItem("Terisi", DividerColor) 
                }
                Spacer(modifier = Modifier.height(32.dp))
                LazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { 
                    items(rowCount) { row -> 
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { 
                            // Left Seats
                            layout.first.forEach { letter -> 
                                val seatId = "${uiState.selectedCoach}-${row + 1}$letter"
                                val isBooked = uiState.bookedSeats.contains(seatId)
                                SeatIcon("${row + 1}$letter", uiState.selectedSeats.contains(seatId), isBooked) { 
                                    viewModel.toggleSeat(seatId)
                                }
                            }
                            
                            // Aisle
                            Spacer(modifier = Modifier.width(if(coachClass == "First Class") 48.dp else 24.dp))
                            
                            // Right Seats
                            layout.second.forEach { letter -> 
                                val seatId = "${uiState.selectedCoach}-${row + 1}$letter"
                                val isBooked = uiState.bookedSeats.contains(seatId)
                                SeatIcon("${row + 1}$letter", uiState.selectedSeats.contains(seatId), isBooked) { 
                                    viewModel.toggleSeat(seatId)
                                }
                            }
                        } 
                    } 
                }
            }

            Surface(shadowElevation = 8.dp, color = SurfaceWhite) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = { 
                            if (uiState.selectedSeats.size == passengerCount) {
                                val seatsParam = uiState.selectedSeats.joinToString(",")
                                // Navigate to checkout instead of ticket
                                val route = "checkout/$seatsParam/${android.net.Uri.encode(price)}/$passengerCount/$trainId/${android.net.Uri.encode(trainName)}/$date" + if (rescheduleId != null) "?rescheduleId=$rescheduleId" else ""
                                navController.navigate(route)
                            }
                        }, 
                        enabled = uiState.selectedSeats.size == passengerCount, 
                        modifier = Modifier.fillMaxWidth().height(54.dp), 
                        colors = ButtonDefaults.buttonColors(containerColor = WooshRed, disabledContainerColor = WooshRed.copy(0.3f)), 
                        shape = RoundedCornerShape(27.dp)
                    ) {
                        Text(if(uiState.selectedSeats.size < passengerCount) "Pilih ${passengerCount - uiState.selectedSeats.size} Kursi Lagi" else "Lanjutkan", fontWeight = FontWeight.Bold, color = Color.White) 
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, hasBorder: Boolean = false) { 
    Row(verticalAlignment = Alignment.CenterVertically) { 
        Box(modifier = Modifier.size(14.dp).background(color, RoundedCornerShape(4.dp)).then(if(hasBorder) Modifier.border(1.dp, DividerColor, RoundedCornerShape(4.dp)) else Modifier)); 
        Spacer(modifier = Modifier.width(8.dp)); 
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary) 
    } 
}

@Composable
fun SeatIcon(displayId: String, isSelected: Boolean, isBooked: Boolean, onToggle: () -> Unit) { 
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) WooshRed else if (isBooked) DividerColor else SurfaceWhite)
            .then(if (!isSelected && !isBooked) Modifier.border(1.dp, DividerColor, RoundedCornerShape(6.dp)) else Modifier)
            .clickable(enabled = !isBooked) { onToggle() }, 
        contentAlignment = Alignment.Center
    ) { 
        Text(displayId, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else if (isBooked) TextSecondary.copy(0.5f) else TextPrimary) 
    } 
}

