package com.dxbmark.nfcmanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dxbmark.nfcmanager.data.repository.NFCRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Onboarding Screen
 * Manages onboarding completion state
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: NFCRepository
) : ViewModel() {

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        Log.e("OnboardingViewModel", "=== OnboardingViewModel.init() STARTED ===")
        // Initialize with default values to prevent crashes
        _isOnboardingCompleted.value = false
        _isLoading.value = true
        Log.e("OnboardingViewModel", "Initial values set - completed: false, loading: true")
        // Check onboarding status from database
        checkOnboardingStatus()
        Log.e("OnboardingViewModel", "=== OnboardingViewModel.init() COMPLETED ===")
    }

    /**
     * Checks if onboarding has been completed
     */
    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val settings = repository.getSettings().first()
                _isOnboardingCompleted.value = settings.isOnboardingCompleted
            } catch (e: Exception) {
                // If there's an error, assume onboarding is not completed
                _isOnboardingCompleted.value = false
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Marks onboarding as completed
     */
    fun completeOnboarding() {
        Log.e("OnboardingViewModel", "=== completeOnboarding() STARTED ===")
        viewModelScope.launch {
            try {
                Log.e("OnboardingViewModel", "Setting loading to true...")
                _isLoading.value = true
                
                // Wait for database initialization to complete
                Log.e("OnboardingViewModel", "Waiting for database initialization...")
                var attempts = 0
                while (!com.dxbmark.nfcmanager.NfcManagerApplication.isDatabaseInitialized && attempts < 10) {
                    kotlinx.coroutines.delay(200)
                    attempts++
                    Log.e("OnboardingViewModel", "Waiting for database init... attempt $attempts")
                }
                
                // Additional delay to ensure all background processes complete
                Log.e("OnboardingViewModel", "Waiting 500ms for additional background processes...")
                kotlinx.coroutines.delay(500)
                
                Log.e("OnboardingViewModel", "Marking onboarding as completed...")
                // Mark as completed
                _isOnboardingCompleted.value = true
                
                Log.e("OnboardingViewModel", "Updating database in background...")
                // Update database in background
                repository.updateOnboardingCompleted(true)
                Log.e("OnboardingViewModel", "Database update completed")
                
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "ERROR in completeOnboarding(): ${e.message}", e)
                e.printStackTrace()
                
                // Check if it's a storage space issue
                if (e.message?.contains("No space left on device") == true || 
                    e.message?.contains("ENOSPC") == true) {
                    Log.e("OnboardingViewModel", "STORAGE SPACE ERROR: Device is out of storage space!")
                    // Show user-friendly error message
                    // For now, just mark as completed to prevent UI blocking
                }
                
                // Even if there's an error, mark as completed to prevent UI blocking
                _isOnboardingCompleted.value = true
            } finally {
                Log.e("OnboardingViewModel", "Setting loading to false...")
                _isLoading.value = false
                Log.e("OnboardingViewModel", "=== completeOnboarding() COMPLETED ===")
            }
        }
    }

    /**
     * Resets onboarding status (for testing purposes)
     */
    fun resetOnboarding() {
        viewModelScope.launch {
            try {
                repository.updateOnboardingCompleted(false)
                _isOnboardingCompleted.value = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
