package com.dxbmark.nfcmanager.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings

/**
 * Utility object for NFC related checks and actions.
 */
object NFCUtils {

    /**
     * Checks if the device has an NFC adapter.
     *
     * @param context The application context.
     * @return True if an NFC adapter exists, false otherwise.
     */
    fun hasNfcAdapter(context: Context): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return nfcAdapter != null
    }

    /**
     * Checks if NFC is currently enabled on the device.
     *
     * @param context The application context.
     * @return True if NFC is enabled, false otherwise. Returns false if no NFC adapter exists.
     */
    fun isNfcEnabled(context: Context): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return nfcAdapter?.isEnabled == true
    }

    /**
     * Opens the system NFC settings screen.
     *
     * @param activity The current activity from which to start the settings intent.
     */
    fun openNfcSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
        activity.startActivity(intent)
    }

    /**
     * Opens the wireless settings screen as an alternative if ACTION_NFC_SETTINGS is not available.
     * Some devices might not have a dedicated NFC settings entry point.
     *
     * @param activity The current activity from which to start the settings intent.
     */
    fun openWirelessSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        activity.startActivity(intent)
    }

}
