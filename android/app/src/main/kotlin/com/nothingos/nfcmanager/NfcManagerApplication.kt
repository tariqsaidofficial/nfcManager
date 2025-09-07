package com.nothingos.nfcmanager

import android.app.Application
import android.util.Log // Import Log
import com.nothingos.nfcmanager.data.repository.NFCRepository
import com.nothingos.nfcmanager.services.NfcMonitoringService
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
        Log.d(TAG, "onCreate: Application starting (HILT RESTORED)")

        applicationScope.launch(Dispatchers.IO) {
            Log.d(TAG, "onCreate: Coroutine started for settings init and service start.")
            try {
                Log.d(TAG, "onCreate: Checking repository access (will throw if not injected properly)...")
                val currentSettingsForCheck = repository.getSettings().first() // Accessing repository
                Log.d(TAG, "onCreate: Repository access check successful. Current reminder interval: ${currentSettingsForCheck.reminderInterval}")

                Log.d(TAG, "onCreate: Calling initializeSettingsIfNeeded...")
                repository.initializeSettingsIfNeeded()
                Log.d(TAG, "onCreate: initializeSettingsIfNeeded completed.")

                Log.d(TAG, "onCreate: Calling getSettings().first() for service check...")
                val settings = repository.getSettings().first() // Re-fetch or use currentSettingsForCheck
                Log.d(TAG, "onCreate: getSettings().first() completed. backgroundServiceMonitoringEnabled = ${settings.backgroundServiceMonitoringEnabled}")

                if (settings.backgroundServiceMonitoringEnabled) {
                    Log.d(TAG, "onCreate: Background service monitoring is enabled, starting service...")
                    NfcMonitoringService.startService(applicationContext)
                    Log.d(TAG, "onCreate: NfcMonitoringService.startService called.")
                } else {
                    Log.d(TAG, "onCreate: Background service monitoring is disabled.")
                }
                Log.d(TAG, "onCreate: Coroutine finished successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "onCreate: CRITICAL EXCEPTION IN APPLICATION COROUTINE", e)
            }
        }
        Log.d(TAG, "onCreate: Application onCreate method finished execution (HILT RESTORED).")
    }
}
