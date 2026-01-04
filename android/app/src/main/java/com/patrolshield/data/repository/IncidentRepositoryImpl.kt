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
class IncidentRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : IncidentRepository {

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
                Resource.Error("Reporting failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.localizedMessage}")
        }
    }
}
