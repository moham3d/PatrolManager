package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.ActiveIncidentsDto
import com.patrolshield.data.remote.dto.LivePatrolDto
import kotlinx.coroutines.flow.Flow

interface SupervisorRepository {
    suspend fun getLivePatrols(): Flow<Resource<List<LivePatrolDto>>>
    suspend fun getActiveIncidents(): Flow<Resource<ActiveIncidentsDto>>
    suspend fun resolveIncident(id: Int, notes: String, evidenceUri: android.net.Uri?): Flow<Resource<Unit>>
}
