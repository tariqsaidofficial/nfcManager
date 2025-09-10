package com.dxbmark.nfcmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dxbmark.nfcmanager.data.database.entities.NFCEventEntity
import com.dxbmark.nfcmanager.data.repository.NFCRepository
import com.dxbmark.nfcmanager.utils.PrivacyScoreCalculator
import com.dxbmark.nfcmanager.utils.SecurityScore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Security Score Screen
 * Manages security score calculation and display
 */
@HiltViewModel
class SecurityScoreViewModel @Inject constructor(
    private val repository: NFCRepository
) : ViewModel() {

    private val privacyScoreCalculator = PrivacyScoreCalculator()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _securityScore = MutableStateFlow(
        SecurityScore(
            score = 100,
            level = com.dxbmark.nfcmanager.utils.SecurityLevel.EXCELLENT,
            violations = emptyList(),
            recommendations = emptyList()
        )
    )
    val securityScore: StateFlow<SecurityScore> = _securityScore.asStateFlow()

    init {
        calculateSecurityScore()
    }

    /**
     * Calculates current security score based on recent events
     */
    fun calculateSecurityScore() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Get events from last 7 days for score calculation
                val recentEvents = repository.getEventsFromLastDays(7).first()
                val calculatedScore = privacyScoreCalculator.calculateSecurityScore(recentEvents)
                
                _securityScore.value = calculatedScore
                
                // Update settings with latest score
                repository.updateSecurityScore(calculatedScore.score, calculatedScore.level.name)
                
            } catch (e: Exception) {
                // Handle error - keep current score
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refreshes security score calculation
     */
    fun refreshSecurityScore() {
        calculateSecurityScore()
    }

    /**
     * Gets security score for a specific time period
     */
    fun getSecurityScoreForPeriod(days: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val events = repository.getEventsFromLastDays(days).first()
                val calculatedScore = privacyScoreCalculator.calculateSecurityScore(events)
                _securityScore.value = calculatedScore
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
