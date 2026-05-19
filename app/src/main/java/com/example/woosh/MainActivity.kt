package com.example.woosh

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.fragment.app.FragmentActivity
import com.example.woosh.navigation.WooshApp
import com.example.woosh.ui.theme.WooshTheme
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        
        checkBiometricCapability()
        
        setContent {
            WooshTheme {
                WooshApp()
            }
        }
    }

    private fun checkBiometricCapability() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("Biometric", "Aplikasi bisa menggunakan sidik jari.")
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("Biometric", "HP ini tidak punya sensor sidik jari.")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.w("Biometric", "User punya sensor, tapi belum daftarin jari di setting HP.")
            }
            else -> {
                Log.e("Biometric", "Status tidak diketahui.")
            }
        }
    }
}
