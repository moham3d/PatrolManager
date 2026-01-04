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
class PatrolRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val shiftDao: ShiftDao
) : PatrolRepository {

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
        return try {
            val response = apiService.getMySchedule()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to fetch schedule: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.localizedMessage}")
        }
    }

    override suspend fun startPatrol(patrolId: Int): Resource<Int> {
        return try {
            val response = apiService.startPatrol(StartPatrolRequest(patrolId))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to start patrol: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.localizedMessage}")
        }
    }

    override suspend fun scanCheckpoint(runId: Int, checkpointId: Int, value: String, lat: Double?, lng: Double?): Resource<Unit> {
        return try {
            val response = apiService.scanCheckpoint(ScanRequest(runId, checkpointId, value, lat, lng))
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Scan failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.localizedMessage}")
        }
    }

    override suspend fun endPatrol(runId: Int): Resource<Unit> {
        return try {
            val response = apiService.endPatrol(runId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to end patrol: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.localizedMessage}")
        }
    }

    override suspend fun triggerPanic(lat: Double, lng: Double, runId: Int?): Resource<Unit> {
        return try {
            val response = apiService.triggerPanic(PanicRequest(lat, lng, runId))
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Panic trigger failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.localizedMessage}")
        }
    }
}
