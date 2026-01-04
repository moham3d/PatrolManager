package com.patrolshield.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.patrolshield.data.local.dao.SyncLogDao
import com.patrolshield.data.local.dao.ShiftDao
import com.patrolshield.data.local.entities.SyncLogEntity
import com.patrolshield.data.local.entities.ShiftEntity

@Database(entities = [SyncLogEntity::class, ShiftEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncLogDao(): SyncLogDao
    abstract fun shiftDao(): ShiftDao
}
