package com.dxbmark.nfcmanager.di

import android.content.Context
import androidx.room.Room
import com.dxbmark.nfcmanager.data.database.AppDatabase
import com.dxbmark.nfcmanager.data.database.dao.NFCEventDao
import com.dxbmark.nfcmanager.data.database.dao.NFCSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "nfc_manager_database"
        )
        .fallbackToDestructiveMigration() // <<< CRITICAL: Prevents database crashes during development
        .build()
    }

    @Provides
    fun provideNfcEventDao(appDatabase: AppDatabase): NFCEventDao {
        return appDatabase.nfcEventDao()
    }

    @Provides
    fun provideNfcSettingsDao(appDatabase: AppDatabase): NFCSettingsDao {
        return appDatabase.nfcSettingsDao()
    }
}
