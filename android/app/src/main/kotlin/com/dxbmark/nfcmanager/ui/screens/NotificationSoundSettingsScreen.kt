package com.dxbmark.nfcmanager.ui.screens

import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow // Or VolumeUp
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSoundSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val currentSoundUriString = settings.customNotificationSoundUri

    var currentRingtone by remember { mutableStateOf<Ringtone?>(null) }
    var isPlayingSound by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Stop sound when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            currentRingtone?.stop()
            isPlayingSound = false
        }
    }

    // Observe success/error messages from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearMessages()
        }
    }

    val pickSoundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                currentRingtone?.stop() // Stop any currently playing sound before changing
                isPlayingSound = false
                try {
                    // Try to get persistent permission, but don't fail if not available
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    try {
                        context.contentResolver.takePersistableUriPermission(it, takeFlags)
                    } catch (e: SecurityException) {
                        // Some content providers don't support persistent permissions
                        // This is OK - we'll still save the URI and it will work for this session
                    }
                    viewModel.updateCustomNotificationSound(it.toString())
                } catch (e: Exception) {
                    Toast.makeText(
                        context, 
                        context.getString(R.string.sound_selection_failed, e.localizedMessage ?: "Unknown error"), 
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    // No permission needed - GetContent() uses system picker with automatic permissions

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification_sound_screen_title), style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = {
                        currentRingtone?.stop()
                        isPlayingSound = false
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.notification_sound_description),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SoundSettingItem(
                title = stringResource(R.string.current_sound_label),
                soundName = getSoundName(context, currentSoundUriString),
                isPlaying = isPlayingSound,
                onClick = {
                    if (isPlayingSound) {
                        currentRingtone?.stop()
                        isPlayingSound = false
                    } else {
                        currentRingtone?.stop() // Stop previous instance if any
                        try {
                            val soundUri = currentSoundUriString?.let { Uri.parse(it) } 
                                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            
                            if (soundUri == null) {
                                 Toast.makeText(context, context.getString(R.string.sound_play_error_null), Toast.LENGTH_SHORT).show()
                                 return@SoundSettingItem
                            }

                            currentRingtone = RingtoneManager.getRingtone(context, soundUri)
                            currentRingtone?.let {
                                it.play()
                                isPlayingSound = true
                                // RingtoneManager doesn't easily provide completion listener to reset isPlayingSound
                                // For simplicity, user has to tap again to stop or play a new sound.
                            } ?: run {
                                Toast.makeText(context, context.getString(R.string.sound_play_error_corrupted), Toast.LENGTH_SHORT).show()
                                isPlayingSound = false
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.sound_play_error_general, e.localizedMessage ?: "Unknown error"), Toast.LENGTH_SHORT).show()
                            isPlayingSound = false
                        }
                    }
                }
            )

            Button(
                onClick = {
                    currentRingtone?.stop()
                    isPlayingSound = false
                    // No permission needed - GetContent() handles it automatically
                    pickSoundLauncher.launch("audio/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.MusicNote, contentDescription = stringResource(R.string.pick_sound_icon), modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.choose_custom_sound))
            }

            Button(
                onClick = {
                    currentRingtone?.stop()
                    isPlayingSound = false
                    viewModel.updateCustomNotificationSound(null) // Reset to default
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentSoundUriString != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ),
                enabled = currentSoundUriString != null // Disable if already default
            ) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = stringResource(R.string.default_sound_icon), modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.use_system_default_sound))
            }
        }
    }
}

@Composable
fun SoundSettingItem(
    title: String, 
    soundName: String, 
    isPlaying: Boolean, // Added to potentially change UI if needed, e.g., icon
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPlaying) {
                    // Optional: You can add a visual indicator like a small animated equalizer or a different icon
                    // For now, keeping it simple as per user request.
                     Icon(
                        Icons.Filled.PlayArrow, // Using PlayArrow to indicate it IS playing, could be Stop icon too
                        contentDescription = stringResource(R.string.sound_playing_indicator), 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp).padding(end = 4.dp)
                    )
                }
                Text(soundName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

fun getSoundName(context: Context, uriString: String?): String {
    if (uriString == null) {
        return context.getString(R.string.system_default_sound)
    }
    return try {
        val uri = Uri.parse(uriString)
        RingtoneManager.getRingtone(context, uri)?.getTitle(context)?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment?.takeIf { it.isNotBlank() } 
            ?: context.getString(R.string.custom_sound_label)
    } catch (e: Exception) {
        context.getString(R.string.custom_sound_error)
    }
}
