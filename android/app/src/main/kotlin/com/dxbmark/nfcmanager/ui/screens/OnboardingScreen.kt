package com.dxbmark.nfcmanager.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dxbmark.nfcmanager.R // Required for R.string access
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
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 5 }) // Changed from 4 to 5
    val coroutineScope = rememberCoroutineScope()
    var notificationPermissionGranted by remember { mutableStateOf(false) }
    
    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
        // Complete onboarding after permission request
        onComplete()
    }
    
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
            repeat(5) { index -> // Changed from 4 to 5
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                    Text(stringResource(R.string.onboarding_button_previous))
                }
            } else {
                // Spacer to keep the "Next/Get Started" button to the right
                Spacer(Modifier.width(IntrinsicSize.Min).weight(1f)) // Occupy space of a button
            }
            
            Button(
                onClick = {
                    when {
                        pagerState.currentPage < 4 -> {
                            // Navigate to next page
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                        pagerState.currentPage == 4 -> {
                            // Last page - request notification permission
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                // For Android 12 and below, notifications are enabled by default
                                onComplete()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NothingColors.NothingRed,
                    contentColor = NothingColors.PureWhite
                )
            ) {
                Text(
                    when (pagerState.currentPage) {
                        4 -> stringResource(R.string.onboarding_button_allow_notifications)
                        else -> stringResource(R.string.onboarding_button_next)
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(
    page: Int,
    modifier: Modifier = Modifier
) {
    val title: String
    val description: String
    val icon: ImageVector
    val features: List<String>

    when (page) {
        0 -> {
            title = stringResource(R.string.onboarding_page0_title)
            description = stringResource(R.string.onboarding_page0_description)
            icon = Icons.Default.Security
            features = listOf(
                stringResource(R.string.onboarding_page0_feature1),
                stringResource(R.string.onboarding_page0_feature2),
                stringResource(R.string.onboarding_page0_feature3)
            )
        }
        1 -> {
            title = stringResource(R.string.onboarding_page1_title)
            description = stringResource(R.string.onboarding_page1_description)
            icon = Icons.Default.Notifications
            features = listOf(
                stringResource(R.string.onboarding_page1_feature1),
                stringResource(R.string.onboarding_page1_feature2),
                stringResource(R.string.onboarding_page1_feature3)
            )
        }
        2 -> {
            title = stringResource(R.string.onboarding_page2_title)
            description = stringResource(R.string.onboarding_page2_description)
            icon = Icons.Default.Star
            features = listOf(
                stringResource(R.string.onboarding_page2_feature1),
                stringResource(R.string.onboarding_page2_feature2),
                stringResource(R.string.onboarding_page2_feature3)
            )
        }
        3 -> {
            title = stringResource(R.string.onboarding_page3_title)
            description = stringResource(R.string.onboarding_page3_description)
            icon = Icons.Default.CheckCircle
            features = listOf(
                stringResource(R.string.onboarding_page3_feature1),
                stringResource(R.string.onboarding_page3_feature2),
                stringResource(R.string.onboarding_page3_feature3)
            )
        }
        4 -> {
            // New page for notification permission
            title = stringResource(R.string.onboarding_page4_title)
            description = stringResource(R.string.onboarding_page4_description)
            icon = Icons.Default.NotificationsActive
            features = listOf(
                stringResource(R.string.onboarding_page4_feature1),
                stringResource(R.string.onboarding_page4_feature2),
                stringResource(R.string.onboarding_page4_feature3)
            )
        }
        else -> { // Fallback
            title = ""
            description = ""
            icon = Icons.Default.Info
            features = emptyList()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 64.dp), // Increased vertical padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null, // Icons are decorative
            modifier = Modifier.size(100.dp), // Slightly reduced icon size
            tint = NothingColors.NothingRed
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = title,
            style = NothingTextStyles.HeaderTitle,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold // Already part of NothingTextStyles.HeaderTitle typically
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp) // Add horizontal padding for longer text
        )
        
        Spacer(modifier = Modifier.height(32.dp)) // Increased space before features
        
        // Feature List for all pages
        OnboardingFeatureList(features = features)
    }
}

@Composable
fun OnboardingFeatureList(features: List<String>) {
    Column(
        horizontalAlignment = Alignment.Start, // Align feature text to the start
        verticalArrangement = Arrangement.spacedBy(8.dp) // Add space between feature items
    ) {
        features.forEach { feature ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check, // Using Filled.Check for consistency
                    contentDescription = null, // Decorative icon
                    modifier = Modifier.size(20.dp), // Slightly larger check icon
                    tint = NothingColors.NothingRed
                )
                Spacer(modifier = Modifier.width(12.dp)) // Increased space after icon
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
