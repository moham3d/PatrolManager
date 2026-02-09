package com.patrolshield.data.repository

import com.patrolshield.common.Resource
import com.patrolshield.data.remote.ApiService
import com.patrolshield.data.remote.dto.ManagerStatsDto
import com.patrolshield.domain.repository.ManagerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ManagerRepositoryImpl @Inject constructor(
    private val api: ApiService
) : ManagerRepository {

    override suspend fun getStats(): Flow<Resource<ManagerStatsDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getManagerStats()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Failed to fetch stats: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network Error: ${e.message}"))
        }
    }
}
