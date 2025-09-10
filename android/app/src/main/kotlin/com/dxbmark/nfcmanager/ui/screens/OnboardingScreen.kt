package com.dxbmark.nfcmanager.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dxbmark.nfcmanager.ui.theme.NothingColors
import com.dxbmark.nfcmanager.ui.theme.NothingTextStyles
import kotlinx.coroutines.launch

/**
 * Onboarding screen with 4 welcome pages
 * Guides new users through app features and setup
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Pager for onboarding pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(
                page = page,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (index == pagerState.currentPage) {
                                NothingColors.NothingRed
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        )
                )
                if (index < 3) Spacer(modifier = Modifier.width(8.dp))
            }
        }
        
        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pagerState.currentPage > 0) {
                TextButton(
                    onClick = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = NothingColors.NothingRed
                    )
                ) {
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }
            
            Button(
                onClick = {
                    if (pagerState.currentPage < 3) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NothingColors.NothingRed,
                    contentColor = NothingColors.PureWhite
                )
            ) {
                Text(if (pagerState.currentPage < 3) "Next" else "Get Started")
            }
        }
    }
}

@Composable
fun OnboardingPage(
    page: Int,
    modifier: Modifier = Modifier
) {
    val (title, description, icon) = when (page) {
        0 -> Triple(
            "Welcome to NFC Manager",
            "Protect your privacy with smart NFC monitoring and security alerts",
            Icons.Default.Security
        )
        1 -> Triple(
            "Smart Monitoring",
            "Advanced notification system keeps you informed about NFC activity and security status",
            Icons.Default.Notifications
        )
        2 -> Triple(
            "Security Score",
            "Track your security level with our intelligent scoring system and get personalized recommendations",
            Icons.Default.Star
        )
        3 -> Triple(
            "Ready to Start",
            "Begin your journey towards better NFC security and privacy protection",
            Icons.Default.CheckCircle
        )
        else -> Triple("", "", Icons.Default.Info)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = NothingColors.NothingRed
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        Text(
            text = title,
            style = NothingTextStyles.HeaderTitle,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        // Additional content for specific pages
        when (page) {
            0 -> {
                Spacer(modifier = Modifier.height(24.dp))
                OnboardingFeatureList(
                    features = listOf(
                        "Real-time NFC monitoring",
                        "Privacy-focused design",
                        "Nothing OS inspired UI"
                    )
                )
            }
            1 -> {
                Spacer(modifier = Modifier.height(24.dp))
                OnboardingFeatureList(
                    features = listOf(
                        "Duration tracking",
                        "Security level alerts",
                        "Smart notifications"
                    )
                )
            }
            2 -> {
                Spacer(modifier = Modifier.height(24.dp))
                OnboardingFeatureList(
                    features = listOf(
                        "5-level security rating",
                        "Personalized recommendations",
                        "Weekly security reports"
                    )
                )
            }
            3 -> {
                Spacer(modifier = Modifier.height(24.dp))
                OnboardingFeatureList(
                    features = listOf(
                        "Complete privacy protection",
                        "Local data storage only",
                        "No external data transmission"
                    )
                )
            }
        }
    }
}

@Composable
fun OnboardingFeatureList(features: List<String>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        features.forEach { feature ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = NothingColors.NothingRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
