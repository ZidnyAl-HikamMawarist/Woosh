package com.example.woosh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woosh.ui.components.ProfileDetailRow
import com.example.woosh.ui.components.ProfileMenuButton
import com.example.woosh.ui.theme.ElegantDark
import com.example.woosh.ui.theme.PrimaryGold
import com.example.woosh.ui.theme.OffWhite
import com.example.woosh.ui.theme.SurfaceWhite
import com.example.woosh.ui.theme.TextSecondary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var userName by remember { mutableStateOf("Memuat...") }
    var userPhone by remember { mutableStateOf("+62 812-xxxx-xxxx") }
    var userAddress by remember { mutableStateOf("Jl. Mawar No. 123, Jakarta") }
    var loyaltyPoints by remember { mutableLongStateOf(0L) }
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("name") ?: "User Woosh"
                    userPhone = document.getString("phone") ?: userPhone
                    userAddress = document.getString("address") ?: userAddress
                    loyaltyPoints = document.getLong("loyaltyPoints") ?: 0L
                }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Keluar", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin keluar dari akun Anda?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantDark, contentColor = PrimaryGold)
                ) {
                    Text("Keluar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal", color = ElegantDark)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = OffWhite)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(ElegantDark.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = ElegantDark
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(userName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(currentUser?.email ?: "email@example.com", fontSize = 14.sp, color = TextSecondary)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = ElegantDark.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), onClick = { navController.navigate("loyalty") }) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, null, tint = ElegantDark, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("${java.text.NumberFormat.getIntegerInstance().format(loyaltyPoints)} Points", color = ElegantDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = OffWhite, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ProfileDetailRow(Icons.Default.Home, "Alamat", userAddress)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileDetailRow(Icons.Default.Phone, "Telepon", userPhone)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Menu Section
            Text(
                "Pengaturan & Lainnya",
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 12.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            
            ProfileMenuButton(
                icon = Icons.Default.Settings,
                title = "Pengaturan Akun",
                onClick = {
                    scope.launch { snackbarHostState.showSnackbar("Fitur Pengaturan Akun segera hadir!") }
                }
            )
            
            ProfileMenuButton(
                icon = Icons.Default.Payment,
                title = "Metode Pembayaran",
                onClick = {
                    scope.launch { snackbarHostState.showSnackbar("Fitur Pembayaran sedang dikembangkan.") }
                }
            )
            
            ProfileMenuButton(
                icon = Icons.AutoMirrored.Filled.Help,
                title = "Pusat Bantuan",
                onClick = {
                    scope.launch { snackbarHostState.showSnackbar("Hubungi kami di support@woosh.id") }
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantDark.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                elevation = null
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = ElegantDark)
                Spacer(Modifier.width(8.dp))
                Text("Keluar dari Akun", color = ElegantDark, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Versi Aplikasi 1.0.4", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

