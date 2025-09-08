package com.dxbmark.nfcmanager.ui.screens

import android.Manifest
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    // Stop sound when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            currentRingtone?.stop()
            isPlayingSound = false
        }
    }

    val pickSoundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                currentRingtone?.stop() // Stop any currently playing sound before changing
                isPlayingSound = false
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(it, takeFlags)
                    viewModel.updateCustomNotificationSound(it.toString())
                } catch (e: SecurityException) {
                    viewModel.updateCustomNotificationSound(it.toString())
                    Toast.makeText(context, "Could not persist sound access, it might not work later.", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    val requestStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                pickSoundLauncher.launch("audio/*")
            } else {
                Toast.makeText(context, "Storage permission is required to choose a custom sound.", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.requestStoragePermissionFlow.collectLatest {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            requestStoragePermissionLauncher.launch(permission)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Sound", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = {
                        currentRingtone?.stop()
                        isPlayingSound = false
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
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
                text = "Customize the sound played for NFC Manager notifications. Choose a custom sound from your device or use the system default.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SoundSettingItem(
                title = "Current Sound",
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
                                 Toast.makeText(context, "Cannot play sound: Default URI is null.", Toast.LENGTH_SHORT).show()
                                 return@SoundSettingItem
                            }

                            currentRingtone = RingtoneManager.getRingtone(context, soundUri)
                            currentRingtone?.let {
                                it.play()
                                isPlayingSound = true
                                // RingtoneManager doesn't easily provide completion listener to reset isPlayingSound
                                // For simplicity, user has to tap again to stop or play a new sound.
                            } ?: run {
                                Toast.makeText(context, "Cannot play sound: Ringtone not found or corrupted.", Toast.LENGTH_SHORT).show()
                                isPlayingSound = false
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error playing sound: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            isPlayingSound = false
                        }
                    }
                }
            )

            Button(
                onClick = {
                    currentRingtone?.stop()
                    isPlayingSound = false
                    if (viewModel.requestStoragePermissionForSoundPicker()) { // Checks if permission already granted
                        pickSoundLauncher.launch("audio/*")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.MusicNote, contentDescription = "Pick Sound", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Choose Custom Sound")
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
                Icon(Icons.Outlined.NotificationsActive, contentDescription = "Default Sound", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Use System Default Sound")
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
                        contentDescription = "Playing", 
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
        return "System Default"
    }
    return try {
        val uri = Uri.parse(uriString)
        RingtoneManager.getRingtone(context, uri)?.getTitle(context)?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment?.takeIf { it.isNotBlank() } 
            ?: "Custom Sound"
    } catch (e: Exception) {
        "Custom Sound (Error)"
    }
}
