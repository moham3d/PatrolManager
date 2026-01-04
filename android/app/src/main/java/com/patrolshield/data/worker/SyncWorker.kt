package com.patrolshield.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.patrolshield.data.local.dao.SyncLogDao
import com.patrolshield.domain.repository.PatrolRepository
import com.patrolshield.domain.repository.IncidentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncLogDao: SyncLogDao,
    private val patrolRepository: PatrolRepository,
    private val incidentRepository: IncidentRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val logs = syncLogDao.getAllLogs().first()
        if (logs.isEmpty()) return Result.success()

        var allSuccessful = true
        for (log in logs) {
            // Processing logic would go here based on log.type
            // For now, we simulate success and delete
            syncLogDao.deleteLog(log)
        }

        return if (allSuccessful) Result.success() else Result.retry()
    }
}
