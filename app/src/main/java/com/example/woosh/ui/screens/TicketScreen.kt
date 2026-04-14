package com.example.woosh.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woosh.model.Ticket
import com.example.woosh.ui.components.CityInfo
import com.example.woosh.ui.components.TicketInfo
import com.example.woosh.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(navController: NavHostController, selectedSeats: String = "4C") {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Aktif", "Riwayat")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    val tickets = remember { mutableStateListOf<Ticket>() }
    var isLoading by remember { mutableStateOf(true) }

    // REAL-TIME FETCH TICKETS FROM FIRESTORE
    DisposableEffect(currentUser) {
        if (currentUser != null) {
            val listenerRegistration = FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid).collection("tickets")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        tickets.clear()
                        snapshot.documents.forEach { doc ->
                            doc.toObject(Ticket::class.java)?.let { tickets.add(it) }
                        }
                    }
                    isLoading = false
                }
            
            onDispose {
                listenerRegistration.remove()
            }
        } else {
            isLoading = false
            onDispose {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { 
            CenterAlignedTopAppBar(
                title = { Text("Tiket Digital", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = OffWhite)
            ) 
        },
        containerColor = OffWhite
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceWhite,
                contentColor = ElegantDark,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ElegantDark
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElegantDark)
                }
            } else {
                if (selectedTab == 0) {
                    val activeTickets = tickets.filter { it.status == "Aktif" }
                    if (activeTickets.isEmpty()) {
                        EmptyState("Belum ada tiket aktif")
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(activeTickets.size) { index ->
                                TicketCard(activeTickets[index])
                            }
                        }
                    }
                } else {
                    val historyTickets = tickets.filter { it.status == "Selesai" }
                    if (historyTickets.isEmpty()) {
                        EmptyState("Belum ada riwayat tiket")
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(historyTickets.size) { index ->
                                TicketCard(historyTickets[index])
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(24.dp),
        color = SurfaceWhite,
        shadowElevation = 4.dp
    ) {
        Column {
            // Top Part (Journey Info)
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("NOMOR TIKET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElegantDark)
                        Text(ticket.id, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = ElegantDark.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(ticket.status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElegantDark)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    CityInfo("JKT", "Halim", Alignment.Start)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Train, null, modifier = Modifier.size(20.dp), tint = ElegantDark)
                    }
                    CityInfo("BDG", "Padalarang", Alignment.End)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Jadwal: ${ticket.date}", fontSize = 12.sp, color = Color.Gray)
                    Text("Kursi: ${ticket.seats}", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            if (isExpanded) {
                // Perforated Line
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(16.dp, 32.dp).offset(x = (-8).dp).background(OffWhite, CircleShape))
                    Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                    Box(modifier = Modifier.size(16.dp, 32.dp).offset(x = (8).dp).background(OffWhite, CircleShape))
                }

                // QR Section
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(120.dp).background(OffWhite, RoundedCornerShape(12.dp)).padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QrCode2, null, modifier = Modifier.fillMaxSize(), tint = TextPrimary)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Scan saat masuk peron", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp), 
        verticalArrangement = Arrangement.Center, 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(Modifier.height(16.dp))
        Text(message, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}

