package com.dxbmark.nfcmanager.utils

import androidx.compose.ui.graphics.Color
import com.dxbmark.nfcmanager.data.database.entities.NFCEventEntity
import com.dxbmark.nfcmanager.ui.theme.NothingColors

/**
 * Calculator for privacy and security scores
 * Analyzes NFC events and provides security recommendations
 */
class PrivacyScoreCalculator {
    
    /**
     * Calculates security score based on NFC events
     * @param events List of NFC events to analyze
     * @return SecurityScore with score, level, violations and recommendations
     */
    fun calculateSecurityScore(events: List<NFCEventEntity>): SecurityScore {
        var score = 100
        val violations = mutableListOf<SecurityViolation>()
        
        events.forEach { event ->
            when (event.eventType) {
                "NFC_ENABLED_LONG" -> {
                    score -= 15
                    violations.add(SecurityViolation.LONG_NFC_USAGE)
                }
                "UNKNOWN_TAG" -> {
                    score -= 10
                    violations.add(SecurityViolation.UNKNOWN_TAG)
                }
                "MULTIPLE_TAGS_RAPID" -> {
                    score -= 20
                    violations.add(SecurityViolation.RAPID_TAG_DETECTION)
                }
                "SUSPICIOUS_ACTIVITY" -> {
                    score -= 25
                    violations.add(SecurityViolation.SUSPICIOUS_ACTIVITY)
                }
                "TAG_DISCOVERED" -> {
                    // Minor deduction for any tag detection
                    score -= 2
                }
            }
        }
        
        return SecurityScore(
            score = maxOf(0, score),
            level = determineSecurityLevel(score),
            violations = violations,
            recommendations = generateRecommendations(violations)
        )
    }
    
    /**
     * Determines security level based on score
     */
    private fun determineSecurityLevel(score: Int): SecurityLevel {
        return when (score) {
            in 90..100 -> SecurityLevel.EXCELLENT
            in 75..89 -> SecurityLevel.GOOD
            in 60..74 -> SecurityLevel.MODERATE
            in 40..59 -> SecurityLevel.POOR
            else -> SecurityLevel.CRITICAL
        }
    }
    
    /**
     * Generates security recommendations based on violations
     */
    private fun generateRecommendations(violations: List<SecurityViolation>): List<String> {
        val recommendations = mutableListOf<String>()
        
        violations.forEach { violation ->
            when (violation) {
                SecurityViolation.LONG_NFC_USAGE -> {
                    recommendations.add("Disable NFC when not in use")
                    recommendations.add("Set shorter reminder intervals")
                }
                SecurityViolation.UNKNOWN_TAG -> {
                    recommendations.add("Be cautious with unknown NFC tags")
                    recommendations.add("Enable unknown tag blocking")
                }
                SecurityViolation.RAPID_TAG_DETECTION -> {
                    recommendations.add("Avoid crowded areas when using NFC")
                    recommendations.add("Check for suspicious devices nearby")
                }
                SecurityViolation.SUSPICIOUS_ACTIVITY -> {
                    recommendations.add("Review your NFC usage patterns")
                    recommendations.add("Enable maximum security mode")
                }
            }
        }
        
        // Add general recommendations if no specific violations
        if (recommendations.isEmpty()) {
            recommendations.add("Keep up the good security practices!")
            recommendations.add("Regularly review your NFC activity log")
        }
        
        return recommendations.distinct()
    }
}

/**
 * Data class representing security score and analysis
 */
data class SecurityScore(
    val score: Int,
    val level: SecurityLevel,
    val violations: List<SecurityViolation>,
    val recommendations: List<String>
)

/**
 * Enum representing security levels with display names and colors
 */
enum class SecurityLevel(val displayName: String, val color: Color) {
    EXCELLENT("Excellent", Color(0xFF4CAF50)),
    GOOD("Good", Color(0xFF8BC34A)),
    MODERATE("Moderate", Color(0xFFFF9800)),
    POOR("Poor", Color(0xFFFF5722)),
    CRITICAL("Critical", Color(0xFFE53E3E))
}

/**
 * Enum representing different types of security violations
 */
enum class SecurityViolation {
    LONG_NFC_USAGE,
    UNKNOWN_TAG,
    RAPID_TAG_DETECTION,
    SUSPICIOUS_ACTIVITY
}
