package com.patrolshield.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.patrolshield.data.local.dao.LogDao
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.StartPatrolRequest
import com.patrolshield.data.remote.dto.ScanRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val logDao: LogDao,
    private val api: ApiService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val pendingLogs = logDao.getUnsyncedLogs()
        
        if (pendingLogs.isEmpty()) {
            return@withContext Result.success()
        }

        var successCount = 0
        
        for (log in pendingLogs) {
            try {
                when (log.type) {
                    "START_PATROL" -> {
                        val request = Gson().fromJson(log.payload, StartPatrolRequest::class.java)
                        val response = api.startPatrol(request)
                        if (response.isSuccessful) {
                             logDao.deleteLog(log.id)
                             successCount++
                        }
                    }
                    "SCAN_CHECKPOINT" -> {
                         val request = Gson().fromJson(log.payload, ScanRequest::class.java)
                         val response = api.scanCheckpoint(request)
                         if (response.isSuccessful) {
                             logDao.deleteLog(log.id)
                             successCount++
                         }
                    }
                    "REPORT_INCIDENT" -> {
                        val request = Gson().fromJson(log.payload, com.patrolshield.data.remote.dto.IncidentRequest::class.java)
                        
                        // Check if we have image path in metadata or payload (Assuming payload for now)
                        // If IncidentRequest doesn't have local path, we might need a separate way to pass it.
                        // For this POC, let's assume imageBase64 field is used to store LOCAL FILE PATH in logs.
                        
                        val typeBody = request.type.toRequestBody("text/plain".toMediaTypeOrNull())
                        val priorityBody = request.priority.toRequestBody("text/plain".toMediaTypeOrNull())
                        val descriptionBody = request.description.toRequestBody("text/plain".toMediaTypeOrNull())
                        val siteIdBody = request.siteId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        val latBody = request.lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                        val lngBody = request.lng?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

                        var evidencePart: MultipartBody.Part? = null
                        if (!request.imageBase64.isNullOrBlank()) {
                            val file = File(request.imageBase64)
                            if (file.exists()) {
                                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                                evidencePart = MultipartBody.Part.createFormData("evidence", file.name, requestFile)
                            }
                        }

                        val response = api.reportIncidentMultipart(
                            typeBody, priorityBody, descriptionBody, siteIdBody, latBody, lngBody, evidencePart
                        )
                        if (response.isSuccessful) {
                            logDao.deleteLog(log.id)
                            successCount++
                        }
                    }
                    "GPS_LOG" -> {
                        logDao.deleteLog(log.id)
                        successCount++
                    }
                    "END_PATROL" -> {
                         val request = Gson().fromJson(log.payload, com.patrolshield.data.remote.dto.EndPatrolRequest::class.java)
                         val response = api.endPatrol(request)
                         if (response.isSuccessful) {
                             logDao.deleteLog(log.id)
                             successCount++
                         }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (successCount == pendingLogs.size) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
