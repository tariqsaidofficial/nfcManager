package com.nothingos.nfcmanager.ui.screens

import androidx.compose.foundation.BorderStroke // Correctly added import
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons // Added for icons
import androidx.compose.material.icons.outlined.DeleteSweep // Added for Clear All icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nothingos.nfcmanager.ui.theme.NothingTextStyles
import com.nothingos.nfcmanager.viewmodel.ActivityViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

/**
 * Activity Screen showing NFC event history
 * Built with Jetpack Compose and LazyColumn for performance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = viewModel()
) {
    val filteredEvents by viewModel.filteredEvents.collectAsState()
    val eventStatistics by viewModel.eventStatistics.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Handle success/error messages from ViewModel
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            // Optionally show a Snackbar or Toast here
            viewModel.clearMessages() // Clear message after handling
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            // Optionally show a Snackbar or Toast here
            viewModel.clearMessages() // Clear message after handling
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 0.dp) // Adjust padding
    ) {
        // Header Section with Title and Clear All Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVITY LOG",
                style = NothingTextStyles.HeaderTitle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            OutlinedButton(
                onClick = { viewModel.clearAllEvents() },
                enabled = eventStatistics.totalEvents > 0,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, if (eventStatistics.totalEvents > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Icon(Icons.Outlined.DeleteSweep, contentDescription = "Clear All Logs", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Clear All")
            }
        }

        Text(
            text = "${eventStatistics.totalEvents} events recorded",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Events List
        if (filteredEvents.isEmpty()) {
            // Empty State
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Activity Yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Activity logs will appear here when you interact with NFC features.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp) // Add padding for FAB or nav bar
            ) {
                items(filteredEvents, key = { it.id }) { event ->
                    EventItem(
                        event = event,
                        onDeleteClick = { viewModel.deleteEvent(event) } // This still uses individual delete
                    )
                }
            }
        }
    }
}

@Composable
private fun EventItem(
    event: com.nothingos.nfcmanager.data.database.entities.NFCEventEntity,
    onDeleteClick: () -> Unit // This can be removed if not used, or kept for swipe-to-delete later
) {
    val eventTypeBackgroundColor = getEventTypeColor(event.eventType)
    // Updated badgeTextColor logic as discussed
    val badgeTextColor = when (event.eventType) {
        "NFC_SCAN" -> MaterialTheme.colorScheme.onTertiaryContainer
        "SETTINGS" -> MaterialTheme.colorScheme.onSecondaryContainer // Use onSecondaryContainer with secondaryContainer
        "SYSTEM" -> MaterialTheme.colorScheme.onSurfaceVariant // Use onSurfaceVariant with surfaceVariant
        "NFC_STATE" -> MaterialTheme.colorScheme.onPrimaryContainer // Use onPrimaryContainer with primaryContainer
        "PRIVACY" -> MaterialTheme.colorScheme.onErrorContainer // Use onErrorContainer with errorContainer
        else -> MaterialTheme.colorScheme.onSurface // Fallback for other types or if outline is used
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // Slightly different from page background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatEventTime(event.timestamp),
                    style = NothingTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                color = eventTypeBackgroundColor,
                shape = MaterialTheme.shapes.small,
                tonalElevation = 2.dp // Add slight elevation to badge
            ) {
                Text(
                    text = event.eventType,
                    style = NothingTextStyles.Caption.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                    color = badgeTextColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun getEventTypeColor(eventType: String): Color {
    return when (eventType) {
        "NFC_SCAN" -> MaterialTheme.colorScheme.tertiaryContainer
        "NFC_STATE" -> MaterialTheme.colorScheme.primaryContainer // Using container colors for more subtlety
        "PRIVACY" -> MaterialTheme.colorScheme.errorContainer
        "SETTINGS" -> MaterialTheme.colorScheme.secondaryContainer
        "SYSTEM" -> MaterialTheme.colorScheme.surfaceVariant // Or a more distinct system color
        else -> MaterialTheme.colorScheme.outline // Fallback color
    }
}

private fun formatEventTime(timestamp: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy, HH:mm", Locale.getDefault())
    return formatter.format(timestamp)
}
