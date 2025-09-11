package com.dxbmark.nfcmanager.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel // Default viewModel import
import androidx.hilt.navigation.compose.hiltViewModel // Import for hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp // Required for sp unit
import androidx.navigation.NavController
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.ui.components.AppRoutes // Make sure AppRoutes is imported
import com.dxbmark.nfcmanager.ui.theme.NothingTextStyles
// import com.dxbmark.nfcmanager.utils.SecurityLevel // Not directly used in this file anymore
import com.dxbmark.nfcmanager.viewmodel.MainViewModel
import com.dxbmark.nfcmanager.viewmodel.SecurityScoreViewModel

/**
 * Home Screen with NFC status and controls
 * Built with Jetpack Compose following Nothing OS design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel = hiltViewModel(), // Changed to hiltViewModel for consistency if needed, or keep viewModel()
    navController: NavController // Added NavController parameter
) {
    val uiState by viewModel.uiState.collectAsState()
    val todayEventCount by viewModel.todayEventCount.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshNfcStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.home_screen_title),
            style = NothingTextStyles.HeaderTitle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Security Score Card
        SecurityScoreQuickCard(
            modifier = Modifier.fillMaxWidth(),
            navController = navController // Pass NavController
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main NFC Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable(enabled = uiState.isNFCSupported && !uiState.isNFCEnabled) {
                    if (context is Activity) {
                        viewModel.requestOpenNfcSettings(context)
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.nfc_status_title),
                    style = NothingTextStyles.SmallCaps,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                when {
                    !uiState.isNFCSupported -> {
                        Text(
                            text = stringResource(R.string.nfc_not_supported),
                            style = NothingTextStyles.NFCStatusLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.nfc_hardware_not_found),
                            style = NothingTextStyles.Caption,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                    uiState.isNFCEnabled -> {
                        Text(
                            text = stringResource(R.string.nfc_status_enabled),
                            style = NothingTextStyles.NFCStatusLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.privacy_monitoring_active),
                            style = NothingTextStyles.Caption,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    else -> { // Supported but Disabled
                        Text(
                            text = stringResource(R.string.nfc_status_disabled),
                            style = NothingTextStyles.NFCStatusLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.tap_to_enable_nfc),
                            style = NothingTextStyles.Caption,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_todays_activity_title),
                    style = NothingTextStyles.SmallCaps,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.events_logged_format, todayEventCount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (uiState.isNFCSupported && !uiState.isNFCEnabled) {
            Button(
                onClick = { 
                    if (context is Activity) {
                        viewModel.requestOpenNfcSettings(context)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary 
                )
            ) {
                Text(
                    text = stringResource(R.string.enable_nfc_button),
                    style = NothingTextStyles.ButtonText
                )
            }
        } else {
             Box(modifier = Modifier.fillMaxWidth().height(48.dp))
        }
    }
}

@Composable
fun SecurityScoreQuickCard(
    modifier: Modifier = Modifier,
    navController: NavController // Added NavController parameter
) {
    val securityScoreViewModel: SecurityScoreViewModel = hiltViewModel()
    val securityScore by securityScoreViewModel.securityScore.collectAsState()
    
    Card(
        modifier = modifier
            .clickable { 
                navController.navigate(AppRoutes.SECURITY_SCORE_DETAIL) // Navigate on click
            },
        colors = CardDefaults.cardColors(
            containerColor = securityScore.level.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = securityScore.level.color.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${securityScore.score}",
                    style = NothingTextStyles.HeaderTitle.copy(
                        fontSize = 20.sp, // You can adjust this size later if needed
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = securityScore.level.color
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.security_score_card_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = securityScore.level.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = securityScore.level.color
                )
            }
            
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
