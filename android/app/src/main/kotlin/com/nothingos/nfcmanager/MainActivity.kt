package com.nothingos.nfcmanager

import android.app.Activity // Required for context cast
import android.os.Bundle
import androidx.activity.ComponentActivity
// import androidx.activity.SystemBarStyle // No longer directly used in onCreate for dynamic changes
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
import androidx.compose.ui.platform.LocalView // Required for ApplySystemBarColors
import androidx.core.view.WindowInsetsControllerCompat // Required for ApplySystemBarColors
import androidx.lifecycle.viewmodel.compose.viewModel
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display. System bar icon colors will be handled dynamically.
        enableEdgeToEdge()
        
        // Initialize Database and Repository
        setupDatabase()
        
        setContent {
            // Pass the repository to NFCManagerApp so it can be provided to ViewModels
            NFCManagerApp(repository)
        }
    }
    
    /**
     * Initialize Room Database and Repository
     */
    private fun setupDatabase() {
        database = AppDatabase.getDatabase(this)
        repository = NFCRepository(
            nfcEventDao = database.nfcEventDao(),
            nfcSettingsDao = database.nfcSettingsDao()
        )
    }
    
    /**
     * Main Compose App
     */
    @Composable
    private fun NFCManagerApp(appRepository: NFCRepository) { // Accept repository
        // Obtain SettingsViewModel to access theme settings
        val settingsViewModel: SettingsViewModel = viewModel {
            SettingsViewModel(appRepository)
        }
        val settings by settingsViewModel.settings.collectAsState()
        val isCurrentlyDarkTheme = settings.isDarkMode

        NothingOSTheme(darkTheme = isCurrentlyDarkTheme) {
            // Apply dynamic system bar colors (icon appearance)
            ApplySystemBarColors(isDarkTheme = isCurrentlyDarkTheme)

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // Create other ViewModels, passing the same repository
                val mainViewModel: MainViewModel = viewModel {
                    MainViewModel(appRepository)
                }
                val activityViewModel: ActivityViewModel = viewModel {
                    ActivityViewModel(appRepository)
                }
                
                NFCManagerNavigation(
                    mainViewModel = mainViewModel,
                    activityViewModel = activityViewModel,
                    settingsViewModel = settingsViewModel // Pass the already created SettingsViewModel
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::database.isInitialized) {
            // Database cleanup is handled by Room
        }
    }
}

/**
 * Composable to dynamically set system bar icon colors based on the current theme.
 */
@Composable
private fun ApplySystemBarColors(isDarkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowInsetsControllerCompat(window, view)
            // Set status bar icons to dark if light theme, light if dark theme
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            // Set navigation bar icons to dark if light theme, light if dark theme
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
}
