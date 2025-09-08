package com.dxbmark.nfcmanager.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.dxbmark.nfcmanager.NfcManagerApplication // To access currentLanguageCode
import java.util.Locale

object LocaleUtils {

    /**
     * Should be called in Activity's attachBaseContext method.
     * It wraps the base context with a new configuration based on the selected language.
     */
    fun onAttach(context: Context): Context {
        // Get the globally stored language code from NfcManagerApplication
        val languageCode = NfcManagerApplication.currentLanguageCode
        return getLocalizedContext(context, languageCode)
    }

    fun getLocalizedContext(context: Context, languageCode: String?): Context {
        val localeToSet: Locale = if (languageCode != null && languageCode.isNotEmpty()) {
            Locale(languageCode) // Use selected language
        } else {
            // Fallback to system's default locale if no language is selected
            // or on initial start before NfcManagerApplication.currentLanguageCode is loaded.
            Resources.getSystem().configuration.locales.get(0) ?: Locale.getDefault()
        }

        Locale.setDefault(localeToSet) // Set this locale as default for the JVM

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(localeToSet)
        configuration.setLayoutDirection(localeToSet) // Crucial for RTL support

        return context.createConfigurationContext(configuration)
    }

    /**
     * Helper function to be called from NfcManagerApplication when the language choice is finalized
     * (either loaded from settings or changed by the user).
     * This ensures the application's own base context is updated.
     */
    fun updateApplicationContext(applicationContext: Context, languageCode: String?): Context {
        val localeToSet: Locale = if (languageCode != null && languageCode.isNotEmpty()) {
            Locale(languageCode)
        } else {
            Resources.getSystem().configuration.locales.get(0) ?: Locale.getDefault()
        }
        Locale.setDefault(localeToSet)

        val appResources: Resources = applicationContext.resources
        val appConfig = Configuration(appResources.configuration)
        appConfig.setLocale(localeToSet)
        appConfig.setLayoutDirection(localeToSet)

        @Suppress("DEPRECATION") // For older API levels
        appResources.updateConfiguration(appConfig, appResources.displayMetrics)
        
        // For newer way to ensure context is updated, though createConfigurationContext is more for Activities
        // return applicationContext.createConfigurationContext(appConfig) // Not typically done for application base context directly this way.
        // The updateConfiguration should suffice for the application's own resource loading.
        return applicationContext // Return original context as it's modified in place for resources
    }
}
