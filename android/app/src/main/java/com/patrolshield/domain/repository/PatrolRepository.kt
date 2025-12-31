package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.local.entities.PatrolEntity
import com.patrolshield.data.remote.dto.PatrolDto
import kotlinx.coroutines.flow.Flow

interface PatrolRepository {
    suspend fun getMySchedule(): Flow<Resource<List<PatrolDto>>>
    suspend fun startPatrol(templateId: Int): Flow<Resource<PatrolEntity>>
    suspend fun scanCheckpoint(runId: Int, checkpointId: Int, lat: Double, lng: Double): Flow<Resource<String>>
    suspend fun getCheckpoints(templateId: Int): List<com.patrolshield.data.local.entities.CheckpointEntity>
    suspend fun sendPanic(lat: Double? = null, lng: Double? = null, runId: Int? = null): Resource<Unit>
    suspend fun endPatrol(): Flow<Resource<Unit>>
    fun getActivePatrol(): Flow<PatrolEntity?>
    suspend fun getCompletedPatrols(): List<PatrolEntity>
}
