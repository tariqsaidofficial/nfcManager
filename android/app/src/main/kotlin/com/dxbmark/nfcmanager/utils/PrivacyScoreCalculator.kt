package com.dxbmark.nfcmanager.utils

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.data.database.entities.NFCEventEntity
import java.util.concurrent.TimeUnit

/**
 * Calculator for privacy and security scores
 * Analyzes NFC events and provides security recommendations
 */
class PrivacyScoreCalculator(private val context: Context) {
    
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
            recommendations = generateRecommendations(violations) // This will be modified later
        )
    }
    
    /**
     * Determines security level based on score
     */
    private fun determineSecurityLevel(score: Int): SecurityLevel {
        // Handle the NOT_APPLICABLE case if score is set to a specific value like -1
        if (score < 0) return SecurityLevel.NOT_APPLICABLE 
        return when (score) {
            in 90..100 -> SecurityLevel.EXCELLENT
            in 75..89 -> SecurityLevel.GOOD
            in 60..74 -> SecurityLevel.MODERATE
            in 40..59 -> SecurityLevel.POOR
            else -> SecurityLevel.CRITICAL
        }
    }
    
    /**
     * Generates security recommendations based on violations using string resources
     */
    private fun generateRecommendations(violations: List<SecurityViolation>): List<String> {
        val recommendations = mutableListOf<String>()
        
        violations.forEach { violation ->
            when (violation) {
                SecurityViolation.LONG_NFC_USAGE -> {
                    recommendations.add(context.getString(R.string.recommendation_disable_nfc))
                    recommendations.add(context.getString(R.string.recommendation_shorter_intervals))
                }
                SecurityViolation.UNKNOWN_TAG -> {
                    recommendations.add(context.getString(R.string.recommendation_cautious_unknown_tags))
                    recommendations.add(context.getString(R.string.recommendation_enable_unknown_tag_blocking))
                }
                SecurityViolation.RAPID_TAG_DETECTION -> {
                    recommendations.add(context.getString(R.string.recommendation_avoid_crowded_areas_nfc))
                    recommendations.add(context.getString(R.string.recommendation_check_suspicious_devices))
                }
                SecurityViolation.SUSPICIOUS_ACTIVITY -> {
                    recommendations.add(context.getString(R.string.recommendation_review_nfc_usage))
                    recommendations.add(context.getString(R.string.recommendation_enable_max_security_mode))
                }
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add(context.getString(R.string.recommendation_keep_up_good_practices))
            recommendations.add(context.getString(R.string.recommendation_review_activity_log))
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
    val recommendations: List<String> // This will eventually be List<Int> for resource IDs or be handled differently
)

/**
 * Enum representing security levels with display name resource IDs and colors
 */
enum class SecurityLevel(val displayNameResId: Int, val color: Color) {
    EXCELLENT(R.string.security_level_excellent, Color(0xFF4CAF50)),
    GOOD(R.string.security_level_good, Color(0xFF8BC34A)),
    MODERATE(R.string.security_level_moderate, Color(0xFFFF9800)),
    POOR(R.string.security_level_poor, Color(0xFFFF5722)),
    CRITICAL(R.string.security_level_critical, Color(0xFFE53E3E)),
    NOT_APPLICABLE(R.string.security_level_not_applicable, Color(0xFF9E9E9E)) // Added new level
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
