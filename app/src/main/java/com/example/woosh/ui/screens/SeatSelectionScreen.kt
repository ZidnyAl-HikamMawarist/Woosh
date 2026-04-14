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
import androidx.navigation.NavHostController
import com.example.woosh.ui.theme.ElegantDark
import com.example.woosh.ui.theme.PrimaryGold
import com.example.woosh.ui.theme.SurfaceWhite
import com.example.woosh.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(navController: NavHostController, passengerCount: Int = 1, price: String = "Rp 0", trainId: String = "WOOSH502", trainName: String = "WOOSH 502") {
    val selectedSeats = remember { mutableStateListOf<String>() }
    val bookedSeats = remember { mutableStateListOf<String>() } // ALL booked seats across all coaches
    var selectedCoach by remember { mutableIntStateOf(4) } // Default Coach 04
    val coaches = (1..8).toList()
    
    // FETCH BOOKED SEATS REAL-TIME
    DisposableEffect(Unit) {
        val listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("trips").document(trainId)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val seats = snapshot.get("bookedSeats") as? List<String> ?: emptyList()
                    bookedSeats.clear()
                    bookedSeats.addAll(seats)
                }
            }
        onDispose { listener.remove() }
    }
    
    val coachClass = when(selectedCoach) {
        in 1..4 -> "Premium Economy"
        in 5..6 -> "Economy"
        else -> "VIP"
    }
    
    val layout = when(selectedCoach) {
        in 1..4 -> Pair(listOf("A", "B", "C"), listOf("D", "E"))
        in 5..6 -> Pair(listOf("A", "B"), listOf("C", "D"))
        else -> Pair(listOf("A"), listOf("B"))
    }

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Pilih Kursi", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ElegantDark) } }) }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text(trainName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElegantDark); Text("Gerbong ${selectedCoach.toString().padStart(2, '0')}", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                        Text(coachClass, color = ElegantDark, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            ScrollableTabRow(
                selectedTabIndex = selectedCoach - 1,
                containerColor = Color.Transparent,
                contentColor = ElegantDark,
                edgePadding = 24.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedCoach - 1]),
                        color = ElegantDark
                    )
                }
            ) {
                coaches.forEach { coach ->
                    Tab(
                        selected = selectedCoach == coach,
                        onClick = { selectedCoach = coach; selectedSeats.clear() },
                        text = { Text("Coach $coach", fontSize = 13.sp, fontWeight = if(selectedCoach == coach) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp).weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { 
                    LegendItem("Tersedia", SurfaceWhite, true)
                    LegendItem("Dipilih", ElegantDark)
                    LegendItem("Terisi", Color.DarkGray.copy(alpha = 0.2f)) 
                }
                Spacer(modifier = Modifier.height(32.dp))
                LazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { 
                    items(if(coachClass == "VIP") 6 else 10) { row -> 
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { 
                            // Left Seats
                            layout.first.forEach { letter -> 
                                val seatId = "$selectedCoach-${row + 1}$letter"
                                val isBooked = bookedSeats.contains(seatId)
                                
                                SeatIcon("${row + 1}$letter", selectedSeats.contains(seatId), isBooked) { 
                                    if (selectedSeats.contains(seatId)) {
                                        selectedSeats.remove(seatId)
                                    } else if (selectedSeats.size < passengerCount) {
                                        selectedSeats.add(seatId)
                                    }
                                }
                            }
                            
                            // Aisle
                            Spacer(modifier = Modifier.width(if(coachClass == "VIP") 64.dp else 32.dp))
                            
                            // Right Seats
                            layout.second.forEach { letter -> 
                                val seatId = "$selectedCoach-${row + 1}$letter"
                                val isBooked = bookedSeats.contains(seatId)
                                
                                SeatIcon("${row + 1}$letter", selectedSeats.contains(seatId), isBooked) { 
                                    if (selectedSeats.contains(seatId)) {
                                        selectedSeats.remove(seatId)
                                    } else if (selectedSeats.size < passengerCount) {
                                        selectedSeats.add(seatId)
                                    }
                                }
                            }
                        } 
                    } 
                }
            }

            Box(modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = { 
                        if (selectedSeats.size == passengerCount) {
                            val seatsParam = selectedSeats.joinToString(",")
                            // Navigate to checkout instead of ticket
                            navController.navigate("checkout/$seatsParam/$price/$passengerCount/$trainId/$trainName")
                        }
                    }, 
                    enabled = selectedSeats.size == passengerCount, 
                    modifier = Modifier.fillMaxWidth().height(56.dp), 
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantDark, contentColor = PrimaryGold), 
                    shape = RoundedCornerShape(16.dp)
                ) { 
                    Text(if(selectedSeats.size < passengerCount) "Pilih ${passengerCount - selectedSeats.size} Kursi Lagi" else "Konfirmasi ${selectedSeats.size} Kursi", fontWeight = FontWeight.Bold) 
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, hasBorder: Boolean = false) { 
    Row(verticalAlignment = Alignment.CenterVertically) { 
        Box(modifier = Modifier.size(14.dp).background(color, RoundedCornerShape(4.dp)).then(if(hasBorder) Modifier.border(1.dp, ElegantDark, RoundedCornerShape(4.dp)) else Modifier)); 
        Spacer(modifier = Modifier.width(8.dp)); 
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) 
    } 
}

@Composable
fun SeatIcon(displayId: String, isSelected: Boolean, isBooked: Boolean, onToggle: () -> Unit) { 
    Box(
        modifier = Modifier
            .padding(6.dp)
            .size(45.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ElegantDark else if (isBooked) Color.DarkGray.copy(alpha = 0.2f) else SurfaceWhite)
            .then(if (!isSelected && !isBooked) Modifier.border(1.dp, ElegantDark.copy(0.3f), RoundedCornerShape(8.dp)) else Modifier)
            .clickable(enabled = !isBooked) { onToggle() }, 
        contentAlignment = Alignment.Center
    ) { 
        Text(displayId, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else if (isBooked) Color.Gray else ElegantDark) 
    } 
}

