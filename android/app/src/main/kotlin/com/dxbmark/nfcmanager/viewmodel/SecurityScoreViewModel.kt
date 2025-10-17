package com.dxbmark.nfcmanager.viewmodel

import android.app.Application
import android.nfc.NfcAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dxbmark.nfcmanager.R // Required for string resources
import com.dxbmark.nfcmanager.data.repository.NFCRepository
import com.dxbmark.nfcmanager.utils.PrivacyScoreCalculator
import com.dxbmark.nfcmanager.utils.SecurityLevel
import com.dxbmark.nfcmanager.utils.SecurityScore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityScoreViewModel @Inject constructor(
    private val application: Application,
    private val repository: NFCRepository
) : ViewModel() {

    private val privacyScoreCalculator = PrivacyScoreCalculator()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // SecurityScore expects List<String> for recommendations and violations
    private val _securityScore = MutableStateFlow(
        SecurityScore(
            score = 0,
            level = SecurityLevel.NOT_APPLICABLE,
            violations = emptyList(), // Correct type: List<String>
            recommendations = listOf(application.getString(R.string.loading_security_score)) // Correct type: List<String>
        )
    )
    val securityScore: StateFlow<SecurityScore> = _securityScore.asStateFlow()

    init {
        calculateSecurityScore()
        // Observe NFC state changes and recalculate score
        observeNfcStateChanges()
    }
    
    private fun observeNfcStateChanges() {
        viewModelScope.launch {
            // Monitor settings changes that might indicate NFC state change
            repository.getSettings().collect {
                // Recalculate score when settings change
                calculateSecurityScore()
            }
        }
    }

    fun calculateSecurityScore() {
        viewModelScope.launch {
            _isLoading.value = true
            val context = application.applicationContext
            val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

            try {
                if (nfcAdapter == null) {
                    // Device doesn't support NFC
                    _securityScore.value = SecurityScore(
                        score = 0,
                        level = SecurityLevel.NOT_APPLICABLE,
                        violations = emptyList(),
                        recommendations = listOf(context.getString(R.string.nfc_not_supported_device))
                    )
                } else if (!nfcAdapter.isEnabled) {
                    // NFC is disabled - check historical data to provide meaningful score
                    val recentEvents = repository.getEventsFromLastDays(7).first()
                    
                    if (recentEvents.isEmpty()) {
                        // No recent activity - NFC has been off, show neutral state
                        _securityScore.value = SecurityScore(
                            score = 0,
                            level = SecurityLevel.NOT_APPLICABLE,
                            violations = emptyList(),
                            recommendations = listOf(
                                context.getString(R.string.recommendation_nfc_disabled_no_data),
                                context.getString(R.string.recommendation_enable_nfc_when_needed)
                            )
                        )
                        repository.updateSecurityScore(0, SecurityLevel.NOT_APPLICABLE.name)
                    } else {
                        // Has historical data - calculate based on past usage
                        val calculatedScore = privacyScoreCalculator.calculateSecurityScore(recentEvents)
                        val adjustedRecommendations = mutableListOf<String>()
                        adjustedRecommendations.add(context.getString(R.string.recommendation_nfc_currently_disabled))
                        adjustedRecommendations.addAll(calculatedScore.recommendations)
                        
                        _securityScore.value = calculatedScore.copy(
                            recommendations = adjustedRecommendations
                        )
                        repository.updateSecurityScore(calculatedScore.score, calculatedScore.level.name)
                    }
                } else {
                    // NFC is enabled - calculate score based on recent events
                    val recentEvents = repository.getEventsFromLastDays(7).first()
                    val calculatedScore = privacyScoreCalculator.calculateSecurityScore(recentEvents)
                    _securityScore.value = calculatedScore
                    repository.updateSecurityScore(calculatedScore.score, calculatedScore.level.name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                 _securityScore.value = SecurityScore(
                        score = 0,
                        level = SecurityLevel.NOT_APPLICABLE,
                        violations = emptyList(),
                        recommendations = listOf(context.getString(R.string.error_calculating_score, e.message ?: "Unknown error"))
                    )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshSecurityScore() {
        calculateSecurityScore()
    }

    fun getSecurityScoreForPeriod(days: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val context = application.applicationContext
            try {
                val events = repository.getEventsFromLastDays(days).first()
                val calculatedScore = privacyScoreCalculator.calculateSecurityScore(events)
                _securityScore.value = calculatedScore // Use directly
            } catch (e: Exception) {
                e.printStackTrace()
                 _securityScore.value = SecurityScore(
                        score = 0,
                        level = SecurityLevel.NOT_APPLICABLE,
                        violations = emptyList(),
                        recommendations = listOf(context.getString(R.string.error_calculating_period_score, e.message ?: "Unknown error"))
                    )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
