package com.example.woosh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woosh.data.EmailService
import com.example.woosh.model.Ticket
import com.example.woosh.ui.theme.ElegantDark
import com.example.woosh.ui.theme.PrimaryGold
import com.example.woosh.ui.theme.OffWhite
import com.example.woosh.ui.theme.SurfaceWhite
import com.example.woosh.ui.theme.TextPrimary
import com.example.woosh.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(navController: NavHostController, totalAmount: String, seats: String = "", trainId: String = "WOOSH502", trainName: String = "WOOSH 502") {
    var selectedMethod by remember { mutableStateOf("QRIS") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var dialogTitle by remember { mutableStateOf("") }
    
    // Format Display
    val rawAmount = totalAmount.toLongOrNull() ?: 0L
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val displayAmount = formatter.format(rawAmount).replace("Rp", "Rp ")
    
    val methods = listOf(
        PaymentMethod("QRIS", Icons.Default.QrCode, "E-Wallet (Dana, OVO, GoPay)"),
        PaymentMethod("Virtual Account", Icons.Default.Payment, "BCA, Mandiri, BNI"),
        PaymentMethod("Kartu Kredit/Debit", Icons.Default.CreditCard, "Visa, Mastercard")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Metode Pembayaran", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ElegantDark)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            // Price Details
            Column(modifier = Modifier.fillMaxWidth().background(ElegantDark.copy(0.05f), RoundedCornerShape(16.dp)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Pembayaran", color = TextSecondary, fontSize = 14.sp)
                Text(displayAmount, fontWeight = FontWeight.Black, fontSize = 28.sp, color = ElegantDark)
            }

            Spacer(Modifier.height(32.dp))
            Text("Pilih Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            methods.forEach { method ->
                PaymentMethodItem(
                    method = method,
                    selected = selectedMethod == method.name,
                    onClick = { selectedMethod = method.name }
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    isProcessing = true
                    scope.launch {
                        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "user@example.com"
                        val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Penumpang Woosh"
                        val ticketId = "WSH-TK-${System.currentTimeMillis().toString().takeLast(6)}"
                        val currentDate = "12 Okt 2026" // Fixed for demo, or use actual current date
                        
                        val (isEmailSent, emailError) = EmailService.sendTicketEmail(
                            toEmail = userEmail,
                            name = userName,
                            seats = seats,
                            train = trainName,
                            ticketId = ticketId,
                            date = currentDate,
                            totalPrice = displayAmount,
                            paymentMethod = selectedMethod
                        )
                        
                        isProcessing = false
                        
                        // Always try to save to Firestore regardless of email status
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            val db = FirebaseFirestore.getInstance()
                            val points = rawAmount / 10000 
                            
                            // 1. Update Points
                            db.collection("users").document(uid)
                                .update("loyaltyPoints", FieldValue.increment(points))
                                .addOnFailureListener { e ->
                                    dialogTitle = "Firestore Error (Poin)"
                                    errorMessage = "Gagal update poin: ${e.message}\n\nPastikan Firebase Rules sudah 'allow read, write'."
                                    showErrorDialog = true
                                }
                            
                            // 2. Save Ticket (Use same ticketId)
                            val newTicket = Ticket(
                                id = ticketId,
                                trainName = trainName,
                                seats = seats,
                                date = currentDate,
                                totalPrice = displayAmount,
                                status = "Aktif"
                            )
                            
                            db.collection("users").document(uid).collection("tickets").add(newTicket)
                                .addOnSuccessListener {
                                    // 3. Register booked seats globally
                                    val seatsList = seats.split(",")
                                    db.collection("trips").document(trainId)
                                        .set(
                                            mapOf("bookedSeats" to FieldValue.arrayUnion(*seatsList.toTypedArray())),
                                            com.google.firebase.firestore.SetOptions.merge()
                                        )
                                    
                                    if (isEmailSent) {
                                        Toast.makeText(context, "Berhasil! Tiket dikirim ke $userEmail", Toast.LENGTH_LONG).show()
                                        navController.navigate("ticket/$seats") {
                                            popUpTo("home") { inclusive = false }
                                        }
                                    } else {
                                        dialogTitle = "Email Gagal Dikirim"
                                        errorMessage = "Tiket Anda sudah tersimpan di sistem, namun pengiriman email gagal:\n\n$emailError\n\nSilakan cek tab 'Tiket' di aplikasi."
                                        showErrorDialog = true
                                    }
                                }
                                .addOnFailureListener { e ->
                                    dialogTitle = "Firestore Error (Tiket)"
                                    errorMessage = "Gagal menyimpan tiket: ${e.message}\n\nDomain: ${e.localizedMessage}\n\nSaran: Cek Firebase Rules di Console."
                                    showErrorDialog = true
                                }
                        } else {
                            dialogTitle = "Auth Error"
                            errorMessage = "Sesi Anda telah berakhir. Silakan login kembali."
                            showErrorDialog = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = ElegantDark, contentColor = PrimaryGold),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Bayar Sekarang", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text(dialogTitle, fontWeight = FontWeight.Bold, color = ElegantDark) },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { 
                    showErrorDialog = false 
                    if (dialogTitle == "Email Gagal Dikirim") {
                        navController.navigate("ticket/$seats") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                }) {
                    Text("OK", color = ElegantDark, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

data class PaymentMethod(val name: String, val icon: ImageVector, val subtitle: String)

@Composable
fun PaymentMethodItem(method: PaymentMethod, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) ElegantDark.copy(0.05f) else SurfaceWhite
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, ElegantDark) else null,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(if(selected) ElegantDark else OffWhite, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(method.icon, null, tint = if(selected) Color.White else ElegantDark, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(method.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text(method.subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = ElegantDark))
        }
    }
}

