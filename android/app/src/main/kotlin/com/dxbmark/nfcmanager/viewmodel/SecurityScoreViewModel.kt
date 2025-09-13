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
    private val application: Application, // Added Application context
    private val repository: NFCRepository
) : ViewModel() {

    private val privacyScoreCalculator = PrivacyScoreCalculator()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Initial state reflects loading, will be updated by calculateSecurityScore
    private val _securityScore = MutableStateFlow(
        SecurityScore(
            score = 0, // Default to 0 or -1 while loading or if not applicable
            level = SecurityLevel.NOT_APPLICABLE, // Default to NOT_APPLICABLE
            violations = emptyList(),
            recommendations = listOf(application.getString(R.string.loading_security_score))
        )
    )
    val securityScore: StateFlow<SecurityScore> = _securityScore.asStateFlow()

    init {
        calculateSecurityScore()
    }

    fun calculateSecurityScore() {
        viewModelScope.launch {
            _isLoading.value = true
            val context = application.applicationContext
            val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

            try {
                if (nfcAdapter == null) {
                    // NFC Not Supported
                    _securityScore.value = SecurityScore(
                        score = 0, // Or -1 to clearly indicate N/A if 0 has other meanings
                        level = SecurityLevel.NOT_APPLICABLE,
                        violations = emptyList(),
                        recommendations = listOf(context.getString(R.string.nfc_not_supported_device))
                    )
                    // Do not update repository if NFC is not supported or score is N/A
                } else if (!nfcAdapter.isEnabled) {
                    // NFC Supported but Disabled - This is a good security state
                    _securityScore.value = SecurityScore(
                        score = 100,
                        level = SecurityLevel.EXCELLENT,
                        violations = emptyList(),
                        recommendations = listOf(context.getString(R.string.nfc_disabled_error_turn_on)) // A general good practice message
                    )
                    // Update settings with this positive score
                    repository.updateSecurityScore(100, SecurityLevel.EXCELLENT.name)
                } else {
                    // NFC Enabled - Calculate score based on events
                    val recentEvents = repository.getEventsFromLastDays(7).first()
                    val calculatedScore = privacyScoreCalculator.calculateSecurityScore(recentEvents)
                    _securityScore.value = calculatedScore
                    // Update settings with latest calculated score
                    repository.updateSecurityScore(calculatedScore.score, calculatedScore.level.name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // In case of error, set a default error state or keep the last known good score if appropriate
                 _securityScore.value = SecurityScore(
                        score = 0,
                        level = SecurityLevel.NOT_APPLICABLE,
                        violations = emptyList(),
                        recommendations = listOf("Error calculating score: ${e.message}")
                    )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshSecurityScore() {
        calculateSecurityScore()
    }

    // getSecurityScoreForPeriod might also need to consider NFC status, 
    // but for now, it assumes if one wants a historical score, it's based on past actual events.
    // If NFC is not supported now, a historical score might still be relevant if it was supported before.
    // For simplicity, it will use the current calculator which doesn't know about current NFC status.
    fun getSecurityScoreForPeriod(days: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            // Note: This does not re-check current NFC adapter status, focuses on historical data
            try {
                val events = repository.getEventsFromLastDays(days).first()
                // If events list is empty and we want to reflect current NFC status, logic would be complex.
                // Assuming PrivacyScoreCalculator handles empty events gracefully.
                val calculatedScore = privacyScoreCalculator.calculateSecurityScore(events)
                _securityScore.value = calculatedScore
            } catch (e: Exception) {
                e.printStackTrace()
                 _securityScore.value = SecurityScore(
                        score = 0,
                        level = SecurityLevel.NOT_APPLICABLE,
                        violations = emptyList(),
                        recommendations = listOf("Error calculating period score: ${e.message}")
                    )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
