package com.example.woosh.data.remote

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections

object RetrofitClient {

    private const val PORT = "8000"
    private const val PREFS_NAME = "woosh_network_prefs"
    private const val KEY_SELECTED_IP = "selected_ip"
    private const val KEY_PRESETS = "ip_presets"

    // Default static presets
    private val DEFAULT_PRESETS = listOf(
        "127.0.0.1",     // ADB Port Forwarding (adb reverse)
        "10.0.2.2",      // Default emulator Android
        "192.168.1.100"  // Contoh IP lokal
    )

    // Untuk backward compatibility jika ada file lain yang merujuk field ini
    val availableIps = arrayOf(
        "127.0.0.1",
        "10.0.2.2",
        "192.168.1.100"
    )

    private var selectedIp: String = "127.0.0.1"

    /**
     * Harus dipanggil sekali saat aplikasi pertama kali dijalankan.
     */
    fun init(context: Context) {
        val prefs = getPrefs(context)
        selectedIp = prefs.getString(KEY_SELECTED_IP, "127.0.0.1") ?: "127.0.0.1"
    }

    /**
     * Mendapatkan daftar alamat server (preset default + custom yang tersimpan)
     */
    fun getAvailableAddresses(context: Context): List<String> {
        val prefs = getPrefs(context)
        val savedCustom = prefs.getString(KEY_PRESETS, "") ?: ""
        val customList = if (savedCustom.isEmpty()) emptyList() else savedCustom.split(",")
        return (DEFAULT_PRESETS + customList).distinct().filter { it.isNotBlank() }
    }

    /**
     * Menyimpan custom IP atau URL baru ke daftar preset
     */
    fun addCustomAddress(context: Context, address: String) {
        val cleanAddress = address.trim()
        if (cleanAddress.isBlank()) return
        val currentCustom = getPrefs(context).getString(KEY_PRESETS, "") ?: ""
        val customList = if (currentCustom.isEmpty()) emptyList() else currentCustom.split(",")
        
        if (!DEFAULT_PRESETS.contains(cleanAddress) && !customList.contains(cleanAddress)) {
            val newCustom = if (currentCustom.isEmpty()) cleanAddress else "$currentCustom,$cleanAddress"
            getPrefs(context).edit().putString(KEY_PRESETS, newCustom).apply()
        }
    }

    /**
     * Menghapus custom IP atau URL dari daftar preset
     */
    fun removeCustomAddress(context: Context, address: String) {
        val cleanAddress = address.trim()
        val currentCustom = getPrefs(context).getString(KEY_PRESETS, "") ?: ""
        if (currentCustom.isEmpty()) return
        
        val customList = currentCustom.split(",").toMutableList()
        if (customList.remove(cleanAddress)) {
            val newCustom = customList.joinToString(",")
            getPrefs(context).edit().putString(KEY_PRESETS, newCustom).apply()
        }
    }

    /**
     * Mengubah alamat server aktif
     */
    fun setSelectedIp(context: Context, ip: String) {
        selectedIp = ip.trim()
        saveIp(context, selectedIp)
    }

    /**
     * Mendapatkan alamat server yang sedang dipilih
     */
    fun getSelectedIp(): String = selectedIp

    /**
     * Mendapatkan BASE_URL yang sedang aktif
     */
    fun getCurrentBaseUrl(): String {
        return if (selectedIp.startsWith("http://") || selectedIp.startsWith("https://")) {
            selectedIp
        } else {
            "http://$selectedIp:$PORT/"
        }
    }

    private val dynamicHostInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        
        // Cek apakah alamat server yang dipilih adalah URL lengkap atau hanya IP/host
        val newBaseUrl = if (selectedIp.startsWith("http://") || selectedIp.startsWith("https://")) {
            selectedIp.toHttpUrlOrNull()
        } else {
            "http://$selectedIp:$PORT/".toHttpUrlOrNull()
        }
        
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
     * Mencari IP lokal HP Anda saat terhubung ke Wi-Fi
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress) {
                        val sAddr = address.hostAddress ?: continue
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4) {
                            return sAddr
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    /**
     * Memindai subnet lokal untuk mencari server backend di port 8000
     */
    fun autoDiscoverServer(context: Context, onCompleted: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val port = PORT.toIntOrNull() ?: 8000
            
            // 1. Coba 10.0.2.2 terlebih dahulu (jika di emulator)
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress("10.0.2.2", port), 150)
                socket.close()
                withContext(Dispatchers.Main) {
                    setSelectedIp(context, "10.0.2.2")
                    onCompleted("10.0.2.2")
                }
                return@launch
            } catch (e: Exception) {
                // Bukan di emulator atau emulator tidak terkoneksi ke host 10.0.2.2
            }

            // 2. Coba localhost/127.0.0.1 (jika adb reverse aktif)
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress("127.0.0.1", port), 150)
                socket.close()
                withContext(Dispatchers.Main) {
                    setSelectedIp(context, "127.0.0.1")
                    onCompleted("127.0.0.1")
                }
                return@launch
            } catch (e: Exception) {
                // adb reverse tidak aktif
            }

            // 3. Scan subnet Wi-Fi lokal
            val localIp = getLocalIpAddress()
            if (localIp == null || (!localIp.startsWith("192.168.") && !localIp.startsWith("10.") && !localIp.startsWith("172."))) {
                withContext(Dispatchers.Main) { onCompleted(null) }
                return@launch
            }

            val ipParts = localIp.split(".")
            if (ipParts.size != 4) {
                withContext(Dispatchers.Main) { onCompleted(null) }
                return@launch
            }

            val subnetPrefix = "${ipParts[0]}.${ipParts[1]}.${ipParts[2]}."
            
            // Jalankan scanning paralel ke 254 IP subnet
            val deferreds = (1..254).map { i ->
                async {
                    val targetIp = subnetPrefix + i
                    if (targetIp == localIp) return@async null
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(targetIp, port), 250) // Timeout 250ms
                        socket.close()
                        targetIp
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            val foundIp = deferreds.awaitAll().filterNotNull().firstOrNull()
            
            withContext(Dispatchers.Main) {
                if (foundIp != null) {
                    setSelectedIp(context, foundIp)
                    addCustomAddress(context, foundIp)
                }
                onCompleted(foundIp)
            }
        }
    }

    /**
     * AlertDialog fallback bawaan (jika masih dipanggil oleh bagian lain)
     */
    fun showIpSelector(context: Context, onSelected: (() -> Unit)? = null) {
        val addresses = getAvailableAddresses(context).toTypedArray()
        val currentIndex = addresses.indexOf(selectedIp).coerceAtLeast(0)

        AlertDialog.Builder(context)
            .setTitle("Pilih Server IP")
            .setSingleChoiceItems(addresses, currentIndex) { dialog, which ->
                setSelectedIp(context, addresses[which])
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
