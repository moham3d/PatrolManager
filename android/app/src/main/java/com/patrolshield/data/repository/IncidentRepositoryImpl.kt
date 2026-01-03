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

class IncidentRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: IncidentDao,
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
            // Attempt to upload
            val request = IncidentRequest(
                type = type,
                priority = priority,
                description = description,
                runId = null,
                siteId = siteId,
                lat = lat,
                lng = lng,
                imageBase64 = null // Will use multipart in next task
            )
            
            val response = api.reportIncident(request)
            if (response.isSuccessful) {
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
