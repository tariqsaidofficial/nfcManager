package com.dxbmark.nfcmanager.utils.error

import java.io.IOException

/**
 * Sealed class representing all possible errors in the app
 * Provides a centralized error handling system
 * 
 * @property message Human-readable error message
 * @property cause Original exception that caused this error
 */
sealed class AppError(
    open val message: String,
    open val cause: Throwable? = null
) {
    
    // ==================== Network Errors ====================
    
    /**
     * Network-related errors
     */
    sealed class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class NoConnection(
            override val cause: Throwable? = null
        ) : NetworkError("No internet connection", cause)
        
        data class Timeout(
            override val cause: Throwable? = null
        ) : NetworkError("Request timed out", cause)
        
        data class ServerError(
            val code: Int,
            override val cause: Throwable? = null
        ) : NetworkError("Server error: $code", cause)
        
        data class Unknown(
            override val message: String = "Unknown network error",
            override val cause: Throwable? = null
        ) : NetworkError(message, cause)
    }
    
    // ==================== Database Errors ====================
    
    /**
     * Database-related errors
     */
    sealed class DatabaseError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class ReadError(
            override val cause: Throwable? = null
        ) : DatabaseError("Failed to read from database", cause)
        
        data class WriteError(
            override val cause: Throwable? = null
        ) : DatabaseError("Failed to write to database", cause)
        
        data class DeleteError(
            override val cause: Throwable? = null
        ) : DatabaseError("Failed to delete from database", cause)
        
        data class CorruptedData(
            override val cause: Throwable? = null
        ) : DatabaseError("Database data is corrupted", cause)
        
        data class Unknown(
            override val message: String = "Unknown database error",
            override val cause: Throwable? = null
        ) : DatabaseError(message, cause)
    }
    
    // ==================== NFC Errors ====================
    
    /**
     * NFC-related errors
     */
    sealed class NfcError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class NotSupported(
            override val cause: Throwable? = null
        ) : NfcError("NFC is not supported on this device", cause)
        
        data class Disabled(
            override val cause: Throwable? = null
        ) : NfcError("NFC is disabled. Please enable it in settings", cause)
        
        data class PermissionDenied(
            override val cause: Throwable? = null
        ) : NfcError("NFC permission is required", cause)
        
        data class ReadError(
            override val cause: Throwable? = null
        ) : NfcError("Failed to read NFC tag", cause)
        
        data class WriteError(
            override val cause: Throwable? = null
        ) : NfcError("Failed to write to NFC tag", cause)
        
        data class TagLost(
            override val cause: Throwable? = null
        ) : NfcError("NFC tag connection lost", cause)
        
        data class Unknown(
            override val message: String = "Unknown NFC error",
            override val cause: Throwable? = null
        ) : NfcError(message, cause)
    }
    
    // ==================== Permission Errors ====================
    
    /**
     * Permission-related errors
     */
    sealed class PermissionError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class NotificationPermissionDenied(
            override val cause: Throwable? = null
        ) : PermissionError("Notification permission is required", cause)
        
        data class NfcPermissionDenied(
            override val cause: Throwable? = null
        ) : PermissionError("NFC permission is required", cause)
        
        data class Unknown(
            val permission: String,
            override val cause: Throwable? = null
        ) : PermissionError("Permission denied: $permission", cause)
    }
    
    // ==================== Service Errors ====================
    
    /**
     * Service-related errors
     */
    sealed class ServiceError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class StartFailed(
            override val cause: Throwable? = null
        ) : ServiceError("Failed to start service", cause)
        
        data class StopFailed(
            override val cause: Throwable? = null
        ) : ServiceError("Failed to stop service", cause)
        
        data class NotRunning(
            override val cause: Throwable? = null
        ) : ServiceError("Service is not running", cause)
        
        data class Unknown(
            override val message: String = "Unknown service error",
            override val cause: Throwable? = null
        ) : ServiceError(message, cause)
    }
    
    // ==================== Validation Errors ====================
    
    /**
     * Input validation errors
     */
    sealed class ValidationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class InvalidInput(
            val field: String,
            override val cause: Throwable? = null
        ) : ValidationError("Invalid input for: $field", cause)
        
        data class OutOfRange(
            val field: String,
            val min: Int,
            val max: Int,
            override val cause: Throwable? = null
        ) : ValidationError("$field must be between $min and $max", cause)
        
        data class Required(
            val field: String,
            override val cause: Throwable? = null
        ) : ValidationError("$field is required", cause)
        
        data class Unknown(
            override val message: String = "Validation error",
            override val cause: Throwable? = null
        ) : ValidationError(message, cause)
    }
    
    // ==================== File Errors ====================
    
    /**
     * File operation errors
     */
    sealed class FileError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        data class NotFound(
            val fileName: String,
            override val cause: Throwable? = null
        ) : FileError("File not found: $fileName", cause)
        
        data class ReadError(
            override val cause: Throwable? = null
        ) : FileError("Failed to read file", cause)
        
        data class WriteError(
            override val cause: Throwable? = null
        ) : FileError("Failed to write file", cause)
        
        data class PermissionDenied(
            override val cause: Throwable? = null
        ) : FileError("File permission denied", cause)
        
        data class Unknown(
            override val message: String = "Unknown file error",
            override val cause: Throwable? = null
        ) : FileError(message, cause)
    }
    
    // ==================== Unknown/Generic Errors ====================
    
    /**
     * Generic unknown error
     */
    data class Unknown(
        override val message: String = "An unexpected error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)
}

/**
 * Extension function to convert Throwable to AppError
 */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is IOException -> AppError.NetworkError.Unknown(
            message = this.message ?: "Network error",
            cause = this
        )
        is SecurityException -> AppError.PermissionError.Unknown(
            permission = "Unknown",
            cause = this
        )
        else -> AppError.Unknown(
            message = this.message ?: "Unknown error",
            cause = this
        )
    }
}
