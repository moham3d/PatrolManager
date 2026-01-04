package com.patrolshield.data.local.dao

import androidx.room.*
import com.patrolshield.data.local.entities.CheckpointEntity
import com.patrolshield.data.local.entities.PatrolEntity
import com.patrolshield.data.local.entities.PatrolWithCheckpoints
import kotlinx.coroutines.flow.Flow

@Dao
interface PatrolDao {
    @Transaction
    @Query("SELECT * FROM patrols")
    fun getSchedule(): List<PatrolWithCheckpoints>

    @Transaction
    @Query("SELECT * FROM patrols WHERE id = :patrolId")
    fun getPatrol(patrolId: Int): PatrolWithCheckpoints?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatrols(patrols: List<PatrolEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckpoints(checkpoints: List<CheckpointEntity>)

    @Query("UPDATE patrols SET status = :status WHERE id = :patrolId")
    suspend fun updatePatrolStatus(patrolId: Int, status: String)

    @Query("UPDATE checkpoints SET isVisited = :isVisited, visitedAt = :visitedAt WHERE id = :checkpointId")
    suspend fun updateCheckpointStatus(checkpointId: Int, isVisited: Boolean, visitedAt: String?)

    @Transaction
    suspend fun updateSchedule(patrols: List<PatrolEntity>, checkpoints: List<CheckpointEntity>) {
        insertPatrols(patrols)
        insertCheckpoints(checkpoints)
    }
}
