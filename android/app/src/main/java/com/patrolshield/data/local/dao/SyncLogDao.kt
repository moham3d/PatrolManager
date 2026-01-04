package com.patrolshield.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.patrolshield.data.local.entities.SyncLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncLogDao {
    @Insert
    suspend fun insertLog(log: SyncLogEntity)

    @Query("SELECT * FROM sync_logs ORDER BY timestamp ASC")
    fun getAllLogs(): Flow<List<SyncLogEntity>>

    @Delete
    suspend fun deleteLog(log: SyncLogEntity)

    @Query("DELETE FROM sync_logs")
    suspend fun clearAll()
}
