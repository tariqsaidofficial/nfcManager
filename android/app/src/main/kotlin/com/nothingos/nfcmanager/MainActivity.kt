package com.nothingos.nfcmanager

import android.app.Activity // Required for context cast
import android.app.PendingIntent // Required for NFC Foreground Dispatch
import android.content.Intent // Required for NFC Intent Handling
import android.content.IntentFilter // Required for NFC Foreground Dispatch
import android.nfc.NfcAdapter // Required for NFC
import android.nfc.Tag // Required for NFC Tag data
import android.os.Build // Required for PendingIntent flags
import android.os.Bundle
import android.util.Log // For temporary logging
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // Import for by viewModels()
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect // Required for ApplySystemBarColors
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView // Required for ApplySystemBarColors
import androidx.core.view.WindowInsetsControllerCompat // Required for ApplySystemBarColors
import androidx.hilt.navigation.compose.hiltViewModel // Import for hiltViewModel()
import com.nothingos.nfcmanager.ui.theme.NothingOSTheme
import com.nothingos.nfcmanager.viewmodel.MainViewModel
import com.nothingos.nfcmanager.viewmodel.ActivityViewModel
import com.nothingos.nfcmanager.viewmodel.SettingsViewModel
import com.nothingos.nfcmanager.ui.components.NFCManagerNavigation
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity following official Android patterns
 * Entry point for the NFC Manager app with Jetpack Compose
 */
@AndroidEntryPoint // Add Hilt Entry Point
class MainActivity : ComponentActivity() {

    // ViewModels injected by Hilt
    private val mainViewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var nfcIntentFilters: Array<IntentFilter>

    companion object {
        private const val TAG = "MainActivityFull"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: MainActivity (FULL HILT) starting...")

        enableEdgeToEdge()
        // setupDatabase() // Removed - Hilt handles repository injection into ViewModels
        setupNFC()

        setContent {
            // Pass Hilt-injected ViewModels to the main composable
            NFCManagerApp(mainViewModel, settingsViewModel)
        }
        Log.d(TAG, "onCreate: MainActivity (FULL HILT) started successfully!")
    }

    // setupDatabase() and repository property removed - Hilt handles this

    private fun setupNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlag)

        val ndefIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                // addDataType("*/*") // Example: filter for specific NDEF records
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                Log.e(TAG, "Failed to add MIME type for NDEF_DISCOVERED filter", e)
                throw RuntimeException("Failed to add MIME type.", e)
            }
        }
        val tagIntentFilter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        nfcIntentFilters = arrayOf(ndefIntentFilter, tagIntentFilter)
        Log.d(TAG, "NFC setup complete. Adapter: ${nfcAdapter != null}")
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, nfcIntentFilters, null)
        mainViewModel.refreshNfcStatus()
        Log.d(TAG, "onResume: Foreground dispatch enabled. NFC status refreshed.")
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
        Log.d(TAG, "onPause: Foreground dispatch disabled.")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: Intent received - Action: ${intent.action}")
        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED -> {
                val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                }
                tag?.let {
                    val tagIdHex = bytesToHexString(it.id)
                    Log.i(TAG, "NFC Tag Scanned: ID - $tagIdHex, Action: ${intent.action}")
                    mainViewModel.logRealNfcTagScan(tagIdHex, intent.action ?: "Unknown Action")
                } ?: Log.w(TAG, "NFC intent received but tag was null.")
            }
            else -> Log.w(TAG, "Received an unknown intent action: ${intent.action}")
        }
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[j * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }

    @Composable
    private fun NFCManagerApp(
        // Application and appRepository removed - Hilt injects them into ViewModels
        mainViewModelInstance: MainViewModel,
        settingsViewModelInstance: SettingsViewModel
    ) {
        // ActivityViewModel is obtained using hiltViewModel() within the composable scope
        val activityViewModel: ActivityViewModel = hiltViewModel()

        val settings by settingsViewModelInstance.settings.collectAsState()
        val isCurrentlyDarkTheme = settings.isDarkMode

        NothingOSTheme(darkTheme = isCurrentlyDarkTheme) {
            ApplySystemBarColors(isDarkTheme = isCurrentlyDarkTheme)

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NFCManagerNavigation(
                    mainViewModel = mainViewModelInstance,
                    activityViewModel = activityViewModel, // Pass Hilt-injected ActivityViewModel
                    settingsViewModel = settingsViewModelInstance
                )
            }
        }
    }

    // ViewModel factories (MainViewModelFactory, SettingsViewModelFactory) removed - Hilt handles this

    @Composable
    private fun ApplySystemBarColors(isDarkTheme: Boolean) {
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as? Activity)?.window
                if (window != null) {
                    val insetsController = WindowInsetsControllerCompat(window, view)
                    insetsController.isAppearanceLightStatusBars = !isDarkTheme
                    insetsController.isAppearanceLightNavigationBars = !isDarkTheme
                } else {
                    Log.w(TAG, "ApplySystemBarColors: Could not get window from context.")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: MainActivity being destroyed.")
    }
}
