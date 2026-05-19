package com.example.woosh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woosh.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhoosherPassScreen(
    navController: NavHostController,
    viewModel: WhoosherPassViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val passes = listOf(
        PassType("Basic Pass", "10 Perjalanan", "Rp 1.500.000", 1500000L, 10, "Hemat 15%", Icons.Default.ConfirmationNumber, Color(0xFF4CAF50)),
        PassType("Silver Pass", "25 Perjalanan", "Rp 3.500.000", 3500000L, 25, "Hemat 25%", Icons.Default.Stars, Color(0xFF9E9E9E)),
        PassType("Gold Pass", "50 Perjalanan", "Rp 6.000.000", 6000000L, 50, "Hemat 40%", Icons.Default.WorkspacePremium, Color(0xFFFFD700))
    )

    LaunchedEffect(uiState.purchaseResult) {
        if (uiState.purchaseResult is PurchaseResult.Success) {
            // Show success or navigate back
            viewModel.resetResult()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Frequent Whoosher Pass", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text(
                        "Pilih paket langganan untuk perjalanan rutin Anda dengan harga lebih hemat.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                items(passes) { pass ->
                    PassCard(pass) {
                        viewModel.buyPass(pass.name, pass.tripCount, pass.priceValue)
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    InfoSection()
                }
            }
            
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = WooshRed
                )
            }
            
            if (uiState.purchaseResult is PurchaseResult.Error) {
                AlertDialog(
                    onDismissRequest = { viewModel.resetResult() },
                    title = { Text("Gagal", color = TextPrimary) },
                    text = { Text((uiState.purchaseResult as PurchaseResult.Error).message, color = TextSecondary) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.resetResult() }) {
                            Text("OK", color = WooshRed)
                        }
                    }
                )
            }
        }
    }
}

data class PassType(
    val name: String,
    val description: String,
    val price: String,
    val priceValue: Long,
    val tripCount: Int,
    val saving: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun PassCard(pass: PassType, onBuy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(pass.accentColor.copy(0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(pass.icon, null, tint = pass.accentColor)
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(pass.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text(pass.description, color = TextSecondary, fontSize = 13.sp)
                }
                Surface(
                    color = WooshRed,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        pass.saving,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Harga Paket", fontSize = 11.sp, color = TextSecondary)
                    Text(pass.price, fontSize = 20.sp, fontWeight = FontWeight.Black, color = WooshRed)
                }
                Button(
                    onClick = onBuy,
                    colors = ButtonDefaults.buttonColors(containerColor = WooshRed, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Beli Sekarang", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WooshRed.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = WooshRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ketentuan Paket", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "• Berlaku selama 30 hari sejak pembelian.\n" +
                "• Hanya berlaku untuk kelas Premium Economy.\n" +
                "• Tiket dapat dipesan kapan saja selama kuota masih ada.\n" +
                "• Tidak dapat dipindahtangankan.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
