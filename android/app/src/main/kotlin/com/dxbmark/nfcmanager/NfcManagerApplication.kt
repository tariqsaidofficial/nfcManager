package com.dxbmark.nfcmanager

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.dxbmark.nfcmanager.data.repository.NFCRepository
import com.dxbmark.nfcmanager.services.NfcMonitoringService
import com.dxbmark.nfcmanager.utils.LocaleUtils
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

        @Volatile
        var isDatabaseInitialized = false
            private set

        fun setDatabaseInitialized() {
            isDatabaseInitialized = true
        }

        fun updateLanguageCode(context: Context, newLanguageCode: String?) {
            Log.d(
                TAG,
                "updateLanguageCode: Updating currentLanguageCode to $newLanguageCode and applying to context"
            )
            currentLanguageCode = newLanguageCode
            LocaleUtils.updateApplicationContext(context.applicationContext, newLanguageCode)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application starting")

        try {
            if (!::repository.isInitialized) {
                Log.e(TAG, "CRITICAL: Repository not initialized by Hilt in Application.onCreate!")
                return 
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking repository initialization", e)
            return
        }

        applicationScope.launch {
            Log.e(TAG, "=== NfcManagerApplication.onCreate() COROUTINE STARTED ===")
            Log.d(
                TAG,
                "onCreate: Coroutine for settings init, locale Caching, service start, and applying locale."
            )
            try {
                Log.e(TAG, "Initializing database settings...")
                repository.initializeSettingsIfNeeded()
                Log.e(TAG, "Database settings initialization completed successfully")
                setDatabaseInitialized()
                Log.d(TAG, "onCreate: initializeSettingsIfNeeded completed.")

                val settings = repository.getSettings().first()
                currentLanguageCode = settings.selectedLanguageCode
                Log.d(TAG, "onCreate: Cached language code from repository: $currentLanguageCode")

                LocaleUtils.updateApplicationContext(
                    this@NfcManagerApplication,
                    currentLanguageCode
                )
                Log.d(TAG, "onCreate: Applied cached language to application context.")

                if (settings.backgroundServiceMonitoringEnabled) {
                    Log.d(
                        TAG,
                        "onCreate: Background service monitoring is enabled, attempting to start service..."
                    )
                    val intent =
                        Intent(applicationContext, NfcMonitoringService::class.java).apply {
                            action = NfcMonitoringService.ACTION_START_MONITORING
                        }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        applicationContext.startForegroundService(intent)
                        Log.d(
                            TAG,
                            "onCreate: startForegroundService called for NfcMonitoringService."
                        )
                    } else {
                        applicationContext.startService(intent)
                        Log.d(TAG, "onCreate: startService called for NfcMonitoringService.")
                    }
                } else {
                    Log.d(
                        TAG,
                        "onCreate: Background service monitoring is disabled, service not started."
                    )
                }
                Log.d(TAG, "onCreate: Coroutine finished successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "CRITICAL EXCEPTION IN APPLICATION COROUTINE: ${e.message}", e)

                if (e.message?.contains("No space left on device") == true ||
                    e.message?.contains("ENOSPC") == true
                ) {
                    Log.e(TAG, "STORAGE SPACE ERROR: Device is out of storage space!")
                } else if (e.message?.contains("database") == true ||
                    e.message?.contains("Room") == true
                ) {
                    Log.e(TAG, "DATABASE ERROR: Failed to initialize database properly")
                }

                currentLanguageCode = null
                setDatabaseInitialized()
                try {
                    LocaleUtils.updateApplicationContext(this@NfcManagerApplication, null)
                } catch (localeError: Exception) {
                    Log.e(TAG, "Failed to set default locale", localeError)
                }
            }
        }
        Log.d(TAG, "onCreate: Application onCreate method finished execution.")
    }
}
