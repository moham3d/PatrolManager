package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    suspend fun reportIncident(
        type: String,
        priority: String,
        description: String,
        siteId: Int,
        lat: Double?,
        lng: Double?
    ): Flow<Resource<Unit>>
}
