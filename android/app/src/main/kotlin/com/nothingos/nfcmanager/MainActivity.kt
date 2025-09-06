package com.nothingos.nfcmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nothingos.nfcmanager.data.database.AppDatabase
import com.nothingos.nfcmanager.data.repository.NFCRepository
import com.nothingos.nfcmanager.ui.theme.NFCManagerTheme
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
        
        // Enable edge-to-edge display (Android 15+ style)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
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
        NFCManagerTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
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
