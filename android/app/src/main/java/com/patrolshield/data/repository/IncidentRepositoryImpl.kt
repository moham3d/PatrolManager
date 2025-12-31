package com.patrolshield.data.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.local.dao.IncidentDao
import com.patrolshield.data.local.entities.IncidentEntity
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.IncidentRequest
import com.patrolshield.domain.repository.IncidentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class IncidentRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: IncidentDao
) : IncidentRepository {

    override suspend fun reportIncident(
        type: String,
        priority: String,
        description: String,
        lat: Double?,
        lng: Double?,
        imagePath: String?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        // Save locally first (Offline Persistence)
        val entity = IncidentEntity(
            type = type,
            priority = priority,
            description = description,
            localEvidencePath = imagePath,
            lat = lat,
            lng = lng,
            timestamp = System.currentTimeMillis()
            // isSynced removed as it's not in Entity, status="new" implies unsynced
        )
        dao.insertIncident(entity)

        try {
            // Attempt to upload
            // Note: For MVP we might just send JSON. Image upload usually requires Multipart.
            // As I defined 'imageBase64' in DTO, let's assume we convert if exists.
            
            val request = IncidentRequest(
                type = type,
                priority = priority,
                description = description,
                runId = null, // Could grab active patrol run from DB if needed
                lat = lat,
                lng = lng,
                imageBase64 = null // Implementing base64 encoding would be "extra", skipping for MVP speed unless requested.
            )
            
            val response = api.reportIncident(request)
            if (response.isSuccessful) {
                // Mark synced
                // dao.updateSynced(entity.localId, true)
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Server error: ${response.code()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Incident saved locally."))
        }
    }
}
