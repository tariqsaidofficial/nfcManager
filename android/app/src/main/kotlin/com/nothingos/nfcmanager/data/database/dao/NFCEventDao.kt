package com.nothingos.nfcmanager.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.nothingos.nfcmanager.data.database.entities.NFCEventEntity
import java.util.Date

/**
 * Data Access Object for NFC Events
 * Following official Android Room DAO patterns
 */
@Dao
interface NFCEventDao {
    
    // Query operations
    @Query("SELECT * FROM nfc_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<NFCEventEntity>>
    
    @Query("SELECT * FROM nfc_events WHERE eventType = :type ORDER BY timestamp DESC")
    fun getEventsByType(type: String): Flow<List<NFCEventEntity>>
    
    @Query("SELECT * FROM nfc_events WHERE DATE(timestamp) = DATE('now') ORDER BY timestamp DESC LIMIT 15")
    fun getTodayEvents(): Flow<List<NFCEventEntity>>
    
    @Query("SELECT * FROM nfc_events WHERE isImportant = 1 ORDER BY timestamp DESC")
    fun getImportantEvents(): Flow<List<NFCEventEntity>>
    
    @Query("SELECT COUNT(*) FROM nfc_events WHERE DATE(timestamp) = DATE('now')")
    fun getTodayEventCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM nfc_events WHERE eventType = :type AND DATE(timestamp) = DATE('now')")
    fun getTodayEventCountByType(type: String): Flow<Int>
    
    @Query("SELECT * FROM nfc_events WHERE timestamp >= :fromDate ORDER BY timestamp DESC")
    fun getEventsFromDate(fromDate: Date): Flow<List<NFCEventEntity>>
    
    // Insert operations
    @Insert
    suspend fun insertEvent(event: NFCEventEntity): Long
    
    @Insert
    suspend fun insertEvents(events: List<NFCEventEntity>)
    
    // Update operations
    @Update
    suspend fun updateEvent(event: NFCEventEntity)
    
    // Delete operations
    @Delete
    suspend fun deleteEvent(event: NFCEventEntity)
    
    @Query("DELETE FROM nfc_events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Long)
    
    @Query("DELETE FROM nfc_events WHERE timestamp < :cutoffDate")
    suspend fun deleteOldEvents(cutoffDate: Date)
    
    @Query("DELETE FROM nfc_events")
    suspend fun deleteAllEvents()
    
    // Utility queries
    @Query("SELECT DISTINCT eventType FROM nfc_events ORDER BY eventType")
    fun getDistinctEventTypes(): Flow<List<String>>
    
    @Query("SELECT * FROM nfc_events WHERE message LIKE '%' || :searchQuery || '%' ORDER BY timestamp DESC")
    fun searchEvents(searchQuery: String): Flow<List<NFCEventEntity>>
}
