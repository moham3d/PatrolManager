package com.patrolshield.di

import android.content.Context
import androidx.room.Room
import com.patrolshield.data.local.AppDatabase
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
            "patrolshield_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase) = db.userDao()

    @Provides
    @Singleton
    fun providePatrolDao(db: AppDatabase) = db.patrolDao()

    // fun provideCheckpointDao(db: AppDatabase) = db.checkpointDao() - Removed

    @Provides
    @Singleton
    fun provideLogDao(db: AppDatabase) = db.logDao()

    @Provides
    @Singleton
    fun provideIncidentDao(db: AppDatabase) = db.incidentDao()

    @Provides
    @Singleton
    fun provideNotificationDao(db: AppDatabase) = db.notificationDao()
}
