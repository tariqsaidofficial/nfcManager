package com.dxbmark.nfcmanager.ui.screens

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.res.stringResource
import com.dxbmark.nfcmanager.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dxbmark.nfcmanager.ui.components.AppRoutes
import com.dxbmark.nfcmanager.ui.theme.NothingColors
import com.dxbmark.nfcmanager.ui.theme.NothingTextStyles
import com.dxbmark.nfcmanager.ui.theme.NothingUIColors
import com.dxbmark.nfcmanager.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

// Data class for language options
data class LanguageOption(val code: String?, val nameResId: Int, val isEnabled: Boolean = true)

val availableLanguages = listOf(
    LanguageOption(null, R.string.language_system_default),
    LanguageOption("en", R.string.language_english),
    LanguageOption("ar", R.string.language_arabic),
    LanguageOption("es", R.string.language_spanish),
    LanguageOption("fr", R.string.language_french),
    LanguageOption("de", R.string.language_german, isEnabled = true), // German added and enabled
    LanguageOption("ru", R.string.language_russian, isEnabled = true),
    LanguageOption("zh", R.string.language_chinese_mandarin, isEnabled = true),
    LanguageOption("hi", R.string.language_hindi, isEnabled = true),
    LanguageOption("fil", R.string.language_filipino, isEnabled = true)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    navController: NavController
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isDarkTheme = settings.isDarkMode
    val context = LocalContext.current

    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentLanguageCode = settings.selectedLanguageCode

    val toastNotificationPermGranted = stringResource(R.string.settings_toast_notification_perm_granted)
    val toastNotificationPermDenied = stringResource(R.string.settings_toast_notification_perm_denied)
    val toastNfcPermGranted = stringResource(R.string.settings_toast_nfc_perm_granted)
    val toastNfcPermDenied = stringResource(R.string.settings_toast_nfc_perm_denied)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(context, toastNotificationPermGranted, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, toastNotificationPermDenied, Toast.LENGTH_SHORT).show()
            }
        }
    )

    val nfcPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(context, toastNfcPermGranted, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, toastNfcPermDenied, Toast.LENGTH_LONG).show()
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
            text = stringResource(R.string.settings_screen_header_title),
            style = NothingTextStyles.HeaderTitle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        SettingsSection(title = stringResource(R.string.settings_section_display_title)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) NothingColors.DarkSurface else NothingUIColors.LightCardBackground
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_item_theme_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isDarkTheme) NothingColors.PureWhite else NothingColors.LightPrimaryText,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp)
                    )
                    ThemeOption(
                        title = stringResource(R.string.settings_item_dark_mode_title),
                        subtitle = stringResource(R.string.settings_item_dark_mode_subtitle),
                        isSelected = isDarkTheme,
                        onClick = { viewModel.setTheme(true) },
                        isDarkTheme = isDarkTheme
                    )
                    ThemeOption(
                        title = stringResource(R.string.settings_item_light_mode_title),
                        subtitle = stringResource(R.string.settings_item_light_mode_subtitle),
                        isSelected = !isDarkTheme,
                        onClick = { viewModel.setTheme(false) },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Language Section - Moved to its own card
        SettingsSection(title = stringResource(R.string.settings_section_language_region_title)) {
             Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) NothingColors.DarkSurface else NothingUIColors.LightCardBackground
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) { // Padding for items inside card
                     LanguageSettingItem(
                        currentLanguageCode = currentLanguageCode,
                        onClick = { showLanguageDialog = true },
                        modifier = Modifier.padding(vertical = 8.dp) // Adjusted padding for consistency
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = stringResource(R.string.settings_section_notifications_alerts_title)) {
            // Card wrapping notification and alert settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) NothingColors.DarkSurface else NothingUIColors.LightCardBackground
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SettingsItem(
                        title = stringResource(R.string.settings_item_auto_reminder_title),
                        subtitle = stringResource(R.string.settings_item_auto_reminder_subtitle),
                        checked = settings.autoReminderEnabled,
                        onCheckedChange = { viewModel.toggleAutoReminder() },
                        modifier = Modifier.padding(top = 8.dp) // Add padding if first item in card
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp))

                    if (settings.autoReminderEnabled) {
                        AdvancedIntervalSelector(
                            currentInterval = settings.reminderInterval,
                            onIntervalChange = { viewModel.updateReminderInterval(it) },
                            validateIntervalString = { viewModel.validateReminderInterval(it) },
                            modifier = Modifier.padding(horizontal = 0.dp) // Remove AdvancedIntervalSelector's own horizontal padding
                        )
                        Divider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp))
                    }

                    SettingsItem(
                        title = stringResource(R.string.settings_item_show_notifications_title),
                        subtitle = stringResource(R.string.settings_item_show_notifications_subtitle),
                        checked = settings.showNotifications,
                        onCheckedChange = { viewModel.toggleNotifications() }
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp))

                    SettingsItem(
                        title = stringResource(R.string.settings_item_vibration_alerts_title),
                        subtitle = stringResource(R.string.settings_item_vibration_alerts_subtitle),
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { viewModel.toggleVibration() }
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp))

                    SettingsItem(
                        title = stringResource(R.string.settings_item_enable_sounds_title),
                        subtitle = stringResource(R.string.settings_item_enable_sounds_subtitle),
                        checked = settings.soundEnabled,
                        onCheckedChange = { viewModel.toggleSound() }
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp))

                    SettingsItemClickable(
                        title = stringResource(R.string.settings_item_notification_sound_title),
                        subtitle = stringResource(R.string.settings_item_notification_sound_subtitle),
                        enabled = settings.soundEnabled,
                        onClick = {
                            if (settings.soundEnabled) {
                                navController.navigate(AppRoutes.NOTIFICATION_SOUND_SETTINGS)
                            }
                        },
                        modifier = Modifier.padding(bottom = 8.dp) // Add padding if last item in card
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = stringResource(R.string.settings_section_privacy_security_title)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) NothingColors.DarkSurface else NothingUIColors.LightCardBackground
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SettingsItem(
                        title = stringResource(R.string.settings_item_privacy_mode_title),
                        subtitle = stringResource(R.string.settings_item_privacy_mode_subtitle),
                        checked = settings.isPrivacyModeEnabled,
                        onCheckedChange = { viewModel.togglePrivacyMode() },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp))

                    SettingsItem(
                        title = stringResource(R.string.settings_item_block_unknown_tags_title),
                        subtitle = stringResource(R.string.settings_item_block_unknown_tags_subtitle),
                        checked = settings.blockUnknownTags,
                        onCheckedChange = { viewModel.toggleBlockUnknownTags() }
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp))

                    SettingsItem(
                        title = stringResource(R.string.settings_item_background_monitoring_title),
                        subtitle = stringResource(R.string.settings_item_background_monitoring_subtitle),
                        checked = settings.backgroundServiceMonitoringEnabled,
                        onCheckedChange = { viewModel.toggleBackgroundServiceMonitoring() },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = stringResource(R.string.settings_section_performance_title)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) NothingColors.DarkSurface else NothingUIColors.LightCardBackground
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SettingsItem(
                        title = stringResource(R.string.settings_item_battery_optimization_title),
                        subtitle = stringResource(R.string.settings_item_battery_optimization_subtitle),
                        checked = settings.batteryOptimized,
                        onCheckedChange = { viewModel.toggleBatteryOptimization() },
                        modifier = Modifier.padding(vertical = 8.dp) // Padding for single item in card
                    )
                }
            }
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
                    text = stringResource(R.string.settings_button_export_settings),
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
                    text = stringResource(R.string.settings_button_reset_defaults),
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
                    text = stringResource(R.string.settings_card_design_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.settings_card_design_info_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            showDialog = { showLanguageDialog = it },
            currentLanguageCode = currentLanguageCode,
            onLanguageSelected = { code ->
                viewModel.updateSelectedLanguage(code)
            }
        )
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
        Column {
            content()
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
            //.padding(horizontal = 20.dp, vertical = 12.dp), // Padding applied by card's column or specific modifier
            .padding(vertical = 12.dp, horizontal = 4.dp), // Reduced horizontal for item itself
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
    enabled: Boolean = true,
    showArrow: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.5f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = enabled)
            .padding(vertical = 12.dp, horizontal = 4.dp) // Adjusted padding
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            Text(
                text = subtitle,
                style = NothingTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.6f else 0.3f)
            )
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.settings_cd_navigate),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.6f else 0.3f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedIntervalSelector(
    currentInterval: Int,
    onIntervalChange: (Int) -> Unit,
    validateIntervalString: (String) -> Boolean,
    modifier: Modifier = Modifier // Added modifier to be passed down
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
            isCustomChipActive = false
        } else if (!isCustomChipActive && !isPreset) {
            isCustomChipActive = true
            textFieldValue = currentInterval.toString()
        } else if (isCustomChipActive && !isPreset && textFieldValue != currentInterval.toString()) {
            textFieldValue = currentInterval.toString()
        }
    }

    Column(
        modifier = modifier // Apply the modifier here
            .fillMaxWidth()
            //.padding(horizontal = 20.dp, vertical = 12.dp) // Removed padding, will be handled by parent card
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_item_reminder_interval_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(R.string.settings_item_reminder_interval_subtitle),
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
                        label = { Text(stringResource(R.string.interval_seconds_format, interval), style = NothingTextStyles.ButtonText) },
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
                if (!isCustomChipActive) {
                  isCustomChipActive = true
                  val currentTextAsInt = textFieldValue.toIntOrNull()
                  if (currentTextAsInt != null && validateIntervalString(textFieldValue)) {
                      if (currentInterval != currentTextAsInt) onIntervalChange(currentTextAsInt)
                      textFieldHasError = false
                  } else {
                      textFieldHasError = textFieldValue.isNotBlank()
                  }
                }
            },
            label = { Text(stringResource(R.string.custom_interval), style = NothingTextStyles.ButtonText) },
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
                label = { Text(stringResource(R.string.settings_custom_interval_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = textFieldHasError,
                supportingText = {
                    if (textFieldHasError && textFieldValue.isNotBlank()) {
                        Text(stringResource(R.string.settings_error_interval_range))
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
            text = stringResource(R.string.settings_item_reminder_interval_current_label, currentInterval),
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
    isDarkTheme: Boolean // To style the radio button itself based on main theme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp), // Consistent padding
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

@Composable
fun getLanguageDisplayName(languageCode: String?, context: Context): String {
    return availableLanguages.firstOrNull { it.code == languageCode }?.let {
        context.getString(it.nameResId)
    } ?: context.getString(R.string.language_system_default)
}

@Composable
fun LanguageSettingItem(
    currentLanguageCode: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguageDisplayName = getLanguageDisplayName(currentLanguageCode, context)

    SettingsItemClickable(
        title = stringResource(R.string.settings_language_title),
        subtitle = currentLanguageDisplayName,
        onClick = onClick,
        modifier = modifier // Padding now handled by parent card or specific call site
    )
}

@Composable
fun LanguageSelectionDialog(
    showDialog: (Boolean) -> Unit,
    currentLanguageCode: String?,
    onLanguageSelected: (String?) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val disabledAlpha = 0.38f // Material 3 standard alpha for disabled content

    AlertDialog(
        onDismissRequest = { showDialog(false) },
        title = { Text(stringResource(R.string.dialog_select_language_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) { // Make the language list scrollable
                availableLanguages.forEach { langOption ->
                    val itemAlpha = if (langOption.isEnabled) 1f else 0.5f
                    val textColor = if (langOption.isEnabled) MaterialTheme.colorScheme.onSurface 
                                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = langOption.isEnabled) {
                                if (langOption.isEnabled) {
                                    onLanguageSelected(langOption.code)
                                    showDialog(false)
                                }
                            }
                            .padding(vertical = 12.dp)
                            .alpha(itemAlpha),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguageCode == langOption.code,
                            onClick = if (langOption.isEnabled) {
                                {
                                    onLanguageSelected(langOption.code)
                                    showDialog(false)
                                }
                            } else null, // Disable RadioButton click if not enabled
                            enabled = langOption.isEnabled,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = if (langOption.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                disabledSelectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha),
                                disabledUnselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(context.getString(langOption.nameResId), color = textColor)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { showDialog(false) }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
