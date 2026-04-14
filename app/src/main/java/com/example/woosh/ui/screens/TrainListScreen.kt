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
import androidx.navigation.NavHostController
import com.example.woosh.model.TrainData
import com.example.woosh.ui.components.TrainItemShimmer
import com.example.woosh.ui.theme.ElegantDark
import com.example.woosh.ui.theme.PrimaryGold
import com.example.woosh.ui.theme.SurfaceWhite
import com.example.woosh.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainListScreen(navController: NavHostController, destination: String = "Stasiun Halim (BDG)", passengers: Int = 1) {
    var selectedClass by remember { mutableStateOf("Semua") }
    val classes = listOf("Semua", "First Class", "Business Class", "Premium Economy")
    
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1500)
        isLoading = false
    }

    val basePrice = 150000L
    val allTrains = listOf(
        TrainData("WOOSH 502", "07:30", "08:15", "", "Premium Economy"),
        TrainData("WOOSH 514", "09:00", "09:45", "", "Business Class"),
        TrainData("WOOSH 528", "13:30", "14:15", "", "Premium Economy"),
        TrainData("WOOSH 532", "15:00", "15:45", "", "First Class")
    ).map { train ->
        val price = when(train.trainClass) {
            "First Class" -> basePrice * 4
            "Business Class" -> basePrice * 2
            else -> basePrice
        }
        val formattedPrice = "Rp ${String.format("%,d", price).replace(',', '.')}"
        train.copy(price = formattedPrice)
    }
    
    val filteredTrains = if (selectedClass == "Semua") allTrains else allTrains.filter { it.trainClass == selectedClass }
    val isFeederRoute = destination.contains("BDG", ignoreCase = true) || destination.contains("Bandung", ignoreCase = true) || destination.contains("Padalarang", ignoreCase = true)

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Pilih Kereta", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ElegantDark) } }) }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Class Filters
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(classes) { className ->
                    FilterChip(
                        selected = selectedClass == className,
                        onClick = { selectedClass = className },
                        label = { Text(className, fontSize = 12.sp, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElegantDark.copy(alpha = 0.1f),
                            selectedLabelColor = ElegantDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedClass == className,
                            borderColor = Color.LightGray,
                            selectedBorderColor = ElegantDark
                        )
                    )
                }
            }
            
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                if (isLoading) {
                    items(3) { TrainItemShimmer() }
                } else {
                    item { Text("Tersedia ${filteredTrains.size} Kereta", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp)) }
                    items(filteredTrains) { train ->
                        TrainItem(train, isFeederRoute) { 
                            val trainId = train.name.replace(" ", "")
                            navController.navigate("seat_selection/$passengers/${train.price}/$trainId/${train.name}") 
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TrainItem(train: TrainData, isFeeder: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (isFeeder) {
                Surface(color = ElegantDark.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text("Termasuk Tiket KA Feeder", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = ElegantDark, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(12.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(train.name, fontWeight = FontWeight.Bold, color = ElegantDark, fontSize = 16.sp)
                Text(train.price, fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column { Text(train.dep, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("Gambir", fontSize = 12.sp, color = Color.Gray) }
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier.size(8.dp).background(ElegantDark, CircleShape))
                    Box(modifier = Modifier.height(1.dp).weight(1f).background(Color.LightGray))
                    Icon(Icons.Default.Train, null, modifier = Modifier.size(16.dp), tint = ElegantDark)
                    Box(modifier = Modifier.height(1.dp).weight(1f).background(Color.LightGray))
                    Box(modifier = Modifier.size(8.dp).background(ElegantDark, CircleShape))
                }
                Column(horizontalAlignment = Alignment.End) { Text(train.arr, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("Halim", fontSize = 12.sp, color = Color.Gray) }
            }
            Spacer(Modifier.height(12.dp))
            Text("Durasi 45m • ${train.trainClass}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

