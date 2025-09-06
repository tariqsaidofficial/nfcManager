package com.nothingos.nfcmanager.ui.screens

import android.Manifest // <-- Add this import
import androidx.activity.compose.rememberLauncherForActivityResult // <-- Add this import
import androidx.activity.result.contract.ActivityResultContracts // <-- Add this import
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Already here
import com.nothingos.nfcmanager.ui.theme.NothingColors
import com.nothingos.nfcmanager.ui.theme.NothingTextStyles
import com.nothingos.nfcmanager.ui.theme.NothingUIColors
import com.nothingos.nfcmanager.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest // <-- Add this import

/**
 * Settings Screen with app configuration
 * Built with Jetpack Compose Material 3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel() // This will now use the factory from MainActivity
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isDarkTheme = settings.isDarkMode

    // Permission Launcher for Post Notifications
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, you could log this or show a toast
                // viewModel.logEvent("PERMISSION", "Notification permission granted", "CheckCircle")
            } else {
                // Permission denied, you could log this or show a toast explaining why it's needed
                // viewModel.logEvent("PERMISSION", "Notification permission denied", "AlertCircle")
                // Note: The setting will remain "on", but notifications won't appear.
                // The user needs to grant it from system settings if they want notifications.
            }
        }
    )

    // Collect the permission request flow
    LaunchedEffect(Unit) {
        viewModel.requestNotificationPermissionFlow.collectLatest {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "SETTINGS",
            style = NothingTextStyles.HeaderTitle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Display Settings Section (New)
        SettingsSection(title = "DISPLAY") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) NothingColors.DarkSurface else NothingUIColors.LightCardBackground
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isDarkTheme) NothingColors.PureWhite else NothingColors.LightPrimaryText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ThemeOption(
                        title = "Dark Mode",
                        subtitle = "Easy on the eyes",
                        isSelected = isDarkTheme,
                        onClick = { viewModel.setTheme(true) },
                        isDarkTheme = isDarkTheme
                    )
                    ThemeOption(
                        title = "Light Mode",
                        subtitle = "Classic look",
                        isSelected = !isDarkTheme,
                        onClick = { viewModel.setTheme(false) },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // NFC Configuration Section
        SettingsSection(title = "NFC CONFIGURATION") {
            SettingsItem(
                title = "Auto Reminder",
                subtitle = "Privacy alerts when NFC stays enabled",
                checked = settings.autoReminderEnabled,
                onCheckedChange = { viewModel.toggleAutoReminder() }
            )
            
            if (settings.autoReminderEnabled) {
                ReminderIntervalSelector(
                    currentInterval = settings.reminderInterval,
                    onIntervalChange = { viewModel.updateReminderInterval(it) }
                )
            }
            
            SettingsItem(
                title = "Show Notifications",
                subtitle = "Display privacy notifications",
                checked = settings.showNotifications,
                onCheckedChange = { viewModel.toggleNotifications() } // This will now trigger the permission flow
            )
            
            SettingsItem(
                title = "Vibration",
                subtitle = "Haptic feedback for alerts",
                checked = settings.vibrationEnabled,
                onCheckedChange = { viewModel.toggleVibration() }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Privacy & Security Section
        SettingsSection(title = "PRIVACY & SECURITY") {
            SettingsItem(
                title = "Privacy Mode",
                subtitle = "Enhanced privacy protection",
                checked = settings.isPrivacyModeEnabled,
                onCheckedChange = { viewModel.togglePrivacyMode() }
            )
            
            SettingsItem(
                title = "Block Unknown Tags",
                subtitle = "Automatically block unrecognized NFC tags",
                checked = settings.blockUnknownTags,
                onCheckedChange = { viewModel.toggleBlockUnknownTags() }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Performance Section
        SettingsSection(title = "PERFORMANCE") {
            SettingsItem(
                title = "Battery Optimization",
                subtitle = "Optimize for better battery life",
                checked = settings.batteryOptimized,
                onCheckedChange = { viewModel.toggleBatteryOptimization() }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.exportSettings() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "EXPORT SETTINGS",
                    style = NothingTextStyles.ButtonText
                )
            }
            
            OutlinedButton(
                onClick = { viewModel.resetAllSettings() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "RESET TO DEFAULTS",
                    style = NothingTextStyles.ButtonText
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // App Information
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
                    text = "Nothing OS Inspired Design",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Built with official Android tools: Kotlin, Jetpack Compose, Room Database, and MVVM architecture.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
    
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = NothingTextStyles.SmallCaps,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (title != "DISPLAY") { 
             Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    content()
                }
            }
        } else {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = NothingTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderIntervalSelector(
    currentInterval: Int,
    onIntervalChange: (Int) -> Unit
) {
    val intervals = listOf(5, 10, 15, 20, 30, 40, 50, 60)
    val firstRowIntervals = intervals.subList(0, 4)
    val secondRowIntervals = intervals.subList(4, 8)
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Reminder Interval",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "How often to remind about NFC privacy",
            style = NothingTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            firstRowIntervals.forEach { interval ->
                val isSelected = currentInterval == interval
                val scale by animateFloatAsState(targetValue = if (isSelected) 1.05f else 1.0f, label = "chipScale")
                FilterChip(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onIntervalChange(interval) 
                    },
                    label = { Text(text = "${interval}s", style = NothingTextStyles.ButtonText) },
                    selected = isSelected,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            secondRowIntervals.forEach { interval ->
                val isSelected = currentInterval == interval
                val scale by animateFloatAsState(targetValue = if (isSelected) 1.05f else 1.0f, label = "chipScale")
                FilterChip(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onIntervalChange(interval) 
                    },
                    label = { Text(text = "${interval}s", style = NothingTextStyles.ButtonText) },
                    selected = isSelected,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Current: ${currentInterval}s",
            style = NothingTextStyles.Caption,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun ThemeOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = NothingColors.NothingRed,
                unselectedColor = if (isDarkTheme) NothingColors.TextSecondary else NothingColors.LightSecondaryText
            )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkTheme) NothingColors.PureWhite else NothingColors.LightPrimaryText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkTheme) NothingColors.TextSecondary else NothingColors.LightSecondaryText
            )
        }
    }
}
