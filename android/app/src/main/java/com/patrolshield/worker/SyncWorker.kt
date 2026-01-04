package com.patrolshield.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.patrolshield.data.local.dao.SyncLogDao
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.PanicRequest
import com.patrolshield.data.remote.dto.ScanRequest
import com.patrolshield.data.remote.dto.StartPatrolRequest
import com.patrolshield.data.local.entities.SyncLogEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncLogDao: SyncLogDao,
    private val apiService: ApiService
) : CoroutineWorker(appContext, workerParams) {

    private val gson = Gson()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val pendingLogs = syncLogDao.getPendingLogs()
            
            if (pendingLogs.isEmpty()) {
                return@withContext Result.success()
            }

            for (log in pendingLogs) {
                val success = processLog(log)
                if (success) {
                    syncLogDao.deleteLog(log)
                } else {
                    // Stop processing for now, let WorkManager retry later
                    // Or continue trying others? 
                    // To preserve order, we should probably stop if strict dependency exists
                    // But for now, we'll try best effort or return Retry
                    return@withContext Result.retry() 
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private suspend fun processLog(log: SyncLogEntity): Boolean {
        return try {
            when (log.action) {
                "START_PATROL" -> {
                    val request = gson.fromJson(log.payload, StartPatrolRequest::class.java)
                    val response = apiService.startPatrol(request)
                    response.isSuccessful
                }
                "SCAN_CHECKPOINT" -> {
                    val request = gson.fromJson(log.payload, ScanRequest::class.java)
                    val response = apiService.scanCheckpoint(request)
                    response.isSuccessful
                }
                "END_PATROL" -> {
                    val map = gson.fromJson(log.payload, Map::class.java)
                    val runId = (map["runId"] as? Double)?.toInt() ?: return false
                    val response = apiService.endPatrol(runId)
                    response.isSuccessful
                }
                "PANIC_ALERT" -> {
                    val request = gson.fromJson(log.payload, PanicRequest::class.java)
                    val response = apiService.triggerPanic(request)
                    response.isSuccessful
                }
                "REPORT_INCIDENT" -> {
                    val map = gson.fromJson(log.payload, Map::class.java)
                    val requestJson = gson.toJson(map["request"])
                    val request = gson.fromJson(requestJson, com.patrolshield.data.remote.dto.IncidentRequest::class.java)
                    val imagePath = map["imagePath"] as? String

                    val type = request.type.toRequestBody("text/plain".toMediaTypeOrNull())
                    val priority = request.priority.toRequestBody("text/plain".toMediaTypeOrNull())
                    val description = request.description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val siteId = request.siteId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val lat = request.lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                    val lng = request.lng?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                    val patrolRunId = request.patrolRunId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

                    val imagePart = if (!imagePath.isNullOrEmpty()) {
                        val file = java.io.File(imagePath)
                        if (file.exists()) {
                            okhttp3.MultipartBody.Part.createFormData(
                                "evidence",
                                file.name,
                                file.asRequestBody("image/*".toMediaTypeOrNull())
                            )
                        } else null
                    } else null

                    val response = apiService.reportIncidentMultipart(type, priority, description, siteId, lat, lng, patrolRunId, imagePart)
                    response.isSuccessful
                }
                "RESOLVE_INCIDENT" -> {
                    val map = gson.fromJson(log.payload, Map::class.java)
                    val incidentId = (map["incidentId"] as? Double)?.toInt() ?: return false
                    val comment = map["comment"] as? String ?: ""
                    val imagePath = map["imagePath"] as? String

                    val commentPart = comment.toRequestBody("text/plain".toMediaTypeOrNull())
                    
                    val imagePart = if (!imagePath.isNullOrEmpty()) {
                        val file = java.io.File(imagePath)
                        if (file.exists()) {
                            okhttp3.MultipartBody.Part.createFormData(
                                "evidence",
                                file.name,
                                file.asRequestBody("image/*".toMediaTypeOrNull())
                            )
                        } else null
                    } else null

                    val response = apiService.resolveIncident(incidentId, commentPart, imagePart)
                    response.isSuccessful
                }
                else -> true 
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
