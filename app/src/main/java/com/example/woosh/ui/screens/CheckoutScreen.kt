package com.example.woosh.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.woosh.model.Passenger
import com.example.woosh.ui.components.TicketInfo
import com.example.woosh.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavHostController, 
    seats: String, 
    pricePerSeat: String, 
    passengerCount: Int, 
    trainId: String = "WOOSH502", 
    trainName: String = "WOOSH 502",
    date: Long = System.currentTimeMillis(),
    rescheduleId: String? = null,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    val dateFormatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
    val dateStr = dateFormatter.format(Date(date))
    
    LaunchedEffect(Unit) {
        viewModel.initPassengers(passengerCount)
        viewModel.fetchSavedPassengers()
    }

    var showSheet by remember { mutableStateOf<Int?>(null) } // Index of passenger being edited

    if (showSheet != null) {
        ModalBottomSheet(onDismissRequest = { showSheet = null }, containerColor = SurfaceWhite) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("Daftar Penumpang Tersimpan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(16.dp))
                if (uiState.savedPassengers.isEmpty()) {
                    Text("Belum ada data tersimpan", color = TextSecondary, modifier = Modifier.padding(vertical = 16.dp))
                }
                uiState.savedPassengers.forEach { p ->
                    Card(
                        onClick = { 
                            viewModel.updatePassenger(showSheet!!, p)
                            showSheet = null 
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = OffWhite)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = WooshRed)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(p.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(p.idNumber, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
    
    // Calculate total price
    val cleanedPrice = pricePerSeat.replace("Rp ", "").replace(".", "").toLongOrNull() ?: 0L
    val totalPrice = cleanedPrice * passengerCount
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val totalPriceStr = formatter.format(totalPrice).replace("Rp", "Rp ")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Konfirmasi Pesanan", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WooshRed)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(OffWhite)) {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp)
            ) {
                // Train Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = WooshRed, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Detail Perjalanan", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TicketInfo("KERETA", trainName, TextPrimary)
                                TicketInfo("TANGGAL", dateStr, TextPrimary)
                                TicketInfo("KURSI", seats, TextPrimary)
                            }
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = DividerColor.copy(0.5f))
                            Spacer(Modifier.height(12.dp))
                            Text("Halim → Tegalluar Summarecon", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }
                    }
                }

                item { Text("Data Penumpang", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary) }

                items(uiState.passengers.size) { index ->
                    PassengerInputCard(
                        index = index + 1,
                        passenger = uiState.passengers[index],
                        onChanged = { updated -> viewModel.updatePassenger(index, updated) },
                        onShowSaved = { showSheet = index }
                    )
                }

                item {
                    Surface(
                        color = SurfaceWhite,
                        shape = RoundedCornerShape(12.dp),
                        onClick = { viewModel.toggleSavePassengers(!uiState.savePassengersToAccount) },
                        border = BorderStroke(1.dp, if(uiState.savePassengersToAccount) WooshRed else DividerColor)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.savePassengersToAccount, 
                                onCheckedChange = { viewModel.toggleSavePassengers(it) },
                                colors = CheckboxDefaults.colors(checkedColor = WooshRed)
                            )
                            Text("Simpan ke Daftar Penumpang Saya", fontSize = 14.sp, color = TextPrimary)
                        }
                    }
                }
            }

            // Price Details Footer
            val isComplete = uiState.passengers.isNotEmpty() && uiState.passengers.all { it.name.isNotBlank() && it.idNumber.length >= 5 }
            Surface(shadowElevation = 16.dp, color = SurfaceWhite) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total Pembayaran", color = TextSecondary, fontSize = 14.sp)
                            Text(if (rescheduleId != null) "Rp 0" else totalPriceStr, fontWeight = FontWeight.Black, fontSize = 20.sp, color = WooshRed)
                        }
                        
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = WooshRed)
                        } else {
                            Button(
                                onClick = {
                                    if (rescheduleId != null) {
                                        viewModel.rescheduleTicket(rescheduleId, trainId, trainName, seats, date) { success ->
                                            if (success) {
                                                navController.navigate("ticket/$seats") {
                                                    popUpTo("home") { inclusive = false }
                                                }
                                            }
                                        }
                                    } else {
                                        viewModel.savePassengersIfRequested {
                                            navController.navigate("payment/$totalPrice/$seats/$trainId/${android.net.Uri.encode(trainName)}")
                                        }
                                    }
                                },
                                enabled = isComplete,
                                modifier = Modifier.width(180.dp).height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WooshRed, disabledContainerColor = WooshRed.copy(0.3f)),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Text(if (rescheduleId != null) "Konfirmasi" else "Bayar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PassengerInputCard(index: Int, passenger: Passenger, onChanged: (Passenger) -> Unit, onShowSaved: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Penumpang $index", fontWeight = FontWeight.Bold, color = TextPrimary)
                TextButton(onClick = onShowSaved) {
                    Text("Pilih dari Daftar", color = WooshRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = passenger.name,
                onValueChange = { onChanged(passenger.copy(name = it)) },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WooshRed, focusedLabelColor = WooshRed),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = passenger.idNumber,
                onValueChange = { onChanged(passenger.copy(idNumber = it)) },
                label = { Text("Nomor Identitas (NIK/Paspor)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WooshRed, focusedLabelColor = WooshRed),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

