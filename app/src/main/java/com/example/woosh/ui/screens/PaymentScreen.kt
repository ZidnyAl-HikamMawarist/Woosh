package com.example.woosh.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

import com.example.woosh.ui.theme.PrimaryGold
import com.example.woosh.ui.theme.OffWhite
import com.example.woosh.ui.theme.SurfaceWhite
import com.example.woosh.ui.theme.TextPrimary
import com.example.woosh.ui.theme.TextSecondary
import com.example.woosh.ui.theme.WooshRed
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavHostController, 
    totalAmount: String, 
    seats: String = "", 
    trainId: String = "WOOSH502", 
    trainName: String = "WOOSH 502",
    viewModel: PaymentViewModel = hiltViewModel()
) {
    var selectedMethod by remember { mutableStateOf("QRIS") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Format Display
    val rawAmount = totalAmount.toLongOrNull() ?: 0L
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val displayAmount = formatter.format(rawAmount).replace("Rp", "Rp ")
    
    val methods = listOf(
        PaymentMethod("QRIS", Icons.Default.QrCode, "E-Wallet (Dana, OVO, GoPay)"),
        PaymentMethod("Virtual Account", Icons.Default.Payment, "BCA, Mandiri, BNI"),
        PaymentMethod("Kartu Kredit/Debit", Icons.Default.CreditCard, "Visa, Mastercard")
    )

    LaunchedEffect(uiState.paymentStatus) {
        when (val status = uiState.paymentStatus) {
            is PaymentStatus.Success -> {
                Toast.makeText(context, "Berhasil! Tiket dikirim ke ${uiState.userEmail}", Toast.LENGTH_LONG).show()
                navController.navigate("ticket/$seats") {
                    popUpTo("home") { inclusive = false }
                }
                viewModel.resetStatus()
            }
            is PaymentStatus.Idle -> { }
            else -> { }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Metode Pembayaran", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            // Price Details
            Column(modifier = Modifier.fillMaxWidth().background(WooshRed.copy(0.05f), RoundedCornerShape(16.dp)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Pembayaran", color = TextSecondary, fontSize = 14.sp)
                Text(displayAmount, fontWeight = FontWeight.Black, fontSize = 28.sp, color = WooshRed)
            }

            Spacer(Modifier.height(32.dp))
            Text("Pilih Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
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
                    viewModel.processPayment(
                        seats = seats,
                        trainId = trainId,
                        trainName = trainName,
                        displayAmount = displayAmount,
                        rawAmount = rawAmount,
                        selectedMethod = selectedMethod
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = WooshRed, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Bayar Sekarang", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    when (val status = uiState.paymentStatus) {
        is PaymentStatus.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetStatus() },
                title = { Text(status.title, fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = { Text(status.message, color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetStatus() }) {
                        Text("OK", color = WooshRed, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SurfaceWhite,
                shape = RoundedCornerShape(24.dp)
            )
        }
        is PaymentStatus.FailedEmail -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetStatus() },
                title = { Text("Email Gagal Dikirim", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = { Text(status.message, color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.resetStatus()
                        navController.navigate("ticket/$seats") {
                            popUpTo("home") { inclusive = false }
                        }
                    }) {
                        Text("OK", color = WooshRed, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SurfaceWhite,
                shape = RoundedCornerShape(24.dp)
            )
        }
        else -> { }
    }
}

data class PaymentMethod(val name: String, val icon: ImageVector, val subtitle: String)

@Composable
fun PaymentMethodItem(method: PaymentMethod, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) WooshRed.copy(0.05f) else SurfaceWhite
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, WooshRed) else null,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(if(selected) WooshRed else OffWhite, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(method.icon, null, tint = if(selected) Color.White else WooshRed, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(method.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text(method.subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = WooshRed))
        }
    }
}

