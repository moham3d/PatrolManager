package com.patrolshield.data.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.local.dao.PatrolDao
import com.patrolshield.data.local.dao.LogDao
import com.patrolshield.data.local.entities.PatrolEntity
import com.patrolshield.data.local.entities.LogEntity
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.PatrolDto
import com.patrolshield.data.remote.dto.StartPatrolRequest
import com.patrolshield.data.remote.dto.ScanRequest
import com.patrolshield.data.remote.dto.LocationDto
import com.patrolshield.domain.repository.PatrolRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import com.google.gson.Gson

class PatrolRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val patrolDao: PatrolDao,
    private val logDao: LogDao
) : PatrolRepository {

    override suspend fun getMySchedule(): Flow<Resource<List<PatrolDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getMySchedule()
            if (response.isSuccessful && response.body() != null) {
                // Sync Checkpoints to Local DB
                response.body()!!.forEach { schedule ->
                    val entities = schedule.checkpoints.map { cp ->
                        com.patrolshield.data.local.entities.CheckpointEntity(
                            id = cp.id,
                            templateId = schedule.id,
                            name = cp.name,
                            lat = cp.lat,
                            lng = cp.lng
                        )
                    }
                    if (entities.isNotEmpty()) {
                        patrolDao.insertCheckpoints(entities)
                    }
                }
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Offline: Cannot fetch new schedule. " + e.message))
        }
    }

    override suspend fun startPatrol(templateId: Int): Flow<Resource<PatrolEntity>> = flow {
        emit(Resource.Loading())
        try {
            // Check for existing active patrol logic to prevent duplicates
            val existingPatrol = patrolDao.getActivePatrolSync()
            if (existingPatrol != null) {
                emit(Resource.Success(existingPatrol))
                return@flow
            }

            // Optimistic Local Creation
            val patrol = PatrolEntity(
                siteId = 1, // TODO: Get from Template
                templateId = templateId,
                status = "started",
                startTime = System.currentTimeMillis()
            )
            patrolDao.insertPatrol(patrol)

            // API Call
            val response = api.startPatrol(StartPatrolRequest(templateId))
            if (response.isSuccessful && response.body() != null) {
                // Update with Remote ID
                val updatedPatrol = patrol.copy(remoteId = response.body()!!.runId)
                patrolDao.insertPatrol(updatedPatrol)
                emit(Resource.Success(updatedPatrol))
            } else {
                // Queue for Sync (Fallback)
                val log = LogEntity(
                    type = "START_PATROL",
                    payload = Gson().toJson(StartPatrolRequest(templateId)),
                    priority = 2,
                    synced = false
                )
                logDao.insertLog(log)
                emit(Resource.Success(patrol)) // Proceed locally
            }
        } catch (e: Exception) {
            // Queue for Sync
             val log = LogEntity(
                type = "START_PATROL",
                payload = Gson().toJson(StartPatrolRequest(templateId)),
                priority = 2,
                synced = false
            )
            logDao.insertLog(log)
            emit(Resource.Success(
                PatrolEntity(
                    siteId = 1, 
                    templateId = templateId,
                    status = "started", 
                    startTime = System.currentTimeMillis()
                )
            ))
        }
    }
    
    override suspend fun scanCheckpoint(runId: Int, checkpointId: Int, lat: Double, lng: Double): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        
        try {
             val request = ScanRequest(runId, checkpointId, LocationDto(lat, lng))
             val response = api.scanCheckpoint(request)
             
             if (response.isSuccessful) {
                 emit(Resource.Success("Scanned Successfully"))
             } else {
                 emit(Resource.Error("Scan Rejected: " + response.message()))
             }
        } catch (e: Exception) {
             // Offline: Queue
             val log = LogEntity(
                type = "SCAN_CHECKPOINT",
                payload = Gson().toJson(ScanRequest(runId, checkpointId, LocationDto(lat, lng))),
                priority = 2,
                synced = false
            )
            logDao.insertLog(log)
            emit(Resource.Success("Scanned (Offline)"))
        }
    }

    override fun getActivePatrol(): Flow<PatrolEntity?> {
        return patrolDao.getActivePatrol()
    }

    override suspend fun endPatrol(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val activePatrol = patrolDao.getActivePatrolSync()
            if (activePatrol != null) {
                // 1. Optimistic Update (Local) - NUKE ALL
                patrolDao.completeAllActivePatrols(System.currentTimeMillis())
                
                // 2. Queue for Background Sync
                if (activePatrol.remoteId != null) {
                    logDao.insertLog(LogEntity(
                        type = "END_PATROL",
                        payload = Gson().toJson(com.patrolshield.data.remote.dto.EndPatrolRequest(activePatrol.remoteId)),
                        priority = 2,
                        synced = false
                    ))
                }
                
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Success(Unit))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error: " + e.message))
        }
    }

    override suspend fun getCheckpoints(templateId: Int): List<com.patrolshield.data.local.entities.CheckpointEntity> {
        return patrolDao.getCheckpointsForTemplate(templateId)
    }

    override suspend fun sendPanic(lat: Double?, lng: Double?, runId: Int?): Resource<Unit> {
        val location = if (lat != null && lng != null) LocationDto(lat, lng) else null
        val request = com.patrolshield.data.remote.dto.PanicRequest(location, runId)
        
        return try {
            val response = api.triggerPanic(request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                // Queue anyway on server error? 
                Resource.Error("Server error")
            }
        } catch (e: Exception) {
            // Offline: Queue with Critical Priority
            logDao.insertLog(LogEntity(
                type = "PANIC_ALERT",
                payload = Gson().toJson(request),
                priority = 1,
                synced = false
            ))
            Resource.Success(Unit) // Consider success locally
        }
    }

    override suspend fun getCompletedPatrols(): List<PatrolEntity> {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return patrolDao.getCompletedPatrolsAfter(calendar.timeInMillis)
    }

    override suspend fun sendHeartbeat(lat: Double, lng: Double, activeRunId: Int?) {
        val request = com.patrolshield.data.remote.dto.HeartbeatRequest(lat, lng, activeRunId)
        try {
            api.sendHeartbeat(request)
        } catch (e: Exception) {
            // Offline: Cache Heartbeat with Low Priority
            logDao.insertLog(LogEntity(
                type = "HEARTBEAT",
                payload = Gson().toJson(request),
                priority = 4,
                synced = false
            ))
        }
    }
}
