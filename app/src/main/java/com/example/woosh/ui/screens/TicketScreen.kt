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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.woosh.model.Ticket
import com.example.woosh.ui.components.CityInfo
import com.example.woosh.ui.components.TicketInfo
import com.example.woosh.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(navController: NavHostController, selectedSeats: String = "", viewModel: TicketViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Aktif", "Riwayat")
    val snackbarHostState = remember { SnackbarHostState() }
    
    val tickets by viewModel.tickets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    var showRefundDialog by remember { mutableStateOf<Ticket?>(null) }

    LaunchedEffect(selectedSeats) {
        if (selectedSeats.isNotBlank()) {
            snackbarHostState.showSnackbar("Reschedule berhasil! Kursi baru Anda: $selectedSeats")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { 
            CenterAlignedTopAppBar(
                title = { Text("Tiket Digital", fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = OffWhite)
            ) 
        },
        containerColor = OffWhite
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceWhite,
                contentColor = WooshRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = WooshRed
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = if(selectedTab == index) WooshRed else TextSecondary, fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = WooshRed)
                }
            } else {
                // Tiket "Aktif" yang tanggal keberangkatannya sudah lewat dianggap selesai
                val now = Calendar.getInstance()
                now.set(Calendar.HOUR_OF_DAY, 0)
                now.set(Calendar.MINUTE, 0)
                now.set(Calendar.SECOND, 0)
                now.set(Calendar.MILLISECOND, 0)
                val today = now.timeInMillis

                val dateParser = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))

                fun isTicketExpired(ticket: Ticket): Boolean {
                    if (ticket.status != "Aktif") return false
                    return try {
                        val ticketDate = dateParser.parse(ticket.date)
                        ticketDate != null && ticketDate.time < today
                    } catch (e: Exception) { false }
                }

                if (selectedTab == 0) {
                    val activeTickets = tickets.filter { it.status == "Aktif" && !isTicketExpired(it) }
                        .sortedBy { 
                            try {
                                dateParser.parse(it.date)?.time ?: Long.MAX_VALUE
                            } catch (e: Exception) { Long.MAX_VALUE }
                        }
                    if (activeTickets.isEmpty()) {
                        EmptyState("Belum ada tiket aktif")
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(activeTickets.size) { index ->
                                TicketCard(
                                    ticket = activeTickets[index],
                                    onRefundClick = { showRefundDialog = it },
                                    onRescheduleClick = { ticket ->
                                        // Sertakan date (timestamp hari ini) agar route lengkap dan tidak crash
                                        navController.navigate("train_list/Halim/1/${System.currentTimeMillis()}?rescheduleId=${ticket.id}")
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Riwayat: tiket berstatus bukan Aktif, ATAU tiket Aktif yang tanggalnya sudah lewat
                    val historyTickets = tickets.filter { it.status != "Aktif" || isTicketExpired(it) }
                        .sortedByDescending { 
                            try {
                                dateParser.parse(it.date)?.time ?: 0L
                            } catch (e: Exception) { 0L }
                        }
                    if (historyTickets.isEmpty()) {
                        EmptyState("Belum ada riwayat tiket")
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(historyTickets.size) { index ->
                                TicketCard(
                                    ticket = historyTickets[index],
                                    isExpiredView = isTicketExpired(historyTickets[index])
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showRefundDialog != null) {
            AlertDialog(
                onDismissRequest = { showRefundDialog = null },
                title = { Text("Konfirmasi Refund", color = TextPrimary) },
                text = { Text("Apakah Anda yakin ingin membatalkan tiket ${showRefundDialog?.id}? Sesuai ketentuan, pengembalian dana akan diproses dalam 1-3 hari kerja.", color = TextSecondary) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRefundDialog?.let { ticket ->
                                viewModel.refundTicket(ticket.id) { success ->
                                    if (success) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Tiket berhasil direfund")
                                        }
                                    }
                                }
                            }
                            showRefundDialog = null
                        }
                    ) {
                        Text("Ya, Batalkan", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRefundDialog = null }) {
                        Text("Kembali", color = WooshRed)
                    }
                },
                containerColor = SurfaceWhite,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun TicketCard(
    ticket: Ticket,
    onRefundClick: ((Ticket) -> Unit)? = null,
    onRescheduleClick: ((Ticket) -> Unit)? = null,
    isExpiredView: Boolean = false
) {
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
                        Text("NOMOR TIKET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text(ticket.id, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    val displayStatus = if (isExpiredView && ticket.status == "Aktif") "Selesai" else ticket.status
                    val statusColor = when {
                        isExpiredView && ticket.status == "Aktif" -> TextSecondary
                        ticket.status == "Aktif" -> Color(0xFF4CAF50)
                        ticket.status == "Refunded" -> Color(0xFFF44336)
                        ticket.status == "Rescheduled" -> Color(0xFF2196F3)
                        else -> TextSecondary
                    }
                    Surface(color = statusColor.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(displayStatus.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    CityInfo("JKT", "Halim", Alignment.Start)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Train, null, modifier = Modifier.size(20.dp), tint = WooshRed)
                    }
                    CityInfo("BDG", "Padalarang", Alignment.End)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Jadwal: ${ticket.date}", fontSize = 12.sp, color = TextSecondary)
                    Text("Kursi: ${ticket.seats}", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            if (isExpanded) {
                // Perforated Line
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(16.dp, 32.dp).offset(x = (-8).dp).background(OffWhite, CircleShape))
                    Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
                        drawLine(
                            color = DividerColor,
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
                    Text("Scan saat masuk peron", fontSize = 10.sp, color = TextSecondary)
                    
                    if (ticket.status == "Aktif" && !isExpiredView && onRefundClick != null && onRescheduleClick != null) {
                        Spacer(Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { onRescheduleClick(ticket) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, WooshRed)
                            ) {
                                Text("Reschedule", color = WooshRed, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { onRefundClick(ticket) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WooshRed)
                            ) {
                                Text("Refund", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
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
        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = WooshRed.copy(alpha = 0.4f))
        Spacer(Modifier.height(16.dp))
        Text(message, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

