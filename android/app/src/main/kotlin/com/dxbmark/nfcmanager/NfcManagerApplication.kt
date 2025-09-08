package com.dxbmark.nfcmanager

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.dxbmark.nfcmanager.data.repository.NFCRepository
import com.dxbmark.nfcmanager.services.NfcMonitoringService
import com.dxbmark.nfcmanager.utils.LocaleUtils // <<< IMPORT LocaleUtils
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

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "NfcManagerApplication"
        var currentLanguageCode: String? = null
            private set

        /**
         * Updates the current language code for the application and applies it to the context.
         * This should be called when the user changes the language in settings.
         */
        fun updateLanguageCode(context: Context, newLanguageCode: String?) {
            Log.d(TAG, "updateLanguageCode: Updating currentLanguageCode to $newLanguageCode and applying to context")
            currentLanguageCode = newLanguageCode
            // Update the application's own context/resources immediately
            LocaleUtils.updateApplicationContext(context.applicationContext, newLanguageCode)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application starting")

        applicationScope.launch { 
            Log.d(TAG, "onCreate: Coroutine for settings init, locale Caching, service start, and applying locale.")
            try {
                repository.initializeSettingsIfNeeded()
                Log.d(TAG, "onCreate: initializeSettingsIfNeeded completed.")

                val settings = repository.getSettings().first() 
                currentLanguageCode = settings.selectedLanguageCode
                Log.d(TAG, "onCreate: Cached language code from repository: $currentLanguageCode")

                // Apply the loaded language to the Application's context
                // This ensures the application resources are up-to-date with the persisted language setting on startup
                LocaleUtils.updateApplicationContext(this@NfcManagerApplication, currentLanguageCode)
                Log.d(TAG, "onCreate: Applied cached language to application context.")

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
