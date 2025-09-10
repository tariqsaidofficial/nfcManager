package com.dxbmark.nfcmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.ui.theme.NothingTextStyles
import com.dxbmark.nfcmanager.utils.SecurityLevel
import com.dxbmark.nfcmanager.utils.SecurityScore
import com.dxbmark.nfcmanager.viewmodel.SecurityScoreViewModel

/**
 * Screen displaying security score and recommendations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScoreScreen(
    viewModel: SecurityScoreViewModel = viewModel()
) {
    val securityScore by viewModel.securityScore.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Security Score",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Security Score Card
            SecurityScoreCard(securityScore = securityScore)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Security Level Indicator
            SecurityLevelIndicator(securityLevel = securityScore.level)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recommendations
            if (securityScore.recommendations.isNotEmpty()) {
                SecurityRecommendations(recommendations = securityScore.recommendations)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Security Tips
            SecurityTipsCard()
        }
    }
}

@Composable
fun SecurityScoreCard(securityScore: SecurityScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = securityScore.level.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score Circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(securityScore.level.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${securityScore.score}",
                        style = NothingTextStyles.NFCStatusLarge,
                        color = securityScore.level.color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Score",
                        style = MaterialTheme.typography.bodyMedium,
                        color = securityScore.level.color
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Security Level
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getSecurityLevelIcon(securityScore.level),
                    contentDescription = null,
                    tint = securityScore.level.color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = securityScore.level.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = securityScore.level.color,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = getSecurityLevelDescription(securityScore.level),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SecurityLevelIndicator(securityLevel: SecurityLevel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Security Level Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SecurityLevel.values().forEach { level ->
                SecurityLevelItem(
                    level = level,
                    isCurrent = level == securityLevel
                )
                if (level != SecurityLevel.values().last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SecurityLevelItem(
    level: SecurityLevel,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Level indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    if (isCurrent) level.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Level name
        Text(
            text = level.displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) level.color else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Score range
        Text(
            text = getScoreRange(level),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SecurityRecommendations(recommendations: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Security Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            recommendations.forEach { recommendation ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Security Tips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val tips = listOf(
                "Disable NFC when not in use",
                "Be cautious with unknown NFC tags",
                "Avoid using NFC in crowded areas",
                "Regularly review your NFC activity log",
                "Keep your device updated"
            )
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Helper functions
private fun getSecurityLevelIcon(level: SecurityLevel): ImageVector {
    return when (level) {
        SecurityLevel.EXCELLENT -> Icons.Default.Verified
        SecurityLevel.GOOD -> Icons.Default.CheckCircle
        SecurityLevel.MODERATE -> Icons.Default.Warning
        SecurityLevel.POOR -> Icons.Default.Error
        SecurityLevel.CRITICAL -> Icons.Default.Dangerous
    }
}

private fun getSecurityLevelDescription(level: SecurityLevel): String {
    return when (level) {
        SecurityLevel.EXCELLENT -> "Excellent security practices! Keep it up."
        SecurityLevel.GOOD -> "Good security level with minor improvements needed."
        SecurityLevel.MODERATE -> "Moderate security. Consider following recommendations."
        SecurityLevel.POOR -> "Poor security level. Immediate action required."
        SecurityLevel.CRITICAL -> "Critical security issues. Please review recommendations."
    }
}

private fun getScoreRange(level: SecurityLevel): String {
    return when (level) {
        SecurityLevel.EXCELLENT -> "90-100"
        SecurityLevel.GOOD -> "75-89"
        SecurityLevel.MODERATE -> "60-74"
        SecurityLevel.POOR -> "40-59"
        SecurityLevel.CRITICAL -> "0-39"
    }
}
