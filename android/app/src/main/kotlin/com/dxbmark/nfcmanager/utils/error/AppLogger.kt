package com.dxbmark.nfcmanager.utils.error

import android.util.Log

/**
 * Centralized logging utility for the application
 * Provides consistent logging with automatic tag generation
 * Logs are disabled in release builds for security
 */
object AppLogger {
    
    private const val TAG_PREFIX = "NFCManager"
    private const val MAX_TAG_LENGTH = 23
    
    /**
     * Log levels
     */
    enum class Level {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }
    
    /**
     * Check if logging is enabled (always enabled for now)
     * TODO: Disable in release builds using BuildConfig.DEBUG
     */
    private val isLoggingEnabled: Boolean
        get() = true // Always enabled for debugging
    
    /**
     * Generate tag from class name
     */
    private fun getTag(tag: String?): String {
        val finalTag = tag ?: "App"
        val fullTag = "$TAG_PREFIX:$finalTag"
        return if (fullTag.length > MAX_TAG_LENGTH) {
            fullTag.substring(0, MAX_TAG_LENGTH)
        } else {
            fullTag
        }
    }
    
    /**
     * Log verbose message
     */
    fun v(tag: String? = null, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            val finalTag = getTag(tag)
            if (throwable != null) {
                Log.v(finalTag, message, throwable)
            } else {
                Log.v(finalTag, message)
            }
        }
    }
    
    /**
     * Log debug message
     */
    fun d(tag: String? = null, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            val finalTag = getTag(tag)
            if (throwable != null) {
                Log.d(finalTag, message, throwable)
            } else {
                Log.d(finalTag, message)
            }
        }
    }
    
    /**
     * Log info message
     */
    fun i(tag: String? = null, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            val finalTag = getTag(tag)
            if (throwable != null) {
                Log.i(finalTag, message, throwable)
            } else {
                Log.i(finalTag, message)
            }
        }
    }
    
    /**
     * Log warning message
     */
    fun w(tag: String? = null, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            val finalTag = getTag(tag)
            if (throwable != null) {
                Log.w(finalTag, message, throwable)
            } else {
                Log.w(finalTag, message)
            }
        }
    }
    
    /**
     * Log error message
     */
    fun e(tag: String? = null, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            val finalTag = getTag(tag)
            if (throwable != null) {
                Log.e(finalTag, message, throwable)
            } else {
                Log.e(finalTag, message)
            }
        }
    }
    
    /**
     * Log with custom level
     */
    fun log(level: Level, tag: String? = null, message: String, throwable: Throwable? = null) {
        when (level) {
            Level.VERBOSE -> v(tag, message, throwable)
            Level.DEBUG -> d(tag, message, throwable)
            Level.INFO -> i(tag, message, throwable)
            Level.WARNING -> w(tag, message, throwable)
            Level.ERROR -> e(tag, message, throwable)
        }
    }
    
    /**
     * Log NFC event
     */
    fun nfc(message: String, throwable: Throwable? = null) {
        d("NFC", message, throwable)
    }
    
    /**
     * Log database event
     */
    fun database(message: String, throwable: Throwable? = null) {
        d("Database", message, throwable)
    }
    
    /**
     * Log network event
     */
    fun network(message: String, throwable: Throwable? = null) {
        d("Network", message, throwable)
    }
    
    /**
     * Log service event
     */
    fun service(message: String, throwable: Throwable? = null) {
        d("Service", message, throwable)
    }
    
    /**
     * Log UI event
     */
    fun ui(message: String, throwable: Throwable? = null) {
        d("UI", message, throwable)
    }
    
    /**
     * Log ViewModel event
     */
    fun viewModel(tag: String, message: String, throwable: Throwable? = null) {
        d("VM:$tag", message, throwable)
    }
}

/**
 * Extension function for easy logging from any class
 */
inline fun <reified T> T.logDebug(message: String, throwable: Throwable? = null) {
    AppLogger.d(T::class.java.simpleName, message, throwable)
}

inline fun <reified T> T.logInfo(message: String, throwable: Throwable? = null) {
    AppLogger.i(T::class.java.simpleName, message, throwable)
}

inline fun <reified T> T.logWarning(message: String, throwable: Throwable? = null) {
    AppLogger.w(T::class.java.simpleName, message, throwable)
}

inline fun <reified T> T.logError(message: String, throwable: Throwable? = null) {
    AppLogger.e(T::class.java.simpleName, message, throwable)
}
