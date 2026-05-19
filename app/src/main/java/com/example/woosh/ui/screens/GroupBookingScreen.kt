package com.example.woosh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun GroupBookingScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Booking", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(WooshRed, Color(0xFFFF6B6B))),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.Groups, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Perjalanan Rombongan", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Nikmati kemudahan pemesanan khusus untuk grup minimal 20 orang dengan layanan prioritas.",
                            color = Color.White.copy(0.8f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Text("Keuntungan Group Booking", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)

            BenefitItem(
                icon = Icons.Default.Discount,
                title = "Harga Khusus",
                description = "Tarif spesial untuk pemesanan rombongan besar."
            )
            BenefitItem(
                icon = Icons.Default.EventSeat,
                title = "Blok Kursi",
                description = "Memastikan seluruh anggota rombongan duduk di area yang sama."
            )
            BenefitItem(
                icon = Icons.Default.SupportAgent,
                title = "Dedicated PIC",
                description = "Bantuan khusus dari tim sales untuk koordinasi perjalanan."
            )

            Spacer(Modifier.height(12.dp))
            
            Text("Cara Pemesanan", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    StepItem("1", "Ajukan permohonan minimal H-10 keberangkatan.")
                    StepItem("2", "Kirimkan data peserta (NIK/Nama) dalam format Excel.")
                    StepItem("3", "Lakukan pembayaran melalui Virtual Account khusus.")
                    StepItem("4", "Terima tiket rombongan via Email atau ambil di stasiun.")
                }
            }

            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = { /* Inquiry Action */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WooshRed, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Chat, null)
                Spacer(Modifier.width(12.dp))
                Text("Hubungi Sales Corporate", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun BenefitItem(icon: ImageVector, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(WooshRed.copy(0.08f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = WooshRed, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            Text(description, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun StepItem(number: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "$number.",
            fontWeight = FontWeight.Bold,
            color = WooshRed,
            modifier = Modifier.width(24.dp)
        )
        Text(text, fontSize = 13.sp, color = TextSecondary)
    }
}
