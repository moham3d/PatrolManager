package com.patrolshield.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.patrolshield.data.local.dao.*
import com.patrolshield.data.local.entities.*


@Database(
    entities = [
        UserEntity::class,
        PatrolEntity::class,
        com.patrolshield.data.local.entities.CheckpointEntity::class,
        LogEntity::class,
        IncidentEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun patrolDao(): PatrolDao
    // abstract fun checkpointDao(): CheckpointDao - Removed
    abstract fun logDao(): LogDao
    abstract fun incidentDao(): IncidentDao
    abstract fun notificationDao(): NotificationDao
}
