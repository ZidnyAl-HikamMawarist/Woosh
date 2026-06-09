package com.example.woosh.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woosh.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.example.woosh.utils.BiometricHelper
import com.example.woosh.utils.SecurityManager
import androidx.fragment.app.FragmentActivity

@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper(context) }
    val securityManager = remember { SecurityManager(context) }

    LaunchedEffect(uiState.loginResult) {
        when (val result = uiState.loginResult) {
            is LoginResult.Success -> {
                navController.navigate("home") { popUpTo("login") { inclusive = true } }
            }
            is LoginResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetResult()
            }
            else -> {}
        }
    }

    // Auto-trigger biometric if enabled
    LaunchedEffect(Unit) {
        if (securityManager.isBiometricEnabled() && biometricHelper.isBiometricAvailable()) {
            val activity = context.findFragmentActivity()
            activity?.let {
                biometricHelper.showBiometricPrompt(
                    activity = it,
                    onSuccess = {
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    },
                    onError = { code, msg ->
                        if (code != 13) { // 13 is user cancel
                            Toast.makeText(context, "Biometrik Gagal: $msg", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(OffWhite)) {
        // Decorative background circle — merah tipis
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(WooshRed.copy(alpha = 0.06f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.woosh.R.drawable.logo),
                contentDescription = "Woosh Login Logo",
                modifier = Modifier.size(240.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(-30.dp))

            Text("Selamat Datang", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Text("Masuk ke akun Woosh Anda", fontSize = 14.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(40.dp))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                leadingIcon = { Icon(Icons.Default.Email, null, tint = WooshRed) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WooshRed,
                    focusedLabelColor = WooshRed,
                    cursorColor = WooshRed
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Kata Sandi") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = WooshRed) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WooshRed,
                    focusedLabelColor = WooshRed,
                    cursorColor = WooshRed
                ),
                singleLine = true
            )

            var showForgotDialog by remember { mutableStateOf(false) }
            var forgotEmail by remember { mutableStateOf("") }

            TextButton(
                onClick = { showForgotDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Lupa Kata Sandi?", color = WooshRed, fontSize = 13.sp)
            }

            if (showForgotDialog) {
                AlertDialog(
                    onDismissRequest = { showForgotDialog = false },
                    title = { Text("Reset Kata Sandi", fontWeight = FontWeight.Bold, color = TextPrimary) },
                    text = {
                        Column {
                            Text("Masukkan email Anda untuk menerima tautan reset kata sandi.", fontSize = 14.sp, color = TextSecondary)
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = forgotEmail,
                                onValueChange = { forgotEmail = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WooshRed,
                                    focusedLabelColor = WooshRed,
                                    cursorColor = WooshRed
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (forgotEmail.isNotBlank()) {
                                    viewModel.sendPasswordReset(forgotEmail) { success, error ->
                                        if (success) {
                                            Toast.makeText(context, "Email reset terkirim!", Toast.LENGTH_LONG).show()
                                            showForgotDialog = false
                                        } else {
                                            Toast.makeText(context, "Gagal: $error", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WooshRed)
                        ) {
                            Text("Kirim", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showForgotDialog = false }) {
                            Text("Batal", color = WooshRed)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.loginWithEmail(email, password)
                        } else {
                            Toast.makeText(context, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WooshRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Masuk Sekarang", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                if (biometricHelper.isBiometricAvailable()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            val activity = context.findFragmentActivity()
                            activity?.let {
                                biometricHelper.showBiometricPrompt(
                                    activity = it,
                                    onSuccess = {
                                        securityManager.setBiometricEnabled(true)
                                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                    },
                                    onError = { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(WooshRed.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = "Biometric Login", tint = WooshRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Belum punya akun?", fontSize = 14.sp, color = TextSecondary)
                TextButton(onClick = { navController.navigate("register") }) {
                    Text("Daftar di sini", color = WooshRed, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- GOOGLE SSO ---
            val gso = remember {
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("676430540085-06lqc18ks7p05ecp212s7e6vq7jqjko0.apps.googleusercontent.com")
                    .requestEmail()
                    .build()
            }
            val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        viewModel.loginWithGoogle(credential)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Google Sign-In Gagal", Toast.LENGTH_SHORT).show()
                }
            }

            OutlinedButton(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, WooshRed.copy(alpha = 0.35f)),
                enabled = !uiState.isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.woosh.R.drawable.ic_google),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Masuk dengan Google", color = TextPrimary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

private fun android.content.Context.findFragmentActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}
