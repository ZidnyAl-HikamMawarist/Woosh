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
import androidx.navigation.NavHostController
import com.example.woosh.model.Passenger
import com.example.woosh.ui.components.TicketInfo
import com.example.woosh.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavHostController, seats: String, pricePerSeat: String, passengerCount: Int, trainId: String = "WOOSH502", trainName: String = "WOOSH 502") {
    val passengers = remember { 
        mutableStateListOf<Passenger>().apply {
            repeat(passengerCount) { add(Passenger()) }
        }
    }
    var savePassengers by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf<Int?>(null) } // Index of passenger being edited
    val savedPassengers = remember { mutableStateListOf<Passenger>() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    
    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(uid).collection("saved_passengers")
                .get().addOnSuccessListener { snapshot ->
                    savedPassengers.clear()
                    snapshot.documents.forEach { doc ->
                        val p = doc.toObject(Passenger::class.java)
                        if (p != null) savedPassengers.add(p)
                    }
                }
        }
    }

    if (showSheet != null) {
        ModalBottomSheet(onDismissRequest = { showSheet = null }, containerColor = SurfaceWhite) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("Daftar Penumpang Tersimpan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ElegantDark)
                Spacer(Modifier.height(16.dp))
                if (savedPassengers.isEmpty()) {
                    Text("Belum ada data tersimpan", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                }
                savedPassengers.forEach { p ->
                    Card(
                        onClick = { 
                            passengers[showSheet!!] = p
                            showSheet = null 
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = OffWhite)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = ElegantDark)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(p.name, fontWeight = FontWeight.Bold)
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
                title = { Text("Ringkasan Pesanan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ElegantDark)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                // Train Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Detail Perjalanan", fontWeight = FontWeight.Bold, color = ElegantDark, fontSize = 14.sp)
                            Spacer(Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TicketInfo("KERETA", trainName, TextPrimary)
                                TicketInfo("KURSI", seats, TextPrimary)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Gambir → Halim", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                }

                item { Text("Data Penumpang ($passengerCount Orang)", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

                items(passengerCount) { index ->
                    PassengerInputCard(
                        index = index + 1,
                        passenger = passengers[index],
                        onChanged = { updated -> passengers[index] = updated },
                        onShowSaved = { showSheet = index }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { savePassengers = !savePassengers },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = savePassengers, 
                            onCheckedChange = { savePassengers = it },
                            colors = CheckboxDefaults.colors(checkedColor = ElegantDark)
                        )
                        Text("Simpan ke Daftar Penumpang Saya", fontSize = 14.sp, color = TextPrimary)
                    }
                }
            }

            // Price Details Footer
            Column(modifier = Modifier.fillMaxWidth().background(SurfaceWhite).padding(24.dp)) {
                Column(modifier = Modifier.fillMaxWidth().background(OffWhite, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Harga ($passengerCount x)", color = TextSecondary)
                        Text(pricePerSeat, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Pembayaran", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(totalPriceStr, fontWeight = FontWeight.Black, fontSize = 18.sp, color = ElegantDark)
                    }
                }

                Spacer(Modifier.height(24.dp))

                val isComplete = passengers.all { it.name.isNotBlank() && it.idNumber.length >= 5 }
                
                Button(
                    onClick = { 
                        if (savePassengers) {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid != null) {
                                val db = FirebaseFirestore.getInstance()
                                passengers.forEach { p ->
                                    db.collection("users").document(uid)
                                        .collection("saved_passengers")
                                        .add(p)
                                }
                            }
                        }
                        // Navigate to payment with raw total and seats
                        navController.navigate("payment/$totalPrice/$seats/$trainId/$trainName") 
                    },
                    enabled = isComplete,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantDark, contentColor = PrimaryGold),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Lanjut ke Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Penumpang $index", fontWeight = FontWeight.Bold, color = ElegantDark, fontSize = 12.sp)
                TextButton(onClick = onShowSaved) {
                    Text("Pilih dari Daftar", fontSize = 12.sp, color = ElegantDark)
                }
            }
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = passenger.name,
                onValueChange = { onChanged(passenger.copy(name = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nama Lengkap") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = ElegantDark) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = passenger.idNumber,
                onValueChange = { if(it.length <= 20) onChanged(passenger.copy(idNumber = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("NIK / Nomor Paspor") },
                leadingIcon = { Icon(Icons.Default.CreditCard, null, tint = ElegantDark) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
    }
}

