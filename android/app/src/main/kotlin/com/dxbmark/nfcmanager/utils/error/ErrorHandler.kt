package com.dxbmark.nfcmanager.utils.error

import android.content.Context
import android.util.Log
import com.dxbmark.nfcmanager.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central error handler for the application
 * Handles error logging, mapping, and user-friendly message generation
 */
@Singleton
class ErrorHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "ErrorHandler"
    }
    
    /**
     * Handle an error and return a user-friendly message
     * 
     * @param error The AppError to handle
     * @param logError Whether to log the error (default: true)
     * @return User-friendly error message
     */
    fun handle(error: AppError, logError: Boolean = true): String {
        if (logError) {
            logError(error)
        }
        return getUserMessage(error)
    }
    
    /**
     * Handle a throwable and convert it to AppError
     * 
     * @param throwable The throwable to handle
     * @param logError Whether to log the error (default: true)
     * @return User-friendly error message
     */
    fun handle(throwable: Throwable, logError: Boolean = true): String {
        val appError = throwable.toAppError()
        return handle(appError, logError)
    }
    
    /**
     * Log an error with appropriate level
     */
    private fun logError(error: AppError) {
        when (error) {
            is AppError.NetworkError -> Log.w(TAG, "Network Error: ${error.message}", error.cause)
            is AppError.DatabaseError -> Log.e(TAG, "Database Error: ${error.message}", error.cause)
            is AppError.NfcError -> Log.w(TAG, "NFC Error: ${error.message}", error.cause)
            is AppError.PermissionError -> Log.w(TAG, "Permission Error: ${error.message}", error.cause)
            is AppError.ServiceError -> Log.e(TAG, "Service Error: ${error.message}", error.cause)
            is AppError.ValidationError -> Log.d(TAG, "Validation Error: ${error.message}", error.cause)
            is AppError.FileError -> Log.w(TAG, "File Error: ${error.message}", error.cause)
            is AppError.Unknown -> Log.e(TAG, "Unknown Error: ${error.message}", error.cause)
        }
    }
    
    /**
     * Get user-friendly error message based on error type
     */
    private fun getUserMessage(error: AppError): String {
        return when (error) {
            // Network Errors
            is AppError.NetworkError.NoConnection -> 
                context.getString(R.string.error_no_internet)
            is AppError.NetworkError.Timeout -> 
                context.getString(R.string.error_timeout)
            is AppError.NetworkError.ServerError -> 
                context.getString(R.string.error_server, error.code)
            is AppError.NetworkError.Unknown -> 
                context.getString(R.string.error_network_unknown)
            
            // Database Errors
            is AppError.DatabaseError.ReadError -> 
                context.getString(R.string.error_database_read)
            is AppError.DatabaseError.WriteError -> 
                context.getString(R.string.error_database_write)
            is AppError.DatabaseError.DeleteError -> 
                context.getString(R.string.error_database_delete)
            is AppError.DatabaseError.CorruptedData -> 
                context.getString(R.string.error_database_corrupted)
            is AppError.DatabaseError.Unknown -> 
                context.getString(R.string.error_database_unknown)
            
            // NFC Errors
            is AppError.NfcError.NotSupported -> 
                context.getString(R.string.nfc_not_supported_error)
            is AppError.NfcError.Disabled -> 
                context.getString(R.string.nfc_disabled_error_turn_on)
            is AppError.NfcError.PermissionDenied -> 
                context.getString(R.string.nfc_permission_required)
            is AppError.NfcError.ReadError -> 
                context.getString(R.string.error_nfc_read)
            is AppError.NfcError.WriteError -> 
                context.getString(R.string.error_nfc_write)
            is AppError.NfcError.TagLost -> 
                context.getString(R.string.error_nfc_tag_lost)
            is AppError.NfcError.Unknown -> 
                context.getString(R.string.error_nfc_unknown)
            
            // Permission Errors
            is AppError.PermissionError.NotificationPermissionDenied -> 
                context.getString(R.string.error_permission_notification)
            is AppError.PermissionError.NfcPermissionDenied -> 
                context.getString(R.string.nfc_permission_required)
            is AppError.PermissionError.Unknown -> 
                context.getString(R.string.error_permission_unknown, error.permission)
            
            // Service Errors
            is AppError.ServiceError.StartFailed -> 
                context.getString(R.string.error_service_start)
            is AppError.ServiceError.StopFailed -> 
                context.getString(R.string.error_service_stop)
            is AppError.ServiceError.NotRunning -> 
                context.getString(R.string.error_service_not_running)
            is AppError.ServiceError.Unknown -> 
                context.getString(R.string.error_service_unknown)
            
            // Validation Errors
            is AppError.ValidationError.InvalidInput -> 
                context.getString(R.string.error_validation_invalid, error.field)
            is AppError.ValidationError.OutOfRange -> 
                context.getString(R.string.error_validation_range, error.field, error.min, error.max)
            is AppError.ValidationError.Required -> 
                context.getString(R.string.error_validation_required, error.field)
            is AppError.ValidationError.Unknown -> 
                error.message
            
            // File Errors
            is AppError.FileError.NotFound -> 
                context.getString(R.string.error_file_not_found, error.fileName)
            is AppError.FileError.ReadError -> 
                context.getString(R.string.error_file_read)
            is AppError.FileError.WriteError -> 
                context.getString(R.string.error_file_write)
            is AppError.FileError.PermissionDenied -> 
                context.getString(R.string.error_file_permission)
            is AppError.FileError.Unknown -> 
                context.getString(R.string.error_file_unknown)
            
            // Unknown Error
            is AppError.Unknown -> 
                error.message
        }
    }
    
    /**
     * Check if error is recoverable (user can retry)
     */
    fun isRecoverable(error: AppError): Boolean {
        return when (error) {
            is AppError.NetworkError -> true
            is AppError.DatabaseError.ReadError -> true
            is AppError.NfcError.ReadError -> true
            is AppError.NfcError.TagLost -> true
            is AppError.ServiceError.StartFailed -> true
            else -> false
        }
    }
    
    /**
     * Check if error requires user action
     */
    fun requiresUserAction(error: AppError): Boolean {
        return when (error) {
            is AppError.NfcError.NotSupported -> false
            is AppError.NfcError.Disabled -> true
            is AppError.PermissionError -> true
            else -> false
        }
    }
}
