package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    suspend fun reportIncident(
        type: String, 
        priority: String, 
        description: String,
        lat: Double?,
        lng: Double?,
        imagePath: String? = null
    ): Flow<Resource<Unit>>
}
