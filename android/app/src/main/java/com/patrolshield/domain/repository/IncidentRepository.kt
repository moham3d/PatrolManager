package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.IncidentRequest
import java.io.File

interface IncidentRepository {
    suspend fun reportIncident(request: IncidentRequest, imageFile: File? = null): Resource<Unit>
    suspend fun getActiveIncidents(): Resource<List<com.patrolshield.data.remote.dto.Incident>>
    suspend fun resolveIncident(incidentId: Int, comment: String, imageFile: File? = null): Resource<Unit>
}
