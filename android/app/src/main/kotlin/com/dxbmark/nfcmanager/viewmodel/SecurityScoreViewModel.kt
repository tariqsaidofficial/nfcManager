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
    }

    fun calculateSecurityScore() {
        viewModelScope.launch {
            _isLoading.value = true
            val context = application.applicationContext
            val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

            try {
                if (nfcAdapter == null) {
                    _securityScore.value = SecurityScore(
                        score = 0,
                        level = SecurityLevel.NOT_APPLICABLE,
                        violations = emptyList(),
                        recommendations = listOf(context.getString(R.string.nfc_not_supported_device))
                    )
                } else if (!nfcAdapter.isEnabled) {
                    _securityScore.value = SecurityScore(
                        score = 100,
                        level = SecurityLevel.EXCELLENT,
                        violations = emptyList(),
                        recommendations = listOf(context.getString(R.string.recommendation_nfc_disabled_secure))
                    )
                    repository.updateSecurityScore(100, SecurityLevel.EXCELLENT.name)
                } else {
                    val recentEvents = repository.getEventsFromLastDays(7).first()
                    // privacyScoreCalculator.calculateSecurityScore() already returns SecurityScore with List<String>
                    val calculatedScore = privacyScoreCalculator.calculateSecurityScore(recentEvents)
                    _securityScore.value = calculatedScore // Use directly
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
