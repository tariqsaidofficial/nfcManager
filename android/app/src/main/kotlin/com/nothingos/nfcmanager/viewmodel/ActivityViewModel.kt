package com.nothingos.nfcmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingos.nfcmanager.data.database.entities.NFCEventEntity
import com.nothingos.nfcmanager.data.repository.NFCRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// Enum for Date Filter Options
enum class DateFilterOption(val displayName: String) {
    TODAY("Today"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    ALL_TIME("All Time")
}

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val repository: NFCRepository
) : ViewModel() {

    // ==================== UI STATE ===================

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    // ==================== SEARCH AND FILTER ===================

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedEventType = MutableStateFlow<String?>(null)
    val selectedEventType: StateFlow<String?> = _selectedEventType.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow(DateFilterOption.ALL_TIME)
    val selectedDateFilter: StateFlow<DateFilterOption> = _selectedDateFilter.asStateFlow()

    private val _showImportantOnly = MutableStateFlow(false)
    val showImportantOnly: StateFlow<Boolean> = _showImportantOnly.asStateFlow()

    val distinctEventTypes: StateFlow<List<String>> = repository.getDistinctEventTypes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // ==================== DATA STREAMS ===================

    private val allEventsFlow: Flow<List<NFCEventEntity>> = repository.getAllEvents() // Private base flow

    // New public StateFlow for the total unfiltered event count
    val totalUnfilteredEventCount: StateFlow<Int> = allEventsFlow
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )

    val filteredEvents: StateFlow<List<NFCEventEntity>> = combine(
        allEventsFlow, // Use the private base flow here
        searchQuery,
        selectedEventType,
        selectedDateFilter,
        showImportantOnly
    ) { events, query, eventType, dateFilter, importantOnly ->
        var currentFilteredEvents = events

        // Apply Date Filter
        val calendar = Calendar.getInstance()
        currentFilteredEvents = when (dateFilter) {
            DateFilterOption.TODAY -> currentFilteredEvents.filter { isSameDay(it.timestamp, calendar.time) }
            DateFilterOption.LAST_7_DAYS -> {
                val sevenDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7); clearTime() }.time
                currentFilteredEvents.filter { it.timestamp.after(sevenDaysAgo) || isSameDay(it.timestamp, sevenDaysAgo) }
            }
            DateFilterOption.LAST_30_DAYS -> {
                val thirtyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30); clearTime() }.time
                currentFilteredEvents.filter { it.timestamp.after(thirtyDaysAgo) || isSameDay(it.timestamp, thirtyDaysAgo) }
            }
            DateFilterOption.ALL_TIME -> currentFilteredEvents
        }

        // Apply Search Query Filter
        if (query.isNotBlank()) {
            currentFilteredEvents = currentFilteredEvents.filter { event ->
                event.message.contains(query, ignoreCase = true) ||
                event.eventType.contains(query, ignoreCase = true) ||
                event.tagId?.contains(query, ignoreCase = true) == true
            }
        }

        // Apply Event Type Filter
        eventType?.let { type ->
            currentFilteredEvents = currentFilteredEvents.filter { it.eventType == type }
        }

        // Apply Important Only Filter
        if (importantOnly) {
            currentFilteredEvents = currentFilteredEvents.filter { it.isImportant }
        }

        currentFilteredEvents
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    val eventStatistics: StateFlow<EventStatistics> = filteredEvents.map { events ->
        EventStatistics(
            totalEvents = events.size,
            todayEvents = events.count { isSameDay(it.timestamp, Calendar.getInstance().time) },
            importantEvents = events.count { it.isImportant },
            eventsByType = events.groupBy { it.eventType }.mapValues { it.value.size },
            recentActivity = events.take(5)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = EventStatistics()
    )

    // ==================== FILTER UPDATE FUNCTIONS ====================

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        updateSearchQuery("")
    }

    fun setEventTypeFilter(eventType: String?) {
        _selectedEventType.value = eventType
    }

    fun setDateFilter(option: DateFilterOption) {
        _selectedDateFilter.value = option
    }

    fun toggleImportantOnly() {
        _showImportantOnly.value = !_showImportantOnly.value
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedEventType.value = null
        _selectedDateFilter.value = DateFilterOption.ALL_TIME
        _showImportantOnly.value = false
    }

    // ==================== EVENT MANAGEMENT ====================
    fun deleteEvent(event: NFCEventEntity) {
        viewModelScope.launch {
            try {
                repository.deleteEvent(event)
                updateSuccess("Event deleted successfully")
            } catch (e: Exception) {
                updateError("Failed to delete event: ${e.message}")
            }
        }
    }

    fun toggleEventImportance(event: NFCEventEntity) {
        viewModelScope.launch {
            try {
                // This requires a proper update function in DAO and Repository
                // For example: repository.updateEvent(event.copy(isImportant = !event.isImportant))
                // The DAO should have an @Update method.
                // For now, this is a placeholder if the update mechanism isn't fully in place.
                // val updatedEvent = event.copy(isImportant = !event.isImportant)
                // repository.updateEvent(updatedEvent) // Ideal implementation
                updateSuccess("Toggle importance for event ID ${event.id}. (Actual update TBD)")
            } catch (e: Exception) {
                updateError("Failed to update event importance: ${e.message}")
            }
        }
    }

    fun exportEventsToCsv(): Flow<String> = flow {
        val eventsToExport = filteredEvents.first() // Get current filtered list once
        if (eventsToExport.isEmpty()) {
            emit("") // Emit empty string if no events
            return@flow
        }
        val header = "ID,Timestamp,EventType,Message,TagID,TagType,Data,AppPackage,IsImportant,IsBlocked"
        val csvData = StringBuilder()
        csvData.appendLine(header)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        eventsToExport.forEach { event ->
            csvData.appendLine(
                listOfNotNull(
                    event.id.toString(),
                    dateFormat.format(event.timestamp),
                    event.eventType,
                    "\"${event.message.replace("\"", "\"\"")}\"", // Escape quotes in message
                    event.tagId,
                    event.tagType,
                    event.data,
                    event.appPackage,
                    event.isImportant.toString(),
                    event.isBlocked.toString()
                ).joinToString(",")
            )
        }
        emit(csvData.toString())
    }


    fun clearAllEvents() {
        viewModelScope.launch {
            try {
                repository.clearAllEvents()
                updateSuccess("All events cleared")
            } catch (e: Exception) {
                updateError("Failed to clear events: ${e.message}")
            }
        }
    }

    fun cleanupOldEvents(daysToKeep: Int = 30) {
        viewModelScope.launch {
            try {
                repository.cleanupOldEvents(daysToKeep)
                updateSuccess("Old events cleaned up")
            } catch (e: Exception) {
                updateError("Failed to cleanup events: ${e.message}")
            }
        }
    }
    // ==================== UI STATE HELPERS ====================
    fun updateLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }

    private fun updateError(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            isLoading = false
        )
    }

    private fun updateSuccess(message: String) {
        _uiState.value = _uiState.value.copy(
            successMessage = message,
            isLoading = false
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun toggleSelectionMode() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = !_uiState.value.isSelectionMode,
            selectedEvents = if (_uiState.value.isSelectionMode) emptySet() else _uiState.value.selectedEvents
        )
    }

    fun toggleEventSelection(eventId: Long) {
        val currentSelected = _uiState.value.selectedEvents
        val newSelected = if (currentSelected.contains(eventId)) {
            currentSelected - eventId
        } else {
            currentSelected + eventId
        }
        _uiState.value = _uiState.value.copy(selectedEvents = newSelected)
    }

    fun selectAllEvents() {
        val visibleEventIds = filteredEvents.value.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedEvents = visibleEventIds)
    }

    fun clearSelections() {
        _uiState.value = _uiState.value.copy(selectedEvents = emptySet())
    }
    // ==================== UTILITY METHODS ====================
    private fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1; clearTime() }
        val cal2 = Calendar.getInstance().apply { time = date2; clearTime() }
        return cal1.timeInMillis == cal2.timeInMillis
    }

    // isToday is a specific case of isSameDay
    private fun isToday(date: Date): Boolean {
        return isSameDay(date, Calendar.getInstance().time)
    }

    fun formatRelativeTime(date: java.util.Date): String {
        val seconds = (System.currentTimeMillis() - date.time) / 1000
        return when {
            seconds < 60 -> "now"
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86400 -> "${seconds / 3600}h ago"
            else -> "${seconds / 86400}d ago"
        }
    }
}

data class ActivityUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSelectionMode: Boolean = false,
    val selectedEvents: Set<Long> = emptySet()
)

data class EventStatistics(
    val totalEvents: Int = 0,
    val todayEvents: Int = 0,
    val importantEvents: Int = 0,
    val eventsByType: Map<String, Int> = emptyMap(),
    val recentActivity: List<NFCEventEntity> = emptyList()
)
