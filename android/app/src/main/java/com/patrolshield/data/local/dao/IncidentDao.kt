package com.patrolshield.data.local.dao

import androidx.room.*
import com.patrolshield.data.local.entities.IncidentEntity

@Dao
interface IncidentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Query("SELECT * FROM incidents WHERE syncStatus = 'PENDING'")
    suspend fun getPendingIncidents(): List<IncidentEntity>

    @Query("UPDATE incidents SET syncStatus = 'SYNCED', apiId = :apiId WHERE id = :id")
    suspend fun markSynced(id: Long, apiId: Int)

    @Query("SELECT * FROM incidents ORDER BY createdAt DESC")
    suspend fun getAllIncidents(): List<IncidentEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncidents(incidents: List<IncidentEntity>)

    @Query("DELETE FROM incidents")
    suspend fun clearAll()
}
