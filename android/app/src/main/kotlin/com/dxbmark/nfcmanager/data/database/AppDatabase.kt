package com.dxbmark.nfcmanager.data.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.dxbmark.nfcmanager.data.database.dao.NFCEventDao
import com.dxbmark.nfcmanager.data.database.dao.NFCSettingsDao
import com.dxbmark.nfcmanager.data.database.entities.NFCEventEntity
import com.dxbmark.nfcmanager.data.database.entities.NFCSettingsEntity
import java.util.Date

/**
 * Room Database for NFC Manager
 * Following official Android Room database patterns
 */

// Type Converters for custom data types
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(
    entities = [
        NFCEventEntity::class,
        NFCSettingsEntity::class
    ],
    version = 2,
    exportSchema = false,
    autoMigrations = []
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    // Abstract DAO functions
    abstract fun nfcEventDao(): NFCEventDao
    abstract fun nfcSettingsDao(): NFCSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val DATABASE_NAME = "nfc_manager_database"
        
        /**
         * Gets the singleton database instance
         * Thread-safe implementation following official patterns
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration() // For development
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Database callback for initialization
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Initialize default settings
                // This could be done via Repository pattern for better separation
            }
        }
        
        /**
         * Migration examples for future versions
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to nfc_settings table
                db.execSQL("ALTER TABLE nfc_settings ADD COLUMN isOnboardingCompleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE nfc_settings ADD COLUMN lastSecurityScore INTEGER NOT NULL DEFAULT 100")
                db.execSQL("ALTER TABLE nfc_settings ADD COLUMN securityLevel TEXT NOT NULL DEFAULT 'EXCELLENT'")
            }
        }
        
        /**
         * Destroy database instance (for testing)
         */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
