package com.example.woosh.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woosh.ui.theme.ElegantDark
import com.example.woosh.ui.theme.PrimaryGold
import com.example.woosh.ui.theme.OffWhite
import com.example.woosh.ui.theme.TextSecondary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(OffWhite)) {
        // Decorative background elements
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 200.dp, y = (-100).dp)
                .background(ElegantDark.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                "Buat Akun", 
                fontSize = 28.sp, 
                fontWeight = FontWeight.Black, 
                color = Color(0xFF1A1A1A),
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                "Daftar untuk menikmati layanan premium Woosh", 
                fontSize = 14.sp, 
                color = TextSecondary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 32.dp)
            )

            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = ElegantDark) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantDark,
                    focusedLabelColor = ElegantDark
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            // Phone Input
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor Telepon") },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = ElegantDark) },
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

            // Register Button
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank()) {
                        isLoading = true
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        val userProfile = hashMapOf(
                                            "name" to name,
                                            "email" to email,
                                            "phone" to phone,
                                            "address" to "Belum diatur",
                                            "loyaltyPoints" to 0L,
                                            "createdAt" to System.currentTimeMillis()
                                        )
                                        FirebaseFirestore.getInstance().collection("users")
                                            .document(userId)
                                            .set(userProfile)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(context, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Harap lengkapi semua data", Toast.LENGTH_SHORT).show()
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
                    Text("Daftar Sekarang", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sudah punya akun?", fontSize = 14.sp, color = TextSecondary)
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Masuk di sini", color = ElegantDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

