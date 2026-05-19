package com.example.woosh.data.remote

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // ============================================================
    // Daftar IP lokal yang tersedia untuk dipilih
    // Tambahkan IP baru di sini sesuai kebutuhan jaringan Anda
    // ============================================================
    val availableIps = arrayOf(
        "192.168.0.104",
        "10.30.203.131",
        "10.10.45.41",   // Jaringan lokal
        "10.0.2.2"       // Default emulator Android
    )

    private const val PORT = "8000"
    private const val PREFS_NAME = "woosh_network_prefs"
    private const val KEY_SELECTED_IP = "selected_ip"

    private var selectedIp: String = availableIps[0]

    /**
     * Harus dipanggil sekali saat aplikasi pertama kali dijalankan
     * (biasanya di Application.onCreate() atau MainActivity.onCreate())
     * untuk memuat IP yang tersimpan dari SharedPreferences.
     */
    fun init(context: Context) {
        val prefs = getPrefs(context)
        val savedIp = prefs.getString(KEY_SELECTED_IP, availableIps.last()) ?: availableIps.last()
        
        // Ensure the saved IP is still valid in availableIps, otherwise use default
        if (availableIps.contains(savedIp)) {
            selectedIp = savedIp
        } else {
            selectedIp = availableIps.last() // 10.0.2.2
            saveIp(context, selectedIp)
        }
    }

    private val dynamicHostInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val newBaseUrl = "http://$selectedIp:$PORT/".toHttpUrlOrNull()
        
        if (newBaseUrl != null) {
            val newUrl = originalRequest.url.newBuilder()
                .scheme(newBaseUrl.scheme)
                .host(newBaseUrl.host)
                .port(newBaseUrl.port)
                .build()
            
            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()
            
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(dynamicHostInterceptor)
            .build()
    }

    /**
     * Instance ApiService Singleton.
     * Menggunakan interceptor untuk mengubah host secara dinamis berdasarkan IP yang dipilih.
     */
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost/") // Placeholder, di-override oleh interceptor
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Mendapatkan BASE_URL yang sedang aktif (untuk debugging/display)
     */
    fun getCurrentBaseUrl(): String = "http://$selectedIp:$PORT/"

    /**
     * Mendapatkan IP yang sedang dipilih
     */
    fun getSelectedIp(): String = selectedIp

    /**
     * Menampilkan AlertDialog untuk memilih IP dari daftar.
     * IP yang dipilih langsung disimpan ke SharedPreferences.
     *
     * Contoh pemanggilan:
     *   RetrofitClient.showIpSelector(context)
     */
    fun showIpSelector(context: Context, onSelected: (() -> Unit)? = null) {
        val currentIndex = availableIps.indexOf(selectedIp).coerceAtLeast(0)

        AlertDialog.Builder(context)
            .setTitle("Pilih Server IP")
            .setSingleChoiceItems(availableIps, currentIndex) { dialog, which ->
                selectedIp = availableIps[which]
                saveIp(context, selectedIp)
                dialog.dismiss()
                onSelected?.invoke()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // --- Private Helpers ---

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun saveIp(context: Context, ip: String) {
        getPrefs(context).edit().putString(KEY_SELECTED_IP, ip).apply()
    }
}
