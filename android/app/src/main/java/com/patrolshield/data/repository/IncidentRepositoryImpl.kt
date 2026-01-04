package com.patrolshield.data.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.IncidentRequest
import com.patrolshield.domain.repository.IncidentRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Singleton
class IncidentRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val incidentDao: IncidentDao,
    private val syncLogDao: SyncLogDao
) : IncidentRepository {

    private val gson = com.google.gson.Gson()

    override suspend fun reportIncident(request: IncidentRequest, imageFile: File?): Resource<Unit> {
        return try {
            val response = if (imageFile == null) {
                apiService.reportIncident(request)
            } else {
                val type = request.type.toRequestBody("text/plain".toMediaTypeOrNull())
                val priority = request.priority.toRequestBody("text/plain".toMediaTypeOrNull())
                val description = request.description.toRequestBody("text/plain".toMediaTypeOrNull())
                val siteId = request.siteId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val lat = request.lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val lng = request.lng?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val patrolRunId = request.patrolRunId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = MultipartBody.Part.createFormData(
                    "evidence",
                    imageFile.name,
                    imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                )

                apiService.reportIncidentMultipart(type, priority, description, siteId, lat, lng, patrolRunId, imagePart)
            }

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val payloadMap = mapOf(
                    "request" to request,
                    "imagePath" to (imageFile?.absolutePath ?: "")
                )
                queueSyncLog("REPORT_INCIDENT", gson.toJson(payloadMap))
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            val payloadMap = mapOf(
                "request" to request,
                "imagePath" to (imageFile?.absolutePath ?: "")
            )
            queueSyncLog("REPORT_INCIDENT", gson.toJson(payloadMap))
            Resource.Success(Unit)
        }
    }

    override suspend fun getActiveIncidents(): Resource<List<com.patrolshield.data.remote.dto.Incident>> {
        // 1. Try to fetch from API and update local DB
        try {
            val response = apiService.getActiveIncidents()
            if (response.isSuccessful && response.body() != null) {
                val incidents = response.body()!!
                val entities = incidents.map { dto ->
                    com.patrolshield.data.local.entities.IncidentEntity(
                        apiId = dto.id,
                        type = dto.type,
                        priority = dto.priority,
                        description = dto.description,
                        siteId = dto.siteId,
                        lat = dto.lat,
                        lng = dto.lng,
                        imagePath = dto.evidence.firstOrNull()?.filePath,
                        createdAt = dto.createdAt,
                        syncStatus = "synced"
                    )
                }
                // Clear old and insert new (simple cache strategy)
                incidentDao.clearAll() // Need to add clearAll to Dao or manually delete
                incidentDao.insertIncidents(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Return from DB
        return try {
            val entities = incidentDao.getAllIncidents()
            val dtos = entities.map { entity ->
                com.patrolshield.data.remote.dto.Incident(
                    id = entity.apiId,
                    type = entity.type,
                    priority = entity.priority,
                    status = "active", // Default since we fetch active
                    description = entity.description,
                    siteId = entity.siteId,
                    reporterId = 0, // Not stored in entity
                    lat = entity.lat,
                    lng = entity.lng,
                    createdAt = entity.createdAt,
                    evidence = if (entity.imagePath != null) listOf(
                        com.patrolshield.data.remote.dto.IncidentEvidence(0, entity.imagePath, "image")
                    ) else emptyList()
                )
            }
            Resource.Success(dtos)
        } catch (e: Exception) {
            Resource.Error("Failed to load incidents: ${e.localizedMessage}")
        }
    }

    override suspend fun resolveIncident(incidentId: Int, comment: String, imageFile: File?): Resource<Unit> {
        return try {
            val commentPart = comment.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val imagePart = imageFile?.let {
                MultipartBody.Part.createFormData(
                    "evidence",
                    it.name,
                    it.asRequestBody("image/*".toMediaTypeOrNull())
                )
            }

            val response = apiService.resolveIncident(incidentId, commentPart, imagePart)
            
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val payloadMap = mapOf(
                    "incidentId" to incidentId,
                    "comment" to comment,
                    "imagePath" to (imageFile?.absolutePath ?: "")
                )
                queueSyncLog("RESOLVE_INCIDENT", gson.toJson(payloadMap))
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            val payloadMap = mapOf(
                "incidentId" to incidentId,
                "comment" to comment,
                "imagePath" to (imageFile?.absolutePath ?: "")
            )
            queueSyncLog("RESOLVE_INCIDENT", gson.toJson(payloadMap))
            Resource.Success(Unit)
        }
    }

    private suspend fun queueSyncLog(action: String, payload: String, priority: Int = 0) {
        syncLogDao.insertLog(com.patrolshield.data.local.entities.SyncLogEntity(
            action = action,
            payload = payload,
            priority = priority,
            timestamp = System.currentTimeMillis()
        ))
    }
}
