package com.nothingos.nfcmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nothingos.nfcmanager.ui.theme.NothingTextStyles
import com.nothingos.nfcmanager.viewmodel.SettingsViewModel

/**
 * Settings Screen with app configuration
 * Built with Jetpack Compose Material 3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
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
        
        // NFC Configuration Section
        SettingsSection(title = "NFC CONFIGURATION") {
            SettingsItem(
                title = "Auto Reminder",
                subtitle = "Privacy alerts when NFC stays enabled",
                checked = settings.autoReminderEnabled,
                onCheckedChange = { viewModel.toggleAutoReminder() }
            )
            
            // Reminder Interval Selection
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
                onCheckedChange = { viewModel.toggleNotifications() }
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
            
            SettingsItem(
                title = "Dark Mode",
                subtitle = "Nothing OS dark theme",
                checked = settings.isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode() }
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
    
    // Show loading indicator
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

@Composable
private fun ReminderIntervalSelector(
    currentInterval: Int,
    onIntervalChange: (Int) -> Unit
) {
    val intervals = listOf(10, 30, 50) // Available intervals in seconds
    
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            intervals.forEach { interval ->
                val isSelected = currentInterval == interval
                
                FilterChip(
                    onClick = { onIntervalChange(interval) },
                    label = {
                        Text(
                            text = "${interval}s",
                            style = NothingTextStyles.ButtonText
                        )
                    },
                    selected = isSelected,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Current: ${currentInterval} seconds",
            style = NothingTextStyles.Caption,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
