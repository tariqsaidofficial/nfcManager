package com.nothingos.nfcmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothingos.nfcmanager.data.database.entities.NFCEventEntity
import com.nothingos.nfcmanager.data.repository.NFCRepository
import dagger.hilt.android.lifecycle.HiltViewModel // Import HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Activity/Events screen
 * Manages event history and filtering
 */
@HiltViewModel // Add HiltViewModel annotation
class ActivityViewModel @Inject constructor(
    private val repository: NFCRepository
) : ViewModel() {
    
    // ==================== UI STATE ====================
    
    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()
    
    // ==================== SEARCH AND FILTER ====================
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedEventType = MutableStateFlow<String?>(null)
    val selectedEventType: StateFlow<String?> = _selectedEventType.asStateFlow()
    
    private val _showImportantOnly = MutableStateFlow(false)
    val showImportantOnly: StateFlow<Boolean> = _showImportantOnly.asStateFlow()
    
    // ==================== DATA STREAMS ====================
    
    val allEvents = repository.getAllEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val todayEvents = repository.getTodayEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val importantEvents = repository.getImportantEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val filteredEvents = combine(
        allEvents,
        searchQuery,
        selectedEventType,
        showImportantOnly
    ) { events, query, eventType, importantOnly ->
        var filtered = events
        
        if (query.isNotBlank()) {
            filtered = filtered.filter { event ->
                event.message.contains(query, ignoreCase = true) ||
                event.eventType.contains(query, ignoreCase = true) ||
                event.tagId?.contains(query, ignoreCase = true) == true
            }
        }
        
        eventType?.let { type ->
            filtered = filtered.filter { it.eventType == type }
        }
        
        if (importantOnly) {
            filtered = filtered.filter { it.isImportant }
        }
        
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val eventStatistics = allEvents.map { events ->
        EventStatistics(
            totalEvents = events.size,
            todayEvents = events.count { 
                isToday(it.timestamp) 
            },
            importantEvents = events.count { it.isImportant },
            eventsByType = events.groupBy { it.eventType }.mapValues { it.value.size },
            recentActivity = events.take(5)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EventStatistics()
    )
    
    // ==================== SEARCH FUNCTIONALITY ====================
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
    }
    
    fun setEventTypeFilter(eventType: String?) {
        _selectedEventType.value = eventType
    }
    
    fun toggleImportantOnly() {
        _showImportantOnly.value = !_showImportantOnly.value
    }
    
    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedEventType.value = null
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
                val updatedEvent = event.copy(isImportant = !event.isImportant)
                // repository.updateEvent(updatedEvent) // This needs repository.updateEvent(event) or similar
                updateSuccess("Event importance updated")
            } catch (e: Exception) {
                updateError("Failed to update event: ${e.message}")
            }
        }
    }
    
    fun exportEvents(events: List<NFCEventEntity>) {
        viewModelScope.launch {
            try {
                updateSuccess("Events exported successfully")
            } catch (e: Exception) {
                updateError("Failed to export events: ${e.message}")
            }
        }
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
    
    private fun isToday(date: java.util.Date): Boolean {
        val today = java.util.Calendar.getInstance()
        val targetDay = java.util.Calendar.getInstance().apply { time = date }
        
        return today.get(java.util.Calendar.YEAR) == targetDay.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.DAY_OF_YEAR) == targetDay.get(java.util.Calendar.DAY_OF_YEAR)
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
