package com.dxbmark.nfcmanager

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
// import android.content.res.Configuration // No longer needed directly here
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
// import com.dxbmark.nfcmanager.data.repository.NFCRepository // Not directly used for locale
import com.dxbmark.nfcmanager.ui.components.NFCManagerNavigation
import com.dxbmark.nfcmanager.ui.theme.NothingOSTheme
import com.dxbmark.nfcmanager.utils.LocaleUtils // <<< IMPORT LOCALE UTILS
import com.dxbmark.nfcmanager.viewmodel.ActivityViewModel
import com.dxbmark.nfcmanager.viewmodel.MainViewModel
import com.dxbmark.nfcmanager.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
// import kotlinx.coroutines.runBlocking // No longer needed
// import java.util.Locale // No longer needed directly here
// import javax.inject.Inject // Not needed for repository here

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var nfcIntentFilters: Array<IntentFilter>
    
    // Splash Screen control
    private var isAppReady = false

    companion object {
        private const val TAG = "MainActivityFull"
    }

    override fun attachBaseContext(newBase: Context) {
        // Use LocaleUtils.onAttach to get the localized context
        // This already fetches currentLanguageCode from NfcManagerApplication internally
        Log.d(TAG, "attachBaseContext: Wrapping context with LocaleUtils.onAttach")
        super.attachBaseContext(LocaleUtils.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen before calling super.onCreate()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: MainActivity (FULL HILT) starting...")

        // Configure splash screen behavior
        splashScreen.setKeepOnScreenCondition {
            !isAppReady
        }

        enableEdgeToEdge()
        setupNFC()

        // Listen for requests to recreate the activity (e.g., after language change)
        lifecycleScope.launch {
            settingsViewModel.recreateActivityFlow.collectLatest {
                Log.d(TAG, "Recreating activity due to settings change (language or reset).")
                recreate()
            }
        }

        setContent {
            NFCManagerApp(mainViewModel, settingsViewModel)
        }
        
        // Smart splash screen control - wait for app initialization
        lifecycleScope.launch {
            try {
                // Wait for ViewModels to be ready
                settingsViewModel.settings.first() // Wait for settings to load
                mainViewModel.uiState.first() // Wait for UI state to load
                
                // Minimum splash duration for better UX and animation
                kotlinx.coroutines.delay(1200) // 1.2 seconds minimum
                
                // Additional delay for smooth animation completion
                kotlinx.coroutines.delay(300) 
                
                isAppReady = true
                Log.d(TAG, "App fully initialized - splash screen ready to dismiss")
            } catch (e: Exception) {
                Log.e(TAG, "Error during app initialization, dismissing splash", e)
                // Fallback: dismiss splash after max duration
                kotlinx.coroutines.delay(2000)
                isAppReady = true
            }
        }
        
        Log.d(TAG, "onCreate: MainActivity (FULL HILT) started successfully!")
    }

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
                // addDataType("*/*") 
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
        mainViewModelInstance: MainViewModel,
        settingsViewModelInstance: SettingsViewModel
    ) {
        val activityViewModel: ActivityViewModel = hiltViewModel()
        val settings by settingsViewModelInstance.settings.collectAsState()
        val isCurrentlyDarkTheme = settings.isDarkMode

        // The Locale is now applied at the Activity and Application level.
        // Jetpack Compose will pick up the correct Locale from the Context implicitly.
        NothingOSTheme(darkTheme = isCurrentlyDarkTheme) {
            ApplySystemBarColors(isDarkTheme = isCurrentlyDarkTheme)
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NFCManagerNavigation(
                    mainViewModel = mainViewModelInstance,
                    activityViewModel = activityViewModel,
                    settingsViewModel = settingsViewModelInstance
                )
            }
        }
    }

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
