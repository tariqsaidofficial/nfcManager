package com.nothingos.nfcmanager

import android.app.Activity // Required for context cast
import android.app.Application // Required for ViewModel instantiation
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect // Required for ApplySystemBarColors
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Required for getting current Activity
import androidx.compose.ui.platform.LocalView // Required for ApplySystemBarColors
import androidx.core.view.WindowInsetsControllerCompat // Required for ApplySystemBarColors
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel // Still useful for ActivityViewModel
import com.nothingos.nfcmanager.data.database.AppDatabase
import com.nothingos.nfcmanager.data.repository.NFCRepository
import com.nothingos.nfcmanager.ui.theme.NothingOSTheme
import com.nothingos.nfcmanager.viewmodel.MainViewModel
import com.nothingos.nfcmanager.viewmodel.ActivityViewModel
import com.nothingos.nfcmanager.viewmodel.SettingsViewModel
import com.nothingos.nfcmanager.ui.components.NFCManagerNavigation

/**
 * Main Activity following official Android patterns
 * Entry point for the NFC Manager app with Jetpack Compose
 */
class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: NFCRepository
    private lateinit var mainViewModel: MainViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var nfcIntentFilters: Array<IntentFilter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setupDatabase()
        setupNFC()

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(application, repository)
        ).get(MainViewModel::class.java)

        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(application, repository)
        ).get(SettingsViewModel::class.java)

        setContent {
            NFCManagerApp(application, repository, mainViewModel, settingsViewModel)
        }
    }

    private fun setupDatabase() {
        database = AppDatabase.getDatabase(this)
        repository = NFCRepository(
            nfcEventDao = database.nfcEventDao(),
            nfcSettingsDao = database.nfcSettingsDao()
        )
    }

    private fun setupNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Create a PendingIntent to handle NFC intents when the app is in the foreground
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlag)

        // Define intent filters for NFC tag discovery
        val ndefIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                // Add MIME type if you want to filter for specific NDEF records, e.g., "text/plain"
                // addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Failed to add MIME type.", e)
            }
        }
        val tagIntentFilter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        // You can also add IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        nfcIntentFilters = arrayOf(ndefIntentFilter, tagIntentFilter)
    }

    override fun onResume() {
        super.onResume()
        // Enable foreground dispatch for NFC intents
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, nfcIntentFilters, null)
        // Refresh NFC status when app resumes
        mainViewModel.refreshNfcStatus() // Corrected method call
    }

    override fun onPause() {
        super.onPause()
        // Disable foreground dispatch when the app is paused
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("NFCManager", "New Intent: $intent, Action: ${intent.action}")
        // Process the NFC intent
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
                    Log.i("NFCManager", "NFC Tag Scanned: ID - $tagIdHex, Action: ${intent.action}")
                    mainViewModel.logRealNfcTagScan(tagIdHex, intent.action ?: "Unknown Action")
                    // We will enhance this logging in MainViewModel later
                }
            }
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
        app: Application,
        appRepository: NFCRepository,
        mainViewModelInstance: MainViewModel,
        settingsViewModelInstance: SettingsViewModel
    ) {
        val settings by settingsViewModelInstance.settings.collectAsState()
        val isCurrentlyDarkTheme = settings.isDarkMode

        NothingOSTheme(darkTheme = isCurrentlyDarkTheme) {
            ApplySystemBarColors(isDarkTheme = isCurrentlyDarkTheme)

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val activityViewModel: ActivityViewModel = viewModel {
                    ActivityViewModel(appRepository)
                }

                NFCManagerNavigation(
                    mainViewModel = mainViewModelInstance,
                    activityViewModel = activityViewModel,
                    settingsViewModel = settingsViewModelInstance
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val repository: NFCRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class SettingsViewModelFactory(
    private val application: Application,
    private val repository: NFCRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
private fun ApplySystemBarColors(isDarkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowInsetsControllerCompat(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
}
