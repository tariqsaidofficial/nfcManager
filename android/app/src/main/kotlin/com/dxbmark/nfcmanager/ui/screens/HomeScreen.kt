package com.dxbmark.nfcmanager.ui.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dxbmark.nfcmanager.ui.theme.NothingTextStyles
import com.dxbmark.nfcmanager.viewmodel.MainViewModel

/**
 * Home Screen with NFC status and controls
 * Built with Jetpack Compose following Nothing OS design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // val settings by viewModel.settings.collectAsState() // Not directly used in this version of UI
    val todayEventCount by viewModel.todayEventCount.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh NFC status when the screen resumes using DisposableEffect for proper cleanup
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
            text = "NFC MANAGER",
            style = NothingTextStyles.HeaderTitle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Main NFC Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable(enabled = uiState.isNFCSupported && !uiState.isNFCEnabled) {
                    // Allow clicking card to open settings only if supported and disabled
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
                    text = "NFC STATUS",
                    style = NothingTextStyles.SmallCaps,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                when {
                    !uiState.isNFCSupported -> {
                        Text(
                            text = "NOT SUPPORTED",
                            style = NothingTextStyles.NFCStatusLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "NFC hardware not found on this device.",
                            style = NothingTextStyles.Caption,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                    uiState.isNFCEnabled -> {
                        Text(
                            text = "ENABLED",
                            style = NothingTextStyles.NFCStatusLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Privacy monitoring active",
                            style = NothingTextStyles.Caption,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    else -> { // Supported but Disabled
                        Text(
                            text = "DISABLED",
                            style = NothingTextStyles.NFCStatusLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap here to enable NFC in system settings.",
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
                    text = "TODAY'S ACTIVITY",
                    style = NothingTextStyles.SmallCaps,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$todayEventCount events logged",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // NFC Control Button - Show only if NFC is supported but disabled
        if (uiState.isNFCSupported && !uiState.isNFCEnabled) {
            Button(
                onClick = { 
                    if (context is Activity) {
                        viewModel.requestOpenNfcSettings(context)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    // Use a more prominent color to prompt action
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary 
                )
            ) {
                Text(
                    text = "ENABLE NFC IN SETTINGS",
                    style = NothingTextStyles.ButtonText
                )
            }
        } else if (!uiState.isNFCSupported) {
             // Optionally, show a disabled-looking message or hide the button space entirely
             Box(modifier = Modifier.fillMaxWidth().height(48.dp)) // Placeholder to maintain layout if button is hidden
        } else {
            // NFC is supported and enabled, button not needed for enabling
            Box(modifier = Modifier.fillMaxWidth().height(48.dp)) // Placeholder or alternative action
        }
    }
}
