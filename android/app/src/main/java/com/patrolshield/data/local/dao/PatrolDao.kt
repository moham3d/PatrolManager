package com.patrolshield.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.patrolshield.data.local.entities.PatrolEntity
import com.patrolshield.data.local.entities.CheckpointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatrolDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatrol(patrol: PatrolEntity)

    @Query("SELECT * FROM patrols WHERE status = 'started' LIMIT 1")
    fun getActivePatrol(): Flow<PatrolEntity?>

    @Query("SELECT * FROM patrols WHERE localId = :localId")
    suspend fun getPatrolById(localId: Int): PatrolEntity?

    @Query("SELECT * FROM patrols WHERE status = 'started' LIMIT 1")
    suspend fun getActivePatrolSync(): PatrolEntity?

    @Query("UPDATE patrols SET status = :status, endTime = :endTime WHERE localId = :id")
    suspend fun updatePatrolStatus(id: Int, status: String, endTime: Long)

    @Query("UPDATE patrols SET status = 'completed', endTime = :endTime WHERE status = 'started'")
    suspend fun completeAllActivePatrols(endTime: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckpoints(checkpoints: List<CheckpointEntity>)

    @Query("SELECT * FROM checkpoints WHERE templateId = :templateId")
    suspend fun getCheckpointsForTemplate(templateId: Int): List<CheckpointEntity>

    @Query("SELECT * FROM patrols WHERE status = 'completed' AND startTime >= :timestamp")
    suspend fun getCompletedPatrolsAfter(timestamp: Long): List<PatrolEntity>
}
