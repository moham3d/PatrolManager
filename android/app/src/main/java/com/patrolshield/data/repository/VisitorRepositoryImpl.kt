package com.patrolshield.data.repository

import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.VisitorDto
import com.patrolshield.domain.repository.VisitorRepository
import javax.inject.Inject

class VisitorRepositoryImpl @Inject constructor(
    private val api: ApiService
) : VisitorRepository {

    override suspend fun getVisitorsToday(): Result<List<VisitorDto>> {
        return try {
            val response = api.getVisitorsToday()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error fetching visitors: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkInVisitor(id: Int): Result<VisitorDto> {
        return try {
            val response = api.checkInVisitor(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error checking in visitor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
