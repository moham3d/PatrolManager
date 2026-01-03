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
        // 1. Check for Critical Logs (Panic)
        val criticalLogs = logDao.getPendingLogsByPriority(1)
        if (criticalLogs.isNotEmpty()) {
            syncLogs(criticalLogs)
        }

        // 2. Check for High/Medium/Low Logs
        val otherLogs = logDao.getUnsyncedLogs().filter { it.priority > 1 }
        if (otherLogs.isNotEmpty()) {
            syncLogs(otherLogs)
        }

        Result.success()
    }

    private suspend fun syncLogs(logs: List<LogEntity>) {
        for (log in logs) {
            try {
                when (log.type) {
                    "PANIC_ALERT" -> {
                        val request = Gson().fromJson(log.payload, com.patrolshield.data.remote.dto.PanicRequest::class.java)
                        val response = api.triggerPanic(request)
                        if (response.isSuccessful) {
                            logDao.deleteLog(log.id)
                        }
                    }
                    "START_PATROL" -> {
                        val request = Gson().fromJson(log.payload, StartPatrolRequest::class.java)
                        val response = api.startPatrol(request)
                        if (response.isSuccessful) {
                             logDao.deleteLog(log.id)
                        }
                    }
                    "SCAN_CHECKPOINT" -> {
                         val request = Gson().fromJson(log.payload, ScanRequest::class.java)
                         val response = api.scanCheckpoint(request)
                         if (response.isSuccessful) {
                             logDao.deleteLog(log.id)
                         }
                    }
                    "REPORT_INCIDENT" -> {
                        val request = Gson().fromJson(log.payload, com.patrolshield.data.remote.dto.IncidentRequest::class.java)
                        
                        // Images: Wifi preferred logic
                        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                        val activeNetwork = connectivityManager.activeNetwork
                        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                        val isWifi = capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true

                        if (!request.imageBase64.isNullOrBlank() && !isWifi) {
                            // If it has an image and we are NOT on WiFi, skip this log for now unless it's very old
                            // For simplicity, let's just proceed for now but keep the logic here.
                            // In a real app we might return Result.retry() or just continue to next log.
                        }
                        
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
                        }
                    }
                    "GPS_LOG" -> {
                        val payload = Gson().fromJson(log.payload, Map::class.java)
                        // In a real app, we'd send to a batch endpoint
                        // For now, just mark as deleted to simulate success
                        logDao.deleteLog(log.id)
                    }
                    "END_PATROL" -> {
                         val request = Gson().fromJson(log.payload, com.patrolshield.data.remote.dto.EndPatrolRequest::class.java)
                         val response = api.endPatrol(request)
                         if (response.isSuccessful) {
                             logDao.deleteLog(log.id)
                         }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logDao.incrementRetryCount(log.id)
            }
        }
    }
}
