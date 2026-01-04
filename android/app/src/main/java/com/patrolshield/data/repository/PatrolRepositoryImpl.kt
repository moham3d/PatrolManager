package com.patrolshield.data.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.local.dao.ShiftDao
import com.patrolshield.data.local.entities.ShiftEntity
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.*
import com.patrolshield.domain.repository.PatrolRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Singleton
class PatrolRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val shiftDao: ShiftDao,
    private val patrolDao: PatrolDao,
    private val syncLogDao: SyncLogDao
) : PatrolRepository {

    private val gson = com.google.gson.Gson()

    override fun getActiveShift(): Flow<ShiftEntity?> = shiftDao.getActiveShift()

    override suspend fun clockIn(lat: Double, lng: Double, siteId: Int): Resource<ShiftResponse> {
        return try {
            val response = apiService.clockIn(ClockInRequest(latitude = lat, longitude = lng, siteId = siteId))
            if (response.isSuccessful && response.body() != null) {
                val shift = response.body()!!
                shiftDao.insertShift(ShiftEntity(
                    id = shift.id,
                    startTime = System.currentTimeMillis(),
                    siteId = shift.siteId
                ))
                Resource.Success(shift)
            } else {
                Resource.Error("Clock-in failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.localizedMessage}")
        }
    }

    override suspend fun clockOut(lat: Double, lng: Double): Resource<Unit> {
        return try {
            val response = apiService.clockOut(ClockOutRequest(latitude = lat, longitude = lng))
            if (response.isSuccessful) {
                shiftDao.clearShift()
                Resource.Success(Unit)
            } else {
                Resource.Error("Clock-out failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.localizedMessage}")
        }
    }

    override suspend fun getMySchedule(): Resource<List<PatrolDto>> {
        // 1. Try to fetch from API and update local DB
        try {
            val response = apiService.getMySchedule()
            if (response.isSuccessful && response.body() != null) {
                val patrols = response.body()!!
                
                // Map to Entities
                val patrolEntities = patrols.map { dto ->
                    PatrolEntity(
                        id = dto.id,
                        name = dto.name,
                        description = "", // DTO is missing desc, using empty
                        scheduledDate = System.currentTimeMillis(), // DTO missing date, using now
                        startTime = null,
                        endTime = null,
                        status = "scheduled",
                        syncStatus = "synced"
                    )
                }

                val checkpointEntities = patrols.flatMap { patrol ->
                    patrol.checkpoints.map { cp ->
                        CheckpointEntity(
                            id = cp.id,
                            patrolId = patrol.id,
                            name = cp.name,
                            type = cp.type,
                            tagId = cp.value, // value is tagId/QR content
                            latitude = 0.0, // DTO missing location
                            longitude = 0.0,
                            isVisited = false,
                            visitedAt = null
                        )
                    }
                }

                // Update DB transactionally
                patrolDao.updateSchedule(patrolEntities, checkpointEntities)
            }
        } catch (e: Exception) {
            // Ignore API failures, fallback to DB
            e.printStackTrace()
        }

        // 2. Return from DB (Single Source of Truth)
        return try {
            val localPatrols = patrolDao.getSchedule()
            val dtos = localPatrols.map { p ->
                PatrolDto(
                    id = p.patrol.id,
                    name = p.patrol.name,
                    siteId = 0, // Not stored in PatrolEntity currently, might need update
                    checkpoints = p.checkpoints.map { cp ->
                        CheckpointDto(
                            id = cp.id,
                            name = cp.name,
                            type = cp.type,
                            value = cp.tagId,
                            order = 0 // Order not preserved in entity yet
                        )
                    }
                )
            }
            Resource.Success(dtos)
        } catch (e: Exception) {
            Resource.Error("Failed to load schedule: ${e.localizedMessage}")
        }
    }

    override suspend fun startPatrol(patrolId: Int): Resource<Int> {
        // 1. Update Local DB
        patrolDao.updatePatrolStatus(patrolId, "started")

        // 2. Try API
        return try {
            val request = StartPatrolRequest(patrolId)
            val response = apiService.startPatrol(request)
            
            if (response.isSuccessful) {
                Resource.Success(patrolId)
            } else {
                queueSyncLog("START_PATROL", gson.toJson(request))
                Resource.Success(patrolId) // Treat as success for offline
            }
        } catch (e: Exception) {
            queueSyncLog("START_PATROL", gson.toJson(StartPatrolRequest(patrolId)))
            Resource.Success(patrolId) // Treat as success for offline
        }
    }

    override suspend fun scanCheckpoint(runId: Int, checkpointId: Int, value: String, lat: Double?, lng: Double?): Resource<Unit> {
        // 1. Update Local DB
        patrolDao.updateCheckpointStatus(checkpointId, true, System.currentTimeMillis())

        // 2. Try API
        return try {
            val request = ScanRequest(runId, checkpointId, value, lat, lng)
            val response = apiService.scanCheckpoint(request)
            
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                queueSyncLog("SCAN_CHECKPOINT", gson.toJson(request))
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            queueSyncLog("SCAN_CHECKPOINT", gson.toJson(ScanRequest(runId, checkpointId, value, lat, lng)))
            Resource.Success(Unit)
        }
    }

    override suspend fun endPatrol(runId: Int): Resource<Unit> {
        // 1. Update Local DB
        patrolDao.updatePatrolStatus(runId, "completed")

        // 2. Try API
        return try {
            val response = apiService.endPatrol(runId)
            
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                // We format a simple ID request since endPatrol takes just ID on path
                // But SyncWorker needs payload. We'll store ID as JSON
                queueSyncLog("END_PATROL", gson.toJson(mapOf("runId" to runId)))
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            queueSyncLog("END_PATROL", gson.toJson(mapOf("runId" to runId)))
            Resource.Success(Unit)
        }
    }

    override suspend fun triggerPanic(lat: Double, lng: Double, runId: Int?): Resource<Unit> {
        return try {
            val request = PanicRequest(lat, lng, runId)
            val response = apiService.triggerPanic(request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                // Priority sync for panic
                queueSyncLog("PANIC_ALERT", gson.toJson(request), 1) // 1 = Critical
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            queueSyncLog("PANIC_ALERT", gson.toJson(PanicRequest(lat, lng, runId)), 1)
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
