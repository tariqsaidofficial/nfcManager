package com.dxbmark.nfcmanager

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import com.dxbmark.nfcmanager.data.repository.NFCRepository
import com.dxbmark.nfcmanager.services.NfcMonitoringService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NfcManagerApplication : Application() {

    @Inject
    lateinit var repository: NFCRepository

    // Changed to IO dispatcher for direct repository access during init
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "NfcManagerApplication"
        var currentLanguageCode: String? = null
            private set // Make setter private to control updates from within this class

        /**
         * Updates the current language code for the application.
         * This should be called when the user changes the language in settings.
         */
        fun updateLanguageCode(newLanguageCode: String?) {
            Log.d(TAG, "updateLanguageCode: Updating currentLanguageCode to $newLanguageCode")
            currentLanguageCode = newLanguageCode
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application starting")

        applicationScope.launch { // Coroutine now runs on IO dispatcher
            Log.d(TAG, "onCreate: Coroutine for settings init, locale Caching, and service start.")
            try {
                // Initialize settings if needed
                repository.initializeSettingsIfNeeded()
                Log.d(TAG, "onCreate: initializeSettingsIfNeeded completed.")

                // Check current settings and cache the language code
                val settings = repository.getSettings().first() // Synchronous for this single value in IO scope
                currentLanguageCode = settings.selectedLanguageCode
                Log.d(TAG, "onCreate: Cached language code: $currentLanguageCode")

                // Background service starting logic (remains the same)
                if (settings.backgroundServiceMonitoringEnabled) {
                    Log.d(TAG, "onCreate: Background service monitoring is enabled, attempting to start service...")
                    val intent = Intent(applicationContext, NfcMonitoringService::class.java).apply {
                        action = NfcMonitoringService.ACTION_START_MONITORING
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        applicationContext.startForegroundService(intent)
                        Log.d(TAG, "onCreate: startForegroundService called for NfcMonitoringService.")
                    } else {
                        applicationContext.startService(intent)
                        Log.d(TAG, "onCreate: startService called for NfcMonitoringService.")
                    }
                } else {
                    Log.d(TAG, "onCreate: Background service monitoring is disabled, service not started.")
                }
                Log.d(TAG, "onCreate: Coroutine finished successfully.")
            } catch (e: Exception) {
                // Log critical exceptions during application startup
                Log.e(TAG, "onCreate: CRITICAL EXCEPTION IN APPLICATION COROUTINE", e)
            }
        }
        Log.d(TAG, "onCreate: Application onCreate method finished execution.")
    }
}
