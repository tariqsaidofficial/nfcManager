package com.nothingos.nfcmanager.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons // Added for potential navigation icon
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight // Added for potential navigation icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController // <<< IMPORT NavController
import com.nothingos.nfcmanager.ui.components.AppRoutes // <<< IMPORT AppRoutes
import com.nothingos.nfcmanager.ui.theme.NothingColors
import com.nothingos.nfcmanager.ui.theme.NothingTextStyles
import com.nothingos.nfcmanager.ui.theme.NothingUIColors
import com.nothingos.nfcmanager.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    navController: NavController // <<< NEW PARAMETER
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isDarkTheme = settings.isDarkMode
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val nfcPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(context, "NFC permission granted. Please toggle monitoring again if needed.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "NFC permission denied. Background monitoring cannot be enabled.", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.requestNotificationPermissionFlow.collectLatest {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.requestNfcPermissionFlow.collectLatest {
            nfcPermissionLauncher.launch(Manifest.permission.NFC)
        }
    }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages() 
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages() 
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = NothingTextStyles.HeaderTitle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 16.dp)
        )

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

        SettingsSection(title = "NOTIFICATIONS & ALERTS") {
            SettingsItem(
                title = "Auto Reminder",
                subtitle = "Privacy alerts when NFC stays enabled",
                checked = settings.autoReminderEnabled,
                onCheckedChange = { viewModel.toggleAutoReminder() }
            )
            
            if (settings.autoReminderEnabled) {
                AdvancedIntervalSelector(
                    currentInterval = settings.reminderInterval,
                    onIntervalChange = { viewModel.updateReminderInterval(it) },
                    validateIntervalString = { viewModel.validateReminderInterval(it) }
                )
            }
            
            SettingsItem(
                title = "Show Notifications",
                subtitle = "Display all privacy and status notifications",
                checked = settings.showNotifications,
                onCheckedChange = { viewModel.toggleNotifications() }
            )
            
            SettingsItem(
                title = "Vibration for Alerts",
                subtitle = "Haptic feedback for privacy alerts",
                checked = settings.vibrationEnabled,
                onCheckedChange = { viewModel.toggleVibration() }
            )

            SettingsItem(
                title = "Enable Sounds",
                subtitle = "Play sounds for alerts and notifications",
                checked = settings.soundEnabled, 
                onCheckedChange = { viewModel.toggleSound() }
            )

            SettingsItemClickable(
                title = "Notification Sound",
                subtitle = "Customize the specific sound for notifications",
                enabled = settings.soundEnabled, // <-- Updated: Use enabled state
                onClick = {
                    if (settings.soundEnabled) { // <-- Updated: Navigate only if enabled
                        navController.navigate(AppRoutes.NOTIFICATION_SOUND_SETTINGS)
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
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

            SettingsItem(
                title = "Background NFC Monitoring",
                subtitle = "Continuously monitors NFC status. Requires NFC permission.",
                checked = settings.backgroundServiceMonitoringEnabled,
                onCheckedChange = { viewModel.toggleBackgroundServiceMonitoring() } 
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        SettingsSection(title = "PERFORMANCE") {
            SettingsItem(
                title = "Battery Optimization",
                subtitle = "Optimize for better battery life",
                checked = settings.batteryOptimized,
                onCheckedChange = { viewModel.toggleBatteryOptimization() }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                // Use a consistent background for all section cards, or differentiate as needed
                containerColor = MaterialTheme.colorScheme.surface 
            )
        ) {
            Column {
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
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingsItemClickable(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true // <-- Updated: Added enabled parameter
) {
    val alpha = if (enabled) 1f else 0.5f // Updated: For visual feedback of disabled state
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = enabled) // <-- Updated: Use enabled parameter
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .alpha(alpha), // <-- Updated: Apply alpha for visual feedback
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha) // <-- Updated: Apply alpha
            )
            Text(
                text = subtitle,
                style = NothingTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.6f else 0.3f) // <-- Updated: Apply alpha
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.6f else 0.3f) // <-- Updated: Apply alpha
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedIntervalSelector(
    currentInterval: Int, 
    onIntervalChange: (Int) -> Unit, 
    validateIntervalString: (String) -> Boolean 
) {
    val predefinedIntervals = listOf(5, 10, 15, 20, 30, 40, 50, 60)
    val haptic = LocalHapticFeedback.current

    var textFieldValue by remember(currentInterval) { 
        mutableStateOf(currentInterval.toString())
    }
    var isCustomChipActive by remember(currentInterval) { 
        mutableStateOf(!predefinedIntervals.contains(currentInterval)) 
    }
    var textFieldHasError by remember(currentInterval, textFieldValue, isCustomChipActive) { 
        mutableStateOf(
            if (isCustomChipActive) !validateIntervalString(textFieldValue) && textFieldValue.isNotBlank()
            else false
        )
    }
    
    LaunchedEffect(currentInterval) {
        val isPreset = predefinedIntervals.contains(currentInterval)
        if (isCustomChipActive && isPreset) {
            // If custom was active, but currentInterval is now a preset (e.g., due to reset or invalid custom input being reverted)
            isCustomChipActive = false
        } else if (!isCustomChipActive && !isPreset) {
            // If custom was not active, but currentInterval is now custom
            isCustomChipActive = true
            textFieldValue = currentInterval.toString()
        } else if (isCustomChipActive && !isPreset && textFieldValue != currentInterval.toString()) {
            // If custom is active, currentInterval is custom, but textField doesn't match (e.g. ViewModel corrected it)
            textFieldValue = currentInterval.toString()
        }
    }

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
        
        predefinedIntervals.chunked(4).forEach { rowIntervals ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowIntervals.forEach { interval ->
                    val isSelected = !isCustomChipActive && currentInterval == interval
                    val scale by animateFloatAsState(targetValue = if (isSelected) 1.05f else 1.0f, label = "chipScale_${interval}")
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isCustomChipActive = false 
                            textFieldHasError = false 
                            onIntervalChange(interval)
                        },
                        label = { Text("${interval}s", style = NothingTextStyles.ButtonText) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant, 
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).scale(scale)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        FilterChip(
            selected = isCustomChipActive,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (!isCustomChipActive) { // If it wasn't active, activate it
                  isCustomChipActive = true
                  // Try to parse current text field value, if valid, use it, else keep current interval
                  val currentTextAsInt = textFieldValue.toIntOrNull()
                  if (currentTextAsInt != null && validateIntervalString(textFieldValue)) {
                      if (currentInterval != currentTextAsInt) onIntervalChange(currentTextAsInt)
                      textFieldHasError = false
                  } else {
                      // If text field is invalid, error will be shown. Keep current settings interval.
                      textFieldHasError = textFieldValue.isNotBlank() // mark error if not blank and invalid
                  }
                } // If already active, clicking it again does nothing to selection state here.
            },
            label = { Text("Custom", style = NothingTextStyles.ButtonText) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (isCustomChipActive) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newText ->
                    textFieldValue = newText
                    val numericValue = newText.toIntOrNull()
                    if (numericValue != null) {
                        if(validateIntervalString(newText)){
                            onIntervalChange(numericValue)
                            textFieldHasError = false
                        } else {
                            textFieldHasError = true 
                        }
                    } else {
                        textFieldHasError = newText.isNotBlank() 
                    }
                },
                label = { Text("Custom Interval (5-300 seconds)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = textFieldHasError,
                supportingText = {
                    if (textFieldHasError && textFieldValue.isNotBlank()) {
                        Text("Interval must be a number between 5-300.")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Current: ${currentInterval}s",
            style = NothingTextStyles.Caption,
            color = if (textFieldHasError && isCustomChipActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
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
