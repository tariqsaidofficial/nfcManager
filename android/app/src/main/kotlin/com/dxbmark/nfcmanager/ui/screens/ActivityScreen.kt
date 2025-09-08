package com.dxbmark.nfcmanager.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Share // For Export Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dxbmark.nfcmanager.ui.theme.NothingTextStyles
import com.dxbmark.nfcmanager.viewmodel.ActivityViewModel
import com.dxbmark.nfcmanager.viewmodel.DateFilterOption
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = viewModel()
) {
    val filteredEvents by viewModel.filteredEvents.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val distinctEventTypes by viewModel.distinctEventTypes.collectAsState()
    val selectedEventType by viewModel.selectedEventType.collectAsState()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsState()

    // Collect the total unfiltered event count
    val totalUnfilteredEventCount by viewModel.totalUnfilteredEventCount.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            scope.launch {
                viewModel.exportEventsToCsv().collect { csvData ->
                    if (csvData.isNotBlank()) {
                        try {
                            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                                outputStream.write(csvData.toByteArray())
                            }
                            Toast.makeText(context, "Events exported successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: IOException) {
                            Toast.makeText(context, "Error exporting events: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "No events to export for the current filter", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVITY LOG",
                style = NothingTextStyles.HeaderTitle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EventTypeFilterDropdown(
                    distinctEventTypes = distinctEventTypes,
                    selectedEventType = selectedEventType,
                    onEventTypeSelected = { viewModel.setEventTypeFilter(it) },
                    modifier = Modifier.weight(1f)
                )
                DateFilterDropdown(
                    selectedOption = selectedDateFilter,
                    onOptionSelected = { viewModel.setDateFilter(it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    val fileName = "nfc_activity_log_${dateFormat.format(Date())}.csv"
                    if (filteredEvents.isNotEmpty()) { // Still good to check here to avoid unnecessary launcher.launch
                         createDocumentLauncher.launch(fileName)
                    } else {
                        Toast.makeText(context, "No events to export", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = filteredEvents.isNotEmpty()
            ) {
                Icon(Icons.Outlined.Share, contentDescription = "Export to CSV", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Export CSV")
            }

            OutlinedButton(
                onClick = { viewModel.clearAllEvents() },
                enabled = totalUnfilteredEventCount > 0, // Changed to enable if any event exists
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, if (totalUnfilteredEventCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Icon(Icons.Outlined.DeleteSweep, contentDescription = "Clear All Logs", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Clear All")
            }
        }

        val showingFilteredResults = selectedEventType != null ||
                                     selectedDateFilter != DateFilterOption.ALL_TIME ||
                                     viewModel.searchQuery.value.isNotBlank() || // Assuming searchQuery is public in VM
                                     viewModel.showImportantOnly.value // Assuming showImportantOnly is public in VM

        Text(
            text = if (showingFilteredResults) {
                "${filteredEvents.size} ${if (filteredEvents.size == 1) "event" else "events"} found"
            } else {
                "$totalUnfilteredEventCount ${if (totalUnfilteredEventCount == 1) "event" else "events"} recorded"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (filteredEvents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (showingFilteredResults) "No Matching Activity" else "No Activity Yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showingFilteredResults) "Try adjusting your filters." else "Activity logs will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredEvents, key = { it.id }) { event ->
                    EventItem(event = event)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTypeFilterDropdown(
    distinctEventTypes: List<String>,
    selectedEventType: String?,
    onEventTypeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val itemsWithAllOption = remember(distinctEventTypes) { listOf("All Types") + distinctEventTypes.sorted() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedEventType ?: "All Types",
            onValueChange = {},
            readOnly = true,
            label = { Text("Event Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = MaterialTheme.shapes.medium
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            itemsWithAllOption.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onEventTypeSelected(if (type == "All Types") null else type)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterDropdown(
    selectedOption: DateFilterOption,
    onOptionSelected: (DateFilterOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = remember { DateFilterOption.values().toList() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Date Range") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = MaterialTheme.shapes.medium
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}


@Composable
private fun EventItem(event: com.dxbmark.nfcmanager.data.database.entities.NFCEventEntity) {
    val eventTypeBackgroundColor = getEventTypeColor(event.eventType)
    val badgeTextColor = when (event.eventType) {
        "NFC_SCAN" -> MaterialTheme.colorScheme.onTertiaryContainer
        "SETTINGS" -> MaterialTheme.colorScheme.onSecondaryContainer
        "SYSTEM" -> MaterialTheme.colorScheme.onSurfaceVariant // Match text to card background for SYSTEM
        "NFC_STATE" -> MaterialTheme.colorScheme.onPrimaryContainer
        "PRIVACY" -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                tonalElevation = 1.dp
            ) {
                Text(
                    text = event.eventType.uppercase(Locale.getDefault()),
                    style = NothingTextStyles.Caption.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize, color = badgeTextColor),
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
        "NFC_STATE" -> MaterialTheme.colorScheme.primaryContainer
        "PRIVACY" -> MaterialTheme.colorScheme.errorContainer
        "SETTINGS" -> MaterialTheme.colorScheme.secondaryContainer
        "SYSTEM" -> MaterialTheme.colorScheme.surface // Keep SYSTEM badge subtle on surfaceVariant cards
        else -> MaterialTheme.colorScheme.outline
    }
}

private fun formatEventTime(timestamp: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy, HH:mm:ss", Locale.getDefault())
    return formatter.format(timestamp)
}
