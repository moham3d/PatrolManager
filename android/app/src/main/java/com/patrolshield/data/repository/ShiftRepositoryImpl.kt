package com.patrolshield.data.repository

import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.ClockInRequest
import com.patrolshield.data.remote.dto.ClockOutRequest
import com.patrolshield.data.remote.dto.ShiftDto
import com.patrolshield.domain.repository.ShiftRepository
import com.patrolshield.data.local.dao.UserDao
import javax.inject.Inject

class ShiftRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val userDao: UserDao
) : ShiftRepository {

    override suspend fun clockIn(siteId: Int, lat: Double, lng: Double): Result<ShiftDto> {
        return try {
            val response = api.clockIn(ClockInRequest(siteId, lat, lng))
            if (response.isSuccessful && response.body() != null) {
                val shift = response.body()!!.shift
                // Update Local User State
                val user = userDao.getUser()
                if (user != null) {
                    val updatedUser = user.copy(activeShiftId = shift.id)
                    userDao.insertUser(updatedUser)
                }
                Result.success(shift)
            } else {
                Result.failure(Exception("Clock-in failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clockOut(lat: Double, lng: Double): Result<Unit> {
        return try {
            val response = api.clockOut(ClockOutRequest(lat, lng))
            if (response.isSuccessful) {
                // Clear Local Shift State
                val user = userDao.getUser()
                if (user != null) {
                    val updatedUser = user.copy(activeShiftId = null)
                    userDao.insertUser(updatedUser)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Clock-out failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
