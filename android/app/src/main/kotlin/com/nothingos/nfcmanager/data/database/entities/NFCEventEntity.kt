package com.nothingos.nfcmanager.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity class representing NFC events in the database
 * Following Android Room database official standards
 */
@Entity(tableName = "nfc_events")
data class NFCEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val timestamp: Date,
    val eventType: String, // "READ", "WRITE", "DETECTED", "STATE_CHANGED"
    val message: String,
    val icon: String, // For UI display (Settings, Shield, Power, etc.)
    val tagId: String? = null,
    val tagType: String? = null,
    val data: String? = null,
    val appPackage: String? = null,
    val isBlocked: Boolean = false,
    val isImportant: Boolean = false
)
