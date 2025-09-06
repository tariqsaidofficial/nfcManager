package com.nothingos.nfcmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Import Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nothingos.nfcmanager.ui.theme.NothingTextStyles
import com.nothingos.nfcmanager.viewmodel.ActivityViewModel

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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "ACTIVITY LOG",
            style = NothingTextStyles.HeaderTitle,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = "${eventStatistics.totalEvents} events recorded",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Events List
        if (filteredEvents.isEmpty()) {
            // Empty State
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            // Events List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEvents) { event ->
                    EventItem(
                        event = event,
                        onDeleteClick = { viewModel.deleteEvent(event) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventItem(
    event: com.nothingos.nfcmanager.data.database.entities.NFCEventEntity,
    onDeleteClick: () -> Unit
) {
    val eventTypeBackgroundColor = getEventTypeColor(event.eventType)
    val badgeTextColor = when (event.eventType) {
        "SETTINGS" -> MaterialTheme.colorScheme.onSecondary // PureBlack for MediumGray background
        "SYSTEM" -> MaterialTheme.colorScheme.onTertiary   // PureBlack for LightGray background
        else -> MaterialTheme.colorScheme.onPrimary        // PureWhite for other backgrounds (Red, LightRed, DarkGray)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatEventTime(event.timestamp),
                    style = NothingTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Event Type Badge
            Surface(
                color = eventTypeBackgroundColor,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = event.eventType,
                    style = NothingTextStyles.Caption,
                    color = badgeTextColor, // Use adaptive text color
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun getEventTypeColor(eventType: String): Color {
    return when (eventType) {
        "NFC_STATE" -> MaterialTheme.colorScheme.primary
        "PRIVACY" -> MaterialTheme.colorScheme.error
        "SETTINGS" -> MaterialTheme.colorScheme.secondary
        "SYSTEM" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
}

private fun formatEventTime(timestamp: java.util.Date): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp.time
    val seconds = diff / 1000
    
    return when {
        seconds < 60 -> "now"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        else -> "${seconds / 86400}d ago"
    }
}
