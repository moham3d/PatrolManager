package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.LivePatrolDto
import kotlinx.coroutines.flow.Flow

interface SupervisorRepository {
    suspend fun getLivePatrols(): Flow<Resource<List<LivePatrolDto>>>
}
