package com.patrolshield.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.patrolshield.data.local.dao.SyncLogDao
import com.patrolshield.data.local.dao.ShiftDao
import com.patrolshield.data.local.entities.SyncLogEntity
import com.patrolshield.data.local.entities.ShiftEntity

import com.patrolshield.data.local.dao.PatrolDao
import com.patrolshield.data.local.dao.IncidentDao
import com.patrolshield.data.local.entities.PatrolEntity
import com.patrolshield.data.local.entities.CheckpointEntity
import com.patrolshield.data.local.entities.IncidentEntity

@Database(
    entities = [
        SyncLogEntity::class, 
        ShiftEntity::class,
        PatrolEntity::class,
        CheckpointEntity::class,
        IncidentEntity::class
    ], 
    version = 2, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncLogDao(): SyncLogDao
    abstract fun shiftDao(): ShiftDao
    abstract fun patrolDao(): PatrolDao
    abstract fun incidentDao(): IncidentDao
}
