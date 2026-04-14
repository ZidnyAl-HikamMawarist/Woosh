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
import androidx.compose.material.icons.filled.Train
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    Box(modifier = Modifier.fillMaxSize().background(OffWhite)) {
        // Decorative background elements
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(ElegantDark.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Icon
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.woosh.R.drawable.logo),
                contentDescription = "Woosh Login Logo",
                modifier = Modifier.size(240.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(-30.dp))

            Text("Selamat Datang", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1A1A))
            Text("Masuk ke akun Woosh Anda", fontSize = 14.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(40.dp))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = ElegantDark) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantDark,
                    focusedLabelColor = ElegantDark
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Kata Sandi") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = ElegantDark) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantDark,
                    focusedLabelColor = ElegantDark
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                } else {
                                    Toast.makeText(context, "Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ElegantDark, contentColor = PrimaryGold),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Masuk Sekarang", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Belum punya akun?", fontSize = 14.sp, color = TextSecondary)
                TextButton(onClick = { navController.navigate("register") }) {
                    Text("Daftar di sini", color = ElegantDark, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // --- GOOGLE SSO ---
            val scope = rememberCoroutineScope()
            val gso = remember {
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    // TODO: Ganti dengan Web Client ID dari Firebase Console -> Google Sign-In
                    .requestIdToken("314649567990-placeholder.apps.googleusercontent.com") 
                    .requestEmail()
                    .build()
            }
            val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }
            
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    isLoading = true
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                                    if (!doc.exists()) {
                                        // Initialize new profile for Google user
                                        val profile = hashMapOf(
                                            "name" to (auth.currentUser?.displayName ?: ""),
                                            "email" to (auth.currentUser?.email ?: ""),
                                            "loyaltyPoints" to 0L,
                                            "createdAt" to System.currentTimeMillis()
                                        )
                                        db.collection("users").document(uid).set(profile)
                                    }
                                    isLoading = false
                                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                }
                            }
                        } else {
                            isLoading = false
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Google Sign-In Gagal", Toast.LENGTH_SHORT).show()
                }
            }

            OutlinedButton(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Train, // Placeholder for Google Icon
                        contentDescription = null,
                        tint = ElegantDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Masuk dengan Google", color = Color.DarkGray, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

