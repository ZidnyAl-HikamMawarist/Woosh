package com.example.woosh.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.woosh.R
import com.example.woosh.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })

    val sheetState = rememberModalBottomSheetState()
    var showSheetType by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.selectedDate)
    val isRouteValid = uiState.origin != uiState.destination

    // Bottom sheet dialogs
    if (showSheetType != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheetType = null },
            sheetState = sheetState,
            containerColor = SurfaceWhite
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                when (showSheetType) {
                    "origin" -> {
                        Text("Pilih Stasiun Asal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.height(16.dp))
                        WooshStations.all.forEach { station ->
                            val isSelected = uiState.origin == station.name
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) WooshRed.copy(0.05f) else SurfaceWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                onClick = { viewModel.updateOrigin(station.name); showSheetType = null }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(40.dp).background(
                                            if (isSelected) WooshRed.copy(0.1f) else Color(0xFFF0F0F0),
                                            CircleShape
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(station.code, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                            color = if (isSelected) WooshRed else TextSecondary)
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        station.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) WooshRed else TextPrimary
                                    )
                                    if (isSelected) {
                                        Spacer(Modifier.weight(1f))
                                        Icon(Icons.Default.CheckCircle, null, tint = WooshRed, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                    "dest" -> {
                        Text("Pilih Stasiun Tujuan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.height(16.dp))
                        WooshStations.all.forEach { station ->
                            val isSelected = uiState.destination == station.name
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) WooshRed.copy(0.05f) else SurfaceWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                onClick = { viewModel.updateDestination(station.name); showSheetType = null }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(40.dp).background(
                                            if (isSelected) WooshRed.copy(0.1f) else Color(0xFFF0F0F0),
                                            CircleShape
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(station.code, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                            color = if (isSelected) WooshRed else TextSecondary)
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        station.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) WooshRed else TextPrimary
                                    )
                                    if (isSelected) {
                                        Spacer(Modifier.weight(1f))
                                        Icon(Icons.Default.CheckCircle, null, tint = WooshRed, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                    "date" -> {
                        DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(
                            selectedDayContainerColor = WooshRed, 
                            selectedDayContentColor = Color.White,
                            todayContentColor = WooshRed
                        ))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = {
                                viewModel.updateSelectedDate(datePickerState.selectedDateMillis ?: uiState.selectedDate)
                                showSheetType = null
                            }) { Text("Selesai", color = WooshRed, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().background(OffWhite)) {
        // =====================================================
        // HERO HEADER — Full-width carousel
        // =====================================================
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background red gradient like official app
                Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(
                    Brush.verticalGradient(listOf(WooshRed, WooshRedDark))
                ))

                // Image Carousel — full width, floating
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 40.dp)
                        .fillMaxWidth()
                        .height(180.dp)
                ) { page ->
                    CarouselBanner(page)
                }

                // Pager dots overlay at bottom of carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 50.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { i ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (pagerState.currentPage == i) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == i) Color.White else Color.White.copy(0.5f))
                        )
                    }
                }
            }
        }

        // =====================================================
        // BOOKING FORM CARD — overlaps the red header
        // =====================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-40).dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // FROM / TO with Swap button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // From column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showSheetType = "origin" }
                                .padding(8.dp)
                        ) {
                            Text("Asal", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text(uiState.origin, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        // Swap button (round, centered)
                        IconButton(
                            onClick = { viewModel.swapRoute() },
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(2.dp, CircleShape)
                                .background(SurfaceWhite, CircleShape)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = WooshRed, modifier = Modifier.size(24.dp))
                        }

                        // To column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showSheetType = "dest" }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("Tujuan", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                            Text(uiState.destination, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    if (!isRouteValid) {
                        Text(
                            "⚠ Stasiun asal dan tujuan tidak boleh sama",
                            color = WooshRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(Modifier.height(16.dp))

                    // Departure date
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showSheetType = "date" }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = WooshRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Tanggal Keberangkatan", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            val formatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
                            Text(formatter.format(Date(uiState.selectedDate)), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // SEARCH BUTTON — red like the real app
                    Button(
                        onClick = {
                            if (isRouteValid) {
                                viewModel.addRecentSearch(uiState.origin, uiState.destination)
                                navController.navigate("train_list/${android.net.Uri.encode(uiState.destination)}/${uiState.passengerCount}/${uiState.selectedDate}")
                            } else {
                                Toast.makeText(context, "Silakan pilih rute yang valid", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WooshRed),
                        shape = RoundedCornerShape(27.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Cari Jadwal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // =====================================================
        // MARQUEE / NOTICE BANNER
        // =====================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-10).dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Campaign, null, tint = WooshRed, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Please check your travel schedule again. The process...",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        // =====================================================
        // RAILWAY REGULATIONS SECTION
        // =====================================================
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Railway regulations", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                TextButton(onClick = { navController.navigate("information") }) {
                    Text("More", color = WooshRed, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Regulation cards
        items(getRegulationItems()) { item ->
            RegulationCard(
                iconRes = item.iconRes,
                title = item.title,
                subtitle = item.subtitle,
                onClick = { navController.navigate("information") }
            )
        }

        // =====================================================
        // RECENT SEARCHES
        // =====================================================
        if (uiState.recentSearches.isNotEmpty()) {
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pencarian Terakhir", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    TextButton(onClick = { viewModel.clearRecentSearches() }) {
                        Text("Hapus", color = WooshRed, fontSize = 12.sp)
                    }
                }
            }
            items(uiState.recentSearches) { search ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        val parts = search.split(" → ")
                        if (parts.size == 2) {
                            viewModel.updateOrigin(parts[0])
                            viewModel.updateDestination(parts[1])
                        }
                    }
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp), tint = WooshRed)
                        Spacer(Modifier.width(12.dp))
                        Text(search, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                }
            }
        }

        // Bottom spacer
        item { Spacer(Modifier.height(32.dp)) }
    }
}

// =====================================================
// CAROUSEL BANNER — mimics real Whoosh carousel
// =====================================================
@Composable
fun CarouselBanner(page: Int) {
    val banners = listOf(
        R.drawable.banner_1,
        R.drawable.banner_2,
        R.drawable.banner_3,
        R.drawable.banner_4
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Image(
            painter = painterResource(id = banners[page % banners.size]),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// =====================================================
// REGULATION CARDS
// =====================================================
data class RegulationItem(
    val iconRes: Int? = null,
    val title: String,
    val subtitle: String
)

fun getRegulationItems(): List<RegulationItem> {
    return listOf(
        RegulationItem(title = "Whoosh and Feeder Train Schedule", subtitle = "Whoosh High Speed Railway schedule and integration with Feeder Trains"),
        RegulationItem(title = "Refund / Reschedule", subtitle = "Refund / Reschedule"),
        RegulationItem(title = "Passenger Guidelines", subtitle = "Terms and conditions for passengers"),
        RegulationItem(title = "Baggage Policy", subtitle = "Rules about luggage and prohibited items")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegulationCard(
    iconRes: Int? = null,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon placeholder (like the real app's square thumbnails)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(WooshRed.copy(0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Train, null, tint = WooshRed, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp, maxLines = 2)
            }
        }
    }
}
