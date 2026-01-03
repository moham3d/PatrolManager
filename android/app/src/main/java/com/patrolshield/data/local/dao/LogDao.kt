package com.patrolshield.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.patrolshield.data.local.entities.LogEntity

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)

    @Query("SELECT * FROM logs WHERE synced = 0 ORDER BY priority ASC, timestamp ASC")
    suspend fun getUnsyncedLogs(): List<LogEntity>

    @Query("SELECT * FROM logs WHERE synced = 0 AND priority = :priority ORDER BY timestamp ASC")
    suspend fun getPendingLogsByPriority(priority: Int): List<LogEntity>

    @Query("DELETE FROM logs WHERE id = :id")
    suspend fun deleteLog(id: Int)

    @Query("UPDATE logs SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Int)
}
