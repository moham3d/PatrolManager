package com.patrolshield.domain.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.remote.dto.IncidentRequest
import java.io.File

interface IncidentRepository {
    suspend fun reportIncident(request: IncidentRequest, imageFile: File? = null): Resource<Unit>
}
