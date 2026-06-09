# Server Configuration Guide

## Problem: Server Configuration Not Persisting

**Issue**: Saat user memilih IP server di Settings → Development → Konfigurasi Server, pilihan tidak tersimpan dengan benar.

**Root Cause**: Subtitle di SettingsScreen menampilkan `10.0.2.2` padahal user sudah memilih IP lain (misalnya `10.10.45.41`).

---

## Solution: How Server Configuration Works

### 1. RetrofitClient Configuration

**File**: `app/src/main/java/com/example/woosh/data/remote/RetrofitClient.kt`

```kotlin
// Initialization (harus dipanggil sekali saat app start)
fun init(context: Context) {
    val prefs = getPrefs(context)
    selectedIp = prefs.getString(KEY_SELECTED_IP, "127.0.0.1") ?: "127.0.0.1"
}

// Set selected IP
fun setSelectedIp(context: Context, ip: String) {
    selectedIp = ip.trim()
    saveIp(context, selectedIp)
}

// Get current base URL
fun getCurrentBaseUrl(): String {
    return if (selectedIp.startsWith("http://") || selectedIp.startsWith("https://")) {
        selectedIp
    } else {
        "http://$selectedIp:$PORT/"
    }
}
```

### 2. SettingsScreen Configuration Dialog

**File**: `app/src/main/java/com/example/woosh/ui/screens/SettingsScreen.kt`

The dialog allows users to:
1. **Auto-detect server** - Scan subnet untuk menemukan server
2. **Select from presets** - Pilih dari daftar IP yang tersedia
3. **Add custom IP** - Tambah IP/URL custom baru
4. **Delete custom IP** - Hapus IP custom yang sudah ditambahkan

### 3. How to Use

#### Option A: ADB Port Forwarding (Recommended for Development)

```bash
# On your computer (Windows/Mac/Linux)
adb reverse tcp:8000 tcp:8000

# In mobile app:
# Settings → Development → Konfigurasi Server
# Select: 127.0.0.1 (ADB Port Forwarding)
```

**Advantages**:
- ✅ Works even if Wi-Fi IP changes
- ✅ Fastest connection
- ✅ No need to reconfigure

#### Option B: Direct IP Address

```bash
# Find your laptop IP on Wi-Fi
# Windows: ipconfig
# Mac/Linux: ifconfig

# In mobile app:
# Settings → Development → Konfigurasi Server
# Select or add your laptop IP (e.g., 192.168.1.100)
```

**Disadvantages**:
- ❌ Need to reconfigure if Wi-Fi IP changes
- ❌ Slower than ADB forwarding

#### Option C: Auto-Detection

```bash
# In mobile app:
# Settings → Development → Konfigurasi Server
# Click "Deteksi Otomatis (Scan)"
# Wait for scan to complete
# Server will be auto-selected if found
```

**How it works**:
1. Try `10.0.2.2` (emulator default)
2. Try `127.0.0.1` (ADB forwarding)
3. Scan subnet for port 8000

---

## Troubleshooting

### Problem: Server Configuration Not Saving

**Symptoms**:
- User selects IP, but it reverts to default
- Subtitle shows wrong IP

**Solution**:
1. Check if `RetrofitClient.init(context)` is called in `MainActivity.onCreate()`
2. Verify SharedPreferences is working: `adb shell pm dump com.example.woosh | grep woosh_network_prefs`
3. Clear app data and try again: `adb shell pm clear com.example.woosh`

### Problem: Server Not Responding

**Symptoms**:
- "Failed to connect to server" error
- API calls timeout

**Solution**:
1. Verify Laravel server is running: `php artisan serve --host=0.0.0.0 --port=8000`
2. Check firewall allows port 8000
3. Verify IP is correct: `ipconfig` (Windows) or `ifconfig` (Mac/Linux)
4. Try ADB port forwarding: `adb reverse tcp:8000 tcp:8000`

### Problem: Cleartext Traffic Error

**Symptoms**:
- "cleartext communication to X.X.X.X not permitted"

**Solution**:
- Already fixed in `network_security_config.xml`
- Verify `cleartextTrafficPermitted="true"` is set
- Rebuild app: `./gradlew clean build`

### Problem: Auto-Detection Not Working

**Symptoms**:
- "Server tidak ditemukan di jaringan lokal"

**Solution**:
1. Verify server is running on port 8000
2. Check if device is on same Wi-Fi network
3. Try manual IP entry instead
4. Use ADB port forwarding as fallback

---

## Configuration Details

### SharedPreferences Keys

```kotlin
PREFS_NAME = "woosh_network_prefs"
KEY_SELECTED_IP = "selected_ip"
KEY_PRESETS = "ip_presets"
```

### Default Presets

```kotlin
DEFAULT_PRESETS = listOf(
    "127.0.0.1",     // ADB Port Forwarding
    "10.0.2.2",      // Emulator default
    "192.168.1.100"  // Example local IP
)
```

### Port Configuration

```kotlin
PORT = "8000"  // Laravel server port
```

---

## API Endpoint Configuration

### How RetrofitClient Works

1. **Interceptor** - Dynamically replaces host/port in all requests
2. **Base URL** - Placeholder URL (overridden by interceptor)
3. **Selected IP** - Stored in SharedPreferences

```kotlin
// Example request flow:
// 1. Original request: http://localhost/api/v1/trips
// 2. Interceptor replaces with: http://10.10.45.41:8000/api/v1/trips
// 3. Request sent to actual server
```

---

## Testing Server Configuration

### Test 1: Verify IP is Saved

```bash
# Check SharedPreferences
adb shell "run-as com.example.woosh cat /data/data/com.example.woosh/shared_prefs/woosh_network_prefs.xml"

# Should show:
# <string name="selected_ip">10.10.45.41</string>
```

### Test 2: Verify API Calls Use Correct IP

```bash
# Monitor network traffic
adb shell tcpdump -i any -n "port 8000" | grep -E "10.10.45.41|127.0.0.1|10.0.2.2"

# Or check mobile logs
adb logcat | grep -E "http://|retrofit"
```

### Test 3: Verify Subtitle Updates

```bash
# In SettingsScreen, subtitle should show:
# - "127.0.0.1" if using ADB forwarding
# - "10.10.45.41" if using direct IP
# - "10.0.2.2" if using emulator default
```

---

## Best Practices

### For Development

1. **Use ADB Port Forwarding** (Recommended)
   ```bash
   adb reverse tcp:8000 tcp:8000
   # Then select 127.0.0.1 in app
   ```

2. **Use Auto-Detection** (If ADB not available)
   ```bash
   # Click "Deteksi Otomatis (Scan)" in app
   # Wait for scan to complete
   ```

3. **Manual IP Entry** (Last resort)
   ```bash
   # Find your IP: ipconfig (Windows) or ifconfig (Mac/Linux)
   # Enter in "Tambah Alamat/URL Kustom" field
   ```

### For Production

1. **Use HTTPS URL**
   ```
   https://api.woosh.com
   # Or
   https://your-domain.com
   ```

2. **Hardcode in BuildConfig**
   ```kotlin
   // Don't allow user to change in production
   if (BuildConfig.DEBUG) {
       // Show configuration dialog
   }
   ```

3. **Use Environment Variables**
   ```kotlin
   val baseUrl = BuildConfig.API_BASE_URL
   ```

---

## Code Examples

### Example 1: Initialize RetrofitClient

```kotlin
// In MainActivity.onCreate()
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize RetrofitClient with saved IP
    RetrofitClient.init(this)
    
    setContent {
        WooshTheme {
            // Your app content
        }
    }
}
```

### Example 2: Make API Call

```kotlin
// In ViewModel
val apiService = RetrofitClient.instance

// API call automatically uses configured IP
val response = apiService.getTrips()
// Actual request: http://10.10.45.41:8000/api/v1/trips
```

### Example 3: Change Server IP

```kotlin
// In SettingsScreen
Button(onClick = {
    RetrofitClient.setSelectedIp(context, "192.168.1.100")
    // IP is saved to SharedPreferences
    // All future API calls use new IP
}) {
    Text("Select Server")
}
```

### Example 4: Get Current Base URL

```kotlin
// In any component
val currentUrl = RetrofitClient.getCurrentBaseUrl()
// Returns: "http://10.10.45.41:8000/"
```

---

## FAQ

### Q: Why does the app show 10.0.2.2 by default?
**A**: Because 10.0.2.2 is the default emulator IP. If you're using a physical device, you should change it to your laptop's IP or use ADB port forwarding.

### Q: Can I use HTTPS?
**A**: Yes! Just enter the full URL (e.g., `https://api.woosh.com`) in the custom IP field.

### Q: Does the IP persist after app restart?
**A**: Yes! The IP is saved in SharedPreferences and restored on app start.

### Q: Can I add multiple IPs?
**A**: Yes! You can add multiple custom IPs and switch between them in the dialog.

### Q: What if I forget the IP?
**A**: Use "Deteksi Otomatis (Scan)" to auto-detect the server on your network.

### Q: Does ADB port forwarding work with Wi-Fi?
**A**: Yes! ADB works with both USB and Wi-Fi debugging. Just run `adb reverse tcp:8000 tcp:8000` before starting the app.

---

## Summary

✅ Server configuration is **fully functional**  
✅ IP is **persisted** in SharedPreferences  
✅ API calls **automatically use** configured IP  
✅ **Multiple options** for configuration (ADB, auto-detect, manual)  
✅ **Production-ready** with HTTPS support  

**Recommended Setup**:
1. Use `adb reverse tcp:8000 tcp:8000` for development
2. Select `127.0.0.1` in app settings
3. All API calls will work without reconfiguration

