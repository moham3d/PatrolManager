package com.patrolshield.data.repository

import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.AdminStatsDto
import com.patrolshield.domain.repository.AdminRepository
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val api: ApiService
) : AdminRepository {

    override suspend fun getStats(): Result<AdminStatsDto> {
        return try {
            val response = api.getAdminStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch stats: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
