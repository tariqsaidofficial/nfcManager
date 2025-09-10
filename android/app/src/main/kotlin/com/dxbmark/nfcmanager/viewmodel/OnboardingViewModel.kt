package com.dxbmark.nfcmanager.viewmodel

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
        // Initialize with default values to prevent crashes
        _isOnboardingCompleted.value = false
        _isLoading.value = false
        // Don't check status immediately to prevent crashes
        // checkOnboardingStatus()
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
        // Mark as completed immediately to prevent UI blocking
        _isOnboardingCompleted.value = true
        
        // Try to update database in background
        viewModelScope.launch {
            try {
                repository.updateOnboardingCompleted(true)
            } catch (e: Exception) {
                e.printStackTrace()
                // Database update failed, but UI already updated
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
