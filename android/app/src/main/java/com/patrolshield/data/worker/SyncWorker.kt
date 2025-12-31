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
                         } else {
                             // If 400/Logic error, maybe delete? For now keep to retry.
                             if (response.code() in 400..499) {
                                 // logDao.deleteLog(log.id) // Optional: Discard invalid
                             }
                         }
                    }
                    "GPS_LOG" -> {
                        // Batch upload logic could go here
                        // For MVP we just assume we sent it or skip
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
                // Keep log for retry
            }
        }

        if (successCount == pendingLogs.size) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
