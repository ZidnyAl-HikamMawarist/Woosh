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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woosh.ui.theme.*
import com.example.woosh.utils.SecurityManager
import com.example.woosh.utils.BiometricHelper
import com.example.woosh.BuildConfig
import com.example.woosh.ui.components.ContactItem
import com.example.woosh.data.remote.RetrofitClient


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    onLanguageChange: (AppLanguage) -> Unit,
    currentLanguage: AppLanguage
) {
    val context = LocalContext.current
    val securityManager = remember { SecurityManager(context) }
    val biometricHelper = remember { BiometricHelper(context) }
    val strings = WooshTheme.strings
    
    var isBiometricEnabled by remember { mutableStateOf(securityManager.isBiometricEnabled()) }
    val isBiometricAvailable = biometricHelper.isBiometricAvailable()
    
    // Notification state (persistent)
    var isNotificationEnabled by remember { mutableStateOf(securityManager.isNotificationEnabled()) }
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Dialogs
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(strings.language, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    LanguageOptionItem("Bahasa Indonesia", AppLanguage.ID, currentLanguage) { onLanguageChange(it); showLanguageDialog = false }
                    LanguageOptionItem("English", AppLanguage.EN, currentLanguage) { onLanguageChange(it); showLanguageDialog = false }
                    LanguageOptionItem("中文", AppLanguage.CN, currentLanguage) { onLanguageChange(it); showLanguageDialog = false }
                }
            },
            confirmButton = {},
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Tentang Woosh", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Default.DirectionsTransit,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = WooshRed
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Woosh Mobile", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("Versi 1.0.4", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Aplikasi resmi pemesanan tiket Kereta Cepat Indonesia. Nikmati perjalanan cepat dan nyaman bersama Woosh.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Tutup", color = WooshRed)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Syarat & Ketentuan", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("1. Layanan Woosh disediakan oleh KCIC untuk memudahkan pemesanan tiket kereta cepat.\n\n2. Pengguna wajib memberikan data diri yang benar dan valid sesuai dengan identitas resmi.\n\n3. Pembatalan dan perubahan jadwal tunduk pada kebijakan yang berlaku.\n\n4. Woosh berhak melakukan perubahan layanan tanpa pemberitahuan sebelumnya.", fontSize = 14.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Tutup", color = WooshRed)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Kebijakan Privasi", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Kami menghargai privasi Anda. Data Anda (Nama, NIK, No. Telepon) digunakan semata-mata untuk keperluan pemesanan tiket dan verifikasi perjalanan.\n\nData Anda tidak akan dibagikan kepada pihak ketiga tanpa persetujuan Anda, kecuali diwajibkan oleh hukum.", fontSize = 14.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Tutup", color = WooshRed)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

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
        topBar = {
            TopAppBar(
                title = { Text(strings.settings, fontWeight = FontWeight.Bold, color = TextPrimary) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Akun & Keamanan
            SettingsSection(title = "Akun & Keamanan") {
                SettingsClickItem(
                    icon = Icons.Default.Group,
                    title = strings.saved_passengers,
                    subtitle = "Kelola data penumpang tersimpan",
                    onClick = { navController.navigate("saved_passengers") }
                )
                
                SettingsToggleItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Login Biometrik",
                    subtitle = if (isBiometricAvailable) "Gunakan sidik jari/wajah untuk masuk" else "Hardware tidak mendukung",
                    checked = isBiometricEnabled,
                    onCheckedChange = { 
                        isBiometricEnabled = it
                        securityManager.setBiometricEnabled(it)
                    },
                    enabled = isBiometricAvailable
                )
                
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifikasi",
                    subtitle = "Terima info perjalanan & promo",
                    checked = isNotificationEnabled,
                    onCheckedChange = { 
                        isNotificationEnabled = it
                        securityManager.setNotificationEnabled(it)
                    }
                )
            }

            // Section: Aplikasi
            SettingsSection(title = "Aplikasi") {
                SettingsClickItem(
                    icon = Icons.Default.Language,
                    title = strings.language,
                    subtitle = when(currentLanguage) {
                        AppLanguage.ID -> "Bahasa Indonesia"
                        AppLanguage.EN -> "English"
                        AppLanguage.CN -> "中文"
                    },
                    onClick = { showLanguageDialog = true }
                )
                
                SettingsClickItem(
                    icon = Icons.Default.Gavel,
                    title = "Syarat & Ketentuan",
                    subtitle = "Hak & kewajiban pengguna",
                    onClick = { showTermsDialog = true }
                )

                SettingsClickItem(
                    icon = Icons.Default.Shield,
                    title = "Kebijakan Privasi",
                    subtitle = "Bagaimana kami menjaga data Anda",
                    onClick = { showPrivacyDialog = true }
                )

                SettingsClickItem(
                    icon = Icons.Default.Help,
                    title = strings.help_center,
                    subtitle = "Bantuan & hubungi kami",
                    onClick = { showHelpDialog = true }
                )

                SettingsClickItem(
                    icon = Icons.Default.Info,
                    title = "Tentang Aplikasi",
                    subtitle = "Versi 1.0.4",
                    onClick = { showAboutDialog = true }
                )
            }
            
            // Section: Development (Only shown in Debug builds)
            if (BuildConfig.DEBUG) {
                var serverUrl by remember { mutableStateOf(RetrofitClient.getCurrentBaseUrl()) }
                var showServerConfigDialog by remember { mutableStateOf(false) }

                // Update serverUrl ketika dialog ditutup
                LaunchedEffect(showServerConfigDialog) {
                    if (!showServerConfigDialog) {
                        serverUrl = RetrofitClient.getCurrentBaseUrl()
                    }
                }

                if (showServerConfigDialog) {
                    var ipPresets by remember { mutableStateOf(RetrofitClient.getAvailableAddresses(context)) }
                    var currentSelectedIp by remember { mutableStateOf(RetrofitClient.getSelectedIp()) }
                    var customIpInput by remember { mutableStateOf("") }
                    var isScanning by remember { mutableStateOf(false) }
                    var scanResultMsg by remember { mutableStateOf<String?>(null) }
                    
                    AlertDialog(
                        onDismissRequest = { showServerConfigDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = WooshRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Konfigurasi Server", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
                            }
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Pilih atau masukkan alamat IP/URL server backend Woosh (port default 8000).",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )

                                // 1. Tombol Deteksi Otomatis (Subnet Scanning)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = OffWhite),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (isScanning) {
                                            CircularProgressIndicator(color = WooshRed, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Memindai subnet lokal...", fontSize = 12.sp, color = TextSecondary)
                                        } else {
                                            Button(
                                                onClick = {
                                                    isScanning = true
                                                    scanResultMsg = null
                                                    RetrofitClient.autoDiscoverServer(context) { foundIp ->
                                                        isScanning = false
                                                        if (foundIp != null) {
                                                            scanResultMsg = "Berhasil! Server ditemukan di: $foundIp"
                                                            currentSelectedIp = foundIp
                                                            serverUrl = RetrofitClient.getCurrentBaseUrl()
                                                            ipPresets = RetrofitClient.getAvailableAddresses(context)
                                                        } else {
                                                            scanResultMsg = "Server tidak ditemukan di jaringan lokal."
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = WooshRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Deteksi Otomatis (Scan)", fontSize = 13.sp)
                                            }
                                        }
                                        
                                        scanResultMsg?.let { msg ->
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = msg,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (msg.contains("Berhasil")) Color(0xFF2E7D32) else WooshRed
                                            )
                                        }
                                    }
                                }

                                // 2. Daftar Preset
                                Text("Daftar Alamat Tersedia:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    ipPresets.forEach { preset ->
                                        val isSelected = preset == currentSelectedIp
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    currentSelectedIp = preset
                                                    RetrofitClient.setSelectedIp(context, preset)
                                                    serverUrl = RetrofitClient.getCurrentBaseUrl()
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    currentSelectedIp = preset
                                                    RetrofitClient.setSelectedIp(context, preset)
                                                    serverUrl = RetrofitClient.getCurrentBaseUrl()
                                                },
                                                colors = RadioButtonDefaults.colors(selectedColor = WooshRed)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = preset,
                                                    fontSize = 14.sp,
                                                    color = TextPrimary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                val label = when (preset) {
                                                    "127.0.0.1" -> "ADB Port Forwarding (adb reverse)"
                                                    "10.0.2.2" -> "Default Emulator"
                                                    else -> if (preset.startsWith("http")) "Custom URL" else "IP Lokal / Custom"
                                                }
                                                Text(label, fontSize = 11.sp, color = TextSecondary)
                                            }
                                            
                                            // Tampilkan ikon hapus hanya jika alamat bukan default presets
                                            val isDefaultPreset = preset in listOf("127.0.0.1", "10.0.2.2", "192.168.1.100")
                                            if (!isDefaultPreset) {
                                                IconButton(
                                                    onClick = {
                                                        RetrofitClient.removeCustomAddress(context, preset)
                                                        ipPresets = RetrofitClient.getAvailableAddresses(context)
                                                        if (currentSelectedIp == preset) {
                                                            currentSelectedIp = "127.0.0.1"
                                                            RetrofitClient.setSelectedIp(context, "127.0.0.1")
                                                            serverUrl = RetrofitClient.getCurrentBaseUrl()
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Hapus",
                                                        tint = TextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 3. Tambah Alamat Kustom Baru
                                Text("Tambah Alamat/URL Kustom:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customIpInput,
                                        onValueChange = { customIpInput = it },
                                        placeholder = { Text("Contoh: 192.168.1.15 atau https://ngrok...", fontSize = 12.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = WooshRed,
                                            focusedLabelColor = WooshRed
                                        ),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                    )
                                    Button(
                                        onClick = {
                                            if (customIpInput.isNotBlank()) {
                                                val ip = customIpInput.trim()
                                                RetrofitClient.addCustomAddress(context, ip)
                                                RetrofitClient.setSelectedIp(context, ip)
                                                currentSelectedIp = ip
                                                serverUrl = RetrofitClient.getCurrentBaseUrl()
                                                ipPresets = RetrofitClient.getAvailableAddresses(context)
                                                customIpInput = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WooshRed),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("Tambah", fontSize = 12.sp)
                                    }
                                }

                                // 4. Informasi Tips ADB
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = WooshRed.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = WooshRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "Tips ADB Port Forwarding:",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = WooshRed
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                "Jika menggunakan kabel USB / Wireless Debugging ke laptop, jalankan perintah berikut di komputer:\n\n" +
                                                "adb reverse tcp:8000 tcp:8000\n\n" +
                                                "Kemudian pilih alamat '127.0.0.1'. Koneksi akan otomatis terhubung secara instan tanpa peduli IP Wi-Fi berubah-ubah!",
                                                fontSize = 11.sp,
                                                color = TextPrimary,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { 
                                showServerConfigDialog = false
                            }) {
                                Text("Tutup", color = WooshRed, fontWeight = FontWeight.Bold)
                            }
                        },
                        containerColor = SurfaceWhite,
                        shape = RoundedCornerShape(24.dp)
                    )
                }

                SettingsSection(title = "Development") {
                    SettingsClickItem(
                        icon = Icons.Default.Dns,
                        title = "Konfigurasi Server",
                        subtitle = serverUrl.removePrefix("http://").removePrefix("https://").removeSuffix("/"),
                        onClick = {
                            showServerConfigDialog = true
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Dibuat dengan ❤️ oleh Tim Woosh",
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(WooshRed.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = WooshRed, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = WooshRed,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = DividerColor
            )
        )
    }
}

@Composable
fun SettingsClickItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(WooshRed.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = WooshRed, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
        }
    }
}

@Composable
fun LanguageOptionItem(label: String, lang: AppLanguage, current: AppLanguage, onClick: (AppLanguage) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(lang) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = lang == current,
            onClick = { onClick(lang) },
            colors = RadioButtonDefaults.colors(
                selectedColor = WooshRed,
                unselectedColor = DividerColor
            )
        )
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 16.sp, color = TextPrimary)
    }
}


