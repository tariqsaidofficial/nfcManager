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
import com.dxbmark.nfcmanager.data.database.entities.NFCSettingsEntity
import com.dxbmark.nfcmanager.viewmodel.ActivityViewModel
import com.dxbmark.nfcmanager.viewmodel.MainViewModel
import com.dxbmark.nfcmanager.viewmodel.OnboardingViewModel
import com.dxbmark.nfcmanager.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
// import kotlinx.coroutines.runBlocking // No longer needed
// import java.util.Locale // No longer needed directly here
// import javax.inject.Inject // Not needed for repository here

/**
 * Main activity for NFC Manager application.
 * 
 * This activity serves as the entry point for the application and handles:
 * - NFC tag detection and processing
 * - Splash screen management
 * - Locale configuration and language switching
 * - Navigation between different screens (onboarding, main app)
 * - System bar theming based on dark/light mode
 * 
 * The activity uses Hilt for dependency injection and follows MVVM architecture
 * with ViewModels for state management.
 * 
 * @author NFC Manager Team
 * @since 1.0.0
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Main ViewModel for handling NFC operations and app state */
    private val mainViewModel: MainViewModel by viewModels()
    
    /** Settings ViewModel for managing app preferences and configuration */
    private val settingsViewModel: SettingsViewModel by viewModels()

    /** NFC adapter for handling NFC operations */
    private var nfcAdapter: NfcAdapter? = null
    
    /** Pending intent for NFC foreground dispatch */
    private lateinit var pendingIntent: PendingIntent
    
    /** Intent filters for different NFC actions */
    private lateinit var nfcIntentFilters: Array<IntentFilter>
    
    /** Flag to control splash screen visibility */
    private var isAppReady = false

    companion object {
        /** Tag for logging purposes */
        private const val TAG = "MainActivityFull"
    }

    /**
     * Attaches the base context with proper locale configuration.
     * 
     * This method is called before onCreate() and ensures that the activity
     * uses the correct locale based on user settings. It wraps the base context
     * with LocaleUtils to apply the selected language.
     * 
     * @param newBase The new base context to attach
     */
    override fun attachBaseContext(newBase: Context) {
        // Use LocaleUtils.onAttach to get the localized context
        // This already fetches currentLanguageCode from NfcManagerApplication internally
        Log.d(TAG, "attachBaseContext: Wrapping context with LocaleUtils.onAttach")
        super.attachBaseContext(LocaleUtils.onAttach(newBase))
    }

    /**
     * Called when the activity is starting.
     * 
     * This method handles:
     * - Splash screen installation and configuration
     * - Edge-to-edge display setup
     * - NFC initialization
     * - ViewModels setup and observation
     * - UI content setup with Compose
     * 
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                          being shut down then this Bundle contains the data it most
     *                          recently supplied in onSaveInstanceState(Bundle)
     */
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
                Log.e(TAG, "=== MainActivity Splash Screen Control STARTED ===")
                
                // Wait for database initialization to complete first
                Log.e(TAG, "Waiting for database initialization...")
                var attempts = 0
                while (!com.dxbmark.nfcmanager.NfcManagerApplication.isDatabaseInitialized && attempts < 20) {
                    kotlinx.coroutines.delay(250)
                    attempts++
                    Log.e(TAG, "Waiting for database init... attempt $attempts")
                }
                
                // Wait for ViewModels to be ready with timeout
                Log.e(TAG, "Waiting for ViewModels to be ready...")
                try {
                    withTimeout(5000) { // 5 second timeout
                        settingsViewModel.settings.first() // Wait for settings to load
                        mainViewModel.uiState.first() // Wait for UI state to load
                    }
                    Log.e(TAG, "ViewModels are ready")
                } catch (timeoutException: kotlinx.coroutines.TimeoutCancellationException) {
                    Log.e(TAG, "ViewModels timeout - proceeding anyway")
                }
                
                // Fixed splash duration - 2 seconds as requested
                Log.e(TAG, "Waiting 2 seconds for splash screen...")
                kotlinx.coroutines.delay(2000) // 2 seconds total 
                
                isAppReady = true
                Log.e(TAG, "=== App fully initialized - splash screen ready to dismiss ===")
            } catch (e: Exception) {
                Log.e(TAG, "CRITICAL ERROR during app initialization: ${e.message}", e)
                // Fallback: dismiss splash after max duration
                kotlinx.coroutines.delay(1000)
                isAppReady = true
                Log.e(TAG, "Fallback: splash screen dismissed due to error")
            }
        }
        
        Log.d(TAG, "onCreate: MainActivity (FULL HILT) started successfully!")
    }

    /**
     * Sets up NFC adapter and intent filters for NFC tag detection.
     * 
     * This method initializes:
     * - NFC adapter from the system
     * - Pending intent for foreground dispatch
     * - Intent filters for NDEF and TAG discovered actions
     * 
     * The setup ensures that the app can receive NFC intents when in foreground.
     */
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

    /**
     * Called when the activity will start interacting with the user.
     * 
     * Enables NFC foreground dispatch to ensure this activity receives
     * NFC intents when it's in the foreground, and refreshes the NFC status.
     */
    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, nfcIntentFilters, null)
        mainViewModel.refreshNfcStatus()
        Log.d(TAG, "onResume: Foreground dispatch enabled. NFC status refreshed.")
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     * 
     * Disables NFC foreground dispatch to prevent this activity from receiving
     * NFC intents when it's not in the foreground.
     */
    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
        Log.d(TAG, "onPause: Foreground dispatch disabled.")
    }

    /**
     * Called when the activity receives a new intent.
     * 
     * This method handles NFC tag discovery intents and processes the detected tags.
     * It supports NDEF_DISCOVERED, TAG_DISCOVERED, and TECH_DISCOVERED actions.
     * 
     * @param intent The new intent that was started for the activity
     */
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

    /**
     * Converts a byte array to a hexadecimal string representation.
     * 
     * This utility function is used to convert NFC tag IDs (which are byte arrays)
     * into readable hexadecimal strings for logging and display purposes.
     * 
     * @param bytes The byte array to convert
     * @return A hexadecimal string representation of the byte array
     */
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
        Log.e("NfcManagerApp", "=== NFCManagerApp Composable STARTED ===")
        
        Log.e("NfcManagerApp", "Creating ViewModels...")
        val activityViewModel: ActivityViewModel = hiltViewModel()
        val onboardingViewModel: OnboardingViewModel = hiltViewModel()
        Log.e("NfcManagerApp", "ViewModels created successfully")
        
        // Add null safety and error handling for settings with explicit initial values
        Log.e("NfcManagerApp", "Collecting settings state...")
        val settings by settingsViewModelInstance.settings.collectAsState(
            initial = NFCSettingsEntity(
                id = 1,
                isOnboardingCompleted = false,
                isDarkMode = false,
                selectedLanguageCode = null,
                backgroundServiceMonitoringEnabled = false,
                soundEnabled = true,
                vibrationEnabled = true,
                showNotifications = true
            )
        )
        Log.e("NfcManagerApp", "Settings collected: $settings")
        
        Log.e("NfcManagerApp", "Collecting onboarding state...")
        val isOnboardingCompleted by onboardingViewModel.isOnboardingCompleted.collectAsState(initial = false)
        val isLoading by onboardingViewModel.isLoading.collectAsState(initial = false)
        Log.e("NfcManagerApp", "Onboarding state - completed: $isOnboardingCompleted, loading: $isLoading")
    
        // Safe theme detection with fallback
        val isCurrentlyDarkTheme = settings.isDarkMode
        Log.e("NfcManagerApp", "Theme detected - isDarkMode: $isCurrentlyDarkTheme")

        // Show loading screen during transitions
        if (isLoading) {
            Log.e("NfcManagerApp", "Showing loading screen...")
            com.dxbmark.nfcmanager.ui.screens.LoadingScreen(
                message = "Setting up your experience..."
            )
        } else if (!isOnboardingCompleted) {
            Log.e("NfcManagerApp", "Showing onboarding screen...")
            // Show onboarding for new users
            com.dxbmark.nfcmanager.ui.screens.OnboardingScreen(
                onComplete = {
                    Log.e("NfcManagerApp", "Onboarding completed - calling completeOnboarding()")
                    onboardingViewModel.completeOnboarding()
                }
            )
        } else {
            Log.e("NfcManagerApp", "Showing main app interface...")
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
        
        Log.e("NfcManagerApp", "=== NFCManagerApp Composable COMPLETED ===")
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
