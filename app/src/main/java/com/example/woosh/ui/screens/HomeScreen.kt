package com.example.woosh.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woosh.ui.components.*
import com.example.woosh.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val sharedPrefs = remember { context.getSharedPreferences("woosh_prefs", android.content.Context.MODE_PRIVATE) }
    
    var origin by remember { mutableStateOf("Stasiun Gambir (JKT)") }
    var destination by remember { mutableStateOf("Stasiun Halim (BDG)") }
    var passengerCount by remember { mutableIntStateOf(1) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedTimeSlot by remember { mutableStateOf("Pagi (06:00 - 11:00)") }
    
    var recentSearches by remember { 
        mutableStateOf(sharedPrefs.getStringSet("recent_searches", emptySet())?.toList()?.take(3) ?: emptyList()) 
    }
    
    val sheetState = rememberModalBottomSheetState()
    var showSheetType by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
    val isRouteValid = origin != destination
    
    var loyaltyPoints by remember { mutableLongStateOf(0L) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    loyaltyPoints = snapshot?.getLong("loyaltyPoints") ?: 0L
                }
        }
    }

    if (showSheetType != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheetType = null },
            sheetState = sheetState,
            containerColor = SurfaceWhite
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                when (showSheetType) {
                    "origin" -> {
                        Text("Pilih Stasiun Asal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ElegantDark)
                        Spacer(Modifier.height(16.dp))
                        listOf("Stasiun Gambir (JKT)", "Stasiun Surabaya Gubeng (SBY)", "Stasiun Halim (BDG)").forEach {
                            TextButton(onClick = { origin = it; showSheetType = null }, modifier = Modifier.fillMaxWidth()) {
                                Text(it, color = if(origin == it) ElegantDark else TextPrimary, fontWeight = if(origin == it) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                    "dest" -> {
                        Text("Pilih Stasiun Tujuan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ElegantDark)
                        Spacer(Modifier.height(16.dp))
                        listOf("Stasiun Halim (BDG)", "Stasiun Yogyakarta (YK)", "Stasiun Padalarang (PDL)", "Stasiun Gambir (JKT)").forEach {
                            TextButton(onClick = { destination = it; showSheetType = null }, modifier = Modifier.fillMaxWidth()) {
                                Text(it, color = if(destination == it) ElegantDark else TextPrimary, fontWeight = if(destination == it) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                    "time" -> {
                        Text("Waktu Keberangkatan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ElegantDark)
                        Spacer(Modifier.height(16.dp))
                        listOf("Pagi (06:00 - 11:00)", "Siang (11:00 - 15:00)", "Sore (15:00 - 19:00)", "Malam (19:00 - 23:00)").forEach {
                            TextButton(onClick = { selectedTimeSlot = it; showSheetType = null }, modifier = Modifier.fillMaxWidth()) {
                                Text(it, color = if(selectedTimeSlot == it) ElegantDark else TextPrimary)
                            }
                        }
                    }
                    "date" -> {
                        DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(selectedDayContainerColor = ElegantDark, selectedDayContentColor = PrimaryGold))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { 
                                selectedDate = datePickerState.selectedDateMillis ?: selectedDate
                                showSheetType = null 
                            }) { Text("Selesai", color = ElegantDark, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        item {
            Spacer(modifier = Modifier.height(32.dp))
            HeaderSection(navController)
            Spacer(modifier = Modifier.height(24.dp))
            FrequentWhoosherCard(points = loyaltyPoints) { navController.navigate("loyalty") }
            Spacer(modifier = Modifier.height(24.dp))
            
            // --- SEARCH FORM ---
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(OffWhite.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        SearchFieldClickable(label = "DARI", value = origin, icon = Icons.Default.TripOrigin) { showSheetType = "origin" }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        SearchFieldClickable(label = "KE", value = destination, icon = Icons.Default.LocationOn) { showSheetType = "dest" }
                    }
                    
                    if (!isRouteValid) {
                        Text("Stasiun asal dan tujuan tidak boleh sama", color = ElegantDark, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp, start = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                            val dateString = formatter.format(Date(selectedDate))
                            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(OffWhite.copy(alpha = 0.5f)).clickable { showSheetType = "date" }.padding(12.dp)) {
                                Text("TANGGAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElegantDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(dateString, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(OffWhite.copy(alpha = 0.5f)).clickable { showSheetType = "time" }.padding(12.dp)) {
                                Text("WAKTU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElegantDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(selectedTimeSlot.split(" ")[0], fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(OffWhite.copy(alpha = 0.5f)).padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, null, modifier = Modifier.size(18.dp), tint = ElegantDark)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("PENUMPANG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElegantDark)
                                    Text("$passengerCount Penumpang", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (passengerCount > 1) passengerCount-- }, modifier = Modifier.size(28.dp).background(SurfaceWhite, CircleShape)) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(14.dp), tint = ElegantDark) }
                                Spacer(Modifier.width(12.dp))
                                IconButton(onClick = { if (passengerCount < 10) passengerCount++ }, modifier = Modifier.size(28.dp).background(SurfaceWhite, CircleShape)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = ElegantDark) }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            if (isRouteValid) {
                                val searchStr = "$origin → $destination"
                                val currentSet = sharedPrefs.getStringSet("recent_searches", emptySet()) ?: emptySet()
                                val newSet = (setOf(searchStr) + currentSet).take(3).toSet()
                                sharedPrefs.edit().putStringSet("recent_searches", newSet).apply()
                                recentSearches = newSet.toList()
                                
                                navController.navigate("train_list/$destination/$passengerCount") 
                            } else {
                                Toast.makeText(context, "Silakan pilih rute yang valid", Toast.LENGTH_SHORT).show()
                            }
                        }, 
                        modifier = Modifier.fillMaxWidth().height(56.dp), 
                        colors = ButtonDefaults.buttonColors(containerColor = if(isRouteValid) ElegantDark else Color.Gray, contentColor = PrimaryGold), 
                        shape = RoundedCornerShape(16.dp), 
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = isRouteValid
                    ) { 
                        Text("Cari Kereta Sekarang", fontWeight = FontWeight.Bold, fontSize = 16.sp) 
                    }
                }
            }
            
            if (recentSearches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Pencarian Terakhir", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    TextButton(onClick = { 
                        sharedPrefs.edit().remove("recent_searches").apply()
                        recentSearches = emptyList()
                    }) { Text("Hapus", color = ElegantDark, fontSize = 12.sp) }
                }
                recentSearches.forEach { search ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        onClick = {
                            val parts = search.split(" → ")
                            if (parts.size == 2) { origin = parts[0]; destination = parts[1] }
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                            Spacer(Modifier.width(16.dp))
                            Text(search, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("Promo Spesial")
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(24.dp))) { page -> OfferBanner() }
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().height(12.dp), horizontalArrangement = Arrangement.Center) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) ElegantDark else Color.LightGray
                    Box(modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(6.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

