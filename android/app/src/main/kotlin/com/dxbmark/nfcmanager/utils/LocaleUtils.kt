package com.dxbmark.nfcmanager.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.dxbmark.nfcmanager.NfcManagerApplication
import java.util.Locale

object LocaleUtils {

    fun onAttach(context: Context): Context {
        val languageCode = NfcManagerApplication.currentLanguageCode
        return getLocalizedContext(context, languageCode)
    }

    fun getLocalizedContext(context: Context, languageCode: String?): Context {
        val localeToSet: Locale = if (languageCode != null && languageCode.isNotEmpty()) {
            Locale(languageCode)
        } else {
            Resources.getSystem().configuration.locales.get(0) ?: Locale.getDefault()
        }

        Locale.setDefault(localeToSet)

        val originalConfiguration = context.resources.configuration
        val newConfiguration = Configuration(originalConfiguration)

        newConfiguration.setLocale(localeToSet)
        newConfiguration.setLayoutDirection(localeToSet)

        // لا يوجد أي تعديل على newConfiguration.uiMode هنا
        
        return context.createConfigurationContext(newConfiguration)
    }

    fun updateApplicationContext(applicationContext: Context, languageCode: String?): Context {
        val localeToSet: Locale = if (languageCode != null && languageCode.isNotEmpty()) {
            Locale(languageCode)
        } else {
            Resources.getSystem().configuration.locales.get(0) ?: Locale.getDefault()
        }
        Locale.setDefault(localeToSet)

        val appResources: Resources = applicationContext.resources
        val originalConfiguration = appResources.configuration
        val newConfiguration = Configuration(originalConfiguration)

        newConfiguration.setLocale(localeToSet)
        newConfiguration.setLayoutDirection(localeToSet)

        // لا يوجد أي تعديل على newConfiguration.uiMode هنا أيضاً
        
        @Suppress("DEPRECATION")
        appResources.updateConfiguration(newConfiguration, appResources.displayMetrics)
        
        return applicationContext
    }
}
