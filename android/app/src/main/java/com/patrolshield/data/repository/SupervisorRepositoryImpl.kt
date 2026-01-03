import android.content.Context
import com.patrolshield.common.ImageUtils
import com.patrolshield.common.Resource
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.ActiveIncidentsDto
import com.patrolshield.data.remote.dto.LivePatrolDto
import com.patrolshield.domain.repository.SupervisorRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class SupervisorRepositoryImpl @Inject constructor(
    private val api: ApiService,
    @ApplicationContext private val context: Context
) : SupervisorRepository {

    override suspend fun getLivePatrols(): Flow<Resource<List<LivePatrolDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getLivePatrols()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Unknown error"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override suspend fun getActiveIncidents(): Flow<Resource<ActiveIncidentsDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getActiveIncidents()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.message}"))
        }
    }

    override suspend fun resolveIncident(id: Int, notes: String, evidenceUri: android.net.Uri?): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val notesBody = notes.toRequestBody("text/plain".toMediaTypeOrNull())
            val evidencePart = evidenceUri?.let { uri ->
                val file = ImageUtils.compressImage(uri, context)
                file?.let {
                    val requestFile = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("evidence", it.name, requestFile)
                }
            }

            val response = api.resolveIncident(id, notesBody, evidencePart)
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.message}"))
        }
    }
}
