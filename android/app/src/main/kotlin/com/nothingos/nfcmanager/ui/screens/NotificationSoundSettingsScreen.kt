package com.nothingos.nfcmanager.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nothingos.nfcmanager.viewmodel.SettingsViewModel
import com.nothingos.nfcmanager.ui.theme.NothingTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSoundSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val currentSoundUriString = settings.customNotificationSoundUri

    // Launcher for picking a custom sound
    val pickSoundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Persist permission for this URI for long-term access
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(it, takeFlags)
                    viewModel.updateCustomNotificationSound(it.toString())
                } catch (e: SecurityException) {
                    // Handle cases where persistable URI permission cannot be taken (e.g., non-document Uris from some pickers)
                    // For now, just save it, but be aware it might not be accessible later if not persistable.
                    viewModel.updateCustomNotificationSound(it.toString())
                    // Optionally show a toast about potential access issues if not persistable.
                }
            }
        }
    )

    // Launcher for requesting storage permission
    val requestStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                pickSoundLauncher.launch("audio/*")
            } else {
                // Inform user that permission is needed
                // You can show a Snackbar or Toast here
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.requestStoragePermissionFlow.collect {
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
                    IconButton(onClick = onNavigateBack) {
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
                onClick = { /* Maybe play the sound or open system settings if default? For now, just displays. */ }
            )

            Button(
                onClick = {
                    if (viewModel.requestStoragePermissionForSoundPicker()) { // Checks if permission already granted
                        pickSoundLauncher.launch("audio/*")
                    }
                    // If false, the LaunchedEffect for requestStoragePermissionFlow will trigger the permission request.
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.MusicNote, contentDescription = "Pick Sound", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Choose Custom Sound")
            }

            Button(
                onClick = {
                    viewModel.updateCustomNotificationSound(null) // Reset to default
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = "Default Sound", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Use System Default Sound")
            }
        }
    }
}

@Composable
fun SoundSettingItem(title: String, soundName: String, onClick: () -> Unit) {
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
            Text(soundName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// Helper function to get a displayable name from a URI string
fun getSoundName(context: Context, uriString: String?): String {
    if (uriString == null) {
        return "System Default"
    }
    return try {
        val uri = Uri.parse(uriString)
        // Try to get actual ringtone title
        RingtoneManager.getRingtone(context, uri)?.getTitle(context) 
            ?: uri.lastPathSegment // Fallback to last path segment if title not found
            ?: "Custom Sound" // Ultimate fallback
    } catch (e: Exception) {
        "Custom Sound (Error)"
    }
}
