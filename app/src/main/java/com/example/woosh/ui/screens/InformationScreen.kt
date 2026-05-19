package com.example.woosh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

import com.example.woosh.ui.theme.OffWhite
import com.example.woosh.ui.theme.PrimaryGold
import com.example.woosh.ui.theme.SurfaceWhite
import com.example.woosh.ui.theme.TextPrimary
import com.example.woosh.ui.theme.TextSecondary
import com.example.woosh.ui.theme.WooshRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformationScreen(navController: NavHostController) {
    var selectedCategory by remember { mutableStateOf<InfoCategory?>(null) }

    if (selectedCategory != null) {
        InfoDetailDialog(category = selectedCategory!!) {
            selectedCategory = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informasi & Layanan", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Panduan Penumpang",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            InfoCard(
                title = "Syarat & Ketentuan Boarding",
                subtitle = "Prosedur masuk stasiun dan kereta",
                icon = Icons.Default.ConfirmationNumber,
                onClick = { selectedCategory = InfoCategory.BOARDING }
            )
            
            InfoCard(
                title = "Regulasi Kereta Api",
                subtitle = "Hak dan kewajiban penumpang",
                icon = Icons.Default.Gavel,
                onClick = { selectedCategory = InfoCategory.REGULATIONS }
            )
            
            InfoCard(
                title = "Kebijakan Refund & Reschedule",
                subtitle = "Informasi pembatalan dan ubah jadwal",
                icon = Icons.Default.Autorenew,
                onClick = { selectedCategory = InfoCategory.POLICY }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Informasi Stasiun & Akses",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            InfoCard(
                title = "Layanan KA Feeder",
                subtitle = "Koneksi gratis Padalarang - Bandung",
                icon = Icons.Default.Train,
                onClick = { selectedCategory = InfoCategory.FEEDER }
            )
            
            InfoCard(
                title = "Integrasi Antarmoda",
                subtitle = "LRT, Bus Damri, dan Shuttle",
                icon = Icons.Default.DirectionsBus,
                onClick = { selectedCategory = InfoCategory.INTERMODAL }
            )
            
            InfoCard(
                title = "Fasilitas Aksesibilitas",
                subtitle = "Layanan prioritas dan difabel",
                icon = Icons.Default.AccessibilityNew,
                onClick = { selectedCategory = InfoCategory.ACCESSIBILITY }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Layanan Khusus",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            InfoCard(
                title = "Pemesanan Rombongan",
                subtitle = "Layanan grup minimal 20 orang",
                icon = Icons.Default.Groups,
                onClick = { navController.navigate("group_booking") }
            )

            InfoCard(
                title = "Cetak Tiket Fisik (TVM)",
                subtitle = "Panduan cetak di Ticket Vending Machine",
                icon = Icons.Default.Print,
                onClick = { selectedCategory = InfoCategory.TVM }
            )
        }
    }
}

@Composable
fun InfoCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(WooshRed.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = WooshRed)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
        }
    }
}

@Composable
fun InfoDetailDialog(category: InfoCategory, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(category.title, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(category.content, fontSize = 14.sp, lineHeight = 20.sp, color = TextSecondary)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = WooshRed)
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp)
    )
}

enum class InfoCategory(val title: String, val content: String) {
    BOARDING(
        "Syarat & Ketentuan Boarding",
        "1. Penumpang wajib tiba di stasiun minimal 30 menit sebelum keberangkatan.\n\n" +
        "2. Gate boarding akan ditutup 5 menit sebelum jadwal keberangkatan kereta.\n\n" +
        "3. Siapkan E-Ticket (QR Code) di aplikasi atau tiket fisik untuk dipindai di gate.\n\n" +
        "4. Penumpang wajib membawa kartu identitas asli (KTP/Passport) yang sesuai dengan data di tiket.\n\n" +
        "5. Pemeriksaan keamanan dilakukan sebelum memasuki area peron."
    ),
    REGULATIONS(
        "Regulasi Kereta Api",
        "1. Dilarang merokok di dalam kereta dan di seluruh area stasiun.\n\n" +
        "2. Dilarang membawa barang berbahaya, senjata tajam, atau barang yang berbau menyengat (seperti durian).\n\n" +
        "3. Berat bagasi maksimal adalah 20kg per penumpang dengan dimensi tertentu.\n\n" +
        "4. Penumpang wajib menjaga ketertiban dan kebersihan selama perjalanan.\n\n" +
        "5. Anak di bawah 3 tahun gratis jika tidak mengambil kursi terpisah."
    ),
    POLICY(
        "Refund & Reschedule",
        "**Reschedule:**\n" +
        "- Dapat dilakukan hingga 5 menit sebelum keberangkatan.\n" +
        "- Reschedule pertama gratis jika tanggal sama & kelas sama.\n" +
        "- Diluar ketentuan tersebut dikenakan bea 25%.\n\n" +
        "**Refund:**\n" +
        "- Dapat diajukan hingga 2 jam sebelum keberangkatan.\n" +
        "- Dikenakan bea pembatalan sebesar 25% dari harga tiket.\n" +
        "- Dana akan dikembalikan ke rekening yang terdaftar dalam 7-15 hari kerja."
    ),
    FEEDER(
        "Layanan KA Feeder",
        "KA Feeder adalah layanan penghubung gratis bagi penumpang Whoosh yang akan menuju atau berasal dari Stasiun Bandung.\n\n" +
        "- Rute: Stasiun Padalarang - Stasiun Cimahi - Stasiun Bandung.\n" +
        "- Tiket Whoosh sudah termasuk akses gratis KA Feeder.\n" +
        "- Jadwal KA Feeder disesuaikan dengan jadwal kedatangan dan keberangkatan Whoosh di Stasiun Padalarang."
    ),
    INTERMODAL(
        "Integrasi Antarmoda",
        "**Stasiun Halim:** Terhubung langsung dengan LRT Jabodebek rute Bekasi & Cibubur.\n\n" +
        "**Stasiun Padalarang:** Terhubung dengan KA Feeder, KA Lokal Commuter Line, dan Bus Trans Metro Pasundan.\n\n" +
        "**Stasiun Tegalluar:** Tersedia layanan Shuttle Bus Damri menuju pusat kota Bandung dan area Mall Summarecon."
    ),
    ACCESSIBILITY(
        "Fasilitas Aksesibilitas",
        "Whoosh berkomitmen memberikan layanan inklusif:\n\n" +
        "- Tersedia kursi prioritas di setiap rangkaian kereta.\n" +
        "- Toilet khusus disabilitas dengan ruang yang luas.\n" +
        "- Lift dan escalator di setiap stasiun untuk aksesibilitas.\n" +
        "- Petugas siap membantu penumpang lansia, ibu hamil, dan difabel dari gate hingga ke dalam kereta."
    ),
    TVM(
        "Cetak Tiket Fisik (TVM)",
        "Anda dapat mencetak tiket fisik melalui Ticket Vending Machine (TVM) di stasiun:\n\n" +
        "1. Pilih menu 'Cetak Tiket' pada layar TVM.\n\n" +
        "2. Pindai QR Code yang ada di aplikasi Woosh Anda atau masukkan Kode Booking.\n\n" +
        "3. Periksa kembali detail perjalanan Anda yang muncul di layar.\n\n" +
        "4. Klik 'Cetak' dan ambil tiket fisik Anda.\n\n" +
        "**Catatan:** Tiket fisik hanya dapat dicetak maksimal 1 jam sebelum keberangkatan."
    )
}
