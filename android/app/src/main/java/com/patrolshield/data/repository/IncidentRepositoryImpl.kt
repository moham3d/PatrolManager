import android.content.Context
import android.net.Uri
import com.patrolshield.common.ImageUtils
import com.patrolshield.common.Resource
import com.patrolshield.data.local.dao.IncidentDao
import com.patrolshield.data.local.entities.IncidentEntity
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.IncidentRequest
import com.patrolshield.domain.repository.IncidentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

import com.patrolshield.data.local.dao.LogDao
import com.patrolshield.data.local.entities.LogEntity
import com.google.gson.Gson

class IncidentRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: IncidentDao,
    private val logDao: LogDao,
    @ApplicationContext private val context: Context
) : IncidentRepository {

    override suspend fun reportIncident(
        type: String,
        priority: String,
        description: String,
        siteId: Int,
        lat: Double?,
        lng: Double?,
        images: List<Uri>
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        val compressedFiles = images.mapNotNull { ImageUtils.compressImage(it, context) }
        val localEvidencePaths = compressedFiles.joinToString(",") { it.absolutePath }

        // Save locally first (Offline Persistence)
        val entity = IncidentEntity(
            type = type,
            priority = priority,
            description = description,
            localEvidencePath = localEvidencePaths,
            lat = lat,
            lng = lng,
            timestamp = System.currentTimeMillis()
        )
        dao.insertIncident(entity)

        try {
            // Attempt to upload using Multipart
            val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
            val priorityBody = priority.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val siteIdBody = siteId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val latBody = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val lngBody = lng?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            // For now, handle only the first image if multiple are provided (as per current backend)
            val evidencePart = compressedFiles.firstOrNull()?.let { file ->
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("evidence", file.name, requestFile)
            }

            val response = api.reportIncidentMultipart(
                typeBody,
                priorityBody,
                descriptionBody,
                siteIdBody,
                latBody,
                lngBody,
                evidencePart
            )

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Offline: Queue for sync
            val request = IncidentRequest(
                type = type,
                priority = priority,
                description = description,
                runId = null,
                siteId = siteId,
                lat = lat,
                lng = lng,
                imageBase64 = compressedFiles.firstOrNull()?.absolutePath // Use this field to store path in logs
            )
            logDao.insertLog(LogEntity(
                type = "REPORT_INCIDENT",
                payload = Gson().toJson(request),
                priority = 3,
                synced = false
            ))
            emit(Resource.Error("Couldn't reach server. Incident saved locally."))
        }
    }
}
