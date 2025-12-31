package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.ManagerStatsDto
import kotlinx.coroutines.flow.Flow

interface ManagerRepository {
    suspend fun getStats(): Flow<Resource<ManagerStatsDto>>
}
