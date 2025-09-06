package com.nothingos.nfcmanager

import android.app.Activity // Required for context cast
import android.app.Application // Required for ViewModel instantiation
import android.os.Bundle
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
    private lateinit var settingsViewModel: SettingsViewModel // Hoist for factory instantiation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setupDatabase()

        // Create ViewModels that require Application context using factories
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(application, repository)
        ).get(MainViewModel::class.java)

        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(application, repository)
        ).get(SettingsViewModel::class.java)
        
        setContent {
            // Pass application, repository, and pre-created ViewModels
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
    
    /**
     * Main Compose App
     */
    @Composable
    private fun NFCManagerApp(
        app: Application, // Pass application
        appRepository: NFCRepository, 
        mainViewModelInstance: MainViewModel, // Pass the MainViewModel instance
        settingsViewModelInstance: SettingsViewModel // Pass the SettingsViewModel instance
    ) {
        val settings by settingsViewModelInstance.settings.collectAsState()
        val isCurrentlyDarkTheme = settings.isDarkMode

        NothingOSTheme(darkTheme = isCurrentlyDarkTheme) {
            ApplySystemBarColors(isDarkTheme = isCurrentlyDarkTheme)

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // ActivityViewModel doesn't require Application context in its constructor directly
                val activityViewModel: ActivityViewModel = viewModel {
                    ActivityViewModel(appRepository)
                }
                
                NFCManagerNavigation(
                    mainViewModel = mainViewModelInstance,
                    activityViewModel = activityViewModel,
                    settingsViewModel = settingsViewModelInstance // Use the passed instance
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
}

/**
 * Factory for creating MainViewModel with Application and Repository.
 */
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

/**
 * Factory for creating SettingsViewModel with Application and Repository.
 */
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
