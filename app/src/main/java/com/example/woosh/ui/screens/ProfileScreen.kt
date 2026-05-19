package com.example.woosh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.woosh.ui.components.ContactItem

import com.example.woosh.ui.components.ProfileDetailRow
import com.example.woosh.ui.components.ProfileMenuButton
import com.example.woosh.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController, 
    onLanguageChange: (AppLanguage) -> Unit,
    currentLanguage: AppLanguage,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUser = viewModel.auth.currentUser
    val userName by viewModel.userName.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()
    val loyaltyPoints by viewModel.loyaltyPoints.collectAsState()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val strings = WooshTheme.strings
    val context = LocalContext.current

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Keluar", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("Apakah Anda yakin ingin keluar dari akun Anda?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WooshRed, contentColor = Color.White)
                ) {
                    Text(strings.logout, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal", color = WooshRed)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Delete Account Confirmation
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text(strings.delete_account, fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("Tindakan ini tidak dapat dibatalkan. Semua data tiket, poin loyalitas, dan profil Anda akan dihapus secara permanen.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount { success ->
                            if (success) {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Gagal menghapus akun. Silakan login ulang dan coba lagi.") }
                            }
                        }
                        showDeleteAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Hapus Permanen", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Batal", color = WooshRed)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Password Reset Confirmation
    if (showPasswordResetDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            title = { Text(strings.change_password, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("Kami akan mengirimkan email instruksi perubahan kata sandi ke ${currentUser?.email}. Lanjutkan?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendPasswordReset { success, error ->
                            val message = if (success) "Email reset berhasil dikirim ke ${currentUser?.email}" else (error ?: "Gagal mengirim email")
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        }
                        showPasswordResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WooshRed)
                ) {
                    Text("Kirim Email", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordResetDialog = false }) {
                    Text("Batal", color = WooshRed)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(strings.language, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    LanguageOption("Bahasa Indonesia", AppLanguage.ID, currentLanguage) { onLanguageChange(it); showLanguageDialog = false }
                    LanguageOption("English", AppLanguage.EN, currentLanguage) { onLanguageChange(it); showLanguageDialog = false }
                    LanguageOption("中文", AppLanguage.CN, currentLanguage) { onLanguageChange(it); showLanguageDialog = false }
                }
            },
            confirmButton = {},
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    var showHelpDialog by remember { mutableStateOf(false) }

    // Help Center Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(strings.help_center, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Hubungi kami melalui saluran resmi berikut:", fontSize = 14.sp, color = TextSecondary)
                    ContactItem(Icons.Default.Phone, "150909 (Call Center)")
                    ContactItem(Icons.Default.Message, "0811-8888-111 (WhatsApp)")
                    ContactItem(Icons.Default.Email, "cs@kcic.co.id")
                    ContactItem(Icons.Default.Public, "@keretacepat_id (Instagram)")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Tutup", color = WooshRed)
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
                title = { Text(strings.profile, fontWeight = FontWeight.Bold, color = TextPrimary) },
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
                            .background(WooshRed.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = WooshRed
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(userName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(currentUser?.email ?: "email@example.com", fontSize = 14.sp, color = TextSecondary)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = WooshRed.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp), onClick = { navController.navigate("loyalty") }) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, null, tint = WooshRed, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("${java.text.NumberFormat.getIntegerInstance().format(loyaltyPoints)} ${strings.points}", color = WooshRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = OffWhite, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    var showEditProfileDialog by remember { mutableStateOf(false) }
                    var editName by remember { mutableStateOf(userName) }
                    var editAddress by remember { mutableStateOf(userAddress) }
                    var editPhone by remember { mutableStateOf(userPhone) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Informasi Kontak", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        TextButton(onClick = { 
                            editName = userName
                            editAddress = userAddress
                            editPhone = userPhone
                            showEditProfileDialog = true 
                        }) {
                            Text("Edit", color = WooshRed)
                        }
                    }
                    
                    ProfileDetailRow(Icons.Default.Home, "Alamat", userAddress)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileDetailRow(Icons.Default.Phone, "Telepon", userPhone)

                    if (showEditProfileDialog) {
                        AlertDialog(
                            onDismissRequest = { showEditProfileDialog = false },
                            title = { Text("Edit Profil", fontWeight = FontWeight.Bold, color = TextPrimary) },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        label = { Text("Nama Lengkap") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = editAddress,
                                        onValueChange = { editAddress = it },
                                        label = { Text("Alamat") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = editPhone,
                                        onValueChange = { editPhone = it },
                                        label = { Text("Nomor Telepon") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.updateProfile(editName, editAddress, editPhone)
                                        showEditProfileDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WooshRed)
                                ) {
                                    Text("Simpan", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEditProfileDialog = false }) {
                                    Text("Batal", color = WooshRed)
                                }
                            },
                            containerColor = SurfaceWhite,
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Menu Section
            Text(
                "Pengaturan & Lainnya",
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 12.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            ProfileMenuButton(
                icon = Icons.Default.Language,
                title = strings.language,
                onClick = {
                    showLanguageDialog = true
                }
            )

            ProfileMenuButton(
                icon = Icons.Default.Lock,
                title = strings.change_password,
                onClick = {
                    showPasswordResetDialog = true
                }
            )
            
            ProfileMenuButton(
                icon = Icons.Default.People,
                title = strings.saved_passengers,
                onClick = {
                    navController.navigate("saved_passengers")
                }
            )
            
            ProfileMenuButton(
                icon = Icons.Default.Info,
                title = strings.info_services,
                onClick = {
                    navController.navigate("information")
                }
            )
            
            ProfileMenuButton(
                icon = Icons.AutoMirrored.Filled.Help,
                title = strings.help_center,
                onClick = {
                    showHelpDialog = true
                }
            )

            ProfileMenuButton(
                icon = Icons.Default.Settings,
                title = "Pengaturan & Keamanan",
                onClick = {
                    navController.navigate("settings")
                }
            )

            ProfileMenuButton(
                icon = Icons.Default.DeleteForever,
                title = strings.delete_account,
                onClick = {
                    showDeleteAccountDialog = true
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WooshRed.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                elevation = null
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = WooshRed)
                Spacer(Modifier.width(8.dp))
                Text(strings.logout, color = WooshRed, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("${strings.version} 1.0.4", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun LanguageOption(label: String, lang: AppLanguage, current: AppLanguage, onClick: (AppLanguage) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick(lang) }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = lang == current, onClick = { onClick(lang) }, colors = RadioButtonDefaults.colors(selectedColor = WooshRed))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 16.sp, color = TextPrimary)
    }
}


