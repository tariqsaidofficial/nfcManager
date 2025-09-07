package com.nothingos.nfcmanager

import android.app.Application
import android.content.Intent // Added import for Intent
import android.os.Build // Added import for Build
import android.util.Log
import com.nothingos.nfcmanager.data.repository.NFCRepository
import com.nothingos.nfcmanager.services.NfcMonitoringService // For ACTION_START_MONITORING
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

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "NfcManagerApplication" // Tag for logging
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application starting")

        applicationScope.launch(Dispatchers.IO) {
            Log.d(TAG, "onCreate: Coroutine for settings init and service start.")
            try {
                // Initialize settings if needed
                repository.initializeSettingsIfNeeded()
                Log.d(TAG, "onCreate: initializeSettingsIfNeeded completed.")

                // Check current settings
                val settings = repository.getSettings().first()
                Log.d(TAG, "onCreate: Background service monitoring enabled: ${settings.backgroundServiceMonitoringEnabled}")

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
                Log.e(TAG, "onCreate: CRITICAL EXCEPTION IN APPLICATION COROUTINE", e)
            }
        }
        Log.d(TAG, "onCreate: Application onCreate method finished execution.")
    }
}
