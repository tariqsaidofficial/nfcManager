package com.nothingos.nfcmanager

import android.graphics.Color // Required for Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
// import androidx.core.view.WindowCompat // No longer explicitly needed here
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
        
        // Configure edge-to-edge display for a dark-themed app
        // NothingOSTheme defaults to darkTheme = true
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT), // Light icons on transparent background
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT) // Light buttons on transparent background (if API level supports)
        )
        // WindowCompat.setDecorFitsSystemWindows(window, false) // This is implicitly handled by enableEdgeToEdge
        
        // Initialize Database and Repository
        setupDatabase()
        
        setContent {
            NFCManagerApp()
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
    private fun NFCManagerApp() {
        NothingOSTheme { // Defaults to darkTheme = true
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background // Should be PureBlack in dark theme
            ) {
                // Create ViewModels with repository
                val mainViewModel: MainViewModel = viewModel {
                    MainViewModel(repository)
                }
                val activityViewModel: ActivityViewModel = viewModel {
                    ActivityViewModel(repository)
                }
                val settingsViewModel: SettingsViewModel = viewModel {
                    SettingsViewModel(repository)
                }
                
                // Main app navigation will go here
                NFCManagerNavigation(
                    mainViewModel = mainViewModel,
                    activityViewModel = activityViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up database connection if needed
        if (::database.isInitialized) {
            // Database will be cleaned up automatically by Room
        }
    }
}

/**
 * Import Navigation Component
 */
