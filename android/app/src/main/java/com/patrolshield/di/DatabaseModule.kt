package com.patrolshield.di

import android.content.Context
import androidx.room.Room
import com.patrolshield.data.local.AppDatabase
import com.patrolshield.data.local.dao.SyncLogDao
import com.patrolshield.data.local.dao.ShiftDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "patrol_shield_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideSyncLogDao(db: AppDatabase): SyncLogDao {
        return db.syncLogDao()
    }

    @Provides
    fun provideShiftDao(db: AppDatabase): ShiftDao {
        return db.shiftDao()
    }

    @Provides
    fun providePatrolDao(db: AppDatabase): com.patrolshield.data.local.dao.PatrolDao {
        return db.patrolDao()
    }

    @Provides
    fun provideIncidentDao(db: AppDatabase): com.patrolshield.data.local.dao.IncidentDao {
        return db.incidentDao()
    }
}
